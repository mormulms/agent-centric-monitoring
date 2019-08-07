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
import org.telegraf.config.TelegrafConfigLineType;

class TelegrafConfigLineTypeTest {
	
	static final String LINE_TESTFILE_LOCATION = "src/test/resources/telegraf-line-test.conf";
	
	static final String SINGLE_NUMER_LINE = "#";
	
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
	void fromString_SingleNumberLine_NoFailures() {

		assertEquals(TelegrafConfigLineType.DEFAULT, TelegrafConfigLineType.fromString(SINGLE_NUMER_LINE));
		
	}
	
	@Test
	void fromString_SingleConfigLines_NoFailures() {

		assertEquals(TelegrafConfigLineType.COMMENT, TelegrafConfigLineType.fromString(lines.get(5)));
		assertEquals(TelegrafConfigLineType.DEFAULT, TelegrafConfigLineType.fromString(lines.get(12)));
		assertEquals(TelegrafConfigLineType.HEADER, TelegrafConfigLineType.fromString(lines.get(1)));
		assertEquals(TelegrafConfigLineType.KEY_VALUE, TelegrafConfigLineType.fromString(lines.get(11)));
		assertEquals(TelegrafConfigLineType.TABLE, TelegrafConfigLineType.fromString(lines.get(4)));
		
	}
	
	@Test
	void fromString_CommentConfigLines_NoFailures() {

		IntStream commentLines = IntStream.concat(IntStream.range(5, 10),IntStream.range(21, 26));
		
		commentLines.forEach(i -> assertEquals(TelegrafConfigLineType.COMMENT, TelegrafConfigLineType.fromString(lines.get(i))));
		
	}
	
	@Test
	void fromString_DefaultConfigLines_NoFailures() {

		IntStream defaultLines = IntStream.concat(IntStream.range(12, 15),IntStream.of(3,19));
		
		defaultLines.forEach(i -> assertEquals(TelegrafConfigLineType.DEFAULT, TelegrafConfigLineType.fromString(lines.get(i))));
		
	}
	
	@Test
	void fromString_HeaderConfigLines_NoFailures() {

		IntStream headerLines = IntStream.of(1,16);
		
		headerLines.forEach(i -> assertEquals(TelegrafConfigLineType.HEADER, TelegrafConfigLineType.fromString(lines.get(i))));
		
	}
	
	@Test
	void fromString_BorderConfigLines_NoFailures() {

		IntStream borderLines = IntStream.of(0,2,15,17);
		
		borderLines.forEach(i -> assertEquals(TelegrafConfigLineType.BORDER, TelegrafConfigLineType.fromString(lines.get(i))));
		
	}
	
	@Test
	void fromString_KeyValueConfigLines_NoFailures() {

		IntStream keyValueLines = IntStream.concat(IntStream.range(10, 12),IntStream.range(27, 32));
		
		keyValueLines.forEach(i -> assertEquals(TelegrafConfigLineType.KEY_VALUE, TelegrafConfigLineType.fromString(lines.get(i))));
		
	}
	
	@Test
	void fromString_TableConfigLines_NoFailures() {

		IntStream tableLines = IntStream.of(4,20,26);
		
		tableLines.forEach(i -> assertEquals(TelegrafConfigLineType.TABLE, TelegrafConfigLineType.fromString(lines.get(i))));
		
	}
}
