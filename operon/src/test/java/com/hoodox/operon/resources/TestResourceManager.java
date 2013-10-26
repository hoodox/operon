/**
 * 
 */
package com.hoodox.operon.resources;

import com.hoodox.operon.exceptions.ResourceNotExistException;
import com.hoodox.operon.exceptions.TaskNotExistException;
import com.hoodox.operon.resourceiface.Resource;
import com.hoodox.operon.resourceiface.ResourceManager;
import com.hoodox.operon.workflow.TriggerContext;

/**
 * @author Chung
 *
 */
public class TestResourceManager implements ResourceManager {

	/**
	 * 
	 */
	public TestResourceManager() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.hoodox.operon.resourceiface.ResourceManager#getChildrenResourcesById(java.lang.String)
	 */
	public Resource[] getChildrenResourcesById(String resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.hoodox.operon.resourceiface.ResourceManager#getResourceById(java.lang.String)
	 */
	public Resource getResourceById(String resourceId) {
		
		return new TestResource(resourceId, true);
	}

	/* (non-Javadoc)
	 * @see com.hoodox.operon.resourceiface.ResourceManager#assignResourceToWorkItem(java.lang.String, java.lang.Long)
	 */
	public void assignResourceToWorkItem(String resourceId, Long workItemId)
			throws ResourceNotExistException, TaskNotExistException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.hoodox.operon.resourceiface.ResourceManager#removeResourceFromWorkItem(java.lang.String, java.lang.Long)
	 */
	public void removeResourceFromWorkItem(String resourceId, Long workItemId)
			throws ResourceNotExistException, TaskNotExistException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.hoodox.operon.resourceiface.ResourceManager#resourceHasAccessToWorkItem(java.lang.String, java.lang.Long)
	 */
	public boolean resourceHasAccessToWorkItem(String resourceId,
			Long workItemId) throws ResourceNotExistException,
			TaskNotExistException {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see com.hoodox.operon.resourceiface.ResourceManager#getDefaultResourceForAutoTrigger()
	 */
	public Resource getDefaultResourceForAutoTrigger() {
		// TODO Auto-generated method stub
		return new TestResource("Tester", false);
	}
	
	public boolean isTriggerResourcePartOfResource(TriggerContext triggerContext, String[] resourceIds) {
		Resource trigResource = triggerContext.getCurrentResource();
		
		for (int i=0; i<resourceIds.length; i++) {
			if (resourceIds[i].equalsIgnoreCase(trigResource.getId())) {
				return true;
			}
			
		}
		
		return false;
	}
	
}
