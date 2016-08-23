package newMI;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class MICategoryCalNew {
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
	int[] top5= new int[5];
	private HashMap<Integer, ArrayList<String>> CategoryDocumentsMap= new HashMap<Integer, ArrayList<String>>();
	private HashMap<String, ArrayList<TermMI>> CategoryRepresentativesMap= new HashMap<String, ArrayList<TermMI>>();
	int numberOfDocs=0;
	private HashSet<String> topics= new HashSet<String>();
	ParallelTopicModel model=null;
	
	 double[] distribution_avg_corp = new double[200];
	
	
	private HashMap<String, Integer> TopicFrequencyMap= new HashMap<String, Integer>();
	public MICategoryCalNew() {
		// TODO Auto-generated constructor stub
	}
	public MICategoryCalNew( HashMap<Integer, ArrayList<String>> CategoryDocumentsMap,ParallelTopicModel model) {
		this.CategoryDocumentsMap= CategoryDocumentsMap;
		if(model==null)
		{
			try {
				System.out.println("reading model from memory");
				//model = ParallelTopicModel.read(new File("C:\\Users\\singh\\FacebookIndex\\"+"topicmodel1.txt"));
				model=ParallelTopicModel.read(new File("C:\\Users\\singh\\flickrmodel"+200+".dat"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			this.model=model;
		}
		for(int i=0; i<200;i++)
        	distribution_avg_corp[i]=0;
		
		computeCorpusScores();
	}
	private void computeCorpusScores()
	{
		 int count=0;
		 InstanceList instances =null;
			ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
			pipeList.add( new CharSequenceLowercase() );
	        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
	        pipeList.add( new TokenSequenceRemoveStopwords(new File("C:\\Users\\singh\\Downloads\\mallet-2.0.7\\mallet-2.0.7\\stoplists\\en.txt"), "UTF-8", false, false, false) );
	        pipeList.add( new TokenSequence2FeatureSequence() );
	        instances = new InstanceList (new SerialPipes(pipeList));
		 double[] distribution_sum_corp = new double[200];
	        for(int i=0; i<200;i++)
	        	distribution_sum_corp[i]=0;
			for(Integer cat: CategoryDocumentsMap.keySet())
			{
				for(String doc:CategoryDocumentsMap.get(cat))//need to get the actual documents
				{
					InstanceList taginstance = new InstanceList(instances.getPipe());
					taginstance.addThruPipe(new Instance(doc, null, "test instance", null));
					double[] distribution = model.getInferencer().getSampledDistribution(taginstance.get(0), 0, 2, 15);
					
					 for(int i=0; i<200;i++)
				        	distribution_sum_corp[i]+=distribution[i];
					 
					
						count++;
						
					
					
					
				}
			}
			 
			 for(int i=0; i<200;i++)
		        	distribution_avg_corp[i]=distribution_sum_corp[i]/(count);
			 
			 numberOfDocs=count;
	}
	public void computePositiveAndNegativeMIvalues(Integer category) {
		//buildLists(category);
		computeValues(category);
	}
	public void computeValuesPairs(Integer category1,Integer category2) {
		//calculate p(z|c) -> output is a vector of topics and the average probability in the given category
		InstanceList instances =null;
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File("C:\\Users\\singh\\Downloads\\mallet-2.0.7\\mallet-2.0.7\\stoplists\\en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );
        instances = new InstanceList (new SerialPipes(pipeList));
        double[] distribution_sum = new double[200];
        for(int i=0; i<200;i++)
        	distribution_sum[i]=0;
        double[] distribution_sum_corp = new double[200];
        for(int i=0; i<200;i++)
        	distribution_sum_corp[i]=0;
        double[] distribution_avg_category = new double[200];
        for(int i=0; i<200;i++)
        	distribution_avg_category[i]=0;
        double[] distribution_avg_corp = new double[200];
        for(int i=0; i<200;i++)
        	distribution_avg_corp[i]=0;
        int count=0;
		
		for(Integer cat: CategoryDocumentsMap.keySet())
		{
			for(String doc:CategoryDocumentsMap.get(cat))//need to get the actual documents
			{
				InstanceList taginstance = new InstanceList(instances.getPipe());
				taginstance.addThruPipe(new Instance(doc, null, "test instance", null));
				double[] distribution = model.getInferencer().getSampledDistribution(taginstance.get(0), 0, 2, 15);
				
				 for(int i=0; i<200;i++)
			        	distribution_sum_corp[i]+=distribution[i];
				 
				if(cat==category1 || cat==category2)
				{
					 for(int i=0; i<200;i++)
				        	distribution_sum[i]+=distribution[i];
					
					count++;
					
					
				}
				
				
			}
		}
		 for(int i=0; i<200;i++)
	        	distribution_avg_category[i]=distribution_sum[i]/(count*4);
		 for(int i=0; i<200;i++)
	        	distribution_avg_corp[i]=distribution_sum_corp[i]/(count*4);
		 //p(c)
		 double prob_of_category=(double)1/CategoryDocumentsMap.keySet().size();
		 //p(z_i) -> directly from mallet
		 double[] topicDistribution = model.getTopicProbabilities(200);
		 //MI(z_i,c)
		 ArrayList<TermMI> list= new ArrayList<MICategoryCalNew.TermMI>();
		 double alpha=0.35;
		 for(int i=0; i<200;i++)
		 {
			 double score=distribution_avg_category[i]*(Math.log(distribution_avg_category[i]/(distribution_avg_corp[i]*prob_of_category)));
			 list.add(new TermMI(""+i, score));
		 }
		 Collections.sort(list);
		 CategoryRepresentativesMap.put(category1+"-"+category2, list);
		 //top5=getHighestIndexes(distribution_avg_corp, 5);
		
		
	}
	
	private void computeValues(Integer category) {
		//calculate p(z|c) -> output is a vector of topics and the average probability in the given category
		InstanceList instances =null;
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File("C:\\Users\\singh\\Downloads\\mallet-2.0.7\\mallet-2.0.7\\stoplists\\en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );
        instances = new InstanceList (new SerialPipes(pipeList));
        double[] distribution_sum = new double[200];
        for(int i=0; i<200;i++)
        	distribution_sum[i]=0;
       
        double[] distribution_avg_category = new double[200];
        for(int i=0; i<200;i++)
        	distribution_avg_category[i]=0;
        
        int count=0;
		
		
			for(String doc:CategoryDocumentsMap.get(category))//need to get the actual documents
			{
				InstanceList taginstance = new InstanceList(instances.getPipe());
				taginstance.addThruPipe(new Instance(doc, null, "test instance", null));
				double[] distribution = model.getInferencer().getSampledDistribution(taginstance.get(0), 0, 2, 15);
				
				 for(int i=0; i<200;i++)
				        	distribution_sum[i]+=distribution[i];
					
				count++;
					
					
			}
				
			
			
		
		 for(int i=0; i<200;i++)
	        	distribution_avg_category[i]=distribution_sum[i]/(numberOfDocs);
		 
		 //p(c)
		 double prob_of_category=(double)1/CategoryDocumentsMap.keySet().size();
		 
		 //MI(z_i,c)
		 ArrayList<TermMI> list= new ArrayList<MICategoryCalNew.TermMI>();
		 double alpha=0.35;
		 for(int i=0; i<200;i++)
		 {
			 double score=distribution_avg_category[i]*(Math.log(distribution_avg_category[i]/(distribution_avg_corp[i]*prob_of_category)));
			 list.add(new TermMI(""+i, score));
		 }
		 Collections.sort(list);
		 CategoryRepresentativesMap.put(""+category, list);
		 top5=getHighestIndexes(distribution_avg_corp, 5);
		
		
	}
	private void buildLists(Integer category) {
		
		ArrayList<String> doclist = CategoryDocumentsMap.get(category);
		numberOfDocs=doclist.size();
		for(String doc:doclist)
		{
			String[] words = doc.split(" ");
			for(String word: words)
			{
				if(TopicFrequencyMap.containsKey(word))
				{
					Integer count = TopicFrequencyMap.get(word);
					count++;
					TopicFrequencyMap.put(word, count);
					topics.add(word);
					
				}
				else
				{
					TopicFrequencyMap.put(word, 1);
					topics.add(word);
				}
				
			}
			
		}
		
	}
	private static int[] sequence(int n) {
        int[] indexes = new int[n];
        for(int i = 0;i < n;i++) {
            indexes[i] = i;
        }
        return indexes;
    }
	public static int[] getHighestIndexes(double[] data, int topN) {
	     if (data.length <= topN) {
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
	public HashMap<Integer, ArrayList<String>> getCategoryDocumentsMap() {
		return CategoryDocumentsMap;
	}
	public void setCategoryDocumentsMap(
			HashMap<Integer, ArrayList<String>> categoryDocumentsMap) {
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
}
