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
import static com.hoodox.operon.helper.jaxb.OperonJaxbElementCreator.DEFAULT_actionClass;
import static com.hoodox.operon.helper.jaxb.OperonJaxbElementCreator.DEFAULT_delay_duration;
import static com.hoodox.operon.helper.jaxb.OperonJaxbElementCreator.DEFAULT_execution_duration;
import static com.hoodox.operon.helper.jaxb.OperonJaxbElementCreator.DEFAULT_resource_id;
import static com.hoodox.operon.helper.jaxb.OperonJaxbElementCreator.DEFAULT_tool_name;
import static com.hoodox.operon.helper.jaxb.OperonJaxbElementCreator.DEFAULT_tool_version;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.FILENAME_DefaultOperonToolSpecifics;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TOOLSPECIFIC_NAME_Woped;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_AND_join;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_AND_join_XOR_split;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_AND_split;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_AND_split_join;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_join;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_join_AND_split;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_split;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_OPERATOR_XOR_split_join;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_TRIGGER_TYPE_message;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_TRIGGER_TYPE_resource;
import static com.hoodox.operon.helper.jaxb.woped.WopedConst.TRANSITION_TRIGGER_TYPE_time;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hoodox.commons.configurable.ConfigurationHelper;
import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.operon.OperonBaseTest;
import com.hoodox.operon.helper.jaxb.OpnmlJAXBHelper;
import com.hoodox.operon.helper.jaxb.woped.WopedJAXBHelper;
import com.hoodox.operon.jaxb.opnml.NetType;
import com.hoodox.operon.jaxb.opnml.PnmlType;
import com.hoodox.operon.jaxb.opnml.TransitionToolspecificMergeType;
import com.hoodox.operon.jaxb.opnml.TransitionType;


