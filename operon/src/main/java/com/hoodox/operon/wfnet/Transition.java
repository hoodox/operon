// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 08/03/2006 12:53:41
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Transition.java

package com.hoodox.operon.wfnet;

import com.hoodox.operon.wfnet.toolspecific.TimeDuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Referenced classes of package com.hoodox.operon.wfnet:
//            PnmlNode

public class Transition extends PnmlNode {

	public Transition() {
	}

	public TimeDuration getTriggerDelayDuration() {
		return triggerDelayDuration;
	}

	public void setTriggerDelayDuration(TimeDuration triggerDelayDuration) {
		this.triggerDelayDuration = triggerDelayDuration;
	}

	public TimeDuration getExecutionTimeLimit() {
		return executionTimeLimit;
	}

	public void setExecutionTimeLimit(TimeDuration executionTimeLimit) {
		this.executionTimeLimit = executionTimeLimit;
	}
	
	public TimeDuration getEstimatedCompletionTime() {
		return estimatedCompletionTime;
	}

	public void setEstimatedCompletionTime(TimeDuration estimatedCompletionTime) {
		this.estimatedCompletionTime = estimatedCompletionTime;
	}

	public String getTransitionType() {
		return transitionType;
	}

	public void setTransitionType(String transitionType) {
		this.transitionType = transitionType;
	}

	public String getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}

	public Integer getPriorityWeighting() {
		return priorityWeighting;
	}

	public void setPriorityWeighting(Integer priorityWeighting) {
		this.priorityWeighting = priorityWeighting;
	}
	
	public void addAction(String action) {
		actionList.add(action);
	}

	public String[] getAllActions() {
		return actionList.toArray(new String[actionList.size()]);
	}
	
	public void setActions (String[] actions) {
		this.actionList = Arrays.asList(actions);
	}
	
	public void addResourceId(String resourceId) {
		resourceIdList.add(resourceId);
	}

	public void setResourceIds (String[] resourceIds) {
		this.resourceIdList = Arrays.asList(resourceIds);
	}
	
	public String[] getResources() {
		return this.resourceIdList.toArray(new String[this.resourceIdList.size()]);
	}

	private static final long serialVersionUID = 0x147e96e936706129L;

	private String transitionType;

	private String triggerType;

	private TimeDuration executionTimeLimit;

	private TimeDuration triggerDelayDuration;
	
	private TimeDuration estimatedCompletionTime;
	
	private Integer priorityWeighting = new Integer(1);

	private List<String> actionList = new ArrayList<String>();

	private List<String> resourceIdList = new ArrayList<String>();
}