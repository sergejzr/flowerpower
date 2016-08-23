package test;

import java.util.Comparator;
import java.util.Map.Entry;

public class TagFreqComparator implements
		Comparator<Entry<String, Integer>> {

	@Override
	public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
		int ret=-1*o1.getValue().compareTo(o2.getValue());
		if(ret!=0) return ret;
		return o1.getKey().compareTo(o2.getKey());
	}

}


