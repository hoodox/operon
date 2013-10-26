package com.hoodox.operon.persistence.ibatis.sqlmapdao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;
import com.hoodox.operon.exceptions.CaseNotExistException;
import com.hoodox.operon.exceptions.DaoLockVersionException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.persistence.iface.CaseDao;
import com.hoodox.operon.valueobjects.CaseVo;
import com.hoodox.operon.valueobjects.EventAuditVo;
import com.hoodox.operon.valueobjects.TaskVo;
import com.hoodox.operon.valueobjects.TimeToLiveSchedulerVo;
import com.hoodox.operon.valueobjects.TimeTriggerSchedulerVo;
import com.hoodox.operon.valueobjects.TokenPlaceRefVo;
import com.hoodox.operon.valueobjects.TokenVo;


public class CaseSqlMapDao extends SqlMapClientDaoSupport implements CaseDao {

	//private Log log = LogFactory.getLog(this.getClass().getName());

	public CaseSqlMapDao() {
		super();
	}



	//=============================================================
	// Implementations of the NodeDAO interface
	//=============================================================
	public CaseVo getCaseById(Long caseId) {
		return (CaseVo) getSqlMapClientTemplate().queryForObject("getCaseById", caseId);
	}

	
	public Long createCase(CaseVo aCaseVo) {
		Long caseId = new Long(this.caseSequenceDao.nextLongValue());
		// add the id and add to DB
		aCaseVo.setCaseId(caseId);		
		
		Long rootParentCaseId = null;
		if (null == aCaseVo.getRootParentCaseId()) {
			rootParentCaseId = caseId;
			aCaseVo.setRootParentCaseId(rootParentCaseId);
		}
		
	    getSqlMapClientTemplate().insert("insertCase", aCaseVo);
		
		return caseId;
		
	}
	
	@SuppressWarnings("unchecked")
	public void updateCaseStatus(CaseVo aCase, Long currentLockVersion) {
		Map paramMap = new HashMap();
		paramMap.put("caseStatus", aCase.getCaseStatus());
		paramMap.put("newLockVersion", aCase.getLockVersion());
		paramMap.put("caseId", aCase.getCaseId());
		paramMap.put("currentLockVersion", currentLockVersion);
		paramMap.put("updatedDate",aCase.getUpdatedDate());
		
		
		if (0 == getSqlMapClientTemplate().update("updateCaseStatus", paramMap)) {
			BaseSystemException ex = new DaoLockVersionException(new ErrorCode(
					Const.ERROR_CODE_dao_lock_version),
					"Cannot update Case id " + aCase.getCaseId() + " to status " + aCase.getCaseStatus()
					+ " because Case lock version in database does not match currentLockVersion" + currentLockVersion);
			throw ex;
		}
		
	}
	
	public void removeCase(Long caseId) throws CaseNotExistException {
		// TODO Auto-generated method stub

	}

	public void addTimeToLiveSchedulers(TimeToLiveSchedulerVo[] ttlSchedulerVos) {
		for (int i=0; i< ttlSchedulerVos.length; i++) {
			addTimeToLiveScheduler(ttlSchedulerVos[i]);
		}
		
	}

	public void addTimeToLiveScheduler(TimeToLiveSchedulerVo ttlSchedulerVo) {		
	    getSqlMapClientTemplate().insert("insertTimeToLiveScheduler", ttlSchedulerVo);
		
	}

	public Long createToken(TokenVo token) {
		Long tokenId = new Long(this.tokenSequenceDao.nextLongValue());
		// add the id and add to DB
		token.setTokenId(tokenId);		

	    getSqlMapClientTemplate().insert("insertToken", token);
		
	    return tokenId;
	}
	@SuppressWarnings("unchecked")
	public void updateToken(TokenVo tokenVo, Long currentLockVersion) {
		Map paramMap = new HashMap();
		paramMap.put("tokenStatus", tokenVo.getTokenStatus());
		paramMap.put("newLockVersion", tokenVo.getLockVersion());
		paramMap.put("tokenId", tokenVo.getTokenId());
		paramMap.put("currentLockVersion", currentLockVersion); 
		paramMap.put("lockByTaskId", tokenVo.getLockByTaskId()); 
		paramMap.put("updatedDate",tokenVo.getUpdatedDate());
		
		
		if (0 == getSqlMapClientTemplate().update("updateToken", paramMap)) {
			BaseSystemException ex = new DaoLockVersionException(new ErrorCode(
					Const.ERROR_CODE_dao_lock_version),
					"Cannot update Token id " + tokenVo.getTokenId() + " to status " + tokenVo.getTokenStatus()
					+ " because Token lock version in database does not match currentLockVersion" + currentLockVersion);
			throw ex;
		}
		
	}

