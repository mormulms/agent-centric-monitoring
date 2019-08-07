package org.esper.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import okhttp3.HttpUrl;

class EsperInstanceAccessTest {

	private static EsperInstance esperInstance;
	
	@BeforeAll
	static void setUp() {
		esperInstance = EsperInstance.getWithHostAddress("127.0.0.1");
		
	}
	
	@Test
	void GetAlerts(){
		
		String alerts = esperInstance.getAlerts();
		System.out.println(alerts);
		
		assertEquals("my-statement\n", alerts);
	}
	

}
