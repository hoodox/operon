package com.hoodox.operon.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.exceptions.ResourceAccessDeniedException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.valueobjects.TaskVo;
import com.hoodox.operon.wfnet.CaseType;
import com.hoodox.operon.wfnet.Transition;

/**
 * 
 * @author huac
 *
 */
public class WorkItem {
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());	
	
	private TaskVo taskVo;
	private CaseType caseType;
	private String applicationName;
	
	protected WorkItem(String applicationName, TaskVo taskVo, CaseType caseType) {
		this.taskVo = taskVo;
		this.applicationName = applicationName;
		this.caseType = caseType;
	}

	public TaskVo getTaskVo() {
		return taskVo;
	}

	public CaseType getCaseType () {
		return this.caseType;
	}
	
	/**
	 * Starts the WorkItem
	 * @param triggerContext
	 * @return Activity if this is a manual Triggered WorkItem otherwise null;
	 */
	public Activity start(TriggerContext triggerContext) throws ResourceAccessDeniedException, ActionExecutionException {
		WorkflowCoreEngine workflowCoreEngine = (WorkflowCoreEngine) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngine);
		Transition transition = this.caseType.findTransitionById(this.taskVo.getWfnetTransitionRef());
		if (Const.TRIGGER_TYPE_message.equals(transition.getTriggerType()) ) {
			// Its a message used this method
			WorkItem[] workItems = workflowCoreEngine.fireTokensAtWorkItemForMessageTrigger(this, triggerContext);

			//====================================
			// kick off any auto or time triggered
			// workItems
			//====================================
			WorkflowCoreEngineImpl._kickOffAutoOrTimeTriggeredWorkItems(workItems, this.applicationName, this.log);
			return null;
			
		} 
			
		return workflowCoreEngine.fireTokensAtWorkItem(this, triggerContext);
			
	
		
		
		
		
	}
	
	
}