	public void addTokenPlaceRef(TokenPlaceRefVo tokenPlaceref) {
	    getSqlMapClientTemplate().insert("insertTokenPlaceRef", tokenPlaceref);

	}

	@SuppressWarnings("unchecked")
	public void addTokenEnabledTask(TokenVo tokenVo,  TaskVo taskVo) {
		Map paramMap = new HashMap();
		paramMap.put("tokenId", tokenVo.getTokenId());
		paramMap.put("taskId", taskVo.getTaskId());
	    getSqlMapClientTemplate().insert("insertTokenEnbaledTask", paramMap);
				
	}
	
	public Long createTask(TaskVo task) {
		Long taskId = new Long(this.taskSequenceDao.nextLongValue());
		// add the id and add to DB
		task.setTaskId(taskId);		
	    getSqlMapClientTemplate().insert("insertTask", task);
	    
	    return taskId;
	}

	@SuppressWarnings("unchecked")
	public void updateTaskStatus(TaskVo taskVo, Long currentLockVersion) {
		Map paramMap = new HashMap();
		paramMap.put("taskStatus", taskVo.getTaskStatus());
		paramMap.put("newLockVersion", taskVo.getLockVersion());
		paramMap.put("taskId", taskVo.getTaskId());
		paramMap.put("currentLockVersion", currentLockVersion);
		paramMap.put("retryCount", taskVo.getRetryCount());		
		paramMap.put("updatedDate",taskVo.getUpdatedDate());
		paramMap.put("actualCompletionDate",taskVo.getActualCompletionDate());
		paramMap.put("inProgressTimeout",taskVo.getInProgressTimeout());
		
				
		if (0 == getSqlMapClientTemplate().update("updateTaskStatus", paramMap)) {
			BaseSystemException ex = new DaoLockVersionException(new ErrorCode(
					Const.ERROR_CODE_dao_lock_version),
					"Cannot update Task id " + taskVo.getTaskId() + " to status " + taskVo.getTaskStatus()
					+ " because Task lock version in database does not match currentLockVersion" + currentLockVersion);
			throw ex;
		}
	}
	

	public void addTimeTriggerSchedulers(
			TimeTriggerSchedulerVo[] timeTrigSchedVos) {
		
		for (int i=0; i< timeTrigSchedVos.length; i++) {
			addTimeTriggerScheduler(timeTrigSchedVos[i]);
		}

	}

	public void addTimeTriggerScheduler(
			TimeTriggerSchedulerVo timeTrigSchedVo) {
	    getSqlMapClientTemplate().insert("insertTimeTriggerScheduler", timeTrigSchedVo);

	}
	
	
	/**
	 * Adds an event audit
	 * @param eventAuditVo
	 */
	public void addEventAudit(EventAuditVo eventAuditVo) {
		Long eventAuditId = new Long(this.eventAuditSequenceDao.nextLongValue());
		// add the id and add to DB
		eventAuditVo.setEventAuditId(eventAuditId);		
	    getSqlMapClientTemplate().insert("insertEventAudit", eventAuditVo);
	    
		
	}

	//==========================================
	// Finder methods
	//==========================================
	
	public Integer howManyChildrenCaseNotClosed(Long parentCaseId) {
		return (Integer) getSqlMapClientTemplate().queryForObject("howManySubcasesNotClosed", parentCaseId);
	}
	
