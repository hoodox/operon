package com.hoodox.operon.valueobjects;

import java.util.Date;

public class EventAuditVo {

	private Long eventAuditId;
	private Long caseId;
	private Long taskId;
	private String event;
	private String initialStatus;
	private String finalStatus;
	private boolean successInd;
	private String resourceId;
	private String errorCode;
	private String errorDetail;
	private Date CreatedDate;
	public Long getCaseId() {
		return caseId;
	}
	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}
	public Date getCreatedDate() {
		return CreatedDate;
	}
	public void setCreatedDate(Date createdDate) {
		CreatedDate = createdDate;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorDetail() {
		return errorDetail;
	}
	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public Long getEventAuditId() {
		return eventAuditId;
	}
	public void setEventAuditId(Long eventAuditId) {
		this.eventAuditId = eventAuditId;
	}
	public String getFinalStatus() {
		return finalStatus;
	}
	public void setFinalStatus(String finalStatus) {
		this.finalStatus = finalStatus;
	}
	public String getInitialStatus() {
		return initialStatus;
	}
	public void setInitialStatus(String initialStatus) {
		this.initialStatus = initialStatus;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public boolean isSuccessInd() {
		return successInd;
	}
	public void setSuccessInd(boolean successInd) {
		this.successInd = successInd;
	}
	public Long getTaskId() {
		return taskId;
	}
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	
	
	
}
