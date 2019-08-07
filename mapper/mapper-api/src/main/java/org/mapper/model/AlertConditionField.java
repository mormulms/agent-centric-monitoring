package org.mapper.model;

import lombok.Data;

@Data
public class AlertConditionField {
	String function;
	String field;

	public AlertConditionField(String function, String field) {
		super();
		this.function = function;
		this.field = field;
	}
}
