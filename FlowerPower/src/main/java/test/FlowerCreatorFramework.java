package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import newMI.FlowerPower.OrderStrategy;

public class FlowerCreatorFramework {
public static void main(String[] args) {
	
	if(args.length<4)
	{
		System.out.println("Usage arguments: table topicnr1,topicnr2,.. order{natural|optimal} databaselable cachedir modeldir");
		return;
	}
	

	ArrayList<Integer> topicnrs=new ArrayList<Integer>();
	for(String s:args[1].split(","))
	{
		topicnrs.add(Integer.parseInt(s));
	}
	
	
	String datasets[]=new String[]{
			/*
			"flower_christianity:flower_christianity_background",
			"flower_angelamerkel:flower_angelamerkel_background",
			"flower_socialism:flower_socialism_background",
			"flower_anarchism:flower_anarchism_background",
			*/
			args[0],
		
			
			
			/*
			"flower_nuclearpower:flower_nuclearpower_background",
			"flower_globalwarming:flower_globalwarming_background",
			"flower_vladimirputin:flower_vladimirputin_background",
			
			*/
			
			//"flower_muhammad:flower_muhammad_background",
			//"flower_jesus:flower_jesus_background",
			//"flower_scientology:flower_scientology_background",
			
			
			//"flower_vilnus:flower_vilnus_background",
			//"flower_georgebush:flower_georgebush_background"
		//	"flower_wikimovies_nopersons",
			
		//	"flower_streetartdesc:flower_streetartdesc_cropping",
		//	"flower_streetart:flower_streetart_cropping",
	//	"flower_streetartdesc",
	//		"flower_streetart",
			/*
			"flower_streetart:flower_streetart_cropping_full",
			"conference_proceedings_clean",
			"auto5000_descriptions:flower_fulltext_sections_auto5000",
			"flower_wikimovies_nopersons",
			"conference_proceedings_www_clean_new",
			"conference_proceedings_chi_clean",
			"flower_streetartdesc"*/
			//"flower_streetartdesc:flower_streetart_croppingdesc"
			//"flower_werft_par:flower_cropped_werft"
			//"auto5000_descriptions:flower_fulltext_auto5000"
			//"auto5000_descriptions:flower_fulltext_sections_auto5000"
			//"auto5000_wiki_descriptions",
			//"auto5000_descriptions",

			//"dataset_20newsgroup_full",
			//"flower_wikimovies_clean",
			//"flower_emotions_clean",
			//"conference_proceedings_clean",
			
			//"flower_wikimovies_nopersons",
			/*
			"flower_werft_par:flower_cropped_werft",
			"auto5000_descriptions:flower_fulltext_sections_auto5000",
			"flower_wikimovies_clean",
			"rowtopocs_wikimovies",
			"dataset_20newsgroup_full",
			"conference_proceedings_chi_clean",
			"flower_emotions_clean",
			"conference_proceedings_clean",
			"flower_wikimovies_nopersons",
>>>>>>> .r2613
			"conference_proceedings_www_clean_new",
<<<<<<< .mine
			
			
			//"flower_werft_par:flower_cropped_werft",
			//"auto5000_descriptions:flower_fulltext_sections_auto5000",
			//"flower_wikimovies_clean",
			//"rowtopocs_wikimovies",
			//"dataset_20newsgroup_full",
			//"conference_proceedings_chi_clean",
			//"flower_emotions_clean",
			


*/
			//"flower_wikimovies_nopersons",


			
			
			//"flower_wikimovies_nopersons_adj_verbs",
			//"auto5000_descriptions"
			//"flower_cropped_auto5000_joint"
			//"flower_cropped_auto5000"
			};
	
	Hashtable<String, String> infos=new Hashtable<String, String>();
	for(String dataset:datasets)
	{
		String[] pair = dataset.split(":");
		
		if(pair.length==1)
		{
			infos.put(pair[0], pair[0]);
		}else if(pair.length==2)
		{
			infos.put(pair[0], pair[1]);
		}
	}
/*	
	try {
		Thread.sleep(1000*60*60*10);
	} catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	*/
	ExecutorService pool = Executors.newFixedThreadPool(12);
	OrderStrategy ordering = OrderStrategy.naturalOrdering;
	
	if(args[2].trim().toLowerCase().equals("natural"))
	{
		ordering=OrderStrategy.naturalOrdering;
	} else if(args[2].trim().toLowerCase().equals("optimal"))
	{
		ordering=OrderStrategy.optimalOrderung;
	}
	String databaselable=args[3];
	for(String flower_table:infos.keySet())
	{
		String model_table=infos.get(flower_table);
		
	

		for(Integer i:topicnrs)

		{
			pool.execute(new FlowerThread(databaselable,new File(args[5]), 
					new File(args[6]),//"flower_cropped_auto5000_joint"
					model_table,flower_table,i, 3, 2000, Integer.parseInt(args[4]), ordering
					));
		}
	
	}
	
	 pool.shutdown();
	 try {
		pool.awaitTermination(7, TimeUnit.DAYS);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

}
}
