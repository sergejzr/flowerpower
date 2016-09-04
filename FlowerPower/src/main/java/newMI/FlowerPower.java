package newMI;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import de.l3s.db.DB;
import de.l3s.flower.Term;
import de.l3s.flower.TermLink;
import de.l3s.flower.Topic;
import de.l3s.flower.TopicLink;
import de.l3s.lemma.lemma;
import l3s.rdj.Diversity;
import l3s.rdj.Document;
import l3s.rdj.impl.AllPairsDJ;
import l3s.toolbox.JaccardSimilarityComparator;
import test.DiagramInput;

//import meta.dbaccess.*; 
//import meta.dbinterfaces.*; 

//import weka.core.*; 
//import weka.classifiers.*; 
//import weka.classifiers.j48.*;
//import weka.filters.*; 

//import org.biojava.stats.svm.*;
//import org.biojava.stats.svm.tools.*;

//import meta.sparse.*; 

/**
 * Klasse zur MI-Selektion faer binaere Klassifikation
 */
public class FlowerPower {

	public enum OrderStrategy {

		naturalOrdering,

		optimalOrderung
	}
	private OrderStrategy ordering;
	public void setOrdering(OrderStrategy ordering) {
		this.ordering = ordering;
	}
	private class TermMI implements Comparable {
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
	public static void main(String[] args) {

		FlowerPower topicFlower = new FlowerPower(null, "dataset_20newsgroup_full", "dataset_20newsgroup_full", 200,
				null);
		topicFlower.generateTopicModel();
		topicFlower.printModel();
		topicFlower.limitDataset(400000);
		topicFlower.computeOrdering();
		topicFlower.computeMILists();
		topicFlower.computeMIPairLists();
		topicFlower.computeTop5();
		DiagramInput di2 = topicFlower.diagramGen();

		HashMap<Integer, String> temp = topicFlower.getIntCategory();
		// ArrayList<de.l3s.graphics.Petal> petals= new ArrayList<Petal>();
		System.out.println("Ordering: ");
		for (int i : topicFlower.getOptimalordering()) {
			System.out.print(temp.get(i) + ",");
			/*
			 * Petal p= new Petal(); p.setCategory(temp.get(i));
			 * p.setTopics(di2.getCategoryRepMap().get(""+i)); petals.add(p);
			 */
		}
		System.out.println();
		for (String key : di2.getCombiningTopics().keySet()) {
			ArrayList<TopicLink> list = di2.getCombiningTopics().get(key);
			String[] categoriesinpair = key.split("-");
			ArrayList<TopicLink> t1 = di2.getCategoryRepMap().get(categoriesinpair[0]);
			ArrayList<TopicLink> t2 = di2.getCategoryRepMap().get(categoriesinpair[1]);
			System.out.println(temp.get(Integer.parseInt(categoriesinpair[0])));
			System.out.println(Arrays.toString(t1.toArray()));
			System.out.println();
			System.out.println(temp.get(Integer.parseInt(categoriesinpair[0])) + "-"
					+ temp.get(Integer.parseInt(categoriesinpair[1])));
			System.out.println("similarity score:" + topicFlower.getScoremap().get(key));
			System.out.println(Arrays.toString(list.toArray()));
			System.out.println();
			System.out.println(temp.get(Integer.parseInt(categoriesinpair[1])));
			System.out.println(Arrays.toString(t2.toArray()));
			System.out.println("-----------------------------------------------");

		}
		// Flower f= new Flower(petals.size(), petals);
		// f.generate();

	}
	public int[] catarray;
	public HashMap<String, Integer> categoryInt = new HashMap<String, Integer>();
	HashMap<String, ArrayList<TopicLink>> categoryRepMap;
	HashMap<String, ArrayList<Double>> categoryScoresMap = new HashMap<String, ArrayList<Double>>();
	private Hashtable<String, int[]> clustered = new Hashtable<String, int[]>();
	HashMap<String, ArrayList<TopicLink>> combiningTopics;
	private Connection con;
	DiagramInput di;

	public HashMap<String, ArrayList<DocMI>> docMap = new HashMap<String, ArrayList<DocMI>>();
	public HashMap<Integer, ArrayList<DocMI>> DocsMap = new HashMap<Integer, ArrayList<DocMI>>();
	private String flower_dataset;
	public HashMap<String, Integer> idCatCount = new HashMap<String, Integer>();
	public HashMap<String, String> idText = new HashMap<String, String>();
	Hashtable<Integer, String> instansids = new Hashtable<Integer, String>();
	public HashMap<Integer, String> intCategory = new HashMap<Integer, String>();
	HashMap<String, String> labels;
	public InstanceList loadedinstances = null;
	private MICategoryCal mic;
	public ParallelTopicModel model;
	private String model_dataset;
	private File modelcachedir;
	InstanceList newInst;
	private Integer nr_topics_for_instance;

