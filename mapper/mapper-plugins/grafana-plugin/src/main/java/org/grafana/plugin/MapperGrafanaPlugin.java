package org.grafana.plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.mapper.api.CentralAlertingInstance;
import org.pf4j.Plugin;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

public class MapperGrafanaPlugin extends Plugin {
	
	public static final String HOST_KEY = "url";
	public static final String TOKEN_KEY = "token";
	
	private static Logger LOGGER = LoggerFactory.getLogger(MapperGrafanaPlugin.class);
	@Getter
	private static LinkedList<CentralAlertingInstance> grafanaInstances;
	
	
	public MapperGrafanaPlugin(PluginWrapper wrapper) {
		super(wrapper);
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
		grafanaInstances = new LinkedList<CentralAlertingInstance>();
	}
	
	@Override
	public void start() throws PluginException {
		LOGGER.info("-----"+this.getClass().getSimpleName()+" started-----");
		super.start();
	}
	
	@Override
	public void stop() throws PluginException {
		super.stop();
		LOGGER.info("-----"+this.getClass().getSimpleName()+" stopped-----");
	}
	
	public static void configureInstances(List<Map<String, String>> configurations) {
		LOGGER.info("-----Configuring GrafanaInstances-----");
		LOGGER.info("Number of GrafanaInstances: "+configurations.size());
		for (Map<String, String> configuration : configurations) {
			LOGGER.info("config size = "+configuration.size());
			String hostaddress = configuration.get(HOST_KEY);
			String token = configuration.get(TOKEN_KEY);
			
			if (hostaddress != null && token != null) {
				
				LOGGER.info("Adding GrafanaInstance at: "+hostaddress);
				grafanaInstances.add(GrafanaInstance.createWithHostAndToken(hostaddress, token));
				
			} else {
				LOGGER.error("hostaddress or token missing");
			}
		}
		LOGGER.info("-----Configuration complete-----");
	}
	
}
