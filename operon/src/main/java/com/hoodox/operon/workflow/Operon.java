package com.hoodox.operon.workflow;

import java.util.HashMap;
import java.util.Map;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.hoodox.commons.exceptions.BaseSystemException;
import com.hoodox.commons.exceptions.ErrorCode;
import com.hoodox.operon.exceptions.OperonInitialiseException;
import com.hoodox.operon.helper.Const;

/**
 * <p>The entry point into this application.</p>
 * 
 * <p>This class is a singleton</p>
 * 
 * <p><b>Eample Usage:</b><br>
 * Make sure that you initialise the instance with a Spring ApplicationContext.
 * </p>
 * 
 * <code><pre>
 * 	// some code before......
 * 
 *	String[] configList = {"/META-INF/applicationContext.xml","/META-INF/operon-dataAccessContext-local.xml"};
 *	ApplicationContext appCtx = new ClassPathXmlApplicationContext(configList);		
 *	Operon.getInstance().initialise("someAppName", appCtx);		
 *	WorkflowEngine workflowEngine = Operon.getInstance().getWorkflowEngine("someAppName");
 *
 *	TriggerContext triggCtx = new TriggerContext();
 *	triggCtx.getTriggerDataMap().put("SomeObj", someObject);	 
 *	Case aCase = workflowEngine.openCase("aCaseType", triggCtx);
 *
 *	//some code after......... 
 * }</pre></code>
 * 
 * @author Chung
 *
 */
public class Operon {
	//========================
	// Singleton
	//========================
	private static Operon singleInstance = new Operon();
	
	@SuppressWarnings("unchecked")
	private static Map appCtxMap = new HashMap();
	
	private Scheduler scheduler = null;
	
	
	private Operon() {
	}
	
	/**
	 * <p>Singleton - returns a single instance of the Operon</p>
	 * 
	 * <p><b>Note:</b> 
	 * Before using this class make sure this single instance is 
	 * initialised with the Spring ApplicationContext
	 * </p>
	 * @return Operon the singleton class
	 */
	public static Operon getInstance() {
		return singleInstance;
	}
	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());	
	
		

	/**
	 * <p>Initialises the Operon framework with the passed Spring ApplicationContext</p>
	 * 
	 * <p><b>Note:</b>
	 * This method must be called at least once before we can use the Singleton class</p>
	 * @param applicationName the application this particular workflow instance is associated with.
	 * @param applicationContext
	 */
	@SuppressWarnings("unchecked")
	public void initialiseApplication(String applicationName, ApplicationContext applicationContext) {
		appCtxMap.put(applicationName, applicationContext);

		WorkflowEngine workflowEngine = (WorkflowEngine) getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowEngine);
		workflowEngine._initialise(applicationName);
		
		WorkflowCoreEngine workflowCoreEngine = (WorkflowCoreEngine) getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowCoreEngine);
		workflowCoreEngine._initialise(applicationName);
		
	}
	
	public ApplicationContext getApplicationContext(String applicationName ){
		ApplicationContext applicationContext = (ApplicationContext) appCtxMap.get(applicationName);
		if (null == applicationContext) {
			BaseSystemException ex = new OperonInitialiseException(new ErrorCode(
					Const.ERROR_CODE_operon_initialise_error),
					"Cannot start WorkflowEngine for application," + applicationName + " because Spring framework ApplicationContext is not initialised and set for this particular application, please do this first");
			ex.setLogged();
			log.error(ex.getMessage(), ex);			
			throw ex;
			
		}
		return applicationContext;
	}
	
	/**
	 * Gets the workflowEngine for a particular application
	 * @param applicationName the application name
	 * @return
	 */
	public WorkflowEngine getWorkflowEngine(String applicationName) {
		return (WorkflowEngine) getApplicationContext(applicationName).getBean(Const.APPLICATION_CTX_BEAN_NAME_workflowEngine);
	}
	
	public Scheduler getScheduler() {
		if (this.scheduler != null) {
			return this.scheduler;
		}
		
		
		try {
			this.scheduler = StdSchedulerFactory.getDefaultScheduler();
	
			this.scheduler.start();
			
		} catch (SchedulerException e) {
			BaseSystemException ex = new OperonInitialiseException(new ErrorCode(
					Const.ERROR_CODE_operon_initialise_error),
					"Cannot start Scheduler for application due to some error", e );
			ex.setLogged();
			log.error(ex.getMessage(), ex);			
			throw ex;
			
		}
		  
		return this.scheduler;

		
	}
	
}
