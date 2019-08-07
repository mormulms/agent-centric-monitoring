package org.mapper.model;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class AlertConditionFilter {
	boolean valueIsString;
	String field;
	String operator;
	List<String> values;
	String filterCombinator;
	
	public AlertConditionFilter(String field, String operator, String value) {
		super();
		this.valueIsString = false;
		this.field = field;
		this.operator = operator;
		LinkedList<String> values = new LinkedList<String>();
		values.add(value);
		this.values = values;
		this.filterCombinator = null;
	}

	public AlertConditionFilter(boolean valueIsString, String field, String operator, String value) {
		super();
		this.valueIsString = valueIsString;
		this.field = field;
		this.operator = operator;
		LinkedList<String> values = new LinkedList<String>();
		values.add(value);
		this.values = values;
		this.filterCombinator = null;
	}

	public AlertConditionFilter(boolean valueIsString, String field, String operator, String value,
			String filterCombinator) {
		super();
		this.valueIsString = valueIsString;
		this.field = field;
		this.operator = operator;
		LinkedList<String> values = new LinkedList<String>();
		values.add(value);
		this.values = values;
		this.filterCombinator = filterCombinator;
	}

	public AlertConditionFilter(boolean valueIsString, String field, String operator, List<String> values,
			String filterCombinator) {
		super();
		this.valueIsString = valueIsString;
		this.field = field;
		this.operator = operator;
		this.values = values;
		this.filterCombinator = filterCombinator;
	}
	
	public AlertConditionFilter(String field, String operator, List<String> values) {
		super();
		this.valueIsString = false;
		this.field = field;
		this.operator = operator;
		this.values = values;
		this.filterCombinator = null;
	}

	public String getValue() {
		if (values != null && !values.isEmpty()) {
			return values.get(0);
		}
		return null;
	}
	
}
