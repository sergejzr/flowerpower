package test;

import java.util.ArrayList;
import java.util.HashMap;

import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

import de.l3s.flower.Connection;
import de.l3s.flower.Flower;
import de.l3s.flower.Term;
import de.l3s.flower.TermLink;
import de.l3s.flower.Topic;
import de.l3s.flower.TopicLink;

public class CosineTopic {

	public static void buildTermIndex(Flower flower) {
		TermIDMap = new HashMap<Integer, String>();
		for (Term term : flower.getTerms().getTerms()) {
			TermIDMap.put(term.getTid(), term.getValue());
		}
		System.out.println("term index built");

	}
	class TermMI implements Comparable {
		public String term;
		public double value;

		public TermMI(String term, double value) {
			this.term = term;
			this.value = value;
		}

		public int compareTo(Object o) {
			TermMI tm = (TermMI) o;
			if (this.value < tm.value) {
				return 1;
			} else if (this.value > tm.value) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	public static HashMap<Integer, String> TermIDMap = new HashMap<Integer, String>();

	public static void main(String[] args) {

		Flower f = new Flower();
		buildTermIndex(f);
		//ArrayList<56>
		for (Connection con : f.getConnections().getConnection()) {
			HashMap<Integer, ArrayList<Tag>> topicVectorMapCat = new HashMap<Integer, ArrayList<Tag>>();
			// HashMap<String, ArrayList<Tag>> topicVectorMapCat2 = new
			// HashMap<String, ArrayList<Tag>>();
			for (TopicLink tl : f.getCategoryById(con.getCat1())
					.getReprsentativetopic()) {
				Topic topic1 = f.getTopicById(tl.getTid());
				Cloud cloud = new Cloud();
				cloud.setMaxWeight(1.0);
				for (TermLink termlink : topic1.getTerm()) {

					cloud.addTag(new Tag(TermIDMap.get(termlink.getTid()),
							termlink.getScore()));
				}
				topicVectorMapCat.put(topic1.getTid(),
						(ArrayList<Tag>) cloud.tags());
			}
			for (TopicLink tl : f.getCategoryById(con.getCat2())
					.getReprsentativetopic()) {
				Topic topic1 = f.getTopicById(tl.getTid());
				if (topicVectorMapCat.containsKey(topic1.getLable())) {
					continue;
				}
				Cloud cloud = new Cloud();
				cloud.setMaxWeight(1.0);
				for (TermLink termlink : topic1.getTerm()) {

					cloud.addTag(new Tag(TermIDMap.get(termlink.getTid()),
							termlink.getScore()));
				}
				topicVectorMapCat.put(topic1.getTid(),
						(ArrayList<Tag>) cloud.tags());
			}
			for (Integer t : topicVectorMapCat.keySet()) {
				for (Integer k : topicVectorMapCat.keySet()) {
					double score = cosineSim(topicVectorMapCat.get(t), 	topicVectorMapCat.get(k));

				}
			}

		}

	}

	private static double cosineSim(ArrayList<Tag> arrayList,
			ArrayList<Tag> arrayList2) {
		// TODO Auto-generated method stub
		return 0;
	}

}
