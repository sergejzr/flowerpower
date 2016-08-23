package analyze;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.Vector;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import l3s.rdj.Diversity;
import l3s.rdj.Document;
import l3s.rdj.impl.AllPairsDJ;
import l3s.toolbox.JaccardSimilarityComparator;



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
public class JaccardClusterScores {

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
	public JaccardClusterScores(ArrayList aPositiveTrainList,
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

	public static void test1() {

		try {
			// TODO:
			Connection con = null;// getyourconnection
			PreparedStatement st = con
					.prepareStatement("SELECT t.photoid,t.value FROM XXXXXXX");

			ResultSet posset = st.executeQuery();

			ArrayList positiveList = new ArrayList();

			String pid = "";
			Terms terms1pos = null;
			ArrayList photo1pos = null;
			while (posset.next()) {
				String photoid = posset.getString("photoid");

				if (pid.compareTo(photoid) != 0) {
					if (photo1pos != null) {
						positiveList.add(terms1pos);
					}
					pid = photoid;
					photo1pos = new ArrayList();
					terms1pos = new Terms(photo1pos);
				}

				photo1pos.add(new TermPos(posset.getString("value"), 0));
				if (posset.isLast()) {
					positiveList.add(terms1pos);
				}
			}

			ArrayList negativeList = new ArrayList();

			st = con.prepareStatement("SELECT t.photoid,t.value FROM `sample_privacy_info` i JOIN sample_tag t ON (i.photoid=t.photoid) WHERE i.isPrivate='yes' ORDER BY t.photoid ");

			posset = st.executeQuery();

			pid = "";
			terms1pos = null;
			photo1pos = null;
			while (posset.next()) {
				String photoid = posset.getString("photoid");

				if (pid.compareTo(photoid) != 0) {
					if (photo1pos != null) {
						negativeList.add(terms1pos);
					}
					pid = photoid;
					photo1pos = new ArrayList();
					terms1pos = new Terms(photo1pos);
				}

				photo1pos.add(new TermPos(posset.getString("value"), 0));
				if (posset.isLast()) {
					negativeList.add(terms1pos);
				}
			}

			JaccardClusterScores mi = new JaccardClusterScores(positiveList, negativeList);
			mi.computePositiveAndNegativeMIvalues();
			System.out.println("mi.getTopNposResults(20) "
					+ mi.getTopNposResults(20));
			System.out.println("mi.getTopNnegResults(20) "
					+ mi.getTopNnegResults(20));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("ready");
		/*
		 * ArrayList positiveList =
		 * NewsBase.getAllTermsList(NewsBase.getIDList("sci.med", 0 , 1000));
		 * ArrayList negativeList =
		 * NewsBase.getAllTermsList(NewsBase.getIDList("sci.space", 0 , 1000));
		 * System.out.println("Berechnung ...."); MIselection msel = new
		 * MIselection(positiveList, negativeList);
		 * msel.computePositiveAndNegativeMIvalues();
		 * System.out.println(msel.getTopNposResults(10));
		 * System.out.println("***************************************");
		 * System.out.println(msel.getTopNnegResults(10)); for (int i = 0; i <
		 * 100; i++) {
		 * System.out.println(((TermMI)msel.posResultList.get(i)).term + " = " +
		 * ((TermMI)msel.posResultList.get(i)).value); }
		 * System.out.println("================================"); for (int i =
		 * 0; i < 100; i++) {
		 * System.out.println(((TermMI)msel.negResultList.get(i)).term + " = " +
		 * ((TermMI)msel.negResultList.get(i)).value); }
		 */

	}

	public static Hashtable<listtype, Collection<String>> selectFeatures(
			int number, Connection con, String sql, String[] stopwords) {
		Hashtable<listtype, Collection<String>> ret = new Hashtable<listtype, Collection<String>>();

		Hashtable<String, String> stopwordstable = new Hashtable<String, String>();
		for (String stopWord : stopwords) {
			stopwordstable.put(stopWord, "");
		}

		Hashtable<listtype, ArrayList<Terms>> lists = JaccardClusterScores.getTagLists(
				number, con, sql);

		ArrayList<Terms> positiveList = lists.get(listtype.LIST_POSITIVE);
		ArrayList<Terms> negativeList = lists.get(listtype.LIST_NEGATIVE);

		JaccardClusterScores mi = new JaccardClusterScores(positiveList, negativeList);
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

		Hashtable<listtype, ArrayList<Terms>> lists = JaccardClusterScores.getTagLists(
				number, con, sql);

		ArrayList<Terms> positiveList = lists.get(listtype.LIST_POSITIVE);
		ArrayList<Terms> negativeList = lists.get(listtype.LIST_NEGATIVE);

		JaccardClusterScores mi = new JaccardClusterScores(positiveList, negativeList);
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

	/**
	 * main-Methode zum Testen
	 */
	public static void main(String[] args) {
		
		
		ArrayList<Integer> numtopicslist= new ArrayList<Integer>();
		numtopicslist.add(120);
		numtopicslist.add(75);
		numtopicslist.add(100);
		numtopicslist.add(150);
		numtopicslist.add(200);
		numtopicslist.add(250);
		
		String type="allconf";
		
		
		
		
		
		 
		for(Integer i: numtopicslist)
		{
				 Vector<Document> collection = new Vector<Document>();
				
				
		        ParallelTopicModel model=null;
				try {
					//System.out.println("reading model from memory");
					
					model=ParallelTopicModel.read(new File("C:\\Users\\singh\\"+type+"model"+i+".dat"));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("model"+i);
				
				Alphabet dataAlphabet = model.alphabet;
				ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
			   
			        
			        for (int topic = 0; topic < i; topic++)
			        {
			            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

			            int rank = 0;
			            Document d= new Document();
			            while (iterator.hasNext() && rank <50) {
			                IDSorter idCountPair = iterator.next();
			                d.add(dataAlphabet.lookupObject(idCountPair.getID()));
			                
			                rank++;
			            }
			            collection.add(d);
			            //System.out.println(Arrays.toString(d.toArray()));
			            
			            
			        }
			        //System.out.println(collection.size());
			        //System.out.println("Calculating pairwise similarity");
			        double error = .005, confidentiality = .95;
					
			
			    	JaccardSimilarityComparator similarityComparator = new JaccardSimilarityComparator();
					Diversity dj1 = new AllPairsDJ(collection, error, confidentiality,similarityComparator);
				//	System.out.println("RDJ:" + dj1.getRDJ());
					System.out.println(i+"\t" + (""+dj1.getRDJ()).replaceAll("\\.", ","));
//		
//					Diversity dj2 = new SampleDJ(collection, error, confidentiality,similarityComparator);
//					System.out.println("RDJ:" + dj2.getRDJ());
//		
//						//TracjDJ works only with Jaccard similarity measure.
//					Diversity dj3 = new TrackDJ(collection, confidentiality, confidentiality);
//					System.out.println("RDJ:" + dj3.getRDJ());
			        
			        
			       
		}
		
		        System.out.println("Done");
		          
		    
		       
		        
	}

}
