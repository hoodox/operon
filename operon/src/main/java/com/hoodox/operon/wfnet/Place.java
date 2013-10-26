// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 08/03/2006 12:53:42
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Place.java

package com.hoodox.operon.wfnet;


// Referenced classes of package com.hoodox.operon.wfnet:
//            PnmlNode

public class Place extends PnmlNode {


	private static final long serialVersionUID = 0x1258f539393d4983L;

	private Integer initialMarking;

	private String type;

	private String postCreateCaseAction;

	private String createSubcasesAction;

	private String[] resources;
	
	private Place refPlace;
	
	
	public Place getRefPlace() {
		return refPlace;
	}

	public void setRefPlace(Place refPlace) {
		this.refPlace = refPlace;
	}

	public String getPostCreateCaseAction() {
		return postCreateCaseAction;
	}

	public void setPostCreateCaseAction(String createCaseAction) {
		this.postCreateCaseAction = createCaseAction;
	}

	public String getCreateSubcasesAction() {
		return createSubcasesAction;
	}

	public void setCreateSubcasesAction(String createSubcasesAction) {
		this.createSubcasesAction = createSubcasesAction;
	}

	public Integer getInitialMarking() {
		return initialMarking;
	}

	public void setInitialMarking(Integer initialMarking) {
		this.initialMarking = initialMarking;
	}

	public String getType() {
		return type;
	}

	public void setType(String placeType) {
		type = placeType;
	}

	public Place() {
	}

	public String[] getResources() {
		return resources;
	}

	public void setResources(String[] resources) {
		this.resources = resources;
	}
	
}