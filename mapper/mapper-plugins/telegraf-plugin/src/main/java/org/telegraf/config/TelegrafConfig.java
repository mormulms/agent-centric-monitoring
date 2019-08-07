package org.telegraf.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mapper.model.AlertCondition;

import lombok.Getter;

@Getter
public class TelegrafConfig {

	private static final String MAYBE = "maybe";
	private static final String TAGPASS = "tagpass";
	private static final String SERVICE_ADDRESS = "service_address";
	private static final String FROM_CEP = "from_cep";
	private static final String HTTP_LISTENER_V2 = "http_listener_v2";
	private static final String AGGREGATED = "aggregated";
	private static final String UNPROCESSED = "unprocessed";
	private static final String TRUE = "true";
	private static final String HTTP = "http";
	private static final String BASICSTATS = "basicstats";
	private static final String OVERRIDE = "override";
	private static final String QUOTE = "\"";
	private static final String NAMEPASS = "namepass";
	private static final String TO_CEP = "to_cep";
	private static final String FALSE = "false";
//	private static final String[] REROUTE_TAGS_TO_EXCLUDE = { TO_CEP, FROM_CEP, AGGREGATED, UNPROCESSED };
	private static final String[] REROUTE_TAGS_TO_EXCLUDE = { TO_CEP, AGGREGATED};
	private static final String[] REROUTE_TAGS_TO_PASS = { TO_CEP, AGGREGATED};
	public static final int EMPYT_LINES_AT_END_OF_PLUGIN = 2;
	private static final String BACKLOOP_PORT = ":9999";

	@Getter
	private List<TelegrafConfigComponent> configComponents;
	// TODO should be added by the parser
	private int numberOfReroutes;

	public TelegrafConfig(List<TelegrafConfigComponent> configComponents) {
		super();
		this.configComponents = configComponents;
		this.numberOfReroutes = 0;
	}

	public TelegrafConfig(List<TelegrafConfigComponent> configComponents, int numberOfLocalAlerts) {
		super();
		this.configComponents = configComponents;
		this.numberOfReroutes = numberOfLocalAlerts;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (TelegrafConfigComponent telegrafConfigComponent : configComponents) {
			builder.append(telegrafConfigComponent.toString());
		}

		return builder.toString();
	}

	public List<TelegrafPlugin> getPluginsByType(TelegrafPluginType pluginType) {
		List<TelegrafPlugin> plugins = new LinkedList<TelegrafPlugin>();

		for (TelegrafConfigComponent component : configComponents) {
			if (component.getType() == TelegrafComponentType.PLUGIN) {
				TelegrafPlugin plugin = (TelegrafPlugin) component;
				if (plugin.getPluginType() == pluginType) {
					plugins.add(plugin);
				}
			}
		}

		return plugins;
	}

	public List<TelegrafPlugin> getPluginByTypeAndName(TelegrafPluginType pluginType, String name) {
		List<TelegrafPlugin> plugins = new LinkedList<TelegrafPlugin>();

		for (TelegrafConfigComponent component : configComponents) {
			if (component.getType() == TelegrafComponentType.PLUGIN) {
				TelegrafPlugin plugin = (TelegrafPlugin) component;
				if (plugin.getPluginType() == pluginType && plugin.getName().equals(name)) {
					plugins.add(plugin);
				}
			}
		}

		return plugins;
	}

