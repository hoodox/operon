====================================
Prerequisites
====================================
1. Java JDK 1.4 and above
2. Eclipse 3.0 and above
3. maven2
4. Mysql
5. Eclipse Plugins
		|-- Sublclipse
6. TortoiseSvn

============================================
Setup Subclipse - Subversion eclipse plugin
=============================================
1. In eclipse Help->software updates -> Find and Install 
	-> search for new features to install
	-> new remote site
	-> enter name=subclipse url=http://subclipse.tigris.org/update
		and install the plugin.
		
2. 	If you are using a proxy server then edit the servers file located in
	<user>\Application Data\Subversion

	e.g.
	C:\Documents and Settings\username\Application Data\Subversion
	
	Here is an example of the config
	http-proxy-host = cacheflow1.aon.co.uk
	http-proxy-port = 8080
	http-proxy-username = huac
	http-proxy-password = somepassword
	http-compression = no

======================================
Set up your Maven project environment
======================================
1. 	You will need to install maven2.

2. 	Start by configuring your work environment. You will often need to define and 
	configure environment or user-specific parameters that should not be 
	distributed to all users. 
	
	In Maven 2, there is settings.xml file, which goes in the 
	<user>/.m2 directory. <user> normaly means $HOME for Unix and 
	C:\Documents and Settings\<username> for Windows
	
	If you have not got the settings.xml file then you can find
	a default copy in the <MAVEN_HOME>/config directory. Copy this file to the 
	<user>/.m2 directory and edit it, alternatively if you want to make
	your settings.xml file global then just edit the file in the <MAVEN_HOME>/config


3. 	If you are behind a firewall with a proxy, 
	you need to configure the proxy settings so that Maven can download JARs from 
	repositories on the Web. 


	Here is an example:

	<?xml version="1.0" encoding="UTF-8"?>
	<settings>
	  <proxies>
		<proxy>
			<id>aonProxy</id>
			<active>true</active>
			<protocol>http</protocol>
			<username>username here</username>
			<password>password here</password>
			<host>cacheflow1.aon.co.uk</host>
			<port>8080</port>
			<nonProxyHosts>127.0.0.1,localhost:49213,127.0.0.1,*.aon.com,*aon.co.uk,192.85.234.203</nonProxyHosts>
		</proxy>
	  </proxies>
	</settings> 

4. 	Point to your chosen Local Repository - the default is <user>/.m2/repository
	For example 
	<localRepository>C:/Downloads/M2_REPO</localRepository>
 
5. 	Default remote repository for maven2 is 
	http://www.ibiblio.org/maven2/
	
	Just a NOTE:
	The directory structure for pom dependency is
		groupId
			|---artifactId
					|--version
						  |--<artifactId>-<version>.<type>

======================================
 Checking out project
======================================
1. Download the latest version of TortioseSvn from http://tortoisesvn.tigris.org/.

2. Create a folder called Operon e.g. c:\chungWorkspace\Operon

3. Under WindowExplorer right click on the Operon directory -> Svn Checkout
   url=https://opensvn.csie.org/Hoodox/operon/trunks
   
4. Once all files are checked out, in the <createFolder dirctory>/Operon create a maven2setupEnv.bat 
   from the maven2setupEnv.bat.sample 
   
5. Open the a dos promp, cd into <createFolder dirctory>/operon and run the newly created maven2setupEnv.bat.     

6. cd into the <createFolder dirctory>/opnmlJAXB folder and
   type> mvn install
   this will install the opnmlJAXB.jar in your local repository.
      
5. 	cd back into <createFolder dirctory>/operon and
    type >mvn test
	to compile the code.
	
7. 	Start Eclipse and add a classpath variable M2_REPO to Eclipse (this points to 
	your local maven2 repository).
8. 	Import the newly compiled project into Eclipse). Project location is where you checked out the
	project e.g. C:\chungWorkspaces\Operon
  
==============================================
Configuring Mysql
============================================== 
Installing
-----------
1. Download the Mysql 5.0.19 Community edition
2. Install Mysql on your computer.
3. Choose Typical -> Developer Machine -> Multifunctional Databae -> Decision Support ->
	Use default values from here. 
	Make sure we are supporting multiple chararacter set
	
Creating the database
---------------------	
1. If you are not installing as a Window service then to start
	open a dos promt and cd to C:\Program Files\MySQL\MySQL Server 5.0\bin and type
	>mysqld --console

2. run program files -> mysql -> mysql 5.0 -> MySql Command Line Client
	Note: your root password is blank by default
	
	or connect to Mysql server using the command line
	>mysql -h localhost -u root
	
3. Create the database run the script 
	--> create database operon;

4. Create a new user giving them access to the database
	-->grant all privileges on operon.* to operonuser@"localhost" identified by 'operonuser';

5. Switch to use the newly created database
	--> use operon;

6. Create the database schemas by running the script operon-mysql-schema.sql
	->source <createFolder dirctory>\Operon\src\resources\META-INF\db\mysql\operon-mysql-schema.sql;
	e.g. -->source C:\chungWorkspaces\hoodoxWorkspace\Operon\src\resources\META-INF\db\mysql\operon-mysql-schema.sql; 
	 
7. The database is now created.

8. 	Note: you can remove the schema by running script
	-->source <createFolder dirctory>\Operon\src\resources\META-INF\db\mysql\operon-mysql-drop-schema.sql;
	e.g. -->source C:\chungWorkspaces\hoodoxWorkspace\Operon\src\resources\META-INF\db\mysql\operon-mysql-drop-schema.sql;

9. If you wish to stop the server via the comman line type
	-->mysqladmin shutdown -u root
	
==============================================
Building the project
==============================================

==============================================
Other notes
==============================================
-----------------------------------
JAXB2.0 required runtime libraries
-----------------------------------
$JAXB_HOME/lib/jaxb-api.jar 
$JAXB_HOME/lib/jaxb-impl.jar 
$JAXB_HOME/lib/jsr173_0.1_api 
$JAXB_HOME/lib/jaxb1-impl.jar (Only required when deploying JAXB 1.0 apps) 

Command Example below shows how to install in Maven2:
mvn install:install-file -Dfile=C:\Programs\jwsdp-2.0\jaxb\lib\jaxb-api.jar -DgroupId=javax.xml -DartifactId=jaxb-api -Dversion=2.0_EA -Dpackaging=jar
mvn install:install-file -Dfile=C:\Programs\jwsdp-2.0\jaxb\lib\jaxb-impl.jar -DgroupId=javax.xml -DartifactId=jaxb-impl -Dversion=2.0_EA -Dpackaging=jar
mvn install:install-file -Dfile=C:\Programs\jwsdp-2.0\sjsxp\lib\jsr173_api.jar -DgroupId=javax.xml -DartifactId=jsr173 -Dversion=1.0 -Dpackaging=jar

------------------------
Useful Maven Commands
------------------------
mvn compile --> compiles the souce code
mvn test --> test the source code
mvn package --> 
mvn test-compile  --> compiles the source code only
mvn package --> makes a jar
mvn install --> installs the packaged jar to the local repository
mvn install -Dmaven.test.skip=true  --> installs without runing test
mvn deploy --> to deploy the jar to a remote repository