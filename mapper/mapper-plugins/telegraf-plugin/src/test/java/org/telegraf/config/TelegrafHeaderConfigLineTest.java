package org.telegraf.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.telegraf.config.TelegrafHeaderConfigLine;
import org.telegraf.config.TelegrafPluginType;

class TelegrafHeaderConfigLineTest {

	static final String INPUT_HEADER = "#                            INPUT PLUGINS                                    #";
	static final String SERVICE_INPUT_HEADER = "#                            SERVICE INPUT PLUGINS                            #";
	static final String OUTPUT_HEADER = "#                            OUTPUT PLUGINS                                   #";
	static final String AGGREGATOR_HEADER = "#                            AGGREGATOR PLUGINS                               #";
	static final String PROCESSOR_HEADER = "#                            PROCESSOR PLUGINS                                #";
	
	@Test
	void toString_AllPossibleHeaders_Success() {
		assertEquals(INPUT_HEADER, new TelegrafHeaderConfigLine(INPUT_HEADER).toString());
		assertEquals(SERVICE_INPUT_HEADER, new TelegrafHeaderConfigLine(SERVICE_INPUT_HEADER).toString());
		assertEquals(OUTPUT_HEADER, new TelegrafHeaderConfigLine(OUTPUT_HEADER).toString());
		assertEquals(AGGREGATOR_HEADER, new TelegrafHeaderConfigLine(AGGREGATOR_HEADER).toString());
		assertEquals(PROCESSOR_HEADER, new TelegrafHeaderConfigLine(PROCESSOR_HEADER).toString());
	}

	@Test
	void getContent_AllPossibleHeaders_Success() {
		assertEquals(TelegrafPluginType.INPUT, (TelegrafPluginType) new TelegrafHeaderConfigLine(INPUT_HEADER).getContent());
		assertEquals(TelegrafPluginType.SERVICE_INPUT, (TelegrafPluginType) new TelegrafHeaderConfigLine(SERVICE_INPUT_HEADER).getContent());
		assertEquals(TelegrafPluginType.OUTPUT, (TelegrafPluginType) new TelegrafHeaderConfigLine(OUTPUT_HEADER).getContent());
		assertEquals(TelegrafPluginType.AGGREGATOR, (TelegrafPluginType) new TelegrafHeaderConfigLine(AGGREGATOR_HEADER).getContent());
		assertEquals(TelegrafPluginType.PROCESSOR, (TelegrafPluginType) new TelegrafHeaderConfigLine(PROCESSOR_HEADER).getContent());
	}
}
