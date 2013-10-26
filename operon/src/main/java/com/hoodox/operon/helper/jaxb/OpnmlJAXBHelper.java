package com.hoodox.operon.helper.jaxb;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;
import com.hoodox.operon.exceptions.OperonConfigException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.helper.jaxb.woped.WopedJAXBHelper;
import com.hoodox.operon.jaxb.opnml.ArcToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.ArcType;
import com.hoodox.operon.jaxb.opnml.NetToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.NetType;
import com.hoodox.operon.jaxb.opnml.OperonResourcesType;
import com.hoodox.operon.jaxb.opnml.OperonTimeDurationExplicitType;
import com.hoodox.operon.jaxb.opnml.OperonTimeDurationImplicitType;
import com.hoodox.operon.jaxb.opnml.OperonTriggerAutoType;
import com.hoodox.operon.jaxb.opnml.OperonTriggerTimeType;
import com.hoodox.operon.jaxb.opnml.OperonTriggerWithResourcesType;
import com.hoodox.operon.jaxb.opnml.PageType;
import com.hoodox.operon.jaxb.opnml.PlaceToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.PlaceType;
import com.hoodox.operon.jaxb.opnml.PnmlType;
import com.hoodox.operon.jaxb.opnml.ReferencePlaceToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.ReferencePlaceType;
import com.hoodox.operon.jaxb.opnml.ToolspecificType;
import com.hoodox.operon.jaxb.opnml.TransitionToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.TransitionType;
import com.hoodox.operon.wfnet.Arc;
import com.hoodox.operon.wfnet.CaseType;
import com.hoodox.operon.wfnet.InArc;
import com.hoodox.operon.wfnet.OutArc;
import com.hoodox.operon.wfnet.Place;
import com.hoodox.operon.wfnet.RootCaseType;
import com.hoodox.operon.wfnet.SubCaseType;
import com.hoodox.operon.wfnet.Transition;
import com.hoodox.operon.wfnet.toolspecific.Scheduler;
import com.hoodox.operon.wfnet.toolspecific.TimeDuration;


/**
 * <p>
 * Helper JAXB class to parse the Opnml net configuration file.
 * <p>
 * This class is a singleton
 * 
 * @author HUAC
 * 
 */
public class OpnmlJAXBHelper {
	
	private static String TOOLSPECIFIC_NAME_OPERON ="Operon"; 
	
	/**
     * The jaxb context used to marshall and unmarshall java objects/ XML
     * Strings
     */
    private static JAXBContext jaxbContext = null;
	
