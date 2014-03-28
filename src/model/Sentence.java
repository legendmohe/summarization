package model;

import java.util.HashMap;

public class Sentence {
	public int id;
	public int paragraphID;
	public int locationInParagraph;
	public String content;
	public char punctuation;
	public double weight;
	public HashMap<String, Integer> wordsFreqHashMap;
	
	public Sentence() {
		this.id = -1;
		this.weight = 0.0;
		this.paragraphID = -1;
		this.locationInParagraph = -1;
		this.wordsFreqHashMap = new HashMap<String, Integer>();
	}
}
