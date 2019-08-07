package org.mapper.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertTreeNodeOperator extends AlertTreeNode {
	
	public AlertTreeNodeOperator(AlertConditionCombinator operator, List<AlertTreeNode> children) {
		this.deployOn = null;
		this.children = children;
		this.operator = operator;
		this.type = AlertTreeNodeType.OPERATOR;
	}
	
	public AlertTreeNodeOperator() {
		super();
		this.type = AlertTreeNodeType.OPERATOR;
	}

	AlertConditionCombinator operator;

	@Override
	protected AlertTreeNodeOperator getCopyWithoutChildren() {
		AlertTreeNodeOperator copy = new AlertTreeNodeOperator();
		copy.setDeployOn(deployOn);
		copy.setOperator(operator);
		copy.setTimewindow(timewindow);
		copy.setTreeName(treeName);
		return copy;
	}
}
