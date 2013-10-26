package com.hoodox.operon.helper.jaxb.woped;

import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TOOLSPECIFIC_NAME_Woped;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_AND_join_XOR_split;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_join;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_join_AND_split;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_split_join;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_subprocess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;
import com.hoodox.operon.exceptions.OperonConfigException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.helper.jaxb.OperonJaxbElementCreator;
import com.hoodox.operon.helper.jaxb.OpnmlJAXBHelper;
import com.hoodox.operon.jaxb.opnml.ArcToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.ArcType;
import com.hoodox.operon.jaxb.opnml.NetToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.NetType;
import com.hoodox.operon.jaxb.opnml.PageType;
import com.hoodox.operon.jaxb.opnml.PlaceToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.PlaceType;
import com.hoodox.operon.jaxb.opnml.ReferencePlaceToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.ToolspecificType;
import com.hoodox.operon.jaxb.opnml.TransitionToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.TransitionType;
import com.hoodox.operon.jaxb.opnml.NetType.Page;
import com.hoodox.operon.wfnet.CaseType;
import com.hoodox.operon.wfnet.Place;

/**
 * <p>
 * Helper to help with merging Operon and Woped JAXB class to parse the
 * pnml-operon.xsd net configuration file.
 * <p>
 * 
 * @author HUAC
 * 
 */
public class WopedJAXBHelper {
	private static final String PLACE_ID_center_place = "CENTER_PLACE_";
	private static final String TOOLSP_PLACE_InReferencePlace = "InReferencePlace";
	private static final String TOOLSP_PLACE_OutReferencePlace = "OutReferencePlace";
	private static final String TOOLSP_PLACE_SinkPlace = "SinkPlace";
	private static final String TOOLSP_PLACE_SourcePlace = "SourcePlace";

	/**
	 * Do some Woped manipulation to prepare for Operon Workflow Engine
	 * 
	 * @param netypes
	 */
	public static void prepareWopedForOperon(NetType[] netypes) {
		for (NetType aNet : netypes) {
			prepareWopedForOperon(aNet);
		}
	}

	/**
	 * Do some Woped manipulation to prepare for Operon Workflow Engine
	 * 
	 * @param netypes
	 */
	public static void prepareWopedForOperon(NetType netType) {
		_cleanupDuplicateArcsAndTransitions(netType);

		// removeDuplicateIds caused by subprocesses
		// Recursively do the page if any

		List<Page> pages = netType.getPage();
		if ((pages != null) && !pages.isEmpty()) {
			// If we get here then remove duplicate ids.
			for (Page aPage : pages) {
				_renameDuplicateArcIdsCausedByWopedSubProcesses(netType, aPage);

			}
		}

	}