	private int num_iterations = 1000;

	int numtopics;

	public List<Integer> optimalordering = new ArrayList<Integer>();



	public HashMap<String, Double> scoremap = new HashMap<String, Double>();

	public double scoremin = -1.;

	Hashtable<Integer, Term> termidx = new Hashtable<Integer, Term>();

	ArrayList<String> top5;

	ArrayList<Integer> top5link;

	public HashMap<String, Integer> userphotocount_map = new HashMap<String, Integer>();

	public FlowerPower(File modelcachedir, String dataset, int numtopics, Integer nr_topics_for_instance) {
		this(modelcachedir, dataset, dataset, numtopics, null, nr_topics_for_instance, null);
	}

	public FlowerPower(File modelcachedir, String model_dataset, String flower_dataset, int numtopics, Connection con,
			Integer nr_topics_for_instance, Integer num_iterations) {
		categoryInt = new HashMap<String, Integer>();
		this.modelcachedir = modelcachedir;
		categoryRepMap = new HashMap<String, ArrayList<TopicLink>>();
		top5 = new ArrayList<String>();
		top5link = new ArrayList<Integer>();
		this.model_dataset = model_dataset;
		this.flower_dataset = flower_dataset;
		this.numtopics = numtopics;
		di = new DiagramInput();
		labels = new HashMap<String, String>();
		this.con = con;
		this.nr_topics_for_instance = nr_topics_for_instance;
		if (num_iterations != null)
			this.num_iterations = num_iterations;
	}

	public FlowerPower(File modelcachedir, String model_dataset, String flower_dataset, int numtopics,
			Integer nr_topics_for_instance) {
		this(modelcachedir, model_dataset, flower_dataset, numtopics, null, nr_topics_for_instance, null);
	}

	public Hashtable<Integer, Topic> applyTopicModel(File modelfile) {

		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		// model = new ParallelTopicModel(numtopics, 1.0, 0.01);
		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		/*
		 * if(local) pipeList.add( new TokenSequenceRemoveStopwords(new File(
		 * "C:\\Users\\singh\\Downloads\\mallet-2.0.7\\mallet-2.0.7\\stoplists\\en.txt"
		 * ), "UTF-8", false, false, false) ); else pipeList.add( new
		 * TokenSequenceRemoveStopwords(new File("/home/singh/flower/en.txt"),
		 * "UTF-8", false, false, false) );
		 */
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));

