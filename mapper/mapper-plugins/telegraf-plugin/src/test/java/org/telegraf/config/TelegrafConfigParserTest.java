package org.telegraf.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TelegrafConfigParserTest {

	static final String TESTFILE_LOCATION = "src/test/resources/telegraf.conf";
	
	TelegrafConfig config;
	
	@BeforeEach
	void setUp(){
		File configFile = new File(TESTFILE_LOCATION);
		config = TelegrafConfigParser.readConfigFromFile(configFile);
	}
	
	@Test
	void readHeaderComponents() {
		int numberOfHeaders = 0;

		for (TelegrafConfigComponent component : config.getConfigComponents()) {
			if (component.getType() == TelegrafComponentType.HEADER) {
//				System.out.println(((TelegrafConfigHeader) component).toString());
				numberOfHeaders++;
			}
		}
		
		assertEquals(4, numberOfHeaders);
	}
	
	@Test
	void readPluginComponents() {
		int numberOfPlugins = 0;

		for (TelegrafConfigComponent component : config.getConfigComponents()) {
			if (component.getType() == TelegrafComponentType.PLUGIN) {
				TelegrafPlugin plugin = (TelegrafPlugin) component;
				for (TelegrafPluginSubComponent subComponent : plugin.getSubComponents()) {
					System.out.println(subComponent.toString());
				}
				numberOfPlugins++;
			}
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println(component.getType().toString());
			System.out.println(component.toString());
		}
		
		assertEquals(17, numberOfPlugins);
	}

}
