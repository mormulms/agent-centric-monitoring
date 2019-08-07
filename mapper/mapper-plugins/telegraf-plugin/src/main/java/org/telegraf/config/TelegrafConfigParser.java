package org.telegraf.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TelegrafConfigParser {

	private static final int LINES_IN_HEADER = 3;
	private static final int NUMBER_OF_EMPTY_LINES_AFTER_PLUGIN = 2;
	private static final int NUMBER_OF_EMPTY_LINES_AFTER_LAST_PLUGIN = 1;

	public static TelegrafConfig readConfigFromFile(File configFile) {
		List<TelegrafConfigComponent> configComponents = new LinkedList<TelegrafConfigComponent>();

		// finally is not needed because BufferedReader implements Autoclosable
		try (BufferedReader reader = new BufferedReader(new FileReader(configFile));) {

			String line = null;
			List<TelegrafConfigLine> linesOfCurrentComponent = new LinkedList<TelegrafConfigLine>();
			boolean pluginSectionReached = false;
			TelegrafComponentType currentComponentType = TelegrafComponentType.DEFAULT;

			while ((line = reader.readLine()) != null) {

				TelegrafConfigLine configLine = TelegrafConfigLine.fromString(line);
				linesOfCurrentComponent.add(TelegrafConfigLine.fromString(line));

				// skip global config
				if (firstHeaderReached(pluginSectionReached, configLine)) {
					pluginSectionReached = true;
				}

				if (pluginSectionReached) {

					// handle special components
					if (configLine.getType() == TelegrafConfigLineType.BORDER) {

						// end of Header reached
						if (currentComponentType == TelegrafComponentType.HEADER) {
							// preceding default component in linesOfCurrentComponent
							configComponents.addAll(extractDefaultComponentAndHeaderFromLines(linesOfCurrentComponent));
							linesOfCurrentComponent.clear();
							currentComponentType = TelegrafComponentType.DEFAULT;
						} else {
							currentComponentType = TelegrafComponentType.HEADER;
						}
					} else if (configLine.getType() == TelegrafConfigLineType.TABLE) {
						TelegrafTableConfigLine tableLine = (TelegrafTableConfigLine) configLine;
						if (tableLine.isPluginLine()) {
							currentComponentType = TelegrafComponentType.PLUGIN;
						}
					} else if ((currentComponentType == TelegrafComponentType.PLUGIN)
							&& (configLine.getType() == TelegrafConfigLineType.DEFAULT)) {
						// conetent is of type STring
						if (((String) configLine.getContent()).isEmpty()) {
							// check if last line was also empty -> end of plugin reached
							TelegrafConfigLine lastLine = linesOfCurrentComponent
									.get(linesOfCurrentComponent.size() - 2);
							if (lastLine.getType() == TelegrafConfigLineType.DEFAULT) {
								if (((String) lastLine.getContent()).isEmpty()) {
									configComponents
											.addAll(extractDefaultComponentAndPluginFromLines(linesOfCurrentComponent, false));
									linesOfCurrentComponent.clear();
									currentComponentType = TelegrafComponentType.DEFAULT;
								}
							}
						}
					}
				}
			}
			
			// add Last Default component
			configComponents.addAll(extractDefaultComponentAndPluginFromLines(linesOfCurrentComponent, true));
			linesOfCurrentComponent.clear();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new TelegrafConfig(configComponents);
	}

	private static List<TelegrafConfigComponent> extractDefaultComponentAndPluginFromLines(
			List<TelegrafConfigLine> linesOfCurrentComponent, boolean lastPlugin) {
		List<TelegrafConfigComponent> configComponents = new LinkedList<TelegrafConfigComponent>();

		int numberOfPrecedingLines =  getNumberOfPrecedingLines(linesOfCurrentComponent);
		int numberOfNotSucceedingLines;
		if (lastPlugin) {
			numberOfNotSucceedingLines = linesOfCurrentComponent.size()-NUMBER_OF_EMPTY_LINES_AFTER_LAST_PLUGIN;
		} else {
			numberOfNotSucceedingLines = linesOfCurrentComponent.size()-NUMBER_OF_EMPTY_LINES_AFTER_PLUGIN;
		}
		int pluginLength = numberOfNotSucceedingLines-numberOfPrecedingLines;
		
		//handle preceding lines
		List<TelegrafConfigLine> precedingtLines = new LinkedList<TelegrafConfigLine>();
		linesOfCurrentComponent.stream().limit(numberOfPrecedingLines).forEach(defaultLine -> {
			precedingtLines.add(defaultLine);
		});
		configComponents.add(TelegrafConfigComponent.fromConfigLines(precedingtLines));
		
		//add plugin
		List<TelegrafConfigLine> pluginLines = new LinkedList<TelegrafConfigLine>();
		linesOfCurrentComponent.stream().skip(numberOfPrecedingLines).limit(pluginLength).forEach(pluginLine -> {
			pluginLines.add(pluginLine);
		});
		
		configComponents.add(TelegrafPlugin.fromConfigLines(pluginLines));
		
		//add component for 2 empty lines
		List<TelegrafConfigLine> succeedingLines = new LinkedList<TelegrafConfigLine>();
		linesOfCurrentComponent.stream().skip(numberOfNotSucceedingLines).forEach(defaultLine -> {
			succeedingLines.add(defaultLine);
		});
		configComponents.add(TelegrafConfigComponent.fromConfigLines(succeedingLines));

		return configComponents;
	}

	private static int getNumberOfPrecedingLines(List<TelegrafConfigLine> linesOfCurrentComponent) {
		int numberOfPrecedingLines = 0;
		
		for (int i = 0; i < linesOfCurrentComponent.size(); i++) {
			if (linesOfCurrentComponent.get(i).getType() == TelegrafConfigLineType.TABLE) {
				break;
			} else {
				numberOfPrecedingLines++;
			}
		}
		
		return numberOfPrecedingLines;
	}

	private static List<TelegrafConfigComponent> extractDefaultComponentAndHeaderFromLines(
			List<TelegrafConfigLine> linesOfCurrentComponent) {
		List<TelegrafConfigComponent> configComponents = new LinkedList<TelegrafConfigComponent>();

		int sizeOfDefaultComponent = linesOfCurrentComponent.size() - LINES_IN_HEADER;
		if (sizeOfDefaultComponent > 0) {
			List<TelegrafConfigLine> defaultLines = new LinkedList<TelegrafConfigLine>();
			linesOfCurrentComponent.stream().limit(sizeOfDefaultComponent).forEach(defaultLine -> {
				defaultLines.add(defaultLine);
			});
			configComponents.add(TelegrafConfigComponent.fromConfigLines(defaultLines));
		}
		// header component
		List<TelegrafConfigLine> headerLines = new LinkedList<TelegrafConfigLine>();
		linesOfCurrentComponent.stream().skip(sizeOfDefaultComponent).forEach(headerLine -> {
			headerLines.add(headerLine);
		});
		configComponents.add(TelegrafConfigHeader.fromConfigLines(headerLines));

		return configComponents;
	}

	private static boolean firstHeaderReached(boolean pluginSectionReached, TelegrafConfigLine configLine) {
		return !pluginSectionReached && (configLine.getType() == TelegrafConfigLineType.BORDER);
	}
}
