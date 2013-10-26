package com.hoodox.operon.workflow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.commons.configurable.ConfigurationHelper;
import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;
import com.hoodox.commons.exceptions.IBaseException;
import com.hoodox.commons.util.ClassCache;
import com.hoodox.operon.exceptions.ActionExecutionException;
import com.hoodox.operon.exceptions.AutoTriggerInProgressException;
import com.hoodox.operon.exceptions.CaseTypeNotExistException;
import com.hoodox.operon.exceptions.EvaluationException;
import com.hoodox.operon.exceptions.InvalidManualOperationException;
import com.hoodox.operon.exceptions.NotRootCaseException;
import com.hoodox.operon.exceptions.OperonConfigException;
import com.hoodox.operon.exceptions.ResourceAccessDeniedException;
import com.hoodox.operon.exceptions.ResourceNotExistException;
import com.hoodox.operon.exceptions.SetAttributeException;
import com.hoodox.operon.exceptions.TaskNotExistException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.helper.bsf.BsfExpressionEvaluator;
import com.hoodox.operon.persistence.iface.CaseDao;
import com.hoodox.operon.quartz.jobs.ExpireCaseJob;
import com.hoodox.operon.quartz.jobs.FinishActivityJob;
import com.hoodox.operon.quartz.jobs.MonitorActivityJob;
import com.hoodox.operon.quartz.jobs.StartWorkItemJob;
import com.hoodox.operon.quartz.jobs.TriggerExpireCasesOrStartWorkItemsJob;
import com.hoodox.operon.resourceiface.Resource;
import com.hoodox.operon.resourceiface.ResourceManager;
import com.hoodox.operon.valueobjects.CaseVo;
import com.hoodox.operon.valueobjects.TaskVo;
import com.hoodox.operon.valueobjects.TimeToLiveSchedulerVo;
import com.hoodox.operon.valueobjects.TimeTriggerSchedulerVo;
import com.hoodox.operon.valueobjects.TokenPlaceRefVo;
import com.hoodox.operon.valueobjects.TokenVo;
import com.hoodox.operon.wfnet.CaseType;
import com.hoodox.operon.wfnet.CaseTypeManager;
import com.hoodox.operon.wfnet.OutArc;
import com.hoodox.operon.wfnet.Place;
import com.hoodox.operon.wfnet.RootCaseType;
import com.hoodox.operon.wfnet.SubCaseType;
import com.hoodox.operon.wfnet.Transition;
import com.hoodox.operon.wfnet.interfaces.Action;
import com.hoodox.operon.wfnet.toolspecific.CronExpression;
import com.hoodox.operon.wfnet.toolspecific.Scheduler;

public class WorkflowCoreEngineImpl  implements WorkflowCoreEngine {

	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	private String applicationName;
	private CaseTypeManager caseTypeManager;
	private ConfigurationHelper configurationHelper;
	private String operonRegistryFilename;
	private CaseDao caseDao;
	private int retryActivityMaxCount = 3; // default
	private String retryActivityCronInterval;
	
	
	public int getRetryActivityMaxCount() {
		return retryActivityMaxCount;
	}

	public void setRetryActivityMaxCount(int retryActivityMaxCount) {
		this.retryActivityMaxCount = retryActivityMaxCount;
	}

	public String getRetryActivityCronInterval() {
		return retryActivityCronInterval;
	}

	public void setRetryActivityCronInterval(String retryActivityCronInterval) {
		this.retryActivityCronInterval = retryActivityCronInterval;
	}

	/**
	 * Public constructor
	 * @param operonRegistryFilename
	 * @param configurationHelper
	 * @param caseDao
	 */
	public WorkflowCoreEngineImpl(String operonRegistryFilename,
									ConfigurationHelper configurationHelper,
									CaseDao caseDao) {
		
		this.configurationHelper = configurationHelper;
		this.operonRegistryFilename = operonRegistryFilename;
		this.caseDao = caseDao;		
		this.caseTypeManager = new CaseTypeManager(this.operonRegistryFilename,this.configurationHelper);
		
	}
	
	/**
	 * Restart any Activities, Auto/Timed WorkItems
	 * explicit and implicit schedulers
	 * that should have been triggered
	 * since the last time this was abruptly shutdown
	 */
	public void _initialise(String applicationName) {
		this.applicationName = applicationName;
		
		//======================================
		// Restart any Activities, Auto/Timed WorkItems
		// explicit and implicit schedulers
		// that should have been triggered
		// since the last time this was abruptly shutdown
		//======================================
		this._restartActivitiesAtStartup();
		try {
			this._restartExpiredTimedWorkitemsAtStartup();
			this._restartAutoWorkitemsAtStartup();
			this._reScheduledNotOverdueExplicitTimedWorkItemsAtStartup();
			
		} catch (ResourceAccessDeniedException e) {
			BaseSystemException ex = new BaseSystemException(new ErrorCode(
					Const.ERROR_CODE_operon_initialise_error),
					e.getMessage(), e);
			throw ex;

			
		}
		
		this._registerImplicitSchedulersWithQuartzAtStartup();
		this._startMonitoringForTimeoutActivitiesAtStartupUsingQuartzScheduler();
		
		
	}
	
	/**
	 * <p>Loads all the Action classes associated with the Activity and executes them one at a time.</p>
	 * 
	 * <p>Finds a new potential places and puts token in the Place</p>
	 * 
	 * @param activity
	 * @return an Array of WorkItems
	 */
	public WorkItem[] finishActivity(Activity activity, TriggerContext triggerContext) throws ActionExecutionException {
		
		CaseType caseType = activity.getCaseType();
		Transition transition = caseType.findTransitionById(activity.getTaskVo().getWfnetTransitionRef());

		
		//================================
		// Finds the Action classes
		// and executes them
		//================================		
		String[] actionNames = transition.getAllActions();
		for (int i=0; i<actionNames.length; i++) {
			Action action = null;
			try {
				Class<?> clazz = Class.forName(actionNames[i]);  
				action = (Action) clazz.newInstance();
				
			} catch (Exception e) {
				BaseSystemException ex = new OperonConfigException(new ErrorCode(
						Const.ERROR_CODE_operon_config_error),
						"Error while trying to load the Action class " + actionNames[i], e);
				
				log.error(ex.getMessage());			
				throw ex;
				
			}
			
			action.execute(triggerContext);
			
			
			
		}
		
		//==============================
		// Update Activity/TASK to FINISH
		//==============================
		_updateWorkItemStatus(activity.getTaskVo(), triggerContext, Const.TASK_EVENT_finish, Const.TASK_STATUS_finished, null, this.caseDao);
		
		
		//===============================
		// Find the next place to produce
		// the tokens
		//===============================
		
		Place[] newPlaces = _evaluatePlacesToPutToken(caseType, transition, activity, triggerContext, this.log);
		
		List<WorkItem> newWorkItemList = new ArrayList<WorkItem>();
		for (int i=0; i<newPlaces.length; i++) {
			if(log.isDebugEnabled()) {
				log.debug("Transition " + transition.getName()+ "(transition.getId()+ _" +  activity.getTaskVo().getTaskId() 
							+ " has produced token in Place " + newPlaces[i].getName() + "("+ newPlaces[i].getId()+ ")" );
			}
			
			WorkItem[] workItems	= null;
			try {
				workItems = _putTokenInASinglePlace(this.applicationName, activity.getTaskVo().getCaseVo().getCaseId(), newPlaces[i], caseType, triggerContext, caseDao, this.log);
				
			} catch (TaskNotExistException e) {
				BaseSystemException ex = new OperonConfigException(e);
				ex.setLogged();
				log.error(ex.getMessage());			
				throw ex;
				
			} catch (ResourceNotExistException e) {
				BaseSystemException ex = new OperonConfigException(e);
				ex.setLogged();
				log.error(ex.getMessage());			
				throw ex;

			}
			newWorkItemList.addAll(Arrays.asList(workItems));
		}
		
		
		return newWorkItemList.toArray(new WorkItem[newWorkItemList.size()]); 
	}
	
	private static Place[] _evaluatePlacesToPutToken(CaseType caseType, Transition transition, Activity activity, TriggerContext triggerCtx, Logger log) {
		
		/*
		 * TODO
		 * We should refactor this into a Command Pattern
		 * 
		 */
		
		Place[] possiblePlaces = CaseTypeManager.findAllPlacesOutFromTransition(caseType, transition);
													
		if (Const.TRANSITION_TYPE_XOR_split.equals(transition.getTransitionType())) {
			for (int i=0; i< possiblePlaces.length; i++) {
				OutArc outArc = CaseTypeManager.findOutArc(caseType, transition, possiblePlaces[i]);
				String guardExpression = outArc.getGuardExpression();
				
				//==========================
				// Evualuate the expression
				// return the first place that
				// matches
				//==========================
				BsfExpressionEvaluator evaluator = new BsfExpressionEvaluator(triggerCtx.getCaseAttributes());
				if (1== evaluator.eval(guardExpression)) {
					// evaluates to true, return this
					return new Place[] {possiblePlaces[i]};
				}
				
				if(log.isDebugEnabled()) {
					log.debug("Transition " + transition.getName() + "(" + transition.getId() + "_" +  activity.getTaskVo().getTaskId() 
							+ ") is an XOR_split, evaluated expression " + guardExpression + " which resulted in false therefore will NOT put Token in Place " + possiblePlaces[i].getId());
				}
				
			}
			
			//====================
			// ERROR if we get her
			//====================
			BaseSystemException ex = new EvaluationException(new ErrorCode(
					Const.ERROR_CODE_bsf_evaulation_error),
					"Case " + caseType.getId() + " has  Transition ref " + transition.getId() + " associated with Activity " + activity.getTaskVo().getTaskId() 
						+ " is an XOR_split Transiton after evaluating the all guard expression no Place can be found, please check the code or the net config.");
			throw ex;
		
		} else if (Const.TRANSITION_TYPE_XOR_split.equals(transition.getTransitionType())) {	
			
			// This supports Multiple Choice
			
			List<Place> chosenPlaces = new ArrayList<Place>();
			
			for (int i=0; i< possiblePlaces.length; i++) {
				OutArc outArc = CaseTypeManager.findOutArc(caseType, transition, possiblePlaces[i]);
				String guardExpression = outArc.getGuardExpression();
				
				//==========================
				// Evualuate the expression
				// return the first place that
				// matches
				//==========================
				BsfExpressionEvaluator evaluator = new BsfExpressionEvaluator(triggerCtx.getCaseAttributes());
				if (1== evaluator.eval(guardExpression)) {
					// evaluates to true, return this
					// add this to chosen list
					chosenPlaces.add(possiblePlaces[i]);
					
				} else {
					if(log.isDebugEnabled() && !chosenPlaces.isEmpty()) {
						log.debug("Transition " + transition.getName() + "(" + transition.getId() + "_" +  activity.getTaskVo().getTaskId() + ") is an XOR_split, evaluated expression " + guardExpression + " which resulted in false therefore will NOT put Token in Place " + possiblePlaces[i].getId());
					}
					
				}
				
				
			}
			
			return chosenPlaces.toArray(new Place[chosenPlaces.size()]);
			
			
			
		} else if (Const.TRANSITION_TYPE_AND_split.equals(transition.getTransitionType())) {
			//=======================
			// Can have multiple places
			//=======================
			return possiblePlaces;
			
		} else {
			//====================
			// NORMAL/AND_join/OR_join
			// there should be only one place here
			//==========================
			if (possiblePlaces.length != 1) {
				BaseSystemException ex = new OperonConfigException(new ErrorCode(
						Const.ERROR_CODE_operon_net_config_error),
						"Case " + caseType.getId() + " has  Transition ref " + transition.getId() + " associated with Activity " + activity.getTaskVo().getTaskId() 
							+ " is a NORMAL Transiton but it has more than one out Place, infact it has " + possiblePlaces.length 
							+ ". This should not happen please check the Net again.");
				throw ex;
				
			} 
			
			return possiblePlaces;
		} 

		
		
		
	}
	