	// adds a plugin after the header or before the first plugin
	public TelegrafPlugin addPlugin(TelegrafPlugin pluginToAdd) {
		
		//TODO implement combination of plugins
		TelegrafPlugin similarPlugin;
		if (false && (pluginToAdd.getPluginType() == TelegrafPluginType.OUTPUT || pluginToAdd.getPluginType() == TelegrafPluginType.AGGREGATOR) && (similarPlugin = getSimilarPlugin(pluginToAdd))!=null) {
//			System.out.println("Ähnliches PlugIn gefunden");
//			
//			for (TelegrafPluginSubComponent subComponent : similarPlugin.getSubComponents()) {
//				if (subComponent.name.equals("tagpass")) {
//					subComponent.getOptionByName("to_cep").add(pluginToAdd.get)
//				}
//			}
//			
//			//TODO change tagpass
			return null;
		} else {
			boolean pluginTypeSectionReached = false;
			
			for (int i = 0; i < configComponents.size(); i++) {
				TelegrafConfigComponent component = configComponents.get(i);
				if (!pluginTypeSectionReached) {
					if (component.getType() == TelegrafComponentType.PLUGIN) {
						TelegrafPlugin plugin = (TelegrafPlugin) component;
						if (plugin.getPluginType() == pluginToAdd.getPluginType()) {
							pluginTypeSectionReached = true;
						}
					} else if (component.getType() == TelegrafComponentType.HEADER) {
						TelegrafConfigHeader header = (TelegrafConfigHeader) component;
						if (header.getPluginType() == pluginToAdd.getPluginType()) {
							pluginTypeSectionReached = true;
						}
					}
				} else {
					configComponents.add(i, TelegrafConfigComponent.createPluginSpacer());
					configComponents.add(i, pluginToAdd);
					return pluginToAdd;
				}
			}
			if (!pluginTypeSectionReached) {
				// TODO add header for new Plugin section
			}
			configComponents.add(configComponents.size() - 1, TelegrafConfigComponent.createPluginSpacer());
			configComponents.add(configComponents.size() - 1, pluginToAdd);
			return pluginToAdd;
		}
	}


	private TelegrafPlugin getSimilarPlugin(TelegrafPlugin pluginToAdd) {
		List<TelegrafPlugin> pluginsToCheck = getPluginByTypeAndName(pluginToAdd.getPluginType(), pluginToAdd.getName());
		
		if (pluginToAdd.getPluginType() == TelegrafPluginType.AGGREGATOR && pluginToAdd.getName().equals(BASICSTATS)) {
			for (TelegrafPlugin similarPlugin : pluginsToCheck) {
				if (similarPlugin.usesSameAggregationMethode(pluginToAdd)) {
					return similarPlugin;
				}
			}
			
		} else if (pluginToAdd.getPluginType() == TelegrafPluginType.OUTPUT && pluginToAdd.getName().equals(HTTP)) {
			for (TelegrafPlugin similarPlugin : pluginsToCheck) {
				if (similarPlugin.usesSameHTTPUrl(pluginToAdd)) {
					return similarPlugin;
				}
			}
		}
		return null;
	}

	public void rerouteMesurementToCep(AlertCondition condition, String cepUrl) {

		String measurement = condition.getMeasurement();
		String cepId = condition.getTreeName();
		
		// plugins that are only needed once
		if (numberOfReroutes < 1) {
			//add to_cep maybe tag to all inputs
			for (TelegrafPlugin plugin : getPluginsByType(TelegrafPluginType.INPUT)) {
				plugin.addAdditionalTags(TelegrafPluginCreator.createTagpsOptionsForInputs());
			}
			
			// add tag drop and exclude for all other outputs
			for (TelegrafPlugin plugin : getPluginsByType(TelegrafPluginType.OUTPUT)) {
				plugin.addTagExclude(REROUTE_TAGS_TO_EXCLUDE);
				plugin.addTagpass(TelegrafPluginCreator.createTagpassOptionsForOutputs());
			}
			// add alert backloop input
			addPlugin(TelegrafPluginCreator.createBackloopHttpListenerInputPlugin());
		}

		
		if (!condition.isProxyCondition()) {
			// add override
			addPlugin(TelegrafPluginCreator.createOverrideProcessorPlugin(null, measurement, cepId, false));
			// add aggregator
			System.out.println("interval: "+condition.getAggregationInterval());
			System.out.println("method: "+condition.getAggregationMethod());
			if(condition.getAggregationInterval() != null && condition.getAggregationMethod() != null) {
				addPlugin(TelegrafPluginCreator.createAggregator(measurement, cepId, condition.getAggregationInterval(), condition.getAggregationMethod()));
			}
		} else {
			// add override
			addPlugin(TelegrafPluginCreator.createOverrideProcessorPlugin(null, measurement, cepId, true));
		}
		// add cep output
		addPlugin(TelegrafPluginCreator.createCepHttpOutputPlugin(cepId, cepUrl));
		

		numberOfReroutes++;
	}

	
	public void removeReroute(String measurement, String cepId) {

		numberOfReroutes--;

		boolean pluginNeedsToBeRemoved = false;

		for (int i = configComponents.size() - 1; i >= 0; i--) {
			if (configComponents.get(i).getType() == TelegrafComponentType.PLUGIN) {
				TelegrafPlugin plugin = (TelegrafPlugin) configComponents.get(i);

				// remove to cep output plugin
				if (plugin.getPluginType() == TelegrafPluginType.OUTPUT && plugin.getName().equals(HTTP)
						&& hasTagpassForToCepId(cepId, plugin)) {

					pluginNeedsToBeRemoved = true;
				}

				// remove aggregator plugin
				else if (plugin.getPluginType() == TelegrafPluginType.AGGREGATOR && plugin.getName().equals(BASICSTATS)
						&& hasTagpassForToCepId(cepId, plugin)) {

					pluginNeedsToBeRemoved = true;
				}

				// remove processor plugin
				else if (plugin.getPluginType() == TelegrafPluginType.PROCESSOR && plugin.getName().equals(OVERRIDE)
						&& hasNamepass(measurement, plugin)) {

					pluginNeedsToBeRemoved = true;
				}

				// remove http backloop input
				else if (numberOfReroutes < 1 && plugin.getPluginType() == TelegrafPluginType.INPUT
						&& plugin.getName().equals(HTTP_LISTENER_V2) && httpListenerIsBackloop(plugin)) {

					pluginNeedsToBeRemoved = true;
				}

				// reset outputs
				else if (numberOfReroutes < 1 && plugin.getPluginType() == TelegrafPluginType.OUTPUT
						&& outputHasDefaultTagpassesAndTagexcludes(plugin)) {
					
					plugin.removeTagexcludeForTags(REROUTE_TAGS_TO_EXCLUDE);
					plugin.removeTagpassForTagsWithValues(gernerateOutputDefaultTagPassMap());
				}
				
				// reset inputs
				else if (numberOfReroutes < 1 && plugin.getPluginType() == TelegrafPluginType.INPUT
						&& inputHasDefaultTags(plugin)) {
					
					plugin.removeAdditionalTags(gernerateInputDefaultTagsMap());
				}

				if (pluginNeedsToBeRemoved) {
					// removes plugin
					configComponents.remove(i);
					// removes spacer
					configComponents.remove(i);
					pluginNeedsToBeRemoved = false;
				}
			}
		}
	}

