package org.grafana.plugin;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfluxDBInstance {

	private static final String HOSTADDRESS_TAG = "host_address";
	private static Logger LOGGER = LoggerFactory.getLogger(InfluxDBInstance.class);

	private String url;
	private String username;
	private String password;
	private InfluxDB influxDB;

	public InfluxDBInstance(String url) {
		super();
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
		this.url = url;
	}

	public InfluxDBInstance(String url, String username, String password) {
		super();
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
		this.url = url;
		this.username = username;
		this.password = password;
	}

	private void connect() {
		if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
			influxDB = InfluxDBFactory.connect(url);
		} else {
			influxDB = InfluxDBFactory.connect(url, username, password);
		}
		LOGGER.info("InfluxDBInstance is connected");
	}

	private void disconnect() {
		influxDB.close();
		influxDB = null;
		LOGGER.info("InfluxDBInstance is disconnected");
	}

	public List<String> getMeasurementSources(String measurement, String database) {
		List<String> measurementSources = new LinkedList<String>();
		LOGGER.info("getMeasurementSource(" + measurement + "," + database + ")");
		this.connect();
		influxDB.setDatabase(database);
		Query hostaddressQuery = new Query("SELECT LAST(*) FROM " + measurement + " GROUP BY " + HOSTADDRESS_TAG);
		QueryResult hostaddressResults = influxDB.query(hostaddressQuery);
		if (hostaddressResults.hasError()) {
			LOGGER.info("getMeasurementSource() - query failed");
			return null;
		}
		for (Series series : hostaddressResults.getResults().get(0).getSeries()) {
			LOGGER.info("measurementSource: " + series.getTags().get(HOSTADDRESS_TAG));
			measurementSources.add(series.getTags().get(HOSTADDRESS_TAG));
		}
		this.disconnect();
		return measurementSources;
	}

}
