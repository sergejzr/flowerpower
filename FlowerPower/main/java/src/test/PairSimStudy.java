package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import javax.xml.bind.JAXBException;

import de.l3s.flower.Category;
import de.l3s.flower.Flower;

public class PairSimStudy {
	
	public static File directory=new File("C:\\Users\\Jaspreet\\Desktop\\study");
	public static HashMap<String,CategoryPair> correct=new HashMap<String, CategoryPair>();
	public static HashMap<String,CategoryPair> others=new HashMap<String, CategoryPair>();
	public static HashSet<CategoryPair> used = new HashSet<CategoryPair>();
	public static void main(String[] args) {
		for (File f : directory.listFiles()) {
			if (!f.getName().endsWith("xml"))
				continue;
			String flowername = f.getName().substring(0,
					f.getName().lastIndexOf("_"));
			System.out.println(flowername);
			
			try {
				Flower flower = Flower.readFlower(f);
				ArrayList<Category> cats=(ArrayList<Category>) flower.getOrderedCategories();
				correct=generateCorrectPairs(cats);
				others= generateOtherPairs(cats);
				createExcelSheetsForStudy(flowername);
				correct.clear();
				others.clear();
				used.clear();
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					

	}
}

	private static void createExcelSheetsForStudy(String name) {
		File f= new File("C:\\Users\\Jaspreet\\Desktop\\study\\Pairs-"+name+".tsv");
		File res= new File("C:\\Users\\Jaspreet\\Desktop\\study\\Pairs-"+name+"-results.tsv");
		FileWriter fw;
		FileWriter fwres;
		try {
			fw = new FileWriter(f);
			fwres = new FileWriter(res);
			int i=0;
			for(String key: correct.keySet())
			{
				i++;
				CategoryPair correctPair = correct.get(key);
				CategoryPair wrongPair = randomSelect(others);
				Random generator = new Random();
				int n=generator.nextInt(10);
				if(n%2==0)
					fw.append(i+"\t"+"Pair 1: "+correctPair.id+"\t"+"Pair 2: "+wrongPair.id+"\n");
				else
					fw.append(i+"\t"+"Pair 1: "+wrongPair.id+"\t"+"Pair 2: "+correctPair.id+"\n");
				fwres.append(i+"\t"+key+"\n");
			}
			
			fw.close();
			fwres.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	private static CategoryPair randomSelect(HashMap<String, CategoryPair> others2) {
		Random generator = new Random();
		Object[] values =  others2.values().toArray();
		CategoryPair randomValue =null;
		randomValue =(CategoryPair) values[generator.nextInt(values.length)];
//		while(used.contains(randomValue))
//		{
//			randomValue =(CategoryPair) values[generator.nextInt(values.length)];
//			
//		}
//		used.add(randomValue);
		return randomValue;
	}

	private static HashMap<String,CategoryPair> generateOtherPairs(ArrayList<Category> cats) {
		HashMap<String,CategoryPair> pairs= new HashMap<String,CategoryPair>();
		Iterator<Category> iter = cats.iterator();
		for(Category c1: cats)
		{
			for(Category c2: cats)
			{
				if(c1==c2)
					continue;
				if(correct.keySet().contains(c1.getName()+"--"+c2.getName())|| correct.keySet().contains(c2.getName()+"--"+c1.getName()))
					continue;
				
				CategoryPair pair= new CategoryPair(c1, c2);
				pair.setNeighbourInFlower(true);
				pairs.put(pair.id,pair);
			}
		}
		
		
		return pairs;
	}

	private static HashMap<String,CategoryPair> generateCorrectPairs(ArrayList<Category> cats) {
		HashMap<String,CategoryPair> pairs= new HashMap<String, CategoryPair>();
		for(int i=0; i<cats.size();i++)
		{
			int j;
			if(i==cats.size()-1)
				j=0;
			else
			 j=i+1;
			CategoryPair pair= new CategoryPair(cats.get(i), cats.get(j));
			pair.setNeighbourInFlower(true);
			pairs.put(pair.id,pair);
		}

		return pairs;
	}
}

