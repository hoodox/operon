package com.hoodox.operon.helper.jaxb;

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
import com.hoodox.operon.jaxb.opnml.ArcType;
import com.hoodox.operon.jaxb.opnml.NetType;
import com.hoodox.operon.jaxb.opnml.PlaceType;
import com.hoodox.operon.jaxb.opnml.PnmlType;
import com.hoodox.operon.jaxb.opnml.TransitionType;


public class OpnmlJaxHelperTest extends OperonBaseTest {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());	
	
	private static String FILENAME_exampleNetNonWopedFormat = "ExampleNetNonWopedFormat.xml";
	private static String FILENAME_ExampleWoped = "ExampleWoped.pnml";
	private static String FILENAME_Woped_OR_Split = "OR-Split.pnml";
	private static String FILENAME_Woped_XOR_Join = "XOR-Join.pnml";
	private static String FILENAME_Woped_SimpleSubnet = "SimpleSubnet.pnml";
	private static String FILENAME_Woped_XOR_split_join = "XOR-split-join.pnml";
	private static String FILENAME_Woped_XOR_join_AND_split = "XOR-join-AND-split.pnml";
	private static String FILENAME_Woped_AND_join_XOR_split = "AND-join-XOR-split.pnml";
	
	
	
	//================== Test begins here ======================//
	
	/**
	 * This test was the original Operon net without the Woped
	 * Merge. This test for backward compatibility.
	 */
	@Test(groups = { "operon.jaxb.helper" })		
	public void testLoadNet () {
		String xmlStr = loadXmlStr(FILENAME_exampleNetNonWopedFormat);		
		NetType[] nets = OpnmlJAXBHelper.loadNetFromXmlStr(xmlStr, this.log);
		Assert.assertEquals(nets.length==1, true);
		Assert.assertEquals(nets[0].getId(), "SampleNet");		
	}

	/**
	 * This test tests we can load a Woped file.
	 */
	@Test(groups = { "operon.jaxb.helper" })	
	public void testLoadWopedNet () {
		String xmlStr = loadXmlStr(FILENAME_ExampleWoped);		
		NetType[] nets = OpnmlJAXBHelper.loadNetFromXmlStr(xmlStr, this.log);
		Assert.assertEquals(nets.length==1, true);
		Assert.assertEquals(nets[0].getId(), "Woped");		

	}
	
	/**
	 * The Woped OR_Split file has:
	 * 1) 3 Places
	 * 2) 2 Transitions (t1_op_1) and (t1_op_2). 
	 *    These transitions should be merged to (t1_op_1)
	 * 3) 4 Arcs of which 2 are duplicates, both have p1->t1_op_1 and p1->t1_op_2.
	 * 
	 * Result:
	 * 
	 * 1) We want to get rid of t1_op_2
	 * 2) Get rid of arcs that point to t1_op_2 i.e. p1->t1_op_2
	 * 4) Result would be:
	 *    a) 3 places - nothing changed.
	 *    b) 1 Transition (t1_op_1)
	 *    c) 3 arcs - removed p1->t1_op_2
	 */
	@Test(groups = { "operon.jaxb.helper" })
	public void testCleanupWoped_OR_Split_forOperon () {
		
		String xmlStr = loadXmlStr(FILENAME_Woped_OR_Split);		
		
		JAXBElement<PnmlType> jaxbElement = OpnmlJAXBHelper.loadNetFromXmlStrReturnPnml(xmlStr, this.log);
		PnmlType pnmlElement = jaxbElement.getValue();
		List<NetType> netList = pnmlElement.getNet();		
		NetType[] nets = netList.toArray(new NetType[netList.size()]);		
		
		WopedJAXBHelper.prepareWopedForOperon(nets);

		NetType changeNet = nets[0]; 
		Assert.assertEquals(nets.length==1, true);
		Assert.assertEquals(changeNet.getId(), "Woped_OR_Split");	
		Assert.assertEquals(changeNet.getPlace().size()==3, true);	
		Assert.assertEquals(changeNet.getTransition().size()==1, true);	
		Assert.assertEquals(changeNet.getTransition().get(0).getId(), "t1_op_1");	
		Assert.assertEquals(changeNet.getArc().size()==3, true);	
		
		List<ArcType> arcs = changeNet.getArc();
		for (ArcType anArc : arcs) {
			String source = null;
			String target = null;
			
			if (anArc.getSource() instanceof PlaceType) {
				source = ((PlaceType) anArc.getSource()).getId();
			} else {
				source = ((TransitionType) anArc.getSource()).getId();
				
			}
			
			if (anArc.getTarget() instanceof PlaceType) {
				target = ((PlaceType) anArc.getTarget()).getId();
			} else {
				target = ((TransitionType) anArc.getTarget()).getId();
				
			}
			
			// no arc of p1->t1_op_2
			Assert.assertEquals(source.equals("p1") && target.equals("t1_op_2"), false);	
			
			//should not find any arc with reference to t1_op_2
			// because it has been removed.
			Assert.assertEquals(source.equals("t1_op_2"), false);				
			Assert.assertEquals(target.equals("t1_op_2"), false);				
			
													
		}
		
		
		
		String contextPackage = "com.hoodox.operon.jaxb.opnml";
		
		try {
			JAXBContext jc = JAXBContext.newInstance( contextPackage );
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal( jaxbElement, System.out );
			
		} catch (JAXBException e) {
			throw new BaseSystemException(e);
			
		}		
		
		
	}

	/**
	 * The Woped XOR_Join file has:
	 * 1) 3 Places
	 * 2) 2 Transitions (t1_op_1) and (t1_op_2). 
	 *    These transitions should be merged to (t1_op_1)
	 * 3) 4 Arcs of which 2 are duplicates, both have t1_op_1->p3 and t1_op_2->p3.
	 * 
	 * Result:
	 * 
	 * 1) We want to get rid of t1_op_2
	 * 2) Get rid of arcs that point from t1_op_2 i.e. t1_op_2 -> p3
	 * 4) Result would be:
	 *    a) 3 places - nothing changed.
	 *    b) 1 Transition (t1_op_1) - removed t1_op_2
	 *    c) 3 arcs - removed t1_op_2->p3
	 */
	@Test(groups = { "operon.jaxb.helper" })
	public void testCleanupWoped_XOR_Join_forOperon () {
		
		String xmlStr = loadXmlStr(FILENAME_Woped_XOR_Join);		
		
		JAXBElement<PnmlType> jaxbElement = OpnmlJAXBHelper.loadNetFromXmlStrReturnPnml(xmlStr, this.log);
		PnmlType pnmlElement = jaxbElement.getValue();
		List<NetType> netList = pnmlElement.getNet();		
		NetType[] nets = netList.toArray(new NetType[netList.size()]);		
		
		WopedJAXBHelper.prepareWopedForOperon(nets);

		NetType changeNet = nets[0]; 
		Assert.assertEquals(nets.length==1, true);
		Assert.assertEquals(changeNet.getId(), "Woped_XOR_Join");	
		Assert.assertEquals(changeNet.getPlace().size()==3, true);	
		Assert.assertEquals(changeNet.getTransition().size()==1, true);	
		Assert.assertEquals(changeNet.getTransition().get(0).getId(), "t1_op_1");	
		Assert.assertEquals(changeNet.getArc().size()==3, true);	
		
		List<ArcType> arcs = changeNet.getArc();
		for (ArcType anArc : arcs) {
			String source = null;
			String target = null;
			
			if (anArc.getSource() instanceof PlaceType) {
				source = ((PlaceType) anArc.getSource()).getId();
			} else {
				source = ((TransitionType) anArc.getSource()).getId();
				
			}
			
			if (anArc.getTarget() instanceof PlaceType) {
				target = ((PlaceType) anArc.getTarget()).getId();
			} else {
				target = ((TransitionType) anArc.getTarget()).getId();
				
			}
			
			
			// no arc of t1_op_2->p3
			Assert.assertEquals(source.equals("t1_op_2") && target.equals("p3"), false);
			
			//should not find any arc with reference to t1_op_2
			// because it has been removed.
			Assert.assertEquals(source.equals("t1_op_2"), false);				
			Assert.assertEquals(target.equals("t1_op_2"), false);				
			
		}
		
		
		
		String contextPackage = "com.hoodox.operon.jaxb.opnml";
		
		try {
			JAXBContext jc = JAXBContext.newInstance( contextPackage );
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal( jaxbElement, System.out );
			
		} catch (JAXBException e) {
			throw new BaseSystemException(e);
			
		}		
		
		
	}

	
	/**
	 * Make sure we are able to parse the subnet without changing anything.
	 * 
	 * 1) 3 Places
	 * 2) 2 Transitions (t1_op_1) and (t1_op_2). 
	 *    These transitions should be merged to (t1_op_1)
	 * 3) 4 Arcs of which 2 are duplicates, both have t1_op_1->p3 and t1_op_2->p3.
	 * 4) Subnet should have 3 Places, 2 Transition, and 4 arcs.
	 * 
	 */
	@Test(groups = { "operon.jaxb.helper" })
	public void testCleanupWoped_Subnets_forOperon () {
		
		String xmlStr = loadXmlStr(FILENAME_Woped_SimpleSubnet);		
		
		JAXBElement<PnmlType> jaxbElement = OpnmlJAXBHelper.loadNetFromXmlStrReturnPnml(xmlStr, this.log);
		PnmlType pnmlElement = jaxbElement.getValue();
		List<NetType> netList = pnmlElement.getNet();		
		NetType[] nets = netList.toArray(new NetType[netList.size()]);		
		
		WopedJAXBHelper.prepareWopedForOperon(nets);

		NetType changeNet = nets[0]; 
		Assert.assertEquals(nets.length==1, true);
		Assert.assertEquals(changeNet.getId(), "SimpleSubnet");	
		Assert.assertEquals(changeNet.getPlace().size()==3, true);	
		Assert.assertEquals(changeNet.getTransition().size()==2, true);	
		Assert.assertEquals(changeNet.getArc().size()==4, true);	
		Assert.assertEquals(changeNet.getPage().size()==1, true);	
		Assert.assertEquals(changeNet.getPage().get(0).getNet().size()==1, true);	
		Assert.assertEquals(changeNet.getPage().get(0).getId(), "sub1");	
		
		NetType subNet = changeNet.getPage().get(0).getNet().get(0);
		Assert.assertEquals(subNet.getPlace().size()==3, true);	
		Assert.assertEquals(subNet.getTransition().size()==2, true);	
		Assert.assertEquals(subNet.getArc().size()==4, true);	
		
		String contextPackage = "com.hoodox.operon.jaxb.opnml";
		
		try {
			JAXBContext jc = JAXBContext.newInstance( contextPackage );
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal( jaxbElement, System.out );
			
		} catch (JAXBException e) {
			throw new BaseSystemException(e);
			
		}		
		
		
	}
	

	/**
	 * The Woped XOR_split_join file has:
	 * 1) 3 Places
	 * 2) 4 Transitions (t1_op_1),(t1_op_2),(t1_op_3), (t1_op_4) . 
	 *    These transitions should be merged to (t1_op_1)
	 * 3) 6 Arcs of which 4 should be removed 
	 *    (t1_op_3->CENTER_PLACE_t1) and (t1_op_4->CENTER_PLACE_t1),
	 *    (CENTER_PLACE_t1->t1_op_1),(CENTER_PLACE_t1->t1_op_2) 
	 * 
	 * Result:
	 * 
	 * 1) We want to get rid of t1_op_2, t1_op_3, t1_op_4
	 * 2) Update the arcs reference of the one we are removing to t1_op_1.
	 * 3) We want to get rid of center places
	 * 4) Remove arcs that references center places.
	 * 5) Result would be:
	 *    a) 4 places - removed 1 CENTER_PLACE_T1.
	 *    b) 1 Transition (t1_op_1) - removed 3 places t1_op_2, t1_op_3, t1_op_4
	 *    c) 4 arcs - removed 4 arcs. (t1_op_3->CENTER_PLACE_T1), (t1_op_4->CENTER_PLACE_T1)
	 *       (CENTER_PLACE_T1->t1_op_1), (CENTER_PLACE_T1->t1_op_2)  
	 */
	@Test(groups = { "operon.jaxb.helper" })
	public void testCleanupWoped_XOR_split_join_forOperon () {
		
		String xmlStr = loadXmlStr(FILENAME_Woped_XOR_split_join);		
		
		JAXBElement<PnmlType> jaxbElement = OpnmlJAXBHelper.loadNetFromXmlStrReturnPnml(xmlStr, this.log);
		PnmlType pnmlElement = jaxbElement.getValue();
		List<NetType> netList = pnmlElement.getNet();		
		NetType[] nets = netList.toArray(new NetType[netList.size()]);		
		
		WopedJAXBHelper.prepareWopedForOperon(nets);

		NetType changeNet = nets[0]; 
		Assert.assertEquals(nets.length==1, true);
		Assert.assertEquals(changeNet.getId(), "Woped_XOR_split_join");	
		Assert.assertEquals(changeNet.getPlace().size()==4, true);	
		Assert.assertEquals(changeNet.getTransition().size()==1, true);	
		Assert.assertEquals(changeNet.getTransition().get(0).getId(), "t1_op_1");	
		Assert.assertEquals(changeNet.getArc().size()==4, true);	
		
		
		List<ArcType> arcs = changeNet.getArc();
		for (ArcType anArc : arcs) {
			String source = null;
			String target = null;
			
			if (anArc.getSource() instanceof PlaceType) {
				source = ((PlaceType) anArc.getSource()).getId();
			} else {
				source = ((TransitionType) anArc.getSource()).getId();
				
			}
			
			if (anArc.getTarget() instanceof PlaceType) {
				target = ((PlaceType) anArc.getTarget()).getId();
			} else {
				target = ((TransitionType) anArc.getTarget()).getId();
				
			}
			
			 // 
			 //       (CENTER_PLACE_T1->t1_op_1), (CENTER_PLACE_T1->t1_op_2)  
			
			// no arcs of (t1_op_3->CENTER_PLACE_T1), (t1_op_4->CENTER_PLACE_T1)
			// (CENTER_PLACE_T1->t1_op_1), (CENTER_PLACE_T1->t1_op_2)
			Assert.assertEquals(source.equals("t1_op_3") && target.equals("CENTER_PLACE_t1"), false);
			Assert.assertEquals(source.equals("t1_op_4") && target.equals("CENTER_PLACE_t1"), false);
			Assert.assertEquals(source.equals("CENTER_PLACE_t1") && target.equals("t1_op_1"), false);
			Assert.assertEquals(source.equals("CENTER_PLACE_t1") && target.equals("t1_op_2"), false);
			
			//should not find any arc with reference to t1_op_2, t1_op_3, t1_op_4 or CENTER_PLACE_t1
			// because it has been removed.
			Assert.assertEquals(source.equals("t1_op_2"), false);				
			Assert.assertEquals(target.equals("t1_op_2"), false);				
			Assert.assertEquals(source.equals("t1_op_3"), false);				
			Assert.assertEquals(target.equals("t1_op_3"), false);				
			Assert.assertEquals(source.equals("t1_op_4"), false);				
			Assert.assertEquals(target.equals("t1_op_4"), false);				
			Assert.assertEquals(source.equals("CENTER_PLACE_t1"), false);				
			Assert.assertEquals(target.equals("CENTER_PLACE_t1"), false);				
			
		}
		
		
		String contextPackage = "com.hoodox.operon.jaxb.opnml";
		
		try {
			JAXBContext jc = JAXBContext.newInstance( contextPackage );
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal( jaxbElement, System.out );
			
		} catch (JAXBException e) {
			throw new BaseSystemException(e);
			
		}		
		
		
	}

	/**
	 * The Woped XOR_Join_AND_split file has:
	 * 1) 5 Places
	 * 2) 3 Transitions (t1_op_1), (t1_op_2, (t1_op_3)). 
	 *    These transitions should be merged to (t1_op_1)
	 * 3) 6 Arcs of which 2 is a CENTER_PLACE arc.
	 * 
	 * Result:
	 * 
	 * 1) We want to get rid of t1_op_2, t1_op_3.
	 * 2) Update the arcs reference of the one we are removing to t1_op_1.
	 * 3) We want to get rid of center places
	 * 4) Remove arcs that references center places.
	 * 5) Result would be:
	 *    a) 4 places - removed 1 CENTER_PLACE_T1.
	 *    b) 1 Transition (t1_op_1) - removed 2 places t1_op_2, t1_op_3.
	 *    c) 4 arcs - removed 3 arcs. (t1_op_1->CENTER_PLACE_T1), (t1_op_2->CENTER_PLACE_T1)
	 *       (CENTER_PLACE_T1->t1_op_3).  
	 */
	@Test(groups = { "operon.jaxb.helper" })
	public void testCleanupWoped_XOR_join_AND_split_forOperon () {
		
		String xmlStr = loadXmlStr(FILENAME_Woped_XOR_join_AND_split);		
		
		JAXBElement<PnmlType> jaxbElement = OpnmlJAXBHelper.loadNetFromXmlStrReturnPnml(xmlStr, this.log);
		PnmlType pnmlElement = jaxbElement.getValue();
		List<NetType> netList = pnmlElement.getNet();		
		NetType[] nets = netList.toArray(new NetType[netList.size()]);		
		
		WopedJAXBHelper.prepareWopedForOperon(nets);

		NetType changeNet = nets[0]; 
		Assert.assertEquals(nets.length==1, true);
		Assert.assertEquals(changeNet.getId(), "Woped_XOR_join_AND_split");	
		Assert.assertEquals(changeNet.getPlace().size()==4, true);	
		Assert.assertEquals(changeNet.getTransition().size()==1, true);	
		Assert.assertEquals(changeNet.getTransition().get(0).getId(), "t1_op_1");	
		Assert.assertEquals(changeNet.getArc().size()==4, true);	
		
		
		List<ArcType> arcs = changeNet.getArc();
		for (ArcType anArc : arcs) {
			String source = null;
			String target = null;
			
			if (anArc.getSource() instanceof PlaceType) {
				source = ((PlaceType) anArc.getSource()).getId();
			} else {
				source = ((TransitionType) anArc.getSource()).getId();
				
			}
			
			if (anArc.getTarget() instanceof PlaceType) {
				target = ((PlaceType) anArc.getTarget()).getId();
			} else {
				target = ((TransitionType) anArc.getTarget()).getId();
				
			}
			
			
			// no arcs of (t1_op_1->CENTER_PLACE_t1), (t1_op_2->CENTER_PLACE_t1)
			// (CENTER_PLACE_t1->t1_op_3)
			Assert.assertEquals(source.equals("t1_op_1") && target.equals("CENTER_PLACE_t1"), false);
			Assert.assertEquals(source.equals("t1_op_2") && target.equals("CENTER_PLACE_t1"), false);
			Assert.assertEquals(source.equals("CENTER_PLACE_t1") && target.equals("t1_op_3"), false);
			
			//should not find any arc with reference to t1_op_2, t1_op_3 or CENTER_PLACE_t1
			// because it has been removed.
			Assert.assertEquals(source.equals("t1_op_2"), false);				
			Assert.assertEquals(target.equals("t1_op_2"), false);				
			Assert.assertEquals(source.equals("t1_op_3"), false);				
			Assert.assertEquals(target.equals("t1_op_3"), false);				
			Assert.assertEquals(source.equals("CENTER_PLACE_t1"), false);				
			Assert.assertEquals(target.equals("CENTER_PLACE_t1"), false);				
			
		}
		
		
		String contextPackage = "com.hoodox.operon.jaxb.opnml";
		
		try {
			JAXBContext jc = JAXBContext.newInstance( contextPackage );
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal( jaxbElement, System.out );
			
		} catch (JAXBException e) {
			throw new BaseSystemException(e);
			
		}		
		
		
	}
	

	/**
	 * The Woped AND_join_XOR_split file has:
	 * 1) 5 Places
	 * 2) 3 Transitions (t1_op_1), (t1_op_2, (t1_op_3)). 
	 *    These transitions should be merged to (t1_op_1)
	 * 3) 7 Arcs of which 3 are CENTER_PLACE arcs.
	 * 
	 * Result:
	 * 
	 * 1) We want to get rid of t1_op_2, t1_op_3.
	 * 2) Update the arcs reference of the one we are removing to t1_op_1.
	 * 3) We want to get rid of the center place.
	 * 4) Remove arcs that references center place.
	 * 5) Result would be:
	 *    a) 4 places - removed 1 CENTER_PLACE_T1.
	 *    b) 1 Transition (t1_op_1) - removed 2 places t1_op_2, t1_op_3.
	 *    c) 4 arcs - removed 3 arcs. (t1_op_1->CENTER_PLACE_T1),
	 *       (CENTER_PLACE_T1->t1_op_2), (CENTER_PLACE_T1->t1_op_3).  
	 */
	@Test(groups = { "operon.jaxb.helper" })
	public void testCleanupWoped_AND_join_OR_split_forOperon () {
		
		String xmlStr = loadXmlStr(FILENAME_Woped_AND_join_XOR_split);		
		
		JAXBElement<PnmlType> jaxbElement = OpnmlJAXBHelper.loadNetFromXmlStrReturnPnml(xmlStr, this.log);
		PnmlType pnmlElement = jaxbElement.getValue();
		List<NetType> netList = pnmlElement.getNet();		
		NetType[] nets = netList.toArray(new NetType[netList.size()]);		
		
		WopedJAXBHelper.prepareWopedForOperon(nets);

		NetType changeNet = nets[0]; 
		Assert.assertEquals(nets.length==1, true);
		Assert.assertEquals(changeNet.getId(), "Woped_AND_join_XOR_split");	
		Assert.assertEquals(changeNet.getPlace().size()==4, true);	
		Assert.assertEquals(changeNet.getTransition().size()==1, true);	
		Assert.assertEquals(changeNet.getTransition().get(0).getId(), "t1_op_1");	
		Assert.assertEquals(changeNet.getArc().size()==4, true);	
		
		
		List<ArcType> arcs = changeNet.getArc();
		for (ArcType anArc : arcs) {
			String source = null;
			String target = null;
			
			if (anArc.getSource() instanceof PlaceType) {
				source = ((PlaceType) anArc.getSource()).getId();
			} else {
				source = ((TransitionType) anArc.getSource()).getId();
				
			}
			
			if (anArc.getTarget() instanceof PlaceType) {
				target = ((PlaceType) anArc.getTarget()).getId();
			} else {
				target = ((TransitionType) anArc.getTarget()).getId();
				
			}
			
			
			// no arcs of (t1_op_1->CENTER_PLACE_t1), (t1_op_2->CENTER_PLACE_t1)
			// (CENTER_PLACE_t1->t1_op_3)
			Assert.assertEquals(source.equals("t1_op_1") && target.equals("CENTER_PLACE_t1"), false);
			Assert.assertEquals(source.equals("CENTER_PLACE_t1") && target.equals("t1_op_2"), false);
			Assert.assertEquals(source.equals("CENTER_PLACE_t1") && target.equals("t1_op_3"), false);
			
			//should not find any arc with reference to t1_op_2, t1_op_3 or CENTER_PLACE_t1
			// because it has been removed.
			Assert.assertEquals(source.equals("t1_op_2"), false);				
			Assert.assertEquals(target.equals("t1_op_2"), false);				
			Assert.assertEquals(source.equals("t1_op_3"), false);				
			Assert.assertEquals(target.equals("t1_op_3"), false);				
			Assert.assertEquals(source.equals("CENTER_PLACE_t1"), false);				
			Assert.assertEquals(target.equals("CENTER_PLACE_t1"), false);				
			
		}
		
		
		String contextPackage = "com.hoodox.operon.jaxb.opnml";
		
		try {
			JAXBContext jc = JAXBContext.newInstance( contextPackage );
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal( jaxbElement, System.out );
			
		} catch (JAXBException e) {
			throw new BaseSystemException(e);
			
		}		
		
		
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
		
		
	
}
