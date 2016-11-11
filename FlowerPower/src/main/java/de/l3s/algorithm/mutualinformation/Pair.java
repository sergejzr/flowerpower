package de.l3s.algorithm.mutualinformation;




public class Pair {

	private ScoredItem el1;
	private ScoredItem el2;
	private double alpha=0.5;

	public Pair(ScoredItem el1, ScoredItem el2) {
		this.el1=el1;
		this.el2=el2;
	}

	public ScoredItem getLeft() {
		
		return el1;
	}
	public ScoredItem getRight() {
	
		return el2;
	}
	
public double score(){return alpha*el1.score()+(1-alpha)*el2.score();}
@Override
public String toString() {
	// TODO Auto-generated method stub
	return el1.getLabel()+"("+el1.score()+"+"+el2.score()+"="+score()+")";
}
}
