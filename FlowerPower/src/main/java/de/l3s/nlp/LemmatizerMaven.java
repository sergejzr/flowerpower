package de.l3s.nlp;

import java.io.StringReader;
import java.text.BreakIterator;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;

public class LemmatizerMaven {

	// private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";        

	    private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");

	   // private final LexicalizedParser parser = LexicalizedParser.loadModel(PCG_MODEL);
public void init()
{
	
}
	    public Tree parse(LexicalizedParser parser, String str) { 
	    	

	    	
	    	
	    	
	    	try{
	        List<CoreLabel> tokens = tokenize(str);
	        Tree tree = parser.apply(tokens);
	        return tree;
	    	}catch(Exception e)
	    	{
	    		System.out.println("problematic: "+str);
	    	}
	    	return null;
	    }

	    private List<CoreLabel> tokenize(String str) {
	        Tokenizer<CoreLabel> tokenizer =
	            tokenizerFactory.getTokenizer(
	                new StringReader(str));    
	        return tokenizer.tokenize();
	    }

	    
	public static void main(String[] args) {
		LemmatizerMaven lm=new LemmatizerMaven();
			Vector<Lemma> lemms;
			System.out.println(lemms=lm.getLemma("o I masturbated in class yesterday Yea imma nut on it and feed it to your mother  Don't get fucked up disrespecting my mother like that.. Better feed it to your father.. Oh wait you don't know who that it.. Suck my dick bitch Lol yo mad can't even spell right you in college right? Neither or can you  Que?", "en"));

	for(Lemma l:lemms)
	{
		System.out.print(l.getLem()+"("+l.getTag()+")");
	}
	
	}
	public Vector<Lemma> getLemma(String s, String lang) {
		
		
		
		Vector<Lemma> res=new Vector<Lemma>();
		if(lang.equals("de")){ 
			
			res = makeGerman(s); }else if(
					lang.equals("en")){
			res=makeEnglish(s);
		}
		return res;
	}
	private  Vector<Lemma> makeGerman(String text) {
		return parseWith("edu/stanford/nlp/models/lexparser/germanPCFG.ser.gz",text);
		
	}

	private  Vector<Lemma> makeEnglish(String text) {
		return parseWith("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",text);
	}

	 Hashtable<String, LexicalizedParser> models=new Hashtable<String, LexicalizedParser>();
	
	private  Vector<Lemma> parseWith(String model, String text) {
		Vector<Lemma> ret=new Vector<Lemma>();
		if(text.trim().length()==0) return ret;
		LexicalizedParser mt=loadParser(model);
		 StringBuilder sb=new StringBuilder();
    	BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
    	String source = text+"";
    	iterator.setText(source);
    	int start = iterator.first();
    	for (int end = iterator.next();
    	    end != BreakIterator.DONE;
    	    start = end, end = iterator.next()) {
    	  text=source.substring(start,end);
    	
		
		
		 Tree tree = parse(mt,text);  

	        List<Tree> leaves = tree.getLeaves();
	      
	        for (Tree leaf : leaves) { 
	            Tree parent = leaf.parent(tree);
	           sb.append(leaf.label().value() + "_" + parent.label().value() + " ");
	        }
	        
    	}
    	
		String output = sb.toString();
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
	}

	public  synchronized LexicalizedParser loadParser(String model) {
		LexicalizedParser mt = models.get(model);
	if(mt==null)
	{
		models.put(model, mt=LexicalizedParser.loadModel(model));
	}
	return mt;
	
	}
	
}
