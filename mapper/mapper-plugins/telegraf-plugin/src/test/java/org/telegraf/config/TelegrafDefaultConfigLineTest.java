package org.telegraf.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.telegraf.config.TelegrafDefaultConfigLine;

class TelegrafDefaultConfigLineTest {

	static final String LINE_TESTFILE_LOCATION = "src/test/resources/telegraf-line-test.conf";

	static List<String> lines;

	@BeforeAll
	static void loadLines() {

		lines = new LinkedList<String>();

		File configFile = new File(LINE_TESTFILE_LOCATION);

		// finally is not needed because BufferedReader implements Autoclosable
		try (BufferedReader reader = new BufferedReader(new FileReader(configFile));) {
			String currentLine;
			while ((currentLine = reader.readLine()) != null) {
				lines.add(currentLine);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	void toString_DefaultConfigLines_NoFailures() {

		IntStream defaultLines = IntStream.concat(IntStream.range(12, 15),IntStream.of(3,19));
		
		defaultLines.forEach(i -> assertEquals(lines.get(i), new TelegrafDefaultConfigLine(lines.get(i)).toString()));
		
	}
	
}
