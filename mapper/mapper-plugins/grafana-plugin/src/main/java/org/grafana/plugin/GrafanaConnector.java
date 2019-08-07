package org.grafana.plugin;

import java.util.List;
import java.util.Map;

import org.mapper.api.CentralAlertingInstance;
import org.mapper.api.CentralAlertingToCoreConnector;
import org.pf4j.Extension;

@Extension
public class GrafanaConnector implements CentralAlertingToCoreConnector{

	private static final String name = "grafana";

	@Override
	public List<CentralAlertingInstance> getCentralAlertingInstances() {
		return MapperGrafanaPlugin.getGrafanaInstances();
	}

	@Override
	public void configureInstances(List<Map<String, String>> configurations) {
		MapperGrafanaPlugin.configureInstances(configurations);
	}

	@Override
	public String getName() {
		return name;
	}
	

}
