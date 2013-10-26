USE tailorit;

-- configurations --
insert into `config_repository` values
  ('tailorit','default','ExampleNetNonWopedForamt.xml','<!-- edited with XML Spy v4.2 U (http://www.xmlspy.com) by Information Technology (Information Technology) -->\r\n<!-- An example -->\r\n<pnml xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"../../../../documents/xsd/Operonml_v-0-1.xsd\">\r\n\t<!--\r\n\t***********************************************************************\r\n\tPETRINET STUFF\r\n\t***********************************************************************\r\n\t-->\r\n\t<net id=\"SampleNet\" type=\"http://www.yasper.org/specs/epnml-1.1\">\r\n\t\t<name>\r\n\t\t\t<text>SampleNet</text>\r\n\t\t</name>\r\n\t\t<description>\r\n\t\t\t<text/>\r\n\t\t</description>\r\n\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t<!-- Specifies which resource manager implementation to use\r\n\t\t\t This is the only part that we are required to implement in order to user the framework.\r\n\t\t\t-->\r\n\t\t\t<resourceManagerInterface>com.hoodox.operon.resources.TestResourceManager</resourceManagerInterface>\r\n\t\t\t<!-- \r\n\t\t\tThis is Optional, if we wish to put a time limit for this\r\n\t\t\tnet to finish\r\n\t\t\t\r\n\t\t\tThere are two types of timer execution\r\n\t\t\t1. explicit - the Scheduler will execute straight away once expired.\r\n\t\t\t2. implicit - the Scheduler will use one of the registered schedulers\r\n\t\t\t    and only execute at the next round trip. This can reduce memory since\r\n\t\t\t    there is only a finite number of jobs where as the explicit one will create\r\n\t\t\t    a job for every single case in memory.\r\n\t\t\t-->\r\n\t\t\t<implicitTimeToLive>\r\n\t\t\t\t<!-- add days - hours - mins - seconds -->\r\n\t\t\t\t<duration>10-0-0-0</duration>\r\n\t\t\t\t<!-- \r\n\t\t\t\tScheduler to use is active is triggerType is implicit.\r\n\t\t\t\t-->\r\n\t\t\t\t<schedulerToUse ref=\"Every5Mins\"/>\r\n\t\t\t</implicitTimeToLive>\r\n\t\t\t<!--\r\n\t\t\tThis is where we registered various types of Quartz Cron Triggers\r\n\t\t\tDifferent types of triggers have different time time interval.\r\n\t\t\tEvery single Timed Transition will have a Scheduler id associated with it.\r\n\t\t\tThe Scheduler will be used to Trigger the Enbaled Time Transition\r\n\t\t\t-->\r\n\t\t\t<schedulerRegistry>\r\n\t\t\t\t<scheduler id=\"Every5Mins\">\r\n\t\t\t\t\t<cronTriggerExpression>0 0/5 * * * ?</cronTriggerExpression>\r\n\t\t\t\t</scheduler>\r\n\t\t\t</schedulerRegistry>\r\n\t\t</toolspecific>\r\n\t\t<!--\r\n\t\t***********************************************************************\r\n\t\tPlaces\r\n\t\ttype: start, intermediate, end\r\n\t\t1. Only one end and one start in a net\r\n\t\t***********************************************************************\r\n\t\t-->\r\n\t\t<place id=\"A\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>A</text>\r\n\t\t\t</name>\r\n\t\t\t<initialMarking>\r\n\t\t\t\t<text>1</text>\r\n\t\t\t</initialMarking>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"28\" y=\"53\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<sourcePlace>\r\n\t\t\t\t\t<postCreateCaseAction>com.hoodox.operon.actions.NullPostCreateCaseAction</postCreateCaseAction>\r\n\t\t\t\t\t<resources>\r\n\t\t\t\t\t\t<defaultId>packerGroup</defaultId>\r\n\t\t\t\t\t</resources>\r\n\t\t\t\t</sourcePlace>\r\n\t\t\t</toolspecific>\r\n\t\t</place>\r\n\t\t<place id=\"B\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>B</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"133\" y=\"152\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t</place>\r\n\t\t<place id=\"C\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>C</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"131\" y=\"399\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t</place>\r\n\t\t<place id=\"D\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>D</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"277\" y=\"54\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t</place>\r\n\t\t<place id=\"E\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>E</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"435\" y=\"161\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t</place>\r\n\t\t<place id=\"F\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>F</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"277\" y=\"281\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t</place>\r\n\t\t<place id=\"G\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>G</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"551\" y=\"55\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t</place>\r\n\t\t<place id=\"J\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>J</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"672\" y=\"469\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t</place>\r\n\t\t<place id=\"K\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>K</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"198\" y=\"532\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<sinkPlace>true</sinkPlace>\r\n\t\t\t</toolspecific>\r\n\t\t</place>\r\n\t\t<!--\r\n\t\t***********************************************************************\r\n\t\tTransition\r\n\t\t1. Four possible trigger-types manual, auto, time, message\r\n\t\t    (a) AUTO - this is the default, transition is automatically triggered\r\n\t\t    \t   once the transition is in READY state. Transition will FINISH\r\n\t\t    \t   when all the actions of the Transition tasks has FINISHED.\r\n\r\n\t\t    (b) MANUAL - each status after READY has to be triggered\r\n\t\t         externally, i.e. an external trigger is required to START\r\n\t\t         the transition and an external trigger lto et the Petrinet\r\n\t\t         know that the transition has FINISHED.\r\n\r\n\t\t   (c) TIME - An enabled task instance is triggered by a clock,\r\n\t\t         i.e., the task is executed at a predefined time. For example,\r\n\t\t         the task \'remove document\' is triggered if a case is trapped\r\n\t\t         in a specific state for more than 15 hours.\r\n\r\n\t\t         Once startedTransition will FINISH when\r\n\t\t         all the actions of the Transition tasks has FINISHED.\r\n\r\n\t\t   (d) MESSAGE - An external event (i.e. a message) triggers an\r\n\t\t        enabled task instance. Examples of messages are\r\n\t\t        telephone-calls, fax messages, e-mails or EDI messages.\r\n\t\t        Each of these external events will probably require some\r\n\t\t        action within an application task so that the workflow system\r\n\t\t        is made aware that the event has taken place.Once this\r\n\t\t        trggers is pulled the transition will execute and end automatically.\r\n\t\t**********************************************************************\r\n\t\t-->\r\n\t\t<transition id=\"ChargeCreditCard\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>ChargeCreditCard</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"134\" y=\"53\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<transitionType type=\"XOR_split\"/>\r\n\t\t\t\t<autoTrigger>\r\n\t\t\t\t\t<executionTimeLimit>\r\n\t\t\t\t\t\t<duration>0-0-10-0</duration>\r\n\t\t\t\t\t</executionTimeLimit>\r\n\t\t\t\t</autoTrigger>\r\n\t\t\t\t<task>\r\n\t\t\t\t\t<priorityWeighting>1</priorityWeighting>\r\n\t\t\t\t\t<estimatedCompletionTime>\r\n\t\t\t\t\t\t<duration>0-0-10-0</duration>\r\n\t\t\t\t\t</estimatedCompletionTime>\r\n\t\t\t\t\t<action>com.hoodox.operon.actions.CheckCreditCardAction</action>\r\n\t\t\t\t</task>\r\n\t\t\t</toolspecific>\r\n\t\t</transition>\r\n\t\t<transition id=\"Pack\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>Pack</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"435\" y=\"55\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<transitionType type=\"XOR_split\"/>\r\n\t\t\t\t<manualTrigger>\r\n\t\t\t\t\t<resources>\r\n\t\t\t\t\t\t<defaultId>packerGroup</defaultId>\r\n\t\t\t\t\t</resources>\r\n\t\t\t\t</manualTrigger>\r\n\t\t\t\t<!-- A Task contains many actions, For manual the actions will only be excuted\r\n\t\t\t\twhen user choses to FINISH\r\n\t\t\t-->\r\n\t\t\t\t<task>\r\n\t\t\t\t\t<priorityWeighting>1</priorityWeighting>\r\n\t\t\t\t\t<estimatedCompletionTime>\r\n\t\t\t\t\t\t<duration>0-3-0-0</duration>\r\n\t\t\t\t\t</estimatedCompletionTime>\r\n\t\t\t\t\t<action>com.hoodox.operon.actions.NullAction</action>\r\n\t\t\t\t</task>\r\n\t\t\t</toolspecific>\r\n\t\t</transition>\r\n\t\t<transition id=\"Ship\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>Ship</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"674\" y=\"55\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<transitionType type=\"normal\"/>\r\n\t\t\t\t<manualTrigger>\r\n\t\t\t\t\t<resources>\r\n\t\t\t\t\t\t<defaultId>ShipperGroup</defaultId>\r\n\t\t\t\t\t</resources>\r\n\t\t\t\t</manualTrigger>\r\n\t\t\t\t<task>\r\n\t\t\t\t\t<priorityWeighting>1</priorityWeighting>\r\n\t\t\t\t\t<estimatedCompletionTime>\r\n\t\t\t\t\t\t<duration>5-0-0-0</duration>\r\n\t\t\t\t\t</estimatedCompletionTime>\r\n\t\t\t\t\t<action>com.hoodox.operon.actions.NullAction</action>\r\n\t\t\t\t</task>\r\n\t\t\t</toolspecific>\r\n\t\t</transition>\r\n\t\t<transition id=\"SpamCustomer\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>SpamCustomer</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"133\" y=\"240\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<transitionType type=\"normal\"/>\r\n\t\t\t\t<timeTrigger>\r\n\t\t\t\t\t<executionTimeLimit>\r\n\t\t\t\t\t\t<duration>0-0-10-0</duration>\r\n\t\t\t\t\t</executionTimeLimit>\r\n\t\t\t\t\t<explicitTriggerDelayDuration>\r\n\t\t\t\t\t\t<duration>0-0-0-30</duration>\r\n\t\t\t\t\t</explicitTriggerDelayDuration>\r\n\t\t\t\t</timeTrigger>\r\n\t\t\t\t<task>\r\n\t\t\t\t\t<priorityWeighting>1</priorityWeighting>\r\n\t\t\t\t\t<estimatedCompletionTime>\r\n\t\t\t\t\t\t<duration>0-0-10-0</duration>\r\n\t\t\t\t\t</estimatedCompletionTime>\r\n\t\t\t\t\t<action>com.hoodox.operon.actions.NullAction</action>\r\n\t\t\t\t</task>\r\n\t\t\t</toolspecific>\r\n\t\t</transition>\r\n\t\t<transition id=\"UpdateBillingInfo\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>UpdateBillingInfo</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"28\" y=\"469\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<transitionType type=\"XOR_split\"/>\r\n\t\t\t\t<messageTrigger>\r\n\t\t\t\t\t<resources>\r\n\t\t\t\t\t\t<defaultId>systemGroup</defaultId>\r\n\t\t\t\t\t</resources>\r\n\t\t\t\t</messageTrigger>\r\n\t\t\t\t<task>\r\n\t\t\t\t\t<priorityWeighting>1</priorityWeighting>\r\n\t\t\t\t\t<estimatedCompletionTime>\r\n\t\t\t\t\t\t<duration>0-0-10-0</duration>\r\n\t\t\t\t\t</estimatedCompletionTime>\r\n\t\t\t\t\t<!--For message triggers the actions will only be excuted\r\n\t\t\t\tfrom START to FINISH\r\n\t\t\t-->\r\n\t\t\t\t\t<action>com.hoodox.operon.actions.NullAction</action>\r\n\t\t\t\t</task>\r\n\t\t\t</toolspecific>\r\n\t\t</transition>\r\n\t\t<transition id=\"CancelOrder\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>CancelOrder</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"342\" y=\"401\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<transitionType type=\"normal\"/>\r\n\t\t\t\t<timeTrigger>\r\n\t\t\t\t\t<executionTimeLimit>\r\n\t\t\t\t\t\t<duration>0-0-10-0</duration>\r\n\t\t\t\t\t</executionTimeLimit>\r\n\t\t\t\t\t<implicitTriggerDelayDuration>\r\n\t\t\t\t\t\t<!-- only use when trigger is time,\r\n\t\t\t\t\tadd days + hours + mins + seconds -->\r\n\t\t\t\t\t\t<duration>0-0-2-0</duration>\r\n\t\t\t\t\t\t<schedulerToUse ref=\"Every5Mins\"/>\r\n\t\t\t\t\t</implicitTriggerDelayDuration>\r\n\t\t\t\t</timeTrigger>\r\n\t\t\t\t<!-- A Task contains many actions-->\r\n\t\t\t\t<task>\r\n\t\t\t\t\t<priorityWeighting>1</priorityWeighting>\r\n\t\t\t\t\t<estimatedCompletionTime>\r\n\t\t\t\t\t\t<duration>0-0-10-0</duration>\r\n\t\t\t\t\t</estimatedCompletionTime>\r\n\t\t\t\t\t<action>com.hoodox.operon.actions.NullAction</action>\r\n\t\t\t\t</task>\r\n\t\t\t</toolspecific>\r\n\t\t</transition>\r\n\t\t<transition id=\"BackOrder\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>BackOrder</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"435\" y=\"283\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<transitionType type=\"normal\"/>\r\n\t\t\t\t<manualTrigger>\r\n\t\t\t\t\t<resources>\r\n\t\t\t\t\t\t<defaultId>systemGroup</defaultId>\r\n\t\t\t\t\t</resources>\r\n\t\t\t\t</manualTrigger>\r\n\t\t\t\t<task>\r\n\t\t\t\t\t<priorityWeighting>1</priorityWeighting>\r\n\t\t\t\t\t<estimatedCompletionTime>\r\n\t\t\t\t\t\t<duration>0-0-10-0</duration>\r\n\t\t\t\t\t</estimatedCompletionTime>\r\n\t\t\t\t\t<action>com.hoodox.operon.actions.NullAction</action>\r\n\t\t\t\t</task>\r\n\t\t\t</toolspecific>\r\n\t\t</transition>\r\n\t\t<transition id=\"Receive\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>Receive</text>\r\n\t\t\t</name>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"275\" y=\"166\"/>\r\n\t\t\t\t<dimension x=\"40\" y=\"40\"/>\r\n\t\t\t</graphics>\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<transitionType type=\"normal\"/>\r\n\t\t\t\t<manualTrigger>\r\n\t\t\t\t\t<resources>\r\n\t\t\t\t\t\t<defaultId>systemGroup</defaultId>\r\n\t\t\t\t\t</resources>\r\n\t\t\t\t</manualTrigger>\r\n\t\t\t\t<task>\r\n\t\t\t\t\t<priorityWeighting>1</priorityWeighting>\r\n\t\t\t\t\t<estimatedCompletionTime>\r\n\t\t\t\t\t\t<duration>0-0-10-0</duration>\r\n\t\t\t\t\t</estimatedCompletionTime>\r\n\t\t\t\t\t<action>com.hoodox.operon.actions.NullAction</action>\r\n\t\t\t\t</task>\r\n\t\t\t</toolspecific>\r\n\t\t</transition>\r\n\t\t<!--\r\n\t\t***********************************************************************\r\n\t\tSubnet\r\n\t\t***********************************************************************\r\n\t\t-->\r\n\t\t<page id=\"MarketSurvey\">\r\n\t\t\t<name>\r\n\t\t\t\t<text>MarketingSurvey</text>\r\n\t\t\t</name>\r\n\t\t\t<description>\r\n\t\t\t\t<text/>\r\n\t\t\t</description>\r\n\t\t\t<graphics>\r\n\t\t\t\t<position x=\"377\" y=\"534\"/>\r\n\t\t\t\t<dimension x=\"32\" y=\"32\"/>\r\n\t\t\t</graphics>\r\n\t\t\t<referencePlace ref=\"J\" id=\"Sin\">\r\n\t\t\t\t<graphics>\r\n\t\t\t\t\t<position x=\"53\" y=\"43\"/>\r\n\t\t\t\t\t<dimension x=\"20\" y=\"20\"/>\r\n\t\t\t\t</graphics>\r\n\t\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t\t<inrefPlace>\r\n\t\t\t\t\t\t<createSubcasesAction>com.hoodox.operon.example.action.CreateSubcasesAction</createSubcasesAction>\r\n\t\t\t\t\t</inrefPlace>\r\n\t\t\t\t</toolspecific>\r\n\t\t\t</referencePlace>\r\n\t\t\t<referencePlace ref=\"K\" id=\"Sout\">\r\n\t\t\t\t<graphics>\r\n\t\t\t\t\t<position x=\"549\" y=\"42\"/>\r\n\t\t\t\t\t<dimension x=\"20\" y=\"20\"/>\r\n\t\t\t\t</graphics>\r\n\t\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t\t<outrefPlace>true</outrefPlace>\r\n\t\t\t\t</toolspecific>\r\n\t\t\t</referencePlace>\r\n\t\t\t<place id=\"SP1\">\r\n\t\t\t\t<name>\r\n\t\t\t\t\t<text>SP1</text>\r\n\t\t\t\t</name>\r\n\t\t\t\t<graphics>\r\n\t\t\t\t\t<position x=\"304\" y=\"42\"/>\r\n\t\t\t\t\t<dimension x=\"20\" y=\"20\"/>\r\n\t\t\t\t</graphics>\r\n\t\t\t</place>\r\n\t\t\t<transition id=\"CallCustomer\">\r\n\t\t\t\t<name>\r\n\t\t\t\t\t<text>CallCustomer</text>\r\n\t\t\t\t</name>\r\n\t\t\t\t<description>\r\n\t\t\t\t\t<text>To Capture Marketing data</text>\r\n\t\t\t\t</description>\r\n\t\t\t\t<graphics>\r\n\t\t\t\t\t<position x=\"175\" y=\"41\"/>\r\n\t\t\t\t\t<dimension x=\"32\" y=\"32\"/>\r\n\t\t\t\t</graphics>\r\n\t\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t\t<transitionType type=\"normal\"/>\r\n\t\t\t\t\t<manualTrigger>\r\n\t\t\t\t\t\t<resources>\r\n\t\t\t\t\t\t\t<defaultId>MarketGroup</defaultId>\r\n\t\t\t\t\t\t</resources>\r\n\t\t\t\t\t</manualTrigger>\r\n\t\t\t\t\t<task>\r\n\t\t\t\t\t\t<priorityWeighting>1</priorityWeighting>\r\n\t\t\t\t\t\t<estimatedCompletionTime>\r\n\t\t\t\t\t\t\t<duration>0-0-10-0</duration>\r\n\t\t\t\t\t\t</estimatedCompletionTime>\r\n\t\t\t\t\t\t<action>com.hoodox.operon.actions.NullAction</action>\r\n\t\t\t\t\t</task>\r\n\t\t\t\t</toolspecific>\r\n\t\t\t</transition>\r\n\t\t\t<transition id=\"SaveMarketData\">\r\n\t\t\t\t<name>\r\n\t\t\t\t\t<text>SaveMarketData</text>\r\n\t\t\t\t</name>\r\n\t\t\t\t<graphics>\r\n\t\t\t\t\t<position x=\"424\" y=\"44\"/>\r\n\t\t\t\t\t<dimension x=\"32\" y=\"32\"/>\r\n\t\t\t\t</graphics>\r\n\t\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t\t<transitionType type=\"normal\"/>\r\n\t\t\t\t\t<messageTrigger>\r\n\t\t\t\t\t\t<resources>\r\n\t\t\t\t\t\t\t<defaultId>SystemGroup</defaultId>\r\n\t\t\t\t\t\t</resources>\r\n\t\t\t\t\t</messageTrigger>\r\n\t\t\t\t\t<task>\r\n\t\t\t\t\t\t<priorityWeighting>1</priorityWeighting>\r\n\t\t\t\t\t\t<estimatedCompletionTime>\r\n\t\t\t\t\t\t\t<duration>0-0-10-0</duration>\r\n\t\t\t\t\t\t</estimatedCompletionTime>\r\n\t\t\t\t\t\t<action>com.hoodox.operon.actions.NullAction</action>\r\n\t\t\t\t\t</task>\r\n\t\t\t\t</toolspecific>\r\n\t\t\t</transition>\r\n\t\t\t<arc id=\"sa1\" source=\"Sin\" target=\"CallCustomer\">\r\n\t\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t\t<arcType direction=\"in\"/>\r\n\t\t\t\t</toolspecific>\r\n\t\t\t</arc>\r\n\t\t\t<arc id=\"sa2\" source=\"CallCustomer\" target=\"SP1\">\r\n\t\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t\t<arcType direction=\"out\"/>\r\n\t\t\t\t</toolspecific>\r\n\t\t\t</arc>\r\n\t\t\t<arc id=\"sa3\" source=\"SP1\" target=\"SaveMarketData\">\r\n\t\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t\t<arcType direction=\"in\"/>\r\n\t\t\t\t</toolspecific>\r\n\t\t\t</arc>\r\n\t\t\t<arc id=\"sa4\" source=\"SaveMarketData\" target=\"Sout\">\r\n\t\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t\t<arcType direction=\"out\"/>\r\n\t\t\t\t</toolspecific>\r\n\t\t\t</arc>\r\n\t\t</page>\r\n\t\t<!--\r\n\t\t***********************************************************************\r\n\t\tThere are 2 Directions\r\n\t\t1. IN - from a Place into a Transition\r\n\t\t2. OUT - from a Transition out to a place\r\n\t\tThere are 7 different arcTypes:\r\n\r\n\t\ttype: start, intermediate, end\r\n\t\t1. normal - ordinary sequential flow\r\n\t\t2. XOR-split Explicit OR split\r\n\t\t3. OR-split Implicit OR split\r\n\t\t4. OR-join implicit OR join\r\n\t\t5. XOR-join Explicit OR join\r\n\t\t6. AND-split AND split\r\n\t\t7. AND-join AND join\r\n\t\t***********************************************************************\r\n\t\t-->\r\n\t\t<arc id=\"a1\" source=\"A\" target=\"ChargeCreditCard\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"in\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a2\" source=\"ChargeCreditCard\" target=\"D\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"out\">\r\n\t\t\t\t\t<guardExpression>success</guardExpression>\r\n\t\t\t\t</arcType>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a3\" source=\"ChargeCreditCard\" target=\"B\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"out\">\r\n\t\t\t\t\t<guardExpression>failure</guardExpression>\r\n\t\t\t\t</arcType>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a4\" source=\"B\" target=\"SpamCustomer\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"in\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a5\" source=\"SpamCustomer\" target=\"C\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"in\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a6\" source=\"C\" target=\"UpdateBillingInfo\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"in\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a7\" source=\"C\" target=\"CancelOrder\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"in\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a8\" source=\"UpdateBillingInfo\" target=\"J\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"out\">\r\n\t\t\t\t\t<guardExpression>cancelled</guardExpression>\r\n\t\t\t\t</arcType>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a9\" source=\"UpdateBillingInfo\" target=\"A\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"out\">\r\n\t\t\t\t\t<guardExpression>updated</guardExpression>\r\n\t\t\t\t</arcType>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a10\" source=\"CancelOrder\" target=\"J\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"out\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a11\" source=\"D\" target=\"Pack\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"in\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a12\" source=\"Pack\" target=\"G\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"out\">\r\n\t\t\t\t\t<guardExpression>complete</guardExpression>\r\n\t\t\t\t</arcType>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a13\" source=\"Pack\" target=\"E\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"out\">\r\n\t\t\t\t\t<guardExpression>incomplete</guardExpression>\r\n\t\t\t\t</arcType>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a14\" source=\"E\" target=\"BackOrder\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"in\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a15\" source=\"BackOrder\" target=\"F\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"out\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a16\" source=\"F\" target=\"Receive\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"in\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a17\" source=\"Receive\" target=\"D\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"out\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a18\" source=\"G\" target=\"Ship\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"in\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t\t<arc id=\"a19\" source=\"Ship\" target=\"J\">\r\n\t\t\t<toolspecific tool=\"Operon\" version=\"1.0\">\r\n\t\t\t\t<arcType direction=\"out\"/>\r\n\t\t\t</toolspecific>\r\n\t\t</arc>\r\n\t</net>\r\n</pnml>\r\n','2009-04-20 00:37:44','2009-04-20 00:37:54'),
  ('tailorit','default','OperonRegistry.xml','<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<operonRegistry xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"C:\\chungWorkspaces\\hoodoxWorkspace\\Operon\\documents\\xsd\\operon-registry_v1.0.xsd\">\r\n\t<netFiles>\r\n\t\t<filename>ExampleNet.xml</filename>\r\n\t</netFiles>\r\n</operonRegistry>','2009-04-20 01:48:18','2009-04-20 00:37:54');

  
