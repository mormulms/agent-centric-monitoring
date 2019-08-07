package org.mapper.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlFormat;

public class MapperPluginConfig {
	
	private static Logger LOGGER = LoggerFactory.getLogger(MapperPluginConfig.class);
	
	private static String LOCAL_ALERTING_PLUGIN_PATH = "local-alerting.";
	private static String CENTRAL_ALERTING_PLUGIN_PATH = "central-alerting.";
	private static String AGENT_PLUGIN_PATH = "agent.";
	
	private CommentedConfig pluginConfigs;

	private MapperPluginConfig(CommentedConfig pluginConfigs) {
		super();
		this.pluginConfigs = pluginConfigs;
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
	}

	public List<Map<String, String>> getLocalAlertingPluginConfigsByName(String pluginName) {
		return getAgentPluginConfigsByNameAndType(pluginName, MapperPluginType.LOCAL_ALERTING);
	}
	
	public List<Map<String, String>> getCentralAlertingPluginConfigsByName(String pluginName) {
		return getAgentPluginConfigsByNameAndType(pluginName, MapperPluginType.CENTRAL_ALERTING);
	}
	
	public List<Map<String, String>> getAgentPluginConfigsByName(String pluginName) {
		return getAgentPluginConfigsByNameAndType(pluginName, MapperPluginType.AGENT);
	}
	
	private List<Map<String, String>> getAgentPluginConfigsByNameAndType(String pluginName, MapperPluginType type){
		String pathPrefix;
		switch (type) {
		case AGENT:
			pathPrefix = AGENT_PLUGIN_PATH;
			break;
		case CENTRAL_ALERTING:
			pathPrefix = CENTRAL_ALERTING_PLUGIN_PATH;
			break;
		case LOCAL_ALERTING:
			pathPrefix = LOCAL_ALERTING_PLUGIN_PATH;
			break;
		default:
			return null;
		}
		
		List<CommentedConfig> configs = pluginConfigs.get(pathPrefix+pluginName);
		List<Map<String, String>> configsToReturn = new LinkedList<Map<String,String>>();
		
		for (CommentedConfig config : configs) {
			Map<String, String> newConfig = new LinkedHashMap<String, String>();
			for (String key : config.valueMap().keySet()) {
				newConfig.put(key, (String) config.valueMap().get(key));
			}
			
			configsToReturn.add(newConfig);
		}
		
		return configsToReturn;
	}
	

	public static MapperPluginConfig parseFromFile(File configFile) {
		CommentedConfig inOrderConfig = CommentedConfig.wrap(new LinkedHashMap<String, Object>(), TomlFormat.instance());
		
		FileReader reader;
		try {
			reader = new FileReader(configFile);
			TomlFormat.instance().createParser().parse(reader, inOrderConfig, ParsingMode.REPLACE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return new MapperPluginConfig(inOrderConfig);
	}
}
