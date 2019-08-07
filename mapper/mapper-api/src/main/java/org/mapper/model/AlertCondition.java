package org.mapper.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertCondition extends AlertTreeNode{
	
	public static final String CONDITION_SEPARATOR = "Condition";
	
	public AlertCondition() {
		this.deployOn = null;
		this.type = AlertTreeNodeType.CONDITION;
		this.children = null;
	}
	
	List<String> measurementSources;
	String name;
	String eventType;
	
	boolean isProxyCondition;
	
	//query stuff
	String queryName;
	String measurement;
	List<AlertConditionField> selects;
	List<AlertConditionFilter> wheres;
	List<AlertConditionGroupBy> groupBys;
	
	//alertStuff
//	AlertConditionTimeWindow timewindow;
	String alertAggregator;
	AlertConditionFilter alertCondition;
	
	String aggregationInterval;
	String aggregationMethod;

	@Override
	protected AlertCondition getCopyWithoutChildren() {
		AlertCondition copy = new AlertCondition();
		
		copy.setDeployOn(deployOn);
		copy.setTreeName(treeName);
		copy.setTimewindow(timewindow);
		
		copy.setMeasurementSources(measurementSources);
		copy.setName(name);
		copy.setEventType(eventType);
		copy.setQueryName(queryName);
		copy.setMeasurement(measurement);
		copy.setSelects(selects);
		copy.setWheres(wheres);
		copy.setGroupBys(groupBys);
		copy.setAlertAggregator(alertAggregator);
		copy.setAlertCondition(alertCondition);
		
		copy.setAggregationInterval(aggregationInterval);
		copy.setAggregationMethod(aggregationMethod);
		
		return copy;
	}
}
