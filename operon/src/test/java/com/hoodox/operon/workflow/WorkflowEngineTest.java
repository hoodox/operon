package com.hoodox.operon.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hoodox.operon.OperonBaseTest;
import com.hoodox.operon.actions.CheckCreditCardAction;
import com.hoodox.operon.exceptions.CaseTypeNotExistException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.resources.TestResource;
import com.hoodox.operon.workflow.Activity;
import com.hoodox.operon.workflow.Case;
import com.hoodox.operon.workflow.TriggerContext;
import com.hoodox.operon.workflow.WorkItem;
import com.hoodox.operon.workflow.WorkflowEngine;

public class WorkflowEngineTest extends OperonBaseTest {

	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());	

	
	//==========================================
	// Tests here
	//==========================================
	
	/**
	 * Tests the open case
	 */
	@Test(groups = { "operon.workflow" })
	public void testOpenCase() {
		final String methodName = "testOpenCase()";		
		
		log.info("========================Start " + methodName + "============================================");

		CheckCreditCardAction.setSuccess(Boolean.TRUE);
		
		WorkflowEngine wfe = getWorkflowEngine();

		Case aCase = null;
		
		try {
			TriggerContext triggCtx = new TriggerContext();
			triggCtx.setCurrentResource(new TestResource("packerGroup", true));
			aCase = wfe.openCase("SampleNet", triggCtx);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
				
		}
		
		log.info("Opened CaseId " + aCase.getCaseVo().getCaseId());
	
		log.info("next Transition should be AUTO ChargeCreditCard " + aCase.getCaseVo().getCaseId());
		
		try {
			log.info("next, Transtion is Manual Pack. Will Manually start the Packing....");
			Thread.sleep(5000); //5secs			
			WorkItem packWorkItem = findWorkItemByTransRef("Pack", aCase.getAllWorkItemsFromRootCase());			
			TriggerContext packTrigCtx = new TriggerContext();
			packTrigCtx.getCaseAttributes().put("complete", Boolean.TRUE);
			packTrigCtx.setCurrentResource(new TestResource("packerGroup", true));
			Activity packActivity = packWorkItem.start(packTrigCtx);
			packActivity.finish(packTrigCtx);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
						
		}
		
		try {
			log.info("next, Transtion is Manual Ship. Will Manually start the Shipping....");
			Thread.sleep(5000); //5secs
			WorkItem shipWorkItem = findWorkItemByTransRef("Ship", aCase.getAllWorkItemsFromRootCase());
			TriggerContext shipTrigCtx = new TriggerContext();
			shipTrigCtx.getCaseAttributes().put("complete", Boolean.TRUE);
			shipTrigCtx.setCurrentResource(new TestResource("shipperGroup", true));
			shipWorkItem.start(shipTrigCtx).finish(shipTrigCtx);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
			
		}
		
		try {
			log.info("next, Transtion is SubCase Manual CallCustomer. Will Manually start the CallingCustomer....");
			Thread.sleep(5000); //5secs
			WorkItem shipWorkItem = findWorkItemByTransRef("CallCustomer", aCase.getAllWorkItemsFromRootCase());
			TriggerContext triggerContext = new TriggerContext();
			triggerContext.setCurrentResource(new TestResource("MarketGroup", true));
			shipWorkItem.start(triggerContext).finish(triggerContext);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
			
		}

		try {
			log.info("next, Transtion is SubCase Message SaveMarketData. Will Message start the SaveMarketData....");
			Thread.sleep(5000); //5secs
			WorkItem shipWorkItem = findWorkItemByTransRef("SaveMarketData", aCase.getAllWorkItemsFromRootCase());
			TriggerContext triggerContext = new TriggerContext();
			triggerContext.setCurrentResource(new TestResource("SystemGroup", true));
			shipWorkItem.start(triggerContext);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
			
		}
		
		try {
			Case currentCase = wfe.getCaseById(aCase.getCaseVo().getCaseId());
			Assert.assertEquals(currentCase.getCaseVo().getCaseStatus(), Const.CASE_STATUS_closed);
			
		} catch (CaseTypeNotExistException e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
			
		}
		
		log.info("========================End " + methodName + "============================================");
		
	}

	/**
	 * Test cancelling a Case
	 */
	@Test(groups = { "operon.workflow" })
	public void testCancelCase() {
		final String methodName = "testCancelCase()";

		log.info("========================Start " + methodName + "============================================");
		
		WorkflowEngine wfe = getWorkflowEngine();

		Case aCase = null;
		
		try {
			TriggerContext triggCtx = new TriggerContext();
			triggCtx.setCurrentResource(new TestResource("packerGroup", true));
			aCase = wfe.openCase("SampleNet", triggCtx);

			log.info("Opened CaseId " + aCase.getCaseVo().getCaseId());
			
			log.info("next Transition should be AUTO ChargeCreditCard " + aCase.getCaseVo().getCaseId());
			

			log.info("next, Transtion is Manual Pack.....");
			Thread.sleep(5000); //5secs			
			
			log.info("next, Will cancel this case....");
			
			aCase.cancelCase(triggCtx, "Just Testing");
			
			log.info("next, Cancelled the case....");
			
			WorkItem packWorkItem = findWorkItemByTransRef("Pack", aCase.getAllWorkItemsFromRootCase());
			
			Assert.assertNull(packWorkItem, "Case " + aCase.getCaseVo().getCaseId() +  " meant to be cancelled but still found WorkItem " + ( null== packWorkItem ? "" : packWorkItem.getTaskVo().getTaskId()));
			Assert.assertEquals(aCase.getCaseVo().getCaseStatus(), Const.CASE_STATUS_cancelled);

			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
				
		}		
		
		log.info("========================End " + methodName + "============================================");
		
	}

	/**
	 * Test suspends a case and then resumes it
	 */
	@Test(groups = { "operon.workflow" })
	public void testSuspendAndResumeCase() {
		final String methodName = "testSuspendAndResumeCase()";

		log.info("========================Start " + methodName + "============================================");
		
		WorkflowEngine wfe = getWorkflowEngine();

		Case aCase = null;
		
		try {
			TriggerContext triggCtx = new TriggerContext();
			triggCtx.setCurrentResource(new TestResource("packerGroup", true));
			aCase = wfe.openCase("SampleNet", triggCtx);

			log.info("Opened CaseId " + aCase.getCaseVo().getCaseId());
			
			log.info("next Transition should be AUTO ChargeCreditCard " + aCase.getCaseVo().getCaseId());
			

			log.info("next, Transtion is Manual Pack.....");
			Thread.sleep(5000); //5secs			
			
			log.info("next, Will suspend this case " + aCase.getCaseVo().getCaseId() + "....");
			
			aCase.suspendCase(triggCtx, "Just Testing Suspend Case " + aCase.getCaseVo().getCaseId());

			log.info("next, suspended the case " + aCase.getCaseVo().getCaseId() + "....");
			
			WorkItem packWorkItem = findWorkItemByTransRef("Pack", aCase.getAllWorkItemsFromRootCase());
			Assert.assertNotNull(packWorkItem, "Case " + aCase.getCaseVo().getCaseId() +  " meant to be suspended and WorkItem " + ( null== packWorkItem ? "" : packWorkItem.getTaskVo().getTaskId()) + " should still be enabled ");
			Assert.assertEquals(aCase.getCaseVo().getCaseStatus(), Const.CASE_STATUS_suspended);

			Thread.sleep(5000); //5secs			
			
			log.info("next, Will resume this case " + aCase.getCaseVo().getCaseId() + "....");
			
			aCase.resumeCase(triggCtx, "Just Testing resume Case");
			
			Thread.sleep(5000); //5secs

			log.info("next, resumed the case " + aCase.getCaseVo().getCaseId() + "....");
			
			packWorkItem = findWorkItemByTransRef("Pack", aCase.getAllWorkItemsFromRootCase());
			Assert.assertNotNull(packWorkItem, "Case " + aCase.getCaseVo().getCaseId() +  " is meant to be resumed but cannot find Pack WorkItem ");
			Assert.assertEquals(aCase.getCaseVo().getCaseStatus(), Const.CASE_STATUS_open);
			
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		}
		
		log.info("========================End " + methodName + "============================================");
		
	}
	
	/**
	 * Tests the open case
	 */
	@Test(groups = { "operon.workflow" })	
	public void testOpenCaseDifferentRoute() {
		final String methodName = "testOpenCaseDifferentRoute()";

		log.info("========================Start " + methodName + "============================================");
		
		CheckCreditCardAction.setSuccess(Boolean.FALSE);
		
		WorkflowEngine wfe = getWorkflowEngine();

		Case aCase = null;
		
		try {
			TriggerContext triggCtx = new TriggerContext();
			triggCtx.setCurrentResource(new TestResource("packerGroup", true));
			aCase = wfe.openCase("SampleNet", triggCtx);
			log.info("Opened CaseId " + aCase.getCaseVo().getCaseId());
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		}
		
		log.info("next Transition should be AUTO ChargeCreditCard " + aCase.getCaseVo().getCaseId());
		log.info("next Transition should be TIME SpamCustomer " + aCase.getCaseVo().getCaseId());

		
		try {
			log.info("next, Transtion should be Message UpdateBillingInfo. Will Message triggered start the UpdateBillingInfo....");
			Thread.sleep(60000); //5secs			
			CheckCreditCardAction.setSuccess(Boolean.TRUE);
			WorkItem workItem = findWorkItemByTransRef("UpdateBillingInfo", aCase.getAllWorkItemsFromRootCase());			
			TriggerContext trigCtx = new TriggerContext();
			trigCtx.getCaseAttributes().put("updated", Boolean.TRUE);
			trigCtx.setCurrentResource(new TestResource("systemGroup", true));
			workItem.start(trigCtx);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
						
		}


		try {
			log.info("next, Transtion is Manual Pack. Will Manually start the Packing....but incomplete....");
			Thread.sleep(5000); //5secs			
			WorkItem packWorkItem = findWorkItemByTransRef("Pack", aCase.getAllWorkItemsFromRootCase());			
			TriggerContext packTrigCtx = new TriggerContext();
			packTrigCtx.getCaseAttributes().put("incomplete", Boolean.TRUE);
			packTrigCtx.setCurrentResource(new TestResource("packerGroup", true));
			Activity packActivity = packWorkItem.start(packTrigCtx);
			packActivity.finish(packTrigCtx);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
						
		}

		try {
			log.info("next, Transition should be Manual BackOrder. Will Manually start the BackOrder....");
			Thread.sleep(5000); //5secs			
			WorkItem workItem = findWorkItemByTransRef("BackOrder", aCase.getAllWorkItemsFromRootCase());			
			TriggerContext trigCtx = new TriggerContext();
			trigCtx.getCaseAttributes().put("incomplete", Boolean.TRUE);
			trigCtx.setCurrentResource(new TestResource("SystemGroup", true));
			Activity activity = workItem.start(trigCtx);
			activity.finish(trigCtx);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
						
		}

		try {
			log.info("next, Transition should be Manual Receive. Will Manually start the Receive....");
			Thread.sleep(5000); //5secs			
			WorkItem workItem = findWorkItemByTransRef("Receive", aCase.getAllWorkItemsFromRootCase());			
			TriggerContext trigCtx = new TriggerContext();
			trigCtx.getCaseAttributes().put("incomplete", Boolean.TRUE);
			trigCtx.setCurrentResource(new TestResource("SystemGroup", true));
			Activity activity = workItem.start(trigCtx);
			activity.finish(trigCtx);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
						
		}

		try {
			log.info("next, Transtion is back to Manual Pack. Will Manually start the Packing and complete...");
			Thread.sleep(5000); //5secs			
			WorkItem packWorkItem = findWorkItemByTransRef("Pack", aCase.getAllWorkItemsFromRootCase());			
			TriggerContext packTrigCtx = new TriggerContext();
			packTrigCtx.getCaseAttributes().put("complete", Boolean.TRUE);
			packTrigCtx.setCurrentResource(new TestResource("packerGroup", true));
			Activity packActivity = packWorkItem.start(packTrigCtx);
			packActivity.finish(packTrigCtx);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
						
		}
		
		try {
			log.info("next, Transtion is Manual Ship. Will Manually start the Shipping....");
			Thread.sleep(5000); //5secs
			WorkItem shipWorkItem = findWorkItemByTransRef("Ship", aCase.getAllWorkItemsFromRootCase());
			TriggerContext shipTrigCtx = new TriggerContext();
			shipTrigCtx.getCaseAttributes().put("complete", Boolean.TRUE);
			shipTrigCtx.setCurrentResource(new TestResource("shipperGroup", true));
			shipWorkItem.start(shipTrigCtx).finish(shipTrigCtx);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
			
		}
		
		try {
			log.info("next, Transtion is SubCase Manual CallCustomer. Will Manually start the CallingCustomer....");
			Thread.sleep(5000); //5secs
			WorkItem shipWorkItem = findWorkItemByTransRef("CallCustomer", aCase.getAllWorkItemsFromRootCase());
			TriggerContext triggerContext = new TriggerContext();
			triggerContext.setCurrentResource(new TestResource("MarketGroup", true));
			shipWorkItem.start(triggerContext).finish(triggerContext);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
			
		}

		try {
			log.info("next, Transtion is SubCase Message SaveMarketData. Will Message start the SaveMarketData....");
			Thread.sleep(5000); //5secs
			WorkItem shipWorkItem = findWorkItemByTransRef("SaveMarketData", aCase.getAllWorkItemsFromRootCase());
			TriggerContext triggerContext = new TriggerContext();
			triggerContext.setCurrentResource(new TestResource("SystemGroup", true));
			shipWorkItem.start(triggerContext);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
			
		}
		
		try {
			Thread.sleep(5000); //5secs
			Case currentCase = wfe.getCaseById(aCase.getCaseVo().getCaseId());
			Assert.assertEquals(currentCase.getCaseVo().getCaseStatus(), Const.CASE_STATUS_closed);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
			
		}
		
		log.info("========================End " + methodName + "============================================");
		
	}
	

	/**
	 * Tests the open case
	 */
	@Test(groups = { "operon.workflow" })
	public void testOpenCaseCancelOrderRoute() {
		final String methodName = "testOpenCaseCancelOrderRoute()";
		
		log.info("========================Start " + methodName + "============================================");
		
		CheckCreditCardAction.setSuccess(Boolean.FALSE);
		
		WorkflowEngine wfe = getWorkflowEngine();

		Case aCase = null;
		
		try {
			TriggerContext triggCtx = new TriggerContext();
			triggCtx.setCurrentResource(new TestResource("packerGroup", true));
			aCase = wfe.openCase("SampleNet", triggCtx);
			log.info("Opened CaseId " + aCase.getCaseVo().getCaseId());
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		}
		
		log.info("next Transition should be AUTO ChargeCreditCard " + aCase.getCaseVo().getCaseId());
		log.info("next Transition should be TIME SpamCustomer " + aCase.getCaseVo().getCaseId());

		log.info("next, Transtion should be TIME CancelOrder. Will wait for TIME trigger to activate this....");
		
		
		try {
			log.info("next, Transtion is SubCase Manual CallCustomer. Will Manually start the CallingCustomer....");
			WorkItem workItem = null;
			
			while (workItem == null) {
				log.info("waiting for CallCustomer to be enabled....");
				Thread.sleep(60000); //30secs
				workItem = findWorkItemByTransRef("CallCustomer", aCase.getAllWorkItemsFromRootCase());
				
			}
			TriggerContext triggerContext = new TriggerContext();
			triggerContext.setCurrentResource(new TestResource("MarketGroup", true));
			workItem.start(triggerContext).finish(triggerContext);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
			
		}

		try {
			log.info("next, Transtion is SubCase Message SaveMarketData. Will Message start the SaveMarketData....");
			Thread.sleep(5000); //5secs
			WorkItem shipWorkItem = findWorkItemByTransRef("SaveMarketData", aCase.getAllWorkItemsFromRootCase());
			TriggerContext triggerContext = new TriggerContext();
			triggerContext.setCurrentResource(new TestResource("SystemGroup", true));
			shipWorkItem.start(triggerContext);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
			
		}
		
		try {
			Thread.sleep(5000); //5secs
			Case currentCase = wfe.getCaseById(aCase.getCaseVo().getCaseId());
			Assert.assertEquals(currentCase.getCaseVo().getCaseStatus(), Const.CASE_STATUS_closed);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
			
		}
		
		log.info("========================End " + methodName + "============================================");
		
	}
	
	private static WorkItem findWorkItemByTransRef(String transitionRef, WorkItem[] workItems) {
		for (int i=0; i< workItems.length; i++) {
			if (transitionRef.equals(workItems[i].getTaskVo().getWfnetTransitionRef())) {
				return workItems[i];
			}
		}
		
		return null;
		
	}
}
