/**
 * 
 */
package com.hoodox.operon.wfnet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoodox.commons.configurable.ConfigurationHelper;
import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;
import com.hoodox.commons.exceptions.XMLAccessException;
import com.hoodox.operon.exceptions.OperonConfigException;
import com.hoodox.operon.helper.Const;
import com.hoodox.operon.helper.jaxb.OpnmlJAXBHelper;
import com.hoodox.operon.jaxb.config.OperonRegistryElement;
import com.hoodox.operon.jaxb.opnml.NetType;
import com.hoodox.operon.jaxb.opnml.NetType.Page;
import com.hoodox.operon.valueobjects.TaskVo;
import com.hoodox.operon.valueobjects.TokenVo;
import com.hoodox.operon.wfnet.toolspecific.CronExpression;
import com.hoodox.operon.wfnet.toolspecific.Scheduler;

/**
 * This class is a singleton.
 * 
 * @author HUAC
 * 
 */
public class CaseTypeManager {
	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());	

	private ConfigurationHelper configHelper;
	private Map<String, CaseType> caseTypeCacheMap = new HashMap<String, CaseType>();
	private Map<String, CronExpression> cronExpMap = new HashMap<String, CronExpression>();
	
	
	/**
	 * Returns a CaseType by the CaseTypeId
	 * @param caseTypeId
	 * @return
	 */
	public RootCaseType getRootCaseTypeById (String caseTypeId) {
		return (RootCaseType) this.caseTypeCacheMap.get(caseTypeId);
		
	}
	
	/**
	 * Returns all CaseTypes
	 * @return
	 */
	public CaseType[] getAllCaseTypes () {
		Collection<CaseType> col = this.caseTypeCacheMap.values();
		return col.toArray(new CaseType[col.size()]);
	}
	
	public TaskVo[] putToken(TokenVo token) {
		return null;
	}
	
	
	public CaseTypeManager(String operonRegistryFilename, ConfigurationHelper configurationHelper) {
		this.configHelper = configurationHelper;
		
		// ========================================
		// Use ConfigurationHelper load the OperonRegistry
		// as an XML String which includes all 
		// the registered net files.
		//
		// Then use JAXB to parse the registry file
		// =========================================
		InputStream ins = null;
		try {
			configHelper.createDomFromFile(operonRegistryFilename);
			String xmlStr = configHelper.getDomAsString();

			// convert xmlStr to an InputStream to be used
			// by JAXB
			byte[] bArray = xmlStr.getBytes();
			ins = new ByteArrayInputStream(bArray);

		} catch (IOException e) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"IOException while parsing " + operonRegistryFilename + ":"
							+ e.getMessage(), e);
			log.error(ex.getMessage(), ex);
			ex.setLogged();
			throw ex;
		} catch (XMLAccessException e) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"XMLAccessException while parsing " + operonRegistryFilename + ":"
							+ e.getMessage(), e);
			log.error(ex.getMessage(), ex);
			ex.setLogged();
			throw ex;
		}

		if (ins == null) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"Error cannot load the configuration file " + operonRegistryFilename);
			log.error(ex.getMessage(), ex);
			ex.setLogged();
			throw ex;
		}
		
    	//=================================
    	// Use JAXB Unmarshall inputstream
    	//=================================
		JAXBContext jaxbContext = null;
        try {
        	jaxbContext = JAXBContext.newInstance("com.hoodox.operon.jaxb.config");
            
        } catch (JAXBException e) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"Error cannot load the JaxbContext com.hoodox.operon.jaxb.config ");
			log.error(ex.getMessage(), ex);
			ex.setLogged();
			throw ex;
        }
		
    	Unmarshaller unmarshaller = null;    	
    	try {
    		unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"Error cannot create unmarshaller from the JaxbContext com.hoodox.operon.jaxb.config: " + e.getMessage(), 
					e);
			log.error(ex.getMessage(), ex);
			ex.setLogged();
			throw ex;
		}
		
		String[] netFiles = null;
		try {
			
			OperonRegistryElement operonRegistryElement = (OperonRegistryElement)unmarshaller.unmarshal(ins);
			List<String> fileNameList = operonRegistryElement.getNetFiles().getFilename();
			netFiles = fileNameList.toArray(new String[fileNameList.size()]);
			
		} catch (JAXBException e) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"Inutstream error, cannot unmarshall : " + operonRegistryFilename +  " -> " + e.getMessage(), 
					e);
			log.error(ex.getMessage(), ex);
			ex.setLogged();
			throw ex;
		}
		
		//=============================
		// Load each net File in the
		// registry
		//=============================
		for (int i=0; i<netFiles.length; i++) {
			_loadASingleNetFile(netFiles[i]);
			
		}
	}
	
	/**
	 * Loads all the Nets from the OperonRegistry.xml file
	 *
	 */
	private void _loadASingleNetFile(String netFile) {
		log.info("Loading net file " + netFile + "...................................");

		// =====================================
		// Use ConfigurationHelper to load the file
		// as an XML string and convert it into
		// an InputStream
		// =====================================
		String netXmlStr = null;
		try {
			configHelper.createDomFromFile(netFile);
			netXmlStr = configHelper.getDomAsString();

		} catch (IOException e) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"IOException while parsing " + netFile + ":"
							+ e.getMessage(), e);
			log.error(ex.getMessage(), ex);
			ex.setLogged();
			throw ex;
		} catch (XMLAccessException e) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_config_error),
					"XMLAccessException while parsing " + netFile + ":"
							+ e.getMessage(), e);
			log.error(ex.getMessage(), ex);
			ex.setLogged();
			throw ex;
		}
		
		//======================================
		// Load the nets and add the net into
		// the netCacheMap.
		//======================================
		NetType[] operonNets = OpnmlJAXBHelper.loadNetFromXmlStr(netXmlStr, log);
		for (int i=0; i< operonNets.length; i++) {
			if (this.caseTypeCacheMap.get(operonNets[i].getId()) != null) {
				BaseSystemException ex = new OperonConfigException(new ErrorCode(
						Const.ERROR_CODE_operon_config_error),
						"Error registering netId " + operonNets[i].getId() + " in netFile "
								+ netFile + " because the netId already exist. Please make sure all the nets are unique.");
				log.error(ex.getMessage(), ex);
				ex.setLogged();
				throw ex;
				
				
			}
			
			// add the CaseType to the map
			CaseType caseType =  _convertToCaseType(operonNets[i], this.cronExpMap);
			this.caseTypeCacheMap.put(caseType.getId(), caseType);
			log.info("loaded Net " + caseType.getId() + "........");
		}
		
		
		log.info("Loaded net file " + netFile + ".....................................");
		
        
        
		
	}
		
	/**
	 * Converts the Net to a CaseType
	 * @param cronExpMap updates the cronExMap.
	 * @param net
	 * @return CaseType
	 */
	private static CaseType _convertToCaseType(NetType net, Map<String, CronExpression> cronExpMap) {
		//==========================================
		// Create a Case with all the net attributes
		//==========================================
		RootCaseType rootCaseType = OpnmlJAXBHelper.createCase(net);
		
		//========================================= 
		// Find all scheduler Ids that have the same 
		// expression and register all at once.
		// Registers all the cronExpression in one place 
		// so that they can be executed all at once
		//=========================================
		Scheduler[] schedulers = rootCaseType.getAllSchedulers();
		for (int i=0; i< schedulers.length; i++) {
			String cronExpStr = schedulers[i].getCronTriggerExpression();
			CronExpression cronExp = cronExpMap.get(cronExpStr);
			if (null == cronExp) {
				cronExp = new CronExpression();
				cronExp.setExpression(cronExpStr);
				cronExp.addschedulerRefId(schedulers[i].getId());
				cronExpMap.put(cronExp.getExpression(), cronExp);
				
			}
			cronExp.addschedulerRefId(schedulers[i].getId());
		}
		
		//=========================
    	// Add places
		//=========================
		rootCaseType.setPlaces(OpnmlJAXBHelper.createPlaces(net));
		
		//=========================
		// Add the transitions
		//=========================
		rootCaseType.setTransitions(OpnmlJAXBHelper.createTransitions(rootCaseType, net));
		
		//=======================
		// Add the Arcs
		//=======================
		rootCaseType.setArcs(OpnmlJAXBHelper.createArcs(rootCaseType, net));
		
		//=====================================
		// Add the subnets
		//=====================================
		
		if ( (null == net.getPage()) && (net.getPage().size() != 0)) {
			//====================
			// No subnets here so
			// will exit
			//====================
			return rootCaseType;
		}
		
		
		Iterator<Page> iterPages = net.getPage().iterator();
		while (iterPages.hasNext()) {
			Page xmlSubPage = iterPages.next();
			rootCaseType.addsubCaseType(_convertToSubCaseType(xmlSubPage, rootCaseType, null));
		}
		
				
		return rootCaseType;
		
	}
	
	private static SubCaseType _convertToSubCaseType(Page xmlPage, RootCaseType rootCaseType, SubCaseType aParentCaseType) {
		CaseType parentCaseType =  aParentCaseType;
		SubCaseType newCaseType = null;
		
		//==========================================
		// Create subcase with all the page attributes
		//==========================================		
		if (null == parentCaseType) {
			parentCaseType = rootCaseType;
			newCaseType = OpnmlJAXBHelper.createSingleSubnet(xmlPage, rootCaseType, null);
			
		} else {
			
			newCaseType = OpnmlJAXBHelper.createSingleSubnet(xmlPage, rootCaseType, (SubCaseType) parentCaseType);
			
		}
		
		//===========================================
		// Add the places
		//===========================================
		newCaseType.setPlaces(OpnmlJAXBHelper.createPlaces(parentCaseType,xmlPage));
		
		//=========================
		// Add the transitions
		//=========================
		newCaseType.setTransitions(OpnmlJAXBHelper.createTransitions(rootCaseType, xmlPage));
		
		//========================
		// Add Arcs
		//========================
		newCaseType.setArcs(OpnmlJAXBHelper.createArcs(newCaseType, xmlPage));
		
		//============================
		// Add more subnets
		// (recursively calls itself)
		//============================
		NetType pageNetType = xmlPage.getNet().get(0);
		if ( (null == pageNetType.getPage()) || (pageNetType.getPage().size() != 0)) {
			//====================
			// No subnets here so
			// will exit
			//====================
			return newCaseType;
		}
		
		
		Iterator<Page> iterPages = pageNetType.getPage().iterator();
		while (iterPages.hasNext()) {
			Page xmlSubPage = iterPages.next();
			newCaseType.addsubCaseType(_convertToSubCaseType(xmlSubPage, rootCaseType, newCaseType));
		}
		
		return newCaseType;
	}
	
	//=================================
	// Some helper methods
	//=================================
	/**
	 * Finds a starting place in the CaseType
	 */
	public static Place findSourcePlace(CaseType caseType) {
		Place[] places = caseType.getPlaces();
		for (int i=0; i<places.length; i++) {
			if ( Const.PLACE_TYPE_source.equals(places[i].getType())) {
				return places[i];
			}
		}
		
		return null;
	}
	
	/**
	 * Finds all inRefPlaces that matches the passed place
	 * 
	 * @param place
	 * @param subCaseType
	 * @return empty Place[] if nothing found
	 */
	public static Place[] findInRefPlaces(Place place, SubCaseType subCaseType) {
		List<Place> matchedPlaceRefList = new ArrayList<Place>(); 
			Place[] places = subCaseType.getPlaces();			
			for (int i=0; i < places.length; i++) {
				if (!Const.PLACE_TYPE_inref.equals(places[i].getType())) {
					// not inref so continue loop
					continue;
				}
				
				// if get here check if passed place matches inref place reference
				if (place.getId().equals(places[i].getRefPlace().getId())) {
					matchedPlaceRefList.add(places[i]);
				}
			}
		
		return matchedPlaceRefList.toArray(new Place[matchedPlaceRefList.size()]);
	}
	
	/**
	 * Finds all Transitions in the CaseType from a Place
	 * @param caseType
	 * @param fromPlace
	 * @return
	 */
	public static Transition[] findAllTransitionsFromPlace(CaseType caseType, Place fromPlace) {
		List<Transition> foundTransitionList = new ArrayList<Transition>();
		
		Arc[] arcs = caseType.getArcs();
		for (int i=0; i<arcs.length; i++) {
			if (!(arcs[i] instanceof InArc)) {
				// not an IN ARC continue loop
				continue;
			}
			
			// if get here its an IN ARC
			InArc inArc = (InArc) arcs[i];
			if (inArc.getSource().getId().equals(fromPlace.getId()) ) {				
				foundTransitionList.add(inArc.getTarget());
				
			}
			
		}
		
		return foundTransitionList.toArray(new Transition[foundTransitionList.size()]);
	}
	
	/**
	 * Finds all places into a Transition
	 * @param caseType
	 * @param transition
	 * @return
	 */
	public static Place[] findAllPlacesIntoTransition(CaseType caseType, Transition transition) {
		List<Place> foundPlacesList = new ArrayList<Place>();
		
		Arc[] arcs = caseType.getArcs();
		for (int i=0; i<arcs.length; i++) {
			if (!(arcs[i] instanceof InArc)) {
				// not an IN ARC continue loop
				continue;
			}
			
			// if get here its an IN ARC
			InArc inArc = (InArc) arcs[i];
			if (inArc.getTarget().getId().equals(transition.getId())) {
				// found place into Transition
				foundPlacesList.add(inArc.getSource());
				
			}			
		}
		
		return foundPlacesList.toArray(new Place[foundPlacesList.size()]);
	}
	
	/**
	 * Finds all places out from Transition
	 * 
	 * @param caseType
	 * @param transition
	 * @return
	 */
	public static Place[] findAllPlacesOutFromTransition (CaseType caseType, Transition transition) {
		List<Place> foundPlacesList = new ArrayList<Place>();

		Arc[] arcs = caseType.getArcs();
		for (int i=0; i<arcs.length; i++) {
			if (!(arcs[i] instanceof OutArc)) {
				// not an OUT ARC continue loop
				continue;
			}
			
			// if get here its an OUT ARC
			OutArc outArc = (OutArc) arcs[i];
			if (outArc.getSource().getId().equals(transition.getId())) {
				// found place out from Transition
				foundPlacesList.add(outArc.getTarget());
				
			}			
		}
		
		return foundPlacesList.toArray(new Place[foundPlacesList.size()]);
		
	}

	/**
	 * Finds all places out from Transition
	 * 
	 * @param caseType
	 * @param transition
	 * @return
	 */
	public static OutArc findOutArc(CaseType caseType, Transition transition, Place place) {		
		Arc[] arcs = caseType.getArcs();
		for (int i=0; i<arcs.length; i++) {
			if (!(arcs[i] instanceof OutArc)) {
				// not an OUT ARC continue loop
				continue;
			}
			
			// if get here its an OUT ARC
			OutArc outArc = (OutArc) arcs[i];
			if (outArc.getSource().getId().equals(transition.getId())
					&& outArc.getTarget().getId().equals(place.getId())) {
				return outArc;
				
			}			
		}
		
		return null;
		
	}
	
	/**
	 * Checks if the transitionId passed is automatically triggered
	 * @param TransitionId
	 * @param caseType the caseType that contains the transition
	 * @return true if AutoTriggered
	 */
	public static boolean isTransitionAutoTrigger(String transitionId, CaseType caseType) {
		Transition transition = caseType.findTransitionById(transitionId);
		if (transition == null) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Transition " + transitionId + " is not in the  net " + caseType.getId()
							+ " Please check the net files again or it could also be someone has been tampering with the database operon_workitem table");
			throw ex;
			
		}
		
		if (Const.TRIGGER_TYPE_auto.equals(transition.getTriggerType())) {
			return true;
		}
		
		return false;
	}

	/**
	 * Checks if the transitionId passed is automatically triggered
	 * @param TransitionId
	 * @param caseType the caseType that contains the transition
	 * @return true if AutoTriggered
	 */
	public static boolean isTransitionTimeTrigger(String transitionId, CaseType caseType) {
		Transition transition = caseType.findTransitionById(transitionId);
		
		if (transition == null) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Transition " + transitionId + " is not in the  net " + caseType.getId()
							+ " Please check the net files again or it could also be someone has been tampering with the database operon_workitem table");
			throw ex;
			
		}
		
		if (Const.TRIGGER_TYPE_time.equals(transition.getTriggerType())) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the transitionId passed is timed triggered
	 * @param TransitionId
	 * @param caseType the caseType that contains the transition
	 * @return true if its TimerTriggered
	 */
	public static boolean isTransitionExplicitTimeTrigger(String transitionId, CaseType caseType) {
		Transition transition = caseType.findTransitionById(transitionId);
		if (transition == null) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_operon_net_config_error),
					"Transition " + transitionId + " is not in the  net " + caseType.getId()
							+ " Please check the net files again or it could also be someone has been tampering with the database operon_workitem table");
			throw ex;
			
		}
		
		if (Const.TRIGGER_TYPE_time.equals(transition.getTriggerType()) && transition.getTriggerDelayDuration().isExplicit() ) {
			return true;
		}
		
		return false;
	}
		
	public CronExpression[] getAllCronExpressions () {
		return this.cronExpMap.values().toArray(new CronExpression[this.cronExpMap.values().size()]);
	}
	
	/**
	 * @param cronExp
	 * @return
	 */
	public void registerUnofficialCronExpStr(String cronExpStr) {
		if (StringUtils.isBlank(cronExpStr)) {
			BaseSystemException ex = new OperonConfigException(new ErrorCode(
					Const.ERROR_CODE_unknown_error),
					"Trying to register an official job with cronExp that is blank. Someone must have been tampering with the database table operon_ttl_scheduler or operon_time_trigger_scheduler");
			throw ex;
			
		}
		CronExpression cronExp = this.cronExpMap.get(cronExpStr.trim());
		if (cronExp == null) {
			cronExp = new CronExpression();
			cronExp.setExpression(cronExpStr);
			this.cronExpMap.put(cronExp.getExpression(), cronExp);
		}
	}
	
}
