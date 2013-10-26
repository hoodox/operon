package com.hoodox.operon.wfnet;

public class SubCaseType extends CaseType {

	private static final long serialVersionUID = 2551402736287583842L;
	
	private RootCaseType rootCaseType = null;
	private CaseType parentCaseType = null;
	
	public SubCaseType() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CaseType getParentCaseType() {
		return parentCaseType;
	}

	public void setParentCaseType(CaseType parentCaseType) {
		this.parentCaseType = parentCaseType;
	}

	public RootCaseType getRootCaseType() {
		return rootCaseType;
	}

	public void setRootCaseType(RootCaseType rootCaseType) {
		this.rootCaseType = rootCaseType;
	}
	
}
