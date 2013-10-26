package com.hoodox.operon.wfnet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hoodox.operon.wfnet.toolspecific.Scheduler;
import com.hoodox.operon.wfnet.toolspecific.TimeDuration;

public class RootCaseType extends CaseType {

	private static final long serialVersionUID = 457862825305905945L;

	private String resourceManagerInterface;
	
	/**
	 * Only root case has this
	 */
	private TimeDuration timeToLive;

	/**
	 * Only root case has this
	 */
	private Map<String, Scheduler> schedulerRegistryMap = new HashMap<String, Scheduler>();

	public RootCaseType() {
		super();
	}
	
	public String getResourceManagerInterface() {
		return resourceManagerInterface;
	}

	public void setResourceManagerInterface(String resourceManagerInterface) {
		this.resourceManagerInterface = resourceManagerInterface;
	}

	public TimeDuration getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(TimeDuration timeToLive) {
		this.timeToLive = timeToLive;
	}

	public Scheduler[] getAllSchedulers() {
		Collection<Scheduler> col = schedulerRegistryMap.values();
		if (col == null || col.isEmpty()) {
			return new Scheduler[0];
		}
		ArrayList<Scheduler> list = new ArrayList<Scheduler>(col);
		return list.toArray(new Scheduler[list.size()]);
	}

	public void setSchedulers(Scheduler[] schedulers) {
		this.schedulerRegistryMap = new HashMap<String, Scheduler>();
		for (int i=0; i<schedulers.length; i++) {
			this.schedulerRegistryMap.put(schedulers[i].getId(), schedulers[i]);
			
		}
	}
	
	public Scheduler getSchedulerById(String id) {
		return schedulerRegistryMap.get(id);
	}

	public void addScheduler(Scheduler scheduler) {
		schedulerRegistryMap.put(scheduler.getId(), scheduler);
	}
	

}
