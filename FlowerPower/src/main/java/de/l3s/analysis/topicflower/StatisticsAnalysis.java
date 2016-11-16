package de.l3s.analysis.topicflower;

import java.io.BufferedReader;
import java.io.File;
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

public class StatisticsAnalysis {

	private Options options;
	private File indir;
	private double tradeoff;
	private int k;
	
	public StatisticsAnalysis(String[] args) {
		options = new Options();
		// add t option

		options.addOption(Option.builder().longOpt("indir").hasArg(false).desc("a directory to read").numberOfArgs(1)
				.argName("path").required().build());
		
		options.addOption(Option.builder().longOpt("tradeoff").hasArg(true)
				.desc("a parameter between [0,1]. 0 - prefer high term frequency, 1-prefer even distribution among categories. Default value: 0.01 ").required()
				.build());
		options.addOption(Option.builder().longOpt("k").hasArg(true)
				.desc("The length of the stoppword candidate list").required()
				.build());
		DefaultParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(options, args);

		String indirstr = cmd.getOptionValue("indir");
		 indir=new File(indirstr);
		 
			String tradeoffstr = cmd.getOptionValue("tradeoff");
			tradeoff=0.005;
			try{
			 tradeoff=Double.parseDouble(tradeoffstr);
			 if(tradeoff<0||tradeoff>1){throw new ParseException("The argument --tradeoff requires a real number [0,1] as argument");}
			}catch(Exception e)
			{
				throw new ParseException("The argument --tradeoff requires a real number [0,1] as argument");
			}
			String kstr = cmd.getOptionValue("k");
			try{
				 k=Integer.parseInt(kstr);
				}catch(Exception e)
				{
					throw new ParseException("The argument --k requires to be an integer");
				}
			
		} catch (ParseException e) {
					// TODO Auto-generated catch block

					HelpFormatter formatter = new HelpFormatter();
					System.err.println(e.getMessage());
					formatter.printHelp("java -cp "+Tool.jarName(this)+" " + this.getClass().getCanonicalName().trim() + " [OPTIONS]",
							options);
					System.exit(1);
				}
		
		
	}

	Hashtable<String, Hashtable<String, Integer>> statistics = new Hashtable<>();

	public void run(File indir) {

		Path walkpath = Paths.get(indir.getAbsolutePath());
		try {
			Files.walkFileTree(walkpath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

					if (attrs.isDirectory() || !file.toString().endsWith("tsv_TMP")) {
						return FileVisitResult.CONTINUE;
					}
					if (file.getParent().endsWith("misc")) {
						return FileVisitResult.CONTINUE;
					}
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
			line = line.toLowerCase();
			HashSet<String> docfreq = new HashSet<>();
			for (String term : line.split("\\s+")) {
				if (docfreq.contains(term)) {
					continue;
				}
				docfreq.add(term);
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
		
		if(args.length==1&&args[0].equals("--test")){
		String argstr = "--indir /media/zerr/BA0E0E3E0E0DF3E3/yaktextscleaned/regions/ --tradeoff 0.01 --k 100";
		args=argstr.split("\\s+");
		}
		StatisticsAnalysis a = new StatisticsAnalysis(args);
		
		a.printTopK();
		//a.doeExperiment();
		
	}

	private void printTopK() {
		run(indir);
		printTopK(k, this.tradeoff,calculateStats());
		
	}

	private void printTopK(int k, double tradeoff, Hashtable<String, ArrayList<Double>> stats) {
		
		Hashtable<String, Double> scores=new Hashtable<>();
		
		for(String term:stats.keySet())
		{
			ArrayList<Double> arr = stats.get(term);
			Double df = arr.get(0);
			Double cos = arr.get(1);
			scores.put(term, tradeoff * Math.log(df) + (1 - tradeoff) * cos);
		}
		
		
		ArrayList<String> allterms = new ArrayList<>();
		allterms.addAll(scores.keySet());

		Collections.sort(allterms, new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				return scores.get(o1).compareTo(scores.get(o2));
			}});

		Collections.reverse(allterms);
		int head = 0;
		int cnt = 0;

		System.out.println("Rank\tTradeoff\tterm\tScore\tDF\tEveness");

		for (String term : allterms) {
			if (head % 20 == 0) {
				// System.out.println("rank\tterm\tScore\toverallcnt");
			}
			System.out.println(cnt++ + "\t" + tradeoff + "\t" + term + "\t" + scores.get(term) + "\t"
					+ stats.get(term).get(0)+ "\t" +stats.get(term).get(1));
			if (head++ > k) {
				break;
			}
		}

	}

	Hashtable<String, ArrayList<Double>> calculateStats()
	{

		Hashtable<String, ArrayList<Double>> scores=new Hashtable<>();
		
	

		HashSet<String> visited = new HashSet<>();



		Hashtable<String, List<Double>> requencydistribution = new Hashtable<>();

		Hashtable<String, Integer> catcnt = new Hashtable<>();
		int allcnt = 0;
		for (String itercat : statistics.keySet()) {
			Hashtable<String, Integer> tab = statistics.get(itercat);
			int sum = 0;
			for (String term : tab.keySet()) {
				sum += tab.get(term);
			}
			catcnt.put(itercat, sum);
			allcnt += sum;
		}

		for (String itercat : statistics.keySet()) {
			for (String term : statistics.get(itercat).keySet()) {
				if (visited.contains(term)) {
					continue;
				}
				visited.add(term);
				int cnt = 0;
				int sum = 0;
				List<Double> frequencies = new ArrayList<>();

				Hashtable<String, Integer> termspercat = new Hashtable<>();

				for (String cat : statistics.keySet()) {

					Double sz = 1. * statistics.get(cat).size();
					Integer termcnt = statistics.get(cat).get(term);
					if (termcnt == null)
						termcnt = 0;
					// frequencies.add(100. * termcnt/catcnt.get(cat));
					frequencies.add(1. * termcnt);
					sum += termcnt;
					cnt++;
				}
				requencydistribution.put(term, frequencies);

				List<Double> h = frequencies;
				double[] values = new double[h.size()];
				for (int i = 0; i < values.length; i++) {
					values[i] = h.get(i);
				}

				double sumdf = 0.0;

				for (Double d : frequencies) {
					sumdf += d;
				}
				double avg = sumdf / frequencies.size() / (allcnt / statistics.size());

				scores.put(term, new ArrayList<Double>(Arrays.asList(new Double[]{avg, Math.abs(cosineSimilarity(values, new double[] { 300, 300, 300, 300 }))})));
			}

		}

		return scores;

	}
		
	
	private void doeExperiment() {
		run(indir);
		Hashtable<String, ArrayList<Double>> list = calculateStats();
	
		Hashtable<Double, Integer> ranks = new Hashtable<>();
		ArrayList<String> allterms = new ArrayList<>();
		allterms.addAll(list.keySet());

		for (Double factor=0.001;factor<0.5;factor+=0.001) {
			Hashtable<String, Double> scores = new Hashtable<>();
			for (String s : allterms) {
				Double avg = list.get(s).get(0);
				Double cos = list.get(s).get(1);
				scores.put(s, factor * Math.log(avg) + (1 - factor) * Math.log(cos));
			}

			Collections.sort(allterms, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return scores.get(o1).compareTo(scores.get(o2));
				}
			});

			Collections.reverse(allterms);
			
			HashSet<String> set = getStopwords();
			int cnt = set.size();
			for (int i = 0; i < 200; i++) {
				if (set.contains(allterms.get(i))) {
					cnt--;
				}
				if (cnt == 1) {
					ranks.put(factor, i);
					break;
				}
			}
		}
ArrayList<Double> rankvals=new ArrayList<>(ranks.keySet());
Collections.sort(rankvals,new Comparator<Double>() {

	@Override
	public int compare(Double o1, Double o2) {
		// TODO Auto-generated method stub
		return ranks.get(o1).compareTo(ranks.get(o2));
	}
	
});

for(Double d:rankvals){
		System.out.println(d+"\t"+ranks.get(d));
}

	}



	public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	static HashSet<String> getStopwords() {
		HashSet<String> ret = new HashSet<>();
		ret.addAll(Arrays.asList(new String[] { "actually", "kind", "also", "get", "haha", "have", "here", "just",
				"lol", "not", "only", "out", "still", "that", "then", "there", "too", "well", "yak", "yeah", "yik" }));
		return ret;
	}
}