	/**
	 * Cleanup the duplicate Arc Ids and rename the
	 * 
	 * @param netType
	 */
	private static void _cleanupDuplicateArcsAndTransitions(NetType netType) {

		WopedDuplicateTransitionHolder duplicateTransHolder = new WopedDuplicateTransitionHolder();

		// 1.
		// Find all Transitions that has the Transition type OR-Split
		// or XOR-Join and add them to the WopedDuplicateTransitionHolder
		List<TransitionType> transLiveList = netType.getTransition();
		for (TransitionType aTrans : transLiveList) {
			TransitionToolspecificMergeType transitionToolSpecific = (TransitionToolspecificMergeType) OpnmlJAXBHelper
					.getToolspecificTypeByName(TOOLSPECIFIC_NAME_Woped, aTrans
							.getToolspecific());

			if ((null != transitionToolSpecific)
					&& (transitionToolSpecific.getOperator() != null)) {
				int operatorType = transitionToolSpecific.getOperator()
						.getType();
				if ((operatorType == TRANSITION_OPERATOR_AND_join_XOR_split)
						|| (operatorType == TRANSITION_OPERATOR_XOR_join)
						|| (operatorType == TRANSITION_OPERATOR_AND_join_XOR_split)
						|| (operatorType == TRANSITION_OPERATOR_XOR_join_AND_split)
						|| (operatorType == TRANSITION_OPERATOR_XOR_split_join)) {
					duplicateTransHolder.addTransition(aTrans);

				}
			}

		}

		List<ArcType> arcLiveList = netType.getArc();

		List<String> duplicateTransitionGroup = duplicateTransHolder
				.getGroupKeys();

		List<TransitionType> totalTransitionToRemoveList = new ArrayList<TransitionType>();

		// 2.
		// for each group update arc references with the keep from the remove
		for (String groupId : duplicateTransitionGroup) {
			TransitionType keep = duplicateTransHolder
					.getKeepTransition(groupId);
			List<TransitionType> aRemoveTransList = duplicateTransHolder
					.getRemoveTransitionList(groupId);
			totalTransitionToRemoveList.addAll(aRemoveTransList);

			_updateArcArcReferencesWithKeepTransition(keep, aRemoveTransList,
					arcLiveList);
		}

		// 3.
		// Find all duplicate arcs from the live list
		WopedDuplicateArcHolder duplicateArcHolder = new WopedDuplicateArcHolder(
				arcLiveList);
		List<ArcType> removeArcList = duplicateArcHolder.getRemoveArcList();
		for (ArcType anArc : removeArcList) {
			arcLiveList.remove(anArc);

		}

		// 4.
		// Remove all duplicate transition from the live list
		for (TransitionType aTrans : totalTransitionToRemoveList) {
			transLiveList.remove(aTrans);

		}

		// 5.
		// Remove all Arcs that reference a CENTER_PLACE
		List<ArcType> removeCenterPlaceArcs = _getCenterPlaceArcs(netType);
		for (ArcType anArc : removeCenterPlaceArcs) {
			arcLiveList.remove(anArc);

		}

		// 6.
		// Remove all CENTER_PLACE places
		List<PlaceType> placeLiveList = netType.getPlace();
		List<PlaceType> removeCenterPlaces = _getCenterPlaces(netType);
		for (PlaceType aPlace : removeCenterPlaces) {
			placeLiveList.remove(aPlace);

		}

		// Recursively do the page if any
		List<Page> pages = netType.getPage();
		if ((pages == null) || pages.isEmpty()) {
			return;
		}

		// If we get here then do the pages

		for (Page aPage : pages) {
			List<NetType> nets = aPage.getNet();

			if ((nets != null) && !nets.isEmpty()) {
				for (NetType aNet : nets) {
					_cleanupDuplicateArcsAndTransitions(aNet);
				}
			}

		}

	}

	/**
	 * Change all remove Arc Transition references with the keep one.
	 */
	private static void _updateArcArcReferencesWithKeepTransition(
			TransitionType keep, List<TransitionType> removeList,
			List<ArcType> arcTypeList) {
		for (TransitionType remove : removeList) {
			_updateArcArcReferencesWithKeepTransition(keep, remove, arcTypeList);
		}
	}

	/**
	 * Change all remove Arc Transition references with the keep one.
	 */
	private static void _updateArcArcReferencesWithKeepTransition(
			TransitionType keep, TransitionType remove,
			List<ArcType> arcTypeList) {
		for (ArcType arc : arcTypeList) {
			TransitionType tran = null;
			if (arc.getSource() instanceof TransitionType) {
				tran = (TransitionType) arc.getSource();
				if (tran.getId().equals(remove.getId())) {
					arc.setSource(keep);
				}

			} else if (arc.getTarget() instanceof TransitionType) {
				tran = (TransitionType) arc.getTarget();
				if (tran.getId().equals(remove.getId())) {
					arc.setTarget(keep);
				}
			}

		}
	}

	/**
	 * See other overloaded method.
	 * 
	 * @param parentNet
	 * @param page
	 */
	private static void _renameDuplicateArcIdsCausedByWopedSubProcesses(
			NetType parentNet, Page page) {

		String oldPageId = page.getId();
		// page.setId("pg_"+oldPageId);

		List<NetType> nets = page.getNet();

		if ((nets != null) && !nets.isEmpty()) {
			for (NetType aNet : nets) {
				_renameDuplicateArcIdsCausedByWopedSubProcesses(parentNet,
						aNet, oldPageId);
			}
		}

	}

