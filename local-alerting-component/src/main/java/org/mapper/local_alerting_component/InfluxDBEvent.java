package org.mapper.local_alerting_component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;


public class InfluxDBEvent {
	
	
	private static final String QUOTE = "\"";
	private static final int KEY_INDEX = 0;
	private static final int VALUE_INDEX = 1;
	
	//TODO add values for separators
	
	private String measurement;
	private Map<String, String> tags;
	private Map<String, Object> fields;
	
	// TODO other timestamp format
	private String timestamp;

	//TODO maybe add error
	public static InfluxDBEvent fromInfluxDBLine(String influxDBLine) {
		
		String[] elements = influxDBLine.split(" ");
		
		String[] measurementAndTags = elements[0].split(",");
		
		String measurement = measurementAndTags[0];
		
		//linked hashmap is used to keep order of insertion
		Map<String,String> tags = new LinkedHashMap<String, String>();
		//only then do tags exist
		if(measurementAndTags.length>1) {
			for (int i = 1; i < measurementAndTags.length; i++) {
				String[] keyAndValue = measurementAndTags[i].split("=");
				tags.put(keyAndValue[KEY_INDEX], keyAndValue[VALUE_INDEX]);
			}
		}
				
		String[] fieldSet = elements[1].split(",");
		Map<String,Object> fields = new LinkedHashMap<String, Object>();
		
		for (String field : fieldSet) {
			String[] keyAndValue = field.split("=");
			fields.put(keyAndValue[KEY_INDEX], createObjectForFieldValue(keyAndValue[VALUE_INDEX]));
		}
		
		
		String timestamp =	elements[2];	
		
		return new InfluxDBEvent(measurement, tags, fields, timestamp);
	}

	static Object createObjectForFieldValue(String value) {
		//value is String
		if (value.startsWith(QUOTE) && value.endsWith(QUOTE)) {
			return value.substring(1, value.length()-1);
		}
		//value is Integer
		if (value.endsWith("i")) {
			return new BigInteger(value.substring(0, value.length()-1));
		}

		//value is Boolean
		if (stringIsBoolean(value)) {
			return value;
		}
		//value is Float
		return new BigDecimal(value);
		
	}
	
	static String createStringFromFieldValue(Object value) {
		if (value instanceof BigDecimal) {
			return value.toString();
		}
		if (value instanceof BigInteger) {
			return value.toString()+"i";
		}
		if (value instanceof String) {
			String tmp = (String) value;
			if (stringIsBoolean(tmp)) {
				return tmp;
			} else {
				return QUOTE+tmp+QUOTE;
			}
		}
		
		return null;
	}
	
	private static boolean stringIsBoolean(String string) {
		switch (string) {
		case "t":
		case "T":
		case "true":
		case "True":
		case "TRUE":
		case "f":
		case "F":
		case "false":
		case "False":
		case "FALSE":
			return true;
		default:
			return false;
		}	
	}

	public String getInfluxDBLine() {
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append(measurement);
		
		for (String tagKey : tags.keySet()) {
			stringBuilder.append(","+tagKey+"="+tags.get(tagKey));
		}
		
		stringBuilder.append(" ");
		
		int fieldsAddedToStringBuilder = 0;
		for (String fieldKey : fields.keySet()) {
			stringBuilder.append(fieldKey+"="+createStringFromFieldValue(fields.get(fieldKey)));
			//dont put comma after last field
			if (fieldsAddedToStringBuilder < fields.size()-1) {
				stringBuilder.append(",");
			}
			fieldsAddedToStringBuilder++;
		}
		
		stringBuilder.append(" ");

		stringBuilder.append(timestamp);
		
		return stringBuilder.toString();
	}
	
	public InfluxDBEvent(String measurement, Map<String, String> tags, Map<String, Object> fields, String timestamp) {
		super();
		this.measurement = measurement;
		this.tags = tags;
		this.fields = fields;
		this.timestamp = timestamp;
	}

	public InfluxDBEvent(String measurement, String tagSet, String fieldSet, String timestamp) {
		super();
		this.measurement = measurement;
		this.timestamp = timestamp;
	}

	public String getMeasurement() {
		return measurement;
	}


	public String getTimestamp() {
		return timestamp;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public Map<String, Object> getFields() {
		return fields;
	}
	
}
