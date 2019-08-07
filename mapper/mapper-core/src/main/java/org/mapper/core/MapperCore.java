package org.mapper.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.mapper.api.AgentInstance;
import org.mapper.api.AgentToCoreConnector;
import org.mapper.api.CentralAlertingInstance;
import org.mapper.api.CentralAlertingToCoreConnector;
import org.mapper.api.LocalAlertingInstance;
import org.mapper.api.LocalAlertingToCoreConnector;
import org.mapper.model.Alert;
import org.mapper.model.AlertCondition;
import org.mapper.model.AlertTreeNode;
import org.mapper.model.AlertUtility;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class MapperCore {

	private static Logger LOGGER = LoggerFactory.getLogger(MapperCore.class);

	private static final String CONFIG_FILE_NAME = "mapper-plugin-config.toml";
	private static final String ALERT_POLICIES_FILE_NAME = "alert-policies.json";

	private static MapperPluginConfig mapperPluginConfig;
	private static MapperAlertPoliciesUtility alertPoliciesUtility;
	
	private static List<Alert> alerts;
	private static Map<String, AlertPolicy> alertPolices;
	private static Map<String, AgentInstance> agentInstances;
	private static Map<String, LocalAlertingInstance> localAlertingInstances;
	private static Map<String, CentralAlertingInstance> centralAlertingInstances;
	private static AlertUtility alertUtility;



	public static void main(String[] args) {
		Configurator.setLevel(LOGGER.getName(), Level.INFO);

		LOGGER.info("MapperCore started");

		File jarFile = new File(MapperCore.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		File configFile = new File(jarFile.getParent() + "/" + CONFIG_FILE_NAME);
		File alertPoliciesFile = new File(jarFile.getParent() + "/" + ALERT_POLICIES_FILE_NAME);
		
		LOGGER.info("load mapper config from: " + configFile.getPath());
		mapperPluginConfig = MapperPluginConfig.parseFromFile(configFile);
		alertPoliciesUtility = new MapperAlertPoliciesUtility(alertPoliciesFile);
		alertUtility = AlertUtility.instance();
		
		LOGGER.info("load plugins");
		PluginManager pluginManager = new DefaultPluginManager();
		pluginManager.loadPlugins();
		LOGGER.info("start plugins");
		pluginManager.startPlugins();

		// handle centralAlertingInstances
		centralAlertingInstances = initializeCentralAlertingInstances(pluginManager);

		// retrieve alerts
		if (centralAlertingInstances != null) {
			alerts = getQualifiedAlerts();
		}
		
		if(alerts != null) {
			LOGGER.info("wirte default alert-policies to: " + alertPoliciesFile.getPath());
			alertPolices = alertPoliciesUtility.writeDefaultPoliciesFileForAlerts(alerts);
		}
		
		// handle agentInstances
		if (alerts != null) {
			agentInstances = initializeAgents(pluginManager, alerts);
		}

		// handle localAlertingInstances
		if (agentInstances != null) {
			localAlertingInstances = initializeLocalAlertingInstances(pluginManager, agentInstances);
		}
		
		printLocalAlertingInstances();
		printAgentInstances();
		printCentralAlertingInstances();
		
		
		// SIMPLE CLI
		String line = null;
		
		LOGGER.info("waiting for input:");
		try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
			while ((line = input.readLine()) != null) {
				if (line.equals("place")) {
					placeAlertsLocally();
				}
				if (line.equals("undo")) {
					revertLocalPlacements();
				}
				if (line.equals("exit")) {
					break;
				}
				LOGGER.info("waiting for input:");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		pluginManager.stopPlugins();
		LOGGER.info("MapperCore stopped");
	}

	private static void printCentralAlertingInstances() {
		printMap(centralAlertingInstances,"central-alerting");
	}

	private static void printAgentInstances() {
		printMap(agentInstances,"agent");
	}

	private static void printLocalAlertingInstances() {
		printMap(localAlertingInstances,"local-alerting");
	}

	private static void printMap(Map<String, ?> map, String name) {
		LOGGER.info(name);
		for (String key : map.keySet()) {
			LOGGER.info(key+":"+map.get(key));
		}
	}


	private static void revertLocalPlacements() {
		LOGGER.info("revertLocalPlacements()");
		for (Alert alert : alerts) {
			revertLocalPlacementOfAlert(alert);
		}
		LOGGER.info("revertLocalPlacements() - finished");
	}

	
	private static void revertLocalPlacementOfAlert(Alert alert) {
		LOGGER.info("revertLocalPlacementOfAlert(" + alert.getAlertId() + ")");

		for (AlertTreeNode alertTree : alertUtility.getLocallyPlaceableAlertTrees(alert)) {
			removeLocalAlertTree(alertTree);
		}

		centralAlertingInstances.get(alert.getCentralAlertingAddress()).restoreInstanceCentralAlertPlacement(alert);
		LOGGER.info("revertLocalPlacementOfAlert(" + alert.getAlertId() + ") - finished");
	}
	
	private static void removeLocalAlertTree(AlertTreeNode alertTree) {
		LOGGER.info("removeLocalAlertCondition(" + alertTree.getTreeName() + ")");
		
		String localAlertingSystemAddress = alertTree.getDeployOn();
		
		for (AlertCondition condition: alertUtility.getAlertConditionsFromAlertTree(alertTree)) {
			agentInstances.get(condition.getMeasurementSources().get(0)).removeRerouteMetricToLocalAlertingSystem(condition);
		}
		localAlertingInstances.get(localAlertingSystemAddress).removeAlertTree(alertTree);

		LOGGER.info("removeLocalAlertCondition(" + alertTree.getTreeName() + ") - finished");
	}

	private static void placeAlertsLocally() {
		LOGGER.info("placeAlertsLocally()");
		//update alert polices
		alertPolices = alertPoliciesUtility.generateAlertPolicyMap(alertPoliciesUtility.readAlertPoliciesFromFile());
		
		for (Alert alert : alerts) {
			placeAlertLocally(alert);
		}
		LOGGER.info("placeAlertsLocally() - finished");
	}
	
	
	private static void placeAlertLocally(Alert alert) {
		LOGGER.info("placeAlertLocally(" + alert.getAlertId() + ")");
		
		alertPoliciesUtility.applyPolicyToAlert(alertPolices.get(alert.getAlertId()), alert);
		
		for (AlertTreeNode alertTree : alertUtility.getLocallyPlaceableAlertTrees(alert)) {
			placeAlertTreeLocally(alertTree);
		}

		centralAlertingInstances.get(alert.getCentralAlertingAddress()).updateInstanceForLocallyPlacedAlert(alert);
		LOGGER.info("placeAlertLocally(" + alert.getAlertId() + ") - finished");
	}

	private static void placeAlertTreeLocally(AlertTreeNode alertTree) {
		String localAlertingSystemAddress = alertTree.getDeployOn();
		LOGGER.info(
				"placeAlertTreeLocally(" + alertTree.getTreeName() + ") - source: " + localAlertingSystemAddress);
		
		
		localAlertingInstances.get(localAlertingSystemAddress).addAlertTree(alertTree);
		for(AlertCondition condition: alertUtility.getAlertConditionsFromAlertTree(alertTree)) {
			System.out.println("place.interval: "+condition.getAggregationInterval());
			System.out.println("place.method: "+condition.getAggregationMethod());
			agentInstances.get(condition.getMeasurementSources().get(0)).addRerouteMetricToLocalAlertingSystem(condition, localAlertingSystemAddress);
		}

		LOGGER.info("placeAlertTreeLocally(" + alertTree.getTreeName() + ") - finished");
	}

	private static List<Alert> getQualifiedAlerts() {
		LOGGER.info("getQualifiedAlerts()");
		List<Alert> qualifiedAlerts = new LinkedList<Alert>();

		for (CentralAlertingInstance centralAlertingInstance : centralAlertingInstances.values()) {
			qualifiedAlerts.addAll(centralAlertingInstance.getAlerts());
		}
		LOGGER.info("getQualifiedAlerts() - " + qualifiedAlerts.size() + " alerts found");
		return qualifiedAlerts;
	}

	private static Map<String, LocalAlertingInstance> initializeLocalAlertingInstances(PluginManager pluginManager,
			Map<String, AgentInstance> agents) {
		Map<String, LocalAlertingInstance> localAlertingInstances = new LinkedHashMap<String, LocalAlertingInstance>();
		LOGGER.info("initializeLocalAlertingInstances()");
		List<LocalAlertingToCoreConnector> localAlertingToCoreConnectors = pluginManager
				.getExtensions(LocalAlertingToCoreConnector.class);
		LOGGER.info("number of LocalAlertingToCoreConnectors: " + localAlertingToCoreConnectors.size());
		for (LocalAlertingToCoreConnector localAlertingToCoreConnector : localAlertingToCoreConnectors) {
			LOGGER.info("initialize " + localAlertingToCoreConnector.getClass().getSimpleName());

			localAlertingToCoreConnector
					.configureInstances(createLocalAlertingConfigurationsFromAgents(agents.values()));
			for (LocalAlertingInstance instance : localAlertingToCoreConnector.getLocalAlertingInstances()) {
				localAlertingInstances.put(instance.getHostAddress(), instance);
			}
		}
		LOGGER.info("number of localAlertingInstances: " + localAlertingInstances.size());
		for (LocalAlertingInstance localAlertingInstance : localAlertingInstances.values()) {
			LOGGER.info(
					localAlertingInstance.getClass().getSimpleName() + " at " + localAlertingInstance.getHostAddress());
		}
		return localAlertingInstances;
	}

	private static List<Map<String, String>> createLocalAlertingConfigurationsFromAgents(
			Collection<AgentInstance> agents) {
		List<Map<String, String>> configurations = new LinkedList<Map<String, String>>();
		LOGGER.info("createLocalAlertingConfigurationsFromAgents()");
		for (AgentInstance agent : agents) {
			Map<String, String> config = new LinkedHashMap<String, String>();
			config.put("host_address", agent.getHostAddress());
			configurations.add(config);
		}

		LOGGER.info(
				"createLocalAlertingConfigurationsFromAgents() - " + configurations.size() + " configurations created");
		return configurations;
	}

	//TODO change creation of agents
	private static Map<String, AgentInstance> initializeAgents(PluginManager pluginManager, List<Alert> alerts) {
		Map<String, AgentInstance> agentInstances = new LinkedHashMap<String, AgentInstance>();
		LOGGER.info("initializeAgents()");
		List<AgentToCoreConnector> agentToCoreConnectors = pluginManager.getExtensions(AgentToCoreConnector.class);
		LOGGER.info("number of AgentToCoreConnectors: " + agentToCoreConnectors.size());
		for (AgentToCoreConnector agentConnector : agentToCoreConnectors) {
			LOGGER.info("initialize " + agentConnector.getClass().getSimpleName());
			
			agentConnector.configureInstances(mapperPluginConfig.getAgentPluginConfigsByName(agentConnector.getName()));
			//agentConnector.configureInstances(createAgentConfigurationsFromAlerts(alerts));
			for (AgentInstance instance : agentConnector.getAgentInstances()) {
				agentInstances.put(instance.getHostAddress(), instance);
			}
		}
		LOGGER.info("number of agentInstances: " + agentInstances.size());
		for (AgentInstance agentInstance : agentInstances.values()) {
			LOGGER.info(agentInstance.getClass().getSimpleName() + " at " + agentInstance.getHostAddress());
		}
		return agentInstances;
	}

	//only works with telegraf for now
	//TODO change
	private static List<Map<String, String>> createAgentConfigurationsFromAlerts(List<Alert> alerts) {
		LOGGER.info("createAgentConfigurationsFromAlerts()");
		List<Map<String, String>> agentConfigurations = new LinkedList<Map<String, String>>();
		
		for (Alert alert : alerts) {
			for (AlertCondition condition : alertUtility.getAlertConditionsFromAlert(alert)) {
				LOGGER.info("createAgentConfigurationsFromAlerts() - condition.getName(): "+condition.getName());
				for (String source : condition.getMeasurementSources()) {
					LOGGER.info("createAgentConfigurationsFromAlerts() - condition.getSources(): "+source);
					if (sourceNotYetAdded(source, agentConfigurations)) {
						Map<String, String> config = new LinkedHashMap<String, String>();
						//check for config for source
						
						config.put("hostaddress", source);
						agentConfigurations.add(config);
					}
				}
			}
		}

		LOGGER.info("createAgentConfigurationsFromAlerts() - " + agentConfigurations.size() + " configurations created");
		return agentConfigurations;
	}

	private static boolean sourceNotYetAdded(String source, List<Map<String, String>> configurations) {
		for (Map<String, String> map : configurations) {
			if (map.containsValue(source)) {
				return false;
			}
		}
		return true;
	}

	private static Map<String, CentralAlertingInstance> initializeCentralAlertingInstances(
			PluginManager pluginManager) {
		Map<String, CentralAlertingInstance> centralAlertingInstances = new LinkedHashMap<String, CentralAlertingInstance>();
		LOGGER.info("initializeCentralAlertingInstances()");
		List<CentralAlertingToCoreConnector> centralAlertingToCoreConnectors = pluginManager
				.getExtensions(CentralAlertingToCoreConnector.class);
		LOGGER.info("number of CentralAlertingToCoreConnectors: " + centralAlertingToCoreConnectors.size());
		for (CentralAlertingToCoreConnector centralAlertingToCoreConnector : centralAlertingToCoreConnectors) {
			LOGGER.info("initialize " + centralAlertingToCoreConnector.getClass().getSimpleName());
			centralAlertingToCoreConnector.configureInstances(
					mapperPluginConfig.getCentralAlertingPluginConfigsByName(centralAlertingToCoreConnector.getName()));
			for (CentralAlertingInstance instance : centralAlertingToCoreConnector.getCentralAlertingInstances()) {
				centralAlertingInstances.put(instance.getHostAddress(), instance);
			}

		}
		LOGGER.info("number of central alerting instances: " + centralAlertingInstances.size());
		for (CentralAlertingInstance centralAlertingInstance : centralAlertingInstances.values()) {
			LOGGER.info(centralAlertingInstance.getClass().getSimpleName() + " at "
					+ centralAlertingInstance.getHostAddress());
		}
		LOGGER.info("initializeCentralAlertingInstances() - " + "finished");
		return centralAlertingInstances;
	}

	public static MapperPluginConfig getMapperPluginConfig() {
		return mapperPluginConfig;
	}
}
