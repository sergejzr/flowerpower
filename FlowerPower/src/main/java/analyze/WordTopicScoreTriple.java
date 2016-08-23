package analyze;

public class WordTopicScoreTriple implements Comparable<WordTopicScoreTriple> {
	private int topic;
	private double score;
	private String word;
	public WordTopicScoreTriple() {
		// TODO Auto-generated constructor stub
	}
	public int getTopic() {
		return topic;
	}
	public void setTopic(int topic) {
		this.topic = topic;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int compareTo(WordTopicScoreTriple wts) {
		 
		double compareQuantity = ((WordTopicScoreTriple) wts).getScore();
		if(compareQuantity - this.score>0)
		{
			return 1;
		}
		return -1;
 
	}	

}
