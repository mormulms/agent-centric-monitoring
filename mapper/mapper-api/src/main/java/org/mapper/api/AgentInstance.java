package org.mapper.api;

import org.mapper.model.AlertCondition;
import org.mapper.model.AlertTreeNode;

public interface AgentInstance {

	String getHostAddress();
	
	String getConfigString();
	
	void addRerouteMetricToLocalAlertingSystem(AlertCondition condition, String localAlertingSystemAddress);
	
	void removeRerouteMetricToLocalAlertingSystem(AlertCondition condition);
	

	
}
