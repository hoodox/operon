// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 08/03/2006 12:46:55
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   TimeDuration.java

package com.hoodox.operon.wfnet.toolspecific;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;
import com.hoodox.operon.exceptions.OperonConfigException;
import com.hoodox.operon.helper.Const;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

public class TimeDuration implements Serializable {

	public TimeDuration() {
		explicit = true;
	}

	public long getDurationInMilliSecs() {
		return this.durationInSecs * 1000;
	}
	
	public void setDurationExpression(String durationExpression) {
		if (StringUtils.isBlank(durationExpression)) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Trying to set the duration expression for the timeDuration but it is blank. Please check the net configuration files");
			throw ex;
			
		}
		
		StringTokenizer st = new StringTokenizer(durationExpression.trim(), "-");
		if (st.countTokens() < 4) {
			com.hoodox.commons.exceptions.BaseSystemException ex = new OperonConfigException(
					new ErrorCode("operon_config_error"),
					"Time duration is not valid, it must be in format of days-hours-mins-seconds :");
			throw ex;
		}

		long days = (new Long(st.nextToken())).longValue() * 24L * 60L * 60L;
		long hours = (new Long(st.nextToken())).longValue() * 60L * 60L;
		long mins = (new Long(st.nextToken())).longValue() * 60L;
		long secs = (new Long(st.nextToken())).longValue();
		this.durationInSecs = days + hours + mins + secs;
		
	}

	public boolean isExplicit() {
		return explicit;
	}

	public void setExplicit(boolean bool) {
		this.explicit = bool;
	}

	public Scheduler[] getSchedulerToUseRefs() {
		return this.schedulerToUseRefList.toArray(new Scheduler[this.schedulerToUseRefList.size()]);
	}

	public void setSchedulerToUseRefs(Scheduler[] schedulerToUseRefs) {		
		this.schedulerToUseRefList = Arrays.asList(schedulerToUseRefs);
	}

	private static final long serialVersionUID = 0xdcc5fdc9ad49fa40L;

	private boolean explicit;

	private long durationInSecs;

	private List<Scheduler> schedulerToUseRefList = new ArrayList<Scheduler>();
}