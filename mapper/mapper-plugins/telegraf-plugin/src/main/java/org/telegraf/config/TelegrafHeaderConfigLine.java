package org.telegraf.config;

public class TelegrafHeaderConfigLine extends TelegrafConfigLine {

	private static final String HEADER_PREFIX = "#                            ";
	private static final String HEADER_PLUGINS_PART = " PLUGINS";
	private static final String HEADER_SUFFIX = "#";
	
	TelegrafPluginType content;
	
	public TelegrafHeaderConfigLine(String line) {

		this.type = TelegrafConfigLineType.HEADER;
		this.commentAndIndentationAllowed = false;

		this.content = TelegrafPluginType.parseHeader(line);
		
	}

	@Override
	public Object getContent() {
		return content;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(HEADER_PREFIX);
		builder.append(content.toString());
		builder.append(HEADER_PLUGINS_PART);
		
		for(int numberOfMissingSpaces = HEADER_WIDTH-builder.length()-1;numberOfMissingSpaces>0;numberOfMissingSpaces--) {
			builder.append(' ');
		}
		
		builder.append(HEADER_SUFFIX);
		
		return builder.toString();
	}

}
