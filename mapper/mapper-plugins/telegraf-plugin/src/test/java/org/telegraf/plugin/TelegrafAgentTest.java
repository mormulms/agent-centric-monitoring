package org.telegraf.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TelegrafAgentTest {

	static final String TESTFILE_LOCATION = "src/test/resources/telegraf.conf";
	static final String SAVED_FILE_LOCATION = "src/test/resources/telegraf-saved.conf";

	static final String LOCALHOST = "localhost";
	
	TelegrafAgent telegrafAgent;
	
	
	@BeforeEach
	void setUp() {
		Map<String,String> configuration = new HashMap<String, String>();
		configuration.put("hostaddress", LOCALHOST);
		
		telegrafAgent = new TelegrafAgent(configuration);
	}
	
	@Disabled
	@Test
	void LoadConfiguration() {
		telegrafAgent.loadConfig();
		
	}
	

	@Disabled
	@Test
	void restart() {
		telegrafAgent.restartAgent();
	}
	
	@Test
	void hostAddressWithoutDots_IPAddress() {
		String address = "192.1.192.45";
		
		String processedAddress = TelegrafAgent.hostAddressWithoutDots(address);
		
		assertEquals("192001192045", processedAddress);
	}
	
	
}
