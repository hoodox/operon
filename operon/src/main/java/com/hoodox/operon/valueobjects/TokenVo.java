package com.hoodox.operon.valueobjects;

import java.io.Serializable;
import java.util.Date;

/**
 * Token Value object
 * @author HUAC
 *
 */
public class TokenVo implements Serializable {

	private static final long serialVersionUID = 1110629713503562935L;
	
	private Long tokenId;
	private String tokenStatus;
	private Long lockByTaskId;
	private Long lockVersion;
	private Date createdDate;
	private Date updatedDate;
	
	public TokenVo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getLockByTaskId() {
		return lockByTaskId;
	}

	public void setLockByTaskId(Long lockByTaskId) {
		this.lockByTaskId = lockByTaskId;
	}

	public Long getLockVersion() {
		return lockVersion;
	}

	public void setLockVersion(Long lockVersion) {
		this.lockVersion = lockVersion;
	}

	public Long getTokenId() {
		return tokenId;
	}

	public void setTokenId(Long tokenId) {
		this.tokenId = tokenId;
	}

	public String getTokenStatus() {
		return tokenStatus;
	}

	public void setTokenStatus(String tokenStatus) {
		this.tokenStatus = tokenStatus;
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
