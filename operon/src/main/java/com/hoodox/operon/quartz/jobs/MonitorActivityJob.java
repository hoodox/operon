package com.hoodox.operon.quartz.jobs;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.operon.helper.Const;
import com.hoodox.operon.workflow.Operon;
import com.hoodox.operon.workflow.WorkflowCoreEngineImpl;

public class MonitorActivityJob implements Job {
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());	

	public MonitorActivityJob() {
		super();
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		//===============================
		// Trigger All due Expired Cases
		//===============================
		String applicationName = (String) context.getJobDetail().getJobDataMap().get(Const.QUARTZ_JOBDATAMAP_KEY_applicationName);
		WorkflowCoreEngineImpl workflowCoreEngineImpl = (WorkflowCoreEngineImpl) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngineTarget);
		CronTrigger cronTrigger = (CronTrigger) context.getTrigger();
		if (log.isDebugEnabled()) {
			log.debug("Triggering cron job exp " + cronTrigger.getCronExpression());
			
		}
		workflowCoreEngineImpl._monitorTimeoutActivities();

	}

}
