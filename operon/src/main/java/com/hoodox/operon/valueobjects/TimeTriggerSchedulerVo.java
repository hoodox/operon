package com.hoodox.operon.valueobjects;

import java.util.Date;

public class TimeTriggerSchedulerVo {

	private Long taskId;
	private String schedulerRef;
	private String cronExp;
	private Date createdDate;
	private Date updatedDate;
	
	public String getCronExp() {
		return cronExp;
	}
	public void setCronExp(String cronExp) {
		this.cronExp = cronExp;
	}
	public String getSchedulerRef() {
		return schedulerRef;
	}
	public void setSchedulerRef(String schedulerRef) {
		this.schedulerRef = schedulerRef;
	}
	public Long getTaskId() {
		return taskId;
	}
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

}
