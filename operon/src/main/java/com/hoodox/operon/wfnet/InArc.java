package com.hoodox.operon.wfnet;

/**
 * An arc into the Transition
 * @author huac
 *
 */
public class InArc extends Arc {


	public InArc() {
		super();
		// TODO Auto-generated constructor stub
	}
		
	public Place getSource() {
		return source;
	}
	public void setSource(Place source) {
		this.source = source;
	}
	public Transition getTarget() {
		return target;
	}
	public void setTarget(Transition target) {
		this.target = target;
	}
	
	private static final long serialVersionUID = -4912659315042614249L;
	private Place source;
	private Transition target;

}
