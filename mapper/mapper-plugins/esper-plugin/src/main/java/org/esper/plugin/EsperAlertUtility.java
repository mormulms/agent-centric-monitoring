package org.esper.plugin;

import java.util.List;

import org.mapper.model.AlertCondition;
import org.mapper.model.AlertConditionField;
import org.mapper.model.AlertConditionFilter;
import org.mapper.model.AlertConditionGroupBy;
import org.mapper.model.AlertConditionTimeWindow;
import org.mapper.model.AlertTreeNode;
import org.mapper.model.AlertTreeNodeOperator;
import org.mapper.model.AlertTreeNodeType;

public class EsperAlertUtility {

	private static final String OR = "OR";
	private static final String INFLUX_DB_EVENT = "InfluxDBEvent";
	private static final String TIME = "#time";
	private static final String FROM = "FROM";
	private static final String GROUP_BY = "GROUP BY";
	private static final String WHERE = "WHERE";
	private static final String SELECT = "SELECT";

	// in grafana only influxdb tags can be used to filter tags are alwasy strings
	public static String tagFilterToEsperStatementComponent(AlertConditionFilter filter) {
		StringBuilder builder = new StringBuilder();

		builder.append("tags('");
		builder.append(filter.getField());
		builder.append("') ");
		builder.append(filter.getOperator());
		builder.append(" ");
		builder.append("'");
		builder.append(filter.getValue());
		builder.append("'");

		return builder.toString();
	}

	public static String tagFiltersToEsperStatementComponent(List<AlertConditionFilter> filters) {
		StringBuilder builder = new StringBuilder();

		for (AlertConditionFilter alertConditionFilter : filters) {
			builder.append(tagFilterToEsperStatementComponent(alertConditionFilter));
			if (alertConditionFilter.getFilterCombinator() != null) {
				builder.append(" ");
				builder.append(alertConditionFilter.getFilterCombinator());
			}
		}

		return builder.toString();
	}

	// in grafana selects can only be on fields
	// TODO change to Other Data Format
	public static String fieldToEsperStatementComponent(AlertConditionField field) {
		StringBuilder builder = new StringBuilder();

		if (field.getFunction() != null) {
			builder.append(field.getFunction());
			builder.append("(");
		}
		builder.append("cast(fields('");
		builder.append(field.getField());
		builder.append("'),BigDecimal)");
		if (field.getFunction() != null) {
			builder.append(")");
		}

		return builder.toString();
	}

	// in grafana group by is only on tags
	public static String groupByToEsperStatementComponent(AlertConditionGroupBy groupBy) {
		StringBuilder builder = new StringBuilder();

		builder.append("tags('");
		builder.append(groupBy.getField());
		builder.append("')");

		return builder.toString();
	}

	public static String timeWindowToEsperStatementComponent(AlertConditionTimeWindow window) {
		StringBuilder builder = new StringBuilder();

		builder.append(TIME);
		builder.append("(");
		builder.append(generateEsperTimePeriod(window.getDuration()));
		builder.append(")");

		return builder.toString();
	}

	private static String generateEsperTimePeriod(String duration) {
		switch (duration.charAt(duration.length() - 1)) {
		case 's':
			return duration.substring(0, duration.length() - 1) + " sec";
		case 'm':
			return duration.substring(0, duration.length() - 1) + " min";
		case 'h':
			return duration.substring(0, duration.length() - 1) + " hour";
		default:
			return duration;
		}
	}

	public static String selectSubqueryToEsperStatementComponent(String alertAggregator, AlertConditionField select,
			String measurement, AlertConditionTimeWindow window, List<AlertConditionFilter> wheres,
			List<AlertConditionGroupBy> groupBys, AlertConditionFilter alertCondition) {
		StringBuilder builder = new StringBuilder();

		// part of condition
		builder.append("(");

		// SELECT
		builder.append(SELECT);
		builder.append(" ");
		if (alertAggregator != null) {
			builder.append(alertAggregator);
			builder.append("(");
		}
		builder.append(fieldToEsperStatementComponent(select));
		if (alertAggregator != null) {
			builder.append(")");
		}
		builder.append(" ");

		// FROM
		builder.append(FROM);
		builder.append(" ");
		builder.append(INFLUX_DB_EVENT);
		builder.append("(");
		builder.append("measurement = '" + measurement + "'");
		builder.append(")");
		if (window != null) {
			builder.append(timeWindowToEsperStatementComponent(window));
		}

		// WHERE
		if (wheres != null && !wheres.isEmpty()) {
			builder.append(" ");
			builder.append(WHERE);
			builder.append(" ");
			builder.append(tagFiltersToEsperStatementComponent(wheres));
		}

		// GROUP BY
		if (groupBys != null && !groupBys.isEmpty()) {
			builder.append(" ");
			builder.append(GROUP_BY);
			builder.append(" ");
			for (AlertConditionGroupBy groupBy : groupBys) {
				builder.append(groupByToEsperStatementComponent(groupBy));
				builder.append(",");
			}
			builder.deleteCharAt(builder.length() - 1);
		}

		// part of condition
		builder.append(")");
		builder.append(" ");
		builder.append(conditionToEsperStatementComponent(alertCondition));

		return builder.toString();
	}

