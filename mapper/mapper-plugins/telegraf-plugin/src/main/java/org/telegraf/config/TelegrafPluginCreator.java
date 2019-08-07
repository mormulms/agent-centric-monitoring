package org.telegraf.config;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TelegrafPluginCreator {
	
	private static final String MAYBE = "maybe";
	private static final String AGGREGATED = "aggregated";
	private static final String FROM_CEP = "from_cep";
	private static final String METHODS = "methods";
	private static final String PUT = "PUT";
	private static final String PATH = "path";
	private static final String SERVICE_ADDRESS = "service_address";
	private static final String HTTP_LISTENER_V2 = "http_listener_v2";
	private static final String SKIP_DATABASE_CREATION = "skip_database_creation";
	private static final String URLS = "urls";
	private static final String DATABASE_TAG = "database_tag";
	private static final String TELEGRAF = "telegraf";
	private static final String DATABASE = "database";
	private static final String INFLUXDB = "influxdb";
	private static final String STATS = "stats";
	private static final String BASICSTATS = "basicstats";
	private static final String HTTP = "http";
	private static final String OVERRIDE = "override";
	private static final String INFLUX = "influx";
	private static final String DATA_FORMAT = "data_format";
	private static final String POST = "POST";
	private static final String METHOD = "method";
	private static final String URL = "url";
	private static final String DROP_ORIGINAL = "drop_original";
	private static final String PERIOD = "period";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String TO_CEP = "to_cep";
	private static final String BACKLOOP_PORT = "9999";

	public static TelegrafPlugin createMinimalInfluxDBOutputPlugin(String url, String database, boolean skipDatabaseCreation) {
		
		TelegrafPlugin influxDBOutputPlugin = TelegrafPlugin.createWithTypeAndName(TelegrafPluginType.OUTPUT, INFLUXDB);
		
		influxDBOutputPlugin.addOptionForceValueArray(URLS, url, false, true);
		influxDBOutputPlugin.addOption(DATABASE,TELEGRAF, false, true);
		influxDBOutputPlugin.addOption(DATABASE_TAG,"", true, true);
		influxDBOutputPlugin.addOption(SKIP_DATABASE_CREATION, Boolean.toString(skipDatabaseCreation), false, false);
		
		return influxDBOutputPlugin;
	}
	
	public static TelegrafPlugin createBackloopHttpListenerInputPlugin() {
		
		TelegrafPlugin httpListenerInputPlugin = TelegrafPlugin.createWithTypeAndName(TelegrafPluginType.INPUT, HTTP_LISTENER_V2);
		
		httpListenerInputPlugin.addOption(SERVICE_ADDRESS, ":"+BACKLOOP_PORT, false, true);
		httpListenerInputPlugin.addOption(PATH,"/telegraf", true, true);
		List<String> methodValues = new LinkedList<String>();
		methodValues.add(POST);
		methodValues.add(PUT);
		httpListenerInputPlugin.addOption(METHODS,methodValues, true, true);
		httpListenerInputPlugin.addOption(DATA_FORMAT, INFLUX, false, true);
		httpListenerInputPlugin.addAdditionalTags(createTagsOptionWithTagAndValue(TO_CEP, FALSE));
		
		return httpListenerInputPlugin;
	}
	
	public static TelegrafPlugin createCepHttpOutputPlugin(String alertId,String url) {
		
		TelegrafPlugin httpOutputPlugin = TelegrafPlugin.createWithTypeAndName(TelegrafPluginType.OUTPUT, HTTP);
		
		httpOutputPlugin.addOption(URL, url, false, true);
		httpOutputPlugin.addOption(METHOD,POST, true, true);
		httpOutputPlugin.addOption(DATA_FORMAT, INFLUX, true, true);
		
		
		httpOutputPlugin.addTagExclude(TO_CEP);
		httpOutputPlugin.addTagpass(createTagsOptionWithTagAndValue(TO_CEP, alertId));
		httpOutputPlugin.addTagdrop(createTagsOptionWithTagAndValue(AGGREGATED, TRUE));
		
		return httpOutputPlugin;
	}

	public static TelegrafPlugin createOverrideProcessorPlugin(Integer order, String namepass, String cepId, boolean proxy) {
		
		TelegrafPlugin overrideProcessor = TelegrafPlugin.createWithTypeAndName(TelegrafPluginType.PROCESSOR, OVERRIDE);
		
		if (order != null) {
			overrideProcessor.addOrder(order);
		}
		overrideProcessor.addNamepass(namepass);
		
		overrideProcessor.addAdditionalTags(createTagsOptionsForOverrideProcessor(cepId));
		if (!proxy) {
			overrideProcessor.addTagdrop(createTagsOptionWithTagAndValue(TO_CEP, FALSE));
		}
		
		return overrideProcessor;
	}

	
	public static TelegrafPlugin createBasicStatsAggregatorPlugin(String period, String namepass, List<String> stats, String cepId) {
		
		TelegrafPlugin basicStatsAggregator = TelegrafPlugin.createWithTypeAndName(TelegrafPluginType.AGGREGATOR, BASICSTATS);
		
		basicStatsAggregator.addOption(PERIOD, period, false, true);
		basicStatsAggregator.addOption(DROP_ORIGINAL, FALSE, false, false);
		basicStatsAggregator.addOptionForceValueArray(STATS, stats, false, true);
		
		basicStatsAggregator.addNamepass(namepass);
		basicStatsAggregator.addAdditionalTags(createTagsOptionWithTagAndValue(AGGREGATED, TRUE));
		basicStatsAggregator.addTagpass(createTagsOptionWithTagAndValue(TO_CEP, cepId));
		
		return basicStatsAggregator;
	}
	
	//TODO check for correct values
	public static TelegrafPlugin createAggregator(String namepass, String cepId, String period, String methods) {
		List<String> stats = Arrays.asList(methods.split(","));
		System.out.println("correct splitting");
		for (String stat : stats) {
			System.out.println(stat);
		}
		return createBasicStatsAggregatorPlugin(period, namepass, stats, cepId);
	}
	
	
	public static Map<String, List<String>> createToCepFalseTagAndValue() {
		return createToCepWithValueTagAndValue(FALSE);
	}

	public static Map<String, List<String>> createToCepWithValueTagAndValue(String toCepValue) {
		Map<String, List<String>> tagAndValue = new LinkedHashMap<String, List<String>>();
		List<String> tagValues = new LinkedList<String>();
		tagValues.add(toCepValue);
		tagAndValue.put(TO_CEP, tagValues);
		return tagAndValue;
	}
	
	public static Map<String, List<String>> createTagsOptionsForOverrideProcessor(String cepId) {
		Map<String, List<String>> tagsAndValues = new LinkedHashMap<String, List<String>>();
		List<String> tagValues = new LinkedList<String>();
		tagValues.add(cepId);
//		tagsAndValues.putAll(createTagsOptionWithTagAndValue(UNPROCESSED, FALSE));
		tagsAndValues.put(TO_CEP, tagValues);
		return tagsAndValues;
	}
	
	
	public static Map<String, List<String>> createTagsOptionWithTagAndValue(String tag, String value) {
		Map<String, List<String>> tagAndValue = new LinkedHashMap<String, List<String>>();
		List<String> tagValues = new LinkedList<String>();
		tagValues.add(value);
		tagAndValue.put(tag, tagValues);
		return tagAndValue;
	}
	
	public static Map<String, List<String>> createTagsOptionWithTagAndValues(String tag, String... values) {
		Map<String, List<String>> tagAndValue = new LinkedHashMap<String, List<String>>();
		List<String> tagValues = new LinkedList<String>();
		for (String value : values) {
			tagValues.add(value);
		}
		tagAndValue.put(tag, tagValues);
		return tagAndValue;
	}

	public static Map<String, List<String>> createTagpassOptionsForOutputs() {
		Map<String, List<String>> options = new LinkedHashMap<String, List<String>>();

		options.putAll(createTagsOptionWithTagAndValues(TO_CEP, FALSE, MAYBE)); 
		options.putAll(createTagsOptionWithTagAndValue(AGGREGATED, TRUE)); 
//		options.putAll(createTagsOptionWithTagAndValue(UNPROCESSED, TRUE)); 
		
		return options;
	}
	
	public static Map<String, List<String>> createTagpsOptionsForInputs() {
		Map<String, List<String>> options = new LinkedHashMap<String, List<String>>();

		options.putAll(createTagsOptionWithTagAndValues(TO_CEP, MAYBE)); 
		
		return options;
	}
}
