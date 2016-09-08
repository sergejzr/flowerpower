package test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.compound.DictionaryCompoundWordTokenFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;

import de.l3s.nlp.Lemma;
import de.l3s.nlp.LemmatizerMaven;

public class StemmThread implements Runnable {

	private Hashtable<Integer, String> origidx = new Hashtable<Integer, String>();
	private Hashtable<Integer, String> lemmatizedidx = new Hashtable<Integer, String>();
	private Stack<Integer> towork = new Stack<Integer>();
	
	private String langtoken;
	

	public StemmThread(Hashtable<Integer, String> origidx,
			Stack<Integer> towork, String langtoken) {

		this.origidx = origidx;

		this.towork = towork;
	
		this.langtoken=langtoken;
	}

	public Hashtable<Integer, String> getLemmatizedidx() {
		return lemmatizedidx;
	};
int donrcnt=5;
Date start=new Date();
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
				
				long dif = (new Date()).getTime()-start.getTime();
				if(dif>1000*60*15)
				{
					if(donrcnt>-1)
					{
						double diffcnt=donrcnt-towork.size();
						
						System.out.println(start+" Speed: "+(diffcnt/(dif/1000/60)) +" entries pers minute");
					}
					System.out.println(" Still to do "+towork.size()+" ");
					start=new Date();
				}
				
				txt = origidx.get(task);
				if (txt == null) {
					System.out.println("txt=null");
				}
				
				if (lemmatizedidx == null) {
					System.out.println("lemmatizedidx=null");
				}
			}

			String lemmatized = getStemmed(txt,langtoken);
			if (lemmatized == null) {
				System.out.println("lemmatized=null");
			}
			lemmatizedidx.put(task, lemmatized);

		}

	}

	


    
    
	public  String getStemmed(String text, String language) {
		
		text=text.toLowerCase();
		StringBuffer sb=new StringBuffer();
		
		for(String s:text.split("\\s+"))
		{
			if(sb.length()>0) sb.append(" ");
			
			if(s.contains("http")) continue;
			if(s.length()<2) continue;
			
			
			sb.append(s);
		}
		
		text=sb.toString();
		
		
		
	     sb = new StringBuffer();
	    if (text!=null && text.trim().length()>0){
	        StringReader reader = new StringReader(text);
	        TokenStream result = new StandardTokenizer(Version.LUCENE_35, reader);

	    	result = new StandardFilter(Version.LUCENE_35, result);
	    	result = new LowerCaseFilter(Version.LUCENE_35, result);
	    	result = new StopFilter(Version.LUCENE_35, result, EnglishAnalyzer.getDefaultStopSet());
	    	//result = new DictionaryCompoundWordTokenFilter(Version.LUCENE_35, result, EnglishAnalyzer.);
	    	result = new SnowballFilter(result, "English");
	        
	        
	        
	    	CharTermAttribute  term=result.addAttribute(CharTermAttribute.class);
	    	


	        try {
	            while (result.incrementToken()){
	            	String t=term.toString().replaceAll(",", "").replaceAll("'", "");
	            	if(t.matches("\\d+")) continue;
	            	if(t.matches("\\?+")) continue;
	            	if(t.length()<2) continue;
	                sb.append(t);
	                sb.append(" ");
	            }
	        } catch (IOException ioe){
	            System.out.println("Error: "+ioe.getMessage());
	        }
	    }
	    String k = sb.toString().trim();
	    
	    if(k.contains("http"))
	    {
	    	int i=0;
	    	i++;
	    }
	    return k;
	}
	

	public static void main(String[] args) {
		
		de.l3s.nlp.LemmatizerMaven mylem=new LemmatizerMaven();
		List<Lemma> lemma = mylem.getLemma(" Anyone around know where a good place to get hair colored at?  Mod Salon!  Kate and Co! Get Kelsey to do it What are the price ranges?  My place  Walmart you can do it yourself unless you want to pay something crazy Depends on how long your hair is and what you want done, if you look on Mods website it will give you an idea  Capri! Ask for an advanced student. Good prices with wonderful results  College hill barbers, ask for jenny  Posh on university-Cedar Loo area", "en");
		
		for(Lemma lem:lemma)
		{
			String tag=lem.getTag();
			String word=lem.getWord();
			String lemmat=lem.getLem();
			
			System.out.println(tag+" "+word+" "+lemmat);
			
		}
	}
}
