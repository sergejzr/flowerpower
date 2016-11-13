package de.l3s.nlp;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class LemmatizeThread implements Runnable {

	private Hashtable<Integer, String> origidx = new Hashtable<Integer, String>();
	private Hashtable<Integer, String> lemmatizedidx = new Hashtable<Integer, String>();
	private Stack<Integer> towork = new Stack<Integer>();
	de.l3s.nlp.Lemmatizer lem;
	private String langtoken;
	private String[] poss;
	boolean stemm;
	HashSet<String> stopwords;
	public static HashSet<String> garbage = new HashSet<>();

	public LemmatizeThread(Hashtable<Integer, String> origidx, Stack<Integer> towork, String langtoken, String[] poss,
			boolean stemm, String[] stopwords) {
		this.poss = poss;
		this.origidx = origidx;

		this.towork = towork;
		lem = new Lemmatizer();
		lem.loadParser("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		this.langtoken = langtoken;
		this.stemm = stemm;
		if (stopwords == null) {
			this.stopwords = null;
		} else {
			this.stopwords=new HashSet<>();
			this.stopwords.addAll(Arrays.asList(stopwords));
		}
	}

	public Hashtable<Integer, String> getLemmatizedidx() {
		return lemmatizedidx;
	};

	int donrcnt = 5;
	Date start = new Date();

	@Override
	public void run() {
		while (!towork.empty()) {

			Integer task = null;
			String txt = null;
			synchronized (towork) {

				try {
					task = towork.pop();
				} catch (Exception e) {
					continue;
				}

				if (task == null) {
					continue;
				}

				long dif = (new Date()).getTime() - start.getTime();
				if (dif > 1000 * 60 * 15) {
					if (donrcnt > -1) {
						double diffcnt = donrcnt - towork.size();

						System.out.println(start + " Speed: " + (diffcnt / (dif / 1000 / 60)) + " entries pers minute");
					}
					System.out.println(" Still to do " + towork.size() + " ");
					start = new Date();
				}

				txt = origidx.get(task);
				if (txt == null) {
					System.out.println("txt=null");
				}

				if (lemmatizedidx == null) {
					System.out.println("lemmatizedidx=null");
				}
			}

			String lemmatized = getNouns(txt, lem, langtoken);
			if (lemmatized == null) {
				System.out.println("lemmatized=null");
			}
			lemmatizedidx.put(task, lemmatized);

		}

	}

	String getNouns(String s, de.l3s.nlp.Lemmatizer mylem, String langtoken) {

		StringBuilder sb = new StringBuilder();
		
		ArrayList<Lemma> res=new ArrayList<>();
		for(String st:s.split("\\."))
		{
			Vector<Lemma> res2 = mylem.getLemma(st, langtoken);
			if(res2!=null)
			{
				res.addAll(res2);
			}
		}
		

		if (res.isEmpty())
			return "";
		for (Lemma k : res) {
			if (shouldTake(k))

			{
				String w;
				w = k.getLem();
				if (w == null || w.trim().length() == 0) {
					w = k.getWord();
				}
				if (w.length() < 3) {
					// garbage.add(w);
					continue;
				}
				if (stopwords != null) {
					if (stopwords.contains(w)) {
						continue;
					}
				}
				sb.append(w);
				sb.append(" ");
			}

		}

		// return new String(sb).trim().toLowerCase();
		return removeStopps(sb.toString().trim(), langtoken);

		// return removeStopps(s, langtoken);
	}

	String removeStopps(String text, String language) {
		StringBuffer sb = new StringBuffer();
		if (text != null && text.trim().length() > 0) {
			StringReader reader = new StringReader(text);
			TokenStream result = new StandardTokenizer(Version.LUCENE_35, reader);

			result = new StandardFilter(Version.LUCENE_35, result);
			result = new LowerCaseFilter(Version.LUCENE_35, result);
			if (stemm) {
				result = new StopFilter(Version.LUCENE_35, result, EnglishAnalyzer.getDefaultStopSet());
			}

			// result = new DictionaryCompoundWordTokenFilter(Version.LUCENE_35,
			// result, EnglishAnalyzer.);

			CharTermAttribute term = result.addAttribute(CharTermAttribute.class);

			try {
				while (result.incrementToken()) {
					sb.append(term.toString());
					sb.append(" ");
				}
			} catch (IOException ioe) {
				System.out.println("Error: " + ioe.getMessage());
			}
		}
		return sb.toString();
	}

	private boolean shouldTake(Lemma k) {

		for (String pos : poss) {
			if (k.getTag().startsWith(pos) // || k.getTag().startsWith("V")
			)
				return true;
		}
		return false;
	}

	public static void main(String[] args) {

		de.l3s.nlp.Lemmatizer mylem = new Lemmatizer();
		List<Lemma> lemma = mylem.getLemma(
				" Anyone around know where a good place to get hair colored at?  Mod Salon!  Kate and Co! Get Kelsey to do it What are the price ranges?  My place  Walmart you can do it yourself unless you want to pay something crazy Depends on how long your hair is and what you want done, if you look on Mods website it will give you an idea  Capri! Ask for an advanced student. Good prices with wonderful results  College hill barbers, ask for jenny  Posh on university-Cedar Loo area",
				"en");

		for (Lemma lem : lemma) {
			String tag = lem.getTag();
			String word = lem.getWord();
			String lemmat = lem.getLem();

			System.out.println(tag + " " + word + " " + lemmat);

		}
	}
}