	/**
	 * Renames SubProcess (subnet) arc ids to make them unique.
	 * 
	 * Naming convention:
	 * 
	 * 1. New Arc id = a<current_id>
	 * 
	 * Process: 1. Find any arc id in parent that matches arc in subprocess and
	 * change subprocess arc id.
	 * 
	 * Note: No need to rename duplicate place ids as the method
	 * {@link #createSingleWopedReferencePlace()} will handle this and turn it
	 * into a referencePlace.
	 * 
	 * @param parentNet
	 *            the parent NetType
	 * @param net
	 *            the child net
	 * @return a new List of TransitionTypes with OR-Split and OR-Join operators
	 */
	/**
     * 
     */
	private static void _renameDuplicateArcIdsCausedByWopedSubProcesses(
			NetType parentNet, NetType net, String oldPageId) {
		/*
		 * Not really sure if we need this so comment out for now.
		 */
		// List<ArcType> parentArcs = parentNet.getArc();
		// List<ArcType> arcs = net.getArc();
		//		
		// for (ArcType aParentArc : parentArcs) {
		// for (ArcType anArc : arcs) {
		// if (aParentArc.getId().equals(anArc.getId())) {
		// // duplicate IDs found in children (subprocess)
		// // change it to make it unique within net.
		// String currentId = anArc.getId();
		// anArc.setId("a"+currentId);
		// }
		// }
		// }
		//    	
		// // recursively do the other pages.
		// List<Page> pages = net.getPage();
		// if ( (pages != null) && !pages.isEmpty()) {
		//   			
		// for (Page aPage : pages) {
		// _renameDuplicateArcIdsCausedByWopedSubProcesses(net, aPage);
		//   				
		// }
		// }
	}

	/**
	 * Finds all CenterPlace Arcs
	 * 
	 * @param net
	 * @return List of CenterPlace ArcType
	 */
	private static List<ArcType> _getCenterPlaceArcs(NetType net) {
		List<ArcType> centerPlaceArcs = new ArrayList<ArcType>();
		List<ArcType> arcs = net.getArc();
		for (ArcType anArc : arcs) {
			PlaceType aPlace = null;
			if (anArc.getSource() instanceof PlaceType) {
				aPlace = (PlaceType) anArc.getSource();

			} else {
				// target must be a place then
				aPlace = (PlaceType) anArc.getTarget();
			}

			if (aPlace.getId().startsWith(PLACE_ID_center_place)) {
				centerPlaceArcs.add(anArc);
			}

		}

		return centerPlaceArcs;
	}

	/**
	 * Finds all CenterPlaces
	 * 
	 * @param net
	 *            a net
	 * @return List of CenterPlace
	 */
	private static List<PlaceType> _getCenterPlaces(NetType net) {

		List<PlaceType> centerPlaces = new ArrayList<PlaceType>();
		List<PlaceType> places = net.getPlace();
		for (PlaceType aPlace : places) {
			if (aPlace.getId().startsWith(PLACE_ID_center_place)) {
				centerPlaces.add(aPlace);
			}
		}

		return centerPlaces;
	}

	/**
	 * Determines if this is a Woped Subnet Transition. Again Woped messed
	 * things up, as this type of transition is only a place holder in Woped so
	 * we do not need it.
	 * 
	 * @return
	 */
	public static boolean isAWopedSubProcessTransition(
			TransitionType transitionType) {
		TransitionToolspecificMergeType transitionToolspecific = (TransitionToolspecificMergeType) OpnmlJAXBHelper
				.getToolspecificTypeByName(TOOLSPECIFIC_NAME_Woped,
						transitionType.getToolspecific());

		if (null == transitionToolspecific) {
			return false;
		}

		Boolean bool = transitionToolspecific.isSubprocess();

		if ((null == bool)) {
			return false;
		}

		return bool.booleanValue();
	}

