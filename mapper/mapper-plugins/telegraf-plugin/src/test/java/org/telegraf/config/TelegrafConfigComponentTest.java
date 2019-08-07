package org.telegraf.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

class TelegrafConfigComponentTest {

	static final String LINE_TESTFILE_LOCATION = "src/test/resources/telegraf.conf";

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
	void toString_CorrectConfigLines_NoFailures() {
		TelegrafConfigComponent component = TelegrafConfigComponent.fromConfigLines(lines);
		
		String[] componentLines = component.toString().split("\\r?\\n");
		
		IntStream.range(0, lines.size()-1).forEach(i -> {
			assertEquals(lines.get(i).toString(), componentLines[i]);
			});
	}
}
