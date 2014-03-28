package model;

import java.util.ArrayList;

public class CorpusDocument {
	private String docName;
	private String title;
	private String abstractText;
	public ArrayList<String> paragraphs;
	
	public CorpusDocument() {
		paragraphs = new ArrayList<String>();
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAbstractText() {
		return abstractText;
	}
	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}
	public String getDocName() {
		return docName;
	}
	public void setDocName(String docName) {
		this.docName = docName;
	}
	
}
