package org.esper.plugin;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.mapper.api.LocalAlertingInstance;
import org.mapper.model.AlertCondition;
import org.mapper.model.AlertTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class EsperInstance implements LocalAlertingInstance {

	private static final String API_HTTP_PREFIX = "http://";
	private static final String API_PORT_SUFFIX = ":10101";

	private static Logger LOGGER = LoggerFactory.getLogger(EsperInstance.class);
	
	private EsperService esperService;
	private String hostAddress;

	public EsperInstance(EsperService esperService, String hostAddress) {
		super();
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
		this.esperService = esperService;
		this.hostAddress = hostAddress;
	}

	public static EsperInstance getWithHostAddress(String hostAddress) {
		
		HttpUrl apiUrl = HttpUrl.get(API_HTTP_PREFIX+hostAddress+API_PORT_SUFFIX);
		Retrofit retrofit = new Retrofit.Builder().baseUrl(apiUrl).addConverterFactory(ScalarsConverterFactory.create()).build();
		EsperService esperService = retrofit.create(EsperService.class);

		return new EsperInstance(esperService, hostAddress);
	}

	@Override
	public String getAlerts() {
		
		StringBuilder stringBuilder = new StringBuilder();
		
		Call<String> alertCall = esperService.getAlerts();
		
		try {
			stringBuilder.append(alertCall.execute().body());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return stringBuilder.toString();
	}

	@Deprecated
	public boolean addAlertCondition(AlertCondition alertCondition) {
		LOGGER.info("addAlertCondition("+alertCondition.getName()+")");
		
		boolean success = addStatement(alertCondition.getName(), EsperAlertUtility.alertConditionToEsperStatement(alertCondition));

		LOGGER.info("addAlertCondition("+alertCondition.getName()+") -  failure");
		return success;
	}

	@Deprecated
	public boolean removeAlertCondition(AlertCondition alertCondition) {
		LOGGER.info("removeAlertCondition("+alertCondition.getName()+")");
		
		boolean success = removeStatement(alertCondition.getName());

		LOGGER.info("removeAlertCondition("+alertCondition.getName()+") - failure");
		return success;
	}

	private boolean addStatement(String name, String statement) {
		LOGGER.info("addStatement("+name+")");
		
		Call<String> addAlertCall = esperService.addAlert(name, statement);
		
		try {
			Response<String> addAlertResponse = addAlertCall.execute();
			if (addAlertResponse.isSuccessful()) {
				LOGGER.info("addStatement("+name+") -  success");
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("addStatement("+name+") -  failure");
		return false;
	}

	private boolean removeStatement(String name) {
		LOGGER.info("removeStatement("+name+")");
		
		Call<String> removeAlertCall = esperService.removeAlert(name);
		
		try {
			Response<String> removeAlertResponse = removeAlertCall.execute();
			if (removeAlertResponse.isSuccessful()) {
				LOGGER.info("removeStatement("+name+") - success");
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("removeStatement("+name+") - failure");
		return false;
	}
	
	@Override
	public String getHostAddress() {
		return hostAddress;
	}

	@Override
	public boolean addAlertTree(AlertTreeNode alertTree) {
		LOGGER.info("addAlertTree("+alertTree.getTreeName()+")");
		
		boolean success = addStatement(alertTree.getTreeName(), EsperAlertUtility.alertTreeToEsperStatement(alertTree));
		
		LOGGER.info("addAlertTree("+alertTree.getTreeName()+") - finished");
		return success;
	}

	@Override
	public boolean removeAlertTree(AlertTreeNode alertTree) {
		LOGGER.info("removeAlertTree("+alertTree.getTreeName()+")");
		
		boolean success = removeStatement(alertTree.getTreeName());
		
		LOGGER.info("removeAlertTree("+alertTree.getTreeName()+")");
		return success;
	}
}
