package org.telegraf.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TelegrafConfigWriter {

	public static void writeConfigToFile(TelegrafConfig config, File configFile){

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
				writer.write(config.toString());;
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
