package org.mapper.core;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mapper.model.Alert;
import org.mapper.model.AlertCondition;
import org.mapper.model.AlertTreeNode;
import org.mapper.model.AlertTreeNodeType;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class MapperAlertPoliciesUtility {
	
	private static final String TRUE = "true";
	private static final String PLACE_ON = "placeOn";
	private static final String AGGREGATION_INTERVAL = "aggregationInterval";
	private static final String AGGREGATION_METHOD = "aggregationMethod";
	private File alertPoliciesFile;
	private ObjectMapper objectMapper;
	

	public MapperAlertPoliciesUtility(File alertPoliciesFile) {
		super();
		this.alertPoliciesFile = alertPoliciesFile;
		this.objectMapper = new ObjectMapper();
		this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	public Map<String, AlertPolicy> generateAlertPolicyMap(List<AlertPolicy> alertPolicies) {
		
		Map<String, AlertPolicy> alertPoliciesMap = new LinkedHashMap<String, AlertPolicy>();
		
		for (AlertPolicy alertPolicy : alertPolicies) {
			alertPoliciesMap.put(alertPolicy.getId(), alertPolicy);
		}
		
		return alertPoliciesMap;
	}
	

	public void applyPolicyToAlert(AlertPolicy policy, Alert alert) {
		applyPolicyToAlertTreeNode(policy.getAlertTree(), alert.getAlertTree());
	}

	//TODO check + remove prints
	private void applyPolicyToAlertTreeNode(AlertTreeNodePolicy nodePolicy, AlertTreeNode node) {

		//System.out.println(node.getTreeName());
		Map<String, String> policies = nodePolicy.getPolicies();
		if(node.getType() == AlertTreeNodeType.CONDITION) {
			AlertCondition condition = (AlertCondition) node;
			condition.setDeployOn(policies.get(PLACE_ON));
			condition.setAggregationInterval(policies.get(AGGREGATION_INTERVAL));
			condition.setAggregationMethod(policies.get(AGGREGATION_METHOD));
			System.out.println("interval: "+condition.getAggregationInterval());
			System.out.println("method: "+condition.getAggregationMethod());
			return;
		} else if (node.getType() == AlertTreeNodeType.OPERATOR) {
			node.setDeployOn(nodePolicy.getPolicies().get(PLACE_ON));
		}
		List<AlertTreeNodePolicy> childrenPolicies = nodePolicy.getChildren();
		List<AlertTreeNode> children = node.getChildren();
		
		if (children!=null) {
			for (int i = 0; i < children.size(); i++) {
				//if polices are already ordered correctly the if clause should be faster
				if (children.get(i).getTreeName().equals(childrenPolicies.get(i).getId())) {
					applyPolicyToAlertTreeNode(childrenPolicies.get(i), children.get(i));
				} else {
					for (AlertTreeNodePolicy childrenPolicy : childrenPolicies) {
						if (children.get(i).getTreeName().equals(childrenPolicy.getId())) {
							applyPolicyToAlertTreeNode(childrenPolicy, children.get(i));
						}
					}
				}
			}
		}
	}

	public List<AlertPolicy> readAlertPoliciesFromFile(){
		List<AlertPolicy> alertPolicies = null;
		try {
			alertPolicies = objectMapper.readValue(alertPoliciesFile, new TypeReference<List<AlertPolicy>>(){});
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return alertPolicies;
	}
	
	public Map<String, AlertPolicy> writeDefaultPoliciesFileForAlerts(List<Alert> alerts) {
		
		List<AlertPolicy> defaultAlertPolicies = generateDefaultAlertPolicies(alerts);
		
		writeAlertPoliciesToFile(defaultAlertPolicies);
		
		return generateAlertPolicyMap(defaultAlertPolicies);
	}

	public void writeAlertPoliciesToFile(List<AlertPolicy> alertPolicies) {
		
		try {
			objectMapper.writeValue(alertPoliciesFile, alertPolicies);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<AlertPolicy> generateDefaultAlertPolicies(List<Alert> alerts) {
		
		List<AlertPolicy> defaultAlertPolicies = new LinkedList<AlertPolicy>();
		
		for (Alert alert : alerts) {
			defaultAlertPolicies.add(generateDefaultAlertPolicy(alert));
		}
		
		return defaultAlertPolicies;
	}

	private static AlertPolicy generateDefaultAlertPolicy(Alert alert) {
		
		AlertPolicy defaultAlertPoliciy = new AlertPolicy();
		defaultAlertPoliciy.setId(alert.getAlertId());
		defaultAlertPoliciy.setVersion(1);
		defaultAlertPoliciy.setPolicies(new LinkedHashMap<String, String>());
		defaultAlertPoliciy.setAlertTree(generateAlertTreeNodePolicies(alert.getAlertTree()));
		
		return defaultAlertPoliciy;
	}

	private static AlertTreeNodePolicy generateAlertTreeNodePolicies(AlertTreeNode alertTreeNode) {
		AlertTreeNodePolicy defaultAlertTreeNodePolicy = new AlertTreeNodePolicy();
		defaultAlertTreeNodePolicy.setId(alertTreeNode.getTreeName());
		Map<String, String> policies = new LinkedHashMap<String, String>();
		if (alertTreeNode.getType()==AlertTreeNodeType.CONDITION) {
			AlertCondition condition = (AlertCondition) alertTreeNode;
			if (condition.getMeasurementSources().size() == 1) {
				policies.put(PLACE_ON, condition.getMeasurementSources().get(0));
			} else {
				policies.put(PLACE_ON, "");
			}
			policies.put(AGGREGATION_INTERVAL, "30s");
			policies.put(AGGREGATION_METHOD, "mean");
			
		} else {
			policies.put(PLACE_ON, "");
		}
		
		defaultAlertTreeNodePolicy.setPolicies(policies);
		
		if (alertTreeNode.getChildren() != null) {
			List<AlertTreeNodePolicy> childrenDefaultTreeNodePolicies = new LinkedList<AlertTreeNodePolicy>();
			for (AlertTreeNode child : alertTreeNode.getChildren()) {
				childrenDefaultTreeNodePolicies.add(generateAlertTreeNodePolicies(child));
			}
			defaultAlertTreeNodePolicy.setChildren(childrenDefaultTreeNodePolicies);
		} 
		return defaultAlertTreeNodePolicy;
	}


}
