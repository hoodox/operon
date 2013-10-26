package com.hoodox.operon.workflow;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.wfnet.interfaces.Action;

/**
 * <p>This action classes allows multiple Subcase instances of the same type to be
 * created.</p>
 * 
 * <p>Each time the <code>createAndRegisterSubcaseId()</code> method is called a
 * newly created subcaseId is created and registered with the WorkflowEngine.
 * Subsequently the subCase is created with the Id by the Workflow engine.</p>
 * 
 * <p>The <code>createAndRegisterSubcaseId()</code> method should be called
 * within the implementations of the <code>execute</code> method.</p>
 * 
 * <p><b>Usage example:</b><br>
 * In the implementation of the <code>execute()</code> method you may have a list of newly created
 * application specific entity objects from some executed workItem. For each
 * object in the list you may wish to assign the object with a subcaseId since
 * each of the object entity has its own worflow states. Here your would assign
 * a subcaseId to each entity object.</p>
 * 
 * <p>
 * For example
 * 
 * <code><pre>
 * public void execute(TriggerContext triggercontext) {
 *    // some code before.......
 * 
 *    //===============================
 *    // Create a subcase for each object
 *    // and assign the id to the object
 *    //================================
 *    SomeObject[] someObjects = (SomeObject[]) triggercontext.getTriggerDataMap().get(&quot;SomeObjects&quot;);
 *    for (int i=0; &lt; isomeObjects.length; i++) {
 *       Long newCaseId = this.createAndRegisterSubCaseId();
 *       someObjects[i].setCaseId(newCaseId);
 *       dao.add(someObjects[i]);
 *    }
 *    // some more code here.........
 * }
 * </pre></code>
 * 
 * @author huac
 * 
 */
public abstract class CreateSubCasesAction implements Action{
	
	private DataFieldMaxValueIncrementer idGenerator = null;
	List<Long> newlyRegisteredSubCaseIdList = new ArrayList<Long>();
	
	/**
	 * <p>Sets an id gnerator for the Case</p>
	 * 
	 * <p><b>NOTE:</b> Do Not Use !!!. This method is for the WorkflowEngine
	 * only</p>
	 * 
	 * @param idGenerator
	 */
	public final void _internalUse_setIdGenerator(Object idGenerator) {
		this.idGenerator = (DataFieldMaxValueIncrementer) idGenerator;
	}
	
	/**
	 * <p>The WorkflowEngine will call this method to get the newly registered
	 * Ids and create an instance of the subCase for each Id in the list. </p>
	 * 
	 * <p><b>NOTE:</b> Do Not Use !!!. This method is for the WorkflowEngine
	 * only</p>
	 * 
	 * @return the list of newly registered subcaseIds.
	 */
	public final Long[] _internalUse_getNewlyRegisteredCaseIds() {
		return newlyRegisteredSubCaseIdList.toArray(new Long[newlyRegisteredSubCaseIdList.size()]);
	}
	
	/**
	 * <p>Each time this method is called
	 * a new subCaseId is created and registered with the
	 * WorkflowEngine, subsequently the subcase will be created by the WorkflowEngine</p>
	 * 
	 * <p>This should be called in implementations of the  <code>execute()</code> method. </p>
	 * 
	 * @return The newly registered subCase Id
	 */
	public final Long createAndRegisterSubcaseId() {
		Long newId = new Long(this.idGenerator.nextLongValue());
		this.newlyRegisteredSubCaseIdList.add(newId);
		return newId;
	}
	
	/**
	 * Workflow engine will call this method to execute the Action after creating a list of new subcases from the newly generated list of subcase Ids.
	 * 
	 * @param triggercontext the triggerContext that contains the DataMap and Resource to execute the WorkItems
	 * @throws ActionExecutionException if an error occured. All application exceptions should be encapsulated in this ActionExecutionException 
	 * 
	 */
	public abstract void execute(TriggerContext triggercontext) throws ActionExecutionException ; 
}
