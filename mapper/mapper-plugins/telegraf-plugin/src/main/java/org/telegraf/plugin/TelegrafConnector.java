package org.telegraf.plugin;

import java.util.List;
import java.util.Map;

import org.mapper.api.AgentInstance;
import org.mapper.api.AgentToCoreConnector;
import org.pf4j.Extension;

@Extension
public class TelegrafConnector implements AgentToCoreConnector {

	private static final String name = "telegraf";
	
	@Override
	public List<AgentInstance> getAgentInstances() {
		return MapperTelegrafPlugin.getTelegrafAgents();
	}

	@Override
	public void configureInstances(List<Map<String, String>> configurations) {
		MapperTelegrafPlugin.configureInstances(configurations);
	}

	@Override
	public String getName() {
		return name;
	}

}
