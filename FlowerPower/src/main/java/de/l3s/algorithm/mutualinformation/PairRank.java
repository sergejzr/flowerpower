package de.l3s.algorithm.mutualinformation;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public class PairRank {
	List<Pair> pairs;
	private int headsize;
	private int firstpos;
	public PairRank(List<String> l1,List<String> l2, int k) 
	{
		List<ScoredItem> sl1=new ArrayList<ScoredItem>();
		List<ScoredItem> sl2=new ArrayList<ScoredItem>();
		for(int i=0;i<l1.size();i++)
		{
			sl1.add(new ScoredItem(l1.get(i),l1.size()-i));
			sl2.add(new ScoredItem(l2.get(i),l1.size()-i));
		}
		rankLists(sl1,sl2,k);
	}
	public int getFirstpos() {
		return firstpos;
	}
	public PairRank() {
		// TODO Auto-generated constructor stub
	}
	public void rankLists(List<ScoredItem> l1,List<ScoredItem> l2, int k) {
		
		Hashtable<String, ScoredItem> h1=new Hashtable<String, ScoredItem>();
		Hashtable<String, ScoredItem> h2=new Hashtable<String, ScoredItem>();
		
		pairs=new ArrayList<Pair>();
		firstpos=-1;
		for(int i=0;i<l1.size();i++)
		{
			ScoredItem el1 = l1.get(i);
			ScoredItem el2 = l2.get(i);
			if(h2.containsKey(el1.getLabel()))
			{
				//System.out.println("pair found "+el1.getLabel());
				if(firstpos==-1)firstpos=i;
				pairs.add(new Pair(el1,h2.get(el1.getLabel())));
			}
			h1.put(el1.getLabel(), el1);
			if(h1.containsKey(el2.getLabel())){
					
					//System.out.println("pair found "+el2.getLabel());
				if(firstpos==-1)firstpos=i;
					pairs.add(new Pair(h1.get(el2.getLabel()),el2));
			}
			h2.put(el2.getLabel(), el2);
			if(pairs.size()>=k)
			{
				 headsize = i+1;
				Collections.sort(pairs,new Comparator<Pair>() {

					@Override
					public int compare(Pair o1, Pair o2) {
						
						return Double.valueOf(o1.score())
								.compareTo(Double.valueOf(o2.score()));
								
								
					}

				});
				return;
			}
			
			
			
			
			
		}
	}
	
	
	public void rankLists(int[] l1,int[] l2, int k) {
		
		
	}
	
	
	public int getHeadsize() {
		return headsize;
	}
public static void main(String[] args) {
	
	List<String> l1=Arrays.asList("1 , 2 , 3 , 4 , 5".split("\\s*,\\s*")); 
	List<String> l2=Arrays.asList("1 , 3 , 5 , 2 , 4".split("\\s*,\\s*")); 
	
	PairRank pr=new PairRank(l1, l2,4);
	
	System.out.println("first pair position "+pr.getFirstpos());
	System.out.println("last pair position "+pr.getHeadsize());
	for(Pair p:pr.getPairs())
	{
		System.out.println(p);	
	}
	
}
public List<Pair> getPairs() {
return pairs;
	
}

}
