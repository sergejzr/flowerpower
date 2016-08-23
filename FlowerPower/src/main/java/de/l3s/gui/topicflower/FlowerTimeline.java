package de.l3s.gui.topicflower;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import de.l3s.flower.Category;
import de.l3s.flower.Flower;
import de.l3s.flower.TopicLink;

public class FlowerTimeline {

	String flowertitle;
	private StringBuilder sb;

	public FlowerTimeline(File dir, String flowerxml) {

		flowertitle = flowerxml.substring(0, flowerxml.length() - 4);

		this.flowertitle = flowertitle;

		Flower flower;
		try {
			flower = Flower.readFlower(new File(dir, flowerxml));
			List<TopicLink> top3 = flower.getGeneral().getTopic().subList(0, 2);

			int numWords = 120;

			int numTopicPerCat = numWords / (flower.getCategories().getCategory().size() * 3);

			flower.getGeneral().setTopic(top3);

			 sb = new StringBuilder();
			sb.append("<table style=\" border: 1px solid black;\"><tr>\n");
			

			sb.append("<td colspan=\""+flower.getCategories().getCategory().size()+"\">");
			sb.append("<b>"+flowerxml+"</b>");
			sb.append("</td></tr>");
			for (Category c : flower.getOrderedCategories()) {
				sb.append("<td>" + c.getName() + "</td>");
			}

			sb.append("</tr>");
			sb.append("<tr>\n");
			int k = 3;
			for (Category c : flower.getOrderedCategories()) {
				List<TopicLink> ot = flower.getOrderedTopics(c.getReprsentativetopic(), false);

				int cnt = 0;
				sb.append("<td>");
				for (TopicLink tl : ot) {
					if (cnt > k) {
						break;
					}
					
					String topiclable = flower.label(tl, 3).trim();
					cnt++;
					if(cnt>1)
					sb.append(", ");
					sb.append(topiclable);
				}
				sb.append("</td>\n");
			}
			sb.append("</tr><td colspan=\""+flower.getCategories().getCategory().size()+"\">");
			sb.append("<b>General topics: </b>");
			int cnt = 0;
			for (TopicLink tl : flower.getGeneral().getTopic()) {
				if (cnt > k) {
					break;
				}
				String topiclable = flower.label(tl, 3).trim();
				cnt++;
				if(cnt>1)
				sb.append(", ");
				sb.append(topiclable);
			}
			
			sb.append("</td><tr>\n");
			sb.append("</tr></table>");
		//	System.out.println(sb);

		} catch (JAXBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
String getHTML(){return sb.toString();}



	public static String createTable(File dir,String xmlfile) {

		

			FlowerTimeline ftl=new FlowerTimeline(dir, xmlfile);
			return ftl.getHTML();


	}

	public static void main(String[] args) {
		File dir = new File("/media/zerr/BA0E0E3E0E0DF3E3/floweryak/");
		if (!dir.exists()) {
			dir = new File("/data3/zerr/autoflowersvilnus");
			dir = new File("/data3/zerr/flowers");
		}
		if (!dir.exists()) {
			dir = new File("D:\\autoflowers\\newautoflowers");
		}
		StringBuilder overviewhtml=new StringBuilder();
		for (File cf : dir.listFiles()) {
			if (!cf.toString().endsWith("xml")) {
				continue;
			}

		String html = createTable(dir,cf.getName());
		FileWriter fw;
		try {
			fw = new FileWriter(new File(cf.getParentFile(),cf.getName()+".html"));
			fw.write(html);
			fw.close();
			overviewhtml.append("<a href='"+cf.getName()+".html"+"'>"+cf.getName()+".html"+"</a><br/>\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
			/*
			 * // if(!cf.toString().contains("christianity")){continue;} //
			 * if(!cf.toString().contains("1000")){continue;}
			 * //if(!cf.toString().contains(
			 * "flower_wikimovies_nopersons_1000.xml")){continue;}
			 * if(cf.toString().contains("auto5000")){continue;} //
			 * if(!cf.toString().contains("rowtopocs_wikimovies")||!cf.toString(
			 * ).contains("_500")){continue;} //
			 * if(!cf.toString().contains("auto5000")||!cf.toString().contains(
			 * "_500")){continue;}
			 * 
			 * FlowerFrame f=new FlowerFrame(dir,cf.getName()
			 * //"flower_wikimovies_nopersons_500.xml"
			 * 
			 * ); //f.convert(flowerxml_in, flowerimg_outdir)
			 * 
			 * //f.getContentPane().add(fp,BorderLayout.CENTER);
			 * f.setBounds(100, 100, 500, 500); f.setVisible(true); break;
			 */
		}
		try {
			FileWriter fw = new FileWriter(new File(dir,"floweroverview.html"));
			fw.write(overviewhtml.toString());
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
