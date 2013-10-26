// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 08/03/2006 12:53:22
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   CaseType.java

package com.hoodox.operon.wfnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

// Referenced classes of package com.hoodox.operon.wfnet:
//            PnmlNode, Place, Transition

public abstract class  CaseType extends PnmlNode {
	private static final long serialVersionUID = 0x34545e95894b6e7dL;


	private List<Place> placeList = new ArrayList<Place>();

	private List<Transition> transitionList = new ArrayList<Transition>();
	private List<Arc> arcList = new ArrayList<Arc>();
	private Integer treeLevel = new Integer(0);
	private List<CaseType> subcaseTypeList = new ArrayList<CaseType>();
	
	
	private String description;
	

	public CaseType() {
	}

	public void addsubCaseType(CaseType caseType) {
		this.subcaseTypeList.add(caseType);
		
	}
	
	public SubCaseType[] getAllSubCaseTypes() {
		return this.subcaseTypeList.toArray(new SubCaseType[this.subcaseTypeList.size()]);
	}
	
	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}



	public void addPlace(Place place) {
		placeList.add(place);
	}	
	
	public void setPlaces(Place[] places) {
		this.placeList = Arrays.asList(places);
	}
	
	public Place[] getPlaces() {
		return this.placeList.toArray(new Place[this.placeList.size()]);
	}

	public void addTransition(Transition transition) {
		this.transitionList.add(transition);
	}
	
	public Transition[] getTranstions() {
		return this.transitionList.toArray(new Transition[this.transitionList.size()]);
	}
	
	public void setTransitions(Transition[] transitions) {
		this.transitionList = Arrays.asList(transitions);
	}

	public void addArc(Arc arc) {
		this.arcList.add(arc);
		
	}
	
	public void setArcs(Arc[] arcs) {
		this.arcList = Arrays.asList(arcs);
		
	}
	
	public Arc[] getArcs() {
		return this.arcList.toArray(new Arc[this.arcList.size()]);
	}
	
	

	public Integer getTreeLevel() {
		return treeLevel;
	}

	public void setTreeLevel(Integer treeLevel) {
		this.treeLevel = treeLevel;
	}

	//========================================
	// Some helper methods
	//========================================
	/**
	 * Finds a transition by id but is non recursive
	 */
	public Transition findTransitionById (String id) {
		boolean recursive = true;
		return findTransitionById(id, !recursive);
	}
	/**
	 * Finds a transition by id
	 * @param id the transition id
	 * @param recursive if recusively searches the subnets.
	 */
	public Transition findTransitionById (String id, boolean recursive) {
		Iterator<Transition> iter = this.transitionList.iterator();
		while (iter.hasNext()) {
			Transition transition = iter.next();
			if (id.equals(transition.getId())) {
				return transition;
			}
		}
		
		if (!recursive) {
			// not recursive.
			return null;
		}
		
		// if get here search recursively
		Iterator<CaseType> caseTypeIter = this.subcaseTypeList.iterator();
		while ( caseTypeIter.hasNext() ) {
			CaseType caseType = caseTypeIter.next();
			Transition transition = caseType.findTransitionById(id, true); 
			
			if (transition != null) {
				return transition;
			}
		}
		
		// if get here nothing found
		return null;
		
	}
	
	/**
	 * Finds a Place by id
	 * @param id
	 * @return
	 */
	public Place findPlaceId(String id) {
		Iterator<Place> iter = this.placeList.iterator();
		while ( iter.hasNext() ) {
			Place place = iter.next();
			if (id.equals(place.getId())) {
				return place;
			}
		}
		
		return null;
	}
	
	/**
	 * Finds a place that has a referencePlace that matches
	 * the passed in ref.
	 * @param ref
	 * @return Place or null
	 */
	public Place findPlaceWithRef(String ref) {
		Iterator<Place> iter = this.placeList.iterator();
		while ( iter.hasNext() ) {
			Place place = iter.next();
			if (null != place.getRefPlace()) {
				if (ref.equals(place.getRefPlace().getId())) {
					return place;
				}
				
			}
		}
		
		return null;
	}
	
	
	/**
	 * Recursively finds a subcase by its Id
	 * @param caseTypeId
	 * @return
	 */
	public CaseType findSubCaseTypeById(String caseTypeId) {
		Iterator<CaseType> iter = this.subcaseTypeList.iterator();
		while ( iter.hasNext() ) {
			CaseType caseType = iter.next();
			if (caseTypeId.equals(caseType.getId())) {
				return caseType;
			}
			
			// recursively go down another level
			CaseType subCaseType = caseType.findSubCaseTypeById(caseTypeId);
			if (null != subCaseType) {
				return subCaseType;
			}
		}
		
		// nothing found
		return null;
		
	}
	
	/**
	 * If this is a subcase
	 * @return
	 */
	public boolean isSubcase() {
		if (this instanceof RootCaseType) {
			return false;
		}
		
		return true;
	}
}