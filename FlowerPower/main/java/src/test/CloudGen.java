package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Span;

import de.l3s.flower.Category;
import de.l3s.flower.Flower;
import de.l3s.flower.Term;
import de.l3s.flower.TermLink;
import de.l3s.flower.Topic;
import de.l3s.flower.TopicLink;

public class CloudGen {

	Flower flower;
	Cloud tagCloud = new Cloud();
	String tagCloudHTML = new String();
	Cloud topicCloud = new Cloud();
	String topicCloudHTML = new String();
	HashMap<String, Cloud> CategoryFlowers = new HashMap<String, Cloud>();
	HashMap<String, String> CategoryFlowersHTML = new HashMap<String, String>();
	HashMap<String, String> CategoryFlowersText = new HashMap<String, String>();
	ArrayList<String> keys = new ArrayList<String>();

	public ArrayList<String> getKeys() {
		return keys;
	}

	public void setKeys(ArrayList<String> keys) {
		this.keys = keys;
	}

	public HashMap<Integer, String> TermIDMap = new HashMap<Integer, String>();
	private String tagCloudText;
	private String topicCloudText;

	public Cloud getTagCloud() {
		return tagCloud;
	}

	public void setTagCloud(Cloud tagCloud) {
		this.tagCloud = tagCloud;
	}

	public String getTagCloudText() {
		return tagCloudText;
	}
	public String getTopicCloudText() {
		return topicCloudText;
	}
	public String getTagCloudHTML() {
		return tagCloudHTML;
	}

	public void setTagCloudHTML(String tagCloudHTML) {
		this.tagCloudHTML = tagCloudHTML;
	}

	public Cloud getTopicCloud() {
		return topicCloud;
	}

	public void setTopicCloud(Cloud topicCloud) {
		this.topicCloud = topicCloud;
	}

	public String getTopicCloudHTML() {
		return topicCloudHTML;
	}

	public void setTopicCloudHTML(String topicCloudHTML) {
		this.topicCloudHTML = topicCloudHTML;
	}

	public HashMap<String, Cloud> getCategoryFlowers() {
		return CategoryFlowers;
	}

	public void setCategoryFlowers(HashMap<String, Cloud> categoryFlowers) {
		CategoryFlowers = categoryFlowers;
	}

	public HashMap<String, String> getCategoryFlowersText() {
		return CategoryFlowersText;
	}
	public void setCategoryFlowersText(
			HashMap<String, String> categoryFlowersText) {
		CategoryFlowersText = categoryFlowersText;
	}
	public HashMap<String, String> getCategoryFlowersHTML() {
		return CategoryFlowersHTML;
	}

	public void setCategoryFlowersHTML(
			HashMap<String, String> categoryFlowersHTML) {
		CategoryFlowersHTML = categoryFlowersHTML;
	}

	public HashMap<Integer, String> getTermIDMap() {
		return TermIDMap;
	}

	public void setTermIDMap(HashMap<Integer, String> termIDMap) {
		TermIDMap = termIDMap;
	}

	public CloudGen(Flower f) {

		flower = f;
		buildTermIndex();
		computeCategoryTermCloud();
		computeTagCloud();
		computeTopicCloud();
	}

	public static void main(String[] args) {

	}

	public static Flower readFlower(File flowerfile) throws JAXBException {
		Flower flower = null;
		JAXBContext jaxbContext = JAXBContext.newInstance(Flower.class);
		if (flowerfile.exists()) {

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			flower = (Flower) jaxbUnmarshaller.unmarshal(flowerfile);

		} else {
		}
		return flower;
	}

	public void buildTermIndex() {
		TermIDMap = new HashMap<Integer, String>();
		for (Term term : flower.getTerms().getTerms()) {
			TermIDMap.put(term.getTid(), term.getValue());
		}
		System.out.println("term index built");
	}

	public void computeTopicCloud() {
		Cloud cloud = new Cloud();
		cloud.setMaxWeight(45.0);

		int count = 0;
		for (Category c : flower.getCategories().getCategory()) {
			count += c.getNumDocs();
		}
		for (Topic t : flower.getTopics().getTopic()) {
			cloud.addTag(new Tag(t.getLable(), (int) (count * t.getScore())));
			//System.out.println(t.getLable().trim().replaceAll(" ", "-")+":"+count * t.getScore());
		}
		/*
		System.out.println("\n\n\n\n---\n");
		for (Topic t : flower.getTopics().getTopic()) {
			//cloud.addTag(new Tag(t.getLable().replaceAll(" ", "_"), (int) (count * t.getScore())));
			System.out.println(t.getLable().trim().replaceAll(" ", "-")+":"+Math.round(count * t.getScore()));
		}*/
		System.out.println("\n\n\n\n---\n");
		for (Topic t : flower.getTopics().getTopic()) {
			//cloud.addTag(new Tag(t.getLable().replaceAll(" ", "_"), (int) (count * t.getScore())));
			System.out.println(t.getLable().trim()+":"+t.getScore());
		}
		topicCloud = cloud;
		topicCloudHTML = getHTML(cloud);
		System.out.println("Topiccloud:");
		topicCloudText=getText( cloud);
		System.out.println(topicCloudText);
	}

