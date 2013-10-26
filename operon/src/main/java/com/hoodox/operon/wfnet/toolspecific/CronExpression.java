package com.hoodox.operon.wfnet.toolspecific;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CronExpression {

	private String expression;
	private List<String> schedulerRefIdList = new ArrayList<String>();
	
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public String[] getSchedulerRefIds() {
		return this.schedulerRefIdList.toArray(new String[this.schedulerRefIdList.size()]);
	}
	public void setSchedulerRefIds(String[] schedulerRefIds) {
		this.schedulerRefIdList = Arrays.asList(schedulerRefIds);
	}
	
	public void addschedulerRefId(String schedulerRefId) {
		this.schedulerRefIdList.add(schedulerRefId);
	}
	
}
