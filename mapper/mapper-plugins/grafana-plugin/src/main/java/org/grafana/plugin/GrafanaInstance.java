package org.grafana.plugin;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.mapper.api.CentralAlertingInstance;
import org.mapper.model.Alert;
import org.mapper.model.AlertCondition;
import org.mapper.model.AlertConditionCombinator;
import org.mapper.model.AlertTreeNode;
import org.mapper.model.AlertTreeNodeOperator;
import org.mapper.model.AlertTreeNodeType;
import org.mapper.model.AlertUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appnexus.grafana.client.GrafanaClient;
import com.appnexus.grafana.client.models.DashboardGraphPanel;
import com.appnexus.grafana.client.models.DashboardPanel;
import com.appnexus.grafana.client.models.DashboardPanelAlert;
import com.appnexus.grafana.client.models.DashboardPanelAlertCondition;
import com.appnexus.grafana.client.models.DashboardPanelAlertConditionEvaluator;
import com.appnexus.grafana.client.models.DashboardPanelAlertConditionOperator;
import com.appnexus.grafana.client.models.DashboardPanelAlertConditionOperator.Type;
import com.appnexus.grafana.client.models.DashboardPanelAlertConditionQuery;
import com.appnexus.grafana.client.models.DashboardPanelAlertConditionReducer;
import com.appnexus.grafana.client.models.DashboardPanelTarget;
import com.appnexus.grafana.client.models.DashboardPanelTargetGroupBy;
import com.appnexus.grafana.client.models.DashboardPanelTargetSelect;
import com.appnexus.grafana.client.models.DashboardPanelTargetTag;
import com.appnexus.grafana.client.models.Datasource;
import com.appnexus.grafana.client.models.GrafanaDashboard;
import com.appnexus.grafana.client.models.GrafanaSearchResult;
import com.appnexus.grafana.configuration.GrafanaConfiguration;
import com.appnexus.grafana.exceptions.GrafanaDashboardDoesNotExistException;
import com.appnexus.grafana.exceptions.GrafanaException;

import lombok.Getter;

public class GrafanaInstance implements CentralAlertingInstance {

	private static final String AGGREGATION_METHOD_SEPARATION= "_";

	private static final String AGGREGATION = "_aggregation";

	private static Logger LOGGER = LoggerFactory.getLogger(GrafanaInstance.class);

	private final String hostaddress;

	private GrafanaClient grafanaClient;
	@Getter
	private List<GrafanaDashboard> dashboards;
	private List<Datasource> datasources;
	private GrafanaAlertUtility grafanaAlertUtility;
	private AlertUtility alertUtility;
	private Map<String, List<DashboardPanelAlertCondition>> savedAlertConditions;

	private GrafanaInstance(String hostaddress, GrafanaClient grafanaClient,
			GrafanaAlertUtility grafanaAlertUtility) {
		super();
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
		this.hostaddress = hostaddress;
		this.grafanaClient = grafanaClient;
		this.grafanaAlertUtility = grafanaAlertUtility;
		this.alertUtility = AlertUtility.instance();
		this.savedAlertConditions = new LinkedHashMap<String, List<DashboardPanelAlertCondition>>();

		this.dashboards = getAllDashboards();
		this.datasources = getDatasources();

	}

	public static GrafanaInstance createWithHostAndToken(String hostaddress, String token) {
		GrafanaConfiguration grafanaConfiguration = new GrafanaConfiguration(hostaddress, token);
		GrafanaClient grafanaClient = new GrafanaClient(grafanaConfiguration);
		GrafanaAlertUtility alertUtility = GrafanaAlertUtility.getInstance();

		return new GrafanaInstance(hostaddress, grafanaClient, alertUtility);
	}

	private List<GrafanaDashboard> getAllDashboards() {
		LOGGER.info("getDashboards()");
		List<GrafanaDashboard> dashboards = new LinkedList<GrafanaDashboard>();

		for (String dashboardUid : getDashboardUids()) {

			GrafanaDashboard grafanaDashboard = null;
			try {
				grafanaDashboard = grafanaClient.getDashboardByUid(dashboardUid);
			} catch (GrafanaDashboardDoesNotExistException e) {
				e.printStackTrace();
			} catch (GrafanaException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (grafanaDashboard != null) {
				dashboards.add(grafanaDashboard);
			}
		}

		LOGGER.info("getDashboards() - " + dashboards.size() + " found");
		return dashboards;
	}

	private List<Datasource> getDatasources() {
		LOGGER.info("getDatasources()");
		List<Datasource> datasources = new LinkedList<Datasource>();
		try {
			datasources = grafanaClient.getDatasources();
		} catch (GrafanaException | IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("getDatasources() - " + datasources.size() + " found");
		return datasources;
	}

	public void printAlerts() {
		for (Alert alert : getAlerts()) {
			LOGGER.info(alert.toString());
		}
	}

	public void printDashboards() {
		for (GrafanaDashboard grafanaDashboard : getAllDashboards()) {
			LOGGER.info("Title of Dashboard found with Uid: " + grafanaDashboard.dashboard().title());
			LOGGER.info("Alerts: ");
			for (DashboardPanelAlert dashboardPanelAlert : getDashboardPanelAlertsFromDashboard(grafanaDashboard)) {
				LOGGER.info(dashboardPanelAlert.toString());
			}
		}
	}

	public void printDatasources() {
		for (Datasource datasource : getDatasources()) {
			LOGGER.info("name: " + datasource.name());
			LOGGER.info("at: " + datasource.url());
		}
	}

	private List<DashboardPanelAlert> getDashboardPanelAlertsFromDashboard(GrafanaDashboard grafanaDashboard) {
		LOGGER.info("getAlertsFromDashboard()");
		List<DashboardPanelAlert> alerts = new LinkedList<DashboardPanelAlert>();
		for (DashboardPanel dashboardPanel : grafanaDashboard.dashboard().panels()) {
			LOGGER.info("panel name: " + dashboardPanel.title());
			LOGGER.info("panel type: " + dashboardPanel.type().value());
			if (dashboardPanel.type() == DashboardPanel.Type.GRAPH) {
				DashboardPanelAlert dashboardPanelAlert = ((DashboardGraphPanel) dashboardPanel).alert();
				if (dashboardPanelAlert != null) {
					alerts.add(dashboardPanelAlert);
				}
			}
		}
		LOGGER.info("Number of alerts: " + alerts.size());
		return alerts;
	}

	private List<String> getDashboardUids() {
		LOGGER.info("getDashboardUids()");
		List<String> dashboardUids = new LinkedList<String>();

		List<GrafanaSearchResult> resultList = null;
		try {
			resultList = grafanaClient.search(null, null, null, null);
		} catch (GrafanaException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (resultList != null) {
			for (GrafanaSearchResult grafanaSearchResult : resultList) {
				dashboardUids.add(grafanaSearchResult.uid());
			}
		}
		LOGGER.info("getDashboardUids() - finished");
		return dashboardUids;
	}

	@Override
	public String getHostAddress() {
		return hostaddress;
	}

	@Override
	public List<Alert> getAlerts() {
		LOGGER.info("getAlerts()");
		LinkedList<Alert> alerts = new LinkedList<Alert>();
		for (GrafanaDashboard grafanaDashboard : dashboards) {
			alerts.addAll(grafanaAlertUtility.getAlertsFromDashboard(grafanaDashboard, hostaddress, datasources));
		}
		LOGGER.info("getAlerts() - " + alerts.size() + " found");
		return alerts;
	}

	@Override
	public boolean updateInstanceForLocallyPlacedAlert(Alert alert) {
		LOGGER.info("updateInstanceForLocallyPlacedAlert(" + alert.getAlertId() + ")");
		String[] alertIdComponent = alert.getAlertId().split(Alert.ALERT_ID_SEPARATOR);

		String dashboardUid = alertIdComponent[0];
		String panelId = alertIdComponent[1].split(AlertCondition.CONDITION_SEPARATOR)[0];

		GrafanaDashboard dashboardToChange = getDashboardByUid(dashboardUid);
		dashboardToChange.overwrite(true);
		DashboardGraphPanel panelToChange = (DashboardGraphPanel) getPanelFromDashboardById(dashboardToChange, panelId);

		savedAlertConditions.put(alert.getAlertId(), panelToChange.alert().conditions());

		// add aggregator queries
		for (AlertCondition condition : alertUtility.getLocallyPlacedAlertConditions(alert.getAlertTree())) {
			// add aggregate query for target if not yet present
			if (!isThereAnAggregateTargetForRefId(condition.getQueryName(), panelToChange.targets())) {
				panelToChange.targets().add(createTargetForAggregateFromTarget(
						getTargetByRefId(condition.getQueryName(), panelToChange.targets()),condition.getAggregationMethod()));
			}
		}

		//create all new alert conditions
		List<DashboardPanelAlertCondition> panelAlertConditions = createDashboardPanelAlertConditionsFromLocallyPlacedAlertTree(
				alert.getAlertTree());

		// add querys for alert conditions
		for (DashboardPanelAlertCondition alertCondition : panelAlertConditions) {
			panelToChange.targets().add(createTargetForLocallyPlacedAlertCondition(alert.getAlertId(), alertCondition));
		}

		// change the alertconditions
		panelToChange.alert().conditions(panelAlertConditions);

		try {
			grafanaClient.updateDashboard(dashboardToChange);
			LOGGER.info("updateInstanceForLocallyPlacedAlert() - update done");
			return true;
		} catch (GrafanaException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("updateInstanceForLocallyPlacedAlert() - update failed");
		return false;
	}




	private List<DashboardPanelAlertCondition> createDashboardPanelAlertConditionsFromLocallyPlacedAlertTree(AlertTreeNode alertTree) {
		LOGGER.info("createDashboardPanelAlertConditionsFromLocallyPlacedAlertTree("+alertTree.getTreeName() +")");
		List<DashboardPanelAlertCondition> alertConditionList = new LinkedList<DashboardPanelAlertCondition>();

		createDashboardPanelAlertConditionsFromLocallyPlacedAlertTree(alertTree, alertConditionList);
		
		LOGGER.info("createDashboardPanelAlertConditionsFromLocallyPlacedAlertTree("+alertTree.getTreeName() +") - finished");
		return alertConditionList;
	}

	private void createDashboardPanelAlertConditionsFromLocallyPlacedAlertTree(AlertTreeNode node,
			List<DashboardPanelAlertCondition> conditionList) {
		LOGGER.info("createDashboardPanelAlertConditionsFromLocallyPlacedAlertTree("+node.getTreeName() +", listSize:"+ conditionList.size()+")");
		if (node.getType() == AlertTreeNodeType.OPERATOR) {
			if (!node.getDeployOn().isEmpty()) {
				System.out.println("Für Operator wurde Platzierung festgelegt");
				conditionList.add(createDashboardPanelAlertConditionForLocallyPlacedAlertTree((AlertTreeNodeOperator) node));
				return;
			} else if (bothChildrenAreAlertConditions(node.getChildren())) {
				System.out.println("Kinder sind alles Blattknoten");
				conditionList.add(createDashboardPanelAlertConditionForLocallyPlacedAlertTreeNode(node.getChildren().get(0),AlertConditionCombinator.AND));
				conditionList.add(createDashboardPanelAlertConditionForLocallyPlacedAlertTreeNode(node.getChildren().get(1), ((AlertTreeNodeOperator) node).getOperator()));
				return;
			} else {
				System.out.println("Wurzelknoten");
				createDashboardPanelAlertConditionsFromLocallyPlacedAlertTree(node.getChildren().get(1), conditionList);
				conditionList.add(createDashboardPanelAlertConditionForLocallyPlacedAlertTreeNode(node.getChildren().get(0), ((AlertTreeNodeOperator) node).getOperator()));
				return;
			}
		}
	}

	private boolean bothChildrenAreAlertConditions(List<AlertTreeNode> children) {
		if (children==null) {
			return false;
		}
		
		boolean allChildrenAreAlertConditions = true;
		
		for (AlertTreeNode alertTreeNode : children) {
			if (alertTreeNode.getType()!=AlertTreeNodeType.CONDITION) {
				allChildrenAreAlertConditions = false;
			}
		}
		return allChildrenAreAlertConditions;
	}

	private DashboardPanelTarget getTargetByRefId(String queryName, List<DashboardPanelTarget> targets) {
		for (DashboardPanelTarget target : targets) {
			if (target.refId().equals(queryName)) {
				return target;
			}
		}
		return null;
	}

	private boolean isThereAnAggregateTargetForRefId(String queryName, List<DashboardPanelTarget> targets) {
		for (DashboardPanelTarget target : targets) {
			if (target.refId().equals(queryName + AGGREGATION)) {
				return true;
			}
		}
		return false;
	}

	private DashboardPanelTarget createTargetForAggregateFromTarget(DashboardPanelTarget existingTarget, String aggregationMethod) {

		DashboardPanelTarget target = new DashboardPanelTarget();
		target.groupBy(new LinkedList<DashboardPanelTargetGroupBy>());
		target.hide(false);
		target.measurement(existingTarget.measurement());
		target.orderByTime("ASC");
		target.policy("default");
		target.refId(existingTarget.refId() + AGGREGATION);
		target.resultFormat("time_series");
		target.select(selectFromOldSelects(existingTarget.select(),aggregationMethod));
		target.tags(existingTarget.tags());

		return target;
	}

	private List<List<DashboardPanelTargetSelect>> selectFromOldSelects(List<List<DashboardPanelTargetSelect>> select, String aggregationMethod) {

		List<List<DashboardPanelTargetSelect>> newSelects = new LinkedList<List<DashboardPanelTargetSelect>>();
		List<DashboardPanelTargetSelect> newSelect = new LinkedList<DashboardPanelTargetSelect>();
		List<DashboardPanelTargetSelect> oldSelect = select.get(0);

		DashboardPanelTargetSelect newSelectComponent = new DashboardPanelTargetSelect();
		List<String> newSelectParams = new LinkedList<String>();

		for (DashboardPanelTargetSelect oldSelectComponent : oldSelect) {
			if (oldSelectComponent.type() == DashboardPanelTargetSelect.Type.FIELD) {
				newSelectParams.add(oldSelectComponent.params().get(0) + AGGREGATION_METHOD_SEPARATION + aggregationMethod);
			}
		}
		newSelectComponent.params(newSelectParams);
		newSelectComponent.type(DashboardPanelTargetSelect.Type.FIELD);

		newSelect.add(newSelectComponent);

		newSelects.add(newSelect);

		return newSelects;
	}

	private DashboardPanelAlertCondition createDashboardPanelAlertConditionForLocallyPlacedAlertTree(
			AlertTreeNodeOperator root) {
		LOGGER.info("createDashboardPanelAlertConditionForLocallyPlacedAlertTree("+root.getTreeName() +")");
		DashboardPanelAlertCondition panelAlertCondition = new DashboardPanelAlertCondition();

		DashboardPanelAlertConditionEvaluator evaluator = new DashboardPanelAlertConditionEvaluator();
		List<Double> evaluatorParams = new LinkedList<Double>();
		evaluatorParams.add(new Double(0));
		evaluator.params(evaluatorParams);
		evaluator.type(DashboardPanelAlertConditionEvaluator.Type.GREATER_THAN);

		DashboardPanelAlertConditionOperator operator = new DashboardPanelAlertConditionOperator();
		operator.type(createOperatorTypeFromAlertCombinator(root.getOperator()));

		DashboardPanelAlertConditionQuery query = new DashboardPanelAlertConditionQuery();
		List<String> queryParams = new LinkedList<String>();
		queryParams.add(root.getTreeName());
		queryParams.add(root.getTimewindow().getDuration());
		queryParams.add(root.getTimewindow().getEnd());

		query.params(queryParams);

		DashboardPanelAlertConditionReducer reducer = new DashboardPanelAlertConditionReducer();
		reducer.params(null);
		reducer.type(DashboardPanelAlertConditionReducer.Type.LAST);

		panelAlertCondition.evaluator(evaluator);
		panelAlertCondition.operator(operator);
		panelAlertCondition.query(query);
		panelAlertCondition.reducer(reducer);
		panelAlertCondition.type(DashboardPanelAlertCondition.Type.QUERY);

		LOGGER.info("createDashboardPanelAlertConditionForLocallyPlacedAlertTree("+root.getTreeName() +") - finished");
		return panelAlertCondition;
	}
	
	private DashboardPanelAlertCondition createDashboardPanelAlertConditionForLocallyPlacedAlertTreeNode(
			AlertTreeNode node, AlertConditionCombinator combinator) {
		LOGGER.info("createDashboardPanelAlertConditionForLocallyPlacedAlertCondition("+node.getTreeName() +")");
		DashboardPanelAlertCondition panelAlertCondition = new DashboardPanelAlertCondition();

		DashboardPanelAlertConditionEvaluator evaluator = new DashboardPanelAlertConditionEvaluator();
		List<Double> evaluatorParams = new LinkedList<Double>();
		evaluatorParams.add(new Double(0));
		evaluator.params(evaluatorParams);
		evaluator.type(DashboardPanelAlertConditionEvaluator.Type.GREATER_THAN);

		DashboardPanelAlertConditionOperator operator = new DashboardPanelAlertConditionOperator();
		operator.type(createOperatorTypeFromAlertCombinator(combinator));

		DashboardPanelAlertConditionQuery query = new DashboardPanelAlertConditionQuery();
		List<String> queryParams = new LinkedList<String>();
		queryParams.add(node.getTreeName());
		queryParams.add(node.getTimewindow().getDuration());
		queryParams.add(node.getTimewindow().getEnd());

		query.params(queryParams);

		DashboardPanelAlertConditionReducer reducer = new DashboardPanelAlertConditionReducer();
		reducer.params(null);
		reducer.type(DashboardPanelAlertConditionReducer.Type.LAST);

		panelAlertCondition.evaluator(evaluator);
		panelAlertCondition.operator(operator);
		panelAlertCondition.query(query);
		panelAlertCondition.reducer(reducer);
		panelAlertCondition.type(DashboardPanelAlertCondition.Type.QUERY);

		LOGGER.info("createDashboardPanelAlertConditionForLocallyPlacedAlertCondition("+node.getTreeName() +") - finished");
		return panelAlertCondition;
	}

	//TODO remove static values
	private String getTimeWindowEndFromAlertTree(AlertTreeNode alertTree) {
		if (alertTree.getType() == AlertTreeNodeType.CONDITION) {
			AlertCondition condition = (AlertCondition) alertTree;
			return condition.getTimewindow().getEnd();
		}
		return "now";
	}

	//TODO remove static values
	private String getTimeWindowDurationFromAlertTree(AlertTreeNode alertTree) {
		if (alertTree.getType() == AlertTreeNodeType.CONDITION) {
			AlertCondition condition = (AlertCondition) alertTree;
			return condition.getTimewindow().getDuration();
		}
		return "1m";
	}

	private Type createOperatorTypeFromAlertCombinator(AlertConditionCombinator combinator) {
		switch (combinator) {
		case AND:
			return DashboardPanelAlertConditionOperator.Type.AND;
		case OR:
			return DashboardPanelAlertConditionOperator.Type.OR;
		default:
			return null;
		}
	}


	private DashboardPanelTarget createTargetForLocallyPlacedAlertCondition(String alertId,
			DashboardPanelAlertCondition alertCondition) {
		DashboardPanelTarget target = new DashboardPanelTarget();
		String alertConditionQueryName = alertCondition.query().params().get(0);
		target.groupBy(new LinkedList<DashboardPanelTargetGroupBy>());
		target.hide(false);
		target.measurement(alertId);
		target.orderByTime("ASC");
		target.policy("default");
		target.refId(alertConditionQueryName);
		target.resultFormat("time_series");
		target.select(createSelectsForLocallyPlacedAlertTree(alertConditionQueryName));
		target.tags(new LinkedList<DashboardPanelTargetTag>());

		return target;
	}


	private List<List<DashboardPanelTargetSelect>> createSelectsForLocallyPlacedAlertTree(String alertTreeName) {
		DashboardPanelTargetSelect select = new DashboardPanelTargetSelect();
		List<String> selectParams = new LinkedList<String>();
		selectParams.add(alertTreeName);
		select.params(selectParams);
		select.type(DashboardPanelTargetSelect.Type.FIELD);

		List<DashboardPanelTargetSelect> selectAndFunction = new LinkedList<DashboardPanelTargetSelect>();
		selectAndFunction.add(select);

		List<List<DashboardPanelTargetSelect>> selects = new LinkedList<List<DashboardPanelTargetSelect>>();
		selects.add(selectAndFunction);

		return selects;
	}

	private DashboardPanel getPanelFromDashboardById(GrafanaDashboard dashboardToChange, String panelId) {
		for (DashboardPanel panel : dashboardToChange.dashboard().panels()) {
			if (panel.id().equals(Integer.parseInt(panelId))) {
				return panel;
			}
		}
		return null;
	}

	private GrafanaDashboard getDashboardByUid(String dashboardUid) {
		for (GrafanaDashboard grafanaDashboard : dashboards) {
			if (grafanaDashboard.dashboard().uid().equals(dashboardUid)) {
				return grafanaDashboard;
			}
		}
		return null;
	}

	@Override
	public boolean restoreInstanceCentralAlertPlacement(Alert alert) {
		LOGGER.info("restoreInstanceCentralAlertPlacement(" + alert.getAlertId() + ")");

		List<DashboardPanelAlertCondition> savedConditions = null;
		if ((savedConditions = savedAlertConditions.remove(alert.getAlertId())) == null) {
			LOGGER.info("restoreInstanceCentralAlertPlacement() - missing save");
			return false;
		}

		String[] alertIdComponent = alert.getAlertId().split(Alert.ALERT_ID_SEPARATOR);

		String dashboardUid = alertIdComponent[0];
		String panelId = alertIdComponent[1].split(AlertCondition.CONDITION_SEPARATOR)[0];

		GrafanaDashboard dashboardToChange = getDashboardByUid(dashboardUid);
		dashboardToChange.overwrite(true);
		DashboardGraphPanel panelToChange = (DashboardGraphPanel) getPanelFromDashboardById(dashboardToChange, panelId);

		// remove new querys and restore old one
		for (int i = panelToChange.targets().size() - 1; i >= 0; i--) {
			LOGGER.info(panelToChange.targets().get(i).measurement() + ":" + alert.getAlertId());
			if (panelToChange.targets().get(i).measurement().equals(alert.getAlertId())) {
				panelToChange.targets().remove(i);
			} else if (panelToChange.targets().get(i).refId().endsWith(AGGREGATION)) {
				panelToChange.targets().remove(i);
			}
		}

		// restore old the alertconditions
		panelToChange.alert().conditions(savedConditions);

		try {
			grafanaClient.updateDashboard(dashboardToChange);
			LOGGER.info("restoreInstanceCentralAlertPlacement() - update done");
			return true;
		} catch (GrafanaException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("restoreInstanceCentralAlertPlacement() - update failed");
		return false;
	}

}
