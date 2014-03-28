package model;

import java.util.ArrayList;

public class Paragraph {
	private int id;
	private ArrayList<Sentence> sentences;
	
	public Paragraph() {
		setSentences(new ArrayList<Sentence>());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(ArrayList<Sentence> sentences) {
		this.sentences = sentences;
	}
}
