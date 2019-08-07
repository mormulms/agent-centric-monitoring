package org.grafana.plugin;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.mapper.model.Alert;
import org.mapper.model.AlertCondition;
import org.mapper.model.AlertConditionCombinator;
import org.mapper.model.AlertConditionField;
import org.mapper.model.AlertConditionFilter;
import org.mapper.model.AlertConditionGroupBy;
import org.mapper.model.AlertConditionTimeWindow;
import org.mapper.model.AlertTreeNode;
import org.mapper.model.AlertTreeNodeOperator;
import org.mapper.model.AlertTreeNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appnexus.grafana.client.models.DashboardGraphPanel;
import com.appnexus.grafana.client.models.DashboardPanel;
import com.appnexus.grafana.client.models.DashboardPanelAlertCondition;
import com.appnexus.grafana.client.models.DashboardPanelAlertConditionEvaluator;
import com.appnexus.grafana.client.models.DashboardPanelAlertConditionOperator.Type;
import com.appnexus.grafana.client.models.DashboardPanelTarget;
import com.appnexus.grafana.client.models.DashboardPanelTargetGroupBy;
import com.appnexus.grafana.client.models.DashboardPanelTargetSelect;
import com.appnexus.grafana.client.models.DashboardPanelTargetTag;
import com.appnexus.grafana.client.models.Datasource;
import com.appnexus.grafana.client.models.GrafanaDashboard;

public class GrafanaAlertUtility {

	private static final String CLOSE_BRACKET = "D";
	private static final String OPEN_BRACKET = "C";
	private static final String CONDITION_SEPARATOR = "Condition";
	private static final String HOST_ADDRESS = "host_address";
	private static final String INFLUX_DB_EVENT = "InfluxDBEvent";
	private static final int REF_ID_POSITION = 0;

	private static GrafanaAlertUtility instance;
	private static Logger LOGGER = LoggerFactory.getLogger(GrafanaAlertUtility.class);

	public GrafanaAlertUtility() {
		super();
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
	}

	public List<Alert> getAlertsFromDashboard(GrafanaDashboard grafanaDashboard, String hostaddress,
			List<Datasource> datasources) {
		LOGGER.info("getAlertsFromDashboard()");
		List<Alert> alerts = new LinkedList<Alert>();
		for (DashboardGraphPanel panel : getGraphPanelsFromDashboard(grafanaDashboard)) {
			alerts.add(getAlertFromGraphPanel(grafanaDashboard.dashboard().uid(), panel, hostaddress, datasources));

		}
		LOGGER.info("getAlertsFromDashboard() - " + alerts.size() + " Alert(s) converted");
		return alerts;
	}

	private Alert getAlertFromGraphPanel(String dashboardUid, DashboardGraphPanel panel, String hostaddress,
			List<Datasource> datasources) {
		LOGGER.info("getAlertFromGraphPanel()");
		
		//TODO rename?
		Datasource datasource = getDatasourceForGraphPanel(panel, datasources);
		String alertId = dashboardUid + Alert.ALERT_ID_SEPARATOR + panel.id();

		// TODO naming
		List<AlertTreeNode> nodeList = generateAlertTreeNodeListFromGraphPanel(alertId, panel, datasource);
		AlertTreeNode alertTree = generateAlertTreeFromList(nodeList);
//				generateAlertTreeNodeListFromGraphPanel(alertId, panel, datasource));

		Alert alert = new Alert(alertId, hostaddress, datasource.url(), nodeList.size(), alertTree);
		LOGGER.info("getAlertFromGraphPanel() - " + "alertId: " + alert.getAlertId());
		return alert;
	}

	// in grafana conditions are executed serially
	private AlertTreeNode generateAlertTreeFromList(List<AlertTreeNode> nodeList) {
		LOGGER.info("generateAlertTreeFromList(listSize: "+nodeList.size()+")");
		if (nodeList.size() > 1) {
			// combine the first two nodes the first operator can be omitted
			AlertTreeNode root = nodeList.get(1);
			root.getChildren().add(0, nodeList.get(0).getChildren().get(0));
			root.setTreeName(generateNodeName(root));

			for (int i = 2; i < nodeList.size(); i++) {
				AlertTreeNode currentNode = nodeList.get(i);
				currentNode.getChildren().add(root);
				currentNode.setTreeName(generateNodeName(currentNode));
				root = currentNode;
			}
			LOGGER.info("generateAlertTreeFromList(listSize: "+nodeList.size()+") - generated tree");
			return root;
		} else if (nodeList.size() == 1) {
			LOGGER.info("generateAlertTreeFromList(listSize: "+nodeList.size()+") - first element");
			return nodeList.get(0);
		} else {
			LOGGER.info("generateAlertTreeFromList(listSize: "+nodeList.size()+") - null");
			return null;
		}
	}

