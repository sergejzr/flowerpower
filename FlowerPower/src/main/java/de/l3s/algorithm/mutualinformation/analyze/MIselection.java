package de.l3s.algorithm.mutualinformation.analyze;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.mallet.pipe.Pipe;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
//import oracle.jdbc.dbaccess.*;
//import oracle.jdbc.driver.*;
//import oracle.sql.*;



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
public class MIselection {

	public enum listtype {
		LIST_POSITIVE, LIST_NEGATIVE
	}

	private ArrayList positiveTrainList;
	private ArrayList negativeTrainList;

	private ArrayList positiveTermLists;
	private ArrayList negativeTermLists;

	private HashMap mapA;
	private HashMap mapB;
	// private HashMap posTerms;
	// private HashMap negTerms;
	private ArrayList posListMI;
	private ArrayList negListMI;
	private int numberOfDocs;
	private int posNumber;
	private int negNumber;
	private ArrayList posResultList;
	private ArrayList negResultList;

	/**
	 * Konstruktor
	 * 
	 * @param aPositiveTrainList
	 *            positive Beispiele faer Dokumente als "Terms"-Objecte
	 * @param aNegativeTrainList
	 *            negative Beispiele faer Dokumente als "Terms"-Objecte
	 */
	public MIselection(ArrayList aPositiveTrainList,
			ArrayList aNegativeTrainList) {
		positiveTrainList = aPositiveTrainList;
		negativeTrainList = aNegativeTrainList;
		posNumber = positiveTrainList.size();
		negNumber = negativeTrainList.size();
		numberOfDocs = posNumber + negNumber;
	}
	public ArrayList getTopNposResultList()
	{
		ArrayList resultSet = new ArrayList();
		int i = 0;
		for (ListIterator iter = posResultList.listIterator(); iter.hasNext()
				; i++) {
			TermMI tmi = (TermMI) iter.next();
			resultSet.add(tmi.term);
		}
		return resultSet;
	}	
	
	public ArrayList getTopNnegResultList()
	{
		ArrayList resultSet = new ArrayList();
		int i = 0;
		for (ListIterator iter = negResultList.listIterator(); iter.hasNext()
				; i++) {
			TermMI tmi = (TermMI) iter.next();
			resultSet.add(tmi.term);
		}
		return resultSet;
	}
	
	public ArrayList getTopNposResultList(int n) {
		ArrayList resultSet = new ArrayList();
		int i = 0;
		for (ListIterator iter = posResultList.listIterator(); iter.hasNext()
				&& i < n; i++) {
			TermMI tmi = (TermMI) iter.next();
			resultSet.add(tmi.term);
		}
		return resultSet;
	}

	public ArrayList getTopNnegResultList(int n) {
		ArrayList resultSet = new ArrayList();
		int i = 0;
		for (ListIterator iter = negResultList.listIterator(); iter.hasNext()
				&& i < n; i++) {
			TermMI tmi = (TermMI) iter.next();
			resultSet.add(tmi.term);
		}
		return resultSet;
	}

	public HashSet getTopNposResults(int n) {
		HashSet resultSet = new HashSet();
		int i = 0;
		for (ListIterator iter = posResultList.listIterator(); iter.hasNext()
				&& i < n; i++) {
			TermMI tmi = (TermMI) iter.next();
			resultSet.add(tmi.term);
		}
		return resultSet;
	}

	public HashSet getTopNnegResults(int n) {
		HashSet resultSet = new HashSet();
		int i = 0;
		for (ListIterator iter = negResultList.listIterator(); iter.hasNext()
				&& i < n; i++) {
			TermMI tmi = (TermMI) iter.next();
			resultSet.add(tmi.term);
		}
		return resultSet;
	}

	public void computePositiveAndNegativeMIvalues() {
		buildTermLists();
		scanTermLists();
		computeValues();
	}

