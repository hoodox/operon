package com.hoodox.operon.persistence.iface;

import com.hoodox.operon.exceptions.CaseNotExistException;
import com.hoodox.operon.valueobjects.CaseVo;
import com.hoodox.operon.valueobjects.EventAuditVo;
import com.hoodox.operon.valueobjects.TaskVo;
import com.hoodox.operon.valueobjects.TimeToLiveSchedulerVo;
import com.hoodox.operon.valueobjects.TimeTriggerSchedulerVo;
import com.hoodox.operon.valueobjects.TokenPlaceRefVo;
import com.hoodox.operon.valueobjects.TokenVo;

public interface CaseDao {
	
	//=================================
	// Crude methods
	//=================================
	
	/**
	 * Returns the Case by the case id
	 *  
	 */
	public CaseVo getCaseById(Long caseId);

	/**
	 * Adds a Case
	 * @returns the new Id
	 */
	public Long createCase(CaseVo aCase);
	
	/**
	 * Updates the Case status
	 * @param aCase
	 * @param currentLockVersion
	 */
	public void updateCaseStatus(CaseVo aCase, Long currentLockVersion);
	
	/**
	 * Remove a node
	 */
	public void removeCase(Long caseId) throws CaseNotExistException;
	
	/**
	 * Adds a TimeToLiveScheduler of a case
	 * @returns the new Id
	 */
	public void addTimeToLiveSchedulers(TimeToLiveSchedulerVo[] ttlSchedulerVos);

	/**
	 * Adds a TimeToLiveScheduler of a case
	 * @returns the new Id
	 */
	public void addTimeToLiveScheduler(TimeToLiveSchedulerVo ttlSchedulerVo);
	
	/**
	 * Creates a token
	 * 
	 * @param token
	 * @return
	 */
	public Long createToken(TokenVo token);

	/**
	 * Updates the token
	 * @param tokenVo
	 * @param lockedByworkItemVo the workItemVo that consumes/locked this Token. can passed null.
	 * @param currentLockVersion
	 */
	public void updateToken(TokenVo tokenVo, Long currentLockVersion);
	
	/**
	 * Puts the token in a Place
	 * 
	 * @param tokenPlaceref
	 */
	public void addTokenPlaceRef(TokenPlaceRefVo tokenPlaceref);

	/**
	 * Assoicate the Token that enabled the Task
	 * 
	 * @param tokenPlaceref
	 */
	public void addTokenEnabledTask(TokenVo tokenVo,  TaskVo workItemVo);
	
	/**
	 * Adds a workItem to the database
	 * @param workItem
	 * @return
	 */
	public Long createTask(TaskVo workItem);
	
	/**
	 * Updates the status of the Task
	 * @param workItem
	 * @param currentLockVersion the currentLockVersion
	 */
	public void updateTaskStatus(TaskVo workItemVo, Long currentLockVersion);

	
	
	/**
	 * Add the Time TriggerSchedulerVos
	 * @param timeTrigSchedVos
	 */
	public void addTimeTriggerSchedulers(TimeTriggerSchedulerVo[] timeTrigSchedVos);
	
	/**
	 * Adds the event audit
	 * @param eventAuditVo
	 */
	public void addEventAudit(EventAuditVo eventAuditVo);
	
	//===============================================
	// Finder methods
	//===============================================
	
	/**
	 * Find subCases
	 */
	public CaseVo[] findSubCasesByParentId(Long parentCaseId);
	
	/**
	 * Recursively finds all subcases from the root
	 * @param rootCaeseId
	 * @return
	 */
	public CaseVo[] getAllSubCasesFromRootId(Long rootCaeseId);
	
	/**
	 * Finds all Cases that are due for Expiration by the cronExp
	 * @param cronExp
	 * @return
	 */
	public CaseVo[] findAllCasesDueToExpireByCronExp(String cronExp);
	
	
	/**
	 * Checks how children Cases not closed
	 * @param parentCaseId
	 * @return
	 */
	public Integer howManyChildrenCaseNotClosed(Long parentCaseId);
	
	/**
	 * Finds all Token for a particular Case
	 * @param caseId the caseId
	 */
	public TokenPlaceRefVo[] findTokenPlaceRefVosByCaseId(Long caseId, String tokenStatus);

	/**
	 * Finds all Token for a particular Case
	 * @param caseId the caseId
	 */
	public TokenVo[] findFreeTokensThatEnabledTask(TaskVo workItemVo);
	
	/**
	 * Finds all enabled Tasks that were triggered by the token
	 * 
	 * @return an Array of TaskVo[], an empty array if nothing is found
	 */
	public TaskVo[] findEnabledTasksByTokenId (Long TokenId);
	
	/**
	 * Finds all the WorkItems from the RootCase
	 * @param rootCaseId
	 * @return
	 */
	public TaskVo[] findWorkItemsFromRootCaseId (Long rootCaseId);
	
	/**
	 * Finds all WorkItems in the passed Case
	 * @param caseId
	 * @return
	 */
	public TaskVo[] findWorkItemsFromCaseId (Long caseId);
	
	/**
	 * Finds all Actvities in the Case
	 * @param caseId
	 * @return
	 */
	public TaskVo[] findActivitiesFromCaseId (Long caseId);	
	
	/**
	 * Finds all Actvities from the root Case
	 * @param rootCaseId
	 * @return
	 */
	public TaskVo[] findActivitiesFromRootCaseId (Long rootCaseId);
	
	/**
	 * Finds all timeout activities
	 * @return
	 */
	public TaskVo[] findTimeoutActivities();
	
	/**
	 * Find all Activities awaiting_retry
	 * @return
	 */
	public TaskVo[] findActivitiesAwaitingRetry();
	
	/**
	 * finds all Activities that have startup=1
	 * @param status
	 * @return
	 */
	public TaskVo[] findActivitiesForStartup ();
	
	/**
	 * Find all Auto Triggerable WorkItems
	 * @return
	 */
	public TaskVo[] findAutoWorkItemsForStartup ();
	
	/***
	 * Finds all expired timed WorkItems that should have been started by the Timer
	 * @return
	 */
	public TaskVo[] findOverdueTimedWorkItemsForStartup () ;
	
	/**
	 * Finds all Explicit Timed WorkItems not overdue
	 * @return
	 */
	public TaskVo[] findNotOverdueExplicitTimedWorkItemsForStartup();
	
	/**
	 * Finds all implicit timed triggered WorkItems that are due to start by the cronExp
	 * @param cronExp
	 * @return
	 */
	public TaskVo[] findAllWorkItemsDueToStartByCronExp(String cronExp);
	
	/**
	 * Finds all existing cronExp for opened Case and enabled WorkItems
	 * @return
	 */
	public String[] findAllExistingOpenCaseCronExp();
}
