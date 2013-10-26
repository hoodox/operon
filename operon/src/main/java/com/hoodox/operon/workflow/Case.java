package com.hoodox.operon.workflow;

import java.util.ArrayList;
import java.util.List;

import com.hoodox.operon.exceptions.AutoTriggerInProgressException;
import com.hoodox.operon.exceptions.InvalidManualOperationException;
import com.hoodox.operon.exceptions.NotRootCaseException;
import com.hoodox.operon.exceptions.ResourceAccessDeniedException;
import com.hoodox.operon.exceptions.SetAttributeException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.persistence.iface.CaseDao;
import com.hoodox.operon.valueobjects.CaseVo;
import com.hoodox.operon.valueobjects.TaskVo;
import com.hoodox.operon.wfnet.CaseType;
import com.hoodox.operon.wfnet.RootCaseType;
import com.hoodox.operon.wfnet.SubCaseType;
/**
 * An instance of a Case
 * @author huac
 *
 */
public class Case {

	private CaseType caseType = null;

	private CaseVo caseVo;
	private String applicationName;
	
	protected Case (String applicationName, CaseVo caseVo, CaseType caseType) {
		this.caseVo = caseVo;
		this.caseType = caseType;
		this.applicationName = applicationName;
	}

	public CaseVo getCaseVo() {
		return caseVo;
	}
	
	
	
	public String getApplicationName() {
		return applicationName;
	}
	