	public String getHTML(Cloud cloud) {
		Div div = new Div();
		int c = 1;
		div.setStyle("width: 600px;word-break: break-word;");
		if (cloud.tags().get(0).getNormScoreInt() < 0)
			c = 10;
		for (Tag t : cloud.tags()) {
			A linktag = new A();
			linktag.setHref("#");

			linktag.setStyle("font-size:" + t.getWeightInt()
					+ "pt; padding: 4px;");
			linktag.appendText(t.getName());
			div.appendChild(linktag);
			Span span = new Span();
			span.setStyle("width: 15px;");
			div.appendChild(span);
		}
		System.out.println(div.write());
		return div.write();
	}

	public void computeCategoryTermCloud() {
		System.out.println("Cattermclouds");
		for (Category c : flower.getOrderedCategories()) {
			Cloud cloud = new Cloud();
			cloud.setMaxWeight(45.0);

			keys.add(c.getName());
			cloud.setMaxWeight(45.0);
			for (TopicLink tl : c.getTopic()) {
				Topic topic = flower.getTopicById(tl.getTid());
				for (TermLink term : topic.getTerm()) {

					Tag tag = new Tag(TermIDMap.get(term.getTid()),
							term.getScore());// this is

					cloud.addTag(tag);
				}

			}
			CategoryFlowers.put(c.getName(), cloud);
			CategoryFlowersHTML.put(c.getName(), getHTML(cloud));
			
			String curcat;
			System.out.println("\t"+c.getName());
			CategoryFlowersText.put(c.getName(), curcat=getText(cloud));
			System.out.println("\t"+curcat);
		}

	}

	public void computeTagCloud() {
		// term - doc freq
		Cloud cloud = new Cloud();
		cloud.setMaxWeight(45.0);

		for (Category c : flower.getCategories().getCategory()) {

			for (TopicLink tl : c.getTopic()) {
				Topic topic = flower.getTopicById(tl.getTid());
				for (int i = 0; i < 30; i++) {
					TermLink t = topic.getTerm().get(i);
					int count = (int) (0 + t.getScore());

					String term = TermIDMap.get(t.getTid());

					Tag tag = new Tag(term, count);
					cloud.addTag(tag);

				}

			}
		}
		tagCloud = cloud;
		tagCloudHTML = getHTML(cloud);
		System.out.println("TagCloudvis:");
		
		tagCloudText=getText(cloud);
		System.out.println(tagCloudText);
	}
Hashtable<String, String> coloursidx=new Hashtable<String, String>();
	private String getText(Cloud cloud) {
	
		int c = 1;
	
		if (cloud.tags().get(0).getNormScoreInt() < 0)
			c = 10;
		StringBuilder res=new StringBuilder();
		Vector<Tag> sorted=new Vector<Tag>();
		for (Tag t : cloud.tags()) {
			
			String color=coloursidx.get(t.getName());
			if(color==null){coloursidx.put(t.getName(), color=selectrandomcolor());}
			if(res.length()>0)res.append("\n");
			res.append(t.getName()+":"+t.getScore()+":"+color);
			sorted.add(t);
		}
		
		Collections.sort(sorted,new Comparator<Tag>(){

			@Override
			public int compare(Tag t1, Tag t2) {
				// TODO Auto-generated method stub
				return Double.compare( t1.getScore(),  t2.getScore())*-1;
			}});
		
		res.append("\n\n\n\n 10 sorted:\n");
		for(int i=0;i<sorted.size()&&i<10;i++)
		{
			res.append(sorted.get(i).getName()+"\t"+sorted.get(i).getScore()+"\n");
		}
		return res.toString();
	}

	private String selectrandomcolor() {
		
		Vector<String> shuf=new Vector<String>();
		shuf.addAll(Arrays.asList("481d2d,953f12,1f1040,140b26,682921".toUpperCase().split(",")));
		Collections.shuffle(shuf);
		return shuf.get(0);
	}

	public Flower getFlower() {
		return flower;
	}

	public void setFlower(Flower flower) {
		this.flower = flower;
	}

}