	private static WorkItem[] _putTokenInASinglePlace(String applicationName, Long currentCaseId, Place place, CaseType caseType, TriggerContext triggerContext, CaseDao caseDao, Logger log) throws TaskNotExistException, ResourceNotExistException {
		if (Const.PLACE_TYPE_sink.equals(place.getType())) {
			//===============================
			// its an END Place will finish here
			//=================================
			CaseVo caseVo = caseDao.getCaseById(currentCaseId);
			_updateCaseStatus(caseVo, triggerContext, Const.CASE_EVENT_close, Const.CASE_STATUS_closed, null, caseDao);
			
			if(log.isDebugEnabled()) {
				log.debug("Place " + place.getName() + "(" + place.getId() + ") is a SINK no more further Transitions, will CLOSE Case " + currentCaseId);
			}
			
			return new WorkItem[0];
			
		} else if (Const.PLACE_TYPE_outref.equals(place.getType())) {
			//==================================
			// Its an OUT_REF Place
			// Update the currentCase to Finish
			// Get the reference parent Place and 
			// the parent Case and recursively call this
			//==================================
			CaseVo caseVo = caseDao.getCaseById(currentCaseId);
			_updateCaseStatus(caseVo, triggerContext, Const.CASE_EVENT_close, Const.CASE_STATUS_closed, null, caseDao);
			
			//======================================
			// Only put Token in OuterPlace if
			// all Subcase of ParentCase is CLOSED
			//======================================
			if ((caseVo.getParentCaseId() != null)  && !_isAllChildrenCaseClosed(caseVo.getParentCaseId(), caseDao)) {
				
				// lets not put any Token in OuterRef place until
				// all Subcases are closed.
				return new WorkItem[0];
			}
			
			Place parentOutRefPlace = place.getRefPlace();
			CaseType parentCaseType = ((SubCaseType) caseType).getParentCaseType();
			Long parentCaseId = caseVo.getParentCaseId();
			
			if(log.isDebugEnabled()) {
				log.debug("Place " + place.getName() + "("+ place.getId() + ") is an OutRef subnet " + caseType.getName() + "("+ caseType.getId() + ") will continue parentNet " 
						+ parentCaseType.getName() + "(" + parentCaseType.getId() +  ") by putting token in Place " + parentOutRefPlace.getName() + "(" + parentOutRefPlace.getId()+")");
			}
			
			return _putTokenInASinglePlace(applicationName, parentCaseId, parentOutRefPlace, parentCaseType, triggerContext, caseDao, log);									
			
		} else {
			//============================================
			// Must be an intermediate Place
			//============================================
			CaseVo caseVo = caseDao.getCaseById(currentCaseId);
			
			//==========================================
			// Create a Token, find the references place
			// if any and put the token in the Place
			//==========================================
			//create token
			TokenVo token = new TokenVo();
			token.setLockVersion(new Long(0));
			token.setTokenStatus(Const.TOKEN_STATUS_free);
			token.setCreatedDate(new Date());		
			token.setUpdatedDate(new Date());		

			token.setTokenId(caseDao.createToken(token));
			
			// put token in Place
			TokenPlaceRefVo intermedPlaceTokenRef = new TokenPlaceRefVo();
			intermedPlaceTokenRef.setTokenId(token.getTokenId());
			intermedPlaceTokenRef.setCaseId(caseVo.getCaseId());
			intermedPlaceTokenRef.setPlaceRef(place.getId());
			intermedPlaceTokenRef.setPlaceRefType(place.getType());
			intermedPlaceTokenRef.setCreatedDate(new Date());		
			intermedPlaceTokenRef.setUpdatedDate(new Date());		
			
			caseDao.addTokenPlaceRef(intermedPlaceTokenRef);
						
			//==================================
			// Find all Transitions from the place
			// enable associated
			// WorkItems
			//==================================
			List<WorkItem> allEnableWorkItemList = new ArrayList<WorkItem>();
			WorkItem[] newWorkItems = _enableWorkItemsFromPlaceWithToken(applicationName, caseVo, caseType, place, caseDao, triggerContext, token, log);
			allEnableWorkItemList.addAll(Arrays.asList(newWorkItems));
			
			//==========================
			// Check for subcases and enable
			// subcase workitems
			//==========================
			if ( ( null != caseType.getAllSubCaseTypes() )
					&& ( caseType.getAllSubCaseTypes().length != 0 ) ) {
				//====================================
				// Open any Subcases that has reference
				// to starting place
				//====================================
				WorkItem[] subWorkItems = _openSubCases(applicationName, caseVo, caseType, token, place, triggerContext, caseDao, log);
				allEnableWorkItemList.addAll(Arrays.asList(subWorkItems));
			}
			
			WorkItem[] enableWorkItems = allEnableWorkItemList.toArray(new WorkItem[allEnableWorkItemList.size()]);
			return enableWorkItems; 
			
		}
		
		
	}
	
