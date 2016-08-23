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
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
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
import de.l3s.db.DB;

public class MalletTopicModelling {

	
	
	private ParallelTopicModel model;
	public MalletTopicModelling(File modelfile) {
		
		
		try {
			 model = ParallelTopicModel.read(modelfile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public MalletTopicModelling(int numtopics)
	{
		model=new ParallelTopicModel(numtopics,1.0, 0.01);
	}
	Hashtable<Integer, String> idxlab=new Hashtable<Integer, String>();
	public String lable(int topicid)
	{
		String lable;
		if((lable=idxlab.get(topicid))==null)
		{
			ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
			 double[] topicDistribution = model.getTopicProbabilities(0);
			 Alphabet dataAlphabet = model.getAlphabet();
	        // Show top 5 words in topics with proportions for the first document
	    
	            Iterator<IDSorter> iterator = topicSortedWords.get(topicid).iterator();
	            
	            Formatter out = new Formatter(new StringBuilder(), Locale.US);
	           
	            int rank = 0;
	            StringBuilder sb=new StringBuilder();
	            while (iterator.hasNext() && rank < 5) {
	                IDSorter idCountPair = iterator.next();
	                if(sb.length()>0) sb.append(" ");
	                sb.append(dataAlphabet.lookupObject(idCountPair.getID()));
	               
	                rank++;
	            }
	            lable= sb.toString();
	        
		}
		return lable;
	}
	public ScoreTopic[] classify(String toclassify)
	{
		InstanceList instances =null;
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
    //    pipeList.add( new TokenSequenceRemoveStopwords(new File("C:\\Users\\singh\\Downloads\\mallet-2.0.7\\mallet-2.0.7\\stoplists\\en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );
        instances = new InstanceList (new SerialPipes(pipeList));
		
        InstanceList taginstance = new InstanceList(instances.getPipe());
		taginstance.addThruPipe(new Instance(toclassify, null, "test instance", null));
		double[] distribution = model.getInferencer().getSampledDistribution(taginstance.get(0), 0, 1, 5);

		for(int i=0;i<distribution.length;i++)
		{
			System.out.println(lable(i)+" ("+distribution[i]+")");
		}
		int[] indlist = getHighestIndexes(distribution, 3);
		
		ScoreTopic t1 = new ScoreTopic(indlist[0],distribution[indlist[0]]);
		ScoreTopic t2 = new ScoreTopic(indlist[1], distribution[indlist[1]]);
		ScoreTopic t3 = new ScoreTopic(indlist[2], distribution[indlist[2]]);
        
		return new ScoreTopic[]{t1,t2,t3};
		
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
	 private static int[] sequence(int n) {
	        int[] indexes = new int[n];
	        for(int i = 0;i < n;i++) {
	            indexes[i] = i;
	        }
	        return indexes;
	    }
	 
	public static void main(String[] args) {
	
		try {Class.forName("com.mysql.jdbc.Driver");
		test2();
		//test();
		if(true) return;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
		
	}
	
	
	
	
	private static void test() {
		File mddir=new File("C:\\data3\\zerr\\newautomodelsx\\");
		if(!mddir.exists())
			mddir=new File("/data3/zerr/newautomodelsx/");
		if(!mddir.exists()){mddir.mkdirs();}
		File modelfile=new File(mddir,"flower_wikimovies_nopersons_10.dat");
		int num_threads=1,num_iterations=200;
	
		try {
	MalletTopicModelling mt1=new MalletTopicModelling(10);
	mt1.setParameters(num_threads,num_iterations);
	
		InstanceList list1 = mt1.fetchFromDB("flower_wikimovies_nopersons",null);
	
	mt1.estimate();
	String toclassify="team war soldiers men battle";
	System.out.println("classify: "+toclassify);
	
	System.out.println("jaust created: ");
	for(ScoreTopic top:mt1.classify(toclassify))
	{
		System.out.println("id="+top.getTopicid()+" lable='"+mt1.lable(top.getTopicid())+"'");
	}
	
	
	mt1.storeModel(modelfile);

	
	MalletTopicModelling mt2=new MalletTopicModelling(modelfile);
	mt2.setParameters(num_threads,num_iterations);
	//InstanceList list2 = mt2.fetchFromDB("flower_wikimovies_nopersons");
	
	System.out.println("from file:");
	for(ScoreTopic top:mt2.classify(toclassify))
	{
		System.out.println("id="+top.getTopicid()+" lable='"+mt1.lable(top.getTopicid())+"'");
	}
	
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
		
	}
	private void setParameters(int num_threads,int num_iterations) {
		model.setNumThreads(num_threads);
        model.setNumIterations(num_iterations);
		
	}
	private void estimate() throws IOException {
		model.estimate();
		
	}
	private void storeModel(File modelfile) {
	model.write(modelfile);
		
	}
	public  InstanceList fetchFromDB(String dataset,InstanceList instances) throws ClassNotFoundException
	{
		

		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
	//	model = new ParallelTopicModel(numtopics, 1.0, 0.01);
        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
       /* if(local)
        	pipeList.add( new TokenSequenceRemoveStopwords(new File("C:\\Users\\singh\\Downloads\\mallet-2.0.7\\mallet-2.0.7\\stoplists\\en.txt"), "UTF-8", false, false, false) );
        else
        	pipeList.add( new TokenSequenceRemoveStopwords(new File("/home/singh/flower/en.txt"), "UTF-8", false, false, false) );*/
        pipeList.add( new TokenSequence2FeatureSequence() );

        if(instances==null)
         instances = new InstanceList (new SerialPipes(pipeList));
		try {
			Connection dbcon;
			
			 dbcon = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
			
			PreparedStatement pstmt= dbcon.prepareStatement("SELECT * FROM `"+dataset+"` WHERE 1");
			ResultSet rs = pstmt.executeQuery();
			 
	        System.out.println("Loading docs into instances");
	        
	        while(rs.next())
			{
	        	String text=rs.getString("lem_nouns");
if(text.trim().split(" ").length<2){continue;}

        		String key= rs.getString("category");
        		String[] categories = key.trim().split(",");
        		
        		for(String k: categories)
        		{
        			
        			
    	        	String str = rs.getInt("id")+" news "+text;
    	        
    		        Reader fileReader = new InputStreamReader(new ByteArrayInputStream(str.getBytes("UTF-8")),"UTF-8");
    		        
    		        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
    		                    3, 2, 1)); // data, label, name fields
        		}
        			
			}
	        rs.close();
	        //System.out.println(comp+","+rec+","+talk);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 model.addInstances(instances);
		 return instances;
		

	}
	private static void test2() throws Exception
	{


        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
      //  pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        
        fetchFromDBI("flower_wikimovies_nopersons",instances);

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 100;
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(50);
        model.estimate();
        printem(model, numTopics, instances);
        File modelf=new File("modtodelete");
        model.write(modelf);
        model=ParallelTopicModel.read(modelf);
        System.out.println("reload");
        

        printem(model, numTopics, instances);
	}
	private static void printem( ParallelTopicModel model, int numTopics, InstanceList instances) {
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
        
        System.out.println("Create a new instance with high probability of topic 0");
        // Create a new instance with high probability of topic 0
        StringBuilder topicZeroText = new StringBuilder();
        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

        int rank = 0;
        while (iterator.hasNext() && rank < 5) {
            IDSorter idCountPair = iterator.next();
            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            rank++;
        }

        // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(instances.getPipe());
        
        System.out.println("it should be: "+topicZeroText.toString());
        testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
        int[] idx = getHighestIndexes(testProbabilities, 3);
        System.out.println(idx[0]+"\t" + testProbabilities[0]);
        
        
        System.out.println("Create a new instance with high probability of topic 10");
        // Create a new instance with high probability of topic 0
         topicZeroText = new StringBuilder();
        iterator = topicSortedWords.get(10).iterator();

         rank = 0;
        while (iterator.hasNext() && rank < 5) {
            IDSorter idCountPair = iterator.next();
            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            rank++;
        }

        // Create a new instance named "test instance" with empty target and source fields.
         testing = new InstanceList(instances.getPipe());
         topicZeroText.append(" school ");
        System.out.println("it should be: "+topicZeroText.toString());
        testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

         inferencer = model.getInferencer();
       testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
    idx = getHighestIndexes(testProbabilities, 3);
        System.out.println(idx[0]+"\t" + testProbabilities[0]);
		
	}
	private static InstanceList fetchFromDBI(String dataset, InstanceList instances) {
		

		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
	//	model = new ParallelTopicModel(numtopics, 1.0, 0.01);
        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
       /* if(local)
        	pipeList.add( new TokenSequenceRemoveStopwords(new File("C:\\Users\\singh\\Downloads\\mallet-2.0.7\\mallet-2.0.7\\stoplists\\en.txt"), "UTF-8", false, false, false) );
        else
        	pipeList.add( new TokenSequenceRemoveStopwords(new File("/home/singh/flower/en.txt"), "UTF-8", false, false, false) );*/
        pipeList.add( new TokenSequence2FeatureSequence() );

        if(instances==null)
         instances = new InstanceList (new SerialPipes(pipeList));
		try {
			Connection dbcon;
			
			
			 dbcon = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
			
			PreparedStatement pstmt= dbcon.prepareStatement("SELECT * FROM `"+dataset+"` WHERE 1");
			ResultSet rs = pstmt.executeQuery();
			 
	        System.out.println("Loading docs into instances");
	        
	        while(rs.next())
			{
	        	String text=rs.getString("lem_nouns");
if(text.trim().split(" ").length<2){continue;}

        		String key= rs.getString("category");
        		String[] categories = key.trim().split(",");
        		
        		for(String k: categories)
        		{
        			
        			
    	        	String str = rs.getInt("id")+" news "+text;
    	        
    		        Reader fileReader = new InputStreamReader(new ByteArrayInputStream(str.getBytes("UTF-8")),"UTF-8");
    		        
    		        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
    		                    3, 2, 1)); // data, label, name fields
        		}
        			
			}
	        rs.close();
	        //System.out.println(comp+","+rec+","+talk);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		 return instances;
		

	}
}
