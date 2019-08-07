package org.telegraf.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TelegrafPluginTest {

	static final String TESTFILE_LOCATION = "src/test/resources/telegraf.conf";
	static final String LINE_TESTFILE_LOCATION = "src/test/resources/telegraf-line-test.conf";

	static List<TelegrafConfigLine> lines;

	@BeforeAll
	static void loadLines() {

		lines = new LinkedList<TelegrafConfigLine>();

		File configFile = new File(LINE_TESTFILE_LOCATION);

		// finally is not needed because BufferedReader implements Autoclosable
		try (BufferedReader reader = new BufferedReader(new FileReader(configFile));) {
			String currentLine;
			while ((currentLine = reader.readLine()) != null) {
				lines.add(TelegrafConfigLine.fromString(currentLine));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	void getOptionByName_CorrectConfigLines_NoFailures() {
		
		TelegrafPlugin outputsFile = TelegrafPlugin.fromConfigLines(lines.subList(4, 12));
		
		List<String> expectedForDataFormat = new LinkedList<String>();
		expectedForDataFormat.add("\"influx\"");
		
		List<String> expectedForFiles = new LinkedList<String>();
		expectedForFiles.add("\"stdout\"");
		expectedForFiles.add("\"test\"");
		expectedForFiles.add("\"/tmp/metrics.out\"");		
		
		assertEquals(null, outputsFile.getOptionByName("nullKey"));
		assertEquals(expectedForDataFormat, outputsFile.getOptionByName("data_format"));
		assertEquals(expectedForFiles, outputsFile.getOptionByName("files"));
		
		
		TelegrafPlugin processorsConverter = TelegrafPlugin.fromConfigLines(lines.subList(21, 32));
		String[] processorOptions = {"string","integer","unsigned","boolean","float"};
		for (TelegrafPluginSubComponent subComponent : processorsConverter.getSubComponents()) {
			for (String name : processorOptions) {
				assertTrue(subComponent.getOptionByName(name).isEmpty());
			}
			
		}
		
		assertNull(processorsConverter.getOptionByName("empty"));
		
	}
	


}