	private static boolean _isAllChildrenCaseClosed(Long parentCaseId, CaseDao caseDao) {
		Integer howMany = caseDao.howManyChildrenCaseNotClosed(parentCaseId);
		if (howMany.intValue() == 0) {
			return true;
			
		}
		// there are childrens not finished
		return false;
			
	}
	/**
	 * Opens a new Case
	 * @param caseTypeRef the caseType
	 * @param triggerCtx
	 * @return An array of enabled WorkItems
	 * @throws CaseTypeNotExistException
	 */
	public WorkItem[] openCase(String caseTypeRef, CaseVo newCaseVo, TriggerContext triggerCtx) throws CaseTypeNotExistException, ActionExecutionException, ResourceAccessDeniedException  {		
		//=====================================
		// Find the CaseType and create a new Case
		//=====================================
		RootCaseType rootCaseType = this.caseTypeManager.getRootCaseTypeById(caseTypeRef);
		if (rootCaseType == null) {			
			CaseTypeNotExistException ex = new CaseTypeNotExistException (new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"Cannot find case " + caseTypeRef + " please check if it has been registered or included in the " + this.operonRegistryFilename);
			log.error(ex.getMessage(), ex);
			ex.setLogged();
			throw ex;
		}
		
		
		
		//=========================
		// Populate the New CaseVo 
		// and Create Case
		//=========================
		newCaseVo.setCaseStatus(Const.CASE_STATUS_open);
		newCaseVo.setCaseTypeRef(rootCaseType.getId());
		newCaseVo.setRootCaseTypeRef(rootCaseType.getId());
		newCaseVo.setLockVersion(new Long(0));
		if (null != rootCaseType.getTimeToLive()) {
			newCaseVo.setExpiryDate(new Date ( System.currentTimeMillis() + rootCaseType.getTimeToLive().getDurationInMilliSecs()) );		
			
		}
		newCaseVo.setUpdatedDate(new Date());
		newCaseVo.setCreatedDate(new Date());
		
		newCaseVo.setCaseId(this.caseDao.createCase(newCaseVo));
		newCaseVo.setRootParentCaseId(newCaseVo.getCaseId());
				
		if ( ( null != rootCaseType.getTimeToLive() ) && !rootCaseType.getTimeToLive().isExplicit()  ) {
			//==========================
			// Implict scheduler is used.
			// Set any implicit scheduler
			// references to check for
			// the expiry date
			//==========================
			Scheduler[] schedulers  = rootCaseType.getAllSchedulers();
			for (int i=0; i<schedulers.length; i++) {
	    		TimeToLiveSchedulerVo ttlSchedVo = new TimeToLiveSchedulerVo();
	    		ttlSchedVo.setSchedulerRef(schedulers[i].getId());
	    		ttlSchedVo.setCaseId(newCaseVo.getCaseId());
	    		ttlSchedVo.setCronExp(schedulers[i].getCronTriggerExpression());
	    		ttlSchedVo.setCreatedDate(new Date());		
	    		ttlSchedVo.setUpdatedDate(new Date());		
	    		

				this.caseDao.addTimeToLiveScheduler(ttlSchedVo);	    		
				
			}
			
		}
		
		//===============================
		// audit event
		//===============================
		AuditImpl._auditEvent(newCaseVo, null, triggerCtx.getCurrentResource(), Const.CASE_EVENT_new, Const.CASE_STATUS_open, Const.CASE_STATUS_open, null, caseDao);
		
		//====================================
		// Create a Token, Find the starting Place, 
		// and its reference places 
		// and put a token in the starting place
		//====================================
		//create token
		TokenVo token = new TokenVo();
		token.setLockVersion(new Long(0));
		token.setTokenStatus(Const.TOKEN_STATUS_free);
		token.setCreatedDate(new Date());		
		token.setUpdatedDate(new Date());		

		token.setTokenId(this.caseDao.createToken(token));
		
		//put token in starting place
		Place startPlace = CaseTypeManager.findSourcePlace(rootCaseType);
		
		//==============================
		// Check if TriggerResource has
		// access to open new Case first
		//===============================
		ResourceManager resourceManager = (ResourceManager) ClassCache.getInstance().getObject(rootCaseType.getResourceManagerInterface());		
		if (!resourceManager.isTriggerResourcePartOfResource(triggerCtx, startPlace.getResources())) {
			ResourceAccessDeniedException ex = new ResourceAccessDeniedException(new ErrorCode(
					Const.ERROR_CODE_access_denied),
					"Resource  " + triggerCtx.getCurrentResource().getId() + " is not allowed to start Case of Type " + rootCaseType.getId());
			throw ex;
			
		}
		
		TokenPlaceRefVo startPlaceTokenRef = new TokenPlaceRefVo();
		startPlaceTokenRef.setTokenId(token.getTokenId());
		startPlaceTokenRef.setCaseId(newCaseVo.getCaseId());
		startPlaceTokenRef.setPlaceRef(startPlace.getId());
		startPlaceTokenRef.setPlaceRefType(startPlace.getType());
		startPlaceTokenRef.setCreatedDate(new Date());		
		startPlaceTokenRef.setUpdatedDate(new Date());		
		
		this.caseDao.addTokenPlaceRef(startPlaceTokenRef);
				
		//====================================
		// Execute the PostCreateCaseAction class
		//====================================
		PostCreateCaseAction postCreateCaseAction = null;
		try {
			Class<?> clazz = Class.forName(startPlace.getPostCreateCaseAction());  
			postCreateCaseAction = (PostCreateCaseAction) clazz.newInstance();
			
		} catch (Exception e) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"Error while trying to load the PostCreateCaseAction class " + startPlace.getPostCreateCaseAction(), e);
			ex.setLogged();
			log.error(ex.getMessage());			
			throw ex;
			
		}
		
		postCreateCaseAction.execute(triggerCtx);
			
		//==================================
		// Find all Transitions from the place
		// enable associated
		// WorkItems
		//==================================
		List<WorkItem> allEnableWorkItemList = new ArrayList<WorkItem>();
		WorkItem[] newWorkItems = null;
		try {
			newWorkItems = _enableWorkItemsFromPlaceWithToken(this.applicationName, newCaseVo, rootCaseType, startPlace, this.caseDao, triggerCtx, token, this.log);
			
		} catch (TaskNotExistException e) {
			BaseSystemException ex = new OperonConfigException(e);
			ex.setLogged();
			log.error(ex.getMessage());			
			throw ex;
			
		} catch (ResourceNotExistException e) {
			BaseSystemException ex = new OperonConfigException(e);
			ex.setLogged();
			log.error(ex.getMessage());			
			throw ex;

		}
		
		allEnableWorkItemList.addAll(Arrays.asList(newWorkItems));
		
		//==========================
		// Check for subcases and enable
		// subcase workitems
		//==========================
		if ( ( null != rootCaseType.getAllSubCaseTypes() )
				&& ( rootCaseType.getAllSubCaseTypes().length != 0 ) ) {
			//====================================
			// Open any Subcases that has reference
			// to starting place
			//====================================
			
			WorkItem[] subWorkItems = null;
			try{
				subWorkItems= _openSubCases(this.applicationName, newCaseVo, rootCaseType, token, startPlace, triggerCtx, this.caseDao, this.log);
				
			} catch (TaskNotExistException e) {
				BaseSystemException ex = new OperonConfigException(e);
				ex.setLogged();
				log.error(ex.getMessage());			
				throw ex;
				
			} catch (ResourceNotExistException e) {
				BaseSystemException ex = new OperonConfigException(e);
				ex.setLogged();
				log.error(ex.getMessage());			
				throw ex;

			}

			allEnableWorkItemList.addAll(Arrays.asList(subWorkItems));
		}
		
		WorkItem[] enableWorkItems = allEnableWorkItemList.toArray(new WorkItem[allEnableWorkItemList.size()]);
		if(log.isDebugEnabled()) {
			log.debug("Put Token in Start Place " + startPlace.getName() + "(" + startPlace.getId() + ") has produced "  + enableWorkItems.length + " Enabled Transitions ");
		}
		
		return enableWorkItems; 
		
	}
	
	/**
	 * Open a list of subcases from the parent Place if the subcases Inref place matches the
	 * parent Place
	 * @param parentCase
	 * @param rootCaseType
	 * @param parentCaseType
	 * @param currentSubCaseTypes
	 * @param currentToken
	 * @param parentPlace
	 * @param triggerContext
	 * @return
	 */
	private static WorkItem[] _openSubCases(String applicationName, CaseVo parentCaseVo, CaseType parentCaseType, 
			TokenVo currentToken, 
			Place parentPlace, TriggerContext triggerContext, CaseDao caseDao, Logger log) throws TaskNotExistException, ResourceNotExistException {
		SubCaseType[] currentSubCaseTypes = parentCaseType.getAllSubCaseTypes();	
		List<WorkItem> newWorkItemList = new ArrayList<WorkItem>();
		for (int i=0; i< currentSubCaseTypes.length; i++) {
			WorkItem[] newWorkItems = _openSingleSubCase(applicationName, parentCaseVo, currentSubCaseTypes[i], currentToken, parentPlace, triggerContext, caseDao, log);
			if (newWorkItems.length != 0) {
				newWorkItemList.addAll(Arrays.asList(newWorkItems));
			}
			
		}
		
		return newWorkItemList.toArray(new WorkItem[newWorkItemList.size()]); 
		
	}
	
	/**
	 * Creates subcase from the current parent place and token passed
	 * @param rootCaseType
	 * @param parentCaseType
	 * @param currentCaseType
	 * @param currentToken
	 * @param parentPlace
	 * @return WorkItem[] - a list of workItems
	 */
	private static WorkItem[] _openSingleSubCase(String applicationName, CaseVo parentCaseVo, 
				SubCaseType currentCaseType, TokenVo currentToken, 
				Place parentPlace, TriggerContext triggerContext, CaseDao caseDao, Logger log) throws TaskNotExistException, ResourceNotExistException {
		
		Place[] inRefPlaces = CaseTypeManager.findInRefPlaces(parentPlace, currentCaseType);
		if (inRefPlaces.length == 0) {
			return new WorkItem[0];
			
		}
		
		//=========================
		// If got here We have a 
		// subcase for this parent place 
		//=========================
		
		
		CaseVo newCaseVo = new CaseVo();
		newCaseVo.setCaseStatus(Const.CASE_STATUS_open);
		newCaseVo.setCaseTypeRef(currentCaseType.getId());
		newCaseVo.setRootCaseTypeRef(currentCaseType.getRootCaseType().getId());		
		newCaseVo.setLockVersion(new Long(0));
		newCaseVo.setRootParentCaseId(parentCaseVo.getRootParentCaseId());
		newCaseVo.setParentCaseId(parentCaseVo.getCaseId());
		newCaseVo.setCreatedDate(new Date());
		newCaseVo.setUpdatedDate(new Date());

		newCaseVo.setCaseId(caseDao.createCase(newCaseVo));

		if(log.isDebugEnabled()) {
			log.debug("Put Token in Place " + parentPlace.getName() + "(" + parentPlace.getId() 
					+ ") has also triggered the subnet "  + newCaseVo.getCaseTypeRef() + "_" + newCaseVo.getCaseId());
		}
		
		//===============================
		// audit event
		//===============================
		AuditImpl._auditEvent(newCaseVo, null, triggerContext.getCurrentResource(), Const.CASE_EVENT_new, Const.CASE_STATUS_open, Const.CASE_STATUS_open, null, caseDao);
		
		
		//=======================================
		// Enable all WorkItems for this subcase
		//=======================================
		List<WorkItem> enableWorkItemList = new ArrayList<WorkItem>();
		for (int i=0; i<inRefPlaces.length; i++) {
			//put token in the other inRef places
			TokenPlaceRefVo inRefToken = new TokenPlaceRefVo();
			inRefToken.setTokenId(currentToken.getTokenId());
			inRefToken.setCaseId(newCaseVo.getCaseId());
			inRefToken.setPlaceRef(inRefPlaces[i].getId());
			inRefToken.setPlaceRefType(inRefPlaces[i].getType());
			inRefToken.setCreatedDate(new Date());
			inRefToken.setUpdatedDate(new Date());
			
			caseDao.addTokenPlaceRef(inRefToken);

			//====================================
			// Run the create subCaseAction classes
			//====================================
			
			
			
			WorkItem[] newWorkItems = _enableWorkItemsFromPlaceWithToken(applicationName, newCaseVo, currentCaseType, inRefPlaces[i], caseDao, triggerContext, currentToken, log);
			enableWorkItemList.addAll(Arrays.asList(newWorkItems));
			
		}
		
		
		return enableWorkItemList.toArray(new WorkItem[enableWorkItemList.size()]);
	}
	
	/**
	 * Enable a list of WorkItems from a place
	 * @param caseVo
	 * @param caseType
	 * @param aPlace
	 * @return
	 */
	private static WorkItem[] _enableWorkItemsFromPlaceWithToken(String applicationName, CaseVo caseVo, CaseType caseType, Place placeHasToken, CaseDao caseDao, TriggerContext triggerContext, TokenVo tokenVo, Logger log ) throws TaskNotExistException, ResourceNotExistException {
		//==================================
		// Find all Transitions from the place
		// created associated
		// WorkItems
		//==================================
		List<WorkItem> newWorkItemList = new ArrayList<WorkItem>();
		Transition[] transitions = CaseTypeManager.findAllTransitionsFromPlace(caseType, placeHasToken);
		for (int i=0; i<transitions.length; i++) {
			WorkItem workItem = _enableSingleWorkItemFromPlaceWithToken(applicationName, caseVo, caseType, transitions[i], caseDao, triggerContext, tokenVo, log);
			if (null != workItem) {
				newWorkItemList.add(workItem);	
				if(log.isDebugEnabled()) {
					log.debug("Put Token in Place "  + placeHasToken.getName() + "("+ placeHasToken.getId() + ") has enabled Transition "  + transitions[i].getName() + "(" + transitions[i].getId()+ "_" +  workItem.getTaskVo().getTaskId() + ")");
					
				}
				
			}
		}
		
		return newWorkItemList.toArray(new WorkItem[newWorkItemList.size()]);
	}
	
	/**
	 * Enables an WorkItem from an Transition
	 * @param caseVo
	 * @param caseType
	 * @param transition
	 * @param caseDao
	 * @return WorkItem the enabled Transition otherwise null
	 */
	private static WorkItem _enableSingleWorkItemFromPlaceWithToken(String applicationName, CaseVo caseVo, CaseType caseType, Transition transition, CaseDao caseDao, TriggerContext triggerContext, TokenVo tokenVo, Logger log ) throws TaskNotExistException, ResourceNotExistException {
	
		if (Const.TRANSITION_TYPE_ignore.equals(transition.getTransitionType())) {
			//======================================
			// If its an ignore transition for some 
			// reason i.e. only used as a place holder
			// then always return null
			//======================================
			return null;
		}
		
		if (Const.TRANSITION_TYPE_AND_join.equals(transition.getTransitionType())) {
			//=================================
			// If Transition is an AND-Join
			// Make sure that all Input Places
			// have token before proceeding.
			// other wise return null
			//=================================
			Place[] places = CaseTypeManager.findAllPlacesIntoTransition(caseType, transition);
			TokenPlaceRefVo[] tokenPlacesRefVos = caseDao.findTokenPlaceRefVosByCaseId(caseVo.getCaseId(), Const.TOKEN_STATUS_free);
			if (!_hasPlacesGotToken(places, tokenPlacesRefVos)) {
				// Transition not ready to be enable.
				// exit
				return null;
			}
		}
		
		//=========================================
		// If we get here we can enable the WorkItem
		//==========================================
		TaskVo taskVo = new TaskVo();
		
		taskVo.setCaseVo(caseVo);
		taskVo.setLockVersion(new Long(0));
		taskVo.setRetryCount(new Long(0));
		taskVo.setWfnetTransitionRef(transition.getId());
		taskVo.setTaskStatus(Const.TRANSITION_STATUS_enabled);
		taskVo.setCreatedDate(new Date());
		taskVo.setUpdatedDate(new Date());
		taskVo.setPriorityWeighting(transition.getPriorityWeighting());
		taskVo.setExpectedCompletionDate(new Date (System.currentTimeMillis() + transition.getEstimatedCompletionTime().getDurationInMilliSecs()));
		
		//---------------
		//This is used for Automatic and Time Triggered 
		//Transitions.
		//----------------
		if (Const.TRIGGER_TYPE_auto.equals(transition.getTriggerType())) {
			long timeout = System.currentTimeMillis() + transition.getExecutionTimeLimit().getDurationInMilliSecs();
			taskVo.setInProgressTimeout(new Date(timeout));
			taskVo.setStartAtStartup(Boolean.TRUE);
			
		}
		
		
		
		if (Const.TRIGGER_TYPE_time.equals(transition.getTriggerType())) {
			long triggerDelayTime = System.currentTimeMillis() + transition.getTriggerDelayDuration().getDurationInMilliSecs(); 
			taskVo.setTriggerTime(new Date(triggerDelayTime));	
			
			long timeout = triggerDelayTime + transition.getExecutionTimeLimit().getDurationInMilliSecs();
			taskVo.setInProgressTimeout(new Date(timeout));
			taskVo.setStartAtStartup(Boolean.TRUE);			
			
		}
		
		taskVo.setTaskId(caseDao.createTask(taskVo));
		caseDao.addTokenEnabledTask(tokenVo, taskVo);
		
		if (Const.TRIGGER_TYPE_manual.equals(transition.getTriggerType()) || Const.TRIGGER_TYPE_message.equals(transition.getTriggerType())) {
			//===============================
			// For manual or message triggers
			// Assign the resources to the
			// WorkItem
			//===============================
			RootCaseType rootCaseType = null;
			if (caseType instanceof RootCaseType) {
				rootCaseType = (RootCaseType) caseType;
			} else {
				rootCaseType = ((SubCaseType) caseType).getRootCaseType();
			}
			ResourceManager resourceMgr = (ResourceManager) ClassCache.getInstance().getObject(rootCaseType.getResourceManagerInterface());				
			String[] resources = transition.getResources();
			for (int i=0; i<resources.length; i++) {
				resourceMgr.assignResourceToWorkItem(resources[i], taskVo.getTaskId());
			}
		}
		
		
		//===============================
		// audit event
		//===============================
		AuditImpl._auditEvent(null, taskVo, triggerContext.getCurrentResource(), Const.TASK_EVENT_new, Const.TASK_STATUS_enabled, Const.TASK_STATUS_enabled, null, caseDao);
		
		
		//--------------------------------
		// Check if the Time Trigger delay
		// is an implicit one, if it is
		// add the scheduler reference
		//------------------------------
		
		if (Const.TRIGGER_TYPE_time.equals(transition.getTriggerType())
				&& !transition.getTriggerDelayDuration().isExplicit() ) {
			
			//------------------------------------
			// If get here its an implicit scheduler
			// add the scheduler reference
			//---------------------------------------
			TimeTriggerSchedulerVo[] timeTrigSchedVos = _createTimeTriggerSchedulers(taskVo, transition.getTriggerDelayDuration().getSchedulerToUseRefs());
			caseDao.addTimeTriggerSchedulers(timeTrigSchedVos);
			
			if(log.isDebugEnabled() && !transition.getTriggerDelayDuration().isExplicit()) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
				log.debug("Implicit Timer Transition " + transition.getName() + "(" + transition.getId() + "_"  + taskVo.getTaskId() + ") is set to start after (yyyy.MM.dd)" + simpleDateFormat.format(taskVo.getTriggerTime()) + ". This is not the exact time since we are relying on an implicit timer to start this.");
			}
			
		} 

		WorkItem workItem= new WorkItem(applicationName, taskVo, caseType);
		
		return workItem;
		
	}
	private static TimeTriggerSchedulerVo[] _createTimeTriggerSchedulers(TaskVo workItem, Scheduler[] schedToUse) {
		List<TimeTriggerSchedulerVo> newTimeTriggerSchedList = new ArrayList<TimeTriggerSchedulerVo>(); 
		for (int i=0; i<schedToUse.length; i++) {
			TimeTriggerSchedulerVo timeTriggSchedVo = new TimeTriggerSchedulerVo();
			timeTriggSchedVo.setCronExp(schedToUse[i].getCronTriggerExpression());
			timeTriggSchedVo.setSchedulerRef(schedToUse[i].getId());
			timeTriggSchedVo.setTaskId(workItem.getTaskId());
			newTimeTriggerSchedList.add(timeTriggSchedVo);
			
		}
		
		return newTimeTriggerSchedList.toArray(new TimeTriggerSchedulerVo[newTimeTriggerSchedList.size()]);
	}
	
	/**
	 * 
	 * Fires tokens at WorkItems
	 * 
	 * <p><b>Note:</b> The Message and Manual Triggered WorkItems should not call this 
	 * method otherwise a runtime exception is thrown.</p>
	 * 
	 */
	public Activity[] fireTokensAtWorkItems(WorkItem[] workItems, TriggerContext triggerContext) throws ResourceAccessDeniedException {
		List<Activity> activityList = new ArrayList<Activity>();
		for (int i=0; i<workItems.length; i++) {
			Activity activity = _fireTokensAtWorkItem(this.applicationName, workItems[i], triggerContext, this.caseDao, this.log);
			if (activity != null) {
				activityList.add(activity);
				
			}
		}
		
		return activityList.toArray( new Activity[activityList.size()]);
	}
	
	/**
	 * Fires triggers for manual triggered WorkItems
	 */
	public Activity fireTokensAtWorkItem(WorkItem workItem, TriggerContext triggerContext) throws ResourceAccessDeniedException {
		return _fireTokensAtWorkItem(this.applicationName, workItem, triggerContext, this.caseDao, this.log);
	}
	
	/**
	 * <p>Fires tokens at the Transition assoicated with the WorkItem</p>
	 * 	<ul>
	 * 		<li>Find all FREE Tokens that are associated with the WorkItem, LOCK all the Tokens</li>
	 * 			<ul>
	 * 				<li>For each Token make all other WorkItems that was ENABLED by the Token REDUNDANT</li>
	 * 				<li>Update Token to LOCKED and WorkItem to IN_PROGRESS and return WorkItem as an Activity</li>
	 * 			</ul>
	 * 		<li>If no Token is found i.e. another WorkItem has already consumed it then Make the WorkItem REDUNDANT</li>
	 * 	</ul>
	 * </ul> 
	 * @param workItem the enable transition
	 * @param triggerContext  the triggerContext
	 * @return Activity - the WorkItem that IN_PROGRESS. Null if for some reason there are not free Tokens associated with this WorkItem.
	 */
	private static Activity _fireTokensAtWorkItem(String applicationName, WorkItem workItem, TriggerContext triggerContext, CaseDao caseDao, Logger log) throws ResourceAccessDeniedException {
		TaskVo taskVo = workItem.getTaskVo();
		CaseType caseType = workItem.getCaseType();
		Transition transition = caseType.findTransitionById(taskVo.getWfnetTransitionRef());
		if (transition == null) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Transition " + taskVo.getWfnetTransitionRef() + " is not in the  net " + caseType.getId()
							+ " Please check the net files again or it could also be someone has been tampering with the database operon_workitem table");
			throw ex;
			
		}
		
		
		RootCaseType rootCaseType = null;
		
		if (caseType instanceof RootCaseType) {
			rootCaseType = (RootCaseType) caseType;
		} else {
			rootCaseType = ((SubCaseType) caseType).getRootCaseType();
		}
						
		if (log.isDebugEnabled()) {
			log.debug("Firing Token at Transition " + taskVo.getWfnetTransitionRef() + "_" + taskVo.getTaskId());
			
		}
		
		//======================
		// authorisation checks
		//======================
		if (Const.TRIGGER_TYPE_manual.equals(transition.getTriggerType())) {
			ResourceManager resourceManager = (ResourceManager) ClassCache.getInstance().getObject(rootCaseType.getResourceManagerInterface());		
			if (!resourceManager.isTriggerResourcePartOfResource(triggerContext, transition.getResources())) {
				ResourceAccessDeniedException ex = new ResourceAccessDeniedException(new ErrorCode(
						Const.ERROR_CODE_access_denied),
						"Resource  " + triggerContext.getCurrentResource().getId() + " is not allowed to start Case of Type " + rootCaseType.getId());
				throw ex;
				
			}
			
		} else {
			//===============================
			// System resource required
			//===============================
			WorkflowCoreEngineImpl._checkIfOnlySystemResourceAllowedToTrigger(triggerContext.getCurrentResource(), taskVo.getWfnetTransitionRef(), caseType);
			
		}
			
		
		//=============================
		// If we get here we have passed
		// authorisation checks
		//==============================
		TokenVo[] tokens = caseDao.findFreeTokensThatEnabledTask(taskVo);
		if (tokens.length == 0) {
			// some other WorkItem must have locked
			// the token
			return null;
		}
		
		//==================================
		// For each Token make all the other
		// Workitems associated with the Token
		// REDUNDANT excluding the current one. 
		//
		// Update each Token to LOCKED
		//==================================
		for (int i=0; i< tokens.length; i++) {
			_makeWorkItemsRedundant(tokens[i], taskVo, triggerContext, caseDao, log);
			Long currentTokenLockVersion = tokens[i].getLockVersion();
			
			tokens[i].setLockByTaskId(taskVo.getTaskId());
			tokens[i].setLockVersion(new Long(currentTokenLockVersion.longValue() + 1));
			tokens[i].setTokenStatus(Const.TOKEN_STATUS_locked);
			tokens[i].setUpdatedDate(new Date());
			caseDao.updateToken(tokens[i], currentTokenLockVersion);
		}
		
		//=================================
		// Update WorkItem to IN_PROGRESS
		//=================================
		_updateWorkItemStatus(taskVo, triggerContext, Const.TASK_EVENT_fire, Const.TASK_STATUS_in_progress, null, caseDao);
		
		return new Activity(applicationName, taskVo, workItem.getCaseType());
			
		
		
	}

	/**
	 * <p>Fires tokens at the Transition assoicated with the WorkItem.</p>
	 * <p>This method is only used for Message and Manual Triggers because with they Trigger transacion
	 * 	starts from the moment we fire the Token all the way to the Activity finishes.
	 * </p>
	 * 	<ul>
	 * 		<li>Find all FREE Tokens that are associated with the WorkItem, LOCK all the Tokens</li>
	 * 			<ul>
	 * 				<li>For each Token make all other WorkItems that was ENABLED by the Token REDUNDANT</li>
	 * 				<li>Update Token to LOCKED and WorkItem to IN_PROGRESS and return WorkItem as an Activity</li>
	 * 			</ul>
	 * 		<li>If no Token is found i.e. another WorkItem has already consumed it then Make the WorkItem REDUNDANT</li>
	 * 	</ul>
	 * </ul> 
	 * @param workItem the enable transition
	 * @param triggerContext  the triggerContext
	 * @return Activity - the WorkItem that IN_PROGRESS. Null if for some reason there are not free Tokens associated with this WorkItem.
	 */
	public WorkItem[] fireTokensAtWorkItemForMessageTrigger(WorkItem workItem, TriggerContext triggerContext) throws ResourceAccessDeniedException, ActionExecutionException {
		TaskVo taskVo = workItem.getTaskVo();
		CaseType caseType = workItem.getCaseType();
		Transition transition = caseType.findTransitionById(taskVo.getWfnetTransitionRef());
		RootCaseType rootCaseType = null;
		
		if (caseType instanceof RootCaseType) {
			rootCaseType = (RootCaseType) caseType;
		} else {
			rootCaseType = ((SubCaseType) caseType).getRootCaseType();
		}
	
		if (log.isDebugEnabled()) {
			log.debug("Firing Token at Transition " + taskVo.getWfnetTransitionRef() + "_" + taskVo.getTaskId());
			
		}
	
		//======================
		// authorisation checks
		//======================
		ResourceManager resourceManager = (ResourceManager) ClassCache.getInstance().getObject(rootCaseType.getResourceManagerInterface());		
		if (!resourceManager.isTriggerResourcePartOfResource(triggerContext, transition.getResources())) {
			ResourceAccessDeniedException ex = new ResourceAccessDeniedException(new ErrorCode(
					Const.ERROR_CODE_access_denied),
					"Resource  " + triggerContext.getCurrentResource().getId() + " is not allowed to start Case of Type " + rootCaseType.getId());
			throw ex;
			
		}
		
		//=============================
		// If we get here we have passed
		// authorisation checks
		//==============================
		TokenVo[] tokens = this.caseDao.findFreeTokensThatEnabledTask(taskVo);
		if (tokens.length == 0) {
			// something other WorkItem must have locked
			// the token
			return null;
		}
		
		//==================================
		// For each Token make all the other
		// Workitems associated with the Token
		// REDUNDANT excluding the current one. 
		//
		// Update each Token to LOCKED
		//==================================
		for (int i=0; i< tokens.length; i++) {
			_makeWorkItemsRedundant(tokens[i], taskVo, triggerContext, this.caseDao, this.log);
			Long currentTokenLockVersion = tokens[i].getLockVersion();
			
			tokens[i].setLockByTaskId(taskVo.getTaskId());
			tokens[i].setLockVersion(new Long(currentTokenLockVersion.longValue() + 1));
			tokens[i].setTokenStatus(Const.TOKEN_STATUS_locked);
			tokens[i].setUpdatedDate(new Date());
			this.caseDao.updateToken(tokens[i], currentTokenLockVersion);
		}
		
		//=================================
		// Update WorkItem to IN_PROGRESS
		//=================================
		_updateWorkItemStatus(taskVo, triggerContext, Const.TASK_EVENT_fire, Const.TASK_STATUS_in_progress, null, this.caseDao);
		
		Activity activity = new Activity(this.applicationName, taskVo, workItem.getCaseType());
		return this.finishActivity(activity, triggerContext);
								
		
		
	}
	
	/**
	 * Finds all WorkItems associated with the token and makes them REDUNDANT excluding the one passed.
	 * 
	 * @param tokenId The tokenId
	 * @param excludeWorkItemVo exclude this workItem i.e. do not make it redundant
	 * @param caseDao
	 */
	private static void _makeWorkItemsRedundant(TokenVo tokenVo, TaskVo excludeWorkItemVo, TriggerContext triggerContext, CaseDao caseDao, Logger log) {
		TaskVo[] taskVos = caseDao.findEnabledTasksByTokenId(tokenVo.getTokenId());
		for (int i=0; i< taskVos.length; i++) {
			if (excludeWorkItemVo.getTaskId().longValue() == taskVos[i].getTaskId().longValue() ) {
				// do nothing continue loop
				continue;
			}
			
			// if get here make the WorkItem REDUNDANT
			_updateWorkItemStatus(taskVos[i], triggerContext, Const.TASK_EVENT_OR_cancel, Const.TASK_STATUS_redundant, null, caseDao);
			if (log.isDebugEnabled()) {
				log.debug("Cancelled Enabled Transition " + taskVos[i].getWfnetTransitionRef() + "_" + taskVos[i].getTaskId() );
				
			}
			
		}
	}

	
	/**
	 * Updates the status and audit the event
	 * @param taskVo
	 * @param newStatus
	 * @param event
	 * @param iBaseException
	 * @param caseDao
	 */
	private static void _updateWorkItemStatus (TaskVo taskVo, TriggerContext triggerContext, String event, String newStatus, IBaseException iBaseException, CaseDao caseDao) {
		Long currentLockVersion = taskVo.getLockVersion();
		String currentStatus = taskVo.getTaskStatus();
		
		taskVo.setLockVersion(new Long(currentLockVersion.longValue() + 1));
		taskVo.setUpdatedDate(new Date());
		taskVo.setTaskStatus(newStatus);
		if (newStatus.equalsIgnoreCase(Const.TASK_STATUS_finished)) {
			taskVo.setActualCompletionDate(new Date());
			
		}
		caseDao.updateTaskStatus(taskVo, currentLockVersion);
		
		//=====================
		// audit event
		//====================
		AuditImpl._auditEvent(null, taskVo, triggerContext.getCurrentResource(), event, currentStatus, newStatus, iBaseException, caseDao);
		
		
	}

	/**
	 * Updates the status and audit the event
	 * @param taskVo
	 * @param newStatus
	 * @param event
	 * @param iBaseException
	 * @param caseDao
	 */
	private static void _updateCaseStatus (CaseVo caseVo, TriggerContext triggerContext, String event, String newStatus, IBaseException iBaseException, CaseDao caseDao) {
		Long currentLockVersion = caseVo.getLockVersion();
		String currentStatus = caseVo.getCaseStatus();
		
		caseVo.setLockVersion(new Long(currentLockVersion.longValue() + 1));
		caseVo.setUpdatedDate(new Date());
		caseVo.setCaseStatus(newStatus);
		caseDao.updateCaseStatus(caseVo, currentLockVersion);
		
		//=====================
		// audit event
		//====================
		AuditImpl._auditEvent(caseVo, null, triggerContext.getCurrentResource(), event, currentStatus, newStatus, iBaseException, caseDao);
		
		
	}
	
	/**
	 * Checks if the each of the <code>Places</code> passed contains at least one <code>Token</code> 
	 * 
	 * @param places
	 * @param tokenPlaceRefVos
	 * @return true if all Places have at least one Token otherwise false
	 */
	private static boolean _hasPlacesGotToken(Place[] places, TokenPlaceRefVo[] tokenPlaceRefVos) {
		for (int i=0; i<places.length; i++) {
			if (!_hasPlaceGotToken(places[i], tokenPlaceRefVos)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Checks if a place has a Token
	 * @param place
	 * @param tokenPlaceRefVos
	 * @return
	 */
	private static boolean _hasPlaceGotToken (Place place, TokenPlaceRefVo[] tokenPlaceRefVos) {
		for (int i=0; i<tokenPlaceRefVos.length; i++) {
			if (place.getId().equalsIgnoreCase(tokenPlaceRefVos[i].getPlaceRef())) {
				return true;
			}
		}
		
		return false;
	}

	public static Resource _getAutoTriggerResource(CaseType caseType) {
		RootCaseType rootCaseType = null;
		if (caseType instanceof RootCaseType) {
			rootCaseType =(RootCaseType) caseType;
			
		} else {
			
			rootCaseType = ((SubCaseType) caseType).getRootCaseType();
		}
		
		ResourceManager resourceManager = (ResourceManager) ClassCache.getInstance().getObject(rootCaseType.getResourceManagerInterface());		
		return resourceManager.getDefaultResourceForAutoTrigger();
		
	}
	
	/**
	 * Uses quartz Scheduler to execute the Job straight away. 
	 * @param activity
	 */
	public static void _autoTriggerFinishActivityUsingQuartzScheduler(Activity activity) {
		org.quartz.Scheduler sched = Operon.getInstance().getScheduler();
		Resource autoTrigResource = _getAutoTriggerResource(activity.getCaseType());
		
		TriggerContext triggCtxt = new TriggerContext();
		triggCtxt.setCurrentResource(autoTrigResource);
		
		String id="Activity_" + activity.getTaskVo().getTaskId();
		String groupName = Const.QUARTZ_GROUP_operon + "_" +  activity.getApplicationName();
		
		JobDetail jobDetail = new JobDetail(id, groupName,FinishActivityJob.class);
		jobDetail.getJobDataMap().put(Const.QUARTZ_JOBDATAMAP_KEY_activity, activity);
		jobDetail.getJobDataMap().put(Const.QUARTZ_JOBDATAMAP_KEY_triggerContext, triggCtxt);
		jobDetail.setDurability(false);
		

		Trigger trigger = new SimpleTrigger(id, groupName);
		
		try {
			
			//==========================
			// We can pass this to a
			// MDB to do the processing here
			//==========================
			sched.scheduleJob(jobDetail, trigger);

			
		} catch (SchedulerException e) {
			BaseSystemException ex = new BaseSystemException(new ErrorCode(
					Const.ERROR_CODE_quartz_registerjob_error),
					"Cannot register job " + jobDetail.getName() + "with the default Scheduler", e );
			throw ex;
			
		}
	}
	
	/**
	 * <p>Same as {@link #_autoTriggerFinishActivityUsingQuartzScheduler()}}</p>
	 * This uses JMS to Finish off the activity.
	 * @param activity
	 */
	private static void _autoTriggerFinishActivityUsingJMS(Activity activity) {
		//TODO
		//put into JMS queue
	}
	
	public static void _setStartWorkItemTimerUsingQuartzScheduler(WorkItem workItem, String applicationName, Logger log) {
		org.quartz.Scheduler sched = Operon.getInstance().getScheduler();
		
		TriggerContext timerTriggerCtx = new TriggerContext();
		Resource timerTriggerResource = _getAutoTriggerResource(workItem.getCaseType());
		timerTriggerCtx.setCurrentResource(timerTriggerResource);

		String id= "WorkItem_"  + workItem.getTaskVo().getTaskId();
		String groupName = Const.QUARTZ_GROUP_operon + "_" + applicationName ;
		
		
		
		JobDetail jobDetail = new JobDetail(id, groupName, StartWorkItemJob.class);
		jobDetail.getJobDataMap().put(Const.QUARTZ_JOBDATAMAP_KEY_workItem, workItem);
		jobDetail.getJobDataMap().put(Const.QUARTZ_JOBDATAMAP_KEY_triggerContext, timerTriggerCtx);
		jobDetail.getJobDataMap().put(Const.QUARTZ_JOBDATAMAP_KEY_applicationName, applicationName);
		jobDetail.setDurability(false);

		Trigger trigger = null;
		Date startTime = workItem.getTaskVo().getTriggerTime();
		
		if (startTime.after(new Date())) {
			trigger = new SimpleTrigger(id, groupName, startTime);
			
		} else {
			trigger = new SimpleTrigger(id, groupName);
			
		}
		
		
		try {
			sched.scheduleJob(jobDetail, trigger);
			if(log.isDebugEnabled()) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
				log.debug("Timer Transition " + workItem.getTaskVo().getWfnetTransitionRef() + "_"  + workItem.getTaskVo().getTaskId() + " will be triggered on (yyyy.MM.dd)" + simpleDateFormat.format(startTime));
			}

		} catch (ObjectAlreadyExistsException e) {
			log.warn("Operon !!!!!!!!!!!!!!! Cannnot register job:  " + e.getMessage() + " !!!!!!!!!!!!!!!!!!!!!");
			return;
			
			
		} catch (SchedulerException e) {
			BaseSystemException ex = new BaseSystemException(new ErrorCode(
					Const.ERROR_CODE_quartz_registerjob_error),
					"Cannot register job " + jobDetail.getName() + "with the default Scheduler", e );
			throw ex;
			
		}
		
	}
	
	public static void _kickOffAutoOrTimeTriggeredWorkItems(WorkItem[] workItems, String applicationName, Logger log) throws ResourceAccessDeniedException {
		//======================================
		// Check if Case is open
		// if it is not then exit
		//======================================		
		if (workItems.length == 0) {
			return;
			
		} 
		
		CaseDao caseDao = (CaseDao) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_caseDao);
		CaseVo rootCaseVo = caseDao.getCaseById(workItems[0].getTaskVo().getCaseVo().getRootParentCaseId());
		if ( !Const.CASE_STATUS_open.equalsIgnoreCase(rootCaseVo.getCaseStatus()) ) {
			//This Case is not Open.
			//do nothing
			return;
		}
		
		//=====================================
		// Filter WorkItems into trigger 
		// types of Timer and Auto
		//=======================================
		List<WorkItem> timerList = new ArrayList<WorkItem>();
		List<WorkItem> autoList = new ArrayList<WorkItem>();
		for (int i=0; i<workItems.length; i++) {
			if (CaseTypeManager.isTransitionAutoTrigger(workItems[i].getTaskVo().getWfnetTransitionRef(), workItems[i].getCaseType())) {
				autoList.add(workItems[i]);
			} 
			
			if (CaseTypeManager.isTransitionExplicitTimeTrigger(workItems[i].getTaskVo().getWfnetTransitionRef(), workItems[i].getCaseType())) {
				timerList.add(workItems[i]);
			} 
			
		}
		WorkItem[] timerWorkItems = timerList.toArray(new WorkItem[timerList.size()]);  
		WorkItem[] autoWorkItems = autoList.toArray(new WorkItem[autoList.size()]);  
		
		//========================================
		// Set timer tasks for explicit 
		// timer triggered
		// workitem
		//========================================
		for (int i=0; i< timerWorkItems.length; i++) {
			WorkflowCoreEngineImpl._setStartWorkItemTimerUsingQuartzScheduler(timerWorkItems[i], applicationName, log);
		}
		
		
		//==========================================
		// Execute automatic Triggered workitems
		//==========================================
		_startWorkItemsAndAutoTriggerFinishActivities(autoWorkItems, applicationName);
		
		
		
	}
	
	public static void _startWorkItemsAndAutoTriggerFinishActivities(WorkItem[] workItems, String applicationName) throws ResourceAccessDeniedException {
		if (workItems.length != 0) {
			TriggerContext autoTriggerCtx = new TriggerContext();
			Resource autTriggerResource = WorkflowCoreEngineImpl._getAutoTriggerResource(workItems[0].getCaseType());
			autoTriggerCtx.setCurrentResource(autTriggerResource);
			
			WorkflowCoreEngine workflowCoreEngine = (WorkflowCoreEngine) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngine);
				
			
			Activity[] autoActivities = workflowCoreEngine.fireTokensAtWorkItems(workItems, autoTriggerCtx);
			for (int i=0; i<autoActivities.length; i++) {
				//TODO
				//check if JMS is chosen otherwise use default
				WorkflowCoreEngineImpl._autoTriggerFinishActivityUsingQuartzScheduler(autoActivities[i]);
			}
			
		}
		
	}
	
	public static void _checkIfOnlySystemResourceAllowedToTrigger(Resource resource, String transitionId, CaseType caseType) throws ResourceAccessDeniedException{
		Transition transition = caseType.findTransitionById(transitionId);
		if ( Const.TRIGGER_TYPE_auto.equals(transition.getTriggerType()) 
				|| Const.TRIGGER_TYPE_time.equals(transition.getTriggerType())) {
			//=====================================
			// Resource must equal system resource
			//=====================================
			String resourceMgrName = null;
			if (caseType instanceof SubCaseType) {
				resourceMgrName = ((SubCaseType) caseType).getRootCaseType().getResourceManagerInterface();
				
			} else {
				resourceMgrName = ((RootCaseType) caseType).getResourceManagerInterface();
			}
			
			
			ResourceManager resourceMgr = (ResourceManager) ClassCache.getInstance().getObject(resourceMgrName);
			
			if (!resource.getId().equals(resourceMgr.getDefaultResourceForAutoTrigger().getId())) {
				//resource is not System
				ResourceAccessDeniedException ex = new ResourceAccessDeniedException(new ErrorCode(
						Const.ERROR_CODE_access_denied),
						"The Transition " + transitionId + " is an automatic Trigger and can only be triggered by the System Resources only. Resource "  + resource.getId() + " is not allowed to trigger this");
				throw ex;
				
			}
		}
		
	}
	
	public  Case _getCaseById(Long caseId) throws CaseTypeNotExistException {
		
		CaseVo caseVo = this.caseDao.getCaseById(caseId);
		
		return _createCaseFromVo(this.applicationName, caseVo, this.caseTypeManager, this.operonRegistryFilename);
		
		
		
	}
	
	
	public Case[] _findOpenCasesByType(String caseType) throws CaseTypeNotExistException {
		throw new UnsupportedOperationException("Operation not yet supported");
	}
	
	public Case[] _getAllOpenCases() throws CaseTypeNotExistException {
		throw new UnsupportedOperationException("Operation not yet supported");
	}
	
	private static Case _createCaseFromVo (String applicationName, CaseVo caseVo, CaseTypeManager caseTypeManager, String operonRegistryFilename )  throws CaseTypeNotExistException{
		//==================================
		// Need to find the CaseType from the
		// RootCaseType if this Case is not 
		// a root Case.
		//==================================
		RootCaseType rootCaseType = caseTypeManager.getRootCaseTypeById(caseVo.getRootCaseTypeRef());
		if (rootCaseType == null) {			
			CaseTypeNotExistException ex = new CaseTypeNotExistException (new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"Cannot find root CaseType " + caseVo.getCaseTypeRef() + " please check if it has been registered or included in the " + operonRegistryFilename);
			throw ex;
		}
		
		if (caseVo.getCaseId().longValue() == caseVo.getRootParentCaseId().longValue()) {
			//=====================
			// Already root Case
			//===================			
			return new Case(applicationName, caseVo, rootCaseType);

		} 
		
		//=======================
		// If get here this is a subcase 
		// get the CaseType from the  root
		//========================================
		CaseType subCaseType = rootCaseType.findSubCaseTypeById(caseVo.getCaseTypeRef());
		return new Case(applicationName, caseVo, subCaseType);
			
		
		
	}
	
	public void _restartActivitiesAtStartup() {
		TaskVo[] taskVos = this.caseDao.findActivitiesForStartup();

		log.info("Operon>>>>>>>>>>>>>>>>> There are  " + taskVos.length + " Activites that did not finish, will restart the Activities now if any......" + " <<<<<<<<<<<<<<<<<<<<<<<<");
		
		List<Activity> activityList = new ArrayList<Activity>();
		for (int i=0; i<taskVos.length; i++) {
			log.info("Operon>>>>>>>>>>>>>>>>> Restarting Activity  " + taskVos[i].getWfnetTransitionRef() + "_" + taskVos[i].getTaskId() + " <<<<<<<<<<<<<<<<<<<<<<<<");
			
			CaseVo caseVo = taskVos[i].getCaseVo();
			RootCaseType rootCaseType = this.caseTypeManager.getRootCaseTypeById(caseVo.getRootCaseTypeRef());
			if (caseVo.getCaseTypeRef().equals(caseVo.getRootCaseTypeRef())) {
				activityList.add(new Activity(this.applicationName, taskVos[i], rootCaseType));
			} else {
				CaseType caseType = rootCaseType.findSubCaseTypeById(caseVo.getCaseTypeRef());
				activityList.add(new Activity(this.applicationName, taskVos[i], caseType));
				
			}
		}
		
		Activity[] activities = activityList.toArray(new Activity[activityList.size()]);
		for (int i=0; i<activities.length; i++) {
			WorkflowCoreEngineImpl._autoTriggerFinishActivityUsingQuartzScheduler(activities[i]);
		}

		
	}
	
	public void _restartAutoWorkitemsAtStartup() throws ResourceAccessDeniedException {
		TaskVo[] taskVos = this.caseDao.findAutoWorkItemsForStartup();
		
		log.info("Operon>>>>>>>>>>>>>>>>> There are  " + taskVos.length + " Automatic Triggered WorkItems that did not start, will restart the WorkItems now if any......" + " <<<<<<<<<<<<<<<<<<<<<<<<");
		
		List<WorkItem> workItemList = new ArrayList<WorkItem>();
		for (int i=0; i<taskVos.length; i++) {
			log.info("Operon>>>>>>>>>>>>>>>>> Restarting Auto Triggered WorkItem  " + taskVos[i].getWfnetTransitionRef() + "_" + taskVos[i].getTaskId() + " <<<<<<<<<<<<<<<<<<<<<<<<");
			
			CaseVo caseVo = taskVos[i].getCaseVo();
			RootCaseType rootCaseType = this.caseTypeManager.getRootCaseTypeById(caseVo.getRootCaseTypeRef());
			if (caseVo.getCaseTypeRef().equals(caseVo.getRootCaseTypeRef())) {
				workItemList.add(new WorkItem(this.applicationName, taskVos[i], rootCaseType));
			} else {
				CaseType caseType = rootCaseType.findSubCaseTypeById(caseVo.getCaseTypeRef());
				workItemList.add(new WorkItem(this.applicationName, taskVos[i], caseType));
				
			}
		}
		
		WorkItem[] workItems = workItemList.toArray(new WorkItem[workItemList.size()]);
		WorkflowCoreEngineImpl._kickOffAutoOrTimeTriggeredWorkItems(workItems, this.applicationName, this.log);
	
		
	}

	public void _restartExpiredTimedWorkitemsAtStartup() throws ResourceAccessDeniedException {
		TaskVo[] taskVos = this.caseDao.findOverdueTimedWorkItemsForStartup();
		
		log.info("Operon>>>>>>>>>>>>>>>>> There are  " + taskVos.length + " Timed Expired WorkItems that should have been started, will restart the WorkItems now if any......" + " <<<<<<<<<<<<<<<<<<<<<<<<");
		
		List<WorkItem> workItemList = new ArrayList<WorkItem>();
		for (int i=0; i<taskVos.length; i++) {
			log.info("Operon>>>>>>>>>>>>>>>>> Restarting Due Triggered Time WorkItem  " + taskVos[i].getWfnetTransitionRef() + "_" + taskVos[i].getTaskId() + " <<<<<<<<<<<<<<<<<<<<<<<<");
			
			CaseVo caseVo = taskVos[i].getCaseVo();
			RootCaseType rootCaseType = this.caseTypeManager.getRootCaseTypeById(caseVo.getRootCaseTypeRef());
			if (caseVo.getCaseTypeRef().equals(caseVo.getRootCaseTypeRef())) {
				workItemList.add(new WorkItem(this.applicationName, taskVos[i], rootCaseType));
			} else {
				CaseType caseType = rootCaseType.findSubCaseTypeById(caseVo.getCaseTypeRef());
				workItemList.add(new WorkItem(this.applicationName, taskVos[i], caseType));
				
			}
		}
		
		WorkItem[] workItems = workItemList.toArray(new WorkItem[workItemList.size()]);
		WorkflowCoreEngineImpl._startWorkItemsAndAutoTriggerFinishActivities(workItems, this.applicationName);
	
		
	}

	public void _reScheduledNotOverdueExplicitTimedWorkItemsAtStartup() throws ResourceAccessDeniedException {
		TaskVo[] taskVos = this.caseDao.findNotOverdueExplicitTimedWorkItemsForStartup();
		
		log.info("Operon>>>>>>>>>>>>>>>>> There are  " + taskVos.length + " Explicit Timed WorkItems that needs to be rescheduled. Will do this now if any......" + " <<<<<<<<<<<<<<<<<<<<<<<<");
		
		List<WorkItem> workItemList = new ArrayList<WorkItem>();
		for (int i=0; i<taskVos.length; i++) {
			log.info("Operon>>>>>>>>>>>>>>>>> Restarting Due Triggered Time WorkItem  " + taskVos[i].getWfnetTransitionRef() + "_" + taskVos[i].getTaskId() + " <<<<<<<<<<<<<<<<<<<<<<<<");
			
			CaseVo caseVo = taskVos[i].getCaseVo();
			RootCaseType rootCaseType = this.caseTypeManager.getRootCaseTypeById(caseVo.getRootCaseTypeRef());
			if (caseVo.getCaseTypeRef().equals(caseVo.getRootCaseTypeRef())) {
				workItemList.add(new WorkItem(this.applicationName, taskVos[i], rootCaseType));
			} else {
				CaseType caseType = rootCaseType.findSubCaseTypeById(caseVo.getCaseTypeRef());
				workItemList.add(new WorkItem(this.applicationName, taskVos[i], caseType));
				
			}
		}
		
		WorkItem[] workItems = workItemList.toArray(new WorkItem[workItemList.size()]);
		for (int i=0; i<workItems.length; i++) {
			WorkflowCoreEngineImpl._setStartWorkItemTimerUsingQuartzScheduler( workItems[i], this.applicationName, this.log);
			
		}
	
		
	}
	
	public void _triggerExpiredCasesOrStartDueWorkItems(String cronExp) {
		
		//=====================================
		// Trigger the Expired Case
		//=====================================
		CaseVo[] caseVos = this.caseDao.findAllCasesDueToExpireByCronExp(cronExp);
		List<Case> caseList = new ArrayList<Case>();
		for (int i=0; i< caseVos.length; i++) {
			try {
				caseList.add(_createCaseFromVo(this.applicationName, caseVos[i], this.caseTypeManager, this.operonRegistryFilename) );
			}catch (CaseTypeNotExistException e) {
				if (!e.isLogged()) {
					e.setLogged();
					log.error(e.getMessage(), e);
										
				}
			}
		}
		
		Case[] expiredCases = caseList.toArray( new Case[caseList.size()]);
		if (log.isDebugEnabled()) {
			log.debug("There are   " + expiredCases.length + " Cases that have expired...");
		
		}
		
		for (int i=0; i<expiredCases.length; i++) {
			_triggerExpireCaseJobUsingQuartzScheduler(expiredCases[i], this.log);
		}
		
		//==============================================
		// Start all WorkItems that matches the cronExp
		//==============================================
		TaskVo[] taskVos = this.caseDao.findAllWorkItemsDueToStartByCronExp(cronExp);
		List<WorkItem> workItemlist = new ArrayList<WorkItem>();
		
		//=============================
		// construct the WorkItems
		//==============================
		for (int i=0; i< taskVos.length; i++) {
			RootCaseType rootCaseType = this.caseTypeManager.getRootCaseTypeById(taskVos[i].getCaseVo().getRootCaseTypeRef());
			if (rootCaseType == null) {
					BaseSystemException ex = new OperonConfigException(new ErrorCode(
							Const.ERROR_CODE_operon_net_config_error),
							"RootCaseType " + taskVos[i].getCaseVo().getRootCaseTypeRef() + " cannot be found for workItem " + taskVos[i].getWfnetTransitionRef() + "_" + taskVos[i].getTaskId()
									+ " Please check the net files again or it could be that someone has been tampering with the database operon_case table");
					throw ex;
					
				
			}
			CaseType caseType = null;
			if (taskVos[i].getCaseVo().getCaseTypeRef().equals(rootCaseType.getId())) {
				caseType = rootCaseType;
				
			} else {
				caseType = rootCaseType.findSubCaseTypeById(taskVos[i].getCaseVo().getCaseTypeRef());
				if (caseType == null) {
					BaseSystemException ex = new OperonConfigException(new ErrorCode(
							Const.ERROR_CODE_operon_net_config_error),
							"SubCaseType " + taskVos[i].getCaseVo().getCaseTypeRef() + " cannot be found for workItem " + taskVos[i].getWfnetTransitionRef() + "_" + taskVos[i].getTaskId()
									+ " Please check the net files again or it could be that someone has been tampering with the database operon_case table");
					throw ex;
					
				
				}
			}
			
			WorkItem workItem = new WorkItem(this.applicationName, taskVos[i], caseType);
			workItemlist.add(workItem);
		}
		
		WorkItem[] workItems = workItemlist.toArray(new WorkItem[workItemlist.size()]);
		
		try {
			_startWorkItemsAndAutoTriggerFinishActivities(workItems, this.applicationName);
			
		} catch (ResourceAccessDeniedException e) {
			if (!e.isLogged()) {
				e.setLogged();
				log.error(e.getMessage(), e);
				
			}
		}
	}
	
	private static void _triggerExpireCaseJobUsingQuartzScheduler(Case aCase, Logger log) {
		org.quartz.Scheduler sched = Operon.getInstance().getScheduler();
		
		
		StringBuffer id = new StringBuffer();
		id.append("ExpireCase_").append(aCase.getCaseVo().getCaseTypeRef()).append("_").append(aCase.getCaseVo().getCaseId());
		String groupName = Const.QUARTZ_GROUP_operon + "_" + aCase.getApplicationName();
		
		JobDetail jobDetail = new JobDetail(id.toString(), groupName,ExpireCaseJob.class);
		jobDetail.getJobDataMap().put(Const.QUARTZ_JOBDATAMAP_KEY_case, aCase);
		jobDetail.setDurability(false);

		Trigger trigger = new SimpleTrigger(id.toString(), groupName);
		
		try {
			sched.scheduleJob(jobDetail, trigger);
			
		} catch (ObjectAlreadyExistsException e) {
			log.warn("Operon !!!!!!!!!!!!!!! Cannnot register job:  " + e.getMessage() + " !!!!!!!!!!!!!!!!!!!!!");
			return;
			
			
		} catch (SchedulerException e) {
			BaseSystemException ex = new BaseSystemException(new ErrorCode(
					Const.ERROR_CODE_quartz_registerjob_error),
					"Cannot register job " + id + "with the default Scheduler", e );
			throw ex;
			
		}

	}
	
	/**
	 * Registers all Implicit schedulers including ones that were already activiated in the last startup.
	 *
	 */
	public void _registerImplicitSchedulersWithQuartzAtStartup() {
		//===========================================
		// Get all existing cronExp for open Cases
		// and enabled WorkItems and re-register them
		//===========================================
		String[] cronStrExps = this.caseDao.findAllExistingOpenCaseCronExp();
		for (int i=0; i< cronStrExps.length; i++) {
			this.caseTypeManager.registerUnofficialCronExpStr(cronStrExps[i]);
		}
		//=======================================
		// Start the schedulers
		//=======================================
		CronExpression cronExps[] = this.caseTypeManager.getAllCronExpressions();
		StringBuffer sb = new StringBuffer();
		sb.append("Operon>>>>>>>>>>>>>>>>>There are " + cronExps.length + " Implicit Schedulers that need to be registered. will do this if any..... <<<<<<<<<<<<<<<<<<<<<<<<");
		log.info(sb.toString());
		for (int i=0; i< cronExps.length; i++ ) {
			_registerASingleImplicitSchedulerUsingQuartzScheduler(this.applicationName, cronExps[i], this.log);
			
		}
		
	}

	
	
	private static void _registerASingleImplicitSchedulerUsingQuartzScheduler (String applicationName, CronExpression cronExp, Logger log) {
		org.quartz.Scheduler sched = Operon.getInstance().getScheduler();
		
		
		String id= cronExp.getExpression();
		String groupName = Const.QUARTZ_GROUP_operon + "_" + applicationName;
		
		JobDetail jobDetail = new JobDetail(id, groupName, TriggerExpireCasesOrStartWorkItemsJob.class);
		jobDetail.getJobDataMap().put(Const.QUARTZ_JOBDATAMAP_KEY_applicationName, applicationName);
		jobDetail.setDurability(false);

		CronTrigger trigger = null;
	
		try {
			trigger = new CronTrigger(id, groupName, cronExp.getExpression());
			
		}catch (ParseException e) {
			BaseSystemException ex = new BaseSystemException(new ErrorCode(
					Const.ERROR_CODE_quartz_registerjob_error),
					"Cannot register cron job " + jobDetail.getName() + "with the default Scheduler because the cronExp " + cronExp.getExpression() + " is invalid. ", e );
			throw ex;
			
		}
			
		try {
			sched.scheduleJob(jobDetail, trigger);
		
		} catch (ObjectAlreadyExistsException e) {
			log.warn("Operon !!!!!!!!!!!!!!! Cannnot register job:  " + e.getMessage() + " !!!!!!!!!!!!!!!!!!!!!");
			return;
			
		} catch (SchedulerException e) {
			BaseSystemException ex = new BaseSystemException(new ErrorCode(
					Const.ERROR_CODE_quartz_registerjob_error),
					"Cannot register job " + jobDetail.getName() + "with the default Scheduler", e );
			throw ex;
			
		}
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("Operon>>>>>>>>>>>>>>>>> Registered Implicit Scheduled job for CronExp  ").append(cronExp.getExpression())
			.append(" for the following schedulers refs .. ");
		String[] schedRefs = cronExp.getSchedulerRefIds();
		if (schedRefs.length != 0) {
			for (int i=0; i<schedRefs.length; i++) {
				sb.append("[").append(schedRefs[i]).append("]");
				
			}
			
		} else {			
			sb.append("[unofficial scheduler from last startup]");
		}
		
		sb.append(" <<<<<<<<<<<<<<<<<<<<<<<<");
		
		log.info(sb.toString());
		
		
		
	}
	
	/**
	 * Deletes a Timed WorkItem job
	 * @param applicationName
	 * @param workItem
	 * @param log
	 */
	private static void _deleteASingleTimedWorkItemJobUsingQuartzScheduler(String applicationName, WorkItem workItem, Logger log) {
		org.quartz.Scheduler sched = Operon.getInstance().getScheduler();
		String id= "WorkItem_"  + workItem.getTaskVo().getTaskId();
		String groupName = Const.QUARTZ_GROUP_operon + "_" + applicationName ;

		try {
			if ( null == sched.getJobDetail(id, groupName)) {
				log.warn("Trying to delete Job id=" +id + " groupName=" + groupName
						+ " from Quartz Scheduler but it does not exist");
				return;
			}
			
			sched.deleteJob(id, groupName);


		} catch (SchedulerException e) {
			BaseSystemException ex = new BaseSystemException(new ErrorCode(
					Const.ERROR_CODE_quartz_registerjob_error),
					"Problem cannot delete job id=" + id + "groupName=" + groupName + " with the default Scheduler", e );
			throw ex;
			
		}
				
		
	}
	
	/**
	 * Deletes a Timed WorkItem job
	 * @param applicationName
	 * @param workItem
	 * @param log
	 */
	private static void _deleteTimedWorkItemsJobUsingQuartzScheduler(String applicationName, WorkItem[] workItems, Logger log) {
		
		for (int i=0; i<workItems.length; i++) {
			_deleteASingleTimedWorkItemJobUsingQuartzScheduler(applicationName, workItems[i], log);
		}
		
	}	
	
	public void expireCase(Case aCase) {
		RootCaseType rootCaseType = (RootCaseType) aCase.getCaseType();
		ResourceManager resourceManager = (ResourceManager) ClassCache.getInstance().getObject(rootCaseType.getResourceManagerInterface());		
		TriggerContext triggerContext = new TriggerContext();
		triggerContext.setCurrentResource(resourceManager.getDefaultResourceForAutoTrigger());
		BaseAppException ex = new BaseAppException(new ErrorCode(
				Const.ERROR_CODE_case_expire),
				"Case has expired");

		//=============================
		// Expire all the WorkItems
		//==============================
		WorkItem[] workItems = aCase.getAllWorkItemsFromRootCase();
		for (int i=0; i< workItems.length; i++) {
			if (this.log.isDebugEnabled()) {
				log.debug("Case has expired " +  aCase.getCaseVo().getCaseTypeRef() + "_" + aCase.getCaseVo().getCaseId()
							+ " will expire WorkItem " + workItems[i].getTaskVo().getWfnetTransitionRef() + "_" + workItems[i].getTaskVo().getTaskId());
			}
			_updateWorkItemStatus(workItems[i].getTaskVo(), triggerContext, Const.TASK_EVENT_case_expires, Const.TASK_STATUS_case_expired, ex, this.caseDao);
		}
		
		Activity[] activities = aCase.getAllActivitiesFromRootCase();
		for (int i=0; i< activities.length; i++) {
			if (this.log.isDebugEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("Case has expired ").append(aCase.getCaseVo().getCaseTypeRef()).append("_").append( aCase.getCaseVo().getCaseId())
					.append(" will expire Activity ").append(activities[i].getTaskVo().getWfnetTransitionRef()).append("_").append(activities[i].getTaskVo().getTaskId());
				log.debug(sb.toString());
			}
			
			_updateWorkItemStatus(activities[i].getTaskVo(), triggerContext, Const.TASK_EVENT_case_expires, Const.TASK_STATUS_case_expired, ex, this.caseDao);
		}
		
		//=====================================
		// Expire all subCases
		//=====================================
		Case[] subCases = aCase.getSubCasesFromRoot();
		for (int i=0; i< subCases.length; i++) {
			if ( Const.CASE_STATUS_open.equals(subCases[i].getCaseVo().getCaseStatus()) ) {
				
				if (this.log.isDebugEnabled()) {
					log.debug("Case has expired " +  aCase.getCaseVo().getCaseTypeRef() + "_" + aCase.getCaseVo().getCaseId()
								+ " will expire Subcase " + subCases[i].getCaseVo().getCaseTypeRef() + "_" + subCases[i].getCaseVo().getCaseId());
				}
				
				_updateCaseStatus(subCases[i].getCaseVo(), triggerContext, Const.CASE_EVENT_expire, Const.CASE_STATUS_expired, ex, this.caseDao);
				
			}
		}
		
		//================================
		// Finally expire the RootCase
		//================================
		_updateCaseStatus(aCase.getCaseVo(), triggerContext, Const.CASE_EVENT_expire, Const.CASE_STATUS_expired, ex, this.caseDao);
		
		
	}
	
	/**
	 * <p>Manually cancels a case. Only RootCase can be cancelled<p> 
	 * <p>Should run in its own transaction<p>
	 * @param aCase
	 */
	public void manuallyCancelCase(Case aCase, TriggerContext triggerContext, String reason) throws NotRootCaseException, ResourceAccessDeniedException, SetAttributeException, AutoTriggerInProgressException, InvalidManualOperationException {
		
		if (StringUtils.isBlank(reason)) {
			SetAttributeException ex = new SetAttributeException(new ErrorCode(
					Const.ERROR_CODE_reason_required),
					"Please supply a reason why you wish to cancel the Case");
			throw ex;
			
		}
		
		
		if (!(aCase.getCaseType() instanceof RootCaseType)) {
			NotRootCaseException ex = new NotRootCaseException(new ErrorCode(
					Const.ERROR_CODE_not_root_case),
					"This is not a RootCase, you can only cancel from the RootCase");
			throw ex;
		}
		
		RootCaseType rootCaseType = (RootCaseType) aCase.getCaseType();

		if (!Const.CASE_STATUS_open.equalsIgnoreCase(aCase.getCaseVo().getCaseStatus())) {
			//not open, do nothing
			String errMsg = "Ignoring request to Cancel Case " +  aCase.getCaseVo().getCaseTypeRef() + "_" +  aCase.getCaseVo().getCaseId() 
					+ " because the case is not Open. Infact it is in " +  aCase.getCaseVo().getCaseStatus();
			
			throw new InvalidManualOperationException(new ErrorCode(
					Const.ERROR_CODE_status_not_cancellable),errMsg); 
		}
		
		//==============================
		// Check if TriggerResource has
		// access to Cancel Case
		//===============================
		Place startPlace = CaseTypeManager.findSourcePlace(rootCaseType);		
		ResourceManager resourceManager = (ResourceManager) ClassCache.getInstance().getObject(rootCaseType.getResourceManagerInterface());		
		if (!resourceManager.isTriggerResourcePartOfResource(triggerContext, startPlace.getResources())) {
			ResourceAccessDeniedException ex = new ResourceAccessDeniedException(new ErrorCode(
					Const.ERROR_CODE_case_cancellation),
					"Resource  " + triggerContext.getCurrentResource().getId() + " is not allowed to Cancel the Case " + rootCaseType.getId() + "_" + aCase.getCaseVo().getCaseId());
			throw ex;
			
		}
		
		BaseAppException reasonEx  = null;
		if (reason.length() > 500) {
			reasonEx = new BaseAppException(new ErrorCode(Const.ERROR_CODE_unknown_error),
								reason.substring(0, 500));
		} else {
			reasonEx = new BaseAppException(new ErrorCode(Const.ERROR_CODE_unknown_error), reason);
			
		}
			
		//=============================
		// Cancel all the WorkItems
		//==============================
		WorkItem[] workItems = aCase.getAllWorkItemsFromRootCase();
		for (int i=0; i< workItems.length; i++) {
			if (this.log.isDebugEnabled()) {
				log.debug("Cancelling Case " +  aCase.getCaseVo().getCaseTypeRef() + "_" + aCase.getCaseVo().getCaseId()
							+ " will result in Cancelling WorkItem " + workItems[i].getTaskVo().getWfnetTransitionRef() + "_" + workItems[i].getTaskVo().getTaskId());
			}
			
			Transition transition = workItems[i].getCaseType().findTransitionById(workItems[i].getTaskVo().getWfnetTransitionRef());
			if (transition == null) {
				BaseSystemException ex = new OperonConfigException(new ErrorCode(
						Const.ERROR_CODE_operon_net_config_error),
						"Transition " + workItems[i].getTaskVo().getWfnetTransitionRef() + " is not in the  net " + workItems[i].getCaseType().getId()
								+ " Please check the net files again or it could be someone has been tampering with the database operon_workitem table");
				throw ex;
			}
			
			if (Const.TRIGGER_TYPE_auto.equalsIgnoreCase(transition.getTriggerType())) {
				AutoTriggerInProgressException ex = new AutoTriggerInProgressException(new ErrorCode(
						Const.ERROR_CODE_auto_trigger_inprogress),
						"Cannot Cancel Case " + workItems[i].getCaseType().getId() + "_"  + aCase.getCaseVo().getCaseId()+ " because Auto Triggered WorkItem " + transition.getId() + "_" + workItems[i].getTaskVo().getTaskId() + " is enbaled, please try again unitl this finishes");
				throw ex;
				
			}
			
			_updateWorkItemStatus(workItems[i].getTaskVo(), triggerContext, Const.TASK_EVENT_case_cancels, Const.TASK_STATUS_case_cancelled, reasonEx, this.caseDao);
		}
		
		//=============================
		// Cancel all the Activities
		//==============================
		Activity[] activities = aCase.getAllActivitiesFromRootCase();
		for (int i=0; i< activities.length; i++) {
			if (this.log.isDebugEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("Cancelling Case ").append(aCase.getCaseVo().getCaseTypeRef()).append("_").append( aCase.getCaseVo().getCaseId())
					.append(" will result in cancelling Activity ").append(activities[i].getTaskVo().getWfnetTransitionRef()).append("_").append(activities[i].getTaskVo().getTaskId());
				log.debug(sb.toString());
			}
			
			Transition transition = activities[i].getCaseType().findTransitionById(activities[i].getTaskVo().getWfnetTransitionRef());
			if (transition == null) {
				BaseSystemException ex = new OperonConfigException(new ErrorCode(
						Const.ERROR_CODE_operon_net_config_error),
						"Transition " + activities[i].getTaskVo().getWfnetTransitionRef() + " is not in the  net " + activities[i].getCaseType().getId()
								+ " Please check the net files again or it could be someone has been tampering with the database operon_workitem table");
				throw ex;
			}
			
			if (Const.TRIGGER_TYPE_auto.equalsIgnoreCase(transition.getTriggerType()) || Const.TRIGGER_TYPE_time.equals(transition.getTriggerType()) ) {
				AutoTriggerInProgressException ex = new AutoTriggerInProgressException(new ErrorCode(
						Const.ERROR_CODE_auto_trigger_inprogress),
						"Cannot Cancel Case " + activities[i].getCaseType().getId() + "_"  + aCase.getCaseVo().getCaseId()+ " because Auto/Time Triggered Activity " + transition.getId() + "_" + activities[i].getTaskVo().getTaskId() + " is being in_progress, please try again unitl this finishes");
				throw ex;
				
			}
			
			_updateWorkItemStatus(activities[i].getTaskVo(), triggerContext, Const.TASK_EVENT_case_cancels, Const.TASK_STATUS_case_cancelled, reasonEx, this.caseDao);
		}
		
		//=====================================
		// Cancel all subCases
		//=====================================
		Case[] subCases = aCase.getSubCasesFromRoot();
		for (int i=0; i< subCases.length; i++) {
			if ( Const.CASE_STATUS_open.equals(subCases[i].getCaseVo().getCaseStatus()) ) {
				
				if (this.log.isDebugEnabled()) {
					log.debug("Cancel Case " +  aCase.getCaseVo().getCaseTypeRef() + "_" + aCase.getCaseVo().getCaseId()
								+ " will cancel Subcase " + subCases[i].getCaseVo().getCaseTypeRef() + "_" + subCases[i].getCaseVo().getCaseId());
				}
				
				_updateCaseStatus(subCases[i].getCaseVo(), triggerContext, Const.CASE_EVENT_m_cancel, Const.CASE_STATUS_cancelled, reasonEx, this.caseDao);
				
			}
		}
		
		//================================
		// Finally Cancel the RootCase
		//================================
		_updateCaseStatus(aCase.getCaseVo(), triggerContext, Const.CASE_EVENT_m_cancel, Const.CASE_STATUS_cancelled, reasonEx, this.caseDao);
		
		//=====================================
		// Remove all timers associated with
		// timer WorkItem
		//=====================================
		for (int i=0; i< workItems.length; i++) {
						
			Transition transition = workItems[i].getCaseType().findTransitionById(workItems[i].getTaskVo().getWfnetTransitionRef());

			if (Const.TRIGGER_TYPE_time.equalsIgnoreCase(transition.getTriggerType()) 
					&& transition.getTriggerDelayDuration().isExplicit()) {
				
				_deleteASingleTimedWorkItemJobUsingQuartzScheduler(aCase.getApplicationName(), workItems[i], log);
			}
			
			
		}
		
		
	}
	
	/**
	 * <p>Manually Suspends a case. Only RootCase can be cancelled<p> 
	 * <p>Should run in its own transaction<p>
	 * @param aCase
	 */
	public void manuallySuspendCase(Case aCase, TriggerContext triggerContext, String reason) throws NotRootCaseException, ResourceAccessDeniedException, SetAttributeException, AutoTriggerInProgressException, InvalidManualOperationException {
		if (StringUtils.isBlank(reason)) {
			SetAttributeException ex = new SetAttributeException(new ErrorCode(
					Const.ERROR_CODE_reason_required),
					"Please supply a reason why you wish to suspend the Case");
			throw ex;
			
		}

		if (!(aCase.getCaseType() instanceof RootCaseType)) {
			NotRootCaseException ex = new NotRootCaseException(new ErrorCode(
					Const.ERROR_CODE_not_root_case),
					"This is not a RootCase, you can only cancel from the RootCase");
			throw ex;
		}
		
		if (!Const.CASE_STATUS_open.equalsIgnoreCase(aCase.getCaseVo().getCaseStatus())) {
			//not open, do nothing
			String errMsg = "Ignoring request to Suspend Case " +  aCase.getCaseVo().getCaseTypeRef() + "_" +  aCase.getCaseVo().getCaseId() 
					+ " because the Case is not Open. Infact it is in " +  aCase.getCaseVo().getCaseStatus();
			
			throw new InvalidManualOperationException(new ErrorCode(
					Const.ERROR_CODE_status_not_suspendable),errMsg); 
			
		}
		
		RootCaseType rootCaseType = (RootCaseType) aCase.getCaseType();

		//==============================
		// Check if TriggerResource has
		// access to Cancel Case
		//===============================
		Place startPlace = CaseTypeManager.findSourcePlace(rootCaseType);		
		ResourceManager resourceManager = (ResourceManager) ClassCache.getInstance().getObject(rootCaseType.getResourceManagerInterface());		
		if (!resourceManager.isTriggerResourcePartOfResource(triggerContext, startPlace.getResources())) {
			ResourceAccessDeniedException ex = new ResourceAccessDeniedException(new ErrorCode(
					Const.ERROR_CODE_case_cancellation),
					"Resource  " + triggerContext.getCurrentResource().getId() + " is not allowed to Cancel the Case " + rootCaseType.getId() + "_" + aCase.getCaseVo().getCaseId());
			throw ex;
			
		}
		
		BaseAppException reasonEx  = null;
		if (reason.length() > 500) {
			reasonEx = new BaseAppException(new ErrorCode(Const.ERROR_CODE_unknown_error),
								reason.substring(0, 500));
		} else {
			reasonEx = new BaseAppException(new ErrorCode(Const.ERROR_CODE_unknown_error), reason);
			
		}

		//========================================
		// Check for Auto/Time Activities are in progress
		//========================================
		Activity[] activities = aCase.getAllActivitiesFromRootCase();
		for (int i=0; i< activities.length; i++) {
			
			Transition transition = activities[i].getCaseType().findTransitionById(activities[i].getTaskVo().getWfnetTransitionRef());
			if (transition == null) {
				BaseSystemException ex = new OperonConfigException(new ErrorCode(
						Const.ERROR_CODE_operon_net_config_error),
						"Transition " + activities[i].getTaskVo().getWfnetTransitionRef() + " is not in the  net " + activities[i].getCaseType().getId()
								+ " Please check the net files again or it could be someone has been tampering with the database operon_workitem table");
				throw ex;
			}
			
			if (Const.TRIGGER_TYPE_auto.equalsIgnoreCase(transition.getTriggerType()) || Const.TRIGGER_TYPE_time.equals(transition.getTriggerType()) ) {
				AutoTriggerInProgressException ex = new AutoTriggerInProgressException(new ErrorCode(
						Const.ERROR_CODE_auto_trigger_inprogress),
						"Cannot Suspend Case " + activities[i].getCaseType().getId() + "_"  + aCase.getCaseVo().getCaseId()+ " because Auto/Time Triggered Activity " + transition.getId() + "_" + activities[i].getTaskVo().getTaskId() + " is being in_progress, please try again unitl this finishes");
				throw ex;
				
			}
			
		}
		
		//==========================================
		// Check for AutoWorkItems in Progress
		//==========================================
		WorkItem[] workItems = aCase.getAllWorkItemsFromRootCase();
		for (int i=0; i< workItems.length; i++) {
			
			Transition transition = workItems[i].getCaseType().findTransitionById(workItems[i].getTaskVo().getWfnetTransitionRef());
			if (transition == null) {
				BaseSystemException ex = new OperonConfigException(new ErrorCode(
						Const.ERROR_CODE_operon_net_config_error),
						"Transition " + workItems[i].getTaskVo().getWfnetTransitionRef() + " is not in the  net " + workItems[i].getCaseType().getId()
								+ " Please check the net files again or it could be someone has been tampering with the database operon_workitem table");
				throw ex;
			}
			
			if (Const.TRIGGER_TYPE_auto.equalsIgnoreCase(transition.getTriggerType())) {
				AutoTriggerInProgressException ex = new AutoTriggerInProgressException(new ErrorCode(
						Const.ERROR_CODE_auto_trigger_inprogress),
						"Cannot Suspend Case " + workItems[i].getCaseType().getId() + "_"  + aCase.getCaseVo().getCaseId()+ " because Auto Triggered WorkItem " + transition.getId() + "_" + workItems[i].getTaskVo().getTaskId() + " is enbaled, please try again unitl this finishes");
				throw ex;
				
			}
			
			
		}
				
		//================================
		// Suspend Case
		//================================
		_updateCaseStatus(aCase.getCaseVo(), triggerContext, Const.CASE_EVENT_m_suspend, Const.CASE_STATUS_suspended, reasonEx, this.caseDao);
		
		//=====================================
		// Remove all timers associated with
		// timer WorkItem
		//=====================================
		for (int i=0; i< workItems.length; i++) {
						
			Transition transition = workItems[i].getCaseType().findTransitionById(workItems[i].getTaskVo().getWfnetTransitionRef());

			if (Const.TRIGGER_TYPE_time.equalsIgnoreCase(transition.getTriggerType()) 
					&& transition.getTriggerDelayDuration().isExplicit()) {
				
				_deleteASingleTimedWorkItemJobUsingQuartzScheduler(aCase.getApplicationName(), workItems[i], log);
			}
			
			
		}
		
		
	}
	
	/**
	 * Manually Resumes a suspended Case.
	 * @param aCase
	 * @param triggerContext
	 * @param reason
	 * @throws NotRootCaseException
	 * @throws ResourceAccessDeniedException
	 * @throws SetAttributeException
	 */
	public void manuallyResumeCase(Case aCase, TriggerContext triggerContext, String reason) throws NotRootCaseException, ResourceAccessDeniedException, SetAttributeException {
		if (StringUtils.isBlank(reason)) {
			SetAttributeException ex = new SetAttributeException(new ErrorCode(
					Const.ERROR_CODE_reason_required),
					"Please supply a reason why you wish to cancel the Case");
			throw ex;
			
		}
				
		if (!(aCase.getCaseType() instanceof RootCaseType)) {
			NotRootCaseException ex = new NotRootCaseException(new ErrorCode(
					Const.ERROR_CODE_not_root_case),
					"This is not a RootCase, you can only cancel from the RootCase");
			throw ex;
		}
		
		if (!Const.CASE_STATUS_suspended.equalsIgnoreCase(aCase.getCaseVo().getCaseStatus())) {
			//not suspended, do nothing
			log.warn("Ignoring request to Resume Case " +  aCase.getCaseVo().getCaseTypeRef() + "_" +  aCase.getCaseVo().getCaseId() 
					+ " because the case is not Suspended. Infact it is in " +  aCase.getCaseVo().getCaseStatus());
			return;
		}
		
		RootCaseType rootCaseType = (RootCaseType) aCase.getCaseType();

		//==============================
		// Check if TriggerResource has
		// access to Cancel Case
		//===============================
		Place startPlace = CaseTypeManager.findSourcePlace(rootCaseType);		
		ResourceManager resourceManager = (ResourceManager) ClassCache.getInstance().getObject(rootCaseType.getResourceManagerInterface());		
		if (!resourceManager.isTriggerResourcePartOfResource(triggerContext, startPlace.getResources())) {
			ResourceAccessDeniedException ex = new ResourceAccessDeniedException(new ErrorCode(
					Const.ERROR_CODE_case_cancellation),
					"Resource  " + triggerContext.getCurrentResource().getId() + " is not allowed to Cancel the Case " + rootCaseType.getId() + "_" + aCase.getCaseVo().getCaseId());
			throw ex;
			
		}
		
		BaseAppException reasonEx  = null;
		if (reason.length() > 500) {
			reasonEx = new BaseAppException(new ErrorCode(Const.ERROR_CODE_unknown_error),
								reason.substring(0, 500));
		} else {
			reasonEx = new BaseAppException(new ErrorCode(Const.ERROR_CODE_unknown_error), reason);
			
		}
		
		//================================
		// Open Case
		//================================
		_updateCaseStatus(aCase.getCaseVo(), triggerContext, Const.CASE_EVENT_m_resume, Const.CASE_STATUS_open, reasonEx, this.caseDao);
		
		//==================================
		// Re-activate Timer for Timer WorkItems
		//==================================
		WorkItem[] workItems = aCase.getAllWorkItemsFromRootCase();
		for (int i=0; i< workItems.length; i++) {
			
			Transition transition = workItems[i].getCaseType().findTransitionById(workItems[i].getTaskVo().getWfnetTransitionRef());
			if (transition == null) {
				BaseSystemException ex = new OperonConfigException(new ErrorCode(
						Const.ERROR_CODE_operon_net_config_error),
						"Transition " + workItems[i].getTaskVo().getWfnetTransitionRef() + " is not in the  net " + workItems[i].getCaseType().getId()
								+ " Please check the net files again or it could be someone has been tampering with the database operon_workitem table");
				throw ex;
			}
			
			if (Const.TRIGGER_TYPE_time.equalsIgnoreCase(transition.getTriggerType())
					&& transition.getTriggerDelayDuration().isExplicit()) {
				_setStartWorkItemTimerUsingQuartzScheduler(workItems[i], aCase.getApplicationName(), log);
				
			}
			
			
		}
		
		
	}
	
	public void _monitorTimeoutActivities() {
		WorkflowCoreEngine workflowCoreEngine = (WorkflowCoreEngine) Operon.getInstance().getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngine);
		
		//====================================
		// Fire all Activities AWAITING_RETRY
		// and execute them
		//====================================
		TaskVo[] awaitRetryVos = this.caseDao.findActivitiesAwaitingRetry();
		if (this.log.isDebugEnabled()) {
			log.debug("There are " + awaitRetryVos.length + " Actvities awaiting_retry");
		}
		
		for (int i=0; i<awaitRetryVos.length; i++) {
			workflowCoreEngine.excuteASingleActivityAwaitingRetry(awaitRetryVos[i]);
		}
		
		//==================================
		// Requeue any timeout Activities
		//==================================
		TaskVo[] timeoutVos = this.caseDao.findTimeoutActivities();
		if (timeoutVos.length != 0) {
			log.info("Operon>>>>>>>>>>>>>>>>> There are " + awaitRetryVos.length + " Activities that have timeout.");
		}
		for (int i=0; i<timeoutVos.length; i++) {
			workflowCoreEngine.requeueASingleTimeoutActivity(timeoutVos[i]);
		}
	}
	
	public void excuteASingleActivityAwaitingRetry(TaskVo taskVo) {
		RootCaseType rootCaseType = this.caseTypeManager.getRootCaseTypeById(taskVo.getCaseVo().getRootCaseTypeRef());
		CaseType caseType = null;
		
		if (this.log.isDebugEnabled()) {
			log.debug("ReExcuting Activity " + taskVo.getWfnetTransitionRef() + "_" + taskVo.getTaskId() + " that has timeout and awaiting retry");
		}
		
		if (rootCaseType.getId().equals(taskVo.getCaseVo().getCaseTypeRef())) {
			caseType = rootCaseType;
			
		} else {
			caseType = rootCaseType.findSubCaseTypeById(taskVo.getCaseVo().getCaseTypeRef());
			
		}

		ResourceManager resourceManager = (ResourceManager) ClassCache.getInstance().getObject(rootCaseType.getResourceManagerInterface());		
		TriggerContext triggerContext = new TriggerContext();
		triggerContext.setCurrentResource(resourceManager.getDefaultResourceForAutoTrigger());
		
		Transition transition = caseType.findTransitionById(taskVo.getWfnetTransitionRef());
		long nextExecutionTimeout = transition.getExecutionTimeLimit().getDurationInMilliSecs() + System.currentTimeMillis();
		
		//==============================
		// Update status to in_progress
		//==============================
		taskVo.setRetryCount( new Long ( taskVo.getRetryCount().longValue() + 1 ) );
		taskVo.setInProgressTimeout(new Date(nextExecutionTimeout));				
		_updateWorkItemStatus(taskVo, triggerContext, Const.TASK_EVENT_re_execute, Const.TASK_STATUS_in_progress, null, this.caseDao);
		
		//============================
		// Trigger Quartz Job to 
		// finish the Activitivy
		//============================
		Activity activity = new Activity(this.applicationName, taskVo, caseType);
		_autoTriggerFinishActivityUsingQuartzScheduler(activity);
		
		
	}
	
	public void _startMonitoringForTimeoutActivitiesAtStartupUsingQuartzScheduler() {
		org.quartz.Scheduler sched = Operon.getInstance().getScheduler();
		
		String id="monitorActivity";
		String groupName = Const.QUARTZ_GROUP_operon + "_" + this.applicationName ;
		
		JobDetail jobDetail = new JobDetail(id, groupName, MonitorActivityJob.class);
		jobDetail.getJobDataMap().put(Const.QUARTZ_JOBDATAMAP_KEY_applicationName, this.applicationName);
		jobDetail.setDurability(false);
		

		CronTrigger trigger = null;
		
		try {
			trigger = new CronTrigger(id, groupName, this.retryActivityCronInterval);
			
		}catch (ParseException e) {
			BaseSystemException ex = new BaseSystemException(new ErrorCode(
					Const.ERROR_CODE_quartz_registerjob_error),
					"Cannot register cron job " + jobDetail.getName() + "with the default Scheduler because the cronExp " + this.retryActivityCronInterval + " is invalid. ", e );
			throw ex;
			
		}
			
		try {
			sched.scheduleJob(jobDetail, trigger);
			
		} catch (ObjectAlreadyExistsException e) {
			log.warn("Operon !!!!!!!!!!!!!!! Cannnot register job:  " + e.getMessage() + " !!!!!!!!!!!!!!!!!!!!!");
			return;
			
		} catch (SchedulerException e) {
			BaseSystemException ex = new BaseSystemException(new ErrorCode(
					Const.ERROR_CODE_quartz_registerjob_error),
					"Cannot register job " + jobDetail.getName() + "with the default Scheduler", e );
			throw ex;
			
		}
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("Operon>>>>>>>>>>>>>>>>> Registered Monitor Activity job with cronExp  ").append(this.retryActivityCronInterval).append(" <<<<<<<<<<<<<<<<<<<<<<<<");		
		log.info(sb.toString());
		
		
		
		
	}
	
	public void requeueASingleTimeoutActivity(TaskVo taskVo) {
		RootCaseType rootCaseType = this.caseTypeManager.getRootCaseTypeById(taskVo.getCaseVo().getRootCaseTypeRef());
		ResourceManager resourceManager = (ResourceManager) ClassCache.getInstance().getObject(rootCaseType.getResourceManagerInterface());		
		TriggerContext triggerContext = new TriggerContext();
		triggerContext.setCurrentResource(resourceManager.getDefaultResourceForAutoTrigger());
		
		if ( taskVo.getRetryCount().longValue() > this.retryActivityMaxCount ) {
			//=================================
			// Max retry reached no need to
			// requeue. Will put this into suspend
			// mode
			//=================================
			if (this.log.isDebugEnabled()) {
				log.debug("Max Retry ("  + this.getRetryActivityMaxCount() + ") exceeded for Activity " + taskVo.getWfnetTransitionRef() + "_" + taskVo.getTaskId() + " Will suspend this Activity.");
			}
			
			_updateWorkItemStatus(taskVo, triggerContext, Const.TASK_EVENT_max_retry, Const.TASK_STATUS_suspended, null, this.caseDao);
			
		} else {
			//==============================
			// Requeue it
			//==============================
			if (this.log.isDebugEnabled()) {
				log.debug("Activity " + taskVo.getWfnetTransitionRef() + "_" + taskVo.getTaskId() + " has timeout, Will put this to await_retry....");
			}
			
			_updateWorkItemStatus(taskVo, triggerContext, Const.TASK_EVENT_timeout, Const.TASK_STATUS_await_retry, null, this.caseDao);
			
		}
	}
	
}
