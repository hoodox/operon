// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 08/03/2006 12:53:41
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   PnmlNode.java

package com.hoodox.operon.wfnet;

import java.io.Serializable;

public abstract class PnmlNode implements Serializable {
	private static final long serialVersionUID = 888L;

	public PnmlNode() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getXdimension() {
		return xdimension;
	}

	public void setXdimension(Integer xdimension) {
		this.xdimension = xdimension;
	}

	public Integer getXposition() {
		return xposition;
	}

	public void setXposition(Integer xposition) {
		this.xposition = xposition;
	}

	public Integer getYdimension() {
		return ydimension;
	}

	public void setYdimension(Integer ydimension) {
		this.ydimension = ydimension;
	}

	public Integer getYposition() {
		return yposition;
	}

	public void setYposition(Integer yposition) {
		this.yposition = yposition;
	}

	private String id;
	private String name;
	private Integer xposition;
	private Integer yposition;
	private Integer xdimension;
	private Integer ydimension;
}