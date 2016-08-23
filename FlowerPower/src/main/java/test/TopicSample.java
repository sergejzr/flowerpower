package test;

import java.util.HashSet;
import java.util.Hashtable;

public class TopicSample {
	Hashtable<Integer, HashSet<Integer>> besttopics;
	Hashtable<Integer, HashSet<Integer>> randomtopics;

	public TopicSample(Hashtable<Integer, HashSet<Integer>> besttopics,
			Hashtable<Integer, HashSet<Integer>> randomtopics) {
		this.besttopics=besttopics;
		this.randomtopics=randomtopics;
	}

	public Hashtable<Integer, HashSet<Integer>> getBest() {
		return besttopics;
	}

	public Hashtable<Integer, HashSet<Integer>> getRandom() {
		return randomtopics;
	}
}
