package de.l3s.topics.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

public class SplitTextInCategories {
	
	public void folder2Table(File parentFolder, String tableName, Connection con, Hashtable<String, String> namemapping) throws SQLException
	{
		String createtablestr="CREATE TABLE `"+tableName+"` " +
				"(  `id` int(11) NOT NULL auto_increment,  " +
				"`category` varchar(100) collate utf8_unicode_ci NOT NULL,  " +
				"`document_id` varchar(200) collate utf8_unicode_ci NOT NULL,  " +
				"`parnum` int(11) NOT NULL,  " +
				"`txt` longtext collate utf8_unicode_ci NOT NULL,  " +
				"`lem_nouns` longtext collate utf8_unicode_ci NOT NULL,  PRIMARY KEY  (`id`) )" +
				" ENGINE=MyISAM AUTO_INCREMENT=225506 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
		
		Statement st = con.createStatement();
		st.executeUpdate(createtablestr);
		
		for(File cat:parentFolder.listFiles())
		{
			String catname=cat.getName();
			if(namemapping!=null){catname=namemapping.get(catname);}
			
			if(cat.toString().endsWith(".txt"))
			{
				//String txt=read(cat);
			}
			
		}
		
		String s="INSERT INTO flickrattractive.auto5000_descriptions SELECT null, t.title,CONCAT(t.title,\" \",d.docid) , p.parid,p.txt,\"\" FROM eval_paragraph p LEFT JOIN eval_paradoc pd ON (pd.parid=p.parid) LEFT JOIN eval_document d ON (d.docid=pd.docid) LEFT JOIN  eval_tiers t ON (t.tid=d.tid)";
	}
	public static String read(File filePath)
		    throws java.io.IOException{
		        StringBuffer fileData = new StringBuffer(1000);
		        BufferedReader reader = new BufferedReader(
		                new FileReader(filePath));
		        char[] buf = new char[1024];
		        int numRead=0;
		        while((numRead=reader.read(buf)) != -1){
		            fileData.append(buf, 0, numRead);
		        }
		        reader.close();
		        return fileData.toString();
		    }
}