	/**
	 * This method may be removed in the future if Woped fixes its bugs.
	 * Populates the Place attributes with the OperonPlaceType place
	 * 
	 * @param place
	 * @param oPlace
	 */
	public static Place createSingleWopedReferencePlace(PlaceType xmlPlace,
			CaseType parentCaseType, PageType xmlPage) {

		// We are only here because we are assuming Woped ReferencePlace
		Place place = new Place();

		// Woped Place Id is not unique,
		// only unique within the net
		place.setId(xmlPlace.getId());

		if (null != xmlPlace.getInitialMarking()) {
			place.setInitialMarking(new Integer(xmlPlace.getInitialMarking()
					.getText()));
		}

		// ==========================
		// Work out the type of place
		// ==========================
		ReferencePlaceToolspecificMergeType refPlaceToolSpecific = (ReferencePlaceToolspecificMergeType) OpnmlJAXBHelper
				.getToolspecificTypeForOperonFromObjectList(xmlPlace
						.getToolspecific());

		if (refPlaceToolSpecific == null) {
			throw new OperonConfigException(
					new ErrorCode("operon_net_config_error"),
					"Cannot register a Woped Place id "
							+ xmlPlace.getId()
							+ " as a ReferencePlace because the ReferencePlace <toolspecific>..<toolspecific/> part for operon is not set. ");
		}

		if (null != refPlaceToolSpecific.getInrefPlace()) {
			place.setType(Const.PLACE_TYPE_inref);
			place.setCreateSubcasesAction(refPlaceToolSpecific.getInrefPlace()
					.getCreateSubcasesAction());

		} else {
			place.setType(Const.PLACE_TYPE_outref);
		}

		// ==========================
		// Get the parent place ref
		// ==========================
		Place parentRefPlace = OpnmlJAXBHelper
				.findParentPlaceOrPlaceWithReferenceToId(xmlPlace.getId(),
						parentCaseType);
		if (null == parentRefPlace) {
			// Cannot have a place linking to another other object other than
			// Transition
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error), "Net "
					+ parentCaseType.getId() + " has a subnet "
					+ xmlPlace.getId() + " has reference place "
					+ place.getId() + " that points to a place "
					+ xmlPlace.getId()
					+ " which is not registered in the parent net");
			throw ex;

		}

		place.setRefPlace(parentRefPlace);
		place.setName(parentRefPlace.getName());

		return place;

	}

	public static void mergeOperonToolSpecificToNet(NetType sourceNet,
			NetType targetNet, NetType defaultOperonToolspecificNet) {
			
		final boolean isSubnet = false;
		mergeOperonToolSpecificToNet(sourceNet, targetNet, defaultOperonToolspecificNet, isSubnet);
				
		
	}

	/**
	 * Merges operonToolspecifics from a source net into a target net. These two
	 * nets should be similar in structure and have the same ids. Scenarios we
	 * would use this would be we have a source net that contains all the
	 * OperonToolspecifics and the target net is a version of the source that we
	 * are modifying in Woped but the target OperonToolspecifics has been
	 * written over by Operon. We need to use the source to merge it back.
	 * 
	 * If the source is null then the defaultOperonToolspecifics are used
	 * instead.
	 * 
	 * @param source
	 * @param target
	 * @param defaultOperonToolspecificNet
	 *            are used instead.
	 * @param isSubnet whether this is s subnet or not.           
	 */
	public static void mergeOperonToolSpecificToNet(NetType sourceNet,
			NetType targetNet, NetType defaultOperonToolspecificNet, boolean isSubnet) {
		List<NetToolspecificMergeType> targetNetToolspecificList = targetNet
				.getToolspecific();
		NetToolspecificMergeType sourceNetToolspecific = (NetToolspecificMergeType) OpnmlJAXBHelper
				.getToolspecificTypeForOperon(sourceNet.getToolspecific());
		NetToolspecificMergeType targetNetToolspecific = (NetToolspecificMergeType) OpnmlJAXBHelper
				.getToolspecificTypeForOperon(targetNet.getToolspecific());
		NetToolspecificMergeType defaultNetToolspecific = (NetToolspecificMergeType) OpnmlJAXBHelper
				.getToolspecificTypeForOperon(defaultOperonToolspecificNet
						.getToolspecific());

		if (!isSubnet) {
			_mergeToolspecificToList(sourceNetToolspecific, targetNetToolspecific,
					defaultNetToolspecific, targetNetToolspecificList,
					defaultOperonToolspecificNet);
			
		}

		// do the places
		List<PlaceType> targetPlaces = targetNet.getPlace();
		List<PlaceType> sourcePlaces = sourceNet.getPlace();
		for (PlaceType targetPlace : targetPlaces) {
			PlaceType sourcePlace = _getPlaceByIdInList(targetPlace.getId(),
					sourcePlaces);
			if (sourcePlace == null) {
				continue;
			}

			// if we get here then found a match
			// will try to get do the merge.

			List<PlaceToolspecificMergeType> targetPlaceToolspecificList = targetPlace
					.getToolspecific();
			PlaceToolspecificMergeType sourcePlaceToolspecific = (PlaceToolspecificMergeType) OpnmlJAXBHelper
					.getToolspecificTypeForOperon(sourcePlace.getToolspecific());
			PlaceToolspecificMergeType targetPlaceToolspecific = (PlaceToolspecificMergeType) OpnmlJAXBHelper
					.getToolspecificTypeForOperon(targetPlace.getToolspecific());
			PlaceToolspecificMergeType defaultPlaceToolspecific = null;
			
			_mergeToolspecificToList(sourcePlaceToolspecific,
					targetPlaceToolspecific, defaultPlaceToolspecific,
					targetPlaceToolspecificList, defaultOperonToolspecificNet);

		}

		// Do the transition
		List<TransitionType> targetTransitions = targetNet.getTransition();
		List<TransitionType> sourceTransitions = sourceNet.getTransition();
		for (TransitionType targetTransition : targetTransitions) {
			TransitionType sourceTransition = _getTransitionByIdInList(
					targetTransition.getId(), sourceTransitions);
			
			TransitionToolspecificMergeType sourceTransitionToolspecific = null;
			
			if (sourceTransition != null) {
				sourceTransitionToolspecific = (TransitionToolspecificMergeType) OpnmlJAXBHelper
				.getToolspecificTypeForOperon(sourceTransition
						.getToolspecific());			}

			// if we get here then found a match
			// will try to get do the merge.

			List<TransitionToolspecificMergeType> targetTransitionToolspecificList = targetTransition
					.getToolspecific();
			TransitionToolspecificMergeType targetTransitionToolspecific = (TransitionToolspecificMergeType) OpnmlJAXBHelper
					.getToolspecificTypeForOperon(targetTransition
							.getToolspecific());
			TransitionToolspecificMergeType targetWopedTransToolSpecific = (TransitionToolspecificMergeType) getToolspecificTypeForWoped(targetTransitionToolspecificList);
			
			_mergeTransitionToolspecificToList(sourceTransitionToolspecific,
					targetTransitionToolspecific,
					targetTransitionToolspecificList,
					targetWopedTransToolSpecific);

		}

		// Do the arcs
		List<ArcType> targetArcs = targetNet.getArc();
		List<ArcType> sourceArcs = sourceNet.getArc();
		for (ArcType targetArc : targetArcs) {
			ArcType sourceArc = _getArcByIdInList(targetArc.getId(), sourceArcs);
			if (sourceArc == null) {
				continue;
			}

			// if we get here then found a match
			// will try to get do the merge.

			List<ArcToolspecificMergeType> targetArcToolspecificList = targetArc
					.getToolspecific();
			ArcToolspecificMergeType sourceArcToolspecific = (ArcToolspecificMergeType) OpnmlJAXBHelper
					.getToolspecificTypeForOperon(sourceArc.getToolspecific());
			ArcToolspecificMergeType targetArcToolspecific = (ArcToolspecificMergeType) OpnmlJAXBHelper
					.getToolspecificTypeForOperon(targetArc.getToolspecific());
			ArcToolspecificMergeType defaultArcToolspecific = null;
			_mergeToolspecificToList(sourceArcToolspecific,
					targetArcToolspecific, defaultArcToolspecific,
					targetArcToolspecificList, defaultOperonToolspecificNet);

		}
		
		// recursively go through the net pages
		List<Page> targetPages = targetNet.getPage();
		List<Page> sourcePages = sourceNet.getPage();
		
		for (Page aTargetPage : targetPages) {
			
			// we are assuming 1 net per page
			Page foundSourcePage = _getPageByIdInList(aTargetPage.getId(), sourcePages);
			
			if (foundSourcePage == null) {
				// cannot merge continue;
				// make it merge with itself
				// so we can have defaults
				// values added in
				foundSourcePage = aTargetPage;
			}
			
			NetType targetSubnet = aTargetPage.getNet().get(0);
			NetType sourceSubnet = foundSourcePage.getNet().get(0);
			
			final boolean subnet = true;
			mergeOperonToolSpecificToNet(sourceSubnet, targetSubnet, defaultOperonToolspecificNet, subnet);
			
		}

	}

	private static TransitionType _getTransitionByIdInList(String id,
			List<TransitionType> transitions) {
		for (TransitionType aTransition : transitions) {
			if (aTransition.getId().equals(id)) {
				return aTransition;
			}
		}

		return null;
	}

	private static Page _getPageByIdInList(String id,
			List<Page> pages) {
		for (Page aPage : pages) {
			if (aPage.getId().equals(id)) {
				return aPage;
			}
		}

		return null;
	}
	
	private static PlaceType _getPlaceByIdInList(String id,
			List<PlaceType> places) {
		for (PlaceType aPlace : places) {
			if (aPlace.getId().equals(id)) {
				return aPlace;
			}
		}

		return null;
	}

	private static ArcType _getArcByIdInList(String id, List<ArcType> arcs) {
		for (ArcType anArc : arcs) {
			if (anArc.getId().equals(id)) {
				return anArc;
			}
		}

		return null;
	}

	/**
	 * Possible place tool specifics:
	 * 
	 * @param name
	 * @param defaultOperonToolspecificNet
	 * @return
	 */
	private static PlaceToolspecificMergeType _findPlaceTooSpecificTypeByName(
			String name, NetType defaultOperonToolspecificNet) {
		List<PlaceType> places = defaultOperonToolspecificNet.getPlace();
		for (PlaceType place : places) {
			if (place.getName().equals(name)) {
				return (PlaceToolspecificMergeType) OpnmlJAXBHelper
						.getToolspecificTypeForOperon(place.getToolspecific());
			}
		}

		return null;

	}


	/**
	 * This just determines how the Toolspecifics are added to the lists.
	 * 
	 * @param sourceNetToolspecific
	 * @param targetNetToolspecific
	 * @param defaultNetToolspecific
	 * @param targetList
	 * @param targetWopedTransToolSpecific
	 *            a woped toolspecific
	 * @return true if a merge occurred, false if nothing happened.
	 */
	private static boolean _mergeTransitionToolspecificToList(
			TransitionToolspecificMergeType sourceNetToolspecific,
			TransitionToolspecificMergeType targetNetToolspecific,
			List<TransitionToolspecificMergeType> targetList,
			TransitionToolspecificMergeType targetWopedTransToolSpecific) {

		if ((sourceNetToolspecific == null) && (targetNetToolspecific == null)) {

			
			TransitionToolspecificMergeType toUpdateTransitionToolspeicific = OperonJaxbElementCreator.createTransitionToolspecific();
				_mergeTransitionToolSpecificsWithDefaultValues(
						toUpdateTransitionToolspeicific,
						targetWopedTransToolSpecific);

				targetList
				.add(toUpdateTransitionToolspeicific);
				
				return true;

			
		} else if ((sourceNetToolspecific != null)
				&& (targetNetToolspecific == null)) {
			
			TransitionToolspecificMergeType toUpdateTransitionToolspeicific = sourceNetToolspecific;			
			_mergeTransitionToolSpecificsWithDefaultValues(
						toUpdateTransitionToolspeicific, 
						targetWopedTransToolSpecific);
			
			targetList.add(sourceNetToolspecific);
			return true;

		} else if ((sourceNetToolspecific == null)
				&& (targetNetToolspecific != null)) {
			
			TransitionToolspecificMergeType toUpdateTransitionToolspeicific = targetNetToolspecific;
			
			_mergeTransitionToolSpecificsWithDefaultValues(
						toUpdateTransitionToolspeicific, 
						targetWopedTransToolSpecific);
			
			// no need to add to target list
			// as it is already in the list
			return false;

		} else if ((sourceNetToolspecific != null)
				&& (targetNetToolspecific != null)) {

			TransitionToolspecificMergeType toUpdateTransitionToolspeicific = sourceNetToolspecific;			
			_mergeTransitionToolSpecificsWithDefaultValues(
						toUpdateTransitionToolspeicific, 
						targetWopedTransToolSpecific);
			
			// source will always override target
			targetList.remove(targetNetToolspecific);
			targetList.add(sourceNetToolspecific);

			return true;
		}

		return false;
	}
	
	
	
	/**
	 * This just determines how the Toolspecifics are added to the lists.
	 * 
	 * @param sourceNetToolspecific
	 * @param targetNetToolspecific
	 * @param defaultNetToolspecific
	 * @param targetList
	 * @param targetWopedTransToolSpecific
	 *            a woped toolspecific
	 * @return true if a merge occurred, false if nothing happened.
	 */
	@SuppressWarnings("unchecked")
	private static boolean _mergeToolspecificToList(
			ToolspecificType sourceNetToolspecific,
			ToolspecificType targetNetToolspecific,
			ToolspecificType defaultNetToolspecific, List targetList,
			NetType defaultOperonToolspecificNet) {

		if ((sourceNetToolspecific == null) 
				&& (targetNetToolspecific == null)
				&& (defaultNetToolspecific == null)) {			
			// nothing to merge
			return false;
			
		} else if ((sourceNetToolspecific == null) && (targetNetToolspecific == null)) {
				targetList
						.add((ToolspecificType) cloneObjectBySerialization((Serializable) defaultNetToolspecific));
				return true;

		} else if ((sourceNetToolspecific != null)
				&& (targetNetToolspecific == null)) {
			targetList.add(sourceNetToolspecific);
			return true;

		} else if ((sourceNetToolspecific == null)
				&& (targetNetToolspecific != null)) {
			// no nothing
			return false;

		} else if ((sourceNetToolspecific != null)
				&& (targetNetToolspecific != null)) {

			// source will always override target
			targetList.remove(targetNetToolspecific);
			targetList.add(sourceNetToolspecific);

			return true;
		}

		return false;
	}

	private static void _mergeTransitionToolSpecificsWithDefaultValues(
			TransitionToolspecificMergeType toUpdateNetToolspecific,
			TransitionToolspecificMergeType targetWopedTransToolSpecific) {
		
		int wopedTransitionType = 0;

		if ( (null != targetWopedTransToolSpecific) && (null != targetWopedTransToolSpecific.getOperator()) ) {
			wopedTransitionType = targetWopedTransToolSpecific.getOperator()
					.getType();

		} else if ((null != targetWopedTransToolSpecific) 
				&& (null!=targetWopedTransToolSpecific.isSubprocess()) 
				&& targetWopedTransToolSpecific.isSubprocess()) {
			wopedTransitionType = TRANSITION_OPERATOR_subprocess; // for subprocess
		}

		OperonJaxbElementCreator.setTransitionTypeByWopedTransitionOperator(wopedTransitionType, toUpdateNetToolspecific);


		// Do the Trigger Type
		int wopedTriggerType = 0;
		if ( (null != targetWopedTransToolSpecific) && (null != targetWopedTransToolSpecific.getTrigger() )) {
			wopedTriggerType = targetWopedTransToolSpecific.getTrigger().getType();
			
		}
		
		if ( (null != targetWopedTransToolSpecific) 
					&& (null != targetWopedTransToolSpecific.isSubprocess()) 
					&& targetWopedTransToolSpecific.isSubprocess()) {			
			// if its a subprocess
			// do nothing
			
		} else {
			OperonJaxbElementCreator.setTriggerTypeByWopedTriggerType(wopedTriggerType, toUpdateNetToolspecific);
			
		}
		
		

	}

	/**
	 * Finds the tool "Woped" ToolSpecificType
	 * 
	 * @param list
	 *            of toolspecifics
	 * @return ToolspecificType or null
	 */
	public static ToolspecificType getToolspecificTypeForWoped(
			List<? extends ToolspecificType> toolspecifics) {
		return OpnmlJAXBHelper.getToolspecificTypeByName(
				TOOLSPECIFIC_NAME_Woped, toolspecifics);
	}

	/**
	 * Clones an object using Serializable
	 * 
	 * @param serializableObj
	 * @return
	 */
	public static Object cloneObjectBySerialization(Serializable serializableObj) {

		ByteArrayOutputStream bos = null;
		ObjectOutputStream out = null;
		ByteArrayInputStream bis = null;
		ObjectInputStream in = null;

		byte[] buf = null;

		try {
			// serialise
			bos = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bos);
			out.writeObject(serializableObj);
			buf = bos.toByteArray();

			bos.flush();
			bos.close();
			bos = null;

			out.flush();
			out.close();
			out = null;

			// de-serialise to get new object
			bis = new ByteArrayInputStream(buf);
			in = new ObjectInputStream(bis);
			Object newObj = (Object) in.readObject();
			in.close();
			in = null;

			return newObj;

		} catch (Exception e) {
			throw new BaseSystemException(e);

		} finally {
			try {
				if (bos != null) {
					bos.flush();
					bos.close();
				}

				if (out != null) {
					out.flush();
					out.close();

				}

				if (in != null) {
					in.close();
				}

			} catch (Exception e1) {
				throw new BaseSystemException(e1);

			}

		}

	}
}
