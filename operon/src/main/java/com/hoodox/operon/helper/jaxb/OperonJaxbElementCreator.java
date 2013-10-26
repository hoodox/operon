package com.hoodox.operon.helper.jaxb;

import static com.hoodox.operon.helper.Const.TRANSITION_TYPE_AND_join;
import static com.hoodox.operon.helper.Const.TRANSITION_TYPE_AND_join_XOR_split;
import static com.hoodox.operon.helper.Const.TRANSITION_TYPE_AND_split;
import static com.hoodox.operon.helper.Const.TRANSITION_TYPE_AND_split_join;
import static com.hoodox.operon.helper.Const.TRANSITION_TYPE_XOR_join;
import static com.hoodox.operon.helper.Const.TRANSITION_TYPE_XOR_join_AND_split;
import static com.hoodox.operon.helper.Const.TRANSITION_TYPE_XOR_split;
import static com.hoodox.operon.helper.Const.TRANSITION_TYPE_XOR_split_join;
import static com.hoodox.operon.helper.Const.TRANSITION_TYPE_ignore;
import static com.hoodox.operon.helper.Const.TRANSITION_TYPE_normal;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_AND_join;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_AND_join_XOR_split;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_AND_split;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_AND_split_join;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_join;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_join_AND_split;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_split;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_split_join;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_subprocess;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_TRIGGER_TYPE_message;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_TRIGGER_TYPE_resource;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_TRIGGER_TYPE_time;

import java.math.BigInteger;

import org.apache.commons.lang.StringUtils;

import com.hoodox.operon.actions.NullAction;
import com.hoodox.operon.jaxb.opnml.NetToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.OperonResourcesType;
import com.hoodox.operon.jaxb.opnml.OperonTimeDurationExplicitType;
import com.hoodox.operon.jaxb.opnml.OperonTimeDurationImplicitType;
import com.hoodox.operon.jaxb.opnml.OperonTimeDurationType;
import com.hoodox.operon.jaxb.opnml.OperonTriggerAutoType;
import com.hoodox.operon.jaxb.opnml.OperonTriggerTimeType;
import com.hoodox.operon.jaxb.opnml.OperonTriggerWithResourcesType;
import com.hoodox.operon.jaxb.opnml.TransitionToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.OperonTimeDurationImplicitType.SchedulerToUse;

/**
 * Helper class to create default elements
 * @author huac
 *
 */
public class OperonJaxbElementCreator {

	public static final String DEFAULT_actionClass = NullAction.class.getName();
	public static final String DEFAULT_delay_duration = "0-0-0-30";
	public static final String DEFAULT_execution_duration = "0-0-10-0";
	public static final String DEFAULT_resource_id = "TODO";
	public static final String DEFAULT_case_time_to_live = "365-0-0-0";
	public static final String DEFAULT_tool_name ="Operon";
	public static final String DEFAULT_tool_version="1.0";
	public static final String DEFAULT_resourceMangerClass="com.hoodox.operon.resources.TestResourceManager";
	public static final String DEFUALT_cron_expression = "0 0/5 * * * ?";

	
	public static final TransitionToolspecificMergeType createTransitionToolspecific() {
		return createTransitionToolspecific(DEFAULT_tool_name, DEFAULT_tool_version);
	}
	
	public static final TransitionToolspecificMergeType createTransitionToolspecific(String tool, String version) {
		TransitionToolspecificMergeType toolspecific = new TransitionToolspecificMergeType();
		toolspecific.setVersion(version);
		toolspecific.setTool(tool);
		
		
		toolspecific.setTask(_createTask());
		
		return toolspecific; 
	}
	
	public static final void setTransitionTypeByWopedTransitionOperator(int wopedTransitionType, TransitionToolspecificMergeType transitionToolspecific) {
		transitionToolspecific.setTransitionType(_createTransitionTypeByWopedTransitionOperator(wopedTransitionType));
	}

	public static final void setTriggerTypeByWopedTriggerType(int wopedTriggerType, TransitionToolspecificMergeType transitionToolspecific) {
		if (TRANSITION_TRIGGER_TYPE_message == wopedTriggerType) {
			if (null == transitionToolspecific.getMessageTrigger()) {
				// make sure we do not override existing value if it exist
				transitionToolspecific.setMessageTrigger(_createOperonTriggerWithResourcesType());				
			}
			transitionToolspecific.setManualTrigger(null);
			transitionToolspecific.setAutoTrigger(null);
			transitionToolspecific.setTimeTrigger(null);

		} else if (TRANSITION_TRIGGER_TYPE_resource == wopedTriggerType) {
			transitionToolspecific.setMessageTrigger(null);			
			if (null == transitionToolspecific.getManualTrigger()) {
				transitionToolspecific.setManualTrigger(_createOperonTriggerWithResourcesType());
				
			}
			transitionToolspecific.setAutoTrigger(null);
			transitionToolspecific.setTimeTrigger(null);

		} else if (TRANSITION_TRIGGER_TYPE_time == wopedTriggerType) {
			transitionToolspecific.setMessageTrigger(null);
			transitionToolspecific.setManualTrigger(null);
			transitionToolspecific.setAutoTrigger(null);			
			if (null == transitionToolspecific.getTimeTrigger()) {
				transitionToolspecific.setTimeTrigger(_createOperonTriggerTimeType());
				
			}

		} else {
			transitionToolspecific.setMessageTrigger(null);
			transitionToolspecific.setManualTrigger(null);			
			if (null == transitionToolspecific.getAutoTrigger()) {
				transitionToolspecific.setAutoTrigger(_createOperonTriggerAutoType());
				
			}			
			transitionToolspecific.setTimeTrigger(null);

		}
		
	}
	
