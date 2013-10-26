package com.hoodox.operon.valueobjects;

import java.io.Serializable;
import java.util.Date;

public class TokenPlaceRefVo implements Serializable {

	private static final long serialVersionUID = 4211032572702159961L;
	private TokenVo tokenVo = new TokenVo();
	
	private Long caseId;
	private String placeRef;
	private String placeRefType;
	private Date updatedDate;
	private Date createdDate;
	

	public Long getCaseId() {
		return caseId;
	}


	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}


	public String getPlaceRef() {
		return placeRef;
	}


	public void setPlaceRef(String placeRef) {
		this.placeRef = placeRef;
	}


	public String getPlaceRefType() {
		return placeRefType;
	}


	public void setPlaceRefType(String placeRefType) {
		this.placeRefType = placeRefType;
	}


	public Long getTokenId() {
		return this.tokenVo.getTokenId();
	}


	public void setTokenId(Long tokenId) {
		this.tokenVo.setTokenId(tokenId);
	}


	public TokenPlaceRefVo() {
		super();
		// TODO Auto-generated constructor stub
	}


	public TokenVo getTokenVo() {
		return tokenVo;
	}


	public void setTokenVo(TokenVo tokenVo) {
		this.tokenVo = tokenVo;
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
