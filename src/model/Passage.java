package model;

import java.util.ArrayList;

public class Passage {
	private String OriginalAbstractString;
	private String name;
	private int length;
	
	private ArrayList<Sentence> sentences;
	private ArrayList<Paragraph> paragraphs;
	
	public Passage() {
		setSentences(new ArrayList<Sentence>());
		setParagraphs(new ArrayList<Paragraph>());
	}

	public String getOriginalAbstractString() {
		return OriginalAbstractString;
	}

	public void setOriginalAbstractString(String originalAbstractString) {
		OriginalAbstractString = originalAbstractString;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public ArrayList<Sentence> getSentences() {
		return sentences;
	}
	
	public ArrayList<Sentence> getSentences(int paragraphID) {
		Paragraph paragraph = this.paragraphs.get(paragraphID);
		if (paragraph != null) {
			return paragraph.getSentences();
		}
		return null;
	}

	public void setSentences(ArrayList<Sentence> sentences) {
		this.sentences = sentences;
	}

	public ArrayList<Paragraph> getParagraphs() {
		return paragraphs;
	}

	public void setParagraphs(ArrayList<Paragraph> paragraphs) {
		this.paragraphs = paragraphs;
	}
}
