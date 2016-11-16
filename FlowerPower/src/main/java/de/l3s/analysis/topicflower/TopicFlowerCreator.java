package de.l3s.analysis.topicflower;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.l3s.algorithm.mutualinformation.DiagramInput;
import de.l3s.flower.FlowerException;
import de.l3s.flower.FlowerPower;
import de.l3s.flower.FlowerPower.OrderStrategy;
import de.l3s.flower.gui.FlowerImage;
import de.l3s.flower.jaxb.Categories;
import de.l3s.flower.jaxb.Category;
import de.l3s.flower.jaxb.Connection;
import de.l3s.flower.jaxb.Connections;
import de.l3s.flower.jaxb.Flower;
import de.l3s.flower.jaxb.General;
import de.l3s.flower.jaxb.Instance;
import de.l3s.flower.jaxb.Instances;
import de.l3s.flower.jaxb.Term;
import de.l3s.flower.jaxb.Terms;
import de.l3s.flower.jaxb.Topic;
import de.l3s.flower.jaxb.TopicLink;
import de.l3s.flower.jaxb.Topics;

public class TopicFlowerCreator {

	private File backgrounddir;
	private File ldamodelfile;
	private File structureoutputfile;
	private File floweroutputfile;
	private String format;
	private boolean usemodel;
	private boolean usestructure;
	private int k;
	private OrderStrategy ordering;
	private Hashtable<String, Integer> petaloptions;
	private int ldathreadsnum;
	private int iternumnum;
	private Options options;
	private int top;
	private File inputdir;
	private String[] stoppwords;

	public TopicFlowerCreator() {

		// create Options object
		options = new Options();
		// add t option

		options.addOption(Option.builder().longOpt("inputdir").hasArg(true)
				.desc("Input directory to construct the flower from").required(false).build());
		options.addOption(Option.builder().longOpt("background").hasArg()
				.desc("a folder(s) with additional textcorpora that the algorithm will use to improve topic models")
				.required(false).build());
		options.addOption(Option.builder().longOpt("ldamodelfile").hasArg()
				.desc("a file to store the Mallet model (default=model.mdl)").required(false).build());
		options.addOption(Option.builder().longOpt("structureoutput").hasArg()
				.desc("a name of XML file to store the flower structure model (default=structure.xml)").required(false)
				.build());
		options.addOption(Option.builder().longOpt("floweroutput").hasArg()
				.desc("a name of png or html file to store the actual flower output (default=flower.png)")
				.required(false).build());
		options.addOption(Option.builder().longOpt("format").hasArg()
				.desc("[html|png] output format of the flower (default = png)").required(false).build());

		options.addOption(Option.builder().longOpt("usestructure")
				.desc("A flower structure file will be re-used, if exisis. ").required(false).build());
		
		
		options.addOption(Option.builder().longOpt("stoppwords").hasArg()
				.desc("A comma separated list of stopwords").required(false).build());
		
		options.addOption(Option.builder().longOpt("usemodel")
				.desc("A topic model file will be re-used, if exisis. (Will be ignored if usestructure is specified and structure xml file exists)")
				.required(false).build());

		options.addOption(
				Option.builder().longOpt("k").hasArg().desc("The number of LDA topics").required(false).build());
		options.addOption(Option.builder().longOpt("top").hasArg()
				.desc("The number of topics used to classify an instance").required(false).build());

		options.addOption(Option.builder().longOpt("ldathreadnum").hasArg()
				.desc("a number of threads EACH(!!!) LDA model generator is allowed to use").required(false).build());

		options.addOption(Option.builder().longOpt("iternum").hasArg()
				.desc("number of iterations the LDA should perform (more tham 2000 -> accurate models)").required(false)
				.build());
		options.addOption(Option.builder().longOpt("petalorder").hasArg()
				.desc("how the petals should be ordered [natural|optimal]").required(false).build());

		options.addOption(Option.builder().longOpt("tpetal").hasArg()
				.desc("need two arguments: how many topics should be on the petal and how many words a topic on the petal should contain, like topicnr:topiclength")
				.numberOfArgs(2).valueSeparator(':').required(false).build());
		options.addOption(Option.builder().longOpt("tconnect").hasArg()
				.desc("need two arguments: how many topics should be connecting petals and how many words such a topic  should contain, like topicnr:topiclength")
				.numberOfArgs(2).valueSeparator(':').required(false).build());

		options.addOption(Option.builder().longOpt("tcenter").hasArg()
				.desc("need two arguments: how many topics should be in the center and how many words such a topic should contain, like topicnr:topiclength")
				.numberOfArgs(2).valueSeparator(':').required(false).build());

	}

