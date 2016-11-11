package de.l3s.topicmodelling;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;

public class MalletModel {
	
	ParallelTopicModel model = null;
	InstanceList instances =createInstanceList();
	public MalletModel(int numTopics,int numIterations) {
		 model = new ParallelTopicModel(numTopics, 1.0, 0.01);
	        // Use two parallel samplers, which each look at one half the corpus and combine
	        //  statistics after every iteration.
	        model.setNumThreads(1);

	        // Run the model for 50 iterations and stop (this is for testing only, 
	        //  for real applications, use 1000 to 2000 iterations)
	        model.setNumIterations(numIterations);
	        
		
	}
	public MalletModel(File f) throws Exception {
		model=ParallelTopicModel.read(f);
	}
	public void write(File f)
	{
		model.write(f);
	}
	public static void main(String[] args) {}
	
	
	private InstanceList createInstanceList()
	{
		 // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
      //  pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        return instances;
	}

	public void estimate() throws IOException {
		 model.addInstances(instances);
		model.estimate();
		
	}
	private  void printem() {
		int numTopics=model.getNumTopics();
		// Show the words and topics in the first instance

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();
        
        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;
        
        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }
        System.out.println(out);
        
        System.out.println("Estimate the topic distribution of the first instance");
        System.out.println(instances.get(0).toString());
        // Estimate the topic distribution of the first instance, 
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        
        System.out.println("Show top 5 words in topics with proportions for the first document");
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            
            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;
            }
            System.out.println(out);
        }
        
  testArtificialDocument(0);
  testArtificialDocument(10);
  testArtificialDocument(50);
  testArtificialDocument(70);
        
      	
	}
	private void testArtificialDocument(int testid) {      
		System.out.println("Create a new instance with high probability of topic "+testid);
    // Create a new instance with high probability of topic 0

    String txt=lable(testid);
    System.out.println("it should be: "+txt);
    // Create a new instance named "test instance" with empty target and source fields.
   List<ScoredTopics> stopics = inferTopicsFor(txt);
   Map<Integer, ScoredTopics> ind=indexScoredTopics(stopics);
  Collections.sort(stopics);
  Collections.reverse(stopics);
    System.out.println("and is: "+lable(stopics.get(0).getTopicid()));
    
    for(int i=0;i<3;i++)
    System.out.println(stopics.get(i).getTopicid()+"\t" + stopics.get(i).getScore() +" should be "+ind.get(testid).score+lable(stopics.get(i).getTopicid()));
    
   
    
	}
	private Map<Integer, ScoredTopics> indexScoredTopics(
			List<ScoredTopics> stopics) {
		HashMap<Integer, ScoredTopics> ret=new HashMap<Integer, ScoredTopics>();
		for(ScoredTopics t:stopics)
		{
			ret.put(t.getTopicid(), t);
		}
		return ret;
	}
	private String lable(int topicId) {
		
	Alphabet dataAlphabet = instances.getDataAlphabet();
	StringBuilder topicZeroText = new StringBuilder();
	 ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
    Iterator<IDSorter> iterator = topicSortedWords.get(topicId).iterator();


    int rank = 0;
   while (iterator.hasNext() && rank < 5) {
       IDSorter idCountPair = iterator.next();
       topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
       rank++;
   }
   return topicZeroText.toString();
	}
	private List<ScoredTopics> inferTopicsFor(String str) { 
		  List<ScoredTopics> ret=new ArrayList<ScoredTopics>();
		InstanceList testing = new InstanceList(instances.getPipe());
   
   testing.addThruPipe(new Instance(str, null, "test instance", null));

    TopicInferencer inferencer = model.getInferencer();
    int i=0;
 for(double d: inferencer.getSampledDistribution(testing.get(0), 10, 1, 5))
 {
	 ret.add(new ScoredTopics(i, d));
	 i++;
 }
  

return ret;
	}
	/*
	private double[] inferTopicsFor(String str) { 
		InstanceList testing = new InstanceList(instances.getPipe());
   
   testing.addThruPipe(new Instance(str, null, "test instance", null));

    TopicInferencer inferencer = model.getInferencer();
  double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
  return testProbabilities;
	}
	*/
	
	Pattern pattern = Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$");
	public  void addTrainingInstance(String str) throws UnsupportedEncodingException { 
		Reader fileReader = new InputStreamReader(new ByteArrayInputStream(str.getBytes("UTF-8")),"UTF-8");
    instances.addThruPipe(new CsvIterator (fileReader, pattern,
                3, 2, 1)); // data, label, name fields}
	}
	public static int[] getHighestIndexes(double[] data, int topN) {
	     if (data.length < topN) {
	        topN=data.length;
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
	
	 private static int[] sequence(int n) {
	        int[] indexes = new int[n];
	        for(int i = 0;i < n;i++) {
	            indexes[i] = i;
	        }
	        return indexes;
	    }
}
