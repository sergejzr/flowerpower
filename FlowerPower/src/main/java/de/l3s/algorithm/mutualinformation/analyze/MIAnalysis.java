package de.l3s.algorithm.mutualinformation.analyze;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.l3s.analysis.topicflower.TextFileCleaner;
import de.l3s.analysis.topicflower.Tool;
import de.l3s.source.DataRow;
import de.l3s.source.DataSource;
import de.l3s.source.FowerReadException;

public class MIAnalysis {
private Options options;
File dir1=null,dir2=null;
String dir1str=null,dir2str=null;
private String lable1;
private String lable2;
private String kstr;
private int k;

public MIAnalysis(String[] args) {
	
	options = new Options();
// add t option

options.addOption(Option.builder().longOpt("indir_pos").hasArg(false).desc("path to the directory with positive examples").numberOfArgs(1)
		.argName("path1").required().build());

options.addOption(Option.builder().longOpt("lable_pos").hasArg(false).desc("positive lable").numberOfArgs(1)
		.argName("lable1").build());

options.addOption(Option.builder().longOpt("indir_neg").hasArg(false).desc("path to the directory with nagative examples ").numberOfArgs(1)
		.argName("path2").required().build());
options.addOption(Option.builder().longOpt("lable_neg").hasArg(false).desc("negative lable").numberOfArgs(1)
		.argName("lable2").build());

options.addOption(
		Option.builder().longOpt("k").hasArg(true).desc("top-k terms to print for each class (default 50)")
				.build());

options.addOption(
		Option.builder().longOpt("test").hasArg(false).desc("The software will run in demo mode")
				.build());
options.addOption(
		Option.builder().longOpt("help").hasArg(false).desc("Prints possible options")
				.build());




DefaultParser parser = new DefaultParser();
try {
	CommandLine cmd = parser.parse(options, args);

	dir1str = cmd.getOptionValue("indir_pos");
	dir2str = cmd.getOptionValue("indir_neg");
	
	lable1 = cmd.getOptionValue("lable_pos","pos");
	lable2 = cmd.getOptionValue("lable_neg","neg");
	
	kstr=cmd.getOptionValue("k","50");


	
	try {
		k = Integer.parseInt(kstr);
		if(k<=0)
		{
			throw new ParseException(
					"argument" + k + " is not correct for the option --k (should be positive integer > 0)");
		}
		
		
		
	} catch (Exception e) {
		throw new ParseException(
				"argument" + k + " is not correct for the option --k (should be integer)");
	}
	
	
	dir1=new File(dir1str);
	dir2=new File(dir2str);
	
	if(!dir1.exists())
	{
		throw new ParseException(
				"Error, directory '" + dir1 + "' does not exist");
	}
	
	if(!dir2.exists())
	{
		throw new ParseException(
				"Error, directory '" + dir2 + "' does not exist");
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

public static void main(String[] args) {
	
	if(args.length==1&&args[0].equals("--test")){
		{
			String argumentline="--indir_pos testtweetsclean/posdir --lable_pos reach --indir_neg testtweetsclean/negdir --lable_neg poor --k 50";
			System.out.println("DEMO mode.\n"+argumentline+"\n");
			args=(argumentline).split("\\s+");
		}
		}
	MIAnalysis mia = new MIAnalysis(
				args
						);
		mia.run();
		
		

}

private void run() {
	

	
	ArrayList aPositiveTrainList=new ArrayList<>();
	ArrayList aNegativeTrainList=new ArrayList<>();
	
	readDir(dir1,aPositiveTrainList);
	readDir(dir2,aNegativeTrainList);
	
	
	
	

	MIselection mis=new MIselection(aPositiveTrainList, aNegativeTrainList);
	
	mis.computePositiveAndNegativeMIvalues();
	
	
	String posTopic=lable1, negTopic=lable2;
	
	System.out.println();
System.out.println("MI");	
System.out.println("mi.getTopNposResults("+k+") "+posTopic+":"
		+ mis.getTopNposResultList(k));


System.out.println("mi.getTopNnegResults("+k+") "+negTopic+":"
		+ mis.getTopNnegResultList(k));
	
	
}

private void readDir(File dir1, ArrayList aPositiveTrainList) {
	
	List<String> l=new ArrayList<>();
	
	DataSource ds=new DataSource(dir1);
	
try {
	ds.connect();
	
	while(ds.hasNext())
	{
		DataRow row = ds.getRow();
		MIselection.addTerms(aPositiveTrainList, row.getText().split("\\s"));
	}
} catch (FowerReadException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
	
	
}
}
