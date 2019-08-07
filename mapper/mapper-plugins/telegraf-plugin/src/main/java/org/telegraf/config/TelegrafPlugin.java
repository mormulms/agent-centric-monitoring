package org.telegraf.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;

public class TelegrafPlugin extends TelegrafConfigComponent {

	private static final String URL = "url";
	private static final String STATS = "stats";
	private static final String TAGPASS = "tagpass";
	private static final String TAGS = "tags";
	private static final String ORDER = "order";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String TO_CEP = "to_cep";
	private static final String NAMEPASS = "namepass";
	private static final String NAMEDROP = "namedrop";
	private static final String TAGEXCLUDE = "tagexclude";
	private static final String TAGINCLUDE = "taginclude";
	private static final String FIELDPASS = "fieldpass";
	private static final String FIELDDROP = "fielddrop";
	private static final String VALUE_QUOTE = "\"";

	@Getter
	String name;
	@Getter
	TelegrafPluginType pluginType;
	@Getter
	boolean multiplePossible;

	LinkedHashMap<String, List<String>> options;
	@Getter
	private List<TelegrafPluginSubComponent> subComponents;
	List<String> commentsAndIndentations;

	public TelegrafPlugin() {
		super(TelegrafComponentType.PLUGIN);
	}

	TelegrafPlugin(List<TelegrafConfigLine> lines) {
		super(TelegrafComponentType.PLUGIN);

		TelegrafTableConfigLine mainTable = getMainTableLine(lines);
		// TODO change
		this.name = mainTable.getTableNameComponents()[1];
		// TODO could be a problem
		this.commentsAndIndentations = mainTable.getCommentsAndIndentations();
		this.multiplePossible = mainTable.isPartOfArray();
		this.pluginType = TelegrafPluginType.parseTableName(mainTable.getTableNameComponents()[0]);
		this.subComponents = new LinkedList<TelegrafPluginSubComponent>();
		// separate pluginLines from subComonentLines
		List<TelegrafConfigLine> pluginLines = new LinkedList<TelegrafConfigLine>();
		List<TelegrafConfigLine> currentSubComponentLines = new LinkedList<TelegrafConfigLine>();
		String[] currentTableComponents = null;
		boolean subComponentLinesReached = false;
		for (TelegrafConfigLine telegrafConfigLine : lines) {
			if (!subComponentLinesReached && telegrafConfigLine.getType() == TelegrafConfigLineType.TABLE) {
				currentTableComponents = ((TelegrafTableConfigLine) telegrafConfigLine).getTableNameComponents();
				if (currentTableComponents.length == 3) {
					subComponentLinesReached = true;
				}
			}
			if (!subComponentLinesReached) {
				pluginLines.add(telegrafConfigLine);
			} else {
				if (telegrafConfigLine.getType() == TelegrafConfigLineType.TABLE) {
					String[] tmpTableComponents = ((TelegrafTableConfigLine) telegrafConfigLine)
							.getTableNameComponents();
					if (!currentSubComponentLines.isEmpty() && tmpTableComponents.length == 3) {
						subComponents.add(
								new TelegrafPluginSubComponent(currentTableComponents[2], currentSubComponentLines));
						currentSubComponentLines = new LinkedList<TelegrafConfigLine>();
						currentTableComponents = tmpTableComponents;
					}
				}
				currentSubComponentLines.add(telegrafConfigLine);
			}
		}
		if (!currentSubComponentLines.isEmpty() && currentTableComponents.length == 3) {
			subComponents.add(new TelegrafPluginSubComponent(currentTableComponents[2], currentSubComponentLines));
			currentSubComponentLines = new LinkedList<TelegrafConfigLine>();
		}

		this.setLines(pluginLines);
		this.options = extractConfigOptionsFromLines(pluginLines);
	}

	TelegrafPlugin(TelegrafPluginType pluginType, String pluginName, boolean multiplePossible) {
		super(TelegrafComponentType.PLUGIN, createPluginLinesFromTypeAndName(pluginType, pluginName, multiplePossible));

		this.name = pluginName;
		this.commentsAndIndentations = new LinkedList<String>();
		this.subComponents = new LinkedList<TelegrafPluginSubComponent>();
		this.multiplePossible = multiplePossible;
		this.pluginType = pluginType;
		this.options = new LinkedHashMap<String, List<String>>();
	}