	private void readOptions(CommandLine cmd) throws ParseException {

		String inputdirstr = cmd.getOptionValue("inputdir");
		if (inputdirstr != null) {
			inputdir = new File(cmd.getOptionValue("inputdir"));

			if (!inputdir.isDirectory()) {
				throw new ParseException("The mandatory argument 'inputdir' shoud be a directory");
			}
		}
		String backgroundstr = cmd.getOptionValue("background", null);
		if (backgroundstr != null) {
			backgrounddir = new File(backgroundstr);

			if (!inputdir.isDirectory()) {
				throw new ParseException("The optional argument 'backgrounddir' shoud be a directory");
			}
		}
		// File tmp = new File("tmp");
		 String stoppwordsstr = cmd.getOptionValue("stoppwords", "");
		
		 stoppwords=stoppwordsstr.split(",");
		
		String ldamodelfilestr = cmd.getOptionValue("ldamodelfile", null);
		if (ldamodelfilestr != null) {
			ldamodelfile = new File(ldamodelfilestr);

			if (ldamodelfile.isDirectory()) {
				throw new ParseException("The optional argument 'ldamodelfile' shoud be a file name");
			}
			if (!ldamodelfile.getAbsoluteFile().getParentFile().exists()) {
				throw new ParseException("WIll not be able to write the model, directory does not exist: "
						+ ldamodelfile.getAbsoluteFile().getParentFile());

			}
		} else {
			// tmp.mkdirs();
			// ldamodelfilefile = new File("tmp/model.mdl");
		}

		String structureoutputstr = cmd.getOptionValue("structureoutput", null);

		if (structureoutputstr != null) {
			structureoutputfile = new File(structureoutputstr);

			if (structureoutputfile.isDirectory()) {
				throw new ParseException("The optional argument --structureoutput shoud be a file name");
			}
			if (!structureoutputfile.getAbsoluteFile().getParentFile().exists()) {
				throw new ParseException("WIll not be able to write the structure, directory does not exist: "
						+ structureoutputfile.getAbsoluteFile().getParentFile());

			}
		} else {
			// tmp.mkdirs();
			// structureoutputfile = new File("tmp/structure.xml");
		}

		String floweroutputstr = cmd.getOptionValue("floweroutput", null);
		if (floweroutputstr != null) {
			floweroutputfile = new File(floweroutputstr);

			if (floweroutputfile.isDirectory()) {
				throw new ParseException("The optional argument --floweroutputfile shoud be a file name");
			}
			if (!floweroutputfile.getAbsoluteFile().getParentFile().exists()) {
				throw new ParseException("Will not be able to write the flower, directory does not exist: "
						+ floweroutputfile.getAbsoluteFile().getParentFile());

			}
		} else {
			// tmp.mkdirs();
			// floweroutputfile = new File("tmp/flower.png");
		}

		format = cmd.getOptionValue("format", "png");

		if (!format.equals("png") && !format.equals("html")) {
			throw new ParseException("The --format has to be png or html.");
		}

		usemodel = cmd.hasOption("usemodel");
		usestructure = cmd.hasOption("usestructure");

		k = 100;
		try {

			k = (Integer.parseInt(cmd.getOptionValue("k", "100")));

		} catch (Exception e) {
			throw new ParseException("Please check your --k argument. It has to be an integer");

		}

		top = 5;
		try {

			top = (Integer.parseInt(cmd.getOptionValue("k", "5")));

		} catch (Exception e) {
			throw new ParseException("Please check your --top argument. It has to be an integer");

		}

		ordering = OrderStrategy.naturalOrdering;

		String ostr = cmd.getOptionValue("ordering", "natural");
		if (ostr.equals("natural")) {
			ordering = OrderStrategy.naturalOrdering;
		} else if (ostr.equals("optimal")) {
			ordering = OrderStrategy.optimalOrderung;
		} else {
			throw new ParseException("the --ordering argument can be only 'optimal' or 'natural'. You entered '" + ostr
					+ "', it is not a valid option");

		}

		ldathreadsnum = 1;

		try {
			ldathreadsnum = Integer.parseInt(cmd.getOptionValue("ldathreadnum", "1"));
		} catch (Exception e) {
			throw new ParseException("--ldathreadnum argument has to be an integer");

		}

		iternumnum = 1;

		try {
			iternumnum = Integer.parseInt(cmd.getOptionValue("iternum", "1"));
		} catch (Exception e) {
			throw new ParseException("--iternum argument has to be an integer");

		}

		String topicstr[] = new String[] { "4", "4" };
		topicstr = cmd.getOptionValues("tpetal");
		if (topicstr == null) {
			topicstr = new String[] { "4", "4" };
		}
		petaloptions = new Hashtable<>();
		try {

			petaloptions.put("tpetalnr", Integer.parseInt(topicstr[0]));
			petaloptions.put("tpetallength", Integer.parseInt(topicstr[1]));
		} catch (Exception e) {
			throw new ParseException("--tpetal need two colon separated integer arguments. topicnr:topiclength");
		}

		topicstr = cmd.getOptionValues("tconnect");
		if (topicstr == null) {
			topicstr = new String[] { "3", "3" };
		}
		try {
			petaloptions.put("tconnectnr", Integer.parseInt(topicstr[0]));
			petaloptions.put("tconnectlength", Integer.parseInt(topicstr[1]));

		} catch (Exception e) {
			throw new ParseException("--tconnect need two colon separated arguments. topicnr:topiclength");
		}
		topicstr = cmd.getOptionValues("tcenter");
		if (topicstr == null) {
			topicstr = new String[] { "5", "5" };
		}
		try {

			petaloptions.put("tcenternr", Integer.parseInt(topicstr[0]));
			petaloptions.put("tcenterlength", Integer.parseInt(topicstr[1]));

		} catch (Exception e) {
			throw new ParseException("--tcenter need two colon separated arguments. topicnr:topiclength");
		}

	}

