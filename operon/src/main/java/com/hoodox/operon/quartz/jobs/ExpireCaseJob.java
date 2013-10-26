package com.hoodox.operon.quartz.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.operon.helper.Const;
import com.hoodox.operon.workflow.Case;
import com.hoodox.operon.workflow.Operon;
import com.hoodox.operon.workflow.WorkflowCoreEngine;

/**
 * Expires a Case and all of its WorkItems and Activities
 * @author huac
 *
 */
public class ExpireCaseJob implements Job {
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());	

	public ExpireCaseJob() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		Case expiredCase = (Case) context.getJobDetail().getJobDataMap().get(Const.QUARTZ_JOBDATAMAP_KEY_case);
		
		WorkflowCoreEngine workflowCoreEngine = (WorkflowCoreEngine) Operon.getInstance().getApplicationContext(expiredCase.getApplicationName()).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngine);
		workflowCoreEngine.expireCase(expiredCase);
		if (this.log.isDebugEnabled()) {
			log.debug("Expired Jog executed - Expired RootCase " + expiredCase.getCaseVo().getCaseTypeRef() + "_" + expiredCase.getCaseVo().getCaseId() + " and all of its subcases.");
			
		}

	}

}
