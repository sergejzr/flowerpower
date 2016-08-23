package de.l3s.nlp;

public class Lemma {

	private String word;
	private String tag;
	private String lem;

	public Lemma(String word, String tag, String lem) {
		this.word=word;
		this.tag=tag;
		this.lem=lem;
	}

	public String getWord() {
		return word;
	}

	public String getTag() {
		return tag;
	}

	public String getLem() {
		return lem;
	}

}
