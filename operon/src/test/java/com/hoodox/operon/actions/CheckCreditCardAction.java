package com.hoodox.operon.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.wfnet.interfaces.Action;
import com.hoodox.operon.workflow.TriggerContext;

public class CheckCreditCardAction implements Action {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	private static Boolean success = Boolean.TRUE; // default
	
	public CheckCreditCardAction() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void execute(TriggerContext triggercontext)
			throws ActionExecutionException {
		
		log.debug("Called CheckCreditCard, will set case Attributes to " + success);
		
		if (success.booleanValue()) {
			triggercontext.getCaseAttributes().put("success", Boolean.TRUE);
			triggercontext.getCaseAttributes().put("failure", Boolean.FALSE);
			
		} else {
			triggercontext.getCaseAttributes().put("success", Boolean.FALSE);
			triggercontext.getCaseAttributes().put("failure", Boolean.TRUE);
			
		}

	}
	
	public static void setSuccess(Boolean succ) {
		success = succ;
	}
}
