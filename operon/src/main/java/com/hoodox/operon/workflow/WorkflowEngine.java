package com.hoodox.operon.workflow;

import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.exceptions.CaseTypeNotExistException;
import com.hoodox.operon.exceptions.ResourceAccessDeniedException;

/**
 * <p>
 * The entry point into this framework.
 * </p>
 * 
 * <p>
 * This class should be configured as a singleton in the Spring Application
 * config.
 * </p>
 * 
 * @author HUAC
 * 
 */
public interface WorkflowEngine {
	
	
	/**
	 * Opens a new Case
	 * 
	 * @param caseTypeRef
	 * @param triggerCtx
	 * @return The New Case
	 * @throws CaseTypeNotExistException
	 *             if a CaseType does not exist
	 * @throws ActionExecutionException
	 *             if an error occurs during the exection of PostCreateAction or
	 *             CreateSubCasesAction classes
	 */
	public Case openCase(String caseTypeRef, TriggerContext triggerCtx)
			throws CaseTypeNotExistException, ActionExecutionException,
			ResourceAccessDeniedException;
	
	/**
	 * Finds All Cases by the caseType
	 * @param caseType
	 * @return
	 */
	public Case[] findOpenCasesByType(String caseType) throws CaseTypeNotExistException;
	
	/**
	 * Gets all Cases in this WorkflowEngine
	 * @return
	 */
	public Case[] getAllOpenCases() throws CaseTypeNotExistException;
	
	/**
	 * Gets a single Case by its Id
	 * @param caseId
	 * @return
	 */
	public Case getCaseById(Long caseId) throws CaseTypeNotExistException ;
	
	/**
	 * <p>Do not use this!!! <b>Internal Use Only</b></p>
	 * @param applicationName
	 */
	public void _initialise(String applicationName);
	
}
