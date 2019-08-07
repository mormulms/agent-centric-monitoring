package org.mapper.model;

import lombok.Data;

@Data
public class AlertConditionGroupBy {
	String field;
	String type;

	public AlertConditionGroupBy(String field) {
		super();
		this.field = field;
	}

	public AlertConditionGroupBy(String field, String type) {
		super();
		this.field = field;
		this.type = type;
	}
}
