package de.l3s.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

public class PageSampler {
	private Integer total;
	private Integer per_page;
	private Hashtable<Integer, Integer> pagestats = null;


	public PageSampler(Integer total, Integer per_page) {
		this.total = total;
		this.per_page = per_page;

	}

	/**
	 * pagestats: Hashtable&lt;pagenum,pagesize&gt;
	 * 
	 * @param pagestats
	 */
	public PageSampler(Hashtable<Integer, Integer> pagestats) {

		this.pagestats = pagestats;
		total = 0;
		for (Integer key : pagestats.keySet()) {
			total += pagestats.get(key);
		}

	}

	private Hashtable<Integer, HashSet<Integer>> samplenotequal(
			Integer sample_size) {
		Hashtable<Integer, HashSet<Integer>> ret = new Hashtable<Integer, HashSet<Integer>>();

		
		
		Vector<Integer> bag = new Vector<Integer>();
		for (int i = 0; i < total; i++) {
			bag.add(i);
		}
		Collections.shuffle(bag);

		for (int i = 0; i < sample_size && i < bag.size(); i++) {
			Integer idx = bag.elementAt(i);
			int curoffset = 0;
			for (Integer key : pagestats.keySet()) {
				curoffset+=pagestats.get(key);
				if(curoffset>idx)
				{
					HashSet<Integer> curset = ret.get(key);
					if(curset==null){ret.put(key,curset=new HashSet<Integer>());}
					curset.add(curoffset-idx);
					break;
				}
				
				
			}

		}
		return ret;
	}

	private Hashtable<Integer, HashSet<Integer>> samplenotequallarge(
			Integer sample_size) {
		Hashtable<Integer, HashSet<Integer>> ret = new Hashtable<Integer, HashSet<Integer>>();

		
		HashSet<String> usedids=new HashSet<String>();
		
		Vector<Integer> bag = new Vector<Integer>();
		for (int i = 0; i < total; i++) {
			bag.add(i);
		}
		Collections.shuffle(bag);

		for (int i = 0; i < sample_size && i < bag.size(); i++) {
			Integer idx = bag.elementAt(i);
			int curoffset = 0;
			for (Integer key : pagestats.keySet()) {
				curoffset+=pagestats.get(key);
				if(curoffset>idx)
				{
					HashSet<Integer> curset = ret.get(key);
					if(curset==null){ret.put(key,curset=new HashSet<Integer>());}
					curset.add(curoffset-idx);
					break;
				}
				
				
			}

		}
		return ret;
	}
	public Hashtable<Integer, Vector<Block>> blocks( Hashtable<Integer, HashSet<Integer>> sample,Integer blocksize)
	{
		Hashtable<Integer, Vector<Block>> ret=new Hashtable<Integer, Vector<Block>>();
		for(Integer key:sample.keySet()){
			HashSet<Integer> curset = sample.get(key);
			ArrayList<Integer> sorted=new ArrayList<Integer>(curset);
			Collections.sort(sorted);
			Vector<Block> curblocks;
			ret.put(key, curblocks=new Vector<Block>());
			
			int curidx=0;
		
			Block b=null;
			
		//	curblocks.add(b);
			
		//	System.out.println(sorted);
			for(Integer t:sorted)
			{

			
				
				if(b==null||t-curidx>=blocksize)
				{
					curblocks.add(b=new Block());
					
					curidx=t;
				}
				b.addS(t);
				
				
				
			}
			
			
		
		}
		return ret;	
	}
	public Hashtable<Integer, HashSet<Integer>> sample(Integer sample_size) {

		if(pagestats!=null) return samplenotequal(sample_size);
		Hashtable<Integer, HashSet<Integer>> ret = new Hashtable<Integer, HashSet<Integer>>();
		Vector<Integer> bag = new Vector<Integer>();
		for (int i = 0; i < total; i++) {
			bag.add(i);
		}

		Collections.shuffle(bag);

		for (int i = 0; i < sample_size && i < bag.size(); i++) {
			Integer idx = bag.elementAt(i);

			int pagenr = idx / per_page + 1;
			int idxonpage = idx % per_page;

			HashSet<Integer> indexes = ret.get(pagenr);

			if (indexes == null) {
				ret.put(pagenr, indexes = new HashSet<Integer>());
			}

			indexes.add(idxonpage);
		}

		return ret;
	}

	public static void main(String[] args) {
		//PageSampler ps = new PageSampler(50000, 500);
		HashSet<Integer> st = new HashSet<Integer>();

		System.out.println(st);
		Hashtable<Integer, Integer> pages=new Hashtable<Integer, Integer>();
		pages.put(0, 100);
		pages.put(1, 70);
		pages.put(2, 99);
		
		Hashtable<Integer, Vector<Integer>> averages=new Hashtable<Integer, Vector<Integer>>();
		PageSampler ps = new PageSampler(pages);
		
		Hashtable<Integer, HashSet<Integer>> sample = ps.sample(20);
		
		System.out.println(sample);
		Hashtable<Integer, Vector<Block>> bocks = ps.blocks(sample, 10);
		
		System.out.println(bocks);
		/*
		for(int i=0;i<1000;i++){
		Hashtable<Integer, HashSet<Integer>> sample = ps.sample(10);
		System.out.println(sample);
		
		
for(int key:sample.keySet())
{
	Vector<Integer> conti = averages.get(key);
	if(conti==null) {averages.put(key, conti=new Vector<Integer>());}
	conti.add(sample.get(key).size());
}
		}
		
		for(Integer key:averages.keySet())
		{
			Vector<Integer> conti = averages.get(key);
			Integer sum=0;
			for(Integer s:conti){sum+=s;}
			System.out.print(key+"={"+(sum/conti.size())+"}, ");
		}
		
		*/
	}

}
