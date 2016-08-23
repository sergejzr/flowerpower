package test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import de.l3s.db.DB;

public class TopicLabeler {
private Connection con;

public TopicLabeler() {
	try {
		refreshcon();
		
		
		
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
public Lable lable(Vector<WeightedTerm> t, String language,int precision)
{
	try {
		refreshcon();
		PreparedStatement pst = con.prepareStatement("SELECT title, parent, match(txt) AGAINST(? IN BOOLEAN MODE) as score FROM gutearbeit.en_wikipedia_sections WHERE match(txt) AGAINST(? IN BOOLEAN MODE) ORDER BY score DESC LIMIT 20");

		Comparator<WeightedTerm> wcomp;
		Collections.sort(t, wcomp=new Comparator<WeightedTerm>() {

			@Override
			public int compare(WeightedTerm arg0, WeightedTerm arg1) {
				// TODO Auto-generated method stub
				return arg0.getScore().compareTo(arg1.getScore());
			}
		});
		String query="";
		
		for(WeightedTerm term:t)
		{if(precision--<=0){break;}
		if(query.length()>0) query+=" ";
		query+=term.getTerm();
		}
		pst.setString(1,query);
		pst.setString(2,query);
		ResultSet rs = pst.executeQuery();
		Hashtable<String, WeightedTerm> scorerpag=new  Hashtable<String, WeightedTerm>();
		Hashtable<String, WeightedTerm> scorersec=new  Hashtable<String, WeightedTerm>();
		while(rs.next())
		{
			Double score= rs.getDouble("score");
			String psr = rs.getString("parent");
			WeightedTerm scorer = scorerpag.get(psr);
			
			if(scorer==null)
			{
				
				scorerpag.put(psr,scorer=new WeightedTerm(psr, 0.));
			}
			scorer.plusWeight(1.0*score);
			String sec = rs.getString("title");
scorer = scorersec.get(sec);
			
			if(scorer==null)
			{
				
				scorersec.put(psr,scorer=new WeightedTerm(psr, 0.));
			}
			scorer.plusWeight(1.0*score);
			
		}

		List<WeightedTerm> sortedpag=new ArrayList<WeightedTerm>();
		sortedpag.addAll(scorerpag.values());
		Collections.sort(sortedpag, wcomp);	
		List<WeightedTerm> sortedsec=new ArrayList<WeightedTerm>();
		sortedpag.addAll(scorersec.values());
		Collections.sort(sortedsec, wcomp);	
		
		System.out.println("Candidates Pages   : "+sortedpag);
		System.out.println("Candidates Sections: "+sortedsec);
		
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
return null;

}
private void refreshcon() throws SQLException, ClassNotFoundException, IOException {
	if(this.con==null||this.con.isClosed()){
		Connection dbcon = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
		
	}
	
}
}
