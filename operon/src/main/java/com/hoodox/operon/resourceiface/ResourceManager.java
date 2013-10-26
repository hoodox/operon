package com.hoodox.operon.resourceiface;

import com.hoodox.operon.exceptions.ResourceNotExistException;
import com.hoodox.operon.exceptions.TaskNotExistException;
import com.hoodox.operon.workflow.TriggerContext;

/**
 * <p>ResourceManager interface allows the Operon framework to interface with the calling 
 * application access controls.</p>
 * 
 * <p>All applications using the Operon framework must implement this interface.</p>
 * 
 * @author huac
 *
 */
public interface ResourceManager {

	/**
	 * Returns all the children resources of the passed resourceId
	 * 
	 * @param resourceId
	 * @return Array of Children Resources otherwise empty Array.
	 */
	public Resource[] getChildrenResourcesById(String resourceId);
	
	/**
	 * Gets a Resource by its Id
	 * 
	 * @param resourceId
	 * @return the Resource otherwise null.
	 */
	public Resource getResourceById(String resourceId);
	
	
	/**
	 * Assigns a Resource to a WorkItem by the workItemId
	 * 
	 * @param resourceId the resourceId
	 * @param workItemId the workItem id to assign to.
	 * 
	 * @throws ResourceNotExistException if Resource does not exist
	 * @throws TaskNotExistException if WorkItem does not exist
	 */
	public void assignResourceToWorkItem(String resourceId, Long workItemId) throws ResourceNotExistException, TaskNotExistException;
	
	/**
	 * Removes a Resource from a WorkItem
	 * 
	 * @param resourceId the Resource Id
	 * @param workItemId the WorkItem Id
	 * @throws ResourceNotExistException if Resource does not exist
	 * @throws TaskNotExistException if WorkItem does not exist
	 */
	public void removeResourceFromWorkItem(String resourceId, Long workItemId) throws ResourceNotExistException, TaskNotExistException;
	
	/**
	 * Determines if a Resource has access to a WorkItem
	 * @param resourceId the Resource Id
	 * @param workItemId the WorkItem Id
	 * @return true if access is allowed otherwise false
	 * @throws ResourceNotExistException if Resource does not exist
	 * @throws TaskNotExistException if WorkItem does not exist
	 */
	public boolean resourceHasAccessToWorkItem(String resourceId, Long workItemId) throws ResourceNotExistException, TaskNotExistException;
	
	/**
	 * Returns a default Resource for triggering auto and time triggered Workitems.
	 * 
	 * @return the default Resource to be used for triggering auto/timed workItems.
	 */	
	public Resource getDefaultResourceForAutoTrigger();
	
	/**
	 * <p>Checks if the triggerContextResource is part of the a resource in the list.</p>
	 * 
	 * <p>The triggerContextResource could be a group itself, which is part of another group.</p>
	 *  
	 * 
	 * @param triggerContext the triggerContext that contains the resource
	 * @param resourceIds the resource list to check against
	 * @return return true if the TriggerContextResource is part of Resource in the list or TriggerContextResource is same as the passed one of the 
	 * 			resources in the list. Otherwise false.
	 */
	public boolean isTriggerResourcePartOfResource(TriggerContext triggerContext, String[] resourceIds);
		
}
