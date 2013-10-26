package com.hoodox.operon.workflow;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hoodox.commons.exceptions.IBaseException;
import com.hoodox.operon.persistence.iface.CaseDao;
import com.hoodox.operon.resourceiface.Resource;
import com.hoodox.operon.valueobjects.CaseVo;
import com.hoodox.operon.valueobjects.TaskVo;

@Transactional(readOnly=true)
public interface Audit {
	
	/**
	 * Event is audited in its own transaction
	 * @param caseVo
	 * @param workItemVo
	 * @param resource
	 * @param event
	 * @param initialStatus
	 * @param finalStatus
	 * @param iBaseException
	 * @param caseDao
	 */
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)	
	public void auditEvent(CaseVo caseVo, TaskVo workItemVo, Resource resource, String event, String initialStatus, String finalStatus, IBaseException iBaseException, CaseDao caseDao);	
	
	/**
	 * Audit event with no status change by using the current status
	 * @param caseVo
	 * @param workItemVo
	 * @param currentStatus
	 * @param ex
	 * @param audit
	 * @param resource
	 * @param event
	 * @param caseDao
	 */
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)	
	public void auditErrorWithNoStatusChange(CaseVo caseVo, TaskVo workItemVo, String currentStatus, Throwable ex, Resource resource, String event, CaseDao caseDao);
	
	
}