		try {
			instances = fetchFromDB(instances, flower_dataset, false);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		userphotocount_map = null;
		loadedinstances = instances;

		System.out.println("serializing for future use");

		if (modelfile.exists()) {
			try {
				model = ParallelTopicModel.read(modelfile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			model.addInstances(instances);
			model.setNumThreads(4);
			model.setNumIterations(400);
			try {
				model.estimate();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			model.write(modelfile);
		}
		generateLabels(model, numtopics);

		Hashtable<Integer, Topic> ret = generateLabels(model, numtopics);

		return ret;
	}

	private void buildScoreIndex() {
		for (int i : intCategory.keySet()) {
			for (int j : intCategory.keySet()) {
				if (j <= i)
					continue;
				double s = jaccardPairScore(DocsMap.get(i), DocsMap.get(j));

				scoremap.put(i + "-" + j, s);
				scoremap.put(j + "-" + i, s);

			}
		}

	}

	private List<Integer> calculateOptimalOrderingJaccard(List<Integer> categories) {
		System.out.println("building score index");
		buildScoreIndex();
		System.out.println("computing ordering");
		switch (this.ordering) {
		case optimalOrderung:
			permute(categories, 0);
			break;
		case naturalOrdering:
			Collections.sort(categories, new MySorter(intCategory) );
			scoremin = 0;
			optimalordering.clear();
			optimalordering.addAll(categories);
			
			break;

		}
		// if(calculateOrdering)
		
		System.out.println("optimal ordering found");
		System.out.println(java.util.Arrays.toString(optimalordering.toArray()));
		System.out.println("avg pairwise jaccard score=" + scoremin);
		return optimalordering;
	}

	public boolean canPhotoBeChosen(String userid, String tags) {
		if (userphotocount_map.get(userid) == null) {
			userphotocount_map.put(userid, 0);
		}
		if (tags.split(" ").length < 35 && userphotocount_map.get(userid) < 50) {
			int t = userphotocount_map.get(userid);
			t++;
			userphotocount_map.put(userid, t);
			return true;
		} else {
			return false;
		}

	}

	private double computeAvgPairwiseSimilarityScore(List<Integer> arr) {
		int[] array = toIntArray(arr);
		double sum = 0;
		double oppdissimilarity = 0;

		int gap = array.length / 2;
		for (int i = 0; i < array.length; i++) {
			int k = i + 1;

			if (i == array.length - 1) // maybe no need for circular check
			{
				k = 0;
				// break;
			}

			sum += jaccardPairScoreWithIndex(array[i], array[k]);
		}
		/*
		 * for(int i=0;i<array.length/2;i++)//opposite categories should be
		 * dissimilar { int k=i+gap; oppdissimilarity+=
		 * 1-jaccardPairScoreWithIndex(array[i],array[k]); }
		 */

		double score = (double) sum / array.length + (double) oppdissimilarity / (array.length / 2);
		return score;
	}

	public void computeMILists() {
		System.out.println("Get MI lists");
		newInst = new InstanceList(loadedinstances.getPipe());
		mic = new MICategoryCal(DocsMap, model, loadedinstances, labels, idCatCount);
		categoryRepMap = new HashMap<String, ArrayList<TopicLink>>();

		if (false && nr_topics_for_instance != null) {
			for (int i = 0; i < loadedinstances.size(); i++) {
				double[] distribution = model.getInferencer().getSampledDistribution(loadedinstances.get(i), 100, 2,
						15);
				int[] highest = getHighestIndexes(distribution, nr_topics_for_instance);
				clustered.put(instansids.get(i), highest);
			}
		}

		for (int i : intCategory.keySet()) {
			System.out.println();

			mic.computePositiveAndNegativeMIvalues(i, numtopics, (InstanceList) newInst.clone());
			ArrayList<newMI.MICategoryCal.TermMI> temp = mic.getCategoryRepresentativesMap().get("" + i);
			System.out.println();
			System.out.println(intCategory.get(i) + "(" + i + ")");
			ArrayList<TopicLink> t2 = new ArrayList<TopicLink>();

			for (newMI.MICategoryCal.TermMI t : temp) {
				System.out.print(labels.get(t.term) + "(" + t.term + "), ");
				TopicLink tl = new TopicLink();
				tl.setTid(Integer.parseInt(t.term));
				tl.setScore(t.value);
				t2.add(tl);
			}

			categoryRepMap.put("" + i, t2);
			// categoryScoresMap.put(""+i,scores );
			System.out.println();
		}

	}

	public void computeMIPairLists() {
		System.out.println("Pair wise representatives- most representative terms for combined categories");
		combiningTopics = new HashMap<String, ArrayList<TopicLink>>();
		for (int k = 0; k < catarray.length; k++) {
			Integer i = catarray[k];
			Integer j = null;
			if (k == catarray.length - 1) {
				j = catarray[0];
			}

			else {
				j = catarray[k + 1];
			}

			// MICategoryCal mic= new
			// MICategoryCal(DocsMap,model,loadedinstances);
			mic.computeValuesPairs(i, j, numtopics, (InstanceList) newInst.clone());
			System.out.println("" + intCategory.get(i) + "-" + intCategory.get(j));

			ArrayList<newMI.MICategoryCal.TermMI> temp = mic.getCategoryRepresentativesMap().get("" + i + "-" + j);
			ArrayList<TopicLink> temp2 = new ArrayList<TopicLink>();
			for (newMI.MICategoryCal.TermMI term : temp) {
				System.out.print(labels.get(term.term) + ", ");
				TopicLink tl = new TopicLink();
				tl.setTid(Integer.parseInt(term.term));
				tl.setScore(term.value);
				temp2.add(tl);
			}
			System.out.println();
			combiningTopics.put("" + i + "-" + j, temp2);
		}

	}

	public void computeOrdering() {

		List<Integer> categories = new ArrayList<Integer>();
		categories.addAll(DocsMap.keySet());
		if (categories.size() > 1) {
			categories = calculateOptimalOrderingJaccard(categories);

		}
		catarray = toIntArray(categories);

	}

	public List<TopicLink> computeTop5() {
		List<TopicLink> ret = new ArrayList<TopicLink>();
		mic.centreCalculationDiversity();// New method with evenness index
		System.out.println("Top 5 most probable topics in corpus");

		top5 = new ArrayList<String>();
		top5link = new ArrayList<Integer>();
		for (int k : mic.top5) {
			System.out.print(labels.get("" + k) + ", ");
			top5.add(labels.get("" + k));
			top5link.add(k);
		}
		int[] g = mic.topall;
		double rank = Double.MAX_VALUE;
		for (int k : mic.top5) {

			// double d = mic.topdistributions[k];

			TopicLink tl = new TopicLink();
			tl.setTid(k);
			tl.setScore(rank -= .001);
			ret.add(tl);

		}

		return ret;

	}

	public List<TopicLink> computeTopN(int n) {
		List<TopicLink> ret = new ArrayList<TopicLink>();
		mic.centreCalculationDiversity();// New method with evenness index
		System.out.println("Top 5 most probable topics in corpus");

		top5 = new ArrayList<String>();
		top5link = new ArrayList<Integer>();
		for (int k : mic.top5) {
			System.out.print(labels.get("" + k) + ", ");
			top5.add(labels.get("" + k));
			top5link.add(k);
		}
		int[] g = mic.topall;
		double rank = Double.MAX_VALUE;
		int i = 0;
		for (int k : g) {

			// double d = mic.topdistributions[k];

			TopicLink tl = new TopicLink();
			tl.setTid(k);
			tl.setScore(rank -= .001);
			ret.add(tl);
			i++;
			if (i > n - 1)
				break;

		}

		return ret;

	}

	public DiagramInput diagramGen() {
		di = new DiagramInput(top5, top5link, categoryRepMap, combiningTopics, optimalordering, 100);
		return di;
	}

	public InstanceList fetchFromDB(InstanceList instances, String dataset, boolean onlyinstances)
			throws ClassNotFoundException {

		try {
			Connection dbcon = this.con;
			if (this.con == null) {
				Class.forName("com.mysql.jdbc.Driver");
				this.con = 	 dbcon = DB.getConnection("l3s","flickrattractive");
				
			}
			PreparedStatement pstmt = dbcon.prepareStatement("SELECT * FROM `" + dataset + "` WHERE 1");
			ResultSet rs = pstmt.executeQuery();

			System.out.println("Loading docs into instances");

			while (rs.next()) {
				String text = rs.getString("lem_nouns");
				if (text == "" || numOfWords(text) < 3)
					continue;
				String key = rs.getString("category");
				String[] categories = key.trim().split(",");

				for (String k : categories) {
					if (!onlyinstances) {
						ArrayList<DocMI> t = docMap.get(k);
						if (t == null) {
							t = new ArrayList<DocMI>();
						}

						t.add(new DocMI("" + rs.getInt("id"), text, rs.getString("document_id")));
						docMap.put(k, t);
						Integer c = idCatCount.get("" + rs.getInt("id"));
						if (c == null)
							c = 0;
						idCatCount.put("" + rs.getInt("id"), ++c);
					}
					String str = rs.getInt("id") + " news " + text;

					Reader fileReader = new InputStreamReader(new ByteArrayInputStream(str.getBytes("UTF-8")), "UTF-8");
					int curidx = instances.size();
					instansids.put(curidx, rs.getString("document_id"));
					instances.addThruPipe(
							new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data,
																														// label,
																														// name
																														// fields
				}

			}
			// System.out.println(comp+","+rec+","+talk);
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
		if (!onlyinstances) {
			System.out.println("Categories: " + docMap.keySet().size());
			int c = 0;
			for (String s : docMap.keySet()) {
				categoryInt.put(s, c);
				intCategory.put(c, s);
				c++;
				System.out.println(s + " docs:" + docMap.get(s).size());
			}
		}
		return instances;

	}

	public InstanceList fetchFromDB_old(InstanceList instances, String dataset) throws ClassNotFoundException {

		try {
			Connection dbcon = this.con;
			if (this.con == null) {
				Class.forName("com.mysql.jdbc.Driver");
				this.con = 	 dbcon = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
				
			}
			PreparedStatement pstmt = dbcon.prepareStatement("SELECT * FROM `" + getDataset() + "` WHERE 1");
			ResultSet rs = pstmt.executeQuery();

			System.out.println("Loading docs into instances");

			while (rs.next()) {
				String text = rs.getString("lem_nouns");
				if (text == "" || numOfWords(text) < 3)
					continue;
				String key = rs.getString("category");
				String[] categories = key.trim().split(",");

				for (String k : categories) {
					ArrayList<DocMI> t = docMap.get(k);
					if (t == null) {
						t = new ArrayList<DocMI>();
					}

					t.add(new DocMI("" + rs.getInt("id"), text, rs.getString("document_id")));
					docMap.put(k, t);
					Integer c = idCatCount.get("" + rs.getInt("id"));
					if (c == null)
						c = 0;
					idCatCount.put("" + rs.getInt("id"), ++c);
					String str = rs.getInt("id") + " news " + text;
					Reader fileReader = new InputStreamReader(new ByteArrayInputStream(str.getBytes()), "UTF-8");
					instances.addThruPipe(
							new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data,
																														// label,
																														// name
																														// fields
				}

			}
			// System.out.println(comp+","+rec+","+talk);
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
		System.out.println("Categories: " + docMap.keySet().size());
		int c = 0;
		for (String s : docMap.keySet()) {
			categoryInt.put(s, c);
			intCategory.put(c, s);
			c++;
			System.out.println(s + " docs:" + docMap.get(s).size());
		}

		return instances;

	}

	private Hashtable<Integer, Topic> generateLabels(ParallelTopicModel model, int numTopics) {
		Hashtable<Integer, Topic> ret = new Hashtable<Integer, Topic>();

		labels = new HashMap<String, String>();
		Alphabet dataAlphabet = model.getAlphabet();
		// Get an array of sorted sets of word ID/count pairs
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		double[] topicDistribution = model.getTopicProbabilities(0);
		// Show top 5 words in topics with proportions for the first document
		for (int topic = 0; topic < numTopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
			String content = new String();
			int rank = 0;
			// content+=topic+"\t";
			Topic t = new Topic();
			t.setTid(topic);
			t.setScore(topicDistribution[topic]);

			ret.put(topic, t);

			while (iterator.hasNext() && rank < 5) {
				IDSorter idCountPair = iterator.next();
				content += dataAlphabet.lookupObject(idCountPair.getID()) + " ";
				Term term = termidx.get(idCountPair.getID());

				if (term == null) {
					term = new Term();
					term.setValue(dataAlphabet.lookupObject(idCountPair.getID()) + "");
					term.setTid(idCountPair.getID());

					termidx.put(idCountPair.getID(), term);
				}
				termidx.put(idCountPair.getID(), term);

				rank++;
			}
			for (IDSorter ts : topicSortedWords.get(topic)) {
				TermLink tt = new TermLink();

				tt.setTid(ts.getID());
				tt.setScore(ts.getWeight());
				t.getTerm().add(tt);
			}
			// System.out.println(topic+": "+topicDistribution[topic]+"
			// "+content);
			t.setLable(content);
			labels.put("" + topic, content);
			System.out.println(content);

		}

		System.out.println("Done");
		// labels=
		// readLabelsFromFile("C:\\Users\\singh\\Desktop\\newsmodel"+numTopics+"labelset.txt");
		return ret;
	}

	public Hashtable<Integer, Topic> generateTopicModel() {

		String chunk = flower_dataset.equals(model_dataset) ? flower_dataset : flower_dataset + "_" + model_dataset;

		File modelfile = new File(chunk + "_" + numtopics + ".dat");
		if (modelcachedir != null) {
			modelfile = new File(modelcachedir, chunk + "_" + numtopics + ".dat");
		}

		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		model = new ParallelTopicModel(numtopics, 1.0, 0.01);
		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		/*
		 * if(local) pipeList.add( new TokenSequenceRemoveStopwords(new File(
		 * "C:\\Users\\singh\\Downloads\\mallet-2.0.7\\mallet-2.0.7\\stoplists\\en.txt"
		 * ), "UTF-8", false, false, false) ); else pipeList.add( new
		 * TokenSequenceRemoveStopwords(new File("/home/singh/flower/en.txt"),
		 * "UTF-8", false, false, false) );
		 */
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));

		try {

			if (flower_dataset.equals(model_dataset)) {
				instances = loadedinstances = fetchFromDB(instances, model_dataset, false);
			} else {
				instances = fetchFromDB(instances, model_dataset, true);
				loadedinstances = fetchFromDB(instances, flower_dataset, false);
			}
		} catch (ClassNotFoundException e1) {

			e1.printStackTrace();
		}
		userphotocount_map = null;

		// loadedinstances=instances;

		System.out.println("serializing for future use");

		if (modelfile.exists()) {
			try {
				model = ParallelTopicModel.read(modelfile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			model.addInstances(instances);
			model.setNumThreads(1);
			model.setNumIterations(num_iterations);
			try {
				model.estimate();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			model.write(modelfile);
		}
		generateLabels(model, numtopics);

		Hashtable<Integer, Topic> ret = generateLabels(model, numtopics);

		return ret;
	}

	public int[] getCatarray() {
		return catarray;
	}

	public HashMap<String, Integer> getCategoryInt() {
		return categoryInt;
	}

	public HashMap<String, ArrayList<TopicLink>> getCategoryRepMap() {
		return categoryRepMap;
	}

	public HashMap<String, ArrayList<Double>> getCategoryScoresMap() {
		return categoryScoresMap;
	}

	public Hashtable<String, int[]> getClustered() {
		return clustered;
	}

	public HashMap<String, ArrayList<TopicLink>> getCombiningTopics() {
		return combiningTopics;
	}

	public String getDataset() {
		return model_dataset;
	}

	public DiagramInput getDi() {
		return di;
	}

	public HashMap<String, ArrayList<DocMI>> getDocMap() {
		return docMap;
	}

	ArrayList<DocMI> getDocsByCategory(String conf, int limit) {
		ArrayList<DocMI> result = new ArrayList<DocMI>();
		try {
			result = (ArrayList<DocMI>) docMap.get(conf).subList(0, limit);
		} catch (Exception e) {
			System.out.println("limit!!!!");
			result = (ArrayList<DocMI>) docMap.get(conf);
		}

		return result;

	}

	public HashMap<Integer, ArrayList<DocMI>> getDocsMap() {
		return DocsMap;
	}

	public int[] getHighestIndexes(double[] data, int topN) {
		if (data.length <= topN) {
			return sequence(topN);
		}
		int[] bestIndex = new int[topN];
		double[] bestVals = new double[topN];

		bestIndex[0] = 0;
		bestVals[0] = data[0];

		for (int i = 1; i < topN; i++) {
			int j = i;
			while ((j > 0) && (bestVals[j - 1] < data[i])) {
				bestIndex[j] = bestIndex[j - 1];
				bestVals[j] = bestVals[j - 1];
				j--;
			}
			bestVals[j] = data[i];
			bestIndex[j] = i;
		}

		for (int i = topN; i < data.length; i++) {
			if (bestVals[topN - 1] < data[i]) {
				int j = topN - 1;
				while ((j > 0) && (bestVals[j - 1] < data[i])) {
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

	public HashMap<Integer, String> getIntCategory() {
		return intCategory;
	}

	public HashMap<String, String> getLabels() {
		return labels;
	}

	public InstanceList getLoadedinstances() {
		return loadedinstances;
	}

	public MICategoryCal getMic() {
		return mic;
	}

	public ParallelTopicModel getModel() {
		return model;
	}

	public InstanceList getNewInst() {
		return newInst;
	}

	public String getNounsFromSentence(String s, lemma lem) {
		StringBuilder sb = new StringBuilder();
		String t = lem.getSentenceLemmatizationWithPOS(s);
		String[] pos = t.split("\\n");
		if (t.isEmpty())
			return null;
		for (String k : pos) {
			String[] wordpos = k.split("\\t");
			if (wordpos[1].startsWith("n") && !wordpos[1].equalsIgnoreCase("nil")) {

				sb.append(wordpos[2] + " ");
			}

		}

		return new String(sb).trim();

	}

	public Integer getNumDocs(Integer cat) {
		return mic.getCategoryDocumentsMap().get(cat).size();
	}

	public int getNumtopics() {
		return numtopics;
	}

	public List<Integer> getOptimalordering() {
		return optimalordering;
	}

	public HashMap<String, Double> getScoremap() {
		return scoremap;
	}

	public double getScoremin() {
		return scoremin;
	}

	public Hashtable<Integer, Term> getTermidx() {
		return termidx;
	}

	public ArrayList<String> getTop5() {
		return top5;
	}

	public Hashtable<String, int[]> getTopicDistribution() {
		return clustered;
	}

	public HashMap<String, Integer> getUserphotocount_map() {
		return userphotocount_map;
	}

	public double jaccardPairScore(ArrayList<DocMI> category1, ArrayList<DocMI> category2) {
		Vector<Document> collection = new Vector<Document>();

		Document d1 = new Document();
		ArrayList<String> temp = new ArrayList<String>();
		for (DocMI d : category1)
			temp.addAll(Arrays.asList(d.text.split(" ")));
		d1.addAll(temp);
		temp = new ArrayList<String>();
		for (DocMI d : category2)
			temp.addAll(Arrays.asList(d.text.split(" ")));
		Document d2 = new Document();
		d2.addAll(temp);
		collection.add(d1);
		collection.add(d2);
		return similarity(d1, d2);

	}

	public double jaccardPairScore_manydocs(ArrayList<DocMI> category1, ArrayList<DocMI> category2) {
		Vector<Document> collection = new Vector<Document>();

		for (DocMI d : category1) {
			Document docum = new Document();

			docum.add(Arrays.asList(d.text.split(" ")));
			collection.add(docum);
		}
		for (DocMI d : category2) {
			Document docum = new Document();

			docum.add(Arrays.asList(d.text.split(" ")));
			collection.add(docum);
		}

		double error = .05, confidentiality = .95;

		JaccardSimilarityComparator similarityComparator = new JaccardSimilarityComparator();

		Diversity dj1 = new AllPairsDJ(collection, error, confidentiality, similarityComparator);
		System.out.println("RDJ:" + dj1.getRDJ());

		// Diversity dj2 = new SampleDJ(collection, error,
		// confidentiality,similarityComparator);
		// System.out.println("RDJ:" + dj2.getRDJ());

		// TracjDJ works only with Jaccard similarity measure.
		// Diversity dj3 = new TrackDJ(collection, confidentiality,
		// confidentiality);
		// System.out.println("RDJ:" + dj3.getRDJ());
		return dj1.getRDJ();
	}

	public double jaccardPairScore_twodocs(ArrayList<DocMI> category1, ArrayList<DocMI> category2) {
		Vector<Document> collection = new Vector<Document>();

		Document d1 = new Document();
		ArrayList<String> temp = new ArrayList<String>();
		for (DocMI d : category1)
			temp.add(d.text);
		d1.addAll(temp);
		temp = new ArrayList<String>();
		for (DocMI d : category2)
			temp.add(d.text);
		Document d2 = new Document();
		d2.addAll(temp);
		collection.add(d1);
		collection.add(d2);

		double error = .005, confidentiality = .95;

		JaccardSimilarityComparator similarityComparator = new JaccardSimilarityComparator();

		Diversity dj1 = new AllPairsDJ(collection, error, confidentiality, similarityComparator);
		System.out.println("RDJ:" + dj1.getRDJ());

		// Diversity dj2 = new SampleDJ(collection, error,
		// confidentiality,similarityComparator);
		// System.out.println("RDJ:" + dj2.getRDJ());

		// TracjDJ works only with Jaccard similarity measure.
		// Diversity dj3 = new TrackDJ(collection, confidentiality,
		// confidentiality);
		// System.out.println("RDJ:" + dj3.getRDJ());
		return dj1.getRDJ();
	}

	private double jaccardPairScoreWithIndex(int i, int k) {
		String s = i + "-" + k;

		return scoremap.get(s);
	}

	/**
	 * main-Methode zum Testen
	 */
	public void limitDataset(int numDocs) {

		for (String c : categoryInt.keySet()) {
			ArrayList<DocMI> list = new ArrayList<DocMI>();

			list = getDocsByCategory(c.toString(), numDocs);

			DocsMap.put(categoryInt.get(c), list);
		}

		docMap = null;
	}

	private HashMap<String, String> loadStopWordList() {
		File file = new File("C:\\Users\\singh\\Desktop\\confstopwords.txt");
		HashMap<String, String> wordlem = new HashMap<String, String>();
		lemma lem = new lemma();
		lem.init();
		try {
			FileReader reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);
			String line;
			while ((line = br.readLine()) != null) {
				if (line.equals(""))
					continue;
				System.out.println(line.trim());
				String lemword = lem.getLemmatization(line);
				wordlem.put(line, lemword);
			}

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return wordlem;

	}

	private int numOfWords(String string) {
		int n = string.split(" ").length;
		return n;
	}

	void permute(java.util.List<Integer> arr, int k) {

		for (int i = k; i < arr.size(); i++) {
			java.util.Collections.swap(arr, i, k);
			permute(arr, k + 1);
			java.util.Collections.swap(arr, k, i);
		}
		if (k == arr.size() - 1) {

			double score = computeAvgPairwiseSimilarityScore(arr);
			if (score > scoremin) {
				scoremin = score;
				optimalordering.clear();
				optimalordering.addAll(arr);
			}

		}

	}

	public void printModel() {

		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		double[] topicDistribution = model.getTopicProbabilities(0);
		Alphabet dataAlphabet = model.getAlphabet();
		// Show top 5 words in topics with proportions for the first document
		for (int topic = 0; topic < numtopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

			Formatter out = new Formatter(new StringBuilder(), Locale.US);
			out.format("%d\t%.7f\t", topic, topicDistribution[topic]);
			int rank = 0;
			while (iterator.hasNext() && rank < 20) {
				IDSorter idCountPair = iterator.next();
				out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
				rank++;
			}
			System.out.println(out);
		}

	}

	public HashMap<String, String> readLabelsFromFile(String path) {
		File f = new File(path);
		HashMap<String, String> labels = new HashMap<String, String>();
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				if (line.equals(""))
					continue;
				System.out.println(line);
				String[] labelvalue_pair = line.split("\\t");
				labels.put(labelvalue_pair[0], labelvalue_pair[1]);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return labels;

	}

	private int[] sequence(int n) {
		int[] indexes = new int[n];
		for (int i = 0; i < n; i++) {
			indexes[i] = i;
		}
		return indexes;
	}

	public void setCatarray(int[] catarray) {
		this.catarray = catarray;
	}

	public void setCategoryInt(HashMap<String, Integer> categoryInt) {
		this.categoryInt = categoryInt;
	}

	public void setCategoryRepMap(HashMap<String, ArrayList<TopicLink>> categoryRepMap) {
		this.categoryRepMap = categoryRepMap;
	}

	public void setCombiningTopics(HashMap<String, ArrayList<TopicLink>> combiningTopics) {
		this.combiningTopics = combiningTopics;
	};
	// private ordering

	public void setDataset(String dataset) {
		this.model_dataset = dataset;
	}

	public void setDi(DiagramInput di) {
		this.di = di;
	}

	public void setDocMap(HashMap<String, ArrayList<DocMI>> docMap) {
		this.docMap = docMap;
	}

	public void setDocsMap(HashMap<Integer, ArrayList<DocMI>> docsMap) {
		DocsMap = docsMap;
	}

	public void setIntCategory(HashMap<Integer, String> intCategory) {
		this.intCategory = intCategory;
	}

	public void setLabels(HashMap<String, String> labels) {
		this.labels = labels;
	}

	public void setLoadedinstances(InstanceList loadedinstances) {
		this.loadedinstances = loadedinstances;
	}

	public void setMic(MICategoryCal mic) {
		this.mic = mic;
	}

	public void setModel(ParallelTopicModel model) {
		this.model = model;
	}

	public void setNewInst(InstanceList newInst) {
		this.newInst = newInst;
	}

	public void setNumtopics(int numtopics) {
		this.numtopics = numtopics;
	}

	public void setOptimalordering(List<Integer> optimalordering) {
		this.optimalordering = optimalordering;
	}

	public void setScoremap(HashMap<String, Double> scoremap) {
		this.scoremap = scoremap;
	}

	public void setScoremin(double scoremin) {
		this.scoremin = scoremin;
	}

	public void setUserphotocount_map(HashMap<String, Integer> userphotocount_map) {
		this.userphotocount_map = userphotocount_map;
	}

	private double similarity(Document d1, Document d2) {

		if (d1.size() == 0 || d1.size() == 0) {
			return 0.0;
		}
		Set<Object> unionXY = new HashSet<Object>(d1);
		unionXY.addAll(d2);

		Set<Object> intersectionXY = new HashSet<Object>(d1);
		intersectionXY.retainAll(d2);

		// intersectionXY.retainAll(d2s);

		return (double) intersectionXY.size() / (double) unionXY.size();
	}

	private double similarity(List<String> x, List<String> y) {

		if (x.size() == 0 || y.size() == 0) {
			return 0.0;
		}

		Set<String> unionXY = new HashSet<String>(x);
		unionXY.addAll(y);

		Set<String> intersectionXY = new HashSet<String>(x);
		intersectionXY.retainAll(y);

		return (double) intersectionXY.size() / (double) unionXY.size();
	}

	public void storeModel() {
		String chunk = flower_dataset.equals(model_dataset) ? flower_dataset : flower_dataset + "_" + model_dataset;

		File modelfile = new File(modelcachedir, chunk + "_" + numtopics + ".dat");
		model.write(modelfile);
	}

	public int[] toIntArray(List<Integer> list) {
		int[] ret = new int[list.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = list.get(i);
		return ret;
	}

	private List<?> top(ArrayList list) {
		ArrayList<Object> top = new ArrayList<Object>();

		int cnt = 30;
		for (Object o : list) {
			if (cnt-- < 0)
				return top;
			top.add(o);

		}
		return top;

	}

}
