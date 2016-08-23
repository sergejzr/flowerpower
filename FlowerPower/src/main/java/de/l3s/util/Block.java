package de.l3s.util;

import java.util.HashSet;

public class Block extends HashSet<Integer>{
	Integer minidx;
	Integer maxidx;
	
	public Integer getMaxidx() {
		return maxidx;
	}
	public Integer getMinidx() {
		return minidx;
	}
	

	public void addS(Integer t) {
		
		if(this.maxidx==null||t>this.maxidx)this.maxidx=t;
		if(this.minidx==null||t<this.minidx)this.minidx=t;
		add(t);
	}

}
