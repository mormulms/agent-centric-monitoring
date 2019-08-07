package org.telegraf.config;

public enum TelegrafConfigLineType {
	DEFAULT, COMMENT, HEADER, BORDER, TABLE, KEY_VALUE;

	//TODO change to precise regexps
	public static TelegrafConfigLineType fromString(String line) {
		if (line.isEmpty()) {
			return DEFAULT;
		}
		if (line.matches("#{79}")) {
			return BORDER;
		}
		if(line.contains("##")) {
			return COMMENT;
		}
		if(line.matches("#[^#]{77}#")) {
			return HEADER;
		}
		if(line.contains(" = ")) {
			return KEY_VALUE;
		}
		if (line.contains("[") && line.contains("]")) {
			return TABLE;
		}
		return DEFAULT;
	} 
}
