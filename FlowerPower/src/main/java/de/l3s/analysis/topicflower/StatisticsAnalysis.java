package de.l3s.analysis.topicflower;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Variance;

import cc.mallet.util.Maths;

public class StatisticsAnalysis {

	Hashtable<String, Hashtable<String, Integer>> statistics = new Hashtable<>();

	public void run(File indir)
	{
		
		Path walkpath=Paths.get(indir.getAbsolutePath());
		try {
			Files.walkFileTree(walkpath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

					if (attrs.isDirectory() || !file.toString().endsWith("tsv")) {
						return FileVisitResult.CONTINUE;
					}
if(file.getParent().endsWith("misc")){return FileVisitResult.CONTINUE;}
					readFile(file.toFile());
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void readFile(File in) throws IOException {
		Hashtable<String, Integer> tab = statistics.get(in.getParent());
		if (tab == null) {
			statistics.put(in.getParent(), tab = new Hashtable<>());
		}

		FileReader fr = new FileReader(in);
		BufferedReader br = new BufferedReader(fr);
		String line = null;

		while ((line = br.readLine()) != null) {
line=line.toLowerCase();
			for (String term : line.split("\\s+")) {
				Integer termcnt = tab.get(term);
				if (termcnt == null) {
					termcnt = 0;
				}
				tab.put(term, termcnt + 1);
			}
		}
		br.close();
	}
public static void main(String[] args) {
	StatisticsAnalysis a=new StatisticsAnalysis();
	a.run(new File("/media/zerr/BA0E0E3E0E0DF3E3/yak/yaktexts/"));
	a.printTop();
}
private void printTop() {
	
	Variance v=new Variance();
	HashSet<String> visited=new HashSet<>();
	Hashtable<String, Integer> dist=new Hashtable<>();
	Hashtable<String, Integer> overallcnt=new Hashtable<>();
	Hashtable<String, Double> scores=new Hashtable<>();
	Hashtable<String, Double> scores2=new Hashtable<>();
	Hashtable<String, Double> scores3=new Hashtable<>();
	Hashtable<String, Double> averages=new Hashtable<>();
	Hashtable<String, Double> scores4=new Hashtable<>();
	
	for(String itercat:statistics.keySet())
	{
		for(String term:statistics.get(itercat).keySet())
		{
			if(visited.contains(term)){continue;}
			visited.add(term);
			int cnt=0;
			int sum=0;
			List<Double> frequencies=new ArrayList<>();
			for(String cat:statistics.keySet())
			{
				Integer termcnt = statistics.get(cat).get(term);
				if(termcnt==null) termcnt=0;
				frequencies.add(1.*termcnt);
				sum+=termcnt;
				cnt++;
			}
			
			
			overallcnt.put(term, sum);
			Double sum1=0.0;
			Double score=0.0;
			for(Double d:frequencies)
			{
			score+=Math.pow((d/sum),2);
			
			sum1+=d*(d-1);
			}
			double score3;
			scores3.put(term, score3=sum1/(cnt*(cnt-1)));
			scores.put(term, score);
			dist.put(term, cnt);
			
			double[] values=new double[frequencies.size()];
			for(int i=0;i<values.length;i++){values[i]=frequencies.get(i);}
			
			scores2.put(term, v.evaluate(values));
			double avg;
			averages.put(term, avg=(1.*sum/cnt));
			
			scores4.put(term,avg*score3);
			
		}
	}
	
	ArrayList<String> allterms=new ArrayList<>();
	allterms.addAll(scores.keySet());
	
	Collections.sort(allterms, new HTComparator(scores3,dist));
	
	Collections.reverse(allterms);
	int head=0;
	for(String term:allterms)
	{
		if(head%20==0)
			System.out.println("term\tSimpson\tVariance\tSimpson2\tMyscore\tcnt\toverallcnt");
		System.out.println(term+ "\t"+scores.get(term)+"\t"+scores2.get(term)+"\t"+scores3.get(term)+"\t"+scores4.get(term)+"\t"+dist.get(term)+"\t"+overallcnt.get(term));
		if(head++>200){break;}
	}
	
}
}
