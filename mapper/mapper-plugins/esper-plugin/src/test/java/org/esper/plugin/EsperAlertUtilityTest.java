package org.esper.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapper.model.AlertCondition;
import org.mapper.model.AlertConditionField;
import org.mapper.model.AlertConditionFilter;
import org.mapper.model.AlertConditionGroupBy;
import org.mapper.model.AlertConditionTimeWindow;

class EsperAlertUtilityTest {

	@Test
	void tagFilterToEsperStatementComponent_AlertConditionFilter_EsperStatementComponent() {
		
		String field = "field";
		String operator = "operator";
		String value = "value";
		
		AlertConditionFilter eval = new AlertConditionFilter(field, operator, value);
		
		assertEquals("tags('"+field+"') "+operator+" '"+value+"'", EsperAlertUtility.tagFilterToEsperStatementComponent(eval));
	}
	
	
	@Test
	void tagFilterToEsperStatementComponent_AlertConditionFilterWithString_EsperStatementComponent() {
		
		String field = "field";
		String operator = "operator";
		String value = "value";
		
		AlertConditionFilter eval = new AlertConditionFilter(true, field, operator, value);
		
		assertEquals("tags('"+field+"') "+operator+" '"+value+"'", EsperAlertUtility.tagFilterToEsperStatementComponent(eval));
	}

	
	@Test
	void fieldToEsperStatementComponent_CorrectAlertCondition_EsperStatementComponent() {
		
		String field = "field";
		String function = "function";

		AlertConditionField subqueryField = new AlertConditionField(function, field);
		
		assertEquals(function+"(cast(fields('"+field+"'),BigDecimal))", EsperAlertUtility.fieldToEsperStatementComponent(subqueryField));
	}
	

	
	@Test
	void alertConditionToEsperStatement_AlertConditionWithOneSelectAndTime_EsperStatement(){
		
		String measurement = "win_mem";
		List<AlertConditionField> selects = new LinkedList<AlertConditionField>();
		selects.add(new AlertConditionField("max", "Available_Bytes"));
		List<AlertConditionFilter> wheres = new LinkedList<AlertConditionFilter>();
		wheres.add(new AlertConditionFilter(true, "host_address", "=", "127.0.0.1"));
		List<AlertConditionGroupBy> groupBys = new LinkedList<AlertConditionGroupBy>();
		groupBys.add(new AlertConditionGroupBy("hosts"));
		
		AlertConditionTimeWindow window = new AlertConditionTimeWindow("1 min", "now"); 
		
		AlertCondition alertCondition = new AlertCondition();
		alertCondition.setSelects(selects);
		alertCondition.setMeasurement(measurement);
		alertCondition.setWheres(wheres);
		alertCondition.setGroupBys(groupBys);
		alertCondition.setTimewindow(window);
		alertCondition.setAlertCondition(new AlertConditionFilter(null, "gt", "10"));
		
		String esperStatement = EsperAlertUtility.alertConditionToEsperStatement(alertCondition);
		assertEquals("SELECT * FROM InfluxDBEvent WHERE (SELECT max(cast(fields('Available_Bytes'),BigDecimal)) FROM InfluxDBEvent(measurement = 'win_mem')#time(1 min) WHERE tags('host_address') = '127.0.0.1' GROUP BY tags('hosts')) > 10", esperStatement);
		
		
	}
	
	@Test
	void alertConditionToEsperStatement_AlertConditionWithTwoSelects_EsperStatement(){
		
		String measurement = "win_mem";
		List<AlertConditionField> selects = new LinkedList<AlertConditionField>();
		selects.add(new AlertConditionField("max", "Available_Bytes"));
		selects.add(new AlertConditionField(null, "null"));
		
		
		AlertCondition alertCondition = new AlertCondition();
		alertCondition.setSelects(selects);
		alertCondition.setMeasurement(measurement);
		alertCondition.setAlertCondition(new AlertConditionFilter(null, "gt", "10"));
		
		String esperStatement = EsperAlertUtility.alertConditionToEsperStatement(alertCondition);
		assertEquals("SELECT * FROM InfluxDBEvent WHERE (SELECT max(cast(fields('Available_Bytes'),BigDecimal)) FROM InfluxDBEvent(measurement = 'win_mem')) > 10 OR (SELECT cast(fields('null'),BigDecimal) FROM InfluxDBEvent(measurement = 'win_mem')) > 10", esperStatement);
		
		
	}
	
	@Test
	void dirty(){
		
		String measurement = "test";
		List<AlertConditionField> selects = new LinkedList<AlertConditionField>();
		selects.add(new AlertConditionField(null, "number"));
		selects.add(new AlertConditionField(null, "number2"));
		
		
		AlertCondition alertCondition = new AlertCondition();
		alertCondition.setSelects(selects);
		alertCondition.setMeasurement(measurement);
		alertCondition.setTimewindow(new AlertConditionTimeWindow("20 sec", "now"));
		alertCondition.setAlertAggregator("sum");
		alertCondition.setAlertCondition(new AlertConditionFilter(null, "gt", "10"));
		
		String esperStatement = EsperAlertUtility.alertConditionToEsperStatement(alertCondition);
		System.out.println(esperStatement);
	}
}
