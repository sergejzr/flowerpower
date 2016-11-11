package de.l3s.algorithm.mutualinformation;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.l3s.flower.jaxb.TopicLink;

public class DiagramInput {
	
	ArrayList<String> top5= new ArrayList<String>();
	HashMap<String, ArrayList<TopicLink>> categoryRepMap= new HashMap<String, ArrayList<TopicLink>>();
	HashMap<String, ArrayList<TopicLink>> combiningTopics= new HashMap<String, ArrayList<TopicLink>>();
	
	
	
	List<Integer> optimalordering= new ArrayList<Integer>();
	private ArrayList<Integer> top5links;
	
	public ArrayList<String> getTop5() {
		return top5;
	}

	public void setTop5(ArrayList<String> top5) {
		this.top5 = top5;
	}

	public HashMap<String, ArrayList<TopicLink>> getCategoryRepMap() {
		return categoryRepMap;
	}

	public void setCategoryRepMap(HashMap<String, ArrayList<TopicLink>> categoryRepMap) {
		this.categoryRepMap = categoryRepMap;
	}

	public HashMap<String, ArrayList<TopicLink>> getCombiningTopics() {
		return combiningTopics;
	}

	public void setCombiningTopics(
			HashMap<String, ArrayList<TopicLink>> combiningTopics) {
		this.combiningTopics = combiningTopics;
	}

	public List<Integer> getOptimalordering() {
		return optimalordering;
	}

	public void setOptimalordering(List<Integer> optimalordering) {
		this.optimalordering = optimalordering;
	}

	public DiagramInput() {
		// TODO Auto-generated constructor stub
	}
	
	public DiagramInput(ArrayList<String> top5,ArrayList<Integer> top5link, HashMap<String, ArrayList<TopicLink>> categoryRepMap,HashMap<String, ArrayList<TopicLink>> combiningTopics,List<Integer> optimalordering, int topN) {
		this.categoryRepMap=categoryRepMap;
		this.combiningTopics=combiningTopics;
		this.top5=top5;
		this.optimalordering=optimalordering;
		this.top5links=top5link;
		compute(topN);
		
	}

	private void compute(int n) {
		System.out.println("Computing intersections and definig structure");
		System.out.println("remove top 5");
		for(String key:combiningTopics.keySet())
		{
			ArrayList<TopicLink> list = combiningTopics.get(key);
			//list.removeAll(top5);
			//list=removeFromIdList(list,top5links);
			try
			{
				ArrayList<TopicLink> t = new ArrayList<TopicLink>(list.subList(0, n));
				combiningTopics.put(key, t);
			}
			catch (IndexOutOfBoundsException e) {
				System.out.println("index out of bounds, whole list considered");
			}
			
			
		}
		for(String key:categoryRepMap.keySet())
		{
			ArrayList<TopicLink> list = categoryRepMap.get(key);
			
		//	list=removeFromIdList(list,top5links);
			//list.removeAll(top5);
			ArrayList<TopicLink> t = new ArrayList<TopicLink>(list);
			categoryRepMap.put(key,t );
			
		}
		System.out.println("reomve top 5 connecting terms from representatives");
		for(String key:combiningTopics.keySet())
		{
			ArrayList<TopicLink> list = combiningTopics.get(key);
			String[] categoriesinpair=key.split("-");
			ArrayList<TopicLink> t1 = categoryRepMap.get(categoriesinpair[0]);
			ArrayList<TopicLink> t2 = categoryRepMap.get(categoriesinpair[1]);
		
			//t1=removeFromList(t1,list,2);
			//t1.removeAll(list);
			//t2=removeFromList(t2,list,2);
			
			//t2.removeAll(list);
			
			/*for(String s:list)
			{
				if((t1.subList(0, 15).contains(s) && t2.subList(0, 15).contains(s)) || (t1.subList(16, 30).contains(s) && t2.subList(16, 30).contains(s)) )
				{
					t1.remove(s);
					t2.remove(s);
					newcon.add(s);
				}
				
				if(newcon.size()>3)
					break;
			}*/
			//combiningTopics.put(key, newcon);
			categoryRepMap.put(categoriesinpair[0], t1);
			categoryRepMap.put(categoriesinpair[1],t2);
			
			
		}
		System.out.println("done");
		
	}

	private ArrayList<TopicLink> removeFromList(ArrayList<TopicLink> t1,
			ArrayList<TopicLink> list, int top) {
		HashSet<Integer> idx=new HashSet<Integer>();
		int cnt=0;
		for(TopicLink tl:list)
		{
			if(cnt++<top)
			idx.add(tl.getTid());
		}
		ArrayList<TopicLink> ret=new ArrayList<TopicLink>();
		for(TopicLink tl:list)
		{
			if(!idx.contains(tl.getTid()))
			ret.add(tl);
		}
		return ret;
	}

	private ArrayList<TopicLink> removeFromIdList(ArrayList<TopicLink> list,
			ArrayList<Integer> top) {
		
		HashSet<Integer> idx=new HashSet<Integer>();
		for(Integer tl:top)
		{
			idx.add(tl);
		}
		
		ArrayList<TopicLink> ret=new ArrayList<TopicLink>();
		for(TopicLink tl:list)
		{
			if(!idx.contains(tl.getTid()))
			ret.add(tl);
		}
		return ret;
	}





	



}