	/**
	 * Cancels a Case
	 * @param triggerContext
	 * @param reason
	 * @throws NotRootCaseException - only can cancel from the Root Case.
	 * @throws ResourceAccessDeniedException Only groups that are able to start the Case can cancel the case.
	 * @throws SetAttributeException - A reason is required.
	 * @throws AutoTriggerInProgressException - Cannot cancel if an auto Triggered WorkItem/Activity is in enabled/progress.
	 */
	public void cancelCase(TriggerContext triggerContext, String reason) throws NotRootCaseException, ResourceAccessDeniedException, SetAttributeException, AutoTriggerInProgressException, InvalidManualOperationException {
		WorkflowCoreEngine workflowCoreEngine = (WorkflowCoreEngine) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngine);		
		workflowCoreEngine.manuallyCancelCase(this, triggerContext, reason);
	}
	
	public void suspendCase(TriggerContext triggerContext, String reason) throws NotRootCaseException, ResourceAccessDeniedException, SetAttributeException, AutoTriggerInProgressException, InvalidManualOperationException {
		WorkflowCoreEngine workflowCoreEngine = (WorkflowCoreEngine) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngine);		
		workflowCoreEngine.manuallySuspendCase(this, triggerContext, reason);
	}
	
	/**
	 * Resumes this suspended case
	 * 
	 * @param triggerContext
	 * @param reason
	 * @throws NotRootCaseException
	 * @throws ResourceAccessDeniedException
	 * @throws SetAttributeException
	 * @throws AutoTriggerInProgressException
	 * @throws InvalidManualOperationException
	 */
	public void resumeCase(TriggerContext triggerContext, String reason) throws NotRootCaseException, ResourceAccessDeniedException, SetAttributeException, AutoTriggerInProgressException, InvalidManualOperationException {
		WorkflowCoreEngine workflowCoreEngine = (WorkflowCoreEngine) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngine);		
		workflowCoreEngine.manuallyResumeCase(this, triggerContext, reason);
	}
	
	/**
	 * Gets all the WorkItems from the RootCase
	 * @return
	 */
	public WorkItem[] getAllWorkItemsFromRootCase() {
		CaseDao caseDao = (CaseDao) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_caseDao);
		TaskVo[] workItemVos = caseDao.findWorkItemsFromRootCaseId(caseVo.getRootParentCaseId());
		return _creatWorkItemsFromVos(this.applicationName, workItemVos, this.caseType);
	}
	
	
	public WorkItem[] getWorkItems() {
		CaseDao caseDao = (CaseDao) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_caseDao);		
		TaskVo[] workItemVos = caseDao.findWorkItemsFromCaseId(caseVo.getCaseId());
		return _creatWorkItemsFromVos(this.applicationName, workItemVos, this.caseType);
		
	}
	
	/**
	 * Get all the Activities from the RootCase
	 * @return
	 */
	public Activity[] getAllActivitiesFromRootCase() {
		CaseDao caseDao = (CaseDao) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_caseDao);
		TaskVo[] workItemVos = caseDao.findActivitiesFromRootCaseId(caseVo.getRootParentCaseId());
		return Case._creatActivitiesFromVos(this.applicationName, workItemVos, this.caseType);
	}

	
	/**
	 * Gets all the activities from the current Case
	 * @return
	 */
	public Activity[] getActivities() {
		CaseDao caseDao = (CaseDao) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_caseDao);
		TaskVo[] workItemVos = caseDao.findActivitiesFromCaseId(caseVo.getCaseId());		
		return Case._creatActivitiesFromVos(this.applicationName, workItemVos, this.caseType);
	}

	public Case[] getSubCases() {
		if (this.caseType.getAllSubCaseTypes().length == 0) {
			// no subcase
			return new Case[0];
			
		} 
		
		CaseDao caseDao = (CaseDao) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_caseDao);
		CaseVo[] caseVos = caseDao.findSubCasesByParentId(this.caseVo.getCaseId());
		return Case._createCasesFromvos(this.applicationName, caseVos, this.caseType);
			
		
	}

	public Case[] getSubCasesFromRoot() {
		if (this.caseType.getAllSubCaseTypes().length == 0) {
			// no subcase
			return new Case[0];
			
		} 
		
		CaseDao caseDao = (CaseDao) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_caseDao);
		CaseVo[] caseVos = caseDao.getAllSubCasesFromRootId(this.caseVo.getRootParentCaseId());
		return Case._createCasesFromvos(this.applicationName, caseVos, this.caseType);
			
		
	}
	
	/**
	 * Returns a list of subCases if any
	 * @return
	 */
	private static Case[] _createCasesFromvos(String applicationName, CaseVo[] caseVos, CaseType anyCaseType) {
		
		RootCaseType rootCaseType = null;
		if (anyCaseType instanceof RootCaseType) {
			rootCaseType = (RootCaseType) anyCaseType;
		} else {
			rootCaseType = ((SubCaseType) anyCaseType).getRootCaseType(); 
		}
		
		List<Case> caseList = new ArrayList<Case>();
		
		for (int i=0; i< caseVos.length; i++) {
			CaseType theCaseType = null;
			if (caseVos[i].getCaseId().longValue() == caseVos[i].getRootParentCaseId().longValue()) {
				theCaseType = rootCaseType;
			} else {
				theCaseType = rootCaseType.findSubCaseTypeById(caseVos[i].getCaseTypeRef());
			}
			
			Case aCase = new Case(applicationName, caseVos[i], theCaseType);
			caseList.add(aCase);
		}
		
		return caseList.toArray(new Case[caseList.size()]);
	}
	
	public CaseType getCaseType() {
		return caseType;
	}
	
	//=======================================
	// Private methods for optimisation
	//=======================================
	private static WorkItem[] _creatWorkItemsFromVos(String applicationName, TaskVo[] workItemVos, CaseType anyCaseType) {
		RootCaseType rootCaseType = null;
		
		if (anyCaseType instanceof RootCaseType) {
			rootCaseType = (RootCaseType) anyCaseType;
		} else {
			rootCaseType = ((SubCaseType) anyCaseType).getRootCaseType();
		}
		
		List<WorkItem> workItemList = new ArrayList<WorkItem>();
		for (int i=0; i< workItemVos.length; i++) {
			CaseType workItemCaseType = null;
			
			if (workItemVos[i].getCaseVo().getCaseTypeRef().equals(rootCaseType.getId())) {
				workItemCaseType = rootCaseType;
				
			} else {
				workItemCaseType = rootCaseType.findSubCaseTypeById(workItemVos[i].getCaseVo().getCaseTypeRef());
				
			}
			
			WorkItem workItem = new WorkItem(applicationName,workItemVos[i], workItemCaseType);
			workItemList.add(workItem);
		}
		
		return workItemList.toArray(new WorkItem[workItemList.size()]);
		
	}

	private static Activity[] _creatActivitiesFromVos(String applicationName, TaskVo[] workItemVos, CaseType anyCaseType) {
		RootCaseType rootCaseType = null;
		
		if (anyCaseType instanceof RootCaseType) {
			rootCaseType = (RootCaseType) anyCaseType;
		} else {
			rootCaseType = ((SubCaseType) anyCaseType).getRootCaseType();
		}
		
		List<Activity> activitytemList = new ArrayList<Activity>();
		for (int i=0; i< workItemVos.length; i++) {
			CaseType activityCaseType = null;
			
			if (workItemVos[i].getCaseVo().getCaseTypeRef().equals(rootCaseType.getId())) {
				activityCaseType = rootCaseType;
				
			} else {
				activityCaseType = rootCaseType.findSubCaseTypeById(workItemVos[i].getCaseVo().getCaseTypeRef());
				
			}
			
			Activity activity = new Activity(applicationName,workItemVos[i], activityCaseType);
			activitytemList.add(activity);
		}
		
		return activitytemList.toArray(new Activity[activitytemList.size()]);
		
	}
	
	
}