USE tailorit;

insert into `config_repository`(`APPLICATION_NAME`,`SERVER_ID`,`FILE_NAME`,`CONTENT`,`UPDATED_DATE`,`CREATED_DATE`) values ('tailorit','default','ExampleNetNonWopedFormat.xml','
	<!--
		edited with XML Spy v4.2 U (http://www.xmlspy.com) by Information
		Technology (Information Technology)
	-->
	<!-- An example -->
<pnml xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:noNamespaceSchemaLocation="../../../../documents/xsd/pnml-operon.xsd">
	<!--
		***********************************************************************
		PETRINET STUFF
		***********************************************************************
	-->
	<net id="SampleNet" type="SampleNet">
		<name>
			<text>SampleNet</text>
		</name>
		<!--
			***********************************************************************
			Places type: start, intermediate, end 1. Only one end and one start
			in a net
			***********************************************************************
		-->
		<place id="A">
			<name>
				<text>A</text>
			</name>
			<graphics>
				<position x="28" y="53" />
				<dimension x="40" y="40" />
			</graphics>
			<initialMarking>
				<text>1</text>
			</initialMarking>
			<toolspecific tool="Operon" version="1.0">
				<sourcePlace>
					<postCreateCaseAction>com.hoodox.operon.actions.NullPostCreateCaseAction</postCreateCaseAction>
					<resources>
						<defaultId>packerGroup</defaultId>
					</resources>
				</sourcePlace>
			</toolspecific>
		</place>
		<place id="B">
			<name>
				<text>B</text>
			</name>
			<graphics>
				<position x="133" y="152" />
				<dimension x="40" y="40" />
			</graphics>
		</place>
		<place id="C">
			<name>
				<text>C</text>
			</name>
			<graphics>
				<position x="131" y="399" />
				<dimension x="40" y="40" />
			</graphics>
		</place>
		<place id="D">
			<name>
				<text>D</text>
			</name>
			<graphics>
				<position x="277" y="54" />
				<dimension x="40" y="40" />
			</graphics>
		</place>
		<place id="E">
			<name>
				<text>E</text>
			</name>
			<graphics>
				<position x="435" y="161" />
				<dimension x="40" y="40" />
			</graphics>
		</place>
		<place id="F">
			<name>
				<text>F</text>
			</name>
			<graphics>
				<position x="277" y="281" />
				<dimension x="40" y="40" />
			</graphics>
		</place>
		<place id="G">
			<name>
				<text>G</text>
			</name>
			<graphics>
				<position x="551" y="55" />
				<dimension x="40" y="40" />
			</graphics>
		</place>
		<place id="J">
			<name>
				<text>J</text>
			</name>
			<graphics>
				<position x="672" y="469" />
				<dimension x="40" y="40" />
			</graphics>
		</place>
		<place id="K">
			<name>
				<text>K</text>
			</name>
			<graphics>
				<position x="198" y="532" />
				<dimension x="40" y="40" />
			</graphics>
			<toolspecific tool="Operon" version="1.0">
				<sinkPlace>true</sinkPlace>
			</toolspecific>
		</place>
		<!--
			***********************************************************************
			Transition 1. Four possible trigger-types manual, auto, time, message
			(a) AUTO - this is the default, transition is automatically triggered
			once the transition is in READY state. Transition will FINISH when
			all the actions of the Transition tasks has FINISHED. (b) MANUAL -
			each status after READY has to be triggered externally, i.e. an
			external trigger is required to START the transition and an external
			trigger lto et the Petrinet know that the transition has FINISHED.

			(c) TIME - An enabled task instance is triggered by a clock, i.e.,
			the task is executed at a predefined time. For example, the task
			''remove document'' is triggered if a case is trapped in a specific
			state for more than 15 hours. Once startedTransition will FINISH when
			all the actions of the Transition tasks has FINISHED. (d) MESSAGE -
			An external event (i.e. a message) triggers an enabled task instance.
			Examples of messages are telephone-calls, fax messages, e-mails or
			EDI messages. Each of these external events will probably require
			some action within an application task so that the workflow system is
			made aware that the event has taken place.Once this trggers is pulled
			the transition will execute and end automatically.
			**********************************************************************
		-->
		<transition id="ChargeCreditCard">
			<name>
				<text>ChargeCreditCard</text>
			</name>
			<graphics>
				<position x="134" y="53" />
				<dimension x="40" y="40" />
			</graphics>
			<toolspecific tool="Operon" version="1.0">
				<transitionType type="XOR_split" />
				<autoTrigger>
					<executionTimeLimit>
						<duration>0-0-10-0</duration>
					</executionTimeLimit>
				</autoTrigger>
				<task>
					<priorityWeighting>1</priorityWeighting>
					<estimatedCompletionTime>
						<duration>0-0-10-0</duration>
					</estimatedCompletionTime>
					<action>com.hoodox.operon.actions.CheckCreditCardAction</action>
				</task>
			</toolspecific>
		</transition>
		<transition id="Pack">
			<name>
				<text>Pack</text>
			</name>
			<graphics>
				<position x="435" y="55" />
				<dimension x="40" y="40" />
			</graphics>
			<toolspecific tool="Operon" version="1.0">
				<transitionType type="XOR_split" />
				<manualTrigger>
					<resources>
						<defaultId>packerGroup</defaultId>
					</resources>
				</manualTrigger>
				<!--
					A Task contains many actions, For manual the actions will only be
					excuted when user choses to FINISH
				-->
				<task>
					<priorityWeighting>1</priorityWeighting>
					<estimatedCompletionTime>
						<duration>0-3-0-0</duration>
					</estimatedCompletionTime>
					<action>com.hoodox.operon.actions.NullAction</action>
				</task>
			</toolspecific>
		</transition>
		<transition id="Ship">
			<name>
				<text>Ship</text>
			</name>
			<graphics>
				<position x="674" y="55" />
				<dimension x="40" y="40" />
			</graphics>
			<toolspecific tool="Operon" version="1.0">
				<transitionType type="normal" />
				<manualTrigger>
					<resources>
						<defaultId>ShipperGroup</defaultId>
					</resources>
				</manualTrigger>
				<task>
					<priorityWeighting>1</priorityWeighting>
					<estimatedCompletionTime>
						<duration>5-0-0-0</duration>
					</estimatedCompletionTime>
					<action>com.hoodox.operon.actions.NullAction</action>
				</task>
			</toolspecific>
		</transition>
		<transition id="SpamCustomer">
			<name>
				<text>SpamCustomer</text>
			</name>
			<graphics>
				<position x="133" y="240" />
				<dimension x="40" y="40" />
			</graphics>
			<toolspecific tool="Operon" version="1.0">
				<transitionType type="normal" />
				<timeTrigger>
					<executionTimeLimit>
						<duration>0-0-10-0</duration>
					</executionTimeLimit>
					<explicitTriggerDelayDuration>
						<duration>0-0-0-30</duration>
					</explicitTriggerDelayDuration>
				</timeTrigger>
				<task>
					<priorityWeighting>1</priorityWeighting>
					<estimatedCompletionTime>
						<duration>0-0-10-0</duration>
					</estimatedCompletionTime>
					<action>com.hoodox.operon.actions.NullAction</action>
				</task>
			</toolspecific>
		</transition>
		<transition id="UpdateBillingInfo">
			<name>
				<text>UpdateBillingInfo</text>
			</name>
			<graphics>
				<position x="28" y="469" />
				<dimension x="40" y="40" />
			</graphics>
			<toolspecific tool="Operon" version="1.0">
				<transitionType type="XOR_split" />
				<messageTrigger>
					<resources>
						<defaultId>systemGroup</defaultId>
					</resources>
				</messageTrigger>
				<task>
					<priorityWeighting>1</priorityWeighting>
					<estimatedCompletionTime>
						<duration>0-0-10-0</duration>
					</estimatedCompletionTime>
					<!--
						For message triggers the actions will only be excuted from START
						to FINISH
					-->
					<action>com.hoodox.operon.actions.NullAction</action>
				</task>
			</toolspecific>
		</transition>
		<transition id="CancelOrder">
			<name>
				<text>CancelOrder</text>
			</name>
			<graphics>
				<position x="342" y="401" />
				<dimension x="40" y="40" />
			</graphics>
			<toolspecific tool="Operon" version="1.0">
				<transitionType type="normal" />
				<timeTrigger>
					<executionTimeLimit>
						<duration>0-0-10-0</duration>
					</executionTimeLimit>
					<implicitTriggerDelayDuration>
						<!--
							only use when trigger is time, add days + hours + mins + seconds
						-->
						<duration>0-0-2-0</duration>
						<schedulerToUse ref="Every5Mins" />
					</implicitTriggerDelayDuration>
				</timeTrigger>
				<!-- A Task contains many actions-->
				<task>
					<priorityWeighting>1</priorityWeighting>
					<estimatedCompletionTime>
						<duration>0-0-10-0</duration>
					</estimatedCompletionTime>
					<action>com.hoodox.operon.actions.NullAction</action>
				</task>
			</toolspecific>
		</transition>
		<transition id="BackOrder">
			<name>
				<text>BackOrder</text>
			</name>
			<graphics>
				<position x="435" y="283" />
				<dimension x="40" y="40" />
			</graphics>
			<toolspecific tool="Operon" version="1.0">
				<transitionType type="normal" />
				<manualTrigger>
					<resources>
						<defaultId>systemGroup</defaultId>
					</resources>
				</manualTrigger>
				<task>
					<priorityWeighting>1</priorityWeighting>
					<estimatedCompletionTime>
						<duration>0-0-10-0</duration>
					</estimatedCompletionTime>
					<action>com.hoodox.operon.actions.NullAction</action>
				</task>
			</toolspecific>
		</transition>
		<transition id="Receive">
			<name>
				<text>Receive</text>
			</name>
			<graphics>
				<position x="275" y="166" />
				<dimension x="40" y="40" />
			</graphics>
			<toolspecific tool="Operon" version="1.0">
				<transitionType type="normal" />
				<manualTrigger>
					<resources>
						<defaultId>systemGroup</defaultId>
					</resources>
				</manualTrigger>
				<task>
					<priorityWeighting>1</priorityWeighting>
					<estimatedCompletionTime>
						<duration>0-0-10-0</duration>
					</estimatedCompletionTime>
					<action>com.hoodox.operon.actions.NullAction</action>
				</task>
			</toolspecific>
		</transition>
		<!--
			***********************************************************************
			There are 2 Directions 1. IN - from a Place into a Transition 2. OUT
			- from a Transition out to a place There are 7 different arcTypes:

			type: start, intermediate, end 1. normal - ordinary sequential flow
			2. XOR-split Explicit OR split 3. OR-split Implicit OR split 4.
			OR-join implicit OR join 5. XOR-join Explicit OR join 6. AND-split
			AND split 7. AND-join AND join
			***********************************************************************
		-->
		<arc id="a1" source="A" target="ChargeCreditCard">
		</arc>
		<arc id="a2" source="ChargeCreditCard" target="D">
			<toolspecific tool="Operon" version="1.0">
				<guardExpression>success</guardExpression>
			</toolspecific>
		</arc>
		<arc id="a3" source="ChargeCreditCard" target="B">
			<toolspecific tool="Operon" version="1.0">
				<guardExpression>failure</guardExpression>
			</toolspecific>
		</arc>
		<arc id="a4" source="B" target="SpamCustomer">
		</arc>
		<arc id="a5" source="SpamCustomer" target="C">
		</arc>
		<arc id="a6" source="C" target="UpdateBillingInfo">
		</arc>
		<arc id="a7" source="C" target="CancelOrder">
		</arc>
		<arc id="a8" source="UpdateBillingInfo" target="J">
			<toolspecific tool="Operon" version="1.0">
				<guardExpression>cancelled</guardExpression>
			</toolspecific>
		</arc>
		<arc id="a9" source="UpdateBillingInfo" target="A">
			<toolspecific tool="Operon" version="1.0">
				<guardExpression>updated</guardExpression>
			</toolspecific>
		</arc>
		<arc id="a10" source="CancelOrder" target="J">
		</arc>
		<arc id="a11" source="D" target="Pack">
		</arc>
		<arc id="a12" source="Pack" target="G">
			<toolspecific tool="Operon" version="1.0">
				<guardExpression>complete</guardExpression>
			</toolspecific>
		</arc>
		<arc id="a13" source="Pack" target="E">
			<toolspecific tool="Operon" version="1.0">
				<guardExpression>incomplete</guardExpression>
			</toolspecific>
		</arc>
		<arc id="a14" source="E" target="BackOrder">
		</arc>
		<arc id="a15" source="BackOrder" target="F">
		</arc>
		<arc id="a16" source="F" target="Receive">
		</arc>
		<arc id="a17" source="Receive" target="D">
		</arc>
		<arc id="a18" source="G" target="Ship">
		</arc>
		<arc id="a19" source="Ship" target="J">
		</arc>

		<toolspecific tool="Operon" version="1.0">
			<!--
				Specifies which resource manager implementation to use This is the
				only part that we are required to implement in order to user the
				framework.
			-->
			<resourceManagerInterface>com.hoodox.operon.resources.TestResourceManager</resourceManagerInterface>
			<!--
				This is Optional, if we wish to put a time limit for this net to
				finish There are two types of timer execution 1. explicit - the
				Scheduler will execute straight away once expired. 2. implicit - the
				Scheduler will use one of the registered schedulers and only execute
				at the next round trip. This can reduce memory since there is only a
				finite number of jobs where as the explicit one will create a job
				for every single case in memory.
			-->
			<implicitTimeToLive>
				<!-- add days - hours - mins - seconds -->
				<duration>10-0-0-0</duration>
				<!-- 
				Scheduler to use is active is triggerType is implicit.
				-->
				<schedulerToUse ref="Every5Mins" />
			</implicitTimeToLive>
			<!--
				This is where we registered various types of Quartz Cron Triggers
				Different types of triggers have different time time interval. Every
				single Timed Transition will have a Scheduler id associated with it.
				The Scheduler will be used to Trigger the Enbaled Time Transition
			-->
			<schedulerRegistry>
				<scheduler id="Every5Mins">
					<cronTriggerExpression>0 0/5 * * * ?</cronTriggerExpression>
				</scheduler>
			</schedulerRegistry>
		</toolspecific>

		<!--
			***********************************************************************
			Subnet
			***********************************************************************
		-->
		<page id="MarketSurvey">
			<net id="MarketSurveyNet" type="xys">
				<name>
					<text>MarketingSurvey</text>
				</name>
				<referencePlace ref="J" id="Sin">
					<graphics>
						<position x="53" y="43" />
						<dimension x="20" y="20" />
					</graphics>
					<toolspecific tool="Operon" version="1.0">
						<inrefPlace>
							<createSubcasesAction>com.hoodox.operon.example.action.CreateSubcasesAction</createSubcasesAction>
						</inrefPlace>
					</toolspecific>
				</referencePlace>
				<referencePlace ref="K" id="Sout">
					<graphics>
						<position x="549" y="42" />
						<dimension x="20" y="20" />
					</graphics>
					<toolspecific tool="Operon" version="1.0">
						<outrefPlace>true</outrefPlace>
					</toolspecific>
				</referencePlace>				
				<place id="SP1">
					<name>
						<text>SP1</text>
					</name>
					<graphics>
						<position x="304" y="42" />
						<dimension x="20" y="20" />
					</graphics>
				</place>
				<transition id="CallCustomer">
					<name>
						<text>CallCustomer</text>
					</name>
					<graphics>
						<position x="175" y="41" />
						<dimension x="32" y="32" />
					</graphics>
					<toolspecific tool="Operon" version="1.0">
						<transitionType type="normal" />
						<manualTrigger>
							<resources>
								<defaultId>MarketGroup</defaultId>
							</resources>
						</manualTrigger>
						<task>
							<priorityWeighting>1</priorityWeighting>
							<estimatedCompletionTime>
								<duration>0-0-10-0</duration>
							</estimatedCompletionTime>
							<action>com.hoodox.operon.actions.NullAction</action>
						</task>
					</toolspecific>
				</transition>
				<transition id="SaveMarketData">
					<name>
						<text>SaveMarketData</text>
					</name>
					<graphics>
						<position x="424" y="44" />
						<dimension x="32" y="32" />
					</graphics>
					<toolspecific tool="Operon" version="1.0">
						<transitionType type="normal" />
						<messageTrigger>
							<resources>
								<defaultId>SystemGroup</defaultId>
							</resources>
						</messageTrigger>
						<task>
							<priorityWeighting>1</priorityWeighting>
							<estimatedCompletionTime>
								<duration>0-0-10-0</duration>
							</estimatedCompletionTime>
							<action>com.hoodox.operon.actions.NullAction</action>
						</task>
					</toolspecific>
				</transition>
				<arc id="sa1" source="Sin" target="CallCustomer">
				</arc>
				<arc id="sa2" source="CallCustomer" target="SP1">
				</arc>
				<arc id="sa3" source="SP1" target="SaveMarketData">
				</arc>
				<arc id="sa4" source="SaveMarketData" target="Sout">
				</arc>

			</net>
		</page>
	</net>
</pnml>
','2009-04-20 00:37:44','2009-04-20 00:37:54')
, ('tailorit','default','OperonRegistery.xml','<?xml version="1.0" encoding="UTF-8"?>
<operonRegistry xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="C:\chungWorkspaces\hoodoxWorkspace\Operon\documents\xsd\operon-registry_v1.0.xsd">
	<netFiles>
		<filename>ExampleNetNonWopedFormat.xml</filename>
	</netFiles>
</operonRegistry>','2009-04-20 19:31:40','2009-04-20 00:37:54');

