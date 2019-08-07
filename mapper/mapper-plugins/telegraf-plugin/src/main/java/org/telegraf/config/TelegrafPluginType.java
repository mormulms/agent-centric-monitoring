package org.telegraf.config;

//TODO add tests
public enum TelegrafPluginType {
	SERVICE_INPUT, INPUT, OUTPUT, AGGREGATOR, PROCESSOR;
	
	private static final String INPUT_TABLE_NAME = "inputs";
	private static final String OUTPUT_TABLE_NAME = "outputs";
	private static final String AGGREGATOR_TABLE_NAME = "aggregators";
	private static final String PROCESSOR_TABLE_NAME = "processors";
	
	public static TelegrafPluginType parseTableName(String type) {
		switch (type) {
		case INPUT_TABLE_NAME:
			return INPUT;
		case OUTPUT_TABLE_NAME:
			return OUTPUT;
		case AGGREGATOR_TABLE_NAME:
			return AGGREGATOR;	
		case PROCESSOR_TABLE_NAME:
			return PROCESSOR;
			
		default:
			return null;
		}
	}
	
	public static TelegrafPluginType parseHeader(String header) {
		
		for (TelegrafPluginType type : TelegrafPluginType.values()) {
			if (header.contains(type.toString())) {
				return type;
			}
		}
		return null;
	}
	
	
	@Override
	public String toString() {
		switch (this) {
		case SERVICE_INPUT:
			return "SERVICE INPUT";

		default:
			return this.name();
		}
	}

	public String toTableName() {
		switch (this) {
		case INPUT:
			return INPUT_TABLE_NAME;
		case OUTPUT:
			return OUTPUT_TABLE_NAME;
		case AGGREGATOR:
			return AGGREGATOR_TABLE_NAME;
		case PROCESSOR:
			return PROCESSOR_TABLE_NAME;
			
		default:
			return null;
		}
	}
}
