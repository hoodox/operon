package com.hoodox.operon.valueobjects;

import java.io.Serializable;
//import java.text.SimpleDateFormat;
//import java.util.Arrays;
import java.util.Date;

/**
 * WorkItem value object
 * @author HUAC
 *
 */
@SuppressWarnings("unchecked")
public class TaskVo implements Comparable, Serializable {

	private static final long serialVersionUID = 1025011679047850378L;
	private Long taskId;
	private String taskStatus;
	private Date triggerTime;
	private String wfnetTransitionRef;
	private Date inProgressTimeout;
	private Long retryCount;
	private Boolean startAtStartup = Boolean.FALSE;
	private Long lockVersion;
	private Date updatedDate;
	private Date createdDate;
	private Integer priorityWeighting = new Integer(1); //default
	private Date expectedCompletionDate;
	private Date actualCompletionDate;
	private long currentPriorityRating = 0;
	
	private CaseVo caseVo = new CaseVo();
		
	public void reCalculateCurrentPriorityRating() {
		this.currentPriorityRating = (System.currentTimeMillis() - this.createdDate.getTime()) * this.priorityWeighting.longValue(); 
		
	}
	
	public long getCurrentPriorityRating() {
		if (this.currentPriorityRating == 0) {
			this.currentPriorityRating = (System.currentTimeMillis() - this.createdDate.getTime()) * this.priorityWeighting.longValue(); 

		}
		
		return this.currentPriorityRating;
					
	}

	
	public Date getExpectedCompletionDate() {
		return expectedCompletionDate;
	}

	public void setExpectedCompletionDate(Date expectedCompletionDate) {
		this.expectedCompletionDate = expectedCompletionDate;
	}
	
	
	public Date getActualCompletionDate() {
		return actualCompletionDate;
	}

	public void setActualCompletionDate(Date actualCompletionDate) {
		this.actualCompletionDate = actualCompletionDate;
	}

	public Integer getPriorityWeighting() {
		return priorityWeighting;
	}

	public void setPriorityWeighting(Integer priorityWeighting) {
		this.priorityWeighting = priorityWeighting;
	}

	public Boolean getStartAtStartup() {
		return startAtStartup;
	}

	public void setStartAtStartup(Boolean startAtStartup) {
		this.startAtStartup = startAtStartup;
	}

	public TaskVo() {
		super();
		// TODO Auto-generated constructor stub
	}

//	public Long getCaseId() {
//		return caseVo.getCaseId();
//	}
//
//	public void setCaseId(Long caseId) {
//		this.caseVo.setCaseId(caseId);
//	}

	public Date getInProgressTimeout() {
		return inProgressTimeout;
	}

	public void setInProgressTimeout(Date inProgressTimeout) {
		this.inProgressTimeout = inProgressTimeout;
	}

	public Long getLockVersion() {
		return lockVersion;
	}

	public void setLockVersion(Long lockVersion) {
		this.lockVersion = lockVersion;
	}

	public Long getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Long retryCount) {
		this.retryCount = retryCount;
	}

	public Date getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(Date triggerTime) {
		this.triggerTime = triggerTime;
	}

	public String getWfnetTransitionRef() {
		return wfnetTransitionRef;
	}

	public void setWfnetTransitionRef(String wfnetTransitionRef) {
		this.wfnetTransitionRef = wfnetTransitionRef;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public CaseVo getCaseVo() {
		return caseVo;
	}

	public void setCaseVo(CaseVo caseVo) {
		this.caseVo = caseVo;
	}
	
	public int compareTo(Object anotherTaskVo) throws ClassCastException {
		TaskVo task2 = (TaskVo) anotherTaskVo;
		
		long task1Val = this.getCurrentPriorityRating();
		long task2Val = task2.getCurrentPriorityRating();
		
		if (task2Val > task1Val) {
			return 1;
		}

		if (task2Val < task1Val) {
			return -1;
		}
		
		return 0;
		
	}
	
//	public static void main(String[] args) {
//		TaskVo[] taskVos = new TaskVo[5];
//		
//		taskVos[0] = new TaskVo();
//		taskVos[0].setCreatedDate(new Date(500));
//		taskVos[0].setPriorityWeighting(new Integer(1));
//		taskVos[0].setTaskId(new Long(1));
//		
//		taskVos[1] = new TaskVo();
//		taskVos[1].setCreatedDate(new Date(new Long("5757017521910").longValue()));
//		taskVos[1].setPriorityWeighting(new Integer(2));
//		taskVos[1].setTaskId(new Long(2));
//
//		taskVos[2] = new TaskVo();
//		taskVos[2].setCreatedDate(new Date());
//		taskVos[2].setPriorityWeighting(new Integer(3));
//		taskVos[2].setTaskId(new Long(3));
//		
//		taskVos[3] = new TaskVo();
//		taskVos[3].setCreatedDate(new Date(500));
//		taskVos[3].setPriorityWeighting(new Integer(4));
//		taskVos[3].setTaskId(new Long(4));
//		
//		taskVos[4] = new TaskVo();
//		taskVos[4].setCreatedDate(new Date(500));
//		taskVos[4].setPriorityWeighting(new Integer(5));
//		taskVos[4].setTaskId(new Long(5));
//		
//		SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
//		
//		System.out.println("Sequence before");		
//		for (int i=0; i<taskVos.length; i++) {
//			System.out.println("i=" + i + " taskId=" + taskVos[i].getTaskId() 
//					+ " priorityWeighting=" + taskVos[i].getPriorityWeighting() 
//					+ " currentPriority=" + taskVos[i].getCurrentPriorityRating() 
//					+ " createdDate=" + df.format(taskVos[i].getCreatedDate())
//					+ " priorityAsDate=" + df.format(new Date(taskVos[i].getCurrentPriorityRating())));
//		}
//		System.out.println("");
//		
//		System.out.println("Sequence After sorting");
//		Arrays.sort(taskVos);
//		for (int i=0; i<taskVos.length; i++) {
//			System.out.println("i=" + i + " taskId=" + taskVos[i].getTaskId() 
//					+ " priorityWeighting=" + taskVos[i].getPriorityWeighting() 
//					+ " currentPriority=" + taskVos[i].getCurrentPriorityRating() 
//					+ " createdDate=" + df.format(taskVos[i].getCreatedDate())
//					+ " priorityAsDate=" + df.format(new Date(taskVos[i].getCurrentPriorityRating())));
//		}
//		
//	}
}
