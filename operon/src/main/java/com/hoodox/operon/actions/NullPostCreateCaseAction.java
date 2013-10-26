/**
 * 
 */
package com.hoodox.operon.actions;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.workflow.PostCreateCaseAction;
import com.hoodox.operon.workflow.TriggerContext;

/**
 * A PostCreateAction that does nothing
 * @author huac
 *
 */
public class NullPostCreateCaseAction extends PostCreateCaseAction {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());	

	/* (non-Javadoc)
	 * @see com.hoodox.operon.workflow.PostCreateCaseAction#execute(com.hoodox.operon.workflow.TriggerContext)
	 */
	@Override
	public void execute(TriggerContext triggerContext)
			throws ActionExecutionException {
		log.debug("Called" + this.getClass().getName() + " at " + SimpleDateFormat.getDateInstance().format(new Date()));

	}

}
