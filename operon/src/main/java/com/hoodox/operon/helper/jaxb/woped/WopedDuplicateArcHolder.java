package com.hoodox.operon.helper.jaxb.woped;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hoodox.operon.jaxb.opnml.ArcType;

/**
 * Class that holds the group of Arc duplicates group by
 * source and target.
 * 
 * @author huac
 *
 */
public class WopedDuplicateArcHolder {
	private Map<String, List<ArcType>> removeMap = new HashMap<String, List<ArcType>>();
	
	public WopedDuplicateArcHolder (List<ArcType> arcList) {
		this._addArcList(arcList);
	}
	
	/**
	 * Takes a list of arc and constructs a list of remove
	 * and keep groups. Remove group contains duplicates and
	 * keep contains one from the duplicate that will not be removed.
	 * @param arcList
	 */
	private void _addArcList (List<ArcType> arcList) {
		for (ArcType anArc : arcList) {
			this._addRemoveArc(anArc);
		}
		
		
		// remove groups that have only 1 element.
		// i.e these are non duplicates.
		// For duplicates remove 1 of them for keep
		List<String> groupKeyList = new ArrayList<String>(this.removeMap.keySet());
		for (String groupKey : groupKeyList) {
			List<ArcType> aRemoveGroup = this.removeMap.get(groupKey);
			
			if (aRemoveGroup.size() <= 1) {
				// only 1 element
				// will remove
				this.removeMap.remove(groupKey);
				
			} else {
				// for a duplicate group, keep one of them
				// and the rest can be removed.
				aRemoveGroup.remove(0);
			}
		}
	}
			
	/**
	 * @param Arc
	 */
	private void _addRemoveArc(ArcType arc) {
    	
    	
    	String groupKey = "[" + arc.getSource()+ "," + arc.getTarget() + "]";
    	List<ArcType> aArcList = this.removeMap.get(groupKey);
    	if (null == aArcList) {
    		// create a new one
    		aArcList = new ArrayList<ArcType>();
    		this.removeMap.put(groupKey, aArcList);
    	}
    	
    	aArcList.add(arc);
    	
		
	}

	
	
	/**
	 * Returns a list of all Duplicate Arcs we want to remove by the keygroupId.
	 * 
	 * This will create a new list and return all the arc in the group we want to remove.
	 * 
	 * @param groupId
	 * @return
	 */
	public List<ArcType> getRemoveArcList() {
		Collection<List<ArcType>> arcCollectionLists = this.removeMap.values();		
		List<ArcType> totalRemoveList = new ArrayList<ArcType>();
		
		for (List<ArcType> aList : arcCollectionLists) {
			totalRemoveList.addAll(aList);
		}
		
		return totalRemoveList;
	}

}

