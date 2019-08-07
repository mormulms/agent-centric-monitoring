package org.mapper.model;

import lombok.Data;

@Data
public class Alert {

	public static final String ALERT_ID_SEPARATOR = "Alert";
	
	final String alertId;
	String centralAlertingAddress;
	String dataSourceAddress;
	int numberOfConditions;
	AlertTreeNode alertTree;

	public Alert(String alertId, String centralAlertingAddress, String dataSourceAddress, int numberOfConditions,
			AlertTreeNode alertTree) {
		super();
		this.alertId = alertId;
		this.centralAlertingAddress = centralAlertingAddress;
		this.dataSourceAddress = dataSourceAddress;
		this.numberOfConditions = numberOfConditions;
		this.alertTree = alertTree;
	}
}
