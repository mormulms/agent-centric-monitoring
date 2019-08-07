package org.mapper.local_alerting_component;

import java.net.URI;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.DeploymentOptions;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPDeploymentService;
import com.espertech.esper.runtime.client.EPEventService;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeDestroyedException;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.EPUndeployException;
import com.sun.net.httpserver.HttpServer;

/**
 * Hello world!
 *
 */
public class LocalAlerting {
	
	private final static int REST_API_PORT = 10101;
	private static Logger LOGGER = LoggerFactory.getLogger(LocalAlerting.class);
	
	private static EPRuntime runtime;
	private static EPEventService eventService;
	private static EPDeploymentService deploymentService;
	private static Configuration configuration;
	private static CompilerArguments compilerArguments;
	private static EPCompiler compiler;
	
	public static void main(String[] args) {
		Configurator.setLevel(LOGGER.getName(), Level.INFO);
		
		LOGGER.info("starting");
		
		esperSetUp();

		startRestAPI();
		
//		exampleConfiguration();
	}

	private static void exampleConfiguration() {
		LOGGER.info("setting up 1 ");
		
		addInfluxDBStatement("my-statement", "select * from InfluxDBEvent");
	}
	
	private static void startRestAPI() {
		LOGGER.info("starting REST API");
		
		ResourceConfig rc = new ResourceConfig().packages("org.mapper.local_alerting_component");
		HttpServer server = JdkHttpServerFactory.createHttpServer(URI.create("http://localhost:"+REST_API_PORT+"/"), rc);

		LOGGER.info("REST API is now available on port: "+REST_API_PORT);
	}

	private static EPDeployment deployCompiledStatement(EPCompiled compiledStatement, DeploymentOptions deploymentOptions) {
		
		EPDeployment deployment = null; 
		
		try {
			deployment = deploymentService.deploy(compiledStatement, deploymentOptions);
		} catch (EPRuntimeDestroyedException e) {
			e.printStackTrace();
		} catch (EPDeployException e) {
			e.printStackTrace();
		}
		return deployment;
	}

	private static EPCompiled complieStatement(String statement) {

		EPCompiled compiledStatement = null;

		try {
			compiledStatement = compiler.compile(statement, compilerArguments);
		} catch (EPCompileException e) {
			e.printStackTrace();
		}
		return compiledStatement;
	}

	private static void esperSetUp() {

		LOGGER.info("Esper set up - Start");

		configuration = new Configuration();
		configuration.getCommon().addEventType(InfluxDBEvent.class);
		configuration.getCompiler().getByteCode().setAllowSubscriber(true);

		compiler = EPCompilerProvider.getCompiler();
		compilerArguments = new CompilerArguments(configuration);

		runtime = EPRuntimeProvider.getDefaultRuntime(configuration);

		deploymentService = runtime.getDeploymentService();
		eventService = runtime.getEventService();

		LOGGER.info("Esper set up - End");

	}

	public static void consumeInfluxDBEvent(InfluxDBEvent event) {
		LOGGER.info("received: "+event.getInfluxDBLine());
		eventService.sendEventBean(event, event.getClass().getSimpleName());
	}
	
	private static String generateNameAnnotation(String name) {
		return "@name('"+name+"')";
	}
	
	public static void addInfluxDBStatement(String statementName, String statement) {
		LOGGER.info("addInfluxDBStatement("+statementName+")");
		
		String combinedStatement = generateNameAnnotation(statementName)+" "+statement;
		
		EPCompiled compiledStatement = complieStatement(combinedStatement); 
		
		DeploymentOptions deploymentOptions = new DeploymentOptions();
		deploymentOptions.setDeploymentId(statementName);
		
		EPDeployment deployment = deployCompiledStatement(compiledStatement, deploymentOptions);
		
		//only one statement per deployment
		addLocalTelegrafSubscriberToStatement(statementName, deployment.getStatements()[0]);
		LOGGER.info("addInfluxDBStatement("+statementName+") - success"+statement);
		LOGGER.info(statement);
	}
	
	private static void addLocalTelegrafSubscriberToStatement(String statementName, EPStatement statement) {
		statement.setSubscriber(LocalTelegrafSubscriber.instanceWithConditionName(statementName));
	}
	
	//One statement per deployment
	public static void removeInfluxDBStatementByName(String statementName) {
		LOGGER.info("removeInfluxDBStatementByName("+statementName+")");
		try {
			deploymentService.undeploy(statementName);
			LOGGER.info("removeInfluxDBStatementByName("+statementName+") - success");
		} catch (EPRuntimeDestroyedException e) {
			e.printStackTrace();
		} catch (EPUndeployException e) {
			e.printStackTrace();
		}
	}
	
	public static String[] getAllStatementNames() {
		return deploymentService.getDeployments();
	}
}
