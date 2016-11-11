package de.l3s.analysis.topicflower;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.l3s.nlp.LemmatizeThread;

public class TextFileCleaner {
	File in;
	File out;
	String langtoken;
	int numthreads;
	String[] poss;
	private boolean stemm;
	private String[] stopwords;
	private Options options;

	public void init(File in, File out, String langtoken, int numthreads, String[] poss, boolean stemm,
			String[] stopwords) {

		this.in = in;
		this.out = out;
		this.langtoken = langtoken;
		this.numthreads = numthreads;
		this.poss = poss;
		this.stemm = stemm;
		this.stopwords = stopwords;
	}

	public TextFileCleaner(String[] args) {
		options = new Options();
		// add t option

		options.addOption(Option.builder().longOpt("infile").hasArg(true).desc("file to read").numberOfArgs(1)
				.argName("filepath").required().build());
		options.addOption(Option.builder().longOpt("outfile").hasArg(true).desc("file to write").numberOfArgs(1)
				.argName("filepath").required().build());
		options.addOption(Option.builder().longOpt("lang").hasArg().desc("file to write").numberOfArgs(1)
				.argName("language of the text files").required(false).build());
		options.addOption(Option.builder().longOpt("numthreads").hasArg().desc("file to write").numberOfArgs(1)
				.argName("number of thread to use").required(false).build());
		options.addOption(
				Option.builder().longOpt("pos").hasArg(true).desc("comma separated list of part of speech tags to keep")
						.numberOfArgs(1).argName("poslist").valueSeparator(',').required(false).build());
		options.addOption(
				Option.builder().longOpt("stem").desc("true, if the words should be stemmed").required(false).build());
		options.addOption(Option.builder().longOpt("stoplist").desc("list of stopwords").required(false).build());
		DefaultParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(options, args);

			String infile = cmd.getOptionValue("infile");
			String outfile = cmd.getOptionValue("outfile");
			String lang = cmd.getOptionValue("lang", "en");
			int numthreads = 1;
			String numthreadsstr = cmd.getOptionValue("numthreads", "1");
			try {
				numthreads = Integer.parseInt(numthreadsstr);
			} catch (Exception e) {
				throw new ParseException(
						"argument" + numthreadsstr + " is not correct for the option --umthreads (should be integer)");
			}

			String posliststr = cmd.getOptionValue("poslist", "F,J,N,R,U,V");
			String[] poslist;
			try {
				poslist = posliststr.split(",");
			} catch (Exception e) {
				throw new ParseException("argument" + posliststr + " is not correct for the option --poslist");
			}
			String stemstr = cmd.getOptionValue("stem", "false");
			boolean stem;
			stem = stemstr.equals("true");

			String stopliststr = cmd.getOptionValue("stoplist", "");
			String[] stoplist;
			try {
				stoplist = stopliststr.split(",");
			} catch (Exception e) {
				throw new ParseException("argument" + stopliststr + " is not correct for the option --stoplist");
			}

			init(new File(infile), new File(outfile), langtoken, numthreads, poslist, stem, stopwords);

		} catch (ParseException e) {
			// TODO Auto-generated catch block

			HelpFormatter formatter = new HelpFormatter();
			System.err.println(e.getMessage());
			formatter.printHelp("java -cp FlowerPower.jar " + this.getClass().getCanonicalName().trim() + " [OPTIONS]",
					options);
			return;
		}

	}

	public TextFileCleaner() {
		// TODO Auto-generated constructor stub
	}

	public void lemmatizeParallel() {
		// lemma lem= new lemma();
		// lem.init();
		try {
			FileReader fr = new FileReader(in);
			FileWriter fw = new FileWriter(out);
			BufferedReader br = new BufferedReader(fr);
			System.out.println("Start");
			int cnt = 0;

			String line = null;
			int id = 1;
			do {

				Hashtable<Integer, String> origidx = new Hashtable<Integer, String>();
				Stack<Integer> towork = new Stack<Integer>();
				List<LemmatizeThread> myworkers = new ArrayList<LemmatizeThread>();

				ExecutorService executor = Executors.newFixedThreadPool(15);

				int batchsize = 100;
				while (batchsize-- > 0 && (line = br.readLine()) != null) {

					origidx.put(id, line);
					towork.add(id);
					id++;
				}

				for (int i = 0; i < numthreads; i++) {
					LemmatizeThread t;
					executor.execute(t = new LemmatizeThread(origidx, towork, langtoken, poss, stemm, stopwords));
					myworkers.add(t);
				}

				try {
					executor.shutdown();
					executor.awaitTermination(7, TimeUnit.DAYS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				for (LemmatizeThread t : myworkers) {
					Hashtable<Integer, String> lemmatizedidx = t.getLemmatizedidx();
					for (Integer task : lemmatizedidx.keySet()) {

						cnt++;
						String lemmatized_nouns = lemmatizedidx.get(task);
						if (lemmatized_nouns.trim().length() == 0) {
							continue;
						}
						fw.write(lemmatized_nouns);
						fw.write("\n");

					}
				}
				myworkers = new ArrayList<>();
				System.out.println(cnt);

				System.out.println("done");

			} while (line != null);

			fr.close();
			fw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public static void main(String[] args) {
		TextFileCleaner tc1 = new TextFileCleaner(args);

		File dir = new File("/media/zerr/BA0E0E3E0E0DF3E3/yak/yaktexts/west/");
		TextFileCleaner tc = new TextFileCleaner();
		tc.init(new File(dir, "StateYaksDocuments_AK.tsv"), new File(dir, "clean_StateYaksDocuments_AK.tsv"), "en", 5,
				new String[] { "F", "J", "N", "R", "U", "V" }, true, null);
		tc.lemmatizeParallel();

		System.out.println(LemmatizeThread.garbage);
	}

}
