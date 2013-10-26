package com.hoodox.operon.wfnet.interfaces;

import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.workflow.TriggerContext;

/**
 * <p>The action interface that is registered with the Wfnet Transitions.</p>
 * 
 * <p>The application specific logics goes here</p>
 * @author huac
 *
 */
public interface Action {
	
	/**
	 * Workflow engine will call this method to execute the Action
	 * 
	 * @param triggercontext the triggerContext that contains the DataMap and Resource to execute the WorkItems
	 * @throws ActionExecutionException if an error occured. All application exceptions should be encapsulated in this ActionExecutionException
	 */
    public void execute(TriggerContext triggercontext) throws ActionExecutionException;
}