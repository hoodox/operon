// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 08/03/2006 12:53:42
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   TriggerContext.java

package com.hoodox.operon.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.hoodox.operon.resourceiface.Resource;

public class TriggerContext implements Serializable {

	private static final long serialVersionUID = 0xd83c8952032b8ca7L;
	
	@SuppressWarnings("unchecked")
	private Map triggerDataMap = new HashMap();
	@SuppressWarnings("unchecked")
	private CaseAttributes caseAttributes = new CaseAttributes();
	private Resource currentResource = null;

	
	public TriggerContext() {
		super();
	}
	
	/**
	 * Store all data to be passed to the Action classes in this map
	 * @return Map
	 */
	@SuppressWarnings("unchecked")
	public Map getTriggerDataMap() {
		
		return triggerDataMap;
	}
	
	/**
	 * <p>This contains case attributes its values.</p>
	 * 
	 * <p>The attributes and its values are evaluated against the Guard Expressions of the output arcs of 
	 * a XOR Transition to decide what Place a Token is produced.</p>
	 * @return CaseAttributes
	 */
	@SuppressWarnings("unchecked")
	public CaseAttributes getCaseAttributes() {
		return this.caseAttributes; 
	}
	
	/**
	 * Sets the current TriggerContext Resource so that the WorkflowEngine can determine if the Resource is allowed
	 * to trigger WorkItems.
	 * @param resource the current Resource
	 */
	public void setCurrentResource(Resource resource) {
		this.currentResource = resource;
	}
	
	/**
	 * Gets the current set Resource for this particular context.
	 * @return the current Resource
	 */
	public Resource getCurrentResource() {
		return this.currentResource;
	}
	
	
}