package com.hoodox.operon.helper.jaxb.woped;

import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TOOLSPECIFIC_NAME_Woped;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_join;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_split;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;
import com.hoodox.operon.exceptions.OperonConfigException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.helper.jaxb.OpnmlJAXBHelper;
import com.hoodox.operon.jaxb.opnml.TransitionToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.TransitionType;

/**
 * Class that holds the group of Transition of type OR-Split and XOR-Join group by
 * there Operator id.
 * 
 * @author huac
 *
 */
public class WopedDuplicateTransitionHolder {
	private Map<String, List<TransitionType>> map = new HashMap<String, List<TransitionType>>();
	
			
	/**
	 * @param transition
	 */
	public void addTransition (TransitionType transition) {
    	TransitionToolspecificMergeType transitionToolSpecific = 
    		(TransitionToolspecificMergeType) OpnmlJAXBHelper.getToolspecificTypeByName(TOOLSPECIFIC_NAME_Woped, transition.getToolspecific());
    	
    	String operatorTransId = transitionToolSpecific.getOperator().getId();
    	List<TransitionType> aTransList = this.map.get(operatorTransId);
    	if (null == aTransList) {
    		// create a new one
    		aTransList = new ArrayList<TransitionType>();
    		this.map.put(operatorTransId, aTransList);
    	}
    	
		aTransList.add(transition);
    	
		
	}
	
	/**
	 * 
	 * @return a the groupKey of the duplicate transitions
	 */
	public List<String> getGroupKeys () {
		return new ArrayList<String>(this.map.keySet());
	}
	
	/**
	 * Returns a list of all Duplicate Transitions we want to remove by the keygroupId.
	 * 
	 * This will create a new list and return everything apart from transition with
	 * id "<id>_op_1". We will keep all "<id>_op1"
	 * 
	 * @param keepId
	 * @return
	 */
	public List<TransitionType> getRemoveTransitionList(String groupId) {
		List<TransitionType> existingList = this.map.get(groupId);
		
		if (!(existingList.size()>1)) {
			//should never really happen
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Something wrong with this woped net, there exists a transition group id " 
					+ groupId  
					+ " which should contain duplicate operator types of OR-Split=" 
					+ TRANSITION_OPERATOR_XOR_split 
					+ " or XOR-Join=" + TRANSITION_OPERATOR_XOR_join
					+ " but it does not.");
			throw ex;
		}
		
		
		TransitionType keep = this.getKeepTransition(groupId);
		
		List<TransitionType> removeList = new ArrayList<TransitionType>();
		for (TransitionType aTran: existingList) {
			if (aTran != keep) {
				removeList.add(aTran);
			}
		}
		
		return removeList;
		
	}

	/**
	 * Gets a keep Transition the duplicates that we want to remove.
	 * The transactionId to keep is "<groupId>_op_1".
	 * @param groupId
	 * @return the Transition we want to keep
	 */
	public TransitionType getKeepTransition(String groupId) {
		
		List<TransitionType> existingList = this.map.get(groupId);

		if (!(existingList.size()>1)) {
			//should never really happen
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Something wrong with this woped net, there exists a transition group id " + groupId  
					+ " which should contain duplicate operator types of OR-Split=" 
					+ TRANSITION_OPERATOR_XOR_split 
					+ " or XOR-Join=" + TRANSITION_OPERATOR_XOR_join
					+ " but it does not.");
			throw ex;
		}

		String keepId = groupId+"_op_1";
		
		for (TransitionType aTran: existingList) {
			if (aTran.getId().equals(keepId)) {
				return aTran;
			}
		}
		
		// if we get here throw error.
		BaseSystemException ex = new OperonConfigException(new ErrorCode(
				Const.ERROR_CODE_operon_net_config_error),
				"Something wrong with this woped net, there exists a transition group id " + groupId  
				+ " which should contain duplicate operator types of OR-Split=" 
				+ TRANSITION_OPERATOR_XOR_split 
				+ " or XOR-Join=" + TRANSITION_OPERATOR_XOR_join
				+ " but we cannot find the transition id="+keepId 
				+ " that we want to keep so that we can remove the duplicates");
		throw ex;

		
	}
}