public class WopedJaxbMergeHelperTest extends OperonBaseTest {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());	
	
	private static String FILENAME_ExampleWoped = "ExampleWoped.pnml";
	private static String FILENAME_ExampleWopedMergedWithOperon = "ExampleWopedMergedWithOperon.pnml";
	private static String FILENAME_ExampleWopedMergedWithOperon_changed = "ExampleWopedMergedWithOperon_changed.pnml";
	
	
	
	//================== Test begins here ======================//
	
	
	/**
	 * This test takes a pure Woped File and addes in the default OperonToolspecifics for
	 * Net, Places, Transitions and arcs.
	 */
	//@Test(groups = { "operon.jaxb.helper.woped.merge" })
	public void testMergeWopedOnlyFile () {
		int expectedTranToolspecificCount = 30;
		
		JAXBElement<PnmlType> sourceJAXBElement = _loadFirstJaxbElementByFileName(FILENAME_ExampleWoped);
		JAXBElement<PnmlType> targetJAXBElement = _loadFirstJaxbElementByFileName(FILENAME_ExampleWoped);
		JAXBElement<PnmlType> defaultJAXBElement = _loadFirstJaxbElementByFileName(FILENAME_DefaultOperonToolSpecifics);
		
		NetType sourceNet = _loadFirstNetInJaxbElement(sourceJAXBElement); 
		NetType targetNet = _loadFirstNetInJaxbElement(targetJAXBElement); 
		NetType defaultNet = _loadFirstNetInJaxbElement(defaultJAXBElement); 
		
		WopedJAXBHelper.mergeOperonToolSpecificToNet(sourceNet, targetNet, defaultNet);
		
		printOutXML(targetJAXBElement);
		
		NetType changeNet = targetNet; 
		Assert.assertEquals(changeNet.getId(), "Woped");	
		Assert.assertEquals(changeNet.getPlace().size()==12, true);	
		Assert.assertEquals(changeNet.getTransition().size()==15, true);	
		Assert.assertEquals(changeNet.getArc().size()==30, true);	
		Assert.assertEquals(changeNet.getToolspecific().size()==2, true);	
		Assert.assertEquals(changeNet.getPage().size()==1, true);	
		
		
		
		// Test Transitions
		List<TransitionType> transitions = changeNet.getTransition();
		int tranToolspecificsCount = 0;
		
		for (TransitionType aTran : transitions) {
			tranToolspecificsCount += aTran.getToolspecific().size();
			
			Assert.assertTrue(transitionToolspecificsAreTheSame(aTran.getToolspecific(), aTran.getName().getText()));
			
		}
		
		Assert.assertEquals(tranToolspecificsCount, expectedTranToolspecificCount);	

		NetType changeSubnet = changeNet.getPage().get(0).getNet().get(0); 
		Assert.assertEquals(changeNet.getPage().get(0).getId(), "sub1");	
		Assert.assertEquals(changeSubnet.getPlace().size()==5, true);	
		Assert.assertEquals(changeSubnet.getTransition().size()==6, true);	
		Assert.assertEquals(changeSubnet.getArc().size()==12, true);	
		Assert.assertEquals(changeSubnet.getToolspecific().size()==1, true);	
		Assert.assertEquals(changeSubnet.getPage().size()==0, true);	
		
		
		
		// Test Transitions
		List<TransitionType> subTransitions = changeSubnet.getTransition();
		int subTranToolspecificsCount = 0;
		
		for (TransitionType aTran : subTransitions) {
			subTranToolspecificsCount += aTran.getToolspecific().size();
			
			Assert.assertTrue(transitionToolspecificsAreTheSame(aTran.getToolspecific(), aTran.getName().getText()));
			
		}
		
		Assert.assertEquals(subTranToolspecificsCount, 12);	
		
		
		
	}


	/**
	 * This test takes a pure Woped File and addes in the default OperonToolspecifics for
	 * Net, Places, Transitions and arcs.
	 */
	@Test(groups = { "operon.jaxb.helper.woped.merge" })
	public void testMergeOperonWopedWithChangedFile () {
		int expectedTranToolspecificCount = 32;
		
		JAXBElement<PnmlType> sourceJAXBElement = _loadFirstJaxbElementByFileName(FILENAME_ExampleWopedMergedWithOperon);
		JAXBElement<PnmlType> targetJAXBElement = _loadFirstJaxbElementByFileName(FILENAME_ExampleWopedMergedWithOperon_changed);
		JAXBElement<PnmlType> defaultJAXBElement = _loadFirstJaxbElementByFileName(FILENAME_DefaultOperonToolSpecifics);
		
		NetType sourceNet = _loadFirstNetInJaxbElement(sourceJAXBElement); 
		NetType targetNet = _loadFirstNetInJaxbElement(targetJAXBElement); 
		NetType defaultNet = _loadFirstNetInJaxbElement(defaultJAXBElement); 
		
		WopedJAXBHelper.mergeOperonToolSpecificToNet(sourceNet, targetNet, defaultNet);
		
		printOutXML(targetJAXBElement);
		
		NetType changeNet = targetNet; 
		Assert.assertEquals(changeNet.getId(), "Woped");	
		Assert.assertEquals(changeNet.getPlace().size()==13, true);	
		Assert.assertEquals(changeNet.getTransition().size()==16, true);	
		Assert.assertEquals(changeNet.getArc().size()==32, true);	
		Assert.assertEquals(changeNet.getToolspecific().size()==2, true);	
		Assert.assertEquals(changeNet.getPage().size()==1, true);	
		
		
		
		// Test Transitions
		List<TransitionType> transitions = changeNet.getTransition();
		int tranToolspecificsCount = 0;
		
		for (TransitionType aTran : transitions) {
			tranToolspecificsCount += aTran.getToolspecific().size();
			
			Assert.assertTrue(transitionToolspecificsAreTheSame(aTran.getToolspecific(), aTran.getName().getText()));
			
		}
		
		Assert.assertEquals(tranToolspecificsCount, expectedTranToolspecificCount);	

		NetType changeSubnet = changeNet.getPage().get(0).getNet().get(0); 
		Assert.assertEquals(changeNet.getPage().get(0).getId(), "sub1");	
		Assert.assertEquals(changeSubnet.getPlace().size()==5, true);	
		Assert.assertEquals(changeSubnet.getTransition().size()==6, true);	
		Assert.assertEquals(changeSubnet.getArc().size()==12, true);	
		Assert.assertEquals(changeSubnet.getToolspecific().size()==1, true);	
		Assert.assertEquals(changeSubnet.getPage().size()==0, true);	
		
		
		
		// Test Transitions
		List<TransitionType> subTransitions = changeSubnet.getTransition();
		int subTranToolspecificsCount = 0;
		
		for (TransitionType aTran : subTransitions) {
			subTranToolspecificsCount += aTran.getToolspecific().size();
			
			Assert.assertTrue(transitionToolspecificsAreTheSame(aTran.getToolspecific(), aTran.getName().getText()));
			
		}
		
		Assert.assertEquals(subTranToolspecificsCount, 12);	
		
		
		
	}
	
	
	/**
	 * This is expecting 2 element in every list we get
	 * @param tranToolspecifics
	 */
	private static boolean transitionToolspecificsAreTheSame(List<TransitionToolspecificMergeType> tranToolspecifics, String transName) {
		
		// some predicate checking
		if (tranToolspecifics.size() != 2) {
			Assert.assertTrue(false, "A transition " + transName + " passed does not have a pair Transition Toolspecific, one for Woped and one for Operon");
			return false;
			
		}
		
		//prove otherwise
		boolean success = true; 
		
		// determine operon or woped
		TransitionToolspecificMergeType operonToolspecific = null;
		TransitionToolspecificMergeType wopedToolspecific = null;
		if (TOOLSPECIFIC_NAME_Woped.equalsIgnoreCase(tranToolspecifics.get(0).getTool())) {
			wopedToolspecific = tranToolspecifics.get(0);
			operonToolspecific = tranToolspecifics.get(1);
			
		} else {
			wopedToolspecific = tranToolspecifics.get(1);
			operonToolspecific = tranToolspecifics.get(0);
			
		}
		
		Assert.assertEquals(operonToolspecific.getTool(), DEFAULT_tool_name);
		Assert.assertEquals(operonToolspecific.getVersion(), DEFAULT_tool_version);
		Assert.assertEquals(operonToolspecific.getTask().getEstimatedCompletionTime().getDuration(), DEFAULT_execution_duration);
		Assert.assertEquals(operonToolspecific.getTask().getPriorityWeighting(), BigInteger.ONE);
		Assert.assertEquals(operonToolspecific.getTask().getAction().size(), 1);
		Assert.assertEquals(operonToolspecific.getTask().getAction().get(0), DEFAULT_actionClass);
		
		
		// make sure transition type are in sync
		int wopedTransitionType = 0;
		if (null!=wopedToolspecific.getOperator() ) {
			wopedTransitionType = wopedToolspecific.getOperator().getType();
		}
		if (TRANSITION_OPERATOR_AND_join == wopedTransitionType) {
			Assert.assertEquals(operonToolspecific.getTransitionType().getType(), TRANSITION_TYPE_AND_join);
		
		} else if (TRANSITION_OPERATOR_AND_join_XOR_split == wopedTransitionType) {
			Assert.assertEquals(operonToolspecific.getTransitionType().getType(), TRANSITION_TYPE_AND_join_XOR_split);
			

		} else if (TRANSITION_OPERATOR_AND_split == wopedTransitionType) {
			Assert.assertEquals(operonToolspecific.getTransitionType().getType(), TRANSITION_TYPE_AND_split);
			

		} else if (TRANSITION_OPERATOR_AND_split_join == wopedTransitionType) {
			Assert.assertEquals(operonToolspecific.getTransitionType().getType(), TRANSITION_TYPE_AND_split_join);

		} else if (TRANSITION_OPERATOR_XOR_join == wopedTransitionType) {
			Assert.assertEquals(operonToolspecific.getTransitionType().getType(), TRANSITION_TYPE_XOR_join);

		} else if (TRANSITION_OPERATOR_XOR_join_AND_split == wopedTransitionType) {
			Assert.assertEquals(operonToolspecific.getTransitionType().getType(), TRANSITION_TYPE_XOR_join_AND_split);

		} else if (TRANSITION_OPERATOR_XOR_split == wopedTransitionType) {
			Assert.assertEquals(operonToolspecific.getTransitionType().getType(), TRANSITION_TYPE_XOR_split);

		} else if (TRANSITION_OPERATOR_XOR_split_join == wopedTransitionType) {
			Assert.assertEquals(operonToolspecific.getTransitionType().getType(), TRANSITION_TYPE_XOR_split_join);

		} else if ((null!=wopedToolspecific.isSubprocess()) && wopedToolspecific.isSubprocess()) {
			Assert.assertEquals(operonToolspecific.getTransitionType().getType(), TRANSITION_TYPE_ignore);
			
		} else {			
			Assert.assertEquals(operonToolspecific.getTransitionType().getType(), TRANSITION_TYPE_normal);
				

		}
		
		
		// check the triggers
		int wopedTriggerType = 0;
		if (null != wopedToolspecific.getTrigger()) {
			wopedTriggerType = wopedToolspecific.getTrigger().getType();
		}
		
		if (TRANSITION_TRIGGER_TYPE_message == wopedTriggerType) {
			Assert.assertNull(operonToolspecific.getAutoTrigger());
			Assert.assertNull(operonToolspecific.getManualTrigger());
			Assert.assertNotNull(operonToolspecific.getMessageTrigger());
			Assert.assertNull(operonToolspecific.getTimeTrigger());
			
			Assert.assertEquals(operonToolspecific.getMessageTrigger().getResources().getDefaultId(), DEFAULT_resource_id);

		} else if (TRANSITION_TRIGGER_TYPE_resource == wopedTriggerType) {
			Assert.assertNull(operonToolspecific.getAutoTrigger());
			Assert.assertNotNull(operonToolspecific.getManualTrigger());
			Assert.assertEquals(operonToolspecific.getManualTrigger().getResources().getDefaultId(), DEFAULT_resource_id);
			Assert.assertNull(operonToolspecific.getMessageTrigger());
			Assert.assertNull(operonToolspecific.getTimeTrigger());

		} else if (TRANSITION_TRIGGER_TYPE_time == wopedTriggerType) {
			Assert.assertNull(operonToolspecific.getAutoTrigger());
			Assert.assertNull(operonToolspecific.getManualTrigger());
			Assert.assertNull(operonToolspecific.getMessageTrigger());
			Assert.assertNotNull(operonToolspecific.getTimeTrigger());
			Assert.assertNotNull(operonToolspecific.getTimeTrigger().getExecutionTimeLimit());
			Assert.assertEquals(operonToolspecific.getTimeTrigger().getExecutionTimeLimit().getDuration(), DEFAULT_execution_duration);
			Assert.assertNotNull(operonToolspecific.getTimeTrigger().getExplicitTriggerDelayDuration());
			Assert.assertEquals(operonToolspecific.getTimeTrigger().getExplicitTriggerDelayDuration().getDuration(), DEFAULT_delay_duration);

		} else {
			if ( (null!=wopedToolspecific.isSubprocess()) && wopedToolspecific.isSubprocess()) {
				Assert.assertNull(operonToolspecific.getAutoTrigger());
				Assert.assertNull(operonToolspecific.getManualTrigger());
				Assert.assertNull(operonToolspecific.getMessageTrigger());
				Assert.assertNull(operonToolspecific.getTimeTrigger());
				
			} else {
				Assert.assertNotNull(operonToolspecific.getAutoTrigger());
				Assert.assertNull(operonToolspecific.getManualTrigger());
				Assert.assertNull(operonToolspecific.getMessageTrigger());
				Assert.assertNull(operonToolspecific.getTimeTrigger());

				Assert.assertNotNull(operonToolspecific.getAutoTrigger().getExecutionTimeLimit());
				Assert.assertEquals(operonToolspecific.getAutoTrigger().getExecutionTimeLimit().getDuration(), DEFAULT_execution_duration);
				
			}
			
		}
		
		
		return success;
	}

	

	private JAXBElement<PnmlType> _loadFirstJaxbElementByFileName(String filename) {
		String xmlStr = loadXmlStr(filename);		
		
		return OpnmlJAXBHelper.loadNetFromXmlStrReturnPnml(xmlStr, this.log);
		
	}
	
	private NetType _loadFirstNetInJaxbElement(JAXBElement<PnmlType> jaxbElement) {
		PnmlType pnmlElement = jaxbElement.getValue();
		List<NetType> netList = pnmlElement.getNet();		
		NetType[] nets = netList.toArray(new NetType[netList.size()]);		
		
		return nets[0]; 
		
	}

	
	private static String loadXmlStr(String filename) {		
			// default will use class path
			// to load up file
			ConfigurationHelper configHelper = new ConfigurationHelper();
			try {
				configHelper.createDomFromFile(filename);
				return configHelper.getDomAsString();
				
			} catch (Exception e) {
				throw new BaseSystemException(e);
			}
			
	}
		
	private static void printOutXML(JAXBElement<PnmlType> targetJAXBElement) {
		final String contextPackage = "com.hoodox.operon.jaxb.opnml";
		
		try {
			JAXBContext jc = JAXBContext.newInstance( contextPackage );
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal( targetJAXBElement, System.out );
			
		} catch (JAXBException e) {
			throw new BaseSystemException(e);
			
		}		
		
	}
		
	
}
