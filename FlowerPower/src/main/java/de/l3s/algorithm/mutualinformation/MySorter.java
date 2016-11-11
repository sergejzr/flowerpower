package de.l3s.algorithm.mutualinformation;

import java.util.Comparator;
import java.util.HashMap;

public class MySorter implements Comparator<Integer> {

	private HashMap<Integer, String> intCategory;

	public MySorter(HashMap<Integer, String> intCategory) {
		this.intCategory=intCategory;
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		// TODO Auto-generated method stub
		return intCategory.get(o1).compareTo(intCategory.get(o2));
	}

}
