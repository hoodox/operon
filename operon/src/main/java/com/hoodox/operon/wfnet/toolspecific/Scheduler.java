// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 08/03/2006 12:47:06
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Scheduler.java

package com.hoodox.operon.wfnet.toolspecific;

import java.io.Serializable;

public class Scheduler
    implements Serializable
{

    public Scheduler()
    {
    }

    public String getCronTriggerExpression()
    {
        return cronTriggerExpression;
    }

    public void setCronTriggerExpression(String cronTriggerExpression)
    {
        this.cronTriggerExpression = cronTriggerExpression;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    private static final long serialVersionUID = 0xbe593b8edbaf868dL;
    private String id;
    private String cronTriggerExpression;
}