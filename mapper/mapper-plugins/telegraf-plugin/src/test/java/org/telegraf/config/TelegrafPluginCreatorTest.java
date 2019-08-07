package org.telegraf.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

class TelegrafPluginCreatorTest {

	@Test
	void createHttpListenerInputPlugin_NoInput_NoFailures() {
		
		TelegrafPlugin httpListenerInput = TelegrafPluginCreator.createBackloopHttpListenerInputPlugin();
		
		assertEquals("http_listener_v2", httpListenerInput.getName());
		assertEquals(TelegrafPluginType.INPUT, httpListenerInput.getPluginType());
		assertTrue(httpListenerInput.getOptionByName("service_address") != null);
		assertTrue(httpListenerInput.getOptionByName("path") != null);
		assertTrue(httpListenerInput.getOptionByName("methods") != null);
		assertTrue(httpListenerInput.getOptionByName("data_format") != null);
		assertTrue(httpListenerInput.getOptionByName("failure") == null);
		
	}
	
	@Test
	void createInfluxDBOutputPlugin_NoInput_NoFailures() {
		String url = "http://127.0.0.1:8086";
		String database = "telegraf";
		boolean skipDatabaseCreation = true;
				
		TelegrafPlugin influxDBOutput = TelegrafPluginCreator.createMinimalInfluxDBOutputPlugin(url, database, skipDatabaseCreation);
		
		assertEquals("influxdb", influxDBOutput.getName());
		assertEquals(TelegrafPluginType.OUTPUT, influxDBOutput.getPluginType());
		assertTrue(influxDBOutput.getOptionByName("urls") != null);
		assertTrue(influxDBOutput.getOptionByName("database") != null);
		assertTrue(influxDBOutput.getOptionByName("database_tag") != null);
		assertTrue(influxDBOutput.getOptionByName("skip_database_creation") != null);
		assertTrue(influxDBOutput.getOptionByName("failure") == null);

	}
	
	
	@Test
	void createHttpOutputPlugin_NoInput_NoFailures() {
		String url = "http://127.0.0.1:8080/metric";
		
		TelegrafPlugin httpOutput = TelegrafPluginCreator.createCepHttpOutputPlugin("test",url);
		
		assertEquals("http", httpOutput.getName());
		assertEquals(TelegrafPluginType.OUTPUT, httpOutput.getPluginType());
		assertTrue(httpOutput.getOptionByName("url") != null);
		assertTrue(httpOutput.getOptionByName("method") != null);
		assertTrue(httpOutput.getOptionByName("data_format") != null);
		assertTrue(httpOutput.getOptionByName("failure") == null);
	}
	
	@Test
	void createOverrideProcessorPlugin_NoInput_NoFailures() {
		String namepass = "win_mem";
		String tagValue = "address";
		
		TelegrafPlugin overrideProcessor = TelegrafPluginCreator.createOverrideProcessorPlugin(0, namepass, tagValue, false);
		
		System.out.println(overrideProcessor);
	}
	
	@Test
	void createBasicStatsAggregatorPlugin_NoInput_NoFailures() {
		String namepass = "win_mem";
		String period = "30s";
		String cepId = "testId";
		
		List<String> stats = new LinkedList<String>();
		stats.add("mean");
		
		TelegrafPlugin overrideProcessor = TelegrafPluginCreator.createBasicStatsAggregatorPlugin(period, namepass, stats, cepId);
		
		System.out.println(overrideProcessor);
	}
}
