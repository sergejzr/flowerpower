package analyze;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
//import oracle.jdbc.dbaccess.*;
//import oracle.jdbc.driver.*;
//import oracle.sql.*;
import de.l3s.db.DB;
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
public class MIselectionFlickr {

	public enum listtype {
		LIST_POSITIVE, LIST_NEGATIVE
	}
	public enum emotion {
		ANGER(0),JOY(1),SADNESS(2),ANTICIPATION(3),SURPRISE(4),TRUST(5),DISGUST(6),FEAR(7);
		
		private final int num;
		
		private emotion(int n) {
			num=n;
		}

		public int getNum() {
			return num;
		}
		
	}
	
	public static HashMap<String, Integer> userphotocount_map= new HashMap<String, Integer>();
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
	public MIselectionFlickr(ArrayList aPositiveTrainList,
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

			MIselectionFlickr mi = new MIselectionFlickr(positiveList, negativeList);
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

		Hashtable<listtype, ArrayList<Terms>> lists = MIselectionFlickr.getTagLists(
				number, con, sql);

		ArrayList<Terms> positiveList = lists.get(listtype.LIST_POSITIVE);
		ArrayList<Terms> negativeList = lists.get(listtype.LIST_NEGATIVE);

		MIselectionFlickr mi = new MIselectionFlickr(positiveList, negativeList);
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

		Hashtable<listtype, ArrayList<Terms>> lists = MIselectionFlickr.getTagLists(
				number, con, sql);

		ArrayList<Terms> positiveList = lists.get(listtype.LIST_POSITIVE);
		ArrayList<Terms> negativeList = lists.get(listtype.LIST_NEGATIVE);

		MIselectionFlickr mi = new MIselectionFlickr(positiveList, negativeList);
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
	public static boolean canPhotoBeChosen(String userid, String tags)
	{
		if(userphotocount_map.get(userid)==null)
		{
			userphotocount_map.put(userid, 0);
		}
		if(tags.split(" ").length<35 && userphotocount_map.get(userid)<50)
		{
			int t=userphotocount_map.get(userid);
			t++;
			userphotocount_map.put(userid, t);
			return true;
		}
		else {
			return false;
		}
		
		
		
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
	
	public static ArrayList<String> fetchFromDB(String emo, int limit)
	{
		ArrayList<String> results= new ArrayList<String>();
		int userphotocount=0;
		int tagcount=0;
		int records_matching_critera=0;
		//50 photos per user and no photo can have more than 35 tags
		try {
			Connection dbcon = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
			
			PreparedStatement pstmt= dbcon.prepareStatement("SELECT `photoid`,`tags`,`owner` FROM `emotag_topics_userordered` WHERE `emotion`=? LIMIT ?");
			pstmt.setString(1, emo);
			pstmt.setInt(2, limit);
			ResultSet rs = pstmt.executeQuery();
			 
	        System.out.println("Loading docs into instances");
	        String prevuser="";
	        while(rs.next())
			{
	        	if(rs.getString(3).equals(prevuser) && userphotocount>50)
	        		continue;
	        	tagcount=rs.getString(2).split(" ").length;
	        	String str= new String();
	        	if(rs.getString(3).equals(prevuser))
	        	{
	        		if(tagcount>35)
		        		continue;
	        		str=rs.getString(1)+" "+"flickr "+rs.getString(2);
		        	Reader fileReader = new InputStreamReader(new ByteArrayInputStream(str.getBytes()),"UTF-8");
		        	results.add(str);
		        	records_matching_critera++;
	        	}
	        	else
	        	{
	        		prevuser=rs.getString(3);
	        		userphotocount=0;
	        			
	        		if(tagcount<35)
	        		{
	        			str=rs.getString(1)+" "+"flickr "+rs.getString(2);
			        	Reader fileReader = new InputStreamReader(new ByteArrayInputStream(str.getBytes()),"UTF-8");
			        	results.add(str);
	        			userphotocount++;
	        			records_matching_critera++;
	        		}
	        		
	        	}
	        	
			}
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
		System.out.println("Number of photos matching the criteria out of "+limit+": "+records_matching_critera);
		return results;
		

	}
	
	static ArrayList<String> getTagsByEmotion(String emo, int limit)
	{
		Connection dbcon;
		ArrayList<String> result= new ArrayList<String>();
		try {
			
			 dbcon = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
			
			PreparedStatement pstmt= dbcon.prepareStatement("SELECT `photoid`,`tags`,`owner` FROM `emotag_topics` WHERE `emotion`=? ORDER BY RAND() LIMIT ?");
			pstmt.setString(1, emo);
			pstmt.setInt(2, limit*2);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next())
			{
				String str = rs.getString(2);
				if(canPhotoBeChosen(rs.getString(1), str))
					result.add(str);
				if(result.size()>limit)
					break;
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
		
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
	public static HashMap<String, String> readLabelsFromFile(String path)
	{
		File f= new File(path);
		HashMap<String, String> labels= new HashMap<String, String>();
		try {
			FileReader fr= new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				if(line.equals(""))
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
	/**
	 * main-Methode zum Testen
	 */
	public static void main(String[] args) {
		Vector<Document> collection = new Vector<Document>();//used for computing jaccard
		double error = .005, confidentiality = .95;
		
		JaccardSimilarityComparator similarityComparator = new JaccardSimilarityComparator();
		
		
		ArrayList<Terms> positiveList = new ArrayList<Terms>();
		ArrayList<Terms> negativeList = new ArrayList<Terms>();
		HashMap<String, String> labels = readLabelsFromFile("C:\\Users\\singh\\Desktop\\labelset.txt");
		
		HashMap<Integer, ArrayList<String>> phototagsmap= new HashMap<Integer, ArrayList<String>>();
		HashMap<Integer, ArrayList<String>> emomap= new HashMap<Integer, ArrayList<String>>();
		ArrayList<String> angerlist= new ArrayList<String>();
		angerlist= getTagsByEmotion("anger", 5000);
		phototagsmap.put(emotion.ANGER.num, angerlist);
		
		ArrayList<String> surpriselist= new ArrayList<String>();
		surpriselist= getTagsByEmotion("surprise", 5000);
		phototagsmap.put(emotion.SURPRISE.num, surpriselist);
		
		ArrayList<String> disgustlist= new ArrayList<String>();
		disgustlist= getTagsByEmotion("disgust", 5000);
		phototagsmap.put(emotion.DISGUST.num, disgustlist);
		
		ArrayList<String> anticipationlist= new ArrayList<String>();
		anticipationlist= getTagsByEmotion("anticipation", 5000);
		phototagsmap.put(emotion.ANTICIPATION.num, anticipationlist);
		
		ArrayList<String> fearlist= new ArrayList<String>();
		fearlist= getTagsByEmotion("fear", 5000);
		phototagsmap.put(emotion.FEAR.num, fearlist);
		
		ArrayList<String> joylist= new ArrayList<String>();
		joylist= getTagsByEmotion("joy", 5000);
		phototagsmap.put(emotion.JOY.num, joylist);
		
		ArrayList<String> sadnesslist= new ArrayList<String>();
		sadnesslist= getTagsByEmotion("sadness", 5000);
		phototagsmap.put(emotion.SADNESS.num, sadnesslist);
		
		ArrayList<String> trustlist= new ArrayList<String>();
		trustlist= getTagsByEmotion("trust", 5000);
		phototagsmap.put(emotion.TRUST.num, trustlist);
		
		userphotocount_map=null;
		
		InstanceList instances =null;
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File("C:\\Users\\singh\\Downloads\\mallet-2.0.7\\mallet-2.0.7\\stoplists\\en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );
        instances = new InstanceList (new SerialPipes(pipeList));
	
		
			
	       
	        int numtopics=200;
	      
	        ParallelTopicModel model=null;
			try {
				System.out.println("reading model from memory");
				//model = ParallelTopicModel.read(new File("C:\\Users\\singh\\FacebookIndex\\"+"topicmodel1.txt"));
				model=ParallelTopicModel.read(new File("C:\\Users\\singh\\flickrmodel"+numtopics+".dat"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
			 double[] topicDistribution = model.getTopicProbabilities(0);
			 Alphabet dataAlphabet = model.getAlphabet();
	        // Show top 5 words in topics with proportions for the first document
	        for (int topic = 0; topic < numtopics; topic++) {
	            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
	            
	            Formatter out = new Formatter(new StringBuilder(), Locale.US);
	            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
	            int rank = 0;
	            while (iterator.hasNext() && rank < 20) {
	                IDSorter idCountPair = iterator.next();
	                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
	                rank++;
	            }
	            System.out.println(out);
	        }
			
			for (emotion i : emotion.values()) 
			{
				for (String temp : phototagsmap.get(i.num))
				{
					
					InstanceList taginstance = new InstanceList(instances.getPipe());
					taginstance.addThruPipe(new Instance(temp, null, "test instance", null));
					double[] distribution = model.getInferencer().getSampledDistribution(taginstance.get(0), 0, 1, 5);
					int[] indlist = getHighestIndexes(distribution, 5);
					double t1 = distribution[indlist[0]];
			        double t2 = distribution[indlist[1]];
			        double t3 = distribution[indlist[2]];
			       
			        String str= new String();
			       /* for(int k=0; k<t1*100; k++)
			        {
			        	str+="t"+indlist[0]+" ";
			        }
			        for(int k=0; k<t2*100; k++)
			        {
			        	str+="t"+indlist[1]+" ";
			        }
			        for(int k=0; k<t3*100; k++)
			        {
			        	str+="t"+indlist[2]+" ";
			        }
			        str.trim();*/
					
			       str="t"+indlist[0]+" t"+indlist[1]+" t"+indlist[2]+" t"+indlist[3]+" t"+indlist[4];
			   
			        if(emomap.get(i.num)==null)
			        {
			        	ArrayList<String> temparray= new ArrayList<String>();
			        	temparray.add(str);
			        	emomap.put(i.num, temparray);
			        }
			        else
			        {
			        	ArrayList<String> temparray = emomap.get(i.num);
			        	temparray.add(str);
			        	emomap.remove(i.num);
			        	emomap.put(i.num, temparray);
			        }
				}
			}
			 
			for (emotion i : emotion.values()) 
			{
				positiveList= new ArrayList<Terms>();
				Document d= new Document();
				ArrayList<String> list= emomap.get(i.num);
				 ArrayList<TermPos> photo1pos = new ArrayList<TermPos>();
	    		   for(String s: list)
	    		   {
	    			   d.add(s);
	    			   String topicids = s.replace("t", "");
	    			   for(String topic: topicids.split(" "))
	    				   photo1pos.add(new TermPos(labels.get(topic),0));
	    			   positiveList.add(new Terms(photo1pos));
	    			   
	    		   }
	    		  
	    		   
	    		  
				for (emotion j : emotion.values()) 
				{
					
					if(j==i)
					continue;
					 photo1pos= new ArrayList<TermPos>();
					 negativeList= new ArrayList<Terms>();
					Document t= new Document();
					ArrayList<String> tlist= emomap.get(j.num);
		    		   
		    		   for(String s: tlist)
		    		   {
		    			   t.add(s);
		    			   String topicids = s.replace("t", "");
	    				   for(String topic: topicids.split(" "))
	    				   photo1pos.add(new TermPos(labels.get(topic),0));
	    				   
	    				   negativeList.add(new Terms(photo1pos));
	    				  
		    		   }
		    		  
		    		   	collection= new Vector<Document>();
		    		   	collection.add(d);
		    		   	collection.add(t);
						Diversity dj1 = new AllPairsDJ(collection, error, confidentiality,similarityComparator);
						System.out.println("RDJ "+i.toString()+"-"+j.toString()+":" + dj1.getRDJ());
						MIselection mi = new MIselection(positiveList, negativeList);
						System.out.println("positive list:"+positiveList.size()); 
				  		 System.out.println("negative list:"+negativeList.size()); 
				  		 mi.computePositiveAndNegativeMIvalues();
				  		System.out.println("mi.getTopNposResults(30) "+i.toString()+":"
								+ mi.getTopNposResultList(30));
				  		 System.out.println("mi.getTopNnegResults(30) "+i.toString()+":"
									+ mi.getTopNnegResultList(150));
					
				}
				
			}
			
		  System.out.println("converting to pos and neg");
			for (emotion i : emotion.values()) 
			{
				System.out.println(i.toString());
				System.out.println("example tags for a few photos in this set:");
	    		System.out.println(phototagsmap.get(i.num).get(0));
	    		System.out.println(phototagsmap.get(i.num).get(1));
	    		System.out.println(phototagsmap.get(i.num).get(2));
				for (int topic = 0; topic < 8; topic++)
				{
		    		   ArrayList<String> tlist= emomap.get(topic);
		    		   
		    		   for(String s: tlist)
		    		   {
		    			  
		    			   ArrayList<TermPos> photo1pos = new ArrayList<TermPos>();
		    			   String[] temp = s.split(" ");
		    			   for(String g: temp)
		    			   {
		    				   if(g==" " || g=="")
		    					   continue;
		    				   g.trim();
		    				   String topicid = g.replace("t", "");
		    				   
		    				   photo1pos.add(new TermPos(labels.get(topicid),0));
		    			   }
		    			   if(topic==i.num)
			                	 positiveList.add(new Terms(photo1pos));
			                 else
			                 {
			                	 negativeList.add(new Terms(photo1pos));
			                 }
				           
		    		   }
		    		   
			        }
				
				
				
	  		 System.out.println("positive list:"+positiveList.size()); 
	  		 System.out.println("negative list:"+negativeList.size()); 
	  		 MIselection mi = new MIselection(positiveList, negativeList);
				
	  		 mi.computePositiveAndNegativeMIvalues();
	  		 System.out.println();
	  		 System.out.println("MI");	
	  		 System.out.println("mi.getTopNposResults(30) "+i.toString()+":"
					+ mi.getTopNposResultList(30));
			ArrayList<?> tail;
			Collections.reverse(tail=mi.getTopNposResultList());
			
			 System.out.println("mi.getPosTail"+top(tail));
			 
	  		 System.out.println("mi.getTopNnegResults(30) "+i.toString()+":"
					+ mi.getTopNnegResultList(30));
	  		 
	  		 
	  		Collections.reverse(tail=mi.getTopNnegResultList());
			
			 System.out.println("mi.getNegTail"+top(tail));
			 
	  		 positiveList.clear();
	  		 negativeList.clear();
			System.out.println();	
			
		}
	
		        
	}
	private static List<?> top(ArrayList list) {
		ArrayList<Object> top=new ArrayList<Object>();
		
		int cnt=30;
		for(Object o:list)
		{
			if(cnt--<0) return top;
			top.add(o);
			
		}
		return top;
		
	}
	


}
