package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import de.l3s.flower.TopicLink;
import newMI.FlowerPower;

public class PrintFlower {

	public static void main(String[] args) {
		FlowerPower topicFlower = new FlowerPower( null,"flower_wikimovies_clean"
				// "dataset_20newsgroup_full"
				//"conference_proceedings_www_clean_new"
				, 100,null);// "table_name",
															// number of topics

		topicFlower.generateTopicModel();// Boolean
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
		
		
		DiagramInput di2 = topicFlower.diagramGen();

		HashMap<Integer, String> temp = topicFlower.getIntCategory();

		// ArrayList<de.l3s.graphics.Petal> petals= new ArrayList<Petal>();

		System.out.println("Ordering: ");




	

		int rank = 0;
		for (int i : topicFlower.getOptimalordering())

		{
			System.out.print(temp.get(i) + ",");
			
			rank++;
		}

		System.out.println();

	

		for (String key : di2.getCombiningTopics().keySet())

		{

		
			ArrayList<TopicLink> list = di2.getCombiningTopics().get(key);

			String[] categoriesinpair = key.split("-");

			ArrayList<TopicLink> t1 = di2.getCategoryRepMap().get(
					categoriesinpair[0]);
			
	

			
			ArrayList<TopicLink> t2 = di2.getCategoryRepMap().get(
					categoriesinpair[1]);

			
			

			
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

	}
}
