package org.mapper.api;

import java.util.List;
import java.util.Map;

import org.pf4j.ExtensionPoint;

public interface AgentToCoreConnector extends ExtensionPoint{
	
	List<AgentInstance> getAgentInstances();
	
	void configureInstances(List<Map<String, String>> configurations);

	String getName();

}
