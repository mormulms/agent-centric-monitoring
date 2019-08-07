package org.telegraf.plugin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginWrapper;

class MapperTelegrafPluginTest {

	MapperTelegrafPlugin mapperPlugin;
	
	@BeforeEach
	void setUp(){
		mapperPlugin = new MapperTelegrafPlugin(new PluginWrapper(null, null, null, null));
	}
	
	@Test
	void nullConstructor() {
		assertTrue(mapperPlugin != null);
	}

}
