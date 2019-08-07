package org.telegraf.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;

public class TelegrafKeyValueConfigLine extends TelegrafConfigLine {

	public static final String KEY_VALUE_SEPARATOR = " = ";
	public static final String ARRAY_PREFIX = "[";
	public static final String ARRAY_SUFFIX = "]";
	public static final String ARRAY_VALUE_SEPARATOR = ",";
	public static final String QUOTE = "\"";
	private static final CharSequence EMPTY = "";
	
	
	@Getter String key;
	@Getter List<String> values;
	
	//TODO change to final?
	@Getter private boolean valueIsArray;
	//TODO values are boolean?
	private boolean valuesAreStrings;
	
	public TelegrafKeyValueConfigLine(String line) {
		this.type = TelegrafConfigLineType.KEY_VALUE;
		this.commentAndIndentationAllowed = true;
		this.commentsAndIndentations = new LinkedList<String>();
		
		String processedString = processCommentsAndIndentationFromStringIntoList(line, commentsAndIndentations);
		
//		this.valueIsArray = valueIsArrayFromString(processedString);
		this.key = extractKeyFromString(processedString);
		this.values = extractValuesFromString(processedString);
	}

	public TelegrafKeyValueConfigLine(String key, List<String> values, List<String> commentsAndIndentations, boolean valueIsArray, boolean valuesAreStrings) {
		this.type = TelegrafConfigLineType.KEY_VALUE;
		this.commentAndIndentationAllowed = true;
		this.commentsAndIndentations = new LinkedList<String>(commentsAndIndentations);
		
		this.valueIsArray = (valueIsArray?true:(values.size()>1));
		this.valuesAreStrings = valuesAreStrings;
		this.key = key;
		this.values = values;
	}

	private List<String> extractValuesFromString(String processedString) {

		String[] splitLine = processedString.split(KEY_VALUE_SEPARATOR);
		List<String> extractedValues = new LinkedList<String>();
		
		if (splitLine.length == 2) {
			String value = splitLine[1].trim();
			this.valueIsArray = valueIsArrayFromString(value);
			this.valuesAreStrings = valueContainsStrings(value);
			if (valuesAreStrings) {
				value = removeQuotes(value);
			}
			if (valueIsArray) {
				value = removeArrayBrackets(value);

				for (String extractedValue : value.split(ARRAY_VALUE_SEPARATOR)) {
					if (!extractedValue.isEmpty()) {
						extractedValues.add(extractedValue);
					}
				}
			} else {
				extractedValues.add(value);
			}
		}
		return extractedValues;
	}

	private String removeQuotes(String value) {
		return value.replace(QUOTE,EMPTY);
	}

	private boolean valueContainsStrings(String value) {
		return value.contains(QUOTE);
	}

	private String removeArrayBrackets(String value) {
		return value.replaceAll("[\\[\\]]","");
	}

	private String extractKeyFromString(String processedString) {
		
		String[] splitLine = processedString.split(KEY_VALUE_SEPARATOR);
		return splitLine[0].trim();
	}

	private boolean valueIsArrayFromString(String line) {
		return line.matches("\\[.*\\]");
	}

	@Override
	public Object getContent() {
		return new HashMap<String, List<String>>().put(key, values);
	}

	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		builder.append(createCommentAndIndentationPrefix());
		builder.append(key);
		builder.append(KEY_VALUE_SEPARATOR);
		builder.append(createValueString());
		
		return builder.toString();
	}

	private String createValueString() {
		StringBuilder builder = new StringBuilder();
		
		if(valueIsArray) {
			builder.append(ARRAY_PREFIX);
			int i = 0;
			for (; i < values.size()-1; i++) {
				
				builder.append(addQuotesIfNeeded(values.get(i)));
				builder.append(ARRAY_VALUE_SEPARATOR);
			}
			if (!values.isEmpty()) {
				builder.append(addQuotesIfNeeded(values.get(i)));
			}
			builder.append(ARRAY_SUFFIX);
		} else {
			if (values.size()!=0) {
				builder.append(addQuotesIfNeeded(values.get(0)));
			}
		}
		return builder.toString();
	}

	private String addQuotesIfNeeded(String string) {
		if (valuesAreStrings) {
			return QUOTE+string+QUOTE;
		}
		return string;
	}

	public static TelegrafKeyValueConfigLine createFromKeyValuesAndIndentation(String key, List<String> values, List<String> commentsAndIndentations, boolean valueIsArray, boolean valuesAreStrings) {
		return new TelegrafKeyValueConfigLine(key, values, commentsAndIndentations, valueIsArray, valuesAreStrings);
	}

}
