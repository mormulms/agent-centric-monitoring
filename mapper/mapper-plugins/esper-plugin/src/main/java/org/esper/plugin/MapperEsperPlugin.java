package org.esper.plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.mapper.api.LocalAlertingInstance;
import org.pf4j.Plugin;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapperEsperPlugin extends Plugin {
	
	private static final Object HOSTADDRESS_KEY = "host_address";
	private static Logger LOGGER = LoggerFactory.getLogger(MapperEsperPlugin.class);
	private static List<LocalAlertingInstance> esperInstances;
	
	public static List<LocalAlertingInstance> getEsperInstances(){
		return esperInstances;
	}
	
	
	public MapperEsperPlugin(PluginWrapper wrapper) {
		super(wrapper);
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
		esperInstances = new LinkedList<LocalAlertingInstance>();
	}

	@Override
	public void start() throws PluginException {
		LOGGER.info("-----"+this.getClass().getSimpleName()+" started-----");
		super.start();
	}

	@Override
	public void stop() throws PluginException {
		super.stop();
		LOGGER.info("-----"+this.getClass().getSimpleName()+" stopped-----");
	}
	
	public static void configureInstances(List<Map<String, String>> configurations) {
		LOGGER.info("-----Configuring EsperInstances-----");
		LOGGER.info("Number of EsperInstances: "+configurations.size());
		for (Map<String, String> configuration : configurations) {
			String hostaddress = configuration.get(HOSTADDRESS_KEY); 
			
			if (hostaddress != null) {
				
				LOGGER.info("Adding EsperInstance at: "+hostaddress);
				esperInstances.add(EsperInstance.getWithHostAddress(hostaddress));
				
			}
		}
		LOGGER.info("-----Configuration complete-----");
	}
}