	private boolean outputHasDefaultTagpassesAndTagexcludes(TelegrafPlugin plugin) {

		return plugin.hasTagexcludeForTags(REROUTE_TAGS_TO_EXCLUDE)
				&& plugin.hasTagpassForTagsWithValues(gernerateOutputDefaultTagPassMap());
	}
	
	
	private boolean inputHasDefaultTags(TelegrafPlugin plugin) {

		return plugin.addsTagsWithValues(gernerateInputDefaultTagsMap());
	}

	private Map<String, String[]> gernerateOutputDefaultTagPassMap() {
		Map<String, String[]> defaultTagsAndValues = new HashMap<String, String[]>();
		String[] defaultToCEPValues = {FALSE, MAYBE};
		defaultTagsAndValues.put(TO_CEP, defaultToCEPValues);
		String[] defaultAggregatedValues = {TRUE};
		defaultTagsAndValues.put(AGGREGATED, defaultAggregatedValues);
		return defaultTagsAndValues;
	}

	private Map<String, String[]> gernerateInputDefaultTagsMap() {
		Map<String, String[]> defaultTagsAndValues = new HashMap<String, String[]>();
		String[] defaultToCEPInputValues = {MAYBE};
		defaultTagsAndValues.put(TO_CEP, defaultToCEPInputValues);
		return defaultTagsAndValues;
	}
	
	private boolean httpListenerIsBackloop(TelegrafPlugin plugin) {
		
		List<String> optionValues = plugin.getOptionByName(SERVICE_ADDRESS);
		
		if (optionValues != null && !optionValues.isEmpty()) {
			return optionValues.get(0).equals(BACKLOOP_PORT);
		}
		return false;
	}

	public boolean hasTagpassForToCepId(String cepId, TelegrafPlugin plugin) {
		for (TelegrafPluginSubComponent subComponent : plugin.getSubComponents()) {
			if (subComponent.getName().equals(TAGPASS) && subComponent.getOptionByName(TO_CEP) != null
					&& subComponent.getOptionByName(TO_CEP).contains(cepId)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasNamepass(String measurement, TelegrafPlugin plugin) {
		if (plugin.getOptionByName(NAMEPASS) != null
				&& plugin.getOptionByName(NAMEPASS).contains(measurement)) {
			return true;
		}
		return false;
	}

}
