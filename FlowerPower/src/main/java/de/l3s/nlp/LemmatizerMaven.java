package de.l3s.nlp;

import java.io.StringReader;
import java.util.Hashtable;
import java.util.List;
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

	    public Tree parse(LexicalizedParser parser, String str) {                
	        List<CoreLabel> tokens = tokenize(str);
	        Tree tree = parser.apply(tokens);
	        return tree;
	    }

	    private List<CoreLabel> tokenize(String str) {
	        Tokenizer<CoreLabel> tokenizer =
	            tokenizerFactory.getTokenizer(
	                new StringReader(str));    
	        return tokenizer.tokenize();
	    }

	    
	public static void main(String[] args) {
		LemmatizerMaven lm=new LemmatizerMaven();
			System.out.println(lm.getLemma("Well fuck", "en"));
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
		LexicalizedParser mt=models.get(model);
		if(mt==null)
		{
			models.put(model, mt=LexicalizedParser.loadModel(model));
		}
		
		 Tree tree = parse(mt,text);  

	        List<Tree> leaves = tree.getLeaves();
	       StringBuilder sb=new StringBuilder();
	        for (Tree leaf : leaves) { 
	            Tree parent = leaf.parent(tree);
	           sb.append(leaf.label().value() + "_" + parent.label().value() + " ");
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
	
}
