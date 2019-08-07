package org.telegraf.config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

//TODO change inheritance
public class TelegrafPluginSubComponent extends TelegrafPlugin {

	public static final String TAGPASS = "tagpass";
	public static final String TAGDROP = "tagdrop";
	private static final String TAGS = "tags";

	TelegrafPluginSubComponent(String name, List<TelegrafConfigLine> lines) {

		this.type = TelegrafComponentType.PLUGIN_SUB_COMPONENT;
		this.name = name;
		this.lines = lines;

		TelegrafTableConfigLine mainTable = (TelegrafTableConfigLine) lines.get(0);

		this.commentsAndIndentations = mainTable.getCommentsAndIndentations();
		this.options = extractConfigOptionsFromLines(lines);
		this.multiplePossible = mainTable.isPartOfArray();
	}

	public static TelegrafPluginSubComponent createWithTableComponents(String[] tableComponents,
			boolean multiplePossible) {
		List<TelegrafConfigLine> lines = new LinkedList<TelegrafConfigLine>();
		lines.add(TelegrafTableConfigLine.createTable(tableComponents, multiplePossible));

		String name = tableComponents[tableComponents.length - 1];
		return new TelegrafPluginSubComponent(name, lines);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		IntStream.range(0, lines.size()).forEach(i -> {
			builder.append(lines.get(i));
			builder.append('\n');
		});

		return builder.toString();
	}

	public static TelegrafPluginSubComponent createTagDropSubComponent(ArrayList<String> tableComponents,
			Map<String, List<String>> tagsAndDropValues) {
		return createTagSubComponent(TAGDROP, tableComponents, tagsAndDropValues);
	}

	public static TelegrafPluginSubComponent createTagPassSubComponent(ArrayList<String> tableComponents,
			Map<String, List<String>> tagsAndPassValues) {
		return createTagSubComponent(TAGPASS, tableComponents, tagsAndPassValues);
	}

	public static TelegrafPluginSubComponent createAddTagInputSubComponent(ArrayList<String> tableComponents,
			Map<String, List<String>> tagsAndValues) {
		return createTagSubComponent(TAGS, tableComponents, tagsAndValues);
	}

	private static TelegrafPluginSubComponent createTagSubComponent(String type, ArrayList<String> tableComponents,
			Map<String, List<String>> tagsAndFilterValues) {
		tableComponents.add(type);

		TelegrafPluginSubComponent tagDropComponent = TelegrafPluginSubComponent
				.createWithTableComponents(tableComponents.toArray(new String[0]), false);
		for (String tag : tagsAndFilterValues.keySet()) {
			if (type.equals(TAGS)) {
				tagDropComponent.addOption(tag, tagsAndFilterValues.get(tag), false, true);
			} else {
				tagDropComponent.addOptionForceValueArray(tag, tagsAndFilterValues.get(tag), false, true);
			}
		}

		return tagDropComponent;
	}

}
