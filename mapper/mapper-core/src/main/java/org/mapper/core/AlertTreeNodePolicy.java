package org.mapper.core;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class AlertTreeNodePolicy {
	
	private String id;
	private Map<String,String> policies;
	private List<AlertTreeNodePolicy> children;
}
