package org.mapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MapperPluginConfigTest {

	static final String CONFIG_LOCATION = "src/test/resources/mapper-plugin-config.toml";
	
	private static MapperPluginConfig config;
	
	@BeforeAll
	static void setUp() {
		File configFile = new File(CONFIG_LOCATION);
		config = MapperPluginConfig.parseFromFile(configFile);
	}
	
	@Test
	void GetLocalAlertingPluginByName_NameIsEsper_ShouldExist() {
		List<Map<String,String>> esperConfigs = config.getLocalAlertingPluginConfigsByName("esper");
		
		assertEquals(1, esperConfigs.size());
		for (Map<String,String> esperConfig : esperConfigs) {
			assertTrue(esperConfig.containsKey("ip"));
			assertTrue(esperConfig.containsKey("port"));
		}
	}
	
	@Test
	void GetCentralAlertingPluginByName_NameIsGrafana_ShouldExist() {
		List<Map<String,String>> grafanaConfigs = config.getCentralAlertingPluginConfigsByName("grafana");
		
		assertEquals(1, grafanaConfigs.size());
		for (Map<String,String> grafanaConfig : grafanaConfigs) {
			assertTrue(grafanaConfig.containsKey("ip"));
			assertTrue(grafanaConfig.containsKey("bearerToken"));
		}
	}
	
	@Test
	void GetAgentPluginByName_NameIsTelegraf_ShouldExist() {
		List<Map<String,String>> agentConfigs = config.getAgentPluginConfigsByName("telegraf");
		
		assertEquals(1, agentConfigs.size());
		for (Map<String,String> agentConfig : agentConfigs) {
			assertTrue(agentConfig.containsKey("ip"));
			assertTrue(agentConfig.containsKey("configPath"));
		}
	}
	

}