	private static JAXBContext getJaxbContext () {
		if (jaxbContext != null) {
			return jaxbContext;
		}
				
		String contextPackage = "com.hoodox.operon.jaxb.opnml";
        try {
            jaxbContext = JAXBContext.newInstance(contextPackage);
            
        } catch (JAXBException e) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"Error cannot load the JaxbContext " + contextPackage);
			throw ex;
        }
        
        return jaxbContext;
		
	}
		
    /**
     * Loads an OperonNet from the XML string.
     * @param netXmlStr the OPNML net
     * @return PnmlType
     */
	public static JAXBElement<PnmlType> loadNetFromXmlStrReturnPnml(String netXmlStr, Logger log) {
    	
    	//===============================
    	// Convert string to inputstream
    	//===============================
    	InputStream ins = null;
		byte[] bArray = netXmlStr.getBytes();
		ins = new ByteArrayInputStream(bArray);

    	//===============================
    	// Unmarshall input stream
    	//===============================    	
    	Unmarshaller unmarshaller = null;    	
    	try {
    		unmarshaller = getJaxbContext().createUnmarshaller();    		
		} catch (JAXBException e) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"Error cannot create unmarshaller from the JaxbContext com.hoodox.operon.opnml.jaxb: " + e.getMessage(), 
					e);
			log.error(ex.getMessage(), ex);
			ex.setLogged();
			throw ex;
		}
		
		try {
			
			@SuppressWarnings("unchecked")
			JAXBElement<PnmlType> jaxbElement = (JAXBElement)unmarshaller.unmarshal(ins);			
			return jaxbElement;
			
		} catch (JAXBException e) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"Inutstream error, cannot unmarshall : " + netXmlStr +  " -> " + e.getMessage(), 
					e);
			log.error(ex.getMessage(), ex);
			ex.setLogged();
			throw ex;
		}
    	
    }
	
	
    
    /**
     * Loads an OperonNet from the XML string.
     * 
     * @param netXmlStr the OPNML net
     * @return a list of OperonNetType
     */
	public static NetType[] loadNetFromXmlStr(String netXmlStr, Logger log) {
		
		JAXBElement<PnmlType> jaxbElement = loadNetFromXmlStrReturnPnml(netXmlStr, log);
		PnmlType pnmlElement = jaxbElement.getValue();
		List<NetType> netList = pnmlElement.getNet();			
		return netList.toArray(new NetType[netList.size()]);
			    	
    }
    
    
    
    /**
     * Converts an expression to a Long
     * @param durationExpression
     * @return
     */
    public static Long convertDurationExpressionToLong(String durationExpression) {
        StringTokenizer st = new StringTokenizer(durationExpression, "-");
        if(st.countTokens() < 4) {
            com.hoodox.commons.exceptions.BaseSystemException ex = new OperonConfigException(
            			new ErrorCode("operon_net_config_error"), 
            			"Time duration is not valid, it must be in format of days-hours-mins-seconds :");
            throw ex;
        } 
        long days = (new Long(st.nextToken())).longValue() * 24L * 60L * 60L;
        long hours = (new Long(st.nextToken())).longValue() * 60L * 60L;
        long mins = (new Long(st.nextToken())).longValue() * 60L;
        long secs = (new Long(st.nextToken())).longValue();
        return new Long (days + hours + mins + secs);
    }
    
    
    
    
    /**
     * <p>
     * Populate the Case attributes with the net attributes.<p>
     * 
     * <p>Before we do this we need to do a Woped cleanup if its straight a Woped file to remove
     * duplicate XOR-Join and OR-Split Transitions by merging them into a single one.
     * Woped is a bit stupid not sure why they create multiple transitions for
     * each XOR-Join and OR-Split.</p>

     * @param net
     */
    public static RootCaseType createCase(NetType net) {
    	
		// do some Woped cleanup and prepare for Operon
		WopedJAXBHelper.prepareWopedForOperon(net);
    	
    	RootCaseType rootCaseType = new RootCaseType();
    	rootCaseType.setId(net.getId());
    	
    	NetToolspecificMergeType netToolSpecific = (NetToolspecificMergeType) getToolspecificTypeForOperon(net.getToolspecific());
    	if (netToolSpecific == null) {
    		throw new OperonConfigException(
        			new ErrorCode("operon_net_config_error"), 
        			"Cannot register ResourceManagerInterface because the Net <toolspecific>..<toolspecific/> part is not set. ");    		
    	}
    	
    	rootCaseType.setResourceManagerInterface(netToolSpecific.getResourceManagerInterface());
    	if (null != net.getName()) {
    		rootCaseType.setName(net.getName().getText());    		
    	}
    	
//    	if (null != net.getDescription()) {
//    		rootCaseType.setDescription(net.getDescription().getText());    		
//    	}
    	
    	//==============================
    	// Register the the schedulers
    	//==============================
    	if ( ( null != netToolSpecific.getSchedulerRegistry() ) 
    		  && ( null != netToolSpecific.getSchedulerRegistry().getScheduler() ) ) {
    		
    		List<NetToolspecificMergeType.SchedulerRegistry.Scheduler> schedulerTypeList = netToolSpecific.getSchedulerRegistry().getScheduler();
    		NetToolspecificMergeType.SchedulerRegistry.Scheduler[] schedulerTypes = 
    			 	schedulerTypeList.toArray(new NetToolspecificMergeType.SchedulerRegistry.Scheduler[schedulerTypeList.size()]);
    		
    		for (int i=0; i< schedulerTypes.length; i++) {
    			Scheduler scheduler = new Scheduler();
    			scheduler.setId(schedulerTypes[i].getId());
    			if (StringUtils.isBlank(schedulerTypes[i].getCronTriggerExpression())) {
    	            com.hoodox.commons.exceptions.BaseSystemException ex = new OperonConfigException(
                			new ErrorCode("operon_net_config_error"), 
                			"Cannot register Scheduler " + schedulerTypes[i].getId() + " because the Cron expression is blank. Please check the net files again");
    	            throw ex;
    				
    			}
    			scheduler.setCronTriggerExpression(schedulerTypes[i].getCronTriggerExpression().trim());
    			
    			rootCaseType.addScheduler(scheduler);

    		}
    	}
    	
    	//================================
    	// Work out timeToLive if Case
    	// has expiration and also if
    	// implicit timer is used
    	//================================
    	if (null!=netToolSpecific.getExplicitTimeToLive()) {
    		TimeDuration expiryDuration = new TimeDuration();
    		expiryDuration.setExplicit(true);
    		expiryDuration.setDurationExpression(netToolSpecific.getExplicitTimeToLive().getDuration()); 
    		rootCaseType.setTimeToLive(expiryDuration);
    	}

    	if (null!=netToolSpecific.getImplicitTimeToLive()) {
    		TimeDuration expiryDuration = new TimeDuration();
    		expiryDuration.setExplicit(false);
    		expiryDuration.setDurationExpression(netToolSpecific.getImplicitTimeToLive().getDuration());
    		
    		Scheduler[] schedToUseRefs = _unwrapShedToUseListToSchedulers(netToolSpecific.getImplicitTimeToLive().getSchedulerToUse(), rootCaseType);
    		
    		expiryDuration.setSchedulerToUseRefs(schedToUseRefs);
    		
    		rootCaseType.setTimeToLive(expiryDuration);
    	}
    	
    	return rootCaseType;
    }
    
    /**
     * Creates a list of places from the OperonNetType
     * @param net
     * @return
     */
    public static Place[] createPlaces(NetType net) {
    	List<Place> newPlaceList = new ArrayList<Place>();
    	Iterator<PlaceType> iter = net.getPlace().iterator();
    	while (iter.hasNext()) {
    		PlaceType placeType = iter.next();
    		newPlaceList.add(_createSinglePlace(placeType));
    	}
    	
    	return newPlaceList.toArray(new Place[newPlaceList.size()]);
    }
    
    /**
     * Creates a list of places from the OperonPageType.
     * @param parentCase - this is used to reference the parent place.
     * @param xmlPage a OperonPageType
     * @return
     */
    public static Place[] createPlaces (CaseType parentCaseType, PageType xmlPage) {
    	List<Place> newPlaceList = new ArrayList<Place>();
    	
    	//=========================
    	// Add the reference places
    	//=========================
    	NetType pageNetType = (NetType) xmlPage.getNet().get(0);
    	
    	Iterator<ReferencePlaceType> iterRefPlaces = pageNetType.getReferencePlace().iterator();
    	while (iterRefPlaces.hasNext()) {
    		ReferencePlaceType xmlRefPlaceType = iterRefPlaces.next();
    		newPlaceList.add(_createSingleReferencePlace(xmlRefPlaceType, parentCaseType));
    	}
    	
    	//===============================================
    	// Then the normal places 
    	// but need to extract the dodgy
    	// WoPed place where it should be reference place
    	//===============================================
    	Iterator<PlaceType> iterPlaces = pageNetType.getPlace().iterator();
    	while (iterPlaces.hasNext()) {
    		PlaceType placeType = iterPlaces.next();
    		
    		// This is to check because Woped is a bit messed up
    		// as it does not support ReferencePlace. It has same place id
    		// in the parent as the child. So we need to check
    		// if this place id already exist in the parent. If it does then
    		// the current place is a ReferencePlace
    		if ( null != findParentPlaceOrPlaceWithReferenceToId(placeType.getId(), parentCaseType) ) {
    			newPlaceList.add(WopedJAXBHelper.createSingleWopedReferencePlace(placeType, parentCaseType, xmlPage) );
    			
    		} else {
        		newPlaceList.add(_createSinglePlace(placeType));
    			
    		}
    		
    	}
    	
    	
    	return newPlaceList.toArray(new Place[newPlaceList.size()]);
    	
    }
    
    
    /**
     * Creates a single referenceplace
     * @param xmlRefPlaceType
     * @return
     */
    private static Place _createSingleReferencePlace (ReferencePlaceType xmlRefPlace, CaseType parentCaseType) {
    	Place place = new Place();
    	place.setId(xmlRefPlace.getId());
    	
    	//==========================
    	// Work out the type of place
    	//==========================
    	ReferencePlaceToolspecificMergeType refPlaceToolSpecific = (ReferencePlaceToolspecificMergeType) getToolspecificTypeForOperonFromObjectList(xmlRefPlace.getGraphicsOrToolspecific());
    	if (refPlaceToolSpecific == null) {
    		throw new OperonConfigException(
        			new ErrorCode("operon_net_config_error"), 
        			"Cannot register ReferencePlace id " + xmlRefPlace.getId() + " because the ReferencePlace <toolspecific>..<toolspecific/> part for operon is not set. ");    		
    	}

    	if (null != refPlaceToolSpecific.getInrefPlace()) {
    		place.setType(Const.PLACE_TYPE_inref);
    		place.setCreateSubcasesAction(refPlaceToolSpecific.getInrefPlace().getCreateSubcasesAction());
    		
    		
    	} else {
    		place.setType(Const.PLACE_TYPE_outref);
    	}
    	
    	//==========================
    	// Get the parent place ref
    	//==========================
    	PlaceType xmlPlace = (PlaceType) xmlRefPlace.getRef();
    	Place parentRefPlace = findParentPlaceOrPlaceWithReferenceToId(xmlPlace.getId(), parentCaseType);    	
    	if (null == parentRefPlace) {
	    	//Cannot have a place linking to another other object other than Transition
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Net " + parentCaseType.getId() + " has a subnet " + xmlPlace.getId() + " has reference place " 
					+ place.getId() + " that points to a place " + xmlPlace.getId() + " which is not registered in the parent net");
			throw ex;
    		
    	}
    	
    	place.setRefPlace(parentRefPlace);
    	place.setName(parentRefPlace.getName());
    	
    	return place;
    }
    
     
    /**
     * Populates the Place attributes with the OperonPlaceType place
     * @param place
     * @param oPlace
     */
    private static Place _createSinglePlace(PlaceType xmlPlace) {
    	Place place = new Place();
    	place.setId(xmlPlace.getId());
    	if (null != xmlPlace.getName()) {
        	place.setName(xmlPlace.getName().getText());
    		
    	}
    	    	
    	if (null != xmlPlace.getInitialMarking() ) {
    		place.setInitialMarking(new Integer( xmlPlace.getInitialMarking().getText()));    		
    	}
    	
    	PlaceToolspecificMergeType placeToolspecific = (PlaceToolspecificMergeType) getToolspecificTypeForOperon(xmlPlace.getToolspecific());
    	if (null ==  placeToolspecific) {
        	place.setType(Const.PLACE_TYPE_intermed);
        	
        	// just an intermdiate place finish here
    		return place;
    	}
    	
    	if (null != placeToolspecific.getSourcePlace() ) {
        	place.setType(Const.PLACE_TYPE_source);
        	place.setPostCreateCaseAction(placeToolspecific.getSourcePlace().getPostCreateCaseAction());
        	String[] resourcesRefs = _unwrapOperonResourcesTypeToStringRefs(placeToolspecific.getSourcePlace().getResources());
        	place.setResources(resourcesRefs);
        	        	
    	} else if (null != placeToolspecific.getSinkPlace()) {
        	place.setType(Const.PLACE_TYPE_sink);
    		    		
    	} else {
        	place.setType(Const.PLACE_TYPE_intermed);
    		
    	}
    	
    	return place;
    }
    
    
    
    /**
     * Creates a list of Transitions from the passed OperonNetType net
     * @parem rootCaseType - contains the list of schedulers for reference
     * @param net
     * @return Transition[]
     */
    public static Transition[] createTransitions (RootCaseType rootCaseType, NetType net) {
    	List<Transition> newTransitionList = new ArrayList<Transition>();
    	Iterator<TransitionType> iter = net.getTransition().iterator();
    	while (iter.hasNext()) {
    		TransitionType transitionType = iter.next();    		
    		newTransitionList.add(_createSingleTransition(transitionType, rootCaseType));
    		
    	}
    	
    	return newTransitionList.toArray(new Transition[newTransitionList.size()]);

    	
    }

    /**
     * Creates a list of Transitions from the passed OperonPageType net
     * @parem rootCaseType - contains the list of schedulers for reference
     * @param page the subnet
     * @return Transition[]
     */
    public static Transition[] createTransitions (RootCaseType rootCaseType, PageType page) {
    	List<Transition> newTransitionList = new ArrayList<Transition>();
    	Iterator<TransitionType> iter = page.getNet().get(0).getTransition().iterator();

    	while (iter.hasNext()) {
    		TransitionType transitionType = iter.next();    		
        	newTransitionList.add(_createSingleTransition(transitionType, rootCaseType));
    			    		    		
    	}
    	
    	return newTransitionList.toArray(new Transition[newTransitionList.size()]);

    	
    }
    
    /**
     * Creates a single Transition from the passed in OperonTransitionType
     * @param rootCaseType contains the list of schedulers for reference
     * @param xmlTrans
     * @return
     */
    private static Transition _createSingleTransition(TransitionType xmlTrans, RootCaseType rootCaseType) {
    	Transition transition = new Transition();
    	
    	transition.setId(xmlTrans.getId());
    	if (null != xmlTrans.getName()) {
        	transition.setName(xmlTrans.getName().getText());
    		
    	}
    	
		if (WopedJAXBHelper.isAWopedSubProcessTransition(xmlTrans)) {
			//==========================
			// Finish here.
			// Ignore Woped Subprocess Transition as its only a 
			// place holder
			//==========================
			transition.setTransitionType(Const.TRANSITION_TYPE_ignore);
			return transition;
		}
    	
    	
    	//==============================
    	// Get the PriorityWeighting
    	//==============================
    	TransitionToolspecificMergeType transitionToolSpecific = 
    		(TransitionToolspecificMergeType) getToolspecificTypeForOperon(xmlTrans.getToolspecific());
    	if (transitionToolSpecific == null) {
    		throw new OperonConfigException(
        			new ErrorCode("operon_net_config_error"), 
        			"Cannot register Transition id " + xmlTrans.getId() + " because the Transition <toolspecific>..<toolspecific/> part for operon is not set. ");    		
    	}
    	
    	if (null == transitionToolSpecific.getTask().getPriorityWeighting() ) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Transition " + transition.getId()  + " either has no or unkown PriorityWeighting");
			throw ex;
    		
    	}
    	
    	transition.setPriorityWeighting(new Integer(transitionToolSpecific.getTask().getPriorityWeighting().intValue()));
    	
    	//==============================
    	// Get Estimated Completion time
    	//==============================
    	if (null == transitionToolSpecific.getTask().getEstimatedCompletionTime()) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Transition " + transition.getId()  + " EstimatedCompletionTime is not set or not known unkown PriorityWeighting");
			throw ex;
    		
    	}
    	
    	//==============================
    	// Estimated Completion 
    	// Time Duration
    	//==============================
    	transition.setEstimatedCompletionTime(new TimeDuration());
    	transition.getEstimatedCompletionTime().setDurationExpression(transitionToolSpecific.getTask().getEstimatedCompletionTime().getDuration());
    	
    	//==============================
    	// The ACTIONS
    	//==============================
    	List<String> actionList = transitionToolSpecific.getTask().getAction();
    	transition.setActions(actionList.toArray(new String[actionList.size()]));
    	
    	//====================
    	// Transition Types
    	//====================
    	transition.setTransitionType(transitionToolSpecific.getTransitionType().getType());
    	
    	//===========================
    	// TRIGGER types
    	//===========================
    	if (null != transitionToolSpecific.getAutoTrigger()) {
    		transition.setTriggerType(Const.TRIGGER_TYPE_auto);    		
    		OperonTriggerAutoType autoTrigg = transitionToolSpecific.getAutoTrigger();
    		transition.setExecutionTimeLimit(new TimeDuration());
    		transition.getExecutionTimeLimit().setDurationExpression(autoTrigg.getExecutionTimeLimit().getDuration());
    		
    	} else if (null != transitionToolSpecific.getTimeTrigger()) {
    		transition.setTriggerType(Const.TRIGGER_TYPE_time);
    		OperonTriggerTimeType xmlTimeTrigg = transitionToolSpecific.getTimeTrigger();
    		transition.setExecutionTimeLimit(new TimeDuration());
    		transition.getExecutionTimeLimit().setDurationExpression(xmlTimeTrigg.getExecutionTimeLimit().getDuration());
    		
    		//=====================
    		// trigger Delay
    		//=====================
			transition.setTriggerDelayDuration(new TimeDuration());
    		if (null != xmlTimeTrigg.getExplicitTriggerDelayDuration()) {
    			//explicit time trigger
    			OperonTimeDurationExplicitType xmlExplicit = xmlTimeTrigg.getExplicitTriggerDelayDuration();
    			transition.getTriggerDelayDuration().setExplicit(true);
    			transition.getTriggerDelayDuration().setDurationExpression(xmlExplicit.getDuration());
    			
    		} else {
    			//implicit trigger delay    			
    			OperonTimeDurationImplicitType xmlImplicit = xmlTimeTrigg.getImplicitTriggerDelayDuration(); 
    			transition.getTriggerDelayDuration().setExplicit(false);
    			transition.getTriggerDelayDuration().setDurationExpression(xmlImplicit.getDuration());
        		transition.getTriggerDelayDuration().setSchedulerToUseRefs(_unwrapShedToUseListToSchedulers(xmlImplicit.getSchedulerToUse(), rootCaseType) );
    			
    		}
    		
    	} else if (null != transitionToolSpecific.getMessageTrigger()) {
    		transition.setTriggerType(Const.TRIGGER_TYPE_message);
    		OperonTriggerWithResourcesType xmlResourcesTrigg = transitionToolSpecific.getMessageTrigger();
    		String[] resourceIds = _unwrapOperonResourcesTypeToStringRefs(xmlResourcesTrigg.getResources());
    		transition.setResourceIds(resourceIds);
    		
    	} else if (null != transitionToolSpecific.getManualTrigger()) {
    		transition.setTriggerType(Const.TRIGGER_TYPE_manual);
    		OperonTriggerWithResourcesType xmlResourcesTrigg = transitionToolSpecific.getManualTrigger();
    		String[] resourceIds = _unwrapOperonResourcesTypeToStringRefs(xmlResourcesTrigg.getResources());
    		transition.setResourceIds(resourceIds);

    	} else {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Transition " + transition.getId()  + " either has no or unkown trigger types");
			throw ex;
    		
    	}
    	
    	return transition;
    }
    
    /**
     * Generates a list of Arcs from Net
     * @param caseType - This is used to reference the existing Place and Transitions 
     * @param net
     * @return
     */
    public static Arc[] createArcs (CaseType caseType, NetType net) {
    	List<Arc> newArcList = new ArrayList<Arc>();
    	Iterator<ArcType> iter = net.getArc().iterator();
    	while (iter.hasNext()) {
    		ArcType xmlArc = iter.next();
    		newArcList.add(_createSingleArc(xmlArc, caseType));
    		
    	}
    	
    	return newArcList.toArray(new Arc[newArcList.size()]);
    }

    /**
     * Generates a list of Arcs from the Page
     * @param caseType - This is used to reference the existing Place and Transitions 
     * @param net
     * @return
     */
    public static Arc[] createArcs (CaseType caseType, PageType xmlPage) {
    	List<Arc> newArcList = new ArrayList<Arc>();
    	Iterator<ArcType> iter = xmlPage.getNet().get(0).getArc().iterator();
    	while (iter.hasNext()) {
    		ArcType xmlArc = iter.next();
    		newArcList.add(_createSingleArc(xmlArc, caseType));
    		
    	}
    	
    	return newArcList.toArray(new Arc[newArcList.size()]);
    }
    
    /**
     * 
     * @param xmlArc OperonArcType to parse
     * @param caseType The caseType that contains refences to the places and transitions
     * @return
     */
    private static Arc _createSingleArc(ArcType xmlArc, CaseType caseType) {
		ArcToolspecificMergeType arcToolSpecific = 
			(ArcToolspecificMergeType) getToolspecificTypeForOperon(xmlArc.getToolspecific());

    	if (xmlArc.getSource() instanceof PlaceType) {
    		//===========================
    		// Its an IN arc
    		// pointing to a normal source place
    		//===========================
    		PlaceType xmlPlace = (PlaceType) xmlArc.getSource();

    		InArc inArc = new InArc();
    		inArc.setId(xmlArc.getId());
    		
    		Place placeRef = caseType.findPlaceId(xmlPlace.getId());
    		if (null == placeRef) {
    			BaseSystemException ex = new OperonConfigException(new ErrorCode(
    					Const.ERROR_CODE_operon_net_config_error),
    					"Arc " + xmlArc.getId()  + " is an IN Arc and has source place " + xmlPlace.getId() + " that does not exist");
    			throw ex;
    			
    		}
    		inArc.setSource(placeRef);
    		
    		TransitionType xmlTransition = (TransitionType) xmlArc.getTarget();
    		Transition transitionRef = caseType.findTransitionById(xmlTransition.getId());
    		if (null == transitionRef) {
    			BaseSystemException ex = new OperonConfigException(new ErrorCode(
    					Const.ERROR_CODE_operon_net_config_error),
    					"Arc " + xmlArc.getId()  + " is an IN Arc and has target transition " + xmlTransition.getId() + " that does not exist");
    			throw ex;
    			
    		}
    		
    		inArc.setTarget(transitionRef);
    		return inArc;
    	}
    	
    	if (xmlArc.getSource() instanceof ReferencePlaceType) {
    		//=============================
    		// Its an IN arc
    		// Pointing to a source reference place
    		//==============================
    		ReferencePlaceType xmlRefPlace = (ReferencePlaceType) xmlArc.getSource();

    		InArc inArc = new InArc();
    		inArc.setId(xmlArc.getId());
    		
    		Place placeRef = caseType.findPlaceId(xmlRefPlace.getId());
    		if (null == placeRef) {
    			BaseSystemException ex = new OperonConfigException(new ErrorCode(
    					Const.ERROR_CODE_operon_net_config_error),
    					"Arc " + xmlArc.getId()  + " is an IN Arc and has source place " + xmlRefPlace.getId() + " that does not exist");
    			throw ex;
    			
    		}
    		inArc.setSource(placeRef);
    		
    		TransitionType xmlTransition = (TransitionType) xmlArc.getTarget();
    		Transition transitionRef = caseType.findTransitionById(xmlTransition.getId());
    		if (null == transitionRef) {
    			BaseSystemException ex = new OperonConfigException(new ErrorCode(
    					Const.ERROR_CODE_operon_net_config_error),
    					"Arc " + xmlArc.getId()  + " is an IN Arc and has target transition " + xmlTransition.getId() + " that does not exist");
    			throw ex;
    			
    		}
    		
    		inArc.setTarget(transitionRef);
    		return inArc;
    		
    	}
    	//============================
    	//  If get here its an OUT arc
    	//  Set source as Transition
    	//============================    	
    	OutArc outArc = new OutArc();
    	outArc.setId(xmlArc.getId());
    	
		TransitionType xmlTransition = (TransitionType) xmlArc.getSource();
		Transition transitionRef = caseType.findTransitionById(xmlTransition.getId());
		if (null == transitionRef) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Arc " + xmlArc.getId()  + " is an OUT Arc and has source transition " + xmlTransition.getId() + " that does not exist");
			throw ex;
			
		}
		outArc.setSource(transitionRef);
		
		if (xmlArc.getTarget() instanceof PlaceType) {
			//===========================================
			// OUT Arc pointing to a normal target place
			//===========================================
			PlaceType xmlPlace = (PlaceType) xmlArc.getTarget();		
			Place placeRef = caseType.findPlaceId(xmlPlace.getId());
			if (null == placeRef) {
				BaseSystemException ex = new OperonConfigException(new ErrorCode(
						Const.ERROR_CODE_operon_net_config_error),
						"Arc " + xmlArc.getId()  + " is an OUT Arc and has target place " + xmlPlace.getId() + " that does not exist");
				throw ex;
				
			}
			outArc.setTarget(placeRef);
			
			if (transitionRef.getTransitionType().equalsIgnoreCase(Const.TRANSITION_TYPE_XOR_split) ) {
				// if XOR_Split requires guard expression
				
				
				if (null == arcToolSpecific) {
					BaseSystemException ex = new OperonConfigException(new ErrorCode(
							Const.ERROR_CODE_operon_net_config_error),
							"OutArc " + xmlArc.getId() 
							+ " is a XOR_split outArc but cannot get the outArc guardExpression because the Operon <toolspecific>..<toolspecific> does not exist");
					throw ex;
					
				}
				
				try {
					outArc.setGuardExpression(arcToolSpecific.getGuardExpression());
					
				} catch (NullPointerException e) {
					BaseSystemException ex = new OperonConfigException(new ErrorCode(
							Const.ERROR_CODE_operon_net_config_error),
							"Arc " + xmlArc.getId()  + " is an OUT Arc and from an XOR_Split Transition but it has no guard expression ", e);
					throw ex;
					
				}
			}
									
		} else if (xmlArc.getTarget() instanceof ReferencePlaceType) {
			//===========================================
			// OUT Arc pointing to a reference target place
			//===========================================
			ReferencePlaceType xmlRefPlace = (ReferencePlaceType) xmlArc.getTarget();		
			Place placeRef = caseType.findPlaceId(xmlRefPlace.getId());
			if (null == placeRef) {
				BaseSystemException ex = new OperonConfigException(new ErrorCode(
						Const.ERROR_CODE_operon_net_config_error),
						"Arc " + xmlArc.getId()  + " is an OUT Arc and has target place " + xmlRefPlace.getId() + " that does not exist");
				throw ex;
				
			}
			outArc.setTarget(placeRef);
			
			if (transitionRef.getTransitionType().equalsIgnoreCase(Const.TRANSITION_TYPE_XOR_split) ) {
				// if XOR_Split requires guard expression
				try {
					outArc.setGuardExpression(arcToolSpecific.getGuardExpression());
					
				} catch (NullPointerException e) {
					BaseSystemException ex = new OperonConfigException(new ErrorCode(
							Const.ERROR_CODE_operon_net_config_error),
							"Arc " + xmlArc.getId()  + " is an OUT Arc and from an XOR_Split Transition but it has no guard expression ", e);
					throw ex;
					
				}
			}
									
		} else {
			//===================
			// If we get here its an
			// unknown target place
			//===================
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Arc " + xmlArc.getId()  + " is an OUT Arc with an unknown target type, its should be either a refernce Place or a normal Place ");
			throw ex;
			
		}
		
		return outArc;
		
		
		
    }
    
    /**
     * Creates a single Subnet recursively
     * @param xmlPage
     * @param rootCaseType
     * @param parentCaseType
     * @return
     */
    public static SubCaseType createSingleSubnet (PageType xmlPage, RootCaseType rootCaseType, SubCaseType parentCaseType) {
    	SubCaseType caseType = new SubCaseType();
    	caseType.setId(xmlPage.getId());
    	caseType.setRootCaseType(rootCaseType);
    	if (null !=parentCaseType) {
    		// only set parent if its not root
        	caseType.setParentCaseType(parentCaseType);
        	caseType.setTreeLevel(new Integer (parentCaseType.getTreeLevel().intValue() + 1 ) );
        	
    	} else {
        	caseType.setParentCaseType(rootCaseType);
        	caseType.setTreeLevel(new Integer (rootCaseType.getTreeLevel().intValue() + 1 ) );
    		
    	}
    	
    	NetType pageNetType = xmlPage.getNet().get(0);
    	if (null != pageNetType.getName()) {
        	caseType.setName(pageNetType.getName().getText());    		
    	}

//    	if (null != pageNetType.getDescription()) {
//        	caseType.setDescription(xmlPage.getDescription().getText());    		
//    	}
    	    	
    	return caseType;
    }
    /**
     * Takes in a list of OperonTimeDurationImplicitType.SchedulerToUse
     * and returns a String[] of SchedulerToUse.getId's
     * @param schedulerToUseList
     * @return
     */
    private static Scheduler[] _unwrapShedToUseListToSchedulers (List<OperonTimeDurationImplicitType.SchedulerToUse> schedulerToUseList, RootCaseType rootCaseType) {
		List<Scheduler> newSchedList = new ArrayList<Scheduler>();
		
		OperonTimeDurationImplicitType.SchedulerToUse[] schedToUses = schedulerToUseList.toArray(new OperonTimeDurationImplicitType.SchedulerToUse[schedulerToUseList.size()]);
		for (int i=0; i<schedToUses.length; i++ ) {
			NetToolspecificMergeType.SchedulerRegistry.Scheduler schedulerType = 
					(NetToolspecificMergeType.SchedulerRegistry.Scheduler) schedToUses[i].getRef();
			
			// find the registered scheduler object in the CaseType and add it to the list
			Scheduler scheduler = rootCaseType.getSchedulerById(schedulerType.getId());
			if (null == scheduler) {
				BaseSystemException ex = new OperonConfigException(new ErrorCode(
						Const.ERROR_CODE_operon_config_error),
						"CaseType " + rootCaseType.getId()  + " has scheduler reference " + schedulerType.getId() + " that does not match any in the Scheduler Registry");
				throw ex;
				
			}
			
			newSchedList.add(scheduler);
		}
    	
		return newSchedList.toArray(new Scheduler[newSchedList.size()]);
    }
    
    /**
     * Unwraps the resourceType resources into a String[]
     * @param resources
     * @return
     */
    private static String[] _unwrapOperonResourcesTypeToStringRefs (OperonResourcesType resources) {

    	List<String> resourceList = new ArrayList<String>();        	
    	resourceList.add( resources.getDefaultId() );
    	
    	
    	if (null != resources.getResourceId()) {
    		resourceList.addAll(resources.getResourceId());
    	}
    	
    	return resourceList.toArray(new String[resourceList.size()]);
    	
    }
    
   
    /**
     * Finds the tool "operon" ToolSpecificType
     * @param list of toolspecifics
     * @return ToolspecificType or null
     */
   public static ToolspecificType getToolspecificTypeForOperon(List<? extends ToolspecificType> toolspecifics) {
	   return getToolspecificTypeByName(TOOLSPECIFIC_NAME_OPERON, toolspecifics);
   }

   /**
    * Finds the tool "operon" ToolSpecificType
    * @param list of toolspecifics
    * @param the toolspecific name e.g. operon or woped
    * @return ToolspecificType or null
    */
  public static ToolspecificType getToolspecificTypeByName(String toolspecificName, List<? extends ToolspecificType> toolspecifics) {
	   if ((toolspecifics == null) || toolspecifics.isEmpty()) {
			return null;
	   }
	   	   
	   for (ToolspecificType aToolspecificType: toolspecifics) {
		   if (aToolspecificType.getTool().equalsIgnoreCase(toolspecificName)) {
			   return aToolspecificType;
		   }
	   }
	   
	   // if we get here its null
	   return null;
  }
   
   /**
    * Overloaded method of the first
    * @param toolspecifics
    * @return
    */
   @SuppressWarnings("unchecked")
   public static ToolspecificType getToolspecificTypeForOperonFromObjectList(List toolspecifics) {
	   if ((toolspecifics == null) || toolspecifics.isEmpty()) {
			return null;
	   }
	   
	   for (Iterator i = toolspecifics.iterator(); i.hasNext();) {
		   Object obj = i.next();
		   if (obj instanceof ToolspecificType) {
			   ToolspecificType aToolspecificType = (ToolspecificType) obj;
			   if (aToolspecificType.getTool().equalsIgnoreCase(TOOLSPECIFIC_NAME_OPERON)) {
				   return aToolspecificType;
			   }
		   }
	   }
	   
	   // if we get here its null
	   return null;
   }

   
   
   /**
    * Finds a place by its Id or a place that has reference to it.
    * 
    * @param id
    * @param caseType
    * @return
    */
	public static Place findParentPlaceOrPlaceWithReferenceToId (String id, CaseType caseType) {
	   	// Find the place by id first 
	   	Place refPlace = caseType.findPlaceId(id);
	   	
	   	if (null == refPlace) {
	   		// if we get here it means the place with id does not
	   		// exist. Will not try to find one with reference to it.
	   		
	   		Place aPlace = caseType.findPlaceWithRef(id);
	   		
	   		if (null != aPlace) {
	   			refPlace = aPlace.getRefPlace();
	   			
	   		}
	   	}
		
	   	return refPlace;
	}
	
   
}