	private static TransitionToolspecificMergeType.TransitionType _createTransitionTypeByWopedTransitionOperator(int wopedTransitionType) {
		TransitionToolspecificMergeType.TransitionType transitionType = new TransitionToolspecificMergeType.TransitionType();
		
		
		if (TRANSITION_OPERATOR_AND_join == wopedTransitionType) {
			transitionType.setType(TRANSITION_TYPE_AND_join);
		
		} else if (TRANSITION_OPERATOR_AND_join_XOR_split == wopedTransitionType) {
			transitionType.setType(TRANSITION_TYPE_AND_join_XOR_split);
			

		} else if (TRANSITION_OPERATOR_AND_split == wopedTransitionType) {
			transitionType.setType(TRANSITION_TYPE_AND_split);

		} else if (TRANSITION_OPERATOR_AND_split_join == wopedTransitionType) {
			transitionType.setType(TRANSITION_TYPE_AND_split_join);

		} else if (TRANSITION_OPERATOR_XOR_join == wopedTransitionType) {
			transitionType.setType(TRANSITION_TYPE_XOR_join);

		} else if (TRANSITION_OPERATOR_XOR_join_AND_split == wopedTransitionType) {
			transitionType.setType(TRANSITION_TYPE_XOR_join_AND_split);

		} else if (TRANSITION_OPERATOR_XOR_split == wopedTransitionType) {
			transitionType.setType(TRANSITION_TYPE_XOR_split);

		} else if (TRANSITION_OPERATOR_XOR_split_join == wopedTransitionType) {
			transitionType.setType(TRANSITION_TYPE_XOR_split_join);

		} else if (TRANSITION_OPERATOR_subprocess == wopedTransitionType) {
			transitionType.setType(TRANSITION_TYPE_ignore);
			
		} else {
			transitionType.setType(TRANSITION_TYPE_normal);

		}
		
		return transitionType;
		
	}
	
	private static OperonTriggerAutoType _createOperonTriggerAutoType() {
		OperonTriggerAutoType trigger = new OperonTriggerAutoType();
		trigger.setExecutionTimeLimit(new OperonTimeDurationType());
		trigger.getExecutionTimeLimit().setDuration(DEFAULT_execution_duration);
		return trigger;
	}

	private static OperonTriggerTimeType _createOperonTriggerTimeType() {
		OperonTriggerTimeType trigger = new OperonTriggerTimeType();
		trigger.setExecutionTimeLimit(new OperonTimeDurationType());
		trigger.getExecutionTimeLimit().setDuration(DEFAULT_execution_duration);
		trigger.setExplicitTriggerDelayDuration(new OperonTimeDurationExplicitType());
		trigger.getExplicitTriggerDelayDuration().setDuration(DEFAULT_delay_duration);		
		return trigger;
	}

	private static OperonTriggerWithResourcesType _createOperonTriggerWithResourcesType() {
		OperonTriggerWithResourcesType trigger = new OperonTriggerWithResourcesType();
		trigger.setResources(new OperonResourcesType());
		trigger.getResources().setDefaultId(DEFAULT_resource_id);		
		return trigger;
	}
	
	private static TransitionToolspecificMergeType.Task _createTask() {
		TransitionToolspecificMergeType.Task task = new TransitionToolspecificMergeType.Task();
		
		task.setEstimatedCompletionTime(new OperonTimeDurationType());
		task.getEstimatedCompletionTime().setDuration(DEFAULT_execution_duration);
		
		task.setPriorityWeighting(BigInteger.ONE);
		
		task.getAction().add(DEFAULT_actionClass);
		
		return task;

	}
	
	public static final NetToolspecificMergeType createNetToolspecificMergeType(String tool, String version) {
		NetToolspecificMergeType toolspecific = new NetToolspecificMergeType();
		toolspecific.setVersion(version);
		toolspecific.setTool(tool);
		
		return setNetToolspecificMergeType(toolspecific);
		
	}
	
	public static final NetToolspecificMergeType setNetToolspecificMergeType(NetToolspecificMergeType toolspecific) {
		
		if (StringUtils.isEmpty(toolspecific.getResourceManagerInterface()) ) {
			toolspecific.setResourceManagerInterface(DEFAULT_resourceMangerClass);
			
		}
		
		if (null == toolspecific.getImplicitTimeToLive()) {
			toolspecific.setImplicitTimeToLive(new OperonTimeDurationImplicitType());
			
		}

		if (StringUtils.isEmpty(toolspecific.getImplicitTimeToLive().getDuration())) {
			toolspecific.getImplicitTimeToLive().setDuration(DEFAULT_case_time_to_live);
			
		}

		if (null == toolspecific.getSchedulerRegistry()) {
			toolspecific.setSchedulerRegistry(new NetToolspecificMergeType.SchedulerRegistry());
			
		}
		
		if (toolspecific.getSchedulerRegistry().getScheduler().isEmpty()) {
			NetToolspecificMergeType.SchedulerRegistry.Scheduler scheduler = new NetToolspecificMergeType.SchedulerRegistry.Scheduler();
			scheduler.setCronTriggerExpression(DEFUALT_cron_expression);
			scheduler.setDescription("Every 5 mins");
			scheduler.setId("Every5Mins");
			toolspecific.getSchedulerRegistry().getScheduler().add(scheduler);
			
		}
		
		if ( (null == toolspecific.getImplicitTimeToLive()) 
				&& (null == toolspecific.getExplicitTimeToLive()) ) {
			SchedulerToUse schedulerToUse = new SchedulerToUse();
			schedulerToUse.setRef(toolspecific.getSchedulerRegistry().getScheduler().get(0));
			toolspecific.getImplicitTimeToLive().getSchedulerToUse().add(schedulerToUse);
			
		}
		
		
		return toolspecific;
		
	}
	
}
