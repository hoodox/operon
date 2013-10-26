/**
 * 
 */
package com.hoodox.operon.quartz.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.valueobjects.TaskVo;
import com.hoodox.operon.workflow.Activity;
import com.hoodox.operon.workflow.Operon;
import com.hoodox.operon.workflow.TriggerContext;
import com.hoodox.operon.workflow.WorkItem;
import com.hoodox.operon.workflow.WorkflowCoreEngine;
import com.hoodox.operon.workflow.WorkflowCoreEngineImpl;

/**
 * Starts the Task automatically
 * 
 * @author huac
 *
 */
public class StartWorkItemJob implements Job {
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());	


	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug("Starting WorkItem " + context.getJobDetail().getName());		

		WorkItem workItem = (WorkItem) context.getJobDetail().getJobDataMap().get(Const.QUARTZ_JOBDATAMAP_KEY_workItem);
		TriggerContext triggCtx = (TriggerContext) context.getJobDetail().getJobDataMap().get(Const.QUARTZ_JOBDATAMAP_KEY_triggerContext);
		String applicationName = (String) context.getJobDetail().getJobDataMap().get(Const.QUARTZ_JOBDATAMAP_KEY_applicationName);
		WorkflowCoreEngine workflowCoreEngine = (WorkflowCoreEngine) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngine);
		
		try {
			Activity activity = workflowCoreEngine.fireTokensAtWorkItem(workItem, triggCtx);
			if (activity == null) {
				TaskVo taskVo = workItem.getTaskVo();
				log.warn("Fired Token at Transition " +taskVo.getWfnetTransitionRef() + "_" + taskVo.getTaskId() + " but no Activity is returned, maybe token is locked by something else...");
				return;
			}
			
			WorkflowCoreEngineImpl._autoTriggerFinishActivityUsingQuartzScheduler(activity);
		} catch (BaseAppException e) {
			if (!e.isLogged()) {
				log.error(e.getMessage(), e);
			}
			throw new JobExecutionException(e);
		}

	}

}
