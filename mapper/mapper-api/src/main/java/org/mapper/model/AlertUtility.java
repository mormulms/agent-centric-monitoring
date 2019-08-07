package org.mapper.model;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertUtility {

	private static final String NOW = "now";
	private static final String HOURS = "h";
	private static final String MINUTES = "m";
	private static final String SECONDS = "s";
	private static final String MINUS = "-";
	private static final String MAX = "max";
	private static final String ZERO = "0";
	private static final String GREATER_THAN = "gt";
	private static final String CONDITION = "Condition";
	private static Logger LOGGER = LoggerFactory.getLogger(AlertUtility.class);
	private static AlertUtility alertUtility;

	public static AlertUtility instance() {
		if (alertUtility == null) {
			return alertUtility = new AlertUtility();
		} else {
			return alertUtility;
		}
	}

	private AlertUtility() {
		super();
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
	}

	public List<AlertCondition> getAlertConditionsFromAlert(Alert alert) {
		List<AlertCondition> alertConditions = new LinkedList<AlertCondition>();

		getAlertConditionsFromAlertTree(alert.getAlertTree(), alertConditions);

		return alertConditions;
	}

	public List<AlertCondition> getAlertConditionsFromAlertTree(AlertTreeNode root) {
		List<AlertCondition> alertConditions = new LinkedList<AlertCondition>();

		getAlertConditionsFromAlertTree(root, alertConditions);

		return alertConditions;
	}

	private void getAlertConditionsFromAlertTree(AlertTreeNode node, List<AlertCondition> alertConditions) {
		if (node.getType() == AlertTreeNodeType.CONDITION) {
			alertConditions.add((AlertCondition) node);
		} else {
			for (AlertTreeNode child : node.getChildren()) {
				getAlertConditionsFromAlertTree(child, alertConditions);
			}
		}

	}

	public List<AlertTreeNode> getLocallyPlaceableAlertTrees(Alert alert) {
		LOGGER.info("getLocallyPlaceableAlertTrees(" + alert.getAlertId() + ")");
		List<AlertTreeNode> alertTrees = new LinkedList<AlertTreeNode>();

		optimizeAlertTree(alert.getAlertTree());
		getLocallyPlaceableAlertTrees(alertTrees, alert.getAlertTree(), true);

		LOGGER.info("getLocallyPlaceableAlertTrees(" + alert.getAlertId() + ") - finished");
		return alertTrees;
	}

	private void getLocallyPlaceableAlertTrees(List<AlertTreeNode> alertTrees, AlertTreeNode node,
			boolean createNewRootNode) {
		LOGGER.info("getLocallyPlaceableAlertTrees(" + node.getTreeName() + ", listSize: " + alertTrees.size() + ")");
		LOGGER.info("deployOn:" + node.getDeployOn());
		if (!node.getDeployOn().isEmpty()) {
			List<AlertTreeNode> children = node.getChildren();
			if (createNewRootNode) {
				node = node.getCopyWithoutChildren();
			}
			if (children != null) {
				List<AlertTreeNode> localTreeChildren = new LinkedList<AlertTreeNode>();
				for (AlertTreeNode child : children) {
					if (!child.getDeployOn().isEmpty() && !child.getDeployOn().equals(node.getDeployOn())) {
						AlertCondition childProxyNode = generateProxyAlertConditionFromNode(child);
						localTreeChildren.add(childProxyNode);
						getLocallyPlaceableAlertTrees(alertTrees, child, true);
					} else {
						localTreeChildren.add(child);
						getLocallyPlaceableAlertTrees(alertTrees, child, false);
					}
				}
				node.setChildren(localTreeChildren);
			}
			if (createNewRootNode) {
				alertTrees.add(node);
			}
		} else {
			for (AlertTreeNode child : node.getChildren()) {
				getLocallyPlaceableAlertTrees(alertTrees, child, true);
			}
		}
		LOGGER.info("getLocallyPlaceableAlertTrees("+node.getTreeName()+", listSize: "+alertTrees.size()+") - finished");
	}

	private AlertCondition generateProxyAlertConditionFromNode(AlertTreeNode node) {
		LOGGER.info("generateAlertConditionFromNode(" + node.getTreeName() + ")");
		AlertCondition alertCondition = new AlertCondition();

		alertCondition.setProxyCondition(true);
		alertCondition.setAlertAggregator(MAX);

		List<AlertConditionField> selects = new LinkedList<AlertConditionField>();
		selects.add(new AlertConditionField(null, node.getTreeName()));
		alertCondition.setSelects(selects);

		alertCondition.setMeasurement(node.getTreeName().split(CONDITION)[0]);
		List<String> measurementSources = new LinkedList<String>();
		measurementSources.add(node.deployOn);
		alertCondition.setMeasurementSources(measurementSources);

		alertCondition.setTimewindow(getTimewindowFromAlertTree(node));

		alertCondition.setAlertCondition(new AlertConditionFilter(node.getTreeName(), GREATER_THAN, ZERO));

		LOGGER.info("generateAlertConditionFromNode(" + node.getTreeName() + ") - finished");
		return alertCondition;
	}

	private AlertConditionTimeWindow getTimewindowFromAlertTree(AlertTreeNode node) {
		if (node.getType() == AlertTreeNodeType.CONDITION) {
			return node.getTimewindow();
		} else {
			List<AlertConditionTimeWindow> childTimeWindows = new LinkedList<AlertConditionTimeWindow>();
			for (AlertTreeNode child : node.getChildren()) {
				childTimeWindows.add(getTimewindowFromAlertTree(child));
			}
			AlertTreeNodeOperator operatorNode = (AlertTreeNodeOperator) node;
			if (operatorNode.getOperator() == AlertConditionCombinator.AND) {
				return timewindowDisjunction(childTimeWindows);
			} else {
				return timewindowConjunction(childTimeWindows);
			}
		}
	}

	private AlertConditionTimeWindow timewindowConjunction(List<AlertConditionTimeWindow> childTimeWindows) {
		int durationInSeconds = getTimeInSeconds(childTimeWindows.get(0).getDuration());
		int endOffsetInseconds = getEndOffsetInSecondsFromNow(childTimeWindows.get(0).getEnd());
		int windowStartInsSecondsFromNow = endOffsetInseconds - durationInSeconds;
		for (int i = 1; i < childTimeWindows.size(); i++) {
			AlertConditionTimeWindow tmp = childTimeWindows.get(i);
			int tmpDurationInSeconds = getTimeInSeconds(tmp.getDuration());
			int tmpEndOffsetInseconds = getEndOffsetInSecondsFromNow(tmp.getEnd());
			int tmpWindowStartInsSecondsFromNow = tmpEndOffsetInseconds - tmpDurationInSeconds;

			// choose the first beginning startpoint
			if (tmpWindowStartInsSecondsFromNow < windowStartInsSecondsFromNow) {
				windowStartInsSecondsFromNow = tmpWindowStartInsSecondsFromNow;
//				durationInSeconds = tmpDurationInSeconds;
			}

			if (tmpEndOffsetInseconds > endOffsetInseconds) {
				endOffsetInseconds = tmpEndOffsetInseconds;
			}
		}
		String duration = generateDurationString((windowStartInsSecondsFromNow - endOffsetInseconds) * -1);
		String end = generateEndString(endOffsetInseconds);
		return new AlertConditionTimeWindow(duration, end);
	}

	private String generateEndString(int endOffsetInseconds) {
		if (endOffsetInseconds == 0) {
			return NOW;
		} else {
			return NOW + "-" + getTimeStringFromSeconds(endOffsetInseconds);
		}
	}

	private String getTimeStringFromSeconds(int endOffsetInSeconds) {
		System.out.println("time in seconds: "+ endOffsetInSeconds);
		if (endOffsetInSeconds % 60 == 0) {
			int endOffsetInMinutes = endOffsetInSeconds / 60;
			if (endOffsetInMinutes % 60 == 0) {
				int endOffsetInHours = endOffsetInMinutes / 60;
				return endOffsetInHours + HOURS + "";
			}
			return endOffsetInMinutes + MINUTES + "";
		}
		return endOffsetInSeconds + SECONDS + "";
	}

	private String generateDurationString(int durationInSeconds) {
		return getTimeStringFromSeconds(durationInSeconds);
	}

	private int getEndOffsetInSecondsFromNow(String end) {
		String[] splitEnd = end.split(MINUS);
		if (splitEnd.length == 2) {
			return getTimeInSeconds(splitEnd[1]) * -1;
		} else {
			return 0;
		}
	}

	private int getTimeInSeconds(String time) {
		String timeunit = time.charAt(time.length() - 1)+ "";
		int value = Integer.parseInt(time.substring(0, time.length() - 1));

		switch (timeunit) {
		case MINUTES:
			value = value * 60;
			break;
		case HOURS:
			value = value * 60 * 60;
			break;
		}
		return value;
	}

	private AlertConditionTimeWindow timewindowDisjunction(List<AlertConditionTimeWindow> childTimeWindows) {
		int durationInSeconds = getTimeInSeconds(childTimeWindows.get(0).getDuration());
		int endOffsetInseconds = getEndOffsetInSecondsFromNow(childTimeWindows.get(0).getEnd());
		int windowStartInsSecondsFromNow = endOffsetInseconds - durationInSeconds;
		for (int i = 1; i < childTimeWindows.size(); i++) {
			AlertConditionTimeWindow tmp = childTimeWindows.get(i);
			int tmpDurationInSeconds = getTimeInSeconds(tmp.getDuration());
			int tmpEndOffsetInseconds = getEndOffsetInSecondsFromNow(tmp.getEnd());
			int tmpWindowStartInsSecondsFromNow = tmpEndOffsetInseconds - tmpDurationInSeconds;

			// choose the first beginning startpoint
			if (tmpWindowStartInsSecondsFromNow > windowStartInsSecondsFromNow) {
				windowStartInsSecondsFromNow = tmpWindowStartInsSecondsFromNow;
//				durationInSeconds = tmpDurationInSeconds;
			}

			if (tmpEndOffsetInseconds < endOffsetInseconds) {
				endOffsetInseconds = tmpEndOffsetInseconds;
			}
		}
		String duration = generateDurationString((windowStartInsSecondsFromNow - endOffsetInseconds) * -1);
		String end = generateEndString(endOffsetInseconds);
		return new AlertConditionTimeWindow(duration, end);
	}

	private void optimizeAlertTree(AlertTreeNode node) {
		LOGGER.info("optimizeAlertTree(" + node.getTreeName() + ")");
		if (node.getType() == AlertTreeNodeType.OPERATOR) {

			for (AlertTreeNode child : node.getChildren()) {
				optimizeAlertTree(child);
			}

			// generate trees for current node
			if (childrenCanBeDeployedOnSameHost(node.getChildren())) {
				System.out.println("optimize on: "+node.getTreeName());
				System.out.println("timewindow: "+getTimewindowFromAlertTree(node).getDuration());
				node.setDeployOn(node.getChildren().get(0).getDeployOn());
				node.setTimewindow(getTimewindowFromAlertTree(node));
			}
		}
		LOGGER.info("optimizeAlertTree(" + node.getTreeName() + ") - finished");

	}

	private boolean childrenCanBeDeployedOnSameHost(List<AlertTreeNode> children) {
		LOGGER.info("childrenCanBeDeployedOnSameHost(listSize: " + children.size() + ")");
		if (!children.isEmpty()) {

			AlertTreeNode firstChild = children.get(0);

			String deployedOn = firstChild.getDeployOn();

			for (AlertTreeNode child : children) {
				if (!deployedOn.equals(child.getDeployOn())) {
					LOGGER.info("childrenCanBeDeployedOnSameHost(listSize: " + children.size() + ") - false");
					return false;
				}
			}

		}
		LOGGER.info("childrenCanBeDeployedOnSameHost(listSize: " + children.size() + ") - true");
		return true;
	}

	public List<AlertCondition> getLocallyPlacedAlertConditions(AlertTreeNode alertTree) {
		LOGGER.info("getLocallyPlacedAlertConditions(" + alertTree.getTreeName() + ")");
		List<AlertCondition> locallyPlacedAlertConditions = new LinkedList<AlertCondition>();

		createListOfLocallyPlacedAlertConditions(locallyPlacedAlertConditions, alertTree);

		LOGGER.info("getLocallyPlacedAlertConditions(" + alertTree.getTreeName() + ") - finished");
		return locallyPlacedAlertConditions;
	}

	private void createListOfLocallyPlacedAlertConditions(List<AlertCondition> locallyPlacedAlertConditions,
			AlertTreeNode alertTree) {
		LOGGER.info("getLocallyPlacedAlertConditions(" + alertTree.getTreeName() + ")");
		if (alertTree.getType() == AlertTreeNodeType.CONDITION && !alertTree.getDeployOn().isEmpty()) {
			locallyPlacedAlertConditions.add((AlertCondition) alertTree);
		} else {
			for (AlertTreeNode child : alertTree.getChildren()) {
				createListOfLocallyPlacedAlertConditions(locallyPlacedAlertConditions, child);
			}
		}

		LOGGER.info("getLocallyPlacedAlertConditions(" + alertTree.getTreeName() + ") - finished");
	}
}