	//only work for grafana style alerts
	private String generateNodeName(AlertTreeNode node) {
		
		if (node.getType() == AlertTreeNodeType.CONDITION) {
			return ((AlertCondition) node).getName();
		} else if(node.getType() == AlertTreeNodeType.OPERATOR){
			List<AlertTreeNode> children = node.getChildren();
			StringBuilder builder = new StringBuilder();
			//alert prefix
			builder.append(children.get(0).getTreeName().split(CONDITION_SEPARATOR)[0]);
			builder.append(CONDITION_SEPARATOR);
			builder.append(OPEN_BRACKET);
			for (int i=0;i<children.size()-1;i++) {
				builder.append(children.get(i).getTreeName().split(CONDITION_SEPARATOR)[1]);
				builder.append(((AlertTreeNodeOperator) node).getOperator().toString());
			}
			builder.append(children.get(children.size()-1).getTreeName().split(CONDITION_SEPARATOR)[1]);
			builder.append(CLOSE_BRACKET);
			
			return builder.toString();
		}
		return null;
	}
		
	private List<AlertTreeNode> generateAlertTreeNodeListFromGraphPanel(String alertId, DashboardGraphPanel panel,
			Datasource datasource) {
		LOGGER.info("generateAlertTreeFromGraphPanel()");
		List<AlertTreeNode> conditionsWithOperators = new LinkedList<AlertTreeNode>();
		Map<String, String> measurementToHostAddressMapping = extractHostAddressToMeasurementMappingFromPanel(panel);

		int currentAlertCondition = 1;
		for (DashboardPanelAlertCondition panelAlertCondition : panel.alert().conditions()) {
			for (DashboardPanelTarget target : panel.targets()) {
				List<String> params = panelAlertCondition.query().params();
				if (!params.isEmpty() && target.refId().contentEquals(params.get(REF_ID_POSITION))) {

					List<String> measurementSources;
					String source;
					//TODO fix?
					if ((source = measurementToHostAddressMapping.get(target.refId())) != null) {
						measurementSources = new LinkedList<String>();
						measurementSources.add(source);
					} else {
						measurementSources = getMeasurementSourcesFromInfluxDB(target.measurement(), datasource);
					}

					AlertCondition alertCondition = new AlertCondition();
					alertCondition.setMeasurementSources(measurementSources);
					alertCondition.setEventType(INFLUX_DB_EVENT);
					alertCondition.setName(alertId + CONDITION_SEPARATOR + currentAlertCondition);
					alertCondition.setQueryName(target.refId());
					alertCondition.setMeasurement(target.measurement());

					// selects from targets
					LinkedList<AlertConditionField> selects = new LinkedList<AlertConditionField>();
					for (List<DashboardPanelTargetSelect> targetSelects : target.select()) {
						String field = null;
						String function = null;
						for (DashboardPanelTargetSelect targetSelect : targetSelects) {
							if (targetSelect.type() == DashboardPanelTargetSelect.Type.FIELD) {
								field = targetSelect.params().get(0);
							} else if (targetSelect.type() != null) {
								function = targetSelect.type().value();
							}
						}
						selects.add(new AlertConditionField(function, field));
					}
					alertCondition.setSelects(selects);

					// filters from tags
					LinkedList<AlertConditionFilter> wheres = new LinkedList<AlertConditionFilter>();
					for (DashboardPanelTargetTag tag : target.tags()) {
						wheres.add(new AlertConditionFilter(tag.key(), tag.operator(), tag.value()));
					}
					alertCondition.setWheres(wheres);

					// groupbys
					LinkedList<AlertConditionGroupBy> groupBys = new LinkedList<AlertConditionGroupBy>();
					for (DashboardPanelTargetGroupBy groupBy : target.groupBy()) {
						groupBys.add(new AlertConditionGroupBy(groupBy.params().get(0), groupBy.type().value()));
					}
					alertCondition.setGroupBys(groupBys);

					// filters from alert
					// AlertAggregator
					alertCondition.setTimewindow(new AlertConditionTimeWindow(params.get(1), params.get(2)));
					alertCondition.setAlertAggregator(panelAlertCondition.reducer().type().value());
					alertCondition.setAlertCondition(createAlertConditionFilter(panelAlertCondition.evaluator()));
					
					alertCondition.setTreeName(generateNodeName(alertCondition));
					List<AlertTreeNode> condition = new LinkedList<AlertTreeNode>();
					condition.add(alertCondition);

					AlertTreeNodeOperator conditionWithOperator = new AlertTreeNodeOperator(
							createAlertConditonCombinator(panelAlertCondition.operator().type()), condition);
					conditionWithOperator.setTreeName(generateNodeName(conditionWithOperator));
					conditionsWithOperators.add(conditionWithOperator);
					break;
				}
			}
			currentAlertCondition++;
		}
		LOGGER.info(
				"generateAlertTreeFromGraphPanel() - " + conditionsWithOperators.size() + " AlertCondition(s) found");
		return conditionsWithOperators;
	}