	@SuppressWarnings("unchecked")
	public TaskVo[] findEnabledTasksByTokenId (Long tokenId) {
		Map paramMap = new HashMap();
		paramMap.put("tokenId", tokenId);
		paramMap.put("taskStatus", Const.TASK_STATUS_enabled);
	    List<TaskVo> list = getSqlMapClientTemplate().queryForList("findTasksBy_tokenId_taskStatus", paramMap);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TaskVo[list.size()]); 
		
	}

	@SuppressWarnings("unchecked")
	public TaskVo[] findWorkItemsFromCaseId (Long caseId) {
		Map paramMap = new HashMap();
		paramMap.put("caseId", caseId);
		paramMap.put("taskStatus", Const.TASK_STATUS_enabled);
	    List list = getSqlMapClientTemplate().queryForList("findTasksBy_StatusAndRootCaseId", paramMap);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
	    
		//if get here found something
	    return (TaskVo[]) list.toArray(new TaskVo[list.size()]); 
		
	}
	
	@SuppressWarnings("unchecked")
	public TaskVo[] findWorkItemsFromRootCaseId (Long rootCaseId) {
		Map paramMap = new HashMap();
		paramMap.put("rootCaseId", rootCaseId);
		paramMap.put("taskStatus", Const.TASK_STATUS_enabled);
	    List<TaskVo> list = getSqlMapClientTemplate().queryForList("findTasksBy_StatusAndRootCaseId", paramMap);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TaskVo[list.size()]); 
		
	}
	
	public TaskVo[] findTimeoutActivities() {
		
		@SuppressWarnings("unchecked")
	    List<TaskVo> list = getSqlMapClientTemplate().queryForList("findTimeoutActivities", null);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TaskVo[list.size()]); 
		
	}
	
	public TaskVo[] findActivitiesAwaitingRetry() {
		@SuppressWarnings("unchecked")
	    List<TaskVo> list = getSqlMapClientTemplate().queryForList("findActivitiesAwaitingRetry", null);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TaskVo[list.size()]); 
		
	}
	
	public TaskVo[] findActivitiesForStartup () {
		@SuppressWarnings("unchecked")
	    List<TaskVo> list = getSqlMapClientTemplate().queryForList("findActivitiesForStartup", null);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TaskVo[list.size()]); 
		
	}
	
	public TaskVo[] findAutoWorkItemsForStartup () {
		@SuppressWarnings("unchecked")
	    List<TaskVo> list = getSqlMapClientTemplate().queryForList("findAutoWorkItemsForStartup", null);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TaskVo[list.size()]); 
		
	}

	public TaskVo[] findOverdueTimedWorkItemsForStartup () {
		@SuppressWarnings("unchecked")
	    List<TaskVo> list = getSqlMapClientTemplate().queryForList("findOverdueTimedWorkItemsForStartup", null);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TaskVo[list.size()]); 
		
	}
	
	public TaskVo[] findNotOverdueExplicitTimedWorkItemsForStartup() {
		@SuppressWarnings("unchecked")
	    List<TaskVo> list = getSqlMapClientTemplate().queryForList("findNotOverdueExplicitTimedWorkItemsForStartup", null);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
		
		//if get here found something
	    return list.toArray(new TaskVo[list.size()]); 

	}
	
	@SuppressWarnings("unchecked")
	public TaskVo[] findActivitiesFromCaseId (Long caseId) {
		Map paramMap = new HashMap();
		paramMap.put("caseId", caseId);
		paramMap.put("taskStatus", Const.TASK_STATUS_in_progress);
	    List<TaskVo> list = getSqlMapClientTemplate().queryForList("findTasksBy_StatusAndRootCaseId", paramMap);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TaskVo[list.size()]); 
		
	}

	@SuppressWarnings("unchecked")
	public TaskVo[] findActivitiesFromRootCaseId (Long rootCaseId) {
		Map paramMap = new HashMap();
		paramMap.put("rootCaseId", rootCaseId);
		paramMap.put("taskStatus", Const.TASK_STATUS_in_progress);
	    List<TaskVo> list = getSqlMapClientTemplate().queryForList("findTasksBy_StatusAndRootCaseId", paramMap);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TaskVo[list.size()]); 
		
	}
	
	/**
	 * Finds all Token for a particular Case
	 * @param caseId the caseId
	 */
	@SuppressWarnings("unchecked")
	public TokenPlaceRefVo[] findTokenPlaceRefVosByCaseId(Long caseId, String tokenStatus) {
		Map paramMap = new HashMap();
		paramMap.put("caseId", caseId);
		paramMap.put("tokenStatus", tokenStatus);
	    List<TokenPlaceRefVo> list = getSqlMapClientTemplate().queryForList("findTokenPlaceRefByCaseId", paramMap);

	    if (null == list || list.isEmpty() ) {
	    	return new TokenPlaceRefVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TokenPlaceRefVo[list.size()]); 
	
	}
	
	@SuppressWarnings("unchecked")
	public TokenVo[] findFreeTokensThatEnabledTask(TaskVo taskVo) {
		Map paramMap = new HashMap();
		paramMap.put("taskId", taskVo.getTaskId());
		paramMap.put("tokenStatus", Const.TOKEN_STATUS_free);

	    List<TokenVo> list = getSqlMapClientTemplate().queryForList("findTokensThatEnabledTask", paramMap);

	    if (null == list || list.isEmpty() ) {
	    	return new TokenVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TokenVo[list.size()]); 
		
	}
	
	public CaseVo[] findSubCasesByParentId(Long caseId) {
		@SuppressWarnings("unchecked")
	    List<CaseVo> list = getSqlMapClientTemplate().queryForList("getSubCasesByParentId", caseId);

	    if (null == list || list.isEmpty() ) {
	    	return new CaseVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new CaseVo[list.size()]); 
	}
	
	/**
	 * Recursively finds all subcaes from the rootId
	 * @return
	 */
	public CaseVo[] getAllSubCasesFromRootId(Long rootCaeseId) {
		@SuppressWarnings("unchecked")
	    List<CaseVo> list = getSqlMapClientTemplate().queryForList("getAllSubCasesFromRootId", rootCaeseId);

	    if (null == list || list.isEmpty() ) {
	    	return new CaseVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new CaseVo[list.size()]); 
		
	}

	public CaseVo[] findAllCasesDueToExpireByCronExp(String cronExp) {
		@SuppressWarnings("unchecked")
	    List<CaseVo> list = getSqlMapClientTemplate().queryForList("findAllCasesDueToExpireByCronExp", cronExp);

	    if (null == list || list.isEmpty() ) {
	    	return new CaseVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new CaseVo[list.size()]); 
	}

	public TaskVo[] findAllWorkItemsDueToStartByCronExp(String cronExp) {
		@SuppressWarnings("unchecked")
	    List<TaskVo> list = getSqlMapClientTemplate().queryForList("findAllWorkItemsDueToStartByCronExp", cronExp);

	    if (null == list || list.isEmpty() ) {
	    	return new TaskVo[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new TaskVo[list.size()]); 
	}
	
	public String[] findAllExistingOpenCaseCronExp() {
		@SuppressWarnings("unchecked")
	    List<String> list = getSqlMapClientTemplate().queryForList("findAllExistingOpenCaseCronExp", null);

	    if (null == list || list.isEmpty() ) {
	    	return new String[0];
	    	
	    }
	    
		//if get here found something
	    return list.toArray(new String[list.size()]); 
		
	}
	
	//===========================================================
	// Get set method used in the Spring framework
	//===========================================================
	private DataFieldMaxValueIncrementer caseSequenceDao;
	private DataFieldMaxValueIncrementer taskSequenceDao;
	private DataFieldMaxValueIncrementer tokenSequenceDao;
	private DataFieldMaxValueIncrementer eventAuditSequenceDao;
	


	public void setCaseSequenceDao(DataFieldMaxValueIncrementer caseSequenceDao) {
		this.caseSequenceDao = caseSequenceDao;
	}
	
	public void setEventAuditSequenceDao(
			DataFieldMaxValueIncrementer eventAuditSequenceDao) {
		this.eventAuditSequenceDao = eventAuditSequenceDao;
	}


	public void setTokenSequenceDao(DataFieldMaxValueIncrementer tokenSequenceDao) {
		this.tokenSequenceDao = tokenSequenceDao;
	}

	public void setTaskSequenceDao(
			DataFieldMaxValueIncrementer taskSequenceDao) {
		this.taskSequenceDao = taskSequenceDao;
	}
	
}
