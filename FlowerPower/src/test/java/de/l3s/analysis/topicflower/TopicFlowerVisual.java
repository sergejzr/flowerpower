package de.l3s.analysis.topicflower;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.l3s.source.DataSource;
import newMI.FlowerPower.OrderStrategy;
import test.FlowerCreatorFramework;

public class TopicFlowerVisual {

	private Options options;

	public TopicFlowerVisual(String[] args) {

		// create Options object
		options = new Options();
		// add t option

		options.addOption(Option.builder().longOpt("input").hasArg(true).desc("xml input file created with TopicFlowerCreator or a directory for batch processing.")
				.required(true).build());		
		options.addOption(Option.builder().longOpt("output").hasArg(true).desc("a directory to write out the images").required(true).build());

		options.addOption(Option.builder().longOpt("format").hasArg(true).desc("[png|txt] the visual representation of the topic distribution. Default is png - a topicflower picture will be produced").required(true).build());

		options.addOption(Option.builder().longOpt("topicspetal").hasArg()
				.desc("need two arguments: how many topics should be on the petal and how many words a topic on the petal should contain, like topicnr:topiclength")
				.numberOfArgs(2).valueSeparator(':').required(false).build());
		options.addOption(Option.builder().longOpt("topicsconnect").hasArg()
				.desc("need two arguments: how many topics should be connecting petals and how many words such a topic  should contain, like topicnr:topiclength")
				.numberOfArgs(2).valueSeparator(':').required(false).build());

		options.addOption(Option.builder().longOpt("topicsconnect").hasArg()
				.desc("need two arguments: how many topics should be in the center and how many words such a topic should contain, like topicnr:topiclength")
				.numberOfArgs(2).valueSeparator(':').required(false).build());



	}

	public static void main(String[] args) {
		TopicFlowerVisual tf = new TopicFlowerVisual(args);
		DefaultParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(tf.options, args);

			DataSource datasource=null;
			String inputfilestr = cmd.getOptionValue("input");
			
			ArrayList<File> toprocess=new ArrayList<>();
			File inputfile =new File(inputfilestr);
			if(inputfile.isFile())
			{
				if(!inputfile.getName().endsWith(".xml"))
				{
					throw new ParseException("Input file should be an XML or directory");
				}
				toprocess.add(inputfile);
			}else
			{
				for(File f:inputfile.listFiles())
				{
					if(f.isFile()&&f.getName().endsWith(".xml"))
					toprocess.add(f);
				}
			}
			
			String outputdirstr = cmd.getOptionValue("output");
			File outputdir=new File(outputdirstr);

			if(outputdir.isDirectory())
			{
				
			}else
			{
				throw new ParseException("Output has to be a directory");
			}
			
			
			String topicspetalstr = cmd.getOptionValue("topicspetal");
			
			
			
			
			
			
			
			
			
			
			
			// cmd.getOptionValue(opt, defaultValue);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			 
			HelpFormatter formatter = new HelpFormatter();
			System.err.println(e.getMessage());
			formatter.printHelp("java -cp FlowerPower.jar " + tf.getClass().getCanonicalName().trim() + " [OPTIONS]",
					tf.options);
			return;
		}
	}
}