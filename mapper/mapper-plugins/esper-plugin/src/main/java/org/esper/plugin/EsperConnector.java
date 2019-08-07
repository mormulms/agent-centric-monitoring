package org.esper.plugin;

import java.util.List;
import java.util.Map;

import org.mapper.api.LocalAlertingInstance;
import org.mapper.api.LocalAlertingToCoreConnector;
import org.pf4j.Extension;

@Extension
public class EsperConnector implements LocalAlertingToCoreConnector{

	private static final String  name = "esper";
	
	@Override
	public List<LocalAlertingInstance> getLocalAlertingInstances() {
		return MapperEsperPlugin.getEsperInstances();
	}

	@Override
	public void configureInstances(List<Map<String, String>> configurations) {
		MapperEsperPlugin.configureInstances(configurations);
	}

	@Override
	public String getName() {
		return name;
	}
	
}
