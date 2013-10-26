package com.hoodox.operon.maven.plugin.pnml;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 * Test that the basic configuration options work
 * 
 * @author huac
 *
 */
public class BasicOptionsTest extends AbstractMojoTestCase {

    protected File outputLocationDirectory;

    protected void setUp() throws Exception {
    	
        super.setUp();
        outputLocationDirectory = new File( getBasedir()+"/target/test-generated-sources/operon/plugin" );
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    
    /**
     * pull in a specific test pom and bootstrap the XjcMojo
     *
     * @param pomPath a <code>String</code> value
     * @return a <code>OperonWopedMergeMojo</code> value
     * @exception Exception if an error occurs
     */
    private OperonWopedMergeMojo configureMojo( String pomPath ) throws Exception {
        //configure the mojo with our test pom
        File pom = new File( getBasedir(), pomPath );
        OperonWopedMergeMojo operonMojo = new OperonWopedMergeMojo();
        operonMojo = (OperonWopedMergeMojo) configureMojo( operonMojo, "maven-operon-woped-merge-plugin", pom );
        assertNotNull( operonMojo );

        //return the configured mojo
        return operonMojo;
    }
    
    
    /**
     * Tests the merge of two files
     */
    public void testMerge() throws Exception {
    	//setup test #1
    	OperonWopedMergeMojo operonMojo = configureMojo( "src/test/resources/test1-pom.xml" );

        //execute the project
    	operonMojo.execute();

        //check output
        //String [] filesInOutputLocation = outputLocationDirectory.list();    	
    }
    
}
