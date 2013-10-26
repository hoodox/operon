package com.hoodox.operon.maven.plugin.pnml;

import java.util.ArrayList;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

public class BasicOptionsProjectStub  extends MavenProjectStub {

    //-------------------
    // member variables
    //-------------------
    protected Build build = new Build();

    @SuppressWarnings("unchecked")
	public BasicOptionsProjectStub() {
        setCompileSourceRoots( new ArrayList(0) );
        setBuildOutputDirectory( System.getProperty("basedir")+"/target" );
    }

    public Build getBuild() {
        return build;
    }

    /**
     * <code>setBuildOutputDirectory</code> sets the outputdirectory
     * for the tests that the XjcMojo uses
     *
     * @param buildOutputDirectory a <code>String</code> value
     */
    public void setBuildOutputDirectory(String buildOutputDirectory) {
        System.out.println( "setting outputdir: "+buildOutputDirectory );
        build.setOutputDirectory( buildOutputDirectory );
    }
	
}
