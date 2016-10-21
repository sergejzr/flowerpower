package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.l3s.db.DB;
import de.l3s.flower.Categories;
import de.l3s.flower.Category;
import de.l3s.flower.Connection;
import de.l3s.flower.Connections;
import de.l3s.flower.Flower;
import de.l3s.flower.General;
import de.l3s.flower.Instance;
import de.l3s.flower.Instances;
import de.l3s.flower.Term;
import de.l3s.flower.Terms;
import de.l3s.flower.Topic;
import de.l3s.flower.TopicLink;
import de.l3s.flower.Topics;
import newMI.FlowerException;
import newMI.FlowerPower;
import newMI.FlowerPower.OrderStrategy;

public class FlowerCreator {

	private FlowerPower topicFlower;
	private Integer nr_topics_for_instance;
	private OrderStrategy ordering;

	public FlowerCreator(Integer nr_topics_for_instance) {
		this.nr_topics_for_instance=nr_topics_for_instance;
	}
	
	public Flower createFlower(String databaselable, File modelcachedir, String model_tablename, String flower_tablename, Integer topicNr, java.sql.Connection dbcon, int num_iterations, OrderStrategy ordering) throws FlowerException
	{
this.ordering=ordering;
		if(modelcachedir!=null && !modelcachedir.exists()){modelcachedir.mkdirs();}
		 topicFlower = new FlowerPower(databaselable,modelcachedir,model_tablename
				// "dataset_20newsgroup_full"
				//"conference_proceedings_www_clean_new"
				, flower_tablename,topicNr,dbcon,nr_topics_for_instance, num_iterations);// "table_name",
															// number of topics

		Hashtable<Integer, Topic> topics =topicFlower.generateTopicModel();// Boolean
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
		
		General general=new General();
		

		List<TopicLink> gens = topicFlower.computeTopN(10); //computeTop5()
		general.setTopic(gens);

		DiagramInput di2 = topicFlower.diagramGen();

		HashMap<Integer, String> temp = topicFlower.getIntCategory();

		// ArrayList<de.l3s.graphics.Petal> petals= new ArrayList<Petal>();

		System.out.println("Ordering: ");

		Flower f = new Flower();
		//topicFlower.
		



		Topics tops = new Topics();

		for (Integer id : topics.keySet()) {
			tops.addTopic(topics.get(id));
		}

		Categories cats = new Categories();

		int rank = 0;
		for (int i : topicFlower.getOptimalordering())

		{
			System.out.print(temp.get(i) + ",");
			Category c = new Category();
			
			c.setName(temp.get(i));
			c.setId(i);
			c.setRank(rank);
			c.setNumDocs(topicFlower.getNumDocs(i));
			cats.addCategory(c);
			rank++;
		}

		System.out.println();

		Connections cons = new Connections();

		for (String key : di2.getCombiningTopics().keySet())

		{

			Connection con = new Connection();

			ArrayList<TopicLink> list = di2.getCombiningTopics().get(key);

			String[] categoriesinpair = key.split("-");

			ArrayList<TopicLink> t1 = di2.getCategoryRepMap().get(
					categoriesinpair[0]);
			
	

			
			ArrayList<TopicLink> t2 = di2.getCategoryRepMap().get(
					categoriesinpair[1]);

			Category cat1 = cats.getCategoryById(Integer.parseInt(categoriesinpair[0]));
			Category cat2 = cats.get(Integer.parseInt(categoriesinpair[1]));

			
			for (TopicLink to : t1) {
				
			
				cat1.addTopiclink(to);

			}

			/*
			for (TopicLink to : t2) {
				
				cat2.addTopiclink(to);

			}*/

			con.setCat1(Integer.parseInt(categoriesinpair[0]));
			con.setCat2(Integer.parseInt(categoriesinpair[1]));

			
			con.setSimilarity(topicFlower.getScoremap().get(key));

		

			
		
		
			for (TopicLink to : list) {				
				con.addConnectingtopic(to);
			}

				
cons.addConnection(con);
System.out.println(temp.get(Integer.parseInt(categoriesinpair[0])));

System.out.println(Arrays.toString(t1.toArray()));

System.out.println();

System.out.println(temp.get(Integer.parseInt(categoriesinpair[0]))
		+ "-" + temp.get(Integer.parseInt(categoriesinpair[1])));

System.out.println("similarity score:"
		+ topicFlower.getScoremap().get(key));

System.out.println(Arrays.toString(list.toArray()));




System.out.println();

System.out.println(temp.get(Integer.parseInt(categoriesinpair[1])));

System.out.println(Arrays.toString(t2.toArray()));

System.out
		.println("-----------------------------------------------");

System.out
.println("----------------------Flower:-------------------------");
		}
		Terms t=new Terms();
		t.setTerms(terms);
		f.setTerms(t);
		f.setCategories(cats);
		f.setConnections(cons);
		List<Connection> sorted=new ArrayList<Connection>();
		sorted.addAll(cons.getConnection());
		
		
		
		Collections.sort(sorted,new Comparator<Connection>() {

			@Override
			public int compare(Connection o1, Connection o2) {
				// TODO Auto-generated method stub
				return o1.getSimilarity().compareTo(o2.getSimilarity());
			}
			
		});
		Connection smallest = sorted.get(0);
		
		Hashtable<Integer, Integer> orderidx=new Hashtable<Integer, Integer>();

	
		HashSet<Integer> weakestidx=new HashSet<Integer>();
		weakestidx.add(smallest.getCat1());
		weakestidx.add(smallest.getCat2());
		
		
		
		f.setGeneral(general);
		f.setTopics(tops);
		
		List<Category> ocats = f.getOrderedCategories();
		
		Category lastcat=null;
	int firstidx=0;
		for(int i=0;i<ocats.size();i++)
		{
			Category c = ocats.get(i);
			if(lastcat!=null)
			{
				if(weakestidx.contains(lastcat.getId())&&weakestidx.contains(c.getId()))
				{
					firstidx=i;
				}
			}
			lastcat=c;
		}
		
		if(firstidx==ocats.size()-1)firstidx=0;
		
		for(int i=0;i<ocats.size();i++)
		{
			int curidx=(i+firstidx)%ocats.size();
			Category curcat = ocats.get(curidx);
			curcat.setRank(i);	
		}
		
		Hashtable<String, int[]> td = topicFlower.getTopicDistribution();
		if(td!=null)
		{
			Instances is=new Instances();
		f.setInstances(is);
			for(String mkey:td.keySet())
			{
				
				Instance ins=new Instance();
				is.getInstance().add(ins);
		
		int[] idxs = td.get(mkey);
		int cnt=idxs.length;
		ins.setId(mkey);
			for(int topidx:idxs)
			{
				TopicLink tl = new TopicLink();
			tl.setTid(topidx);
			tl.setScore(cnt--*1.);
				ins.add(tl);
			}
		}
		}

	return f;
	}
	public static void main(String[] args) throws JAXBException, IOException {
		

		try {
			
			FlowerCreator fc=new FlowerCreator(null);
			String tabname=//"auto5000_descriptions"
					"flower_wikimovies_nopersons_copy"
					//"rowtopocs_wikimovies"
					//"dataset_20newsgroup_full"
					//"conference_proceedings_chi_clean"
					//"flower_emotions_clean"
					//"conference_proceedings_paragraphs"
					//"conference_proceedings_www_clean_new"
					;
					Integer numtopics=200;
			OrderStrategy ordering=OrderStrategy.optimalOrderung;
			Flower f = fc.createFlower( "localhost",null,tabname
					, tabname,
					numtopics,//new File("rowtopocs_wikimovies"+"_"+numtopics+".dat")
					null,2000, ordering
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
				System.out.println("sinilarity:"+con.getSimilarity());
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
			
			//System.out.println(f);
			JAXBContext jaxbContext = JAXBContext.newInstance(Flower.class);
			 Marshaller marshaller = jaxbContext.createMarshaller();
			 OutputStream outputStream = new FileOutputStream(new File(tabname+"_"+numtopics+".xml"));
			 try {
			     marshaller.marshal(f, outputStream);
			 } finally {
			     outputStream.close();
			 }
			 
		} catch (FlowerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
	private Flower getCached(String databaselable, String tablename,Integer nrtopics, FlowerCreator fc) throws JAXBException, IOException, ClassNotFoundException, SQLException, FlowerException {

		File cachedir=new File("/data3/zerr/flowers");
		File cacheflower=new File(tablename+"-"+nrtopics+"_flower.xml");
		if(cachedir.exists()){
		 cacheflower=new File(cachedir, tablename+"-"+nrtopics+"_flower.xml");
		}
		Flower flower = null;
		JAXBContext jaxbContext = JAXBContext.newInstance(Flower.class);
		if(cacheflower.exists())
		{
			 
		        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		       
		        flower = (Flower)jaxbUnmarshaller.unmarshal(cacheflower);

		        
		}else{
			Class.forName("com.mysql.jdbc.Driver"); 
			java.sql.Connection dbcon = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
			
		
		// flower = fc.createFlower("auto5000_descriptions", 50,dbcon);
			 flower = fc.createFlower(databaselable,null, tablename, tablename,nrtopics,dbcon,1000, ordering);
		 Marshaller marshaller = jaxbContext.createMarshaller();
		 OutputStream outputStream = new FileOutputStream(cacheflower);
		 try {
		     marshaller.marshal(flower, outputStream);
		 } finally {
		     outputStream.close();
		 }
		}
		return flower;
	}

	public void storeModel() {
		topicFlower.storeModel();
		
	}	
}
