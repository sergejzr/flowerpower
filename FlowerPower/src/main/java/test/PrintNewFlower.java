package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import de.l3s.flower.Categories;
import de.l3s.flower.Category;
import de.l3s.flower.Connection;
import de.l3s.flower.Connections;
import de.l3s.flower.Flower;
import de.l3s.flower.General;
import de.l3s.flower.Topic;
import de.l3s.flower.TopicLink;
import de.l3s.flower.Topics;
import newMI.FlowerException;
import newMI.FlowerPower;

public class PrintNewFlower {

	public static void main(String[] args) {

															// number of topics

		try {
			FlowerPower topicFlower = new FlowerPower("loclahost", null,"auto5000_descriptions"
					// "dataset_20newsgroup_full"
					//"conference_proceedings_www_clean_new"
					, 50,null);// "table_name",
																// number of topics

			Hashtable<Integer, Topic> topics = topicFlower.generateTopicModel();// Boolean
																				// variable
																				// is
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
			

			List<TopicLink> gens = topicFlower.computeTop5();
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

				Category cat1 = cats.get(Integer.parseInt(categoriesinpair[0]));
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

				System.out.println(temp.get(Integer.parseInt(categoriesinpair[0])));

				System.out.println(Arrays.toString(t1.toArray()));

				System.out.println();

				System.out.println(temp.get(Integer.parseInt(categoriesinpair[0]))
						+ "-" + temp.get(Integer.parseInt(categoriesinpair[1])));

				System.out.println("similarity score:"
						+ topicFlower.getScoremap().get(key));
				con.setSimilarity(topicFlower.getScoremap().get(key));

				System.out.println(Arrays.toString(list.toArray()));

				
			
			
				for (TopicLink to : list) {				
					con.addConnectingtopic(to);
				}

				System.out.println();

				System.out.println(temp.get(Integer.parseInt(categoriesinpair[1])));

				System.out.println(Arrays.toString(t2.toArray()));

				System.out
						.println("-----------------------------------------------");
				
				System.out
				.println("----------------------Flower:-------------------------");
				
	cons.addConnection(con);
			}
			f.setCategories(cats);
			f.setConnections(cons);
			f.setGeneral(general);
			f.setTopics(tops);
			/*
			System.out
			.println(f.toString());
			*/
			
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
						System.out.print(t.getLable()+",");
					}
					System.out.println();
				}
			 

		} catch (FlowerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// Boolean
																			// variable
																			// is
		
		 

	}
}
