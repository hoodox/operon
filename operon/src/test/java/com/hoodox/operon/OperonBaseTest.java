package com.hoodox.operon;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.operon.workflow.Operon;
import com.hoodox.operon.workflow.WorkflowEngine;

public class OperonBaseTest {
	
	private WorkflowEngine workflowEngine;
	
	protected void initialise() throws Exception {
		String[] configList = {"/META-INF/applicationContext.xml","/META-INF/operon-dataAccessContext-local.xml"};
		ApplicationContext appCtx = new ClassPathXmlApplicationContext(configList);
		Operon.getInstance().initialiseApplication("tailorit", appCtx);		
		this.workflowEngine = Operon.getInstance().getWorkflowEngine("tailorit");
		 		 		
	}

	protected void tearDown() throws Exception {
	}

	
	public WorkflowEngine getWorkflowEngine() {
		if (this.workflowEngine == null) {
			try {
				this.initialise();
				
			} catch (Exception e) {
				throw new BaseSystemException(e);
				
			}
		}
		
		return this.workflowEngine;
	}
}
