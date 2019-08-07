package org.telegraf.config;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.Setter;

public class TelegrafConfigComponent {

	@Getter
	TelegrafComponentType type;
	@Getter
	@Setter
	List<TelegrafConfigLine> lines;
	
	TelegrafConfigComponent(TelegrafComponentType type, List<TelegrafConfigLine> lines) {
		super();
		this.type = type;
		this.lines = lines;
	}

	TelegrafConfigComponent(TelegrafComponentType type) {
		super();
		this.type = type;
	}

	public static TelegrafConfigComponent fromConfigLines(List<TelegrafConfigLine> lines) {
		return new TelegrafConfigComponent(TelegrafComponentType.DEFAULT, lines);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		IntStream.range(0, lines.size()).forEach(i ->{
			builder.append(lines.get(i));
			builder.append('\n');
		});
		
		return builder.toString();
	}

	public static TelegrafConfigComponent createPluginSpacer() {
		List<TelegrafConfigLine> emptyLines = new LinkedList<TelegrafConfigLine>();
		
		for (int i = 1; i <= TelegrafConfig.EMPYT_LINES_AT_END_OF_PLUGIN; i++) {
			emptyLines.add(TelegrafConfigLine.createEmptyLine());
		}
		
		return new TelegrafConfigComponent(TelegrafComponentType.DEFAULT, emptyLines);
	}
	
}
