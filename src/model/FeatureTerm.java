package model;

public class FeatureTerm {
	private int N01;
	private int N11;
	private int N00;
	private int N10;
	private int tf;
	private double filiterWeight;
	private String text;
	private int sentenceID;
	private int paragraphID;
	
	public int getN01() {
		return N01;
	}
	public void setN01(int n01) {
		N01 = n01;
	}
	public int getN11() {
		return N11;
	}
	public void setN11(int n11) {
		N11 = n11;
	}
	public double getFiliterWeight() {
		return filiterWeight;
	}
	public void setFiliterWeight(double filiterWeight) {
		this.filiterWeight = filiterWeight;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getSentenceID() {
		return this.sentenceID;
	}
	public void setSentenceID(int sentenceID) {
		this.sentenceID = sentenceID;
	}
	public int getN00() {
		return N00;
	}
	public void setN00(int n00) {
		N00 = n00;
	}
	public int getN10() {
		return N10;
	}
	public void setN10(int n10) {
		N10 = n10;
	}
	public int getParagraphID() {
		return paragraphID;
	}
	public void setParagraphID(int paragraphID) {
		this.paragraphID = paragraphID;
	}
	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}
}
