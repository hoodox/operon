/**
 * 
 */
package com.hoodox.operon.resources;

import com.hoodox.operon.resourceiface.Resource;

/**
 * @author Chung
 *
 */
public class TestResource implements Resource {
	
	private String id;
	private boolean group;
	
	public TestResource (String id, boolean isGroup) {
		this.id= id;
		this.group = isGroup;
	}
	

	/* (non-Javadoc)
	 * @see com.hoodox.operon.resourceiface.Resource#getId()
	 */
	public String getId() {
		// TODO Auto-generated method stub
		return this.id;
	}

	/* (non-Javadoc)
	 * @see com.hoodox.operon.resourceiface.Resource#isGroup()
	 */
	public boolean isGroup() {
		// TODO Auto-generated method stub
		return group;
	}

}
