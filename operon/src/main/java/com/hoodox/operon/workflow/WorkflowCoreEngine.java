package com.hoodox.operon.workflow;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.exceptions.AutoTriggerInProgressException;
import com.hoodox.operon.exceptions.CaseTypeNotExistException;
import com.hoodox.operon.exceptions.InvalidManualOperationException;
import com.hoodox.operon.exceptions.NotRootCaseException;
import com.hoodox.operon.exceptions.ResourceAccessDeniedException;
import com.hoodox.operon.exceptions.ResourceNotExistException;
import com.hoodox.operon.exceptions.SetAttributeException;
import com.hoodox.operon.valueobjects.CaseVo;
import com.hoodox.operon.valueobjects.TaskVo;

/**
 * This class is used in Spring for defining Trasaction boundaries
 * 
 * @author Chung
 * 
 */
@Transactional(readOnly = true)
public interface WorkflowCoreEngine {

	/**
	 * <p>
	 * Opens a new Case
	 * </p>
	 * 
	 * @param caseTypeRef
	 * @param newCaseVo
	 * @param triggerCtx
	 * @return a list of workItems ready to be triggered
	 * @throws CaseTypeNotExistException
	 * @throws ActionExecutionException
	 * @throws ResourceAccessDeniedException
	 * @throws ResourceNotExistException
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = { CaseTypeNotExistException.class, ActionExecutionException.class,
			ResourceAccessDeniedException.class })
	public WorkItem[] openCase(String caseTypeRef, CaseVo newCaseVo, TriggerContext triggerCtx) throws CaseTypeNotExistException, ActionExecutionException,
			ResourceAccessDeniedException;

	/**
	 * <p>
	 * Manually Cancels the Case.
	 * </p>
	 * <p>
	 * Only RootCase can be suspended
	 * </p>
	 * 
	 * @param aCase
	 * @param triggerContext
	 * @param reason
	 * @throws NotRootCaseException
	 * @throws ResourceAccessDeniedException
	 * @throws SetAttributeException
	 * @thorws AutoTriggerInProgressException if an Auto/Timed WorkItem or
	 *         Activity is in progress
	 * @throws InvalidManualOperationException
	 *             - if the Case status is not Open
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = { SetAttributeException.class, AutoTriggerInProgressException.class})
	public void manuallyCancelCase(Case aCase, TriggerContext triggerContext, String reason) throws NotRootCaseException, ResourceAccessDeniedException,
			SetAttributeException, AutoTriggerInProgressException, InvalidManualOperationException;

	/**
	 * <p>
	 * Manually Suspends the Case
	 * </p>
	 * <p>
	 * Only RootCase can be suspended
	 * </p>
	 * 
	 * @param aCase
	 * @param triggerContext
	 * @param reason
	 * @throws NotRootCaseException
	 * @throws ResourceAccessDeniedException
	 * @throws SetAttributeException
	 * @thorws AutoTriggerInProgressException if an Auto/Timed WorkItem or
	 *         Activity is in progress
	 * @throws InvalidManualOperationException
	 *             - if the Case status is not Open
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = { SetAttributeException.class, AutoTriggerInProgressException.class,
			InvalidManualOperationException.class })
	public void manuallySuspendCase(Case aCase, TriggerContext triggerContext, String reason) throws NotRootCaseException, ResourceAccessDeniedException,
			SetAttributeException, AutoTriggerInProgressException, InvalidManualOperationException;

	/**
	 * <p>
	 * Manually Resumes a suspended Case.
	 * </p>
	 * <p>
	 * Only RootCase can be suspended
	 * </p>
	 * 
	 * @param aCase
	 * @param triggerContext
	 * @param reason
	 * @throws NotRootCaseException
	 * @throws ResourceAccessDeniedException
	 * @throws SetAttributeException
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = { SetAttributeException.class, NotRootCaseException.class,
			InvalidManualOperationException.class })
	public void manuallyResumeCase(Case aCase, TriggerContext triggerContext, String reason) throws NotRootCaseException, ResourceAccessDeniedException,
			SetAttributeException;

	/**
	 * Fire the associated Tokens at each WorkItem in the list.
	 * <p>
	 * Fires tokens at the Transition assoicated with the WorkItem
	 * </p>
	 * 
	 * <ul>
	 * <li>For each WorkItem, find all FREE Tokens that are associated with the
	 * WorkItem, LOCK all the Tokens</li>
	 * <ul>
	 * <li>For each Token make all other WorkItems that was ENABLED by the Token
	 * REDUNDANT</li>
	 * <li>Update Token to LOCKED and WorkItem to IN_PROGRESS and return
	 * WorkItem as an Activity</li>
	 * </ul>
	 * <li>If no Token is found i.e. another WorkItem has already consumed it
	 * then Make the WorkItem REDUNDANT</li> </ul> </ul>
	 * 
	 * @param workItems
	 * @param triggerContext
	 * @param caseDao
	 * @param audit
	 * @return A list of activities
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = {ResourceAccessDeniedException.class})
	public Activity fireTokensAtWorkItem(WorkItem workItem, TriggerContext triggerContext) throws ResourceAccessDeniedException;

	/**
	 * <p>
	 * Fires tokens at the Transition assoicated with the WorkItem
	 * </p>
	 * 
	 * <ul>
	 * <li>Find all FREE Tokens that are associated with the WorkItem, LOCK all
	 * the Tokens</li>
	 * <ul>
	 * <li>For each Token make all other WorkItems that was ENABLED by the Token
	 * REDUNDANT</li>
	 * <li>Update Token to LOCKED and WorkItem to IN_PROGRESS and return
	 * WorkItem as an Activity</li>
	 * </ul>
	 * <li>If no Token is found i.e. another WorkItem has already consumed it
	 * then Make the WorkItem REDUNDANT</li> </ul>
	 * 
	 * @param workItem
	 *            the workItem
	 * @param triggerContext
	 *            the triggerContext
	 * @param caseDao
	 * @param audit
	 *            - audit object to audit event.
	 * @return a list of new Enabled WorkItems
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = {ActionExecutionException.class})
	public WorkItem[] fireTokensAtWorkItemForMessageTrigger(WorkItem workItem, TriggerContext triggerContext) throws ResourceAccessDeniedException,
			ActionExecutionException;

	/**
	 * Fire the associated Tokens at each WorkItem in the list.
	 * <p>
	 * Fires tokens at the Transition assoicated with the WorkItem
	 * </p>
	 * 
	 * <ul>
	 * <li>For each WorkItem, find all FREE Tokens that are associated with the
	 * WorkItem, LOCK all the Tokens</li>
	 * <ul>
	 * <li>For each Token make all other WorkItems that was ENABLED by the Token
	 * REDUNDANT</li>
	 * <li>Update Token to LOCKED and WorkItem to IN_PROGRESS and return
	 * WorkItem as an Activity</li>
	 * </ul>
	 * <li>If no Token is found i.e. another WorkItem has already consumed it
	 * then Make the WorkItem REDUNDANT</li> </ul> </ul>
	 * 
	 * @param workItems
	 * @param triggerContext
	 * @param caseDao
	 * @param audit
	 * @return A list of activities
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)	
	public Activity[] fireTokensAtWorkItems(WorkItem[] workItems, TriggerContext triggerContext) throws ResourceAccessDeniedException;

	/**
	 * Finishes off an Activity
	 * 
	 * @param activity
	 * @param triggerContext
	 * @return A list of new WorkItems, null if we reach the end of the Case
	 * @throws ActionExecutionException
	 *             if an error occured during the execution of the Actions
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = {ActionExecutionException.class})
	public WorkItem[] finishActivity(Activity activity, TriggerContext triggerContext) throws ActionExecutionException;

	/**
	 * Executes a single Activity that is awaiting retry
	 * 
	 * @param workItemVo
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)	
	public void excuteASingleActivityAwaitingRetry(TaskVo workItemVo);

	/**
	 * Requeues a timeout Activity
	 * 
	 * @param workItemVo
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)	
	public void requeueASingleTimeoutActivity(TaskVo workItemVo);

	/**
	 * Expires the RootCase
	 * 
	 * @param aCase
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)	
	public void expireCase(Case aCase);

	// //================================================================
	// // Some finder methods
	// //================================================================
	// public Case[] findOpenCasesByType(String caseType) throws
	// CaseTypeNotExistException;
	//	
	// public Case[] getAllOpenCases() throws CaseTypeNotExistException;
	//	
	// public Case getCaseById(Long caseId) throws CaseTypeNotExistException;

	/**
	 * <p>
	 * Do not use this!!! <b>Internal Use Only</b>
	 * </p>
	 * 
	 * @param applicationName
	 */
	public void _initialise(String applicationName);

}
