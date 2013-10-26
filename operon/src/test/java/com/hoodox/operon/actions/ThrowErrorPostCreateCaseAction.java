package com.hoodox.operon.actions;

import com.hoodox.commons.exceptions.ErrorCode;
import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.workflow.PostCreateCaseAction;
import com.hoodox.operon.workflow.TriggerContext;

public class ThrowErrorPostCreateCaseAction extends PostCreateCaseAction {

	public ThrowErrorPostCreateCaseAction() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void execute(TriggerContext triggerContext)
			throws ActionExecutionException {

		ActionExecutionException ex = new ActionExecutionException (new ErrorCode(
				Const.ERROR_CODE_operon_config_error),
				"This class is to test for rollbacks, Case " + getCreatedCaseId() + " should not be created");
		throw ex;

	}

}
