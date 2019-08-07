package org.grafana.plugin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InfluxDBInstanceTest {

	private static final String URL = "http://127.0.0.1:8086";
	private static final String USERNAME = "";
	private static final String PASSWORD = "";

	
	@Test
	void test() {
		InfluxDBInstance instance = new InfluxDBInstance(URL);
		instance.getMeasurementSources("win_mem", "telegraf");
	}

}
