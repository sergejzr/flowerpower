package test;



import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import de.l3s.nlp.Lemma;
import de.l3s.nlp.Lemmatizer;
import de.l3s.nlp.LemmatizerMaven;

public class LemmatizeThread implements Runnable {

	private Hashtable<Integer, String> origidx = new Hashtable<Integer, String>();
	private Hashtable<Integer, String> lemmatizedidx = new Hashtable<Integer, String>();
	private Stack<Integer> towork = new Stack<Integer>();
	de.l3s.nlp.LemmatizerMaven lem;
	private String langtoken;
	private String[] poss;

	public LemmatizeThread(Hashtable<Integer, String> origidx,
			Stack<Integer> towork, String langtoken, String[] poss) {
this.poss=poss;
		this.origidx = origidx;

		this.towork = towork;
		lem = new LemmatizerMaven();
		this.langtoken=langtoken;
	}

	public Hashtable<Integer, String> getLemmatizedidx() {
		return lemmatizedidx;
	};
int donrcnt=-1;
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

			String lemmatized = getNouns(txt, lem,langtoken);
			if (lemmatized == null) {
				System.out.println("lemmatized=null");
			}
			lemmatizedidx.put(task, lemmatized);

		}

	}

	String getNouns(String s, de.l3s.nlp.LemmatizerMaven mylem,String langtoken) {

		StringBuilder sb = new StringBuilder();
		List<Lemma> res = mylem.getLemma(s, langtoken);

		if (res.isEmpty())
			return "";
		for (Lemma k : res) {
			if (shouldTake(k))

			{
				String w;
				w=k.getLem();
if(w==null||w.trim().length()==0)
{
w=k.getWord();
}
				sb.append(w);
				sb.append(" ");
			}

		}

		return new String(sb).trim().toLowerCase();
	}
	private boolean shouldTake(Lemma k) {
		
		for(String pos:poss)
		{
			if(k.getTag().startsWith(pos) //|| k.getTag().startsWith("V")
					) return true;
		}
		return false;
	}

	public static void main(String[] args) {
		
		de.l3s.nlp.Lemmatizer mylem=new Lemmatizer();
		List<Lemma> lemma = mylem.getLemma("Well fuck", "en");
		
		for(Lemma lem:lemma)
		{
			String tag=lem.getTag();
			String word=lem.getWord();
			String lemmat=lem.getLem();
			
			System.out.println(tag+" "+word+" "+lemmat);
			
		}
	}
}

