package de.l3s.flower;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
import de.l3s.aglorithm.diversity.AllPairsDJ;
import de.l3s.aglorithm.diversity.JaccardSimilarityComparator;
import de.l3s.algorithm.diversity.document.Document;
import de.l3s.algorithm.mutualinformation.DiagramInput;
import de.l3s.algorithm.mutualinformation.DocMI;
import de.l3s.algorithm.mutualinformation.MICategoryCal;
import de.l3s.algorithm.mutualinformation.MySorter;
import de.l3s.algorithm.mutualinformation.MICategoryCal.TermMI;
import de.l3s.flower.jaxb.Term;
import de.l3s.flower.jaxb.TermLink;
import de.l3s.flower.jaxb.Topic;
import de.l3s.flower.jaxb.TopicLink;
import de.l3s.source.DataRow;
import de.l3s.source.DataSource;
import de.l3s.source.FowerReadException;

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

	public int[] catarray;
	public HashMap<String, Integer> categoryInt = new HashMap<String, Integer>();
	HashMap<String, ArrayList<TopicLink>> categoryRepMap;
	HashMap<String, ArrayList<Double>> categoryScoresMap = new HashMap<String, ArrayList<Double>>();
	private Hashtable<String, int[]> clustered = new Hashtable<String, int[]>();
	HashMap<String, ArrayList<TopicLink>> combiningTopics;

	DiagramInput di;

	public HashMap<String, ArrayList<DocMI>> docMap = new HashMap<String, ArrayList<DocMI>>();
	public HashMap<Integer, ArrayList<DocMI>> DocsMap = new HashMap<Integer, ArrayList<DocMI>>();

	public HashMap<String, Integer> idCatCount = new HashMap<String, Integer>();
	public HashMap<String, String> idText = new HashMap<String, String>();
	Hashtable<Integer, String> instansids = new Hashtable<Integer, String>();
	public HashMap<Integer, String> intCategory = new HashMap<Integer, String>();
	HashMap<String, String> labels;
	public InstanceList loadedinstances = null;
	private MICategoryCal mic;
	public ParallelTopicModel model;
	InstanceList newInst;

	public List<Integer> optimalordering = new ArrayList<Integer>();

	public HashMap<String, Double> scoremap = new HashMap<String, Double>();

	public double scoremin = -1.;

	Hashtable<Integer, Term> termidx = new Hashtable<Integer, Term>();

	ArrayList<String> top5;

	ArrayList<Integer> top5link;

	public HashMap<String, Integer> userphotocount_map = new HashMap<String, Integer>();

	private int numthreads;
	private int numtopics;
	private Integer nr_topics_for_instance;
	private File inputdir;
	private File backgrounddir;
	private boolean usemodel;
	private File modeloutputfile;
	private int iternumnum;
	private HashSet stoppwords;

	public FlowerPower(int numtopics, int nr_topics_for_instance, File inputdir, File backgrounddir, int ldathreadsnum,
			int iternumnum, boolean usemodel, File modeloutputfile, String[] stoppwords) {

		this.numtopics = numtopics;
		this.nr_topics_for_instance = nr_topics_for_instance;
		this.inputdir = inputdir;
		this.backgrounddir = backgrounddir;
		this.iternumnum = iternumnum;
		this.numthreads = ldathreadsnum;
		this.usemodel = usemodel;
		this.modeloutputfile = modeloutputfile;
		this.stoppwords=new HashSet<>(Arrays.asList(stoppwords));

		categoryRepMap = new HashMap<String, ArrayList<TopicLink>>();
		top5 = new ArrayList<String>();
		top5link = new ArrayList<Integer>();
		di = new DiagramInput();
		labels = new HashMap<String, String>();
		categoryInt = new HashMap<String, Integer>();
	}

	public Hashtable<Integer, Topic> applyTopicModel(File modelfile) throws FlowerException {

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

		instances = fetchFromDB(instances, new DataSource(inputdir), false);

		userphotocount_map = null;
		loadedinstances = instances;

		System.out.println("serializing for future use");

		if (modelfile.exists()) {
			try {
				model = ParallelTopicModel.read(modelfile);
				numtopics = model.getNumTopics();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			model.addInstances(instances);
			model.setNumThreads(this.numthreads);
			model.setNumIterations(this.iternumnum);
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
			Collections.sort(categories, new MySorter(intCategory));
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
			ArrayList<de.l3s.algorithm.mutualinformation.MICategoryCal.TermMI> temp = mic.getCategoryRepresentativesMap().get("" + i);
			System.out.println();
			System.out.println(intCategory.get(i) + "(" + i + ")");
			ArrayList<TopicLink> t2 = new ArrayList<TopicLink>();

			for (de.l3s.algorithm.mutualinformation.MICategoryCal.TermMI t : temp) {
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

			ArrayList<de.l3s.algorithm.mutualinformation.MICategoryCal.TermMI> temp = mic.getCategoryRepresentativesMap().get("" + i + "-" + j);
			ArrayList<TopicLink> temp2 = new ArrayList<TopicLink>();
			for (de.l3s.algorithm.mutualinformation.MICategoryCal.TermMI term : temp) {
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
		for (int k : mic.getTop5()) {
			System.out.print(labels.get("" + k) + ", ");
			top5.add(labels.get("" + k));
			top5link.add(k);
		}
		int[] g = mic.getTopall();
		double rank = Double.MAX_VALUE;
		for (int k : mic.getTop5()) {

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
		for (int k : mic.getTop5()) {
			System.out.print(labels.get("" + k) + ", ");
			top5.add(labels.get("" + k));
			top5link.add(k);
		}
		int[] g = mic.getTopall();
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

	public InstanceList fetchFromDB(InstanceList instances, DataSource dataset, boolean onlyinstances)
			throws FlowerException {

		try {
			dataset.connect();
			System.out.println("Loading docs into instances");

			while (dataset.hasNext()) {
				DataRow rs = dataset.getRow();
				String text = rs.getText();
				text=removeStoppWords(text);
				if (text == "" || numOfWords(text) < 2)
					continue;
				
				String key = rs.getCategory();
				String[] categories = key.trim().split(",");

				for (String k : categories) {
					if (!onlyinstances) {
						ArrayList<DocMI> t = docMap.get(k);
						if (t == null) {
							t = new ArrayList<DocMI>();
						}

						t.add(new DocMI("" + rs.getDocid(), text, rs.getDocstrid()));
						docMap.put(k, t);
						Integer c = idCatCount.get("" + rs.getDocid());
						if (c == null)
							c = 0;
						idCatCount.put("" + rs.getDocid(), ++c);
					}
					String str = rs.getDocid() + " news " + text;

					Reader fileReader = new InputStreamReader(new ByteArrayInputStream(str.getBytes("UTF-8")), "UTF-8");
					int curidx = instances.size();
					instansids.put(curidx, rs.getDocstrid());
					instances.addThruPipe(
							new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data,
																														// label,
																														// name
																														// fields
				}

			}
			// System.out.println(comp+","+rec+","+talk);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FowerReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (docMap.keySet().size() < 2) {
			ArrayList<String> cats = new ArrayList<>();
			for (String s : docMap.keySet()) {
				cats.add("category: " + s + ", count: " + docMap.get(s).size() + "\n");
			}

			throw new FlowerException(
					"The application can not run with a single category in your source table: \n" + cats);
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

	private String removeStoppWords(String text) {
		StringBuilder sb=new StringBuilder();
		for(String s:text.split("\\s+"))
		{
			if(stoppwords.contains(s)){continue;}
			if(sb.length()>0){}sb.append(" ");
			sb.append("s");
		}
		return sb.toString();
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

	public Hashtable<Integer, Topic> generateTopicModel() throws FlowerException {

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

		instances = fetchFromDB(instances, new DataSource(inputdir), false);

		if (backgrounddir != null) {
			instances = fetchFromDB(instances, new DataSource(backgrounddir), true);
		}
		loadedinstances = instances;

		userphotocount_map = null;

		// loadedinstances=instances;

		System.out.println("serializing for future use");

		if (usemodel && modeloutputfile != null) {

			try {
				model = ParallelTopicModel.read(modeloutputfile);
				numtopics = model.getNumTopics();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			model.addInstances(instances);
			model.setNumThreads(numthreads);
			model.setNumIterations(iternumnum);
			try {
				model.estimate();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (modeloutputfile != null)
				model.write(this.modeloutputfile);
		}
		generateLabels(model, numtopics);

		Hashtable<Integer, Topic> ret = generateLabels(model, numtopics);

		return ret;
	}

	private InstanceList fetchInstances(InstanceList instances, File inputdir, boolean onlyinstances)
			throws FlowerException {

		try {
			DataSource dataset = new DataSource(inputdir);
			System.out.println("Loading docs into instances");

			while (dataset.hasNext()) {
				DataRow rs = dataset.getRow();
				String text = rs.getText();
				if (text == "" || numOfWords(text) < 3)
					continue;
				String key = rs.getCategory();
				String[] categories = key.trim().split(",");

				for (String k : categories) {
					if (!onlyinstances) {
						ArrayList<DocMI> t = docMap.get(k);
						if (t == null) {
							t = new ArrayList<DocMI>();
						}

						t.add(new DocMI("" + rs.getDocid(), text, rs.getDocstrid()));
						docMap.put(k, t);
						Integer c = idCatCount.get("" + rs.getDocid());
						if (c == null)
							c = 0;
						idCatCount.put("" + rs.getDocid(), ++c);
					}
					String str = rs.getDocid() + " news " + text;

					Reader fileReader = new InputStreamReader(new ByteArrayInputStream(str.getBytes("UTF-8")), "UTF-8");
					int curidx = instances.size();
					instansids.put(curidx, rs.getDocstrid());
					instances.addThruPipe(
							new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data,
																														// label,
																														// name
																														// fields
				}

			}
			// System.out.println(comp+","+rec+","+talk);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FowerReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (docMap.keySet().size() < 2) {
			ArrayList<String> cats = new ArrayList<>();
			for (String s : docMap.keySet()) {
				cats.add("category: " + s + ", count: " + docMap.get(s).size() + "\n");
			}

			throw new FlowerException(
					"The application can not run with a single category in your source table: \n" + cats);
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

		AllPairsDJ dj1 = new AllPairsDJ(collection, error, confidentiality, similarityComparator);
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

		AllPairsDJ dj1 = new AllPairsDJ(collection, error, confidentiality, similarityComparator);
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

	public String printModel() {

		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		double[] topicDistribution = model.getTopicProbabilities(0);
		Alphabet dataAlphabet = model.getAlphabet();
		// Show top 5 words in topics with proportions for the first document
		StringBuilder sb = new StringBuilder();
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
			sb.append(out.toString() + "\n");

		}
		return sb.toString();
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

		model.write(modeloutputfile);
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
