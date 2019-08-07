package org.telegraf.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TelegrafConfigTest {

	static final String TESTFILE_LOCATION = "src/test/resources/telegraf.conf";
	static final String CHECKFILE_LOCATION = "src/test/resources/telegraf-unchanged.conf";
	static final String SAVED_FILE_LOCATION = "src/test/resources/telegraf-saved.conf";
	static final String LINES_ADDED_FILE_LOCATION = "src/test/resources/telegraf-lines-added.conf";
	static final String PLUGIN_ADDED_FILE_LOCATION = "src/test/resources/telegraf-plugin-added.conf";
	static final String SIMPLE_FILE_LOCATION = "src/test/resources/telegraf-simple.conf";
	
	TelegrafConfig telegrafConfig;
	
	
	@BeforeEach
	void loadConfig() {
		telegrafConfig = TelegrafConfigParser.readConfigFromFile(new File(TESTFILE_LOCATION));
	}


	//TODO move 
	@Disabled
	@Test
	void LoadAndSaveConfigFile() throws Exception {

		TelegrafConfigWriter.writeConfigToFile(telegrafConfig, new File(SAVED_FILE_LOCATION));
			
		File actualConfig = new File(SAVED_FILE_LOCATION);
		File expectedConfig = new File(CHECKFILE_LOCATION);
		
		byte[] actualConfigBytes = null;
		byte[] expectedConfigBytes = null;
		try {
			actualConfigBytes = Files.readAllBytes(actualConfig.toPath());
			expectedConfigBytes = Files.readAllBytes(expectedConfig.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertNotNull(actualConfigBytes);
		assertNotNull(expectedConfigBytes);
		assertArrayEquals(expectedConfigBytes, actualConfigBytes);
	}
	
	@Disabled
	@Test
	void getPluginByType_TelegrafConf_CorrectPlugins() {
		
		assertEquals(2, telegrafConfig.getPluginsByType(TelegrafPluginType.INPUT).size());
		assertEquals(9, telegrafConfig.getPluginsByType(TelegrafPluginType.PROCESSOR).size());
		assertEquals(4, telegrafConfig.getPluginsByType(TelegrafPluginType.AGGREGATOR).size());
		assertEquals(1, telegrafConfig.getPluginsByType(TelegrafPluginType.OUTPUT).size());
		
	}
	@Disabled
	@Test
	void getPluginByTypeAndName_TelegrafConf_CorrectPlugins() {
		
		TelegrafPluginType[] typesToTest = TelegrafPluginType.values();
		
		//skip SERVICE_INPUT
		for (int i = 1; i < typesToTest.length; i++) {
			List<TelegrafPlugin> typedPlufgins = telegrafConfig.getPluginsByType(typesToTest[i]);
			for (TelegrafPlugin telegrafPlugin : typedPlufgins) {
				assertEquals(1, telegrafConfig.getPluginByTypeAndName(typesToTest[i], telegrafPlugin.getName()).size());
			}
		}
	}
	
	@Disabled
	@Test
	void addPlugin_TelegrafConf_CorrectPlugins() {
		String testName = "test";
		int numberOfComponentsBeforeAdditionOfPlugins = telegrafConfig.getConfigComponents().size();
		
		TelegrafPluginType[] typesToTest = TelegrafPluginType.values();
		//skip SERVICE_INPUT
		for (int i = 1; i < typesToTest.length; i++) {
			TelegrafPlugin plugin = TelegrafPlugin.createWithTypeAndName(typesToTest[i], testName);
			telegrafConfig.addPlugin(plugin);
			
			List<TelegrafPlugin> typedPlufgins = telegrafConfig.getPluginsByType(typesToTest[i]);
			assertEquals(testName, typedPlufgins.get(typedPlufgins.size()-1).getName());
		}
		assertEquals(numberOfComponentsBeforeAdditionOfPlugins+8, telegrafConfig.getConfigComponents().size());
	}
	
	@Test
	void dirty() {
		telegrafConfig = TelegrafConfigParser.readConfigFromFile(new File(TESTFILE_LOCATION));
		
		String measurementToReroute = "win_mem";
		String cepId = "local|1";
		String cepUrl = "cepUrl";
		
//		telegrafConfig.rerouteMesurementToCep(measurementToReroute, cepId, cepUrl);
		
		telegrafConfig.removeReroute(measurementToReroute, cepId);
		
		
		
		TelegrafConfigWriter.writeConfigToFile(telegrafConfig, new File(SAVED_FILE_LOCATION));
	}
}
