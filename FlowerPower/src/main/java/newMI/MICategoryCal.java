package newMI;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import de.l3s.util.alg.Pair;
import de.l3s.util.alg.PairRank;
import de.l3s.util.alg.ScoredItem;

public class MICategoryCal {
	class TermMI implements Comparable {
		public String term;
		public double value;

		public TermMI(String term, double value) {
			this.term = term;
			this.value = value;
		}

		public int compareTo(Object o) {
			TermMI tm = (TermMI) o;
			if (this.value < tm.value) {
				return 1;
			} else if (this.value > tm.value) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	
	private HashMap<String, String> labels;
	int[] top5= new int[5];
	private HashMap<Integer, ArrayList<DocMI>> CategoryDocumentsMap= new HashMap<Integer, ArrayList<DocMI>>();
	private HashMap<String, ArrayList<TermMI>> CategoryRepresentativesMap= new HashMap<String, ArrayList<TermMI>>();
	private HashMap<String, ArrayList<Double>> CategoryTopicProbabilityMap= new HashMap<String, ArrayList<Double>>();
	int numberOfDocsInCategory=0;
	public HashMap<String, Integer> idCatCount;
	public HashMap<Integer, double[]> categoryTopicProb= new HashMap<Integer, double[]>();
	public HashMap<String, Double> categoryTopicMIScore= new HashMap<String, Double>();
	
	public HashMap<Integer, Double> priorProb= new HashMap<Integer, Double>();
	
	private HashSet<String> topics= new HashSet<String>();
	ParallelTopicModel model=null;
	private HashMap<String, Integer> TopicFrequencyMap= new HashMap<String, Integer>();
	double[] topdistributions;
	int[] topall;
	public MICategoryCal(HashMap<Integer, ArrayList<DocMI>> docsMap, ParallelTopicModel model, InstanceList loadedinstances, HashMap<String, String> labels, HashMap<String, Integer> idCatCount) {
		this.CategoryDocumentsMap= docsMap;
		this.model=model;
		this.labels=labels;
		this.idCatCount=idCatCount;
		this.topall= new int[model.numTopics];
	}
	public MICategoryCal( HashMap<Integer, ArrayList<String>> CategoryDocumentsMap,ParallelTopicModel model,InstanceList loadedinstances,HashMap<String, String> labels) {
		
		//compute(loadedinstances,model.numTopics);
		
	}
	
	public void centreCalculation()
	 {

			System.out.println("New centre calculation");
			 double[] topicDistribution =topdistributions=model.getTopicProbabilities(0);
			 double[] topicDistributionCentre= new double[model.getNumTopics()];
			 for(int i=0; i<model.getNumTopics();i++)
				 topicDistributionCentre[i]=0;
	        for (int topic = 0; topic < model.getNumTopics(); topic++) {
	        	double tscore=topicDistribution[topic]/(double)CategoryDocumentsMap.keySet().size();
	        	double score=0;
	        	for(int c: categoryTopicProb.keySet())
	        	{
	        		double catscore = categoryTopicProb.get(c)[topic];
	        		double diff = catscore-tscore;
	        		if(diff<0)
	        			diff=diff*-1;
	        		score+=diff;
	        	}
	            topicDistributionCentre[topic]=score;
	        }
	        System.out.println(Arrays.toString((getHighestIndexes(topicDistributionCentre, 199))));
	        System.out.println(Arrays.toString(getLowestIndexes(topicDistributionCentre, 5)));
	        
	        
	 }
	
	public void centreCalculationDiversity()
	 {

			System.out.println("New centre calculation");
			System.out.println("topic,topic prob,simpson,pieolu");
			 double[] topicDistribution =topdistributions=model.getTopicProbabilities(0);
			 double[] topicDistributionCentre= new double[model.getNumTopics()];
			 
			 for(int i=0; i<model.getNumTopics();i++)
				 topicDistributionCentre[i]=0;
	        for (int topic = 0; topic < model.getNumTopics(); topic++) {
	        	double topicProbSum=0;
	        	HashMap<Integer, Double> map= new HashMap<Integer, Double>();
	        	for(int c: categoryTopicProb.keySet())
	        	{
	        		double[] topicDistForAllTopics = categoryTopicProb.get(c);
	        		map.put(c, topicDistForAllTopics[topic]);
	        		topicProbSum += topicDistForAllTopics[topic];
	        	}
	        	double diversityScore=0;
	        	double diversityScorePieolu=0;
	        	
	        	for(int category: map.keySet())
	        	{
	        		diversityScore+=(map.get(category)/topicProbSum)*(map.get(category)/topicProbSum);
	        		diversityScorePieolu+=(map.get(category)/topicProbSum)*Math.log((map.get(category)/topicProbSum));
	        	}
	        	System.out.println();
	        	System.out.print(labels.get(""+topic)+",");
	        	System.out.print(topicDistribution[topic]+",");
	        	System.out.print(diversityScore+",");
	        	System.out.print(-diversityScorePieolu);
	        //	generateCsvFile("C:\\Users\\singh\\Desktop\\Flower Results\\topicProbDistributionAcrossCategories\\topic"+topic+".csv", map, topicProbSum);
	        	//topicDistributionCentre[topic]=diversityScore*topicDistribution[topic];//Simpson Index
	        	topicDistributionCentre[topic]=-((diversityScorePieolu)/Math.log(model.getNumTopics()));// Pielou's evenness index	
	        	
	        }
	        //---top10 evenness index topics reranked by initial probablility-----------
	        int[] top10Evenness= getHighestIndexes(topicDistributionCentre, 10);
	        ArrayList<ScoredItem> items= new ArrayList<ScoredItem>();
	        for(int item: top10Evenness)
	        {
	        	ScoredItem si= new ScoredItem(""+item, topicDistribution[item]);
	        	items.add(si);
	        }
	        
	        Collections.sort(items, new Comparator<ScoredItem>() {

				@Override
				public int compare(ScoredItem o1, ScoredItem o2) {
					return -Double.valueOf(o1.score())
							.compareTo(Double.valueOf(o2.score()));
				}
			});
	        
	        int[] top5EveReRank= new int[5];
	        int j=0;
	        System.out.println("");
	        for (ScoredItem si: items.subList(0, 5))
	        {
	        	top5EveReRank[j]= Integer.parseInt(si.getLabel());
	        	j++;
	        }
	       
	        //----------------------Pair rank-----------------------
	        int[] top200Evenness = getHighestIndexes(topicDistributionCentre, topicDistributionCentre.length);
	        int[] top200Probability = getHighestIndexes(topicDistribution, topicDistribution.length);
	        ArrayList<String> list1= new ArrayList<String>();
	        ArrayList<String> list2= new ArrayList<String>();
	        
	        //alpha set in Pair Class. List1 is favoured.
	        
	        for( int i: top200Evenness)
	        	list1.add(""+i);
	        for( int i: top200Probability)
	        	list2.add(""+i);
	        
	       	PairRank pr= new PairRank(list1, list2, 200);
	       	int[] rankedCentre= new int[topicDistribution.length];
	       	System.out.println("Pair Rank");
	       	int i=topicDistribution.length-1;
	       	for(Pair p: pr.getPairs())
	       	{
	       		rankedCentre[i]=Integer.parseInt(p.getLeft().getLabel());
	       		System.out.println(labels.get(""+rankedCentre[i])+","+i);
	       		i--;
	       	}
	       	System.out.println("-----------------------------------------------");
	       	
	       	//----------------------------------------------------------------------------------------
	       	
	       	//top5=getHighestIndexes(topicDistributionCentre, 5); - Evenness
			//topall=getHighestIndexes(topicDistributionCentre, 200);
			
			
			//top5=Arrays.copyOf(rankedCentre, 5); //-pair rank
			//topall=rankedCentre;
			
			top5=top5EveReRank; //- rerenk
			topall=rankedCentre;
			
			
			System.out.println("Centre:");
			
			System.out.println(Arrays.toString(top5));
	        
	 }
	private static void generateCsvFile(String sFileName, HashMap<Integer, Double> map, Double topicProbSum)
	{
		try
		{
		    FileWriter writer = new FileWriter(sFileName);
		    
		    for(int category: map.keySet())
       	{
		    	writer.append("category"+category);
		    	writer.append(',');
			    writer.append(""+(map.get(category)/topicProbSum));
			    writer.append('\n');	
       	}
	 
		    writer.flush();
		    writer.close();
		}
		catch(IOException e)
		{
		     e.printStackTrace();
		} 
	 }
	
	
	
	
		
	
	
	/*private void compute( InstanceList loadedinstances, int numtopics) {
		
		 double[] distribution_sum_corp = new double[numtopics];
	        for(int i=0; i<numtopics;i++)
	        	distribution_sum_corp[i]=0;
	     double[] distribution_avg_corp = new double[numtopics];
	        for(int i=0; i<numtopics;i++)
	        	distribution_avg_corp[i]=0;
	     int count=0;
	     for(Integer cat: CategoryDocumentsMap.keySet())
	     {
	    	 	count+=CategoryDocumentsMap.get(cat).size();
	     }
		
	     for(Integer cat: CategoryDocumentsMap.keySet())
		{
				 
	     
	    	double[] distribution_sum = new double[numtopics];
	        for(int i=0; i<numtopics;i++)
	        	distribution_sum[i]=0;
	       
	        double[] distribution_avg_category = new double[numtopics];
	        for(int i=0; i<numtopics;i++)
	        	distribution_avg_category[i]=0;
	        
	        double[] distribution_avg_category2 = new double[numtopics];
	        for(int i=0; i<numtopics;i++)
	        	distribution_avg_category2[i]=0;
	       
	        int count_cat=CategoryDocumentsMap.get(cat).size();
			
				for(String doc:CategoryDocumentsMap.get(cat))//need to get the actual documents
				{
					
					loadedinstances.addThruPipe(new Instance(doc, null, "test instance", null));
					double[] distribution = model.getInferencer().getSampledDistribution(loadedinstances.get(0), 100, 2, 15);
					loadedinstances.remove(0);
					 for(int i=0; i<numtopics;i++)
				        	distribution_sum_corp[i]+=distribution[i];
					 
					 for(int i=0; i<numtopics;i++)
					        	distribution_sum[i]+=distribution[i];
						
					
					
				}
				 for(int i=0; i<numtopics;i++)
			        	distribution_avg_category[i]=distribution_sum[i]/(count);
				 for(int i=0; i<numtopics;i++)
			        	distribution_avg_category2[i]=distribution_sum[i]/(count_cat);
				
				
				categoryTopicProb.put(cat, distribution_avg_category);
				categoryTopicProb2.put(cat, distribution_avg_category);
		}
			
			 for(int i=0; i<numtopics;i++)
		        	distribution_avg_corp[i]=distribution_sum_corp[i]/(count);
			 
			 
		 for(Integer cat: CategoryDocumentsMap.keySet())
		 {
			 double prob_of_category=(double)count_cat/count;
			
			 ArrayList<TermMI> list= new ArrayList<MICategoryCal.TermMI>();
			 double alpha=0.35;
			 for(int i=0; i<numtopics;i++)
			 {
				 double score=distribution_avg_category[i]*(Math.log(distribution_avg_category[i]/(distribution_avg_corp[i]*prob_of_category)));
				 if(score>(2/CategoryDocumentsMap.keySet().size()) )
				 {
					System.out.println("found!!! "+score);
					 list.add(new TermMI(""+i, score));
				 }
				 
			 }
			 Collections.sort(list);
			 CategoryRepresentativesMap.put(category1+"-"+category2, list);
		 }
		
	}*/
	public void computePositiveAndNegativeMIvalues(Integer category,int numtopics, InstanceList loadedinstances) {
		//buildLists(category);
		computeValues(category, numtopics,loadedinstances);
	}
	public void computeValuesPairs(Integer category1,Integer category2, int numtopics, InstanceList loadedinstances) {
		//calculate p(z|c) -> output is a vector of topics and the average probability in the given category
		
        double[] distribution_sum = new double[numtopics];
        for(int i=0; i<numtopics;i++)
        	distribution_sum[i]=0;
        double[] distribution_sum_corp = new double[numtopics];
        for(int i=0; i<numtopics;i++)
        	distribution_sum_corp[i]=0;
        double[] distribution_avg_category = new double[numtopics];
        for(int i=0; i<numtopics;i++)
        	distribution_avg_category[i]=0;
        double[] distribution_avg_corp = new double[numtopics];
        for(int i=0; i<numtopics;i++)
        	distribution_avg_corp[i]=0;
        int count=0;
        int count_cat=0;
		for(Integer cat: CategoryDocumentsMap.keySet())
		{
			for(DocMI doc:CategoryDocumentsMap.get(cat))//need to get the actual documents
			{
				
				loadedinstances.addThruPipe(new Instance(doc.text, null, "test instance", null));
				double[] distribution = model.getInferencer().getSampledDistribution(loadedinstances.get(0), 100, 2, 15);
				loadedinstances.remove(0);
				 for(int i=0; i<numtopics;i++)
			        	distribution_sum_corp[i]+=distribution[i];
				 
				if(cat==category1 || cat==category2)
				{
					 for(int i=0; i<numtopics;i++)
				        	distribution_sum[i]+=distribution[i]*(double)(1/idCatCount.get(doc.id));
					
					 count_cat++;	
				}
				count++;
				
			}
		}
		 for(int i=0; i<numtopics;i++)
	        	distribution_avg_category[i]=distribution_sum[i]/(count);
		
		
		 for(int i=0; i<numtopics;i++)
	        	distribution_avg_corp[i]=distribution_sum_corp[i]/(count);
		 //p(c)
		 double prob_of_category=(double)count_cat/count;
		 //p(z_i) -> directly from mallet
		
		 ArrayList<TermMI> list= new ArrayList<MICategoryCal.TermMI>();
		 ArrayList<TermMI> listnew= new ArrayList<MICategoryCal.TermMI>();
		 double alpha=0.35;
		 for(int i=0; i<numtopics;i++)
		 {
			 double score=distribution_avg_category[i]*(Math.log(distribution_avg_category[i]/(distribution_avg_corp[i]*prob_of_category)));
			 
			 if(score>0 && categoryTopicMIScore.get(category1+"-"+i)>0 && categoryTopicMIScore.get(category2+"-"+i)>0)
			 {
				listnew.add(new TermMI(""+i, score));
				
			 }
			
			 list.add(new TermMI(""+i, score));
		 }
		 System.out.println("old MI score method result");
		 
		 Collections.sort(list);
		 Collections.sort(listnew);
		 for(TermMI m:list)
			 System.out.print(labels.get(m.term)+",");	
		 System.out.println();
		 CategoryRepresentativesMap.put(category1+"-"+category2, listnew);
		 //top5=getHighestIndexes(distribution_avg_corp, 5);
		 topdistributions=distribution_avg_corp;
		
		
	}
	
	@SuppressWarnings("unchecked")
	private void computeValues(Integer category, int numtopics, InstanceList loadedinstances) {
		//calculate p(z|c) -> output is a vector of topics and the average probability in the given category
		
        double[] distribution_sum = new double[numtopics];
        for(int i=0; i<numtopics;i++)
        	distribution_sum[i]=0;
        double[] distribution_sum_corp = new double[numtopics];
        for(int i=0; i<numtopics;i++)
        	distribution_sum_corp[i]=0;
        double[] distribution_avg_category = new double[numtopics];
        for(int i=0; i<numtopics;i++)
        	distribution_avg_category[i]=0;
        double[] distribution_avg_corp = new double[numtopics];
        for(int i=0; i<numtopics;i++)
        	distribution_avg_corp[i]=0;
        double[] distribution_avg_category2 = new double[numtopics];
        for(int i=0; i<numtopics;i++)
        	distribution_avg_category2[i]=0;
        int count=0;
        int count_cat=0;
     	for(Integer cat: CategoryDocumentsMap.keySet())
		{
			for(DocMI doc:CategoryDocumentsMap.get(cat))//need to get the actual documents
			{
				
				loadedinstances.addThruPipe(new Instance(doc.text, null, "test instance", null));
				double[] distribution = model.getInferencer().getSampledDistribution(loadedinstances.get(0), 100, 2, 15);
				loadedinstances.remove(0);

				
				 for(int i=0; i<numtopics;i++)
			        	distribution_sum_corp[i]+=distribution[i];
				
				count++;
			}
		}
		System.out.println("number of docs in category: "+CategoryDocumentsMap.get(category).size());
		for(DocMI doc:CategoryDocumentsMap.get(category))//need to get the actual documents
		{
		
			loadedinstances.addThruPipe(new Instance(doc.text, null, "test instance", null));
			double[] distribution = model.getInferencer().getSampledDistribution(loadedinstances.get(0), 100, 2, 15);
			loadedinstances.remove(0);
				 for(int i=0; i<numtopics;i++)
			        	distribution_sum[i]+=distribution[i]*(double)(1./idCatCount.get(doc.id));
				 
				 count_cat++;
				
			
			
		}
		
		 for(int i=0; i<numtopics;i++)
		 {
			 distribution_avg_category[i]=distribution_sum[i]/(count);
			 distribution_avg_category2[i]=distribution_sum[i]/(count_cat);
		 }
	     categoryTopicProb.put(category, distribution_avg_category2);  	
		 int[] top = getHighestIndexes(distribution_avg_category, 10);
		 System.out.println("Top for category:"+category);
		 System.out.println(Arrays.toString(top));
		 
		 for(int i=0; i<numtopics;i++)
	        	distribution_avg_corp[i]=distribution_sum_corp[i]/(1.*count);
		 //p(c)
		 double prob_of_category=(double)1.*count_cat/count;
		 //p(z_i) -> directly from mallet
	
		 ArrayList<TermMI> list= new ArrayList<MICategoryCal.TermMI>();
		 ArrayList<TermMI> list1= new ArrayList<MICategoryCal.TermMI>();
		 for(int i=0; i<numtopics;i++)
		 {
			 double score=distribution_avg_category[i]*(Math.log(distribution_avg_category[i]/(distribution_avg_corp[i]*prob_of_category)));
			 categoryTopicMIScore.put(category+"-"+i, score);
			
			 if(score>0)
				 list.add(new TermMI(""+i, score));
			 else
				 list1.add(new TermMI(""+i, score));
		 }
		 Collections.sort(list);
		 Collections.sort(list1);
		 System.out.println("neg list");
		 for(TermMI m:list1)
			 System.out.print(labels.get(m.term)+",");
		 System.out.println();
		 System.out.println("size of pos list: "+list.size());
		 CategoryRepresentativesMap.put(""+category, list);
		 top5=getHighestIndexes(distribution_avg_corp, 5);
		 
		 topall=getHighestIndexes(distribution_avg_corp, distribution_avg_corp.length);
		
		
	}
	
	
	private static int[] sequence(int n) {
        int[] indexes = new int[n];
        for(int i = 0;i < n;i++) {
            indexes[i] = i;
        }
        return indexes;
    }
	public static int[] getHighestIndexes(double[] data, int topN) {
	     if (data.length < topN) {
	         return sequence(topN);
	     }
	     int[] bestIndex = new int[topN];
	     double[] bestVals = new double[topN];

	     bestIndex[0] = 0;
	     bestVals[0] = data[0];

	     for(int i = 1;i < topN;i++) {
	         int j = i;
	         while( (j > 0) && (bestVals[j - 1] < data[i]) ) {
	             bestIndex[j] = bestIndex[j - 1];
	             bestVals[j] = bestVals[j - 1];
	             j--;
	         }
	         bestVals[j] = data[i];
	         bestIndex[j] = i;
	     }

	     for(int i = topN;i < data.length;i++) {
	         if (bestVals[topN - 1] < data[i]) {
	             int j = topN - 1;
	             while( (j > 0) && (bestVals[j - 1] < data[i]) ) {
	                 bestIndex[j] = bestIndex[j - 1];
	                 bestVals[j] = bestVals[j - 1];
	                 j--;
	             }
	             bestVals[j] = data[i];
	             bestIndex[j] = i;
	         }
	     }

	     return bestIndex;
	 }
	public static int[] getLowestIndexes(double[] data, int topN) {
	     if (data.length <= topN) {
	         return sequence(topN);
	     }
	     int[] bestIndex = new int[topN];
	     double[] bestVals = new double[topN];

	     bestIndex[0] = 0;
	     bestVals[0] = data[0];

	     for(int i = 1;i < topN;i++) {
	         int j = i;
	         while( (j > 0) && (bestVals[j - 1] > data[i]) ) {
	             bestIndex[j] = bestIndex[j - 1];
	             bestVals[j] = bestVals[j - 1];
	             j--;
	         }
	         bestVals[j] = data[i];
	         bestIndex[j] = i;
	     }

	     for(int i = topN;i < data.length;i++) {
	         if (bestVals[topN - 1] > data[i]) {
	             int j = topN - 1;
	             while( (j > 0) && (bestVals[j - 1] > data[i]) ) {
	                 bestIndex[j] = bestIndex[j - 1];
	                 bestVals[j] = bestVals[j - 1];
	                 j--;
	             }
	             bestVals[j] = data[i];
	             bestIndex[j] = i;
	         }
	     }

	     return bestIndex;
	 }
	public HashMap<Integer, ArrayList<DocMI>> getCategoryDocumentsMap() {
		return CategoryDocumentsMap;
	}
	public void setCategoryDocumentsMap(
			HashMap<Integer, ArrayList<DocMI>> categoryDocumentsMap) {
		CategoryDocumentsMap = categoryDocumentsMap;
	}
	public HashMap<String, ArrayList<TermMI>> getCategoryRepresentativesMap() {
		return CategoryRepresentativesMap;
	}
	public void setCategoryRepresentativesMap(
			HashMap<String, ArrayList<TermMI>> categoryRepresentativesMap) {
		CategoryRepresentativesMap = categoryRepresentativesMap;
	}
	public HashSet<String> getTopics() {
		return topics;
	}
	public void setTopics(HashSet<String> topics) {
		this.topics = topics;
	}
	public HashMap<String, Integer> getTopicFrequencyMap() {
		return TopicFrequencyMap;
	}
	public void setTopicFrequencyMap(HashMap<String, Integer> topicFrequencyMap) {
		TopicFrequencyMap = topicFrequencyMap;
	}
	public HashMap<String, ArrayList<Double>> getCategoryTopicProbabilityMap() {
		return CategoryTopicProbabilityMap;
	}
	public void setCategoryTopicProbabilityMap(
			HashMap<String, ArrayList<Double>> categoryTopicProbabilityMap) {
		CategoryTopicProbabilityMap = categoryTopicProbabilityMap;
	}
}
