package de.l3s.flowertools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

public class CSV2Filesystem {
public static void main(String[] args) {
	File csv=new File("/home/zerr/tweetsdl/flower_streetart_cropping_full.csv");
	File out=new File(new File("flowers"),csv.getName().substring(0,csv.getName().length()-4));
	try {
		BufferedReader br=new BufferedReader(new FileReader(csv));
		String line=null;
		Hashtable<String, FileWriter> flowerfiles=new Hashtable<>();
		try {
			while((line=br.readLine())!=null)
			{
				String[] cols = line.split("\",\"");
				if(cols.length<6){continue;}
				String last=cols[5].replaceAll("\"", "");
				String category=cols[1].replaceAll("\"", "");
				category=category.replaceAll("&", "and").replaceAll("\\/", " ");
				FileWriter fw = flowerfiles.get(category);
				if(fw==null){
					File dir=new File(out, category);
					dir.mkdirs();
					flowerfiles.put(category, fw=new FileWriter(new File(dir,category+".txt")));
					}else{
				fw.write("\n");
					}
				fw.write(last);
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(FileWriter fw:flowerfiles.values())
		{
			try {
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}
}
