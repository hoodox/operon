package com.hoodox.operon.workflow;

import com.hoodox.operon.valueobjects.TaskVo;
import com.hoodox.operon.wfnet.CaseType;

public class Task {

	protected String applicationName;
	protected TaskVo taskVo = null;
	protected CaseType caseType;
	
	protected Task(String applicationName, TaskVo taskVo, CaseType caseType) {
		this.taskVo = taskVo;
		this.applicationName = applicationName;
		this.caseType = caseType;
	}

	public TaskVo getTaskVo() {
		return taskVo;
	}
	
	
	
	public CaseType getCaseType() {
		return this.caseType;
	}

	public String getApplicationName() {
		return this.applicationName;
	}
}
