package org.telegraf.plugin;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

class TelegrafConnectorTest{ 

	TelegrafConnector telegrafConnector;
	
	@BeforeClass
	void setUp() {
		telegrafConnector = new TelegrafConnector();
	}
}