	public static void main(String[] args) {

		//
		TopicFlowerCreator tf = new TopicFlowerCreator();
		if(args.length==1&&args[0].equals("--test")){
		String arguments = "--inputdir /home/zerr/Dropbox/us/Flowerinput/ --stoppwords lol,haha --k 150 --ldathreadnum 2 --iternum 100 --top 3 "
				+ "--ldamodelfile tmp/model.mdl --structureoutput tmp/flower.xml "
				+ "--petalorder natural --tcenter 5:5 --tconnect 3:3 --tpetal 4:4 --format png --floweroutput test.png";

		arguments = "--inputdir /home/zerr/Dropbox/us/Flowerinput/ --usemodel "
				+ "--ldamodelfile tmp/model.mdl --structureoutput tmp/flower.xml "
				+ "--petalorder natural --tcenter 5:5 --tconnect 3:3 --tpetal 4:4 --format png --floweroutput test2reusemodel.png";

		arguments = "--usestructure " + "--structureoutput tmp/flower.xml "
				+ "--petalorder natural --tcenter 5:5 --tconnect 3:3 --tpetal 4:4 --format png --floweroutput cntteststructreusemodel.png";
		arguments = "--usestructure " + "--structureoutput tmp/flower.xml "
				+ "--petalorder natural --tcenter 5:5 --tconnect 3:3 --tpetal 4:4 --format html --floweroutput cntteststructreusemodel.html";
		args=arguments.split("\\s+");
		}
		try {
			tf.runCreation(args);
		} catch (FlowerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void runCreation(String[] args) throws FlowerException {

		DefaultParser parser = new DefaultParser();
		try {
			readOptions(parser.parse(options, args));

		} catch (ParseException e) {
			// TODO Auto-generated catch block

			HelpFormatter formatter = new HelpFormatter();
			System.err.println(e.getMessage());
			formatter.printHelp("java -cp "+Tool.jarName(this)+" " + getClass().getCanonicalName().trim() + " [OPTIONS]",
					options);
			return;
		}
		Flower f = null;
		if (!usestructure || structureoutputfile == null) {
			// FlowerCreator fc = new FlowerCreator(k);
			f = createFlower();
			log().info("JAXB-FLOWER");
			log().info("JAXB-FLOWER");

			log().info("ordering");

			for (Category cat : f.getOrderedCategories()) {
				log().info(cat.getName() + ",");
			}
			log().info("\n");

			for (Connection con : f.getOrderedConnections()) {
				log().info(f.getCategoryById(con.getCat1()).getName() + " - "
						+ f.getCategoryById(con.getCat2()).getName());
				log().info("similarity:" + con.getSimilarity());
			}
			log().info("\n");

			log().info("top general topics");
			for (TopicLink tl : f.getOrderedTopics(f.getGeneral().getTopic(), false)) {
				log().info(f.getTopicById(tl.getTid()).getLable() + ",");
			}

			log().info("\n");
			for (Category cat : f.getOrderedCategories()) {
				log().info(cat.getName() + ", num docs:" + cat.getNumDocs() + "");

				for (TopicLink rtop : f.getOrderedTopics(cat.getTopic(), false)) {
					Topic t = f.getTopicById(rtop.getTid());
					log().info(t.getLable() + "(" + rtop.getTid() + "),");
				}
				log().info("\n");
				Connection con = f.getConnectionByLeadingCatId(cat.getId());
				log().info("Connection:" + f.getLable(con) + "[" + con.getSimilarity() + "]");
				for (TopicLink rtop : f.getOrderedTopics(con.getTopic(), false)) {
					Topic t = f.getTopicById(rtop.getTid());
					log().info(t.getLable() + "(" + t.getTid() + "),");
				}
				log().info("\n");
			}

			// Hashtable<String, Vector<String>> clustered=new Hashtable<String,
			// Vector<String>>();
			Hashtable<String, Hashtable<String, Integer>> clustered = new Hashtable<String, Hashtable<String, Integer>>();

			if (f.getInstances() != null)
				for (Instance ins : f.getInstances().getInstance()) {
					for (TopicLink tl : ins.getTopiclinks()) {
						String key = f.getTopicById(tl.getTid()).getLable();
						Hashtable<String, Integer> curset = clustered.get(key);
						if (curset == null)
							clustered.put(key, curset = new Hashtable<String, Integer>());
						curset.put(ins.getId() + "", tl.getScore().intValue());
					}
				}

			if (this.structureoutputfile != null) {
				JAXBContext jaxbContext;
				try {
					jaxbContext = JAXBContext.newInstance(Flower.class);

					Marshaller marshaller = jaxbContext.createMarshaller();

					OutputStream outputStream = new FileOutputStream(this.structureoutputfile);

					try {
						marshaller.marshal(f, outputStream);
					} finally {
						try {
							outputStream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					// fc.storeModel();

				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					log().error("structure output file could not be stored");
				}
			}
		} else {
			if (structureoutputfile != null) {

				try {
					f = Flower.readFlower(structureoutputfile);
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}
		}

		String title = "flower";
		if (floweroutputfile != null) {
			title = floweroutputfile.getName();
		}
		if (format.equals("png")) {
			visualizeFlower(title, f);
		} else {
			visualzeHTML(title, f);
		}

	}

	private void visualizeFlower(String flowertitle, Flower flower) {

		FlowerImage fi = new FlowerImage(this.petaloptions.get("tcenterlength"), this.petaloptions.get("tcenternr"),
				this.petaloptions.get("tconnectlength"), this.petaloptions.get("tconnectnr"),
				this.petaloptions.get("tpetallength"), this.petaloptions.get("tpetalnr"));

		fi.setFlower(flower);

		fi.paint();
		BufferedImage bi = fi.getFlowerImage();

		try {
			if (floweroutputfile != null)
				ImageIO.write(bi, "png", floweroutputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Flower createFlower() throws FlowerException {

		FlowerPower topicFlower = new FlowerPower(this.k, this.top, this.inputdir, this.backgrounddir,
				this.ldathreadsnum, this.iternumnum, this.usemodel, this.ldamodelfile,stoppwords);// "table_name",
		// number
		// of
		// topics

		Hashtable<Integer, Topic> topics = topicFlower.generateTopicModel();// Boolean
																			// variable
		ArrayList<Term> terms = new ArrayList<Term>();
		terms.addAll(topicFlower.getTermidx().values());// is

		topicFlower.setOrdering(ordering);

		// of
		// no
		// significance. will remove it in
		// the next jar file iteration

		topicFlower.printModel();

		topicFlower.limitDataset(400000);// if the limit is too high it
											// considers all docs

		topicFlower.computeOrdering();

		topicFlower.computeMILists();

		topicFlower.computeMIPairLists();

		General general = new General();

		List<TopicLink> gens = topicFlower.computeTopN(10); // computeTop5()
		general.setTopic(gens);

		DiagramInput di2 = topicFlower.diagramGen();

		HashMap<Integer, String> temp = topicFlower.getIntCategory();

		// ArrayList<de.l3s.graphics.Petal> petals= new ArrayList<Petal>();

		log().info("Ordering: ");

		Flower f = new Flower();
		// topicFlower.

		Topics tops = new Topics();

		for (Integer id : topics.keySet()) {
			tops.addTopic(topics.get(id));
		}

		Categories cats = new Categories();

		int rank = 0;
		for (int i : topicFlower.getOptimalordering())

		{
			log().info(temp.get(i) + ",");
			Category c = new Category();

			c.setName(temp.get(i));
			c.setId(i);
			c.setRank(rank);
			c.setNumDocs(topicFlower.getNumDocs(i));
			cats.addCategory(c);
			rank++;
		}

		log().info("\n");

		Connections cons = new Connections();

		for (String key : di2.getCombiningTopics().keySet())

		{

			Connection con = new Connection();

			ArrayList<TopicLink> list = di2.getCombiningTopics().get(key);

			String[] categoriesinpair = key.split("-");

			ArrayList<TopicLink> t1 = di2.getCategoryRepMap().get(categoriesinpair[0]);

			ArrayList<TopicLink> t2 = di2.getCategoryRepMap().get(categoriesinpair[1]);

			Category cat1 = cats.getCategoryById(Integer.parseInt(categoriesinpair[0]));
			Category cat2 = cats.get(Integer.parseInt(categoriesinpair[1]));

			for (TopicLink to : t1) {

				cat1.addTopiclink(to);

			}

			/*
			 * for (TopicLink to : t2) {
			 * 
			 * cat2.addTopiclink(to);
			 * 
			 * }
			 */

			con.setCat1(Integer.parseInt(categoriesinpair[0]));
			con.setCat2(Integer.parseInt(categoriesinpair[1]));

			con.setSimilarity(topicFlower.getScoremap().get(key));

			for (TopicLink to : list) {
				con.addConnectingtopic(to);
			}

			cons.addConnection(con);
			log().info(temp.get(Integer.parseInt(categoriesinpair[0])));

			log().info(Arrays.toString(t1.toArray()));

			log().info("\n");

			log().info(temp.get(Integer.parseInt(categoriesinpair[0])) + "-"
					+ temp.get(Integer.parseInt(categoriesinpair[1])));

			log().info("similarity score:" + topicFlower.getScoremap().get(key));

			log().info(Arrays.toString(list.toArray()));

			log().info("\n");

			log().info(temp.get(Integer.parseInt(categoriesinpair[1])));

			log().info(Arrays.toString(t2.toArray()));

			log().info("-----------------------------------------------");

			log().info("----------------------Flower:-------------------------");
		}
		Terms t = new Terms();
		t.setTerms(terms);
		f.setTerms(t);
		f.setCategories(cats);
		f.setConnections(cons);
		List<Connection> sorted = new ArrayList<Connection>();
		sorted.addAll(cons.getConnection());

		Collections.sort(sorted, new Comparator<Connection>() {

			@Override
			public int compare(Connection o1, Connection o2) {
				// TODO Auto-generated method stub
				return o1.getSimilarity().compareTo(o2.getSimilarity());
			}

		});
		Connection smallest = sorted.get(0);

		Hashtable<Integer, Integer> orderidx = new Hashtable<Integer, Integer>();

		HashSet<Integer> weakestidx = new HashSet<Integer>();
		weakestidx.add(smallest.getCat1());
		weakestidx.add(smallest.getCat2());

		f.setGeneral(general);
		f.setTopics(tops);

		List<Category> ocats = f.getOrderedCategories();

		Category lastcat = null;
		int firstidx = 0;
		for (int i = 0; i < ocats.size(); i++) {
			Category c = ocats.get(i);
			if (lastcat != null) {
				if (weakestidx.contains(lastcat.getId()) && weakestidx.contains(c.getId())) {
					firstidx = i;
				}
			}
			lastcat = c;
		}

		if (firstidx == ocats.size() - 1)
			firstidx = 0;

		for (int i = 0; i < ocats.size(); i++) {
			int curidx = (i + firstidx) % ocats.size();
			Category curcat = ocats.get(curidx);
			curcat.setRank(i);
		}

		Hashtable<String, int[]> td = topicFlower.getTopicDistribution();
		if (td != null) {
			Instances is = new Instances();
			f.setInstances(is);
			for (String mkey : td.keySet()) {

				Instance ins = new Instance();
				is.getInstance().add(ins);

				int[] idxs = td.get(mkey);
				int cnt = idxs.length;
				ins.setId(mkey);
				for (int topidx : idxs) {
					TopicLink tl = new TopicLink();
					tl.setTid(topidx);
					tl.setScore(cnt-- * 1.);
					ins.add(tl);
				}
			}
		}

		return f;
	}

	private Logger log() {
		// TODO Auto-generated method stub
		return LoggerFactory.getLogger(this.getClass());
	}

	private void visualzeHTML(String flowertitle, Flower flower) {

		StringBuilder sb = new StringBuilder();

		int generaltopicnr = this.petaloptions.get("tcenternr");
		List<TopicLink> top3 = flower.getGeneral().getTopic().subList(0, generaltopicnr);

		// int numWords = 120;

		// int numTopicPerCat = numWords /
		// (flower.getCategories().getCategory().size() * 3);

		flower.getGeneral().setTopic(top3);

		sb = new StringBuilder();
		sb.append("<table style=\" border: 1px solid black;\"><tr>\n");

		sb.append("<td colspan=\"" + flower.getCategories().getCategory().size() + "\">");
		sb.append("<b>" + flowertitle + "</b>");
		sb.append("</td></tr>");
		for (Category c : flower.getOrderedCategories()) {
			sb.append("<td>" + c.getName() + "</td>");
		}

		sb.append("</tr>");
		sb.append("<tr>\n");

		int k = this.petaloptions.get("tpetalnr");
		for (Category c : flower.getOrderedCategories()) {
			List<TopicLink> ot = flower.getOrderedTopics(c.getReprsentativetopic(), false);
			// style='font-size:"+(14-cnt)+"'
			int cnt = 0;
			sb.append("<td >");
			for (TopicLink tl : ot) {
				if (cnt > k) {
					break;
				}

				String topiclable = flower.label(tl, this.petaloptions.get("tpetallength")).trim();
				cnt++;
				if (cnt > 1)
					sb.append(", ");
				sb.append("<span style='font-size:" + (15 - cnt) + "'>" + topiclable + "</span>");
			}
			sb.append("</td>\n");
		}
		sb.append("</tr><td colspan=\"" + flower.getCategories().getCategory().size() + "\">");
		sb.append("<b>General topics: </b>");
		int cnt = 0;

		for (TopicLink tl : flower.getGeneral().getTopic()) {
			if (cnt > this.petaloptions.get("tcenternr")) {
				break;
			}
			String topiclable = flower.label(tl, this.petaloptions.get("tcenterlength")).trim();
			cnt++;
			if (cnt > 1)
				sb.append(", ");
			sb.append("<span style='font-size:" + (15 - cnt) + "'>" + topiclable + "</span>");
		}

		sb.append("</td><tr>\n");
		sb.append("</tr></table>");
		// System.out.println(sb);

		try {
			FileWriter fw = new FileWriter(this.floweroutputfile);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}