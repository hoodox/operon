package com.hoodox.operon.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.commons.exceptions.IBaseException;
import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.exceptions.ResourceAccessDeniedException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.persistence.iface.CaseDao;
import com.hoodox.operon.valueobjects.TaskVo;
import com.hoodox.operon.wfnet.CaseType;
import com.hoodox.operon.wfnet.Transition;

public class Activity {
	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());	
	
	private String applicationName;
	private TaskVo taskVo = null;
	private boolean shouldAuditBecauseItsAutoTriggered = false;
	private CaseType caseType;
	


	protected Activity (String applicationName, TaskVo taskVo, CaseType caseType) {
		this.taskVo = taskVo;
		this.caseType = caseType;
		this.applicationName = applicationName;
		
		Transition transition = caseType.findTransitionById(taskVo.getWfnetTransitionRef());
		if ( Const.TRIGGER_TYPE_auto.equals(transition.getTriggerType()) 
				|| Const.TRIGGER_TYPE_time.equals(transition.getTriggerType())) {
			
			this.shouldAuditBecauseItsAutoTriggered = true;
		}
	}
	
	public String getApplicationName() {
		return applicationName;
	}
	
	public TaskVo getTaskVo() {
		return taskVo;
	}
	
	
	
	public CaseType getCaseType() {
		return this.caseType;
	}

	
	
	
	public void finish(TriggerContext triggerContext) throws ActionExecutionException, ResourceAccessDeniedException {
		String currentStatus = this.taskVo.getTaskStatus();
		
		//====================================
		// Check if only System resource is allowed
		// to finish this
		//====================================
		WorkflowCoreEngineImpl._checkIfOnlySystemResourceAllowedToTrigger(triggerContext.getCurrentResource(), this.getTaskVo().getWfnetTransitionRef(), this.caseType);
		
		
		//=======================
		// If get here resource
		// allowed to trigger this
		//========================
		WorkItem workItems[] = null;

		Audit audit = (Audit) Operon.getInstance().getApplicationContext(this.applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_audit);
		
		
		try {
			//==================================
			// Finish Activity in its own Transaction
			//==================================
			WorkflowCoreEngine workflowCoreEngine = (WorkflowCoreEngine) Operon.getInstance().getApplicationContext(this.applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngine);
			workItems =  workflowCoreEngine.finishActivity(this, triggerContext);
			
			//====================================
			// kick off any auto or time triggered
			// workItems
			//====================================
			WorkflowCoreEngineImpl._kickOffAutoOrTimeTriggeredWorkItems(workItems, this.applicationName, this.log);
			
			
		} catch (ActionExecutionException e) {
			if (!e.isLogged()) {
				e.setLogged();
				log.error(e.getMessage(), e);
			}
			
			if (this.shouldAuditBecauseItsAutoTriggered) {
				CaseDao caseDao = (CaseDao) Operon.getInstance().getApplicationContext(this.applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_caseDao);
				audit.auditErrorWithNoStatusChange(null, this.getTaskVo(), currentStatus, e, triggerContext.getCurrentResource(), Const.TASK_EVENT_finish, caseDao);
				
			}
			throw e;
			
		} catch (RuntimeException e) {
			if ( e instanceof IBaseException ) {
				IBaseException ibaseEx = (IBaseException) e;
				if (!ibaseEx.isLogged()) {
					ibaseEx.setLogged();
					log.error(ibaseEx.getMessage(), e);
				}
			} else {
				log.error(e.getMessage(), e);
			}
			
			if (this.shouldAuditBecauseItsAutoTriggered) {
				CaseDao caseDao = (CaseDao) Operon.getInstance().getApplicationContext(this.applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_caseDao);				
				audit.auditErrorWithNoStatusChange(null, this.getTaskVo(), currentStatus, e, triggerContext.getCurrentResource(), Const.TASK_EVENT_finish, caseDao);
			}
			
			throw e;
			
		}
	}
	
	
}
