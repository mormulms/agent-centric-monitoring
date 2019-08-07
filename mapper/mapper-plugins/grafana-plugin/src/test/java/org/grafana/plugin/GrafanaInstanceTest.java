package org.grafana.plugin;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapper.model.Alert;

class GrafanaInstanceTest {

	private final String GRAFANA_HOST = "http://127.0.0.1:8080/";
	private final String GRAFANA_API_KEY = "Bearer eyJrIjoic2w3OGg4Y0FrNDhXVUZPb3Nma0JEZkk1RjNtRVdCUXMiLCJuIjoiYWRtaW5LZXkiLCJpZCI6MX0=";
	
	
	@Test
	void getAlerts() {
		GrafanaInstance grafanaInstance = GrafanaInstance.createWithHostAndToken(GRAFANA_HOST, GRAFANA_API_KEY);
		
		grafanaInstance.printAlerts();
		
		List<Alert> alerts = grafanaInstance.getAlerts();
		
		grafanaInstance.updateInstanceForLocallyPlacedAlert(alerts.get(0));
		grafanaInstance.restoreInstanceCentralAlertPlacement(alerts.get(0));
	}

}
