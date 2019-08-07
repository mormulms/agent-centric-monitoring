package org.telegraf.plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.mapper.api.AgentInstance;
import org.pf4j.Plugin;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapperTelegrafPlugin extends Plugin {

	private static final String HOSTADDRESS_KEY= "hostaddress";
	
	private static Logger LOGGER = LoggerFactory.getLogger(MapperTelegrafPlugin.class);
	private static List<AgentInstance> telegrafAgents;

	public MapperTelegrafPlugin(PluginWrapper wrapper) {
		super(wrapper);
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
		telegrafAgents = new LinkedList<AgentInstance>();
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

	public static List<AgentInstance> getTelegrafAgents() {
		LOGGER.info("getTelegrafAgents()");
		return telegrafAgents;
	}

	public static void configureInstances(List<Map<String, String>> configurations) {
		LOGGER.info("-----Configuring TelegrafAgents-----");
		LOGGER.info("Number of TelegrafAgents: "+configurations.size());
		for (Map<String, String> configuration : configurations) {
			String hostaddress = configuration.get(HOSTADDRESS_KEY); 
			
			if (hostaddress != null) {
				
				LOGGER.info("Adding TelegrafAgent at: "+hostaddress);
				telegrafAgents.add(TelegrafAgent.createAndLoad(configuration));
				
			}
		}
		LOGGER.info("-----Configuration complete-----");
	}
}