	private static List<TelegrafConfigLine> createPluginLinesFromTypeAndName(TelegrafPluginType pluginType,
			String pluginName, boolean multiplePossible) {
		List<TelegrafConfigLine> pluginLines = new LinkedList<TelegrafConfigLine>();
		// table
		pluginLines.add(TelegrafTableConfigLine.createPluginTable(pluginType, pluginName, multiplePossible));

		return pluginLines;
	}

	public static TelegrafPlugin fromConfigLines(List<TelegrafConfigLine> lines) {
		return new TelegrafPlugin(lines);
	}

	TelegrafTableConfigLine getMainTableLine(List<TelegrafConfigLine> lines) {
		for (TelegrafConfigLine telegrafConfigLine : lines) {
			if (telegrafConfigLine.type == TelegrafConfigLineType.TABLE) {
				return (TelegrafTableConfigLine) telegrafConfigLine;
			}
		}
		return null;
	}

	LinkedHashMap<String, List<String>> extractConfigOptionsFromLines(List<TelegrafConfigLine> lines) {
		LinkedHashMap<String, List<String>> configOptions = new LinkedHashMap<String, List<String>>();

		for (TelegrafConfigLine telegrafConfigLine : lines) {
			if (telegrafConfigLine.getType() == TelegrafConfigLineType.KEY_VALUE) {
				TelegrafKeyValueConfigLine keyValueLine = (TelegrafKeyValueConfigLine) telegrafConfigLine;
				configOptions.put(keyValueLine.getKey(), keyValueLine.getValues());
			}
		}

		return configOptions;
	}

	public List<String> getOptionByName(String optionName) {
		return options.get(optionName);
	}

	public static TelegrafPlugin createWithTypeAndName(TelegrafPluginType pluginType, String pluginName) {
		return new TelegrafPlugin(pluginType, pluginName, true);
	}

	public static TelegrafPlugin createUniqueWithTypeAndName(TelegrafPluginType pluginType, String pluginName) {
		return new TelegrafPlugin(pluginType, pluginName, false);
	}

	protected void addOption(String key, String value, boolean isDeactivated, boolean valuesAreStrings) {
		List<String> values = new LinkedList<String>();
		values.add(value);
		addOption(key, values, isDeactivated, valuesAreStrings);
	}

	protected void addOptionForceValueArray(String key, String value, boolean isDeactivated, boolean valuesAreStrings) {
		List<String> values = new LinkedList<String>();
		values.add(value);
		addOptionForceValueArray(key, values, isDeactivated, valuesAreStrings);
	}

	protected void addOption(String key, List<String> values, boolean isDeactivated, boolean valuesAreStrings) {

		// add to lines
		TelegrafKeyValueConfigLine lineToAdd = TelegrafKeyValueConfigLine.createFromKeyValuesAndIndentation(key, values,
				commentsAndIndentations, false, valuesAreStrings);
		lineToAdd.indentContent();
		if (isDeactivated) {
			lineToAdd.commentContent();
		}
		lines.add(lineToAdd);

		// add to map
		options.put(key, values);
	}

	// used to force array
	protected void addOptionForceValueArray(String key, List<String> values, boolean isDeactivated,
			boolean valuesAreStrings) {

		// add to lines
		TelegrafKeyValueConfigLine lineToAdd = TelegrafKeyValueConfigLine.createFromKeyValuesAndIndentation(key, values,
				commentsAndIndentations, true, valuesAreStrings);
		lineToAdd.indentContent();
		if (isDeactivated) {
			lineToAdd.commentContent();
		}
		lines.add(lineToAdd);

		// add to map
		options.put(key, values);
	}

