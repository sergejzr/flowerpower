package de.l3s.topicmodelling;

public class ScoreTopic {
int topicid;
double score;
public ScoreTopic(int topicid, double score) {
	super();
	this.topicid = topicid;
	this.score = score;
}

public int getTopicid() {
	return topicid;
}
public double getScore() {
	return score;
}
}
