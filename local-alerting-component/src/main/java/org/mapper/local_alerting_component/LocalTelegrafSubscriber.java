package org.mapper.local_alerting_component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalTelegrafSubscriber {

	private static final String CONDITION = "Condition";

	public static final String TELEGRAF_HTTP_LISTENER_URL = "http://127.0.0.1:9999/telegraf";
	
	private static Logger LOGGER = LoggerFactory.getLogger(LocalTelegrafSubscriber.class);
	
	private Invocation.Builder requestBuilder;
	private String alertNname;
	private Map<String, String> alertEventTags;
	private Map<String, Object> alertEventFields;

	
	public LocalTelegrafSubscriber(Builder requestBuilder, String alertName, Map<String, String> alertEventTags, Map<String, Object> alertEventFields) {
		super();
		this.requestBuilder = requestBuilder;
		this.alertNname = alertName;
		this.alertEventTags = alertEventTags;
		this.alertEventFields = alertEventFields;
	}

	
	public static LocalTelegrafSubscriber instanceWithConditionName(String conditionName) {
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
		
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(TELEGRAF_HTTP_LISTENER_URL);

		String alertName = conditionName.split(CONDITION)[0];
		
		Map<String, String> alertEventTags = new LinkedHashMap<String, String>();
		alertEventTags.put("host_address", "placeholder");
		
		Map<String, Object> alertEventFields = new LinkedHashMap<String, Object>();
		alertEventFields.put(conditionName, new BigDecimal(1));
		
		return new LocalTelegrafSubscriber(webTarget.request(), alertName, alertEventTags, alertEventFields);
	}
	
	
	public void update(InfluxDBEvent influxDBEvent) {
		
		InfluxDBEvent alertEvent = new InfluxDBEvent(alertNname, alertEventTags, alertEventFields, influxDBEvent.getTimestamp());
		
		String influxDBLine = alertEvent.getInfluxDBLine();
		
		LOGGER.info("sending: "+influxDBLine);
		
		requestBuilder.post(Entity.text(influxDBLine));
	}
	
	
}
