package test;

public class WeightedTerm {
String term;
Double score;
public WeightedTerm(String term, Double score) {
	super();
	this.term = term;
	this.score = score;
}
public String getTerm() {
	return term;
}
public Double getScore() {
	return score;
}
public void plusWeight(double d) {
	score+=d;
	
}
@Override
	public String toString() {
		// TODO Auto-generated method stub
		return term+"("+score+")";
	}
}
