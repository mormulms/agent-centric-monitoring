package org.mapper.model;

import lombok.Data;

@Data
public class AlertConditionTimeWindow{
	String duration;
	String end;
	
	public AlertConditionTimeWindow(String duration, String end) {
		super();
		this.duration = duration;
		this.end = end;
	}
	
}
