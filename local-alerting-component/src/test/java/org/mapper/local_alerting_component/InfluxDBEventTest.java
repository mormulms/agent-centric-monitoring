package org.mapper.local_alerting_component;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

class InfluxDBEventTest {

	final String MULTIPLE_TAGS_LINE = "weather,location=us-midwest,season=summer temperature=82 1465839830100400200";
	final String MINIMAL_LINE ="weather temperature=82 1465839830100400200";
	final String MULTIPLE_FIELDS_LINE = "weather,location=us-midwest temperature=82,humidity=71 1465839830100400200";
	
	@Test
	void fromInfluxDBLine_MinimalInfluxDBLine_ShouldRun() {
		InfluxDBEvent event = InfluxDBEvent.fromInfluxDBLine(MINIMAL_LINE);
		
		assertEquals(event.getMeasurement(), "weather");
		assertEquals(event.getTags().size(), 0);
		assertEquals(event.getFields().size(), 1);
		assertEquals(event.getFields().get("temperature").toString(), "82");
		assertEquals(event.getTimestamp(), "1465839830100400200");
	}
	
	@Test
	void fromInfluxDBLine_MultipleFieldsInfluxDBLine_ShouldRun() {
		InfluxDBEvent event = InfluxDBEvent.fromInfluxDBLine(MULTIPLE_FIELDS_LINE);
		
		assertEquals(event.getMeasurement(), "weather");
		assertEquals(event.getTags().size(), 1);
		assertEquals(event.getFields().size(), 2);
		assertEquals(event.getTags().get("location"), "us-midwest");
		assertEquals(event.getFields().get("temperature").toString(), "82");
		assertEquals(event.getFields().get("humidity").toString(), "71");
		assertEquals(event.getTimestamp(), "1465839830100400200");
	}
	
	@Test
	void fromInfluxDBLine_MultipleTagsInfluxDBLine_ShouldRun() {
		InfluxDBEvent event = InfluxDBEvent.fromInfluxDBLine(MULTIPLE_TAGS_LINE);
		
		assertEquals(event.getMeasurement(), "weather");
		assertEquals(event.getTags().size(), 2);
		assertEquals(event.getFields().size(), 1);
		assertEquals(event.getTags().get("location"), "us-midwest");
		assertEquals(event.getTags().get("season"), "summer");
		assertEquals(event.getFields().get("temperature").toString(), "82");
		assertEquals(event.getTimestamp(), "1465839830100400200");
	}
	
	@Test
	void toString_MinimalInfluxDBLine_ShouldRun() {
		InfluxDBEvent event = InfluxDBEvent.fromInfluxDBLine(MINIMAL_LINE);
		
		assertEquals(MINIMAL_LINE, event.getInfluxDBLine());
	}
	
	@Test
	void toString_MultipleFieldsInfluxDBLine_ShouldRun() {
		InfluxDBEvent event = InfluxDBEvent.fromInfluxDBLine(MULTIPLE_FIELDS_LINE);
		
		assertEquals(MULTIPLE_FIELDS_LINE, event.getInfluxDBLine());
	}
	
	@Test
	void toString_MultipleTagsInfluxDBLine_ShouldRun() {
		InfluxDBEvent event = InfluxDBEvent.fromInfluxDBLine(MULTIPLE_TAGS_LINE);
		
		assertEquals(MULTIPLE_TAGS_LINE, event.getInfluxDBLine());
	}
	
	@Test
	void createStringFromFieldValue_BigInteger_String() {
		Object bigInteger = new BigInteger("12");
		
		assertEquals("12i", InfluxDBEvent.createStringFromFieldValue(bigInteger));
	}

}
