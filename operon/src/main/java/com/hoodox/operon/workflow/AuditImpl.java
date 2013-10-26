package com.hoodox.operon.workflow;

import java.util.Date;

import com.hoodox.commons.exceptions.BaseAppException;
import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.IBaseException;
import com.hoodox.operon.persistence.iface.CaseDao;
import com.hoodox.operon.resourceiface.Resource;
import com.hoodox.operon.valueobjects.CaseVo;
import com.hoodox.operon.valueobjects.EventAuditVo;
import com.hoodox.operon.valueobjects.TaskVo;

public class AuditImpl implements Audit {


	
	public void auditErrorWithNoStatusChange(CaseVo caseVo, TaskVo taskVo, String currentStatus, Throwable ex, Resource resource, String event, CaseDao caseDao) {
		IBaseException ibaseEx = null;
		if ( !(ex instanceof IBaseException) ) {
			if (ex instanceof RuntimeException) {
				ibaseEx = new BaseSystemException(ex);
			} else {
				ibaseEx = new BaseAppException(ex);
			}
		} else {
			ibaseEx = (IBaseException) ex;
		}
		
		//=====================
		// Logged in its own transaction
		//====================
		AuditImpl._auditEvent(caseVo, taskVo,resource, event, currentStatus, currentStatus, ibaseEx, caseDao );
		
	}
	
	/**
	 * Audits the events in using an existing transaction
	 */
	public void auditEvent(CaseVo caseVo, TaskVo taskVo, Resource resource, String event, String initialStatus, String finalStatus, IBaseException iBaseException, CaseDao caseDao) {
		AuditImpl._auditEvent(caseVo, taskVo, resource, event, initialStatus, finalStatus, iBaseException, caseDao);
	}
	

	public static void _auditEvent(CaseVo caseVo, TaskVo taskVo,
			Resource resource, String event, String initialStatus,
			String finalStatus, IBaseException iBaseException, CaseDao caseDao) {
		
		EventAuditVo eventAuditVo = new EventAuditVo();
		eventAuditVo.setCreatedDate(new Date());
		eventAuditVo.setResourceId(resource.getId());
		
		if (null != taskVo) {
			eventAuditVo.setTaskId(taskVo.getTaskId());
			eventAuditVo.setCaseId(taskVo.getCaseVo().getCaseId());
			
		} else {
			eventAuditVo.setCaseId(caseVo.getCaseId());
			
		}
		
		eventAuditVo.setEvent(event);
		eventAuditVo.setInitialStatus(initialStatus);
		eventAuditVo.setFinalStatus(finalStatus);
		
		if (iBaseException != null) {
			eventAuditVo.setSuccessInd(false);
			eventAuditVo.setErrorCode(iBaseException.getErrorCode());
			if ((iBaseException.getMessage() != null) && iBaseException.getMessage().length() > 500 ) {
				eventAuditVo.setErrorDetail(iBaseException.getMessage().substring(0, 500));
				
			}else {
				eventAuditVo.setErrorDetail(iBaseException.getMessage());
				
			}
			
		} else {
			eventAuditVo.setSuccessInd(true);
			
		}
		
		caseDao.addEventAudit(eventAuditVo);

	}
	
}
