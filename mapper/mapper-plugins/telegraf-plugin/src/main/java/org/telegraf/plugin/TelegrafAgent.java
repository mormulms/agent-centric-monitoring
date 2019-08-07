package org.telegraf.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.mapper.api.AgentInstance;
import org.mapper.model.AlertCondition;
import org.mapper.model.AlertTreeNode;
import org.mapper.model.AlertUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegraf.config.TelegrafConfig;
import org.telegraf.config.TelegrafConfigParser;
import org.telegraf.config.TelegrafConfigWriter;

import lombok.Getter;

public class TelegrafAgent implements AgentInstance{
	
	private static final String PASSWORD_KEY = "password";
	private static final String KEY_PATH_KEY = "keyPath";
	private static final String USER_KEY = "user";
	private static Logger LOGGER = LoggerFactory.getLogger(TelegrafAgent.class);
	private static String OS = System.getProperty("os.name").toLowerCase();

	private static final String RESTART_SCRIPT_PATH_UBUNTU = "/etc/telegraf/restartWithNewConfig.sh";
	private static final String REMOTE_CONFIG_PATH_UBUNTU = "/etc/telegraf/telegraf.conf";
	private final String LOCAL_CONFIG_REPO = "agent-configs/";
	private final String hostaddress;
	private final String pathRemoteConfig;
	private final String pathLocalCopyOfRemoteConfig;
	private final File localCopyOfRemoteConfig;
	private final String keyFilePath;
	private final String user;
	private final String password;
	private final boolean useKeyFileAuthentictaionInsteadOfPassword;
	
	private final ProcessBuilder restartAgent;
	private final ProcessBuilder loadRemoteConfig;
	private final ProcessBuilder saveRemoteConfig;
	
	@Getter
	private TelegrafConfig config;
	

