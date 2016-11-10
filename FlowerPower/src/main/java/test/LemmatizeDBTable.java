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

public class LemmatizeDBTable {
	
	public String table;
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public  void lemmatizeParallel(String langtoken,int numthreads)
	{
		//lemma lem= new lemma();
		//lem.init();
		try {
			 Connection dbcon = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
			
			//SELECT * FROM `conference_proceedings_paragraphs` WHERE `ckey` LIKE '%chi\\\\%' AND `txt` LIKE 'abstract%'
			PreparedStatement pstmt1= dbcon.prepareStatement("SELECT `id`,`txt` FROM `"+table+"` WHERE lem_nouns=''");
			
			PreparedStatement pstmt2= dbcon.prepareStatement("UPDATE `"+table+"` SET `lem_nouns`=? WHERE `id`=?");
			
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
				executor.execute(t=new LemmatizeThread(origidx,towork,langtoken,"N".split(","),false, null));
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
	

	public LemmatizeDBTable(String table) {
		this.table=table;
	}
	public static void main(String[] args) {
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LemmatizeDBTable ld= new LemmatizeDBTable(
				//"flower_fulltext_sections_auto5000"
			//	"flower_fulltext_auto5000"
				//"flower_werft"
				//"flower_cropped_werft"
				//"flower_werft_par"
				//"flower_eclipse_quater"
				"flower_yak_state"
				);
		
		ld.lemmatizeParallel("en",20);
	}

}

