package org.telegraf.config;

import java.util.LinkedList;

import lombok.Getter;

public class TelegrafTableConfigLine extends TelegrafConfigLine {

	public static final String TABEL_PREFIX = "[";
	public static final String TABEL_SUFFIX = "]";
	public static final String TABLE_NAME_SEPARATOR = "\\.";

	// TODO make levels more generic
	@Getter
	private final String[] tableNameComponents;
	@Getter
	private final boolean isPartOfArray;

	public TelegrafTableConfigLine(String line) {

		this.type = TelegrafConfigLineType.TABLE;
		this.commentAndIndentationAllowed = true;
		this.commentsAndIndentations = new LinkedList<String>();

		String preprocessedLine = processCommentsAndIndentationFromStringIntoList(line, commentsAndIndentations);
		this.isPartOfArray = isPartOfArrayFromString(preprocessedLine);

		tableNameComponents = extractTableNameComponentsFromFullName(extractFullTableName(preprocessedLine));
	}

	public TelegrafTableConfigLine(TelegrafPluginType pluginType, String pluginName, boolean multiplePossible) {
		this.type = TelegrafConfigLineType.TABLE;
		this.commentAndIndentationAllowed = true;
		this.commentsAndIndentations = new LinkedList<String>();
		
		this.isPartOfArray = multiplePossible;
		
		tableNameComponents = new String[2];
		tableNameComponents[0] = pluginType.toTableName();
		tableNameComponents[1] = pluginName;
	}

	public TelegrafTableConfigLine(String[] tableComponents, boolean multiplePossible) {
		this.type = TelegrafConfigLineType.TABLE;
		this.commentAndIndentationAllowed = true;
		this.commentsAndIndentations = new LinkedList<String>();
		
		this.isPartOfArray = multiplePossible;
		this.tableNameComponents = tableComponents;
	}

	private String extractFullTableName(String processedLine) {
		return processedLine.replaceAll("[\\[\\]]", " ").trim();
	}

	private boolean isPartOfArrayFromString(String line) {

		return line.contains(TABEL_PREFIX + TABEL_PREFIX) && line.contains(TABEL_SUFFIX + TABEL_SUFFIX);
	}

	private String[] extractTableNameComponentsFromFullName(String processedLine) {
		return processedLine.split(TABLE_NAME_SEPARATOR);
	}

	@Override
	public Object getContent() {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < tableNameComponents.length - 1; i++) {
			builder.append(tableNameComponents[i]);
			builder.append(".");
		}
		builder.append(tableNameComponents[tableNameComponents.length - 1]);

		return builder.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(createCommentAndIndentationPrefix());
		builder.append(TABEL_PREFIX);
		if (isPartOfArray) {
			builder.append(TABEL_PREFIX);
		}
		for (int i = 0; i < tableNameComponents.length - 1; i++) {
			builder.append(tableNameComponents[i]);
			builder.append(".");
		}
		builder.append(tableNameComponents[tableNameComponents.length - 1]);
		if (isPartOfArray) {
			builder.append(TABEL_SUFFIX);
		}
		builder.append(TABEL_SUFFIX);

		return builder.toString();
	}

	public boolean isPluginLine() {
		return (tableNameComponents.length == 2);
	}

	public static TelegrafTableConfigLine createPluginTable(TelegrafPluginType pluginType, String pluginName,boolean multiplePossible) {
		return new TelegrafTableConfigLine(pluginType, pluginName, multiplePossible);
	}
	
	public static TelegrafTableConfigLine createTable(String[] tableComponents,boolean multiplePossible) {
		return new TelegrafTableConfigLine(tableComponents, multiplePossible);
	}

}
