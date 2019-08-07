package org.mapper.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AlertTreeNode {
	
	String treeName;
	String deployOn;
	AlertConditionTimeWindow timewindow;
	AlertTreeNodeType type;
	List<AlertTreeNode> children;
	protected abstract AlertTreeNode getCopyWithoutChildren();
}
