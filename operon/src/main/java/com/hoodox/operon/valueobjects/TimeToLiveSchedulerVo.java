package com.hoodox.operon.valueobjects;

import java.io.Serializable;
import java.util.Date;

public class TimeToLiveSchedulerVo implements Serializable {

	private static final long serialVersionUID = -4261379728761747084L;
	
	private Long caseId;
	private String schedulerRef;
	private String cronExp;
	private Date updatedDate;
	private Date createdDate;
	

	public Long getCaseId() {
		return caseId;
	}

	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}

	public String getSchedulerRef() {
		return schedulerRef;
	}

	public void setSchedulerRef(String schedulerRef) {
		this.schedulerRef = schedulerRef;
	}


	public String getCronExp() {
		return cronExp;
	}

	public void setCronExp(String cronExp) {
		this.cronExp = cronExp;
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
