package org.mapper.api;

import java.util.List;
import java.util.Map;

import org.pf4j.ExtensionPoint;

public interface LocalAlertingToCoreConnector extends ExtensionPoint{
	
	List<LocalAlertingInstance> getLocalAlertingInstances();
	
	void configureInstances(List<Map<String, String>> configurations);

	String getName();

}