	private void buildTermLists() {
		positiveTermLists = new ArrayList();
		negativeTermLists = new ArrayList();
		for (int i = 0; i < positiveTrainList.size(); i++) {
			ArrayList termList = new ArrayList();
			HashSet termSet = new HashSet();
			Terms terms = (Terms) positiveTrainList.get(i);
			ArrayList tpl = terms.getTermPosList();
			for (int j = 0; j < tpl.size(); j++) {
				TermPos tPos = (TermPos) tpl.get(j);
				String term = tPos.getTerm();
				termSet.add(term);
			}
			termList.addAll(termSet);
			positiveTermLists.addAll(termList);
		}
		for (int i = 0; i < negativeTrainList.size(); i++) {
			ArrayList termList = new ArrayList();
			HashSet termSet = new HashSet();
			Terms terms = (Terms) negativeTrainList.get(i);
			ArrayList tpl = terms.getTermPosList();
			for (int j = 0; j < tpl.size(); j++) {
				TermPos tPos = (TermPos) tpl.get(j);
				String term = tPos.getTerm();
				termSet.add(term);
			}
			termList.addAll(termSet);
			negativeTermLists.addAll(termList);
		}
	}

	private void scanTermLists() {
		mapA = new HashMap();
		mapB = new HashMap();
		// System.out.println(positiveTermLists.size());
		// System.out.println(negativeTermLists.size());
		for (int i = 0; i < positiveTermLists.size(); i++) {
			String term = (String) positiveTermLists.get(i);
			// System.out.println(term);
			if (mapA.containsKey(term)) {
				int value = ((Integer) mapA.get(term)).intValue();
				value++;
				mapA.put(term, new Integer(value));
			} else {
				mapA.put(term, new Integer(1));
			}
		}
		for (int i = 0; i < negativeTermLists.size(); i++) {
			String term = (String) negativeTermLists.get(i);
			// System.out.println(term);
			if (mapB.containsKey(term)) {
				int value = ((Integer) mapB.get(term)).intValue();
				value++;
				mapB.put(term, new Integer(value));
			} else {
				mapB.put(term, new Integer(1));
			}
		}
		// System.out.println(mapA.size());
		// System.out.println(mapB.size());
	}

	private void computeValues() {
		posResultList = new ArrayList();
		negResultList = new ArrayList();
		// posTerms = new HashMap();
		// negTerms = new HashMap();
		HashSet allTerms = new HashSet();
		allTerms.addAll(mapA.keySet());
		allTerms.addAll(mapB.keySet());
		// System.out.println(allTerms.size());
		ArrayList allTermsList = new ArrayList();
		allTermsList.addAll(allTerms);
		for (int i = 0; i < allTermsList.size(); i++) {
			String term = (String) allTermsList.get(i);
			double a = 0;
			if (mapA.containsKey(term)) {
				a = ((Integer) mapA.get(term)).doubleValue();
			}
			double b = 0;
			if (mapB.containsKey(term)) {
				b = ((Integer) mapB.get(term)).doubleValue();
			}
			double n = (double) numberOfDocs;
			double c = (double) posNumber - a;
			double d = (double) negNumber - b;
			// posTerms.put(term, new Double(a/n *
			// Math.log(a*n/((a+b)+(a+c)))));
			// negTerms.put(term, new Double(b/n *
			// Math.log(b*n/((b+a)+(b+d)))));
			if (a != 0) {
				posResultList.add(new TermMI(term, (a / n)
						* Math.log(a * n / ((a + b) * (a + c))) / Math.log(2)));
			}
			if (b != 0) {
				negResultList.add(new TermMI(term, (b / n)
						* Math.log(b * n / ((b + a) * (b + d))) / Math.log(2)));

			}
		}
		Collections.sort(posResultList);
		Collections.sort(negResultList);

	}

	// Tupel aus Termen und MI, Wert
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


