package org.mapper.api;

import java.util.List;

import org.mapper.model.Alert;

public interface CentralAlertingInstance {
	
	String getHostAddress();
	
	List<Alert> getAlerts();
	
	boolean updateInstanceForLocallyPlacedAlert(Alert alert);
	
	boolean restoreInstanceCentralAlertPlacement(Alert alert);

}
