package edu.cmu.lti.qalab.dependency;


public class Dependency_ {
	Token_ governor,dependent;
	String Relation;

	public Token_ getGovernor() {
		return governor;
	}

	public void setGovernor(Token_ governor) {
		this.governor = governor;
	}

	public Token_ getDependent() {
		return dependent;
	}

	public void setDependent(Token_ dependent) {
		this.dependent = dependent;
	}

	public String getRelation() {
		return Relation;
	}

	public void setRelation(String relation) {
		Relation = relation;
	}

}
