package test;




import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.l3s.db.DB;
import de.l3s.lemma.lemma;
import de.l3s.nlp.Lemma;
import de.l3s.nlp.LemmatizerMaven;

public class LemmatizeFramework {
	
	public String table;
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public  void lemmatizeParallel(String langtoken,int numthreads, String[] poss)
	{
		//lemma lem= new lemma();
		//lem.init();
		try {
			Connection dbcon = DB.getConnection("l3s","flickrattractive");
			
			//SELECT * FROM `conference_proceedings_paragraphs` WHERE `ckey` LIKE '%chi\\\\%' AND `txt` LIKE 'abstract%'
			PreparedStatement pstmt1= dbcon.prepareStatement("SELECT `id`,`txt` FROM "+table+" WHERE lem_nouns=''");
			
			PreparedStatement pstmt2= dbcon.prepareStatement("UPDATE "+table+" SET `lem_nouns`=? WHERE `id`=?");
			
			ResultSet rs = pstmt1.executeQuery();
			 
	        System.out.println("Start");
	        int cnt=0;
	        ExecutorService executor = Executors.newFixedThreadPool(15);
	        Hashtable<Integer, String> origidx=new Hashtable<Integer, String>();
	        Stack<Integer> towork=new Stack<Integer>();
	        
	       
	        while(rs.next())
			{
	        	 origidx.put(rs.getInt("id"), rs.getString("txt"));
	        	 towork.add(rs.getInt("id"));
			}
	        rs.close();
	        
	        List<LemmatizeThread> myworkers=new ArrayList<LemmatizeThread>();
	        for(int i=0;i<numthreads;i++)
	        {
	        	LemmatizeThread t;
				executor.execute(t=new LemmatizeThread(origidx,towork,langtoken,poss));
				myworkers.add(t);
	        }
	        
	        try {
	        	executor.shutdown();
				executor.awaitTermination(7, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        for(LemmatizeThread t:myworkers)
	        {
	        	Hashtable<Integer, String> lemmatizedidx = t.getLemmatizedidx();
	        	 for(Integer task:lemmatizedidx.keySet()){
	     	        
	 	        	cnt++;
	 	        	String lemmatized_nouns = lemmatizedidx.get(task);
	 	        	
	 	        		
	 	        		pstmt2.setString(1, lemmatized_nouns);
	 	        		pstmt2.setInt(2, task);
	 	        		
	 	        		pstmt2.addBatch();
	 	        		if(cnt%1000==0)
	 	        		{
	 	        			System.out.println(cnt);
	 	        			pstmt2.executeBatch();
	 	        	        System.out.println("done");
	 	        		}
	 	        			
	 		        	
	 			}
	        }
	       
	        System.out.println(cnt);
			pstmt2.executeBatch();
	        System.out.println("done");
	        
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		

	}
	public  void lemmatize(LemmatizerMaven mylem, String lang)
	{
		lemma lem= new lemma();
		lem.init();
		try {
			Connection dbcon = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
			
			//SELECT * FROM `conference_proceedings_paragraphs` WHERE `ckey` LIKE '%chi\\\\%' AND `txt` LIKE 'abstract%'
			PreparedStatement pstmt1= dbcon.prepareStatement("SELECT `id`,`txt` FROM `"+table+"` WHERE lem_nouns=''");
			
			PreparedStatement pstmt2= dbcon.prepareStatement("UPDATE `"+table+"` SET `lem_nouns`=? WHERE `id`=?");
			
			ResultSet rs = pstmt1.executeQuery();
			 
	        System.out.println("Start");
	        int cnt=0;
	        ExecutorService pool = Executors.newFixedThreadPool(5);
	        
	       
	        while(rs.next())
			{
	        	cnt++;
	        	String lemmatized_nouns = new String();
	        		if(rs.getString("txt").length()>10)
	        		{
	        			//lemmatized_nouns = getNounsFromSentenceWithoutNames(rs.getString("txt"), new Lemmatizer(null));
	        			//lemmatized_nouns = getNounsFromSentence(rs.getString("txt"),lem);
	        			lemmatized_nouns = getNounsFromSentence(rs.getString("txt"), mylem, lang);
	        		}
	        		
	        		pstmt2.setString(1, lemmatized_nouns.toLowerCase());
	        		pstmt2.setInt(2, rs.getInt("id"));
	        		
	        		pstmt2.addBatch();
	        		if(cnt%1000==0)
	        		{
	        			System.out.println(cnt);
	        			pstmt2.executeBatch();
	        	        System.out.println("done");
	        		}
	        			
		        	
			}
	        System.out.println(cnt);
			pstmt2.executeBatch();
	        System.out.println("done");
	        
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
		
		
		

	}
	/*
	private String getNounsFromSentenceWithoutNames(String txt, Lemmatizer lem) {
		//flower_wikimovies_nopersons

		StringBuilder sb= new StringBuilder();
		
		for(String k:lem.lemmitize(txt))
		{
			if(k.length()>2){
			sb.append(k);
			sb.append(" ");
			}
		}
		return new String(sb).trim();
		
	
		
	}
	*/
	
	private String getNounsFromSentence(String s,
			de.l3s.nlp.LemmatizerMaven mylem, String lang) {
		
		StringBuilder sb= new StringBuilder();
		List<Lemma> res=mylem.getLemma(s,lang);
		
		if(res.isEmpty())
			return null;
		for(Lemma k: res)
		{
			if(k.getTag().startsWith("N")//||k.getTag().startsWith("V")
					)
			
			{
			
				sb.append(k.getLem());
				sb.append(" ");
			}
			
			
		}
		
		return new String(sb).trim().toLowerCase();
	}
	public static String getNounsFromSentence(String s,lemma lem)
	{
		StringBuilder sb= new StringBuilder();
		String t = lem.getSentenceLemmatizationWithPOS(s);
		String[] pos = t.split("\\n");
		if(t.isEmpty())
			return null;
		for(String k: pos)
		{
			String[] wordpos = k.split("\\t");
			if(wordpos[1].startsWith("n") && !wordpos[1].equalsIgnoreCase("nil"))
			{
			
				sb.append(wordpos[2]);
				sb.append(" ");
			}
			
			
		}
		
		return new String(sb).trim();
		
	}
	public LemmatizeFramework(String table) {
		this.table=table;
	}
	public static void main(String[] args) {
		
		if(args.length<3)
		{
			System.out.println("use with arguments : java -jar LemmatizeFramework database_table language N,V,A");
			return;
		}
			String lang=args[1];
			String poss[]=args[2].split(",");
			
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LemmatizeFramework ld= new LemmatizeFramework(
				//"flower_fulltext_sections_auto5000"
			//	"flower_fulltext_auto5000"
				//"flower_werft"
				//"flower_cropped_werft"
				//"flower_werft_par"
				//"flower_eclipse_quater"
				args[0]
				);
		
		ld.lemmatizeParallel(lang,20, poss);
	}

}

