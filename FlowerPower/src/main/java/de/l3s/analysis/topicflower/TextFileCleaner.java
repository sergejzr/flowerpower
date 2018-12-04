package de.l3s.analysis.topicflower;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
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
import org.apache.commons.io.FileUtils;

import de.l3s.nlp.LemmatizeThread;

public class TextFileCleaner {

	int numthreads;
	private boolean stemm;
	private String[] stopwords;
	private Options options;
	private String indir;
	private String outdir;
	private String lang;
	private String[] poslist;
	private int batchsize;
	private int maxdocs;

	

	public TextFileCleaner(String[] args) {
		options = new Options();
		// add t option

		options.addOption(Option.builder().longOpt("indir").hasArg(false).desc("a directory to read").numberOfArgs(1)
				.argName("path").required().build());
		options.addOption(Option.builder().longOpt("outdir").hasArg(false)
				.desc("a directory to write (will be created if not exists)").numberOfArgs(1).argName("path").required()
				.build());
		options.addOption(Option.builder().longOpt("lang").hasArg().desc("language of the text files").numberOfArgs(1)
				.argName("").required(false).build());
		options.addOption(Option.builder().longOpt("numthreads").hasArg().desc("number of thread to use")
				.numberOfArgs(1).argName("").required(false).build());
		options.addOption(
				Option.builder().longOpt("pos").hasArg(true).desc("comma separated list of part of speech tags to keep")
						.numberOfArgs(1).argName("poslist").valueSeparator(',').required(false).build());
		options.addOption(
				Option.builder().longOpt("batchsize").hasArg(true).desc("number of documents handled by a single thread")
						.required(false).build());
		options.addOption(
				Option.builder().longOpt("maxdocs").hasArg(true).desc("maximum number of documents to consider per file")
						.required(false).build());
		
		options.addOption(
				Option.builder().longOpt("stem").desc("true, if the words should be stemmed").required(false).build());
		options.addOption(Option.builder().longOpt("stoplist").desc("list of stopwords").required(false).build());
		
		options.addOption(
				Option.builder().longOpt("test").hasArg(false).desc("The software will run in demo mode")
						.build());
		options.addOption(
				Option.builder().longOpt("help").hasArg(false).desc("Prints possible options")
						.build());
		
		DefaultParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(options, args);

			indir = cmd.getOptionValue("indir");
			outdir = cmd.getOptionValue("outdir");


			 lang = cmd.getOptionValue("lang", "en");
			 numthreads = 1;
			String numthreadsstr = cmd.getOptionValue("numthreads", "1");
			try {
				numthreads = Integer.parseInt(numthreadsstr);
			} catch (Exception e) {
				throw new ParseException(
						"argument" + numthreadsstr + " is not correct for the option --numthreads (should be integer)");
			}
			
			String batchsizestr = cmd.getOptionValue("batchsize", "100");
			try {
				batchsize = Integer.parseInt(batchsizestr);
			} catch (Exception e) {
				throw new ParseException(
						"argument" + batchsizestr + " is not correct for the option --batchsize (should be integer)");
			}
			
			String maxdocsstr = cmd.getOptionValue("maxdocs", "100");
			try {
				maxdocs = Integer.parseInt(maxdocsstr);
			} catch (Exception e) {
				throw new ParseException(
						"argument" + maxdocsstr + " is not correct for the option --maxdocs (should be integer)");
			}
			

			String posliststr = cmd.getOptionValue("poslist", "F,J,N,R,U,V");
		
			try {
				this.poslist = posliststr.split(",");
			} catch (Exception e) {
				throw new ParseException("argument" + posliststr + " is not correct for the option --poslist");
			}
			String stemstr = cmd.getOptionValue("stem", "false");
		
			this.stemm = stemstr.equals("true");

			String stopliststr = cmd.getOptionValue("stoplist", "");
		
			try {
				this.stopwords = stopliststr.split(",");
			} catch (Exception e) {
				throw new ParseException("argument" + stopliststr + " is not correct for the option --stoplist");
			}

		if(indir.equals(outdir))
		{
			throw new ParseException("--outdir cannot be the same as --indir");
			
		}
		
		if(!new File(indir).exists())
		{
			throw new ParseException("Error: --indir "+indir+" does not exist");
			
		}
		new File(outdir).mkdirs();

		

		} catch (ParseException e) {
			// TODO Auto-generated catch block

			HelpFormatter formatter = new HelpFormatter();
			System.err.println(e.getMessage());
			formatter.printHelp("java -cp "+Tool.jarName(this)+" " + this.getClass().getCanonicalName().trim() + " [OPTIONS]",
					options);
			System.exit(1);
		}

	}

	public TextFileCleaner() {
		// TODO Auto-generated constructor stub
	}

	public void run() {

		Path inpath = Paths.get(indir);
		Path outpath = Paths.get(outdir);

		Path walkpath=inpath;
		try {
			Files.walkFileTree(walkpath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

					if (attrs.isDirectory() || !file.toString().endsWith("txt")) {
						return FileVisitResult.CONTINUE;
					}

					Path rel = inpath.relativize(file);
					Path newpath = outpath.resolve(rel);
					newpath.toFile().getParentFile().mkdirs();
					
					File targetfile = new File(newpath.toFile().toString());
					if(targetfile.exists()){return FileVisitResult.CONTINUE;}
					lemmatizeParallel(file.toFile(), targetfile);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
System.out.println("Preprocessing done");


	
	
	}

	public void lemmatizeParallel(File in, File out) {
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
			int maxdocs = this.maxdocs;
			do {

				Hashtable<Integer, String> origidx = new Hashtable<Integer, String>();
				Stack<Integer> towork = new Stack<Integer>();
				List<LemmatizeThread> myworkers = new ArrayList<LemmatizeThread>();

				ExecutorService executor = Executors.newFixedThreadPool(numthreads);

				int batchsize = this.batchsize;
				while (batchsize-- > 0 && (line = br.readLine()) != null) {

					origidx.put(id, line);
					towork.add(id);
					id++;
					if(maxdocs--<=0)
					{
						continue;
					}
				}

				for (int i = 0; i < numthreads; i++) {
					LemmatizeThread t;
					executor.execute(t = new LemmatizeThread(origidx, towork, this.lang, poslist, stemm, stopwords));
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
				System.out.print(cnt);

				System.out.println(" done");

			} while (line != null);

			fr.close();
			fw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public static void main(String[] args) {
		
		if(args.length==1&&args[0].equals("--test")){
		{
			String argumentline="--indir testtweets/ --lang de --numthreads 4 --outdir testtweetsclean/ --stem"
					;
			System.out.println("DEMO mode.\n"+argumentline+"\n");
			args=(argumentline).split("\\s+");
			

		}
		}
		TextFileCleaner tc1 = new TextFileCleaner(
				args
						);
		tc1.run();
		/*
		 * File dir = new
		 * File("/media/zerr/BA0E0E3E0E0DF3E3/yak/yaktexts/west/");
		 * TextFileCleaner tc = new TextFileCleaner(); tc.init(new File(dir,
		 * "StateYaksDocuments_AK.tsv"), new File(dir,
		 * "clean_StateYaksDocuments_AK.tsv"), "en", 5, new String[] { "F", "J",
		 * "N", "R", "U", "V" }, true, null); tc.lemmatizeParallel();
		 * 
		 * System.out.println(LemmatizeThread.garbage);
		 */
	}

}
