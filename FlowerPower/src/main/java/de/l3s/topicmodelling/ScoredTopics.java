package de.l3s.topicmodelling;

public class ScoredTopics implements Comparable<ScoredTopics>{
int topicid;
double score;

	public ScoredTopics(int topicid, double score) {
	super();
	this.topicid = topicid;
	this.score = score;
}

	public double getScore() {
		return score;
	}
	public int getTopicid() {
		return topicid;
	}
	@Override
	public int compareTo(ScoredTopics o) {
		// TODO Auto-generated method stub
		return Double.compare(score, o.score);
	}

}
