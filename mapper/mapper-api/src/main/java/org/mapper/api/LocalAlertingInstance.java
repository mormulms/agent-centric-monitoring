package org.mapper.api;

import org.mapper.model.AlertTreeNode;

public interface LocalAlertingInstance {

	String getHostAddress();
	
	String getAlerts();
	
	boolean addAlertTree(AlertTreeNode alertTree);
	
	boolean removeAlertTree(AlertTreeNode alertTree);
}
