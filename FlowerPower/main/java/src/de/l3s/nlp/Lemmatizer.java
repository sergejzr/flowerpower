package de.l3s.nlp;

import java.util.List;



public class Lemmatizer {

	public List<Lemma> getLemma(String s, String langtoken) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	public Vector<Lemma> getLemma(String s, String lang) {
		
		Vector<Lemma> res=new Vector<Lemma>();
		if(lang.equals("de")){ res = makeGerman(s); }else if(lang.equals("en")){
			res=makeEnglish(s);
		}
		return res;
	}
	private  Vector<Lemma> makeGerman(String text) {
		return parseWith("models/german-dewac.tagger",text);
		
	}

	private  Vector<Lemma> makeEnglish(String text) {
		return parseWith(MaxentTagger.DEFAULT_DISTRIBUTION_PATH,text);
	}

	Hashtable<String, MaxentTagger> models=new Hashtable<String, MaxentTagger>();
	private  Vector<Lemma> parseWith(String model, String text) {
		Vector<Lemma> ret=new Vector<Lemma>();
		
		MaxentTagger mt=models.get(model);
		if(mt==null)
		{
			models.put(model, mt=new MaxentTagger(model));
		}
		String output = mt.tagString(text);
		Morphology m=new Morphology();
		String patternCapitalCaption = "(\\p{L}+)_([A-Za-z]+)";
		Pattern pattern = Pattern.compile(patternCapitalCaption);

		Matcher matcher = pattern.matcher(output);

		while (matcher.find()) 
		{
		String	word=matcher.group(1); String tag=matcher.group(2);
		String lem = m.lemma(word, tag);
		ret.add(new Lemma(word,tag,lem));
		//System.out.println(word+"("+tag+")="+lem);
		}
		
		return ret;
	}*/
}
