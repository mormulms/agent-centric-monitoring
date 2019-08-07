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
import org.telegraf.config.TelegrafConfigLine;

class TelegrafConfigLineTest {

	static final String LINE_TESTFILE_LOCATION = "src/test/resources/telegraf.conf";

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
	void toString_CorrectConfigLines_NoFailures() {
		
		IntStream.range(0, lines.size()).forEach(i -> {
			assertEquals(lines.get(i), TelegrafConfigLine.fromString(lines.get(i)).toString());
			});
	}
	
	@Test
	void commentUncommentWholeLine_CorrectConfigLines_NoFailures() {
		
		IntStream.range(0, lines.size()).forEach(i -> {
			TelegrafConfigLine line = TelegrafConfigLine.fromString(lines.get(i));
			line.commentWholeLine();
			line.uncommenWholeLine();
			assertEquals(lines.get(i), line.toString());
			});
	}
	
	@Test
	void commentUncommentContent_CorrectConfigLines_NoFailures() {
		
		IntStream.range(0, lines.size()).forEach(i -> {
			TelegrafConfigLine line = TelegrafConfigLine.fromString(lines.get(i));
			line.commentWholeLine();
			line.uncommenWholeLine();
			assertEquals(lines.get(i), line.toString());
			});
	}

}
