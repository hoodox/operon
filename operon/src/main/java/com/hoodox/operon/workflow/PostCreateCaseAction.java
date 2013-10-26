package com.hoodox.operon.workflow;

import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.wfnet.interfaces.Action;

/**
 * <p>Abstract PostCreateCase Action All PostCreateCaseAction classes must extend
 * this.</p>
 * 
 * <p><b>Usage example:</b><br> 
 * In the <code>execute()</code> you may wish to assign the
 * created Case Id to some applictioan specific entity.</p>
 * 
 * </p>For example
 * 
 * <code><pre>
 * public void execute(TriggerContext triggerContext) {
 *  //some code before......
 * 	SomeObject someObject = triggerContext.getTriggerDataMap().get("SomeEntityObject");
 * 
 * 	//assign object with createdCaseId
 * 	someObject.setCaseId(getCreatedCaseId());
 *	dao.add(someObject);
 * 
 * 	//more code......
 * }</pre></code>
 * 
 * @author huac
 * 
 */
public abstract class PostCreateCaseAction implements Action {

	private Long createdCaseId = null;
	
	/**
	 * <p>Sets the createdCaseId</p>
	 * 
	 * <p><b>NOTE</b>: Used internally by the Operon Framework</p>
	 * 
	 * @param createdCaseId
	 */
	public final void _internalUse_setCreatedCaseId(Long createdCaseId) {
		this.createdCaseId = createdCaseId;
	}
	
	/**
	 * <p>Returns the CreatedCaseId.</p>
	 * 
	 * <p>Should be used by implementations of the <code>execute</code> Method</p>
	 * @return
	 */
	public final Long getCreatedCaseId() {
		return this.createdCaseId;
	}
	
	/**
	 * <p>Put any post create Case work here</p>
	 * <p>Workflow engine will call this method to execute the Action everytime it creates a new case.</p>
	 * 
	 * @param triggercontext the triggerContext that contains the DataMap and Resource to execute the WorkItems
	 * @throws ActionExecutionException if an error occured. All application exceptions should be encapsulated in this ActionExecutionException 
	 */
	public abstract void execute(TriggerContext triggerContext) throws ActionExecutionException ;
}
