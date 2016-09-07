package test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import l3s.rdj.document.Diversity;
import l3s.rdj.document.Document;
import l3s.rdj.impl.AllPairsDJ;
import l3s.toolbox.JaccardSimilarityComparator;

public class jacccardtester {
	public static double jaccardSimilarity(String similar1, String similar2){
		HashSet<String> h1 = new HashSet<String>();
		HashSet<String> h2 = new HashSet<String>();
		
		for(String s: similar1.split("\\s+")){
		h1.add(s);		
		}
		System.out.println("h1 "+ h1);
		for(String s: similar2.split("\\s+")){
		h2.add(s);		
		}
		System.out.println("h2 "+ h2);
		
		int sizeh1 = h1.size();
		//Retains all elements in h3 that are contained in h2 ie intersection
		h1.retainAll(h2);
		//h1 now contains the intersection of h1 and h2
		System.out.println("Intersection "+ h1);
		
			
		h2.removeAll(h1);
		//h2 now contains unique elements
		System.out.println("Unique in h2 "+ h2);
		
		//Union 
		int union = sizeh1 + h2.size();
		int intersection = h1.size();
		
		return (double)intersection/union;
		
	}
	public static void main(String args[]){
		 ParallelTopicModel model=null;
		  int cnt=120;
			try {
				System.out.println("reading model from memory");
				
				model=ParallelTopicModel.read(new File("C:\\Users\\singh\\flickrmodel"+cnt+".dat"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("start");
			Alphabet dataAlphabet = model.alphabet;
			ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
			
			Vector<Document> collection = new Vector<Document>();   
		      
				for (int topic = 0; topic < cnt; topic++)
		        {
		            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
		            
		            
		            int rank = 0;
		          
		            Document d= new Document();
		            while (iterator.hasNext() && rank <20) {
		                IDSorter idCountPair = iterator.next();
		                d.add(dataAlphabet.lookupObject(idCountPair.getID()));
		               
		                rank++;
		            }
		           collection.add(d);
		        }
		        
		      
				double error = .005, confidentiality = .95;
					
				JaccardSimilarityComparator similarityComparator = new JaccardSimilarityComparator();
						
				Diversity dj1 = new AllPairsDJ(collection, error, confidentiality,similarityComparator);
				System.out.println(dj1.getRDJ());
	}

}