	private static String conditionToEsperStatementComponent(AlertConditionFilter alertCondition) {

		switch (alertCondition.getOperator()) {
		case "gt":
			return "> " + alertCondition.getValue();
		case "lt":
			return "< " + alertCondition.getValue();
		case "outside_range":
			return "NOT BETWEEN " + alertCondition.getValues().get(0) + " AND " + alertCondition.getValues().get(1);
		case "within_range":
			return "BETWEEN " + alertCondition.getValues().get(0) + " AND " + alertCondition.getValues().get(1);
		default:
			return "operator not supported";
		}
	}

	public static String selectSubquerysToEsperStatementComponent(String alertAggregator,
			List<AlertConditionField> selects, String measurement, AlertConditionTimeWindow window,
			List<AlertConditionFilter> wheres, List<AlertConditionGroupBy> groupBys,
			AlertConditionFilter alertCondition) {
		StringBuilder builder = new StringBuilder();

		for (AlertConditionField select : selects) {
			builder.append(selectSubqueryToEsperStatementComponent(alertAggregator, select, measurement, window, wheres,
					groupBys, alertCondition));
			builder.append(" ");
			builder.append(OR);
			builder.append(" ");
		}
		builder.delete(builder.length() - 4, builder.length());
		
		return builder.toString();
	}

	public static String alertConditionToEsperStatement(AlertCondition condition) {
		StringBuilder builder = new StringBuilder();

		builder.append(SELECT);
		builder.append(" ");
		builder.append("*");
		builder.append(" ");
		builder.append(FROM);
		builder.append(" ");
		builder.append(INFLUX_DB_EVENT);
		builder.append(" ");
		builder.append(WHERE);
		builder.append(" ");
		builder.append(selectSubquerysToEsperStatementComponent(condition.getAlertAggregator(), condition.getSelects(),
				condition.getMeasurement(), condition.getTimewindow(), condition.getWheres(), condition.getGroupBys(),
				condition.getAlertCondition()));

		return builder.toString();
	}

	public static String alertConditionToEsperStatementComponent(AlertCondition condition) {

		return selectSubquerysToEsperStatementComponent(condition.getAlertAggregator(), condition.getSelects(),
				condition.getMeasurement(), condition.getTimewindow(), condition.getWheres(), condition.getGroupBys(),
				condition.getAlertCondition());

	}

	public static String alertTreeToEsperStatement(AlertTreeNode root) {

		StringBuilder builder = new StringBuilder();

		builder.append(SELECT);
		builder.append(" ");
		builder.append("*");
		builder.append(" ");
		builder.append(FROM);
		builder.append(" ");
		builder.append(INFLUX_DB_EVENT);
		builder.append(" ");
		builder.append(WHERE);
		builder.append(" ");
		builder.append(alertTreeSubquerysToEsperStatementComponent(root));

		return builder.toString();
	}

	private static String alertTreeSubquerysToEsperStatementComponent(AlertTreeNode node) {
		if (node.getType() == AlertTreeNodeType.CONDITION) {
			return alertConditionToEsperStatementComponent((AlertCondition) node);
		} else {
			AlertTreeNodeOperator operatorNode = (AlertTreeNodeOperator) node;
				
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			for (AlertTreeNode child : node.getChildren()) {
				builder.append(alertTreeSubquerysToEsperStatementComponent(child));
				builder.append(" ");
				builder.append(operatorNode.getOperator().toString());
				builder.append(" ");
			}
			builder.delete(builder.length() - 4, builder.length());
			builder.append(")");
			return builder.toString();
		}
	}

}
