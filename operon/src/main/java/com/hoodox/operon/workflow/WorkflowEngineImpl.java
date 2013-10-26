package com.hoodox.operon.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.exceptions.CaseTypeNotExistException;
import com.hoodox.operon.exceptions.ResourceAccessDeniedException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.valueobjects.CaseVo;

public class WorkflowEngineImpl implements WorkflowEngine {
	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

	
	private String applicationName;
	

	public void _initialise(String applicationName) {
		this.applicationName = applicationName;
		
	}
	
	//===============================================
	// Interface implemenation methods
	//===============================================
	
	
	/**
	 * Opens a new Case
	 */
	public Case openCase(String caseTypeRef, TriggerContext triggerCtx)
			throws CaseTypeNotExistException, ActionExecutionException, ResourceAccessDeniedException {
		
		CaseVo newCaseVo = new CaseVo();
		
		//=============================
		// Calls in its own transaction
		//=============================
		WorkflowCoreEngine workflowCoreEngine = (WorkflowCoreEngine) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngine);
		
		WorkItem[] workItems = workflowCoreEngine.openCase(caseTypeRef, newCaseVo, triggerCtx);
						
		WorkflowCoreEngineImpl._kickOffAutoOrTimeTriggeredWorkItems(workItems, this.applicationName, this.log);
		
		
		return getCaseById(newCaseVo.getCaseId());
	}
			
	public Case[] findOpenCasesByType(String caseType) throws CaseTypeNotExistException {
		//@TODOD
		return null;
	}
	public Case[] getAllOpenCases() throws CaseTypeNotExistException {
		//@TODOD
		return null;
	}
	public Case getCaseById(Long caseId) throws CaseTypeNotExistException {
		WorkflowCoreEngineImpl workflowCoreEngineImpl = (WorkflowCoreEngineImpl) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngineTarget);
		return workflowCoreEngineImpl._getCaseById(caseId);
	}
			
	
	

}


