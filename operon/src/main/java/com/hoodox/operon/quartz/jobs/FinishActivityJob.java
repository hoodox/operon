/**
 * 
 */
package com.hoodox.operon.quartz.jobs;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.workflow.Activity;
import com.hoodox.operon.workflow.TriggerContext;

/**
 * <p>This Job finishes an Activity automatically</p>
 * 
 * @author huac
 *
 */
public class FinishActivityJob implements Job {
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());	
	

	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {		
		Activity activity = (Activity) context.getJobDetail().getJobDataMap().get(Const.QUARTZ_JOBDATAMAP_KEY_activity);
		TriggerContext triggCtx = (TriggerContext) context.getJobDetail().getJobDataMap().get(Const.QUARTZ_JOBDATAMAP_KEY_triggerContext);
		
		log.debug(" Finishing off Activity " + context.getJobDetail().getName());
		
		try {
			activity.finish(triggCtx);

			log.debug(SimpleDateFormat.getDateInstance().format(new Date()) + " Finished Activity " + context.getJobDetail().getName());
			
		} catch (BaseAppException e) {
			if (!e.isLogged()) {
				e.setLogged();
				log.error(e.getMessage(), e);
				
			}
		} catch (BaseSystemException e) {
			if (!e.isLogged()) {
				e.setLogged();
				log.error(e.getMessage(), e);
				
			}

		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
		}
		

	}

}
