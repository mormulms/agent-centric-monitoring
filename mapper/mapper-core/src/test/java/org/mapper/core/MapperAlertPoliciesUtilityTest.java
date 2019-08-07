package org.mapper.core;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

class MapperAlertPoliciesUtilityTest {

	static final String SIMPLE_ALERT_POLICY = "src/test/resources/alert-policies.json";
	
	@Test
	void writeAlertPoliciesToFile_OneAlertPolicy() {
		
		MapperAlertPoliciesUtility utility = new MapperAlertPoliciesUtility(new File(SIMPLE_ALERT_POLICY));
		
		AlertTreeNodePolicy alertTreeNodePolicy11 = new AlertTreeNodePolicy();
		alertTreeNodePolicy11.setId("test11");
		
		AlertTreeNodePolicy alertTreeNodePolicy12 = new AlertTreeNodePolicy();
		alertTreeNodePolicy12.setId("test12");
		
		List<AlertTreeNodePolicy> children = new LinkedList<AlertTreeNodePolicy>();
		children.add(alertTreeNodePolicy11);
		children.add(alertTreeNodePolicy12);
		
		AlertTreeNodePolicy alertTreeNodePolicy1 = new AlertTreeNodePolicy();
		alertTreeNodePolicy1.setId("test1");
		alertTreeNodePolicy1.setChildren(children);
		
		
		AlertPolicy alertPolicy = new AlertPolicy();
		alertPolicy.setId("test");
		alertPolicy.setAlertTree(alertTreeNodePolicy1);
		
		List<AlertPolicy> alertPolicies = new LinkedList<AlertPolicy>();
		alertPolicies.add(alertPolicy);
		
		utility.writeAlertPoliciesToFile(alertPolicies);
	}
}
