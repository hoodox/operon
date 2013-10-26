package com.hoodox.operon.actions;

import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.workflow.CreateSubCasesAction;
import com.hoodox.operon.workflow.TriggerContext;

/**
 * Creates and registers one subcase only
 * @author Chung
 *
 */
public class DefaultCreateSubcasesAction extends CreateSubCasesAction {

	public DefaultCreateSubcasesAction() {
		super();
	}

	@Override
	public void execute(TriggerContext triggercontext)
			throws ActionExecutionException {
		
		this.createAndRegisterSubcaseId();
	}

}
