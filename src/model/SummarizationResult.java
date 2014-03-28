package model;

import java.util.ArrayList;

public class SummarizationResult {
	
	public String passageName;
	public String originalAbstract;
	public int originalLength;
	public ArrayList<Sentence> sentences;
	
	public SummarizationResult() {
		sentences = new ArrayList<Sentence>();
	}
}
