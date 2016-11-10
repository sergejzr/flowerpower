package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import de.l3s.flower.Category;
import de.l3s.flower.Connection;
import de.l3s.flower.Flower;
import de.l3s.flower.Instance;
import de.l3s.flower.Topic;
import de.l3s.flower.TopicLink;
import de.l3s.source.DataSource;
import newMI.FlowerPower.OrderStrategy;

public class FlowerThread implements Runnable {
	private File cachedir;

	private File modelcachedir;
	private String modeltouse;
	private Integer numtopics;
	private String flower_table;
	private File filetostore=null;

	private String model_table;

	private Integer nr_topics_for_instance;

	private int num_iterations;

	private OrderStrategy ordering;

	private DataSource databaselable;

	private Integer numthreads;


	public FlowerThread(DataSource databaselable2, File cachedir, File modelcachedir, String model_table, String flower_table,Integer numtopics, Integer nr_topics_for_instance, int num_iterations, Integer numthreads,OrderStrategy ordering)
	{
		this.databaselable=databaselable2;
		this.cachedir=cachedir;
		this.modelcachedir=modelcachedir;
		this.modeltouse=model_table;
		this.flower_table=flower_table;
		this.model_table=model_table;
		this.numtopics=numtopics;
		this.nr_topics_for_instance=nr_topics_for_instance;
		this.num_iterations=num_iterations;
		this.ordering=ordering;
		this.numthreads=numthreads;
	}
@Override
public void run() {
	

	try{
		
		
		
String chunk=flower_table.equals(model_table)?flower_table:flower_table+"_"+model_table;

		 filetostore=new File(cachedir, chunk+"_"+numtopics+".xml");
		
		if(filetostore.exists()){return;}
		FlowerCreator fc=new FlowerCreator(nr_topics_for_instance);


	Flower f = fc.createFlower( databaselable,modelcachedir,modeltouse
	, flower_table,numtopics,null,num_iterations,numthreads,
 ordering
	//new File("rowtopocs_wikimovies"+"_"+numtopics+".dat")
	
	);
	System.out.println("JAXB-FLOWER");

	System.out.println("ordering");

	for(Category cat:f.getOrderedCategories())
	{
	System.out.print(cat.getName()+",");	
	}
	System.out.println();

	for(Connection con:f.getOrderedConnections())
	{
	System.out.println(f.getCategoryById(con.getCat1()).getName()+" - "+f.getCategoryById(con.getCat2()).getName());
	System.out.println("similarity:"+con.getSimilarity());
	}
	System.out.println();


	System.out.println("top general topics");
	for(TopicLink tl: f.getOrderedTopics(f.getGeneral().getTopic(),false))
	{
	System.out.print(f.getTopicById(tl.getTid()).getLable()+",");
	}

	System.out.println();
	for(Category cat:f.getOrderedCategories())
	{
	System.out.println(cat.getName()+", num docs:"+cat.getNumDocs()+"");


	for(TopicLink rtop : f.getOrderedTopics(cat.getTopic(),false))
	{
		Topic t = f.getTopicById(rtop.getTid());
		System.out.print(t.getLable()+"("+rtop.getTid()+"),");
	}
	System.out.println();
	Connection con=f.getConnectionByLeadingCatId(cat.getId());
	System.out.println("Connection:"+f.getLable(con)+"["+con.getSimilarity()+"]");
	for(TopicLink rtop : f.getOrderedTopics(con.getTopic(),false))
	{
		Topic t = f.getTopicById(rtop.getTid());
		System.out.print(t.getLable()+"("+t.getTid()+"),");
	}
	System.out.println();
	}

	//Hashtable<String, Vector<String>> clustered=new Hashtable<String, Vector<String>>();
	Hashtable<String, Hashtable<String, Integer>> clustered=new Hashtable<String, Hashtable<String,Integer>>();
	
	if(f.getInstances()!=null)
	for(Instance ins:f.getInstances().getInstance())
	{
		for(TopicLink tl:ins.getTopiclinks())
		{
			String key = f.getTopicById(tl.getTid()).getLable();
			Hashtable<String, Integer> curset = clustered.get(key);
			if(curset==null) clustered.put(key,curset=new Hashtable<String, Integer>());
			curset.put(ins.getId()+"",tl.getScore().intValue());
		}
	}
	
	//printastable(clustered);
	//System.out.println(f);
	JAXBContext jaxbContext = JAXBContext.newInstance(Flower.class);
	Marshaller marshaller = jaxbContext.createMarshaller();
	if(!cachedir.exists())cachedir.mkdirs();
	OutputStream outputStream = new FileOutputStream(filetostore);
	
	try {
	marshaller.marshal(f, outputStream);
	} finally {
	outputStream.close();
	}

	fc.storeModel();
	
	}catch(Exception e )
	{e.printStackTrace();}
	catch(Throwable t)
	{
		t.printStackTrace();
	}
	

	
	
}
private static void printastable(
		Hashtable<String, Hashtable<String, Integer>> catidx) {
	Hashtable<String, List<Entry<String, Integer>>> sortedidx = new Hashtable<String, List<Entry<String, Integer>>>();
	Integer maxlength = 0;
	for (String cat : catidx.keySet()) {
		Vector<Entry<String, Integer>> sorted = new Vector<Entry<String, Integer>>();

		sorted.addAll(catidx.get(cat).entrySet());
		Collections.sort(sorted, new TagFreqComparator());
		sortedidx.put(cat, sorted);
		if (maxlength < sorted.size())
			maxlength = sorted.size();
	}
	StringBuilder header = new StringBuilder();
	for (String cat : sortedidx.keySet()) {
		if (header.length() > 0)
			header.append("\t");
		header.append(cat);
	}
	FileWriter fw;
	try {
		fw = new FileWriter(new File("csvinput/autocluster.csv"));
		fw.write(header.toString()+"\n");
	for (int i = 0; i < maxlength; i++) {
		StringBuilder line = new StringBuilder();
		for (String cat : sortedidx.keySet()) {
			if (line.length() > 0)
				line.append("\t");

			List<Entry<String, Integer>> curlist = sortedidx.get(cat);

			if (curlist != null && curlist.size() > i) {
				Entry<String, Integer> entry = curlist.get(i);
				line.append(entry.getKey());
			} else {
				line.append("\t");
			}

		}
		
		
		fw.write(line.toString()+"\n");
	}
	
		
		fw.flush();
		fw.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}
