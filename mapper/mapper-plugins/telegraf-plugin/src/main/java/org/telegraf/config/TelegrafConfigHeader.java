package org.telegraf.config;

import java.util.List;

import lombok.Getter;

public class TelegrafConfigHeader extends TelegrafConfigComponent{

	@Getter
	TelegrafPluginType pluginType;
	
	TelegrafConfigHeader(List<TelegrafConfigLine> lines) {
		super(TelegrafComponentType.HEADER, lines);
		//TODO improve
		this.pluginType = (TelegrafPluginType) lines.get(1).getContent();
	}
	
	public static TelegrafConfigHeader fromConfigLines(List<TelegrafConfigLine> lines) {
		return new TelegrafConfigHeader(lines);
	}

	
}
