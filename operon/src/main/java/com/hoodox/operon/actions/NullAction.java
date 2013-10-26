package com.hoodox.operon.actions;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.wfnet.interfaces.Action;
import com.hoodox.operon.workflow.TriggerContext;

public class NullAction implements Action {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());	
	

	public void execute(TriggerContext triggercontext)
			throws ActionExecutionException {
		log.debug("Called" + this.getClass().getName() + " at " + SimpleDateFormat.getDateInstance().format(new Date()));

	}

}
