package org.mapper.api;

import java.util.List;
import java.util.Map;

import org.pf4j.ExtensionPoint;

public interface CentralAlertingToCoreConnector extends ExtensionPoint{
	
	List<CentralAlertingInstance> getCentralAlertingInstances();
	
	void configureInstances(List<Map<String, String>> configurations);
	
	String getName();

}