	private List<String> addQuotesToValues(List<String> values) {
		for (int i = 0; i < values.size(); i++) {
			values.set(i, VALUE_QUOTE + values.get(i) + VALUE_QUOTE);
		}
		return values;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString());
		for (TelegrafPluginSubComponent subComponent : subComponents) {
			builder.append(subComponent.toString());
		}
		return builder.toString();
	}

	public void addSubComponent(TelegrafPluginSubComponent subComponent) {
		for (TelegrafConfigLine subComponentLine : subComponent.getLines()) {
			subComponentLine.indentContent();
		}
		// add tagdrop and tagpass only at the end of the plugin
		if (!subComponent.getName().equals(TelegrafPluginSubComponent.TAGDROP) && !subComponents.isEmpty()
				&& subComponents.get(subComponents.size() - 1).name
						.matches(TelegrafPluginSubComponent.TAGPASS + "|" + TelegrafPluginSubComponent.TAGDROP)) {
			subComponents.add(subComponents.size() - 1, subComponent);
		} else {
			subComponents.add(subComponent);
		}
	}

	public void addTagpass(Map<String, List<String>> tagsAndPassValues) {
		ArrayList<String> tableComponents = new ArrayList<String>();
		tableComponents.add(pluginType.toTableName());
		tableComponents.add(name);

		addSubComponent(TelegrafPluginSubComponent.createTagPassSubComponent(tableComponents, tagsAndPassValues));
	}

	public void addTagdrop(Map<String, List<String>> tagsAndDropValues) {
		ArrayList<String> tableComponents = new ArrayList<String>();
		tableComponents.add(pluginType.toTableName());
		tableComponents.add(name);

		addSubComponent(TelegrafPluginSubComponent.createTagDropSubComponent(tableComponents, tagsAndDropValues));
	}

	public void addAdditionalTags(Map<String, List<String>> tagsAndValues) {
		ArrayList<String> tableComponents = new ArrayList<String>();
		tableComponents.add(pluginType.toTableName());
		tableComponents.add(name);

		addSubComponent(TelegrafPluginSubComponent.createAddTagInputSubComponent(tableComponents, tagsAndValues));
	}

	public void addToCepTagPassWithValueToOutputPlugin(boolean toCepValue) {

		// tagexclude
		List<String> tagsToExclude = new LinkedList<String>();
		tagsToExclude.add(TO_CEP);
		addOptionForceValueArray(TAGEXCLUDE, tagsToExclude, false, true);

		// tagpass
		ArrayList<String> tableComponents = new ArrayList<String>();
		tableComponents.add(pluginType.toTableName());
		tableComponents.add(name);

		Map<String, List<String>> tagsAndPassValues = new LinkedHashMap<String, List<String>>();
		List<String> passValues = new LinkedList<String>();
		passValues.add((toCepValue ? TRUE : FALSE));
		tagsAndPassValues.put(TO_CEP, passValues);

		addSubComponent(TelegrafPluginSubComponent.createTagPassSubComponent(tableComponents, tagsAndPassValues));
	}

	public void addFieldpass(String... fieldsToPass) {
		List<String> fields = new LinkedList<String>();
		for (String field : fieldsToPass) {
			fields.add(field);
		}
		addFilter(FIELDPASS, fields);
	}

	public void addFielddrop(String... fieldsToDrop) {
		List<String> fields = new LinkedList<String>();
		for (String field : fieldsToDrop) {
			fields.add(field);
		}
		addFilter(FIELDDROP, fields);
	}

	public void addTagInclude(String... tagsToInclude) {
		List<String> tags = new LinkedList<String>();
		for (String tag : tagsToInclude) {
			tags.add(tag);
		}
		addFilter(TAGINCLUDE, tags);
	}

	public void addTagExclude(String... tagsToExclude) {
		List<String> tags = new LinkedList<String>();
		for (String tag : tagsToExclude) {
			tags.add(tag);
		}
		addFilter(TAGEXCLUDE, tags);
	}

	public void addNamepass(String... valuesToPassOn) {
		List<String> values = new LinkedList<String>();
		for (String value : valuesToPassOn) {
			values.add(value);
		}

		addFilter(NAMEPASS, values);
	}

	public void addNamedrop(String... valuesToDropOn) {
		List<String> values = new LinkedList<String>();
		for (String value : valuesToDropOn) {
			values.add(value);
		}

		addFilter(NAMEDROP, values);
	}

	private void addFilter(String type, List<String> valuesToFilterOn) {
		addOptionForceValueArray(type, valuesToFilterOn, false, true);
	}

	public void addOrder(int order) {
		List<String> orderOption = new LinkedList<String>();
		orderOption.add(Integer.toString(order));

		addOption(ORDER, orderOption, false, false);

	}

	public boolean hasTagpassForTagsWithValues(Map<String, String []> tagsAndValues) {
		TelegrafPluginSubComponent tagpassCompnent = getSubComponentByName(TAGPASS);
		return hasOptionsWithKeyAndValues(tagsAndValues, tagpassCompnent);
	}

	private boolean hasOptionsWithKeyAndValues(Map<String, String[]> tagsAndValues,
			TelegrafPluginSubComponent tagpassCompnent) {
		if (tagpassCompnent != null) {
			for (String tag : tagsAndValues.keySet()) {
				for (String value : tagsAndValues.get(tag)) {
					if (!tagpassCompnent.getOptionByName(tag).contains(value)) {
						return false;
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}
	

	private TelegrafPluginSubComponent getSubComponentByName(String subComponentName) {
		for (TelegrafPluginSubComponent subComponent : getSubComponents()) {
			if (subComponent.getName().equals(subComponentName)) {
				return subComponent;
			}
		}
		return null;
	}

	public boolean hasTagexcludeForTags(String... tags) {
		List<String> tagsToExlclude = getOptionByName(TAGEXCLUDE);
		if (tagsToExlclude != null) {
			for (String tag : tags) {
				if (!tagsToExlclude.contains(tag)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public void removeTagexcludeForTags(String... tags) {
		removeValuesFromOption(TAGEXCLUDE, tags);
	}

	protected void removeValuesFromOption(String optionName, String... values) {
		List<String> optionValues = getOptionByName(optionName);
		for (String string : optionValues) {
		}
		if (optionValues != null) {
			for (String value : values) {
				if (optionValues.contains(value)) {
					optionValues.remove(value);
				}
			}
			if (optionValues.isEmpty()) {
				// remove option
				options.remove(optionName);
				// remove line
				removeConfigOptionLineByName(optionName);
			}
		}
	}

	private void removeConfigOptionLineByName(String optionName) {
		for (int i = 0; i < lines.size(); i++) {
			TelegrafConfigLine line = lines.get(i);
			if (lines.get(i).getType() == TelegrafConfigLineType.KEY_VALUE) {
				TelegrafKeyValueConfigLine optionLine = (TelegrafKeyValueConfigLine) line;
				if (optionLine.getKey().equals(optionName)) {
					lines.remove(i);
					break;
				}
			}
		}
	}

	public void removeTagpassForTagsWithValues(Map<String, String[]> tagsAndValues) {
		TelegrafPluginSubComponent tagpassCompnent = getSubComponentByName(TAGPASS);
		removeValuesFromOptions(tagsAndValues, tagpassCompnent);
	}

	private void removeValuesFromOptions(Map<String, String[]> optionsAndValues,
			TelegrafPluginSubComponent tagpassCompnent) {
		for (String key : optionsAndValues.keySet()) {
			tagpassCompnent.removeValuesFromOption(key, optionsAndValues.get(key));
		}
		// tagpass component not needed anymore
		if (tagpassCompnent.options.isEmpty()) {
			// TODO maybe needs to be changed
			getSubComponents().remove(tagpassCompnent);
		}
	}

	public boolean addsTagsWithValues(Map<String, String[]> tagsAndValues) {
		
		TelegrafPluginSubComponent tagsComponent = getSubComponentByName(TAGS);
		return hasOptionsWithKeyAndValues(tagsAndValues, tagsComponent);
	}

	public void removeAdditionalTags(Map<String, String[]> tagsAndValues) {
		
		TelegrafPluginSubComponent tagsComponent = getSubComponentByName(TAGS);
		removeValuesFromOptions(tagsAndValues, tagsComponent);
	}

	public boolean usesSameAggregationMethode(TelegrafPlugin pluginToAdd) {
		if (getOptionByName(STATS).contains(pluginToAdd.getOptionByName(STATS))) {
			return true;
		} else {
			return false;
		}
	}

	public boolean usesSameHTTPUrl(TelegrafPlugin pluginToCompareTo) {
		
		String httpUrl = (options.containsKey(URL) ? getOptionByName(URL).get(0) : null);
		String httpUrlToCompareTo = (pluginToCompareTo.options.containsKey(URL) ? pluginToCompareTo.getOptionByName(URL).get(0) : null);
		
		if (httpUrl != null && httpUrlToCompareTo != null && httpUrl.equals(httpUrlToCompareTo)) {
			return true;
		} else {
			return false;
		}
	}



}
