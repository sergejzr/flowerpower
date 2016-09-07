package test;



import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import l3s.rdj.document.Document;
import l3s.rdj.impl.AllPairsDJ;
import l3s.rdj.impl.SampleDJ;
import l3s.rdj.impl.TrackDJ;
import l3s.toolbox.JaccardSimilarityComparator;


public class DiversityExample {

	public static void main(String[] args) {

		Vector<Document> collection = new Vector<Document>();
		

		double error = .005, confidentiality = .95;
		
		JaccardSimilarityComparator similarityComparator = new JaccardSimilarityComparator();
		
		 AllPairsDJ dj1 = new AllPairsDJ(collection, error, confidentiality,similarityComparator);
		System.out.println("RDJ:" + dj1.getRDJ());

		 SampleDJ dj2 = new SampleDJ(collection, error, confidentiality,similarityComparator);
		System.out.println("RDJ:" + dj2.getRDJ());

		//TracjDJ works only with Jaccard similarity measure.
		 TrackDJ dj3 = new TrackDJ(collection, confidentiality, confidentiality);
		System.out.println("RDJ:" + dj3.getRDJ());

	}

	private static Vector<Document> generateCollection(int numOfDocuments, int maxDoclength, int vocabularySize) {
		Vector<Document> collecion = new Vector<Document>();

		Random r = new Random(19);
		int[][] line = new int[numOfDocuments][];

		for (int i = 0; i < line.length; i++) {
			int len = r.nextInt(maxDoclength) + 1;
			Document d = new Document();
			line[i] = new int[len];
			for (int y = 0; y < len; y++) {
				int curval = r.nextInt(vocabularySize);
				d.add(curval + "");
				line[i][y] = curval;
			}
			Arrays.sort(line[i]);
			collecion.add(d);
		}
		return collecion;
	}
}