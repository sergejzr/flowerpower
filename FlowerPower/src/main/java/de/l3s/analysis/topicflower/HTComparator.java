package de.l3s.analysis.topicflower;

import java.util.Comparator;
import java.util.Hashtable;

public class HTComparator implements Comparator<String> {

	private Hashtable<String, Double> scores;
	private Hashtable<String, Integer> dist;

	public HTComparator(Hashtable<String, Double> scores, Hashtable<String, Integer> dist) {
		this.scores=scores;
		this.dist=dist;
	}

	@Override
	public int compare(String o1, String o2) {
		// TODO Auto-generated method stub
		return this.scores.get(o1).compareTo(this.scores.get(o2));
	}

	

}