	private Logger log() {

		return LoggerFactory.getLogger(this.getClass());
	}
	public static Hashtable<listtype, Collection<String>> selectFeatures(
			int number, Connection con, String sql, String[] stopwords) {
		Hashtable<listtype, Collection<String>> ret = new Hashtable<listtype, Collection<String>>();

		Hashtable<String, String> stopwordstable = new Hashtable<String, String>();
		for (String stopWord : stopwords) {
			stopwordstable.put(stopWord, "");
		}

		Hashtable<listtype, ArrayList<Terms>> lists = MIselection.getTagLists(
				number, con, sql);

		ArrayList<Terms> positiveList = lists.get(listtype.LIST_POSITIVE);
		ArrayList<Terms> negativeList = lists.get(listtype.LIST_NEGATIVE);

		MIselection mi = new MIselection(positiveList, negativeList);
		mi.computePositiveAndNegativeMIvalues();
		ret.put(listtype.LIST_POSITIVE, mi.getTopNposResults(number));
		ret.put(listtype.LIST_NEGATIVE, mi.getTopNnegResults(number));
		return ret;
		/*
		 * System.out.println("mi.getTopNposResults(20) "+mi.getTopNposResults(20
		 * ));
		 * System.out.println("mi.getTopNnegResults(20) "+mi.getTopNnegResults
		 * (20));
		 */
	}

	public static Hashtable<listtype, Collection<String>> selectFeatureLists(
			int number, Connection con, String sql, String[] stopwords) {
		// Hashtable<String, HashSet<String>> ret = new Hashtable<String,
		// HashSet<String>>();
		Hashtable<listtype, Collection<String>> ret = new Hashtable<listtype, Collection<String>>();

		Hashtable<listtype, ArrayList<Terms>> lists = MIselection.getTagLists(
				number, con, sql);

		ArrayList<Terms> positiveList = lists.get(listtype.LIST_POSITIVE);
		ArrayList<Terms> negativeList = lists.get(listtype.LIST_NEGATIVE);

		MIselection mi = new MIselection(positiveList, negativeList);
		mi.computePositiveAndNegativeMIvalues();

		ret.put(listtype.LIST_POSITIVE, mi.getTopNposResultList(number));
		ret.put(listtype.LIST_NEGATIVE, mi.getTopNnegResultList(number));
		return ret;
		/*
		 * System.out.println("mi.getTopNposResults(20) "+mi.getTopNposResults(20
		 * ));
		 * System.out.println("mi.getTopNnegResults(20) "+mi.getTopNnegResults
		 * (20));
		 */
	}