	public TelegrafAgent(Map<String, String> configuration) {
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
		
		File jarFile = new File(TelegrafAgent.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		
		this.hostaddress = configuration.get("hostaddress");
		
		if(configuration.get(USER_KEY) != null) {
			this.user = configuration.get(USER_KEY);
		}else {
			this.user = "ubuntu";
		}
		
		if(configuration.get(PASSWORD_KEY) != null) {
			this.password = configuration.get(PASSWORD_KEY);
			this.useKeyFileAuthentictaionInsteadOfPassword = false;
		} else {
			this.password = "";
			this.useKeyFileAuthentictaionInsteadOfPassword = true;
		}
		
		if(configuration.get(KEY_PATH_KEY) != null) {
			this.keyFilePath = configuration.get(KEY_PATH_KEY);
		} else {
			this.keyFilePath = jarFile.getParentFile().getParent()+"/"+"ic4f.pem";
		}
		
			
		this.pathLocalCopyOfRemoteConfig = jarFile.getParentFile().getParent() + "/" + LOCAL_CONFIG_REPO+hostAddressWithoutDots(hostaddress)+"telegraf.conf";
		this.localCopyOfRemoteConfig = new File(pathLocalCopyOfRemoteConfig);
		this.pathRemoteConfig = this.user+"@"+hostaddress+":"+REMOTE_CONFIG_PATH_UBUNTU;		
		this.restartAgent = new ProcessBuilder();
		this.loadRemoteConfig  = new ProcessBuilder();
		this.saveRemoteConfig  = new ProcessBuilder();
		
		//restart command for windows
		if (OS.contains("win")) {
			//TODO add keyfile authentication for windows
			if (!useKeyFileAuthentictaionInsteadOfPassword) {
				this.restartAgent.command("plink",user+"@"+hostaddress,"-pw",password,"sudo "+RESTART_SCRIPT_PATH_UBUNTU);
				this.loadRemoteConfig.command("pscp","-pw",password,pathRemoteConfig,pathLocalCopyOfRemoteConfig);
				this.saveRemoteConfig.command("pscp","-pw",password,pathLocalCopyOfRemoteConfig,pathRemoteConfig);
			}
		} else {
			//TODO add password authentication for ubuntu
			if (useKeyFileAuthentictaionInsteadOfPassword) {
				this.restartAgent.command("ssh","-i",keyFilePath,user+"@"+hostaddress,"sudo "+RESTART_SCRIPT_PATH_UBUNTU);
				this.loadRemoteConfig.command("scp","-i",keyFilePath,pathRemoteConfig,pathLocalCopyOfRemoteConfig);
				this.saveRemoteConfig.command("scp","-i",keyFilePath,pathLocalCopyOfRemoteConfig,pathRemoteConfig);
			}
		}
		
		LOGGER.info("Created TelegrafAgent for: "+hostaddress);
		LOGGER.info("user: "+user);
		LOGGER.info("method of authentictation:"+ (useKeyFileAuthentictaionInsteadOfPassword?"keyfile":"password"));
		if (useKeyFileAuthentictaionInsteadOfPassword) {
			LOGGER.info("keyfile expected at:" +keyFilePath);
		}
		LOGGER.info("config expected at: "+pathRemoteConfig);
		LOGGER.info("config will be saved at: "+pathLocalCopyOfRemoteConfig);
	
	}
	
	static String hostAddressWithoutDots(String hostaddress) {
		String[] addressParts = hostaddress.split("\\.");
		StringBuilder builder = new StringBuilder();
		//every part should have the length 3
		for (String part : addressParts) {
				while (part.length()<3) {
					part = "0"+part;
				}
				builder.append(part);
		}
		
		return builder.toString();
	}

	public void loadConfig() {
		
		LOGGER.info("loadConfig()");
		try {
			Process process = loadRemoteConfig.start();
			
			if (process.waitFor() == 0) {
				LOGGER.info("loadConfig() - success");
				config = TelegrafConfigParser.readConfigFromFile(localCopyOfRemoteConfig);
				LOGGER.info("localCopy is saved at: "+pathLocalCopyOfRemoteConfig);
			} else {
				LOGGER.info("loadConfig() -  failure");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	public void saveConfig() {
		
		LOGGER.info("saveConfig()");
		try {
			TelegrafConfigWriter.writeConfigToFile(config, localCopyOfRemoteConfig);
			
			Process process = saveRemoteConfig.start();
			
			if (process.waitFor() == 0) {
				LOGGER.info("saveConfig() - success");
			} else {
				LOGGER.info("saveConfig() -  failure");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	@Deprecated
	public void addLocalAlertingForAlertCondition(AlertCondition condition) {
		LOGGER.info("addLocalAlertingForAlertCondition()");
		//TODO change
		String localCepUrl = "http://127.0.0.1:10101/event/influxdb";
		config.rerouteMesurementToCep(condition, localCepUrl);
		saveConfig();
		restartAgent();
		
	}

	public void restartAgent() {
		LOGGER.info("restartAgent()");
		
		try {
			Process process = restartAgent.start();
			
			if (process.waitFor() == 0) {
				LOGGER.info("restartAgent() - success");
			} else {
				LOGGER.info("restartAgent() -  failure");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public void removeLocalAlertingForAlertCondition(AlertCondition condition) {
		LOGGER.info("removeLocalAlertingForAlertCondition()");
		config.removeReroute(condition.getMeasurement(), condition.getName());
		saveConfig();
		restartAgent();
		
	}

	@Override
	public String getHostAddress() {
		return hostaddress;
	}

	@Override
	public String getConfigString() {
		return config.toString();
	}
	
	public static TelegrafAgent createAndLoad(Map<String, String> configuration) {
		TelegrafAgent agent = new TelegrafAgent(configuration);
		agent.loadConfig();
		return agent;
	}


	@Override
	public void addRerouteMetricToLocalAlertingSystem(AlertCondition condition, String localAlertingSystemAddress) {
		
		LOGGER.info("addRerouteMetricToLocalAlertingSystem()");
		String localCepUrl = "http://"+localAlertingSystemAddress+":10101/event/influxdb";
		config.rerouteMesurementToCep(condition, localCepUrl);
		saveConfig();
		restartAgent();
		
	}

	@Override
	public void removeRerouteMetricToLocalAlertingSystem(AlertCondition condition) {
		
		LOGGER.info("removeRerouteMetricToLocalAlertingSystem()");
		config.removeReroute(condition.getMeasurement(), condition.getName());
		saveConfig();
		restartAgent();
	}
}
