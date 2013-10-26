package com.hoodox.operon.wfnet;

/**
 * An arc from Transition out to a Place
 * @author huac
 *
 */
public class OutArc extends Arc {
	
	private static final long serialVersionUID = 4806159535387801608L;

	private Transition source ;
	private Place target;
	private String guardExpression;
	
	public String getGuardExpression() {
		return guardExpression;
	}

	public void setGuardExpression(String guardExpression) {
		this.guardExpression = guardExpression;
	}

	public OutArc() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Transition getSource() {
		return source;
	}

	public void setSource(Transition source) {
		this.source = source;
	}

	public Place getTarget() {
		return target;
	}

	public void setTarget(Place target) {
		this.target = target;
	}

	
}