	/**
	 * SQL has to be of the form: SELECT a as id, b as tags, c as title, d as
	 * description, e as lable lable can be {private,public,undecidable}
	 * 
	 * @param number
	 * 
	 * @param con
	 * @param stopwordstable
	 * @return
	 */
	static Hashtable<listtype, ArrayList<Terms>> getTagLists(int number,
			Connection con, String sql) {
		Hashtable<listtype, ArrayList<Terms>> ret = new Hashtable<listtype, ArrayList<Terms>>();
		ArrayList<Terms> positiveList = new ArrayList<Terms>();
		ArrayList<Terms> negativeList = new ArrayList<Terms>();
		ret.put(listtype.LIST_POSITIVE, positiveList);
		ret.put(listtype.LIST_NEGATIVE, negativeList);
		int fcount = 0;
		try {
			if (con == null) {
				con = null;// getyourconnection
			}
			if (sql == null)
				sql = " SELECT photoid, value, lable FROM XXXXX";// TODO: your
																	// table
			PreparedStatement st = null;
			ResultSet rs = null;
			int min = 1000000;

			String selecttrain = "SELECT COUNT(*) as cnt,x.lable FROM (" + sql
					+ ") x GROUP BY lable ";

			st = con.prepareStatement(selecttrain);
			rs = st.executeQuery();

			while (rs.next()) {
				int curcnt = rs.getInt("cnt");
				String lable = rs.getString("lable");
				if (lable.compareTo("publc") == 0
						|| lable.compareTo("private") == 0) {
					if (min > curcnt) {
						min = curcnt;
					}
				}
			}
			min = 40000;
			rs.close();
			st.close();

			st = con.prepareStatement(sql);
			System.out.println(sql);
			rs = st.executeQuery();
			ArrayList<Terms> curlist = null;
			Terms terms1pos = null;
			ArrayList<TermPos> photo1pos = null;

			int cntall = 0;
			while (rs.next()) {
				cntall++;

				if (cntall % 1000 == 0) {
					System.out.println("MI got " + cntall);
				}

				String photoid = rs.getString("photoid");

				String lable = rs.getString("lable");

				if (photoid == null) {
					continue;
				}

				// tagstext=

				if (lable.compareTo("public") == 0) {
					if (negativeList.size() >= min) {
						continue;
					}
					curlist = negativeList;
				} else {
					if (lable.compareTo("private") == 0) {
						if (positiveList.size() >= min) {
							continue;
						}
						curlist = positiveList;

					} else {
						continue;
					}
				}

				photo1pos = new ArrayList<TermPos>();

				Vector<String> terms = new Vector<String>();// TODO: get your
															// term list
				// allfeatures.getSimpleTermList();

				for (String term : terms) {

					if (term.length() == 0) {
						// System.out.println("zero found");
						continue;
					}
					if (term.startsWith("childr")) {
						int z = 0;
						z++;
						z++;
					}
					photo1pos.add(new TermPos(term, 0));
				}

				if (photo1pos.size() > 0) {
					terms1pos = new Terms(photo1pos);
					curlist.add(terms1pos);
				}

			}
			rs.close();
			st.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	
	
	public static void addTerms(ArrayList<Terms> curlist, String texte[])
	{

		ArrayList<TermPos> photo1pos = new ArrayList<TermPos>();

		Vector<String> terms = new Vector<String>();
		for(String text:texte)
		{
			terms.add(text);
		}
	
		
		// TODO: get your
													// term list
		// allfeatures.getSimpleTermList();

		for (String term : terms) {

			if (term.length() == 0) {
				// System.out.println("zero found");
				continue;
			}
			
			photo1pos.add(new TermPos(term, 0));
		}

		if (photo1pos.size() > 0) {
			Terms terms1pos = new Terms(photo1pos);
			curlist.add(terms1pos);
		}
	
	}
	
	/**
	 * main-Methode zum Testen
	 */
	public static void main(String[] args) {
		
		
		ArrayList<Terms> positiveList = new ArrayList<Terms>();
		ArrayList<Terms> negativeList = new ArrayList<Terms>();
		
		String postexte[]=new String[]{"sea sun sand", "beach sand", "sand warm sea"};
		String negtexte[]=new String[]{"freezw sun snow", "snow ice", "ice cold freeze"};
		
		
	addTerms(positiveList,postexte);
	addTerms(negativeList,negtexte);
	
		
		
		MIselection mi = new MIselection(positiveList, negativeList);
		
		mi.computePositiveAndNegativeMIvalues();
		
		/*
		System.out.println("mi.getTopNposResults() "
				+ mi.getTopNposResultList());
		System.out.println("mi.getTopNnegResults() "
				+ mi.getTopNnegResultList());
		
		ArrayList temp = mi.getTopNposResultList();
		Collections.reverse(temp);
		ArrayList temp2 = mi.getTopNnegResultList();
		Collections.reverse(temp2);
		System.out.println("rev mi.getTopNposResults() "
				+temp) ;
		System.out.println("rev mi.getTopNnegResults() "
				+ temp2);
		
		*/
		
			
			String posTopic="summer", negTopic="winter";
			
			System.out.println();
		System.out.println("MI");	
		System.out.println("mi.getTopNposResults(30) "+posTopic+":"
				+ mi.getTopNposResultList(30));
		
		
		System.out.println("mi.getTopNnegResults(30) "+negTopic+":"
				+ mi.getTopNnegResultList(30));
		
		
	
		/*
		int numtopics=26;
		HashMap<String, Double> map= new HashMap<String, Double>();
		
		//Texts about summer
		 
			
	        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

	        // Pipes: lowercase, tokenize, remove stopwords, map to features
	       
	        ParallelTopicModel model=null;
			try {
				System.out.println("reading model from memory");
				//model = ParallelTopicModel.read(new File("C:\\Users\\singh\\FacebookIndex\\"+"topicmodel1.txt"));
				model=ParallelTopicModel.read(new File("C:\\Users\\singh\\flickrmodel"+numtopics+".dat"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double[] topicDistribution = model.getTopicProbabilities(0);
			
			Formatter out=null;
			Alphabet dataAlphabet = model.alphabet;
			ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		        
		        // Show top 5 words in topics with proportions for the first document
		        for (int topic = 0; topic < numtopics; topic++) {
		            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
		            
		            out = new Formatter(new StringBuilder(), Locale.US);
		            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
		            int rank = 0;
		          
		            while (iterator.hasNext() && rank < 2000) {
		                IDSorter idCountPair = iterator.next();
		               
		                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
		                map.put((String)dataAlphabet.lookupObject(idCountPair.getID())+topic, idCountPair.getWeight());
		                rank++;
		            }
		            
		            System.out.println(out);
		            
		        }
		        //add each photo to the poslist
		        
		       
		     
		        System.out.println("creating map");
		        
		        HashMap<Integer, ArrayList<String>> topicmap= new HashMap<Integer, ArrayList<String>>();
		        
		    
		        
		        
		       for(int i=0; i<model.getData().size();i++)
		       {
		    	   int winneridx=0;
			        int runnerup=0;
			      
			        double[] distribution = model.getInferencer().getSampledDistribution(model.getData().get(i).instance, 0, 1, 5);

			        for(int j=0;j<distribution.length;j++)
			        {
			        	  
			        	  if(distribution[winneridx]<distribution[j])
			        	  {
			        		  runnerup=winneridx;
			        		  winneridx=j;
			        		 
			        	  }
			        		 
			        }
			        
			        if(topicmap.get(winneridx)==null)
			        {
			        	ArrayList<String> temparray= new ArrayList<String>();
			        	String s = model.getData().get(i).instance.getData().toString().replaceAll("\\P{L}+", " ");
			        	temparray.add(s);
			        	topicmap.put(winneridx, temparray);
			        }
			        else
			        {
			        	ArrayList<String> temparray = topicmap.get(winneridx);
			        	String s = model.getData().get(i).instance.getData().toString().replaceAll("\\P{L}+", " ");
			        	temparray.add(s);
			        	topicmap.remove(winneridx);
			        	topicmap.put(winneridx, temparray);
			        }
			        
		       }
		       model= new ParallelTopicModel(2);
		       System.out.println("converting to pos and neg");
		        
		       for(int posTopic=0; posTopic < numtopics; posTopic++)
		       {
		    	   for (int topic = 0; topic < numtopics; topic++) {
		    		   ArrayList<String> tlist=topicmap.get(topic);
		    		   
		    		   for(String s: tlist)
		    		   {
		    			   ArrayList<TermPos> photo1pos = new ArrayList<TermPos>();
		    			   String[] temp = s.split(" ");
		    			   for(String g: temp)
		    			   {
		    				   if(g==" " || g=="")
		    					   continue;
		    				   photo1pos.add(new TermPos(g,0));
		    			   }
		    			   if(topic==posTopic)
			                	 positiveList.add(new Terms(photo1pos));
			                 else
			                 {
			                	 negativeList.add(new Terms(photo1pos));
			                 }
				           
		    		   }
			            
		                 
			        }
			       

					
		       }*/
		        
	}

}
