package com.hoodox.operon.valueobjects;

import java.io.Serializable;
import java.util.Date;

/**
 * A Case value object
 * @author HUAC
 *
 */
public class CaseVo implements Serializable {

	private static final long serialVersionUID = 6136266089899322052L;

	private Long caseId;
	private Long parentCaseId;
	private Long rootParentCaseId;
	private String caseTypeRef;
	private String rootCaseTypeRef;
	private String caseStatus;
	
	private Date expiryDate;
	
	private Long lockVersion;
	
	private Date createdDate;
	private Date updatedDate;
	
	public CaseVo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getCaseId() {
		return caseId;
	}

	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}

	public String getCaseStatus() {
		return caseStatus;
	}

	public void setCaseStatus(String caseStatus) {
		this.caseStatus = caseStatus;
	}

	public String getCaseTypeRef() {
		return caseTypeRef;
	}

	public void setCaseTypeRef(String caseTypeRef) {
		this.caseTypeRef = caseTypeRef;
	}
	
	

	public String getRootCaseTypeRef() {
		return rootCaseTypeRef;
	}

	public void setRootCaseTypeRef(String rootCaseTypeRef) {
		this.rootCaseTypeRef = rootCaseTypeRef;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Long getLockVersion() {
		return lockVersion;
	}

	public void setLockVersion(Long lockVersion) {
		this.lockVersion = lockVersion;
	}

	public Long getParentCaseId() {
		return parentCaseId;
	}

	public void setParentCaseId(Long parentCaseId) {
		this.parentCaseId = parentCaseId;
	}

	public Long getRootParentCaseId() {
		return rootParentCaseId;
	}

	public void setRootParentCaseId(Long rootParentCaseId) {
		this.rootParentCaseId = rootParentCaseId;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

}
