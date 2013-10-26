package com.hoodox.operon.wfnet;

import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hoodox.commons.configurable.ConfigurationHelper;
import com.hoodox.operon.OperonBaseTest;
import com.hoodox.operon.wfnet.CaseType;
import com.hoodox.operon.wfnet.CaseTypeManager;
import com.hoodox.operon.wfnet.Place;

public class CaseTypeManagerTest extends OperonBaseTest {

	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	private String operonRegistryFilename = "OperonRegistery.xml";
	
	public static TestSuite suite() {
		return new TestSuite(CaseTypeManagerTest.class);
	}
		
	@Test(groups = { "operon.case_type_manager" })		
	public void testLoadCaseTypeManager() {
		// default will use class path
		// to load up file
		ConfigurationHelper configHelper = new ConfigurationHelper();
		CaseTypeManager caseTypeMgr = new CaseTypeManager(this.operonRegistryFilename, configHelper);
		CaseType[] caseTypes = caseTypeMgr.getAllCaseTypes();
		log.info("Found " + caseTypes.length + " CaseTypes ");
		Assert.assertEquals(caseTypes.length==1, true );
		Assert.assertEquals(caseTypes[0].getName() , "SampleNet");
		
		//===========================
		// check some places
		//===========================
		Place[] places = caseTypes[0].getPlaces();
		Assert.assertEquals(places.length==9, true);
		
//		Place startPlace = null;
//		Place sinkPlace = null;
		
		for (int i=0; i< caseTypes.length; i++) {
			//some asserts here
		}
		
		//=========================
		// Check some Transactions
		//=========================
		
	}

}
