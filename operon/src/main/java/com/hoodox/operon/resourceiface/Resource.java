package com.hoodox.operon.resourceiface;

/**
 * <p>The resource interface is used in conjuntion with the ResourceManger</p>
 * 
 * <p>The ResourceManager interface allows the Operon framework to interface with the calling 
 * application access controls.</p>
 * 
 * @author huac
 * 
 * @see ResourceManger
 */
public interface Resource {
	
	/**
	 * The resource Id
	 * @return The resource id
	 */
	public String getId();
	
	/**
	 * Determines if this resource is a Group. i.e. has childrens.
	 * @return true if the resource is a group otherwise fasle.
	 */
	public boolean isGroup();

}