	//TODO what if a target has mutliple host addresses?
	private Map<String, String> extractHostAddressToMeasurementMappingFromPanel(DashboardGraphPanel panel) {

		Map<String, String> hostAddressToMeasurementMapping = new LinkedHashMap<String, String>();

		for (DashboardPanelTarget target : panel.targets()) {
			for (DashboardPanelTargetTag tag : target.tags()) {
				if (tag.key().equals(HOST_ADDRESS)) {
					hostAddressToMeasurementMapping.put(target.refId(), tag.value());
				}
			}
		}

		return hostAddressToMeasurementMapping;
	}

	private AlertConditionCombinator createAlertConditonCombinator(Type type) {
		switch (type) {
		case AND:
			return AlertConditionCombinator.AND;
		case OR:
			return AlertConditionCombinator.OR;
		default:
			return null;
		}
	}

	private AlertConditionFilter createAlertConditionFilter(DashboardPanelAlertConditionEvaluator evaluator) {
		List<String> values = new LinkedList<String>();
		for (Double param : evaluator.params()) {
			values.add(param.toString());
		}

		return new AlertConditionFilter(null, evaluator.type().value(), values);
	}

	private List<String> getMeasurementSourcesFromInfluxDB(String measurement, Datasource datasource) {
		LOGGER.info("getMeasurementSource() - start");
		// TODO change to work with other auth
		InfluxDBInstance influxDBInstance = new InfluxDBInstance(datasource.url());
		List<String> measurementSources = influxDBInstance.getMeasurementSources(measurement, datasource.database());
		LOGGER.info("getMeasurementSource() - end");
		return measurementSources;
	}

	private List<DashboardGraphPanel> getGraphPanelsFromDashboard(GrafanaDashboard grafanaDashboard) {
		LOGGER.info("getGraphPanelsFromDashboard()");
		List<DashboardGraphPanel> graphPanels = new LinkedList<DashboardGraphPanel>();
		for (DashboardPanel dashboardPanel : grafanaDashboard.dashboard().panels()) {
			if (dashboardPanel.type() == DashboardPanel.Type.GRAPH) {
				graphPanels.add((DashboardGraphPanel) dashboardPanel);
			}
		}
		LOGGER.info("getGraphPanelsFromDashboard() - " + graphPanels.size() + " DashboardGraphPanel(s) found");
		return graphPanels;
	}

	private Datasource getDatasourceForGraphPanel(DashboardGraphPanel panel, List<Datasource> datasources) {
		LOGGER.info("getDatasourceForGraphPanel()");
		for (Datasource datasource : datasources) {
			if (datasource.name().equals(panel.datasource())) {
				LOGGER.info("getDatasourceForGraphPanel() - " + "found at: " + datasource.url());
				return datasource;
			}
		}
		LOGGER.info("getDatasourceForGraphPanel() - " + "not found");
		return null;
	}

	public static GrafanaAlertUtility getInstance() {
		if (instance == null) {
			instance = new GrafanaAlertUtility();
		}
		return instance;
	}

}
