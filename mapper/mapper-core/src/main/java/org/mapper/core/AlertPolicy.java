package org.mapper.core;

import java.util.Map;

import lombok.Data;

@Data
public class AlertPolicy {
	
	private String id;
	private long version;
	private Map<String,String> policies;
	private AlertTreeNodePolicy alertTree;
}
