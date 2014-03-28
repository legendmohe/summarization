package corpus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import model.CorpusDocument;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CorpusPreprocessor {
	DocumentBuilder db;
	
	public CorpusPreprocessor(){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
		dbf.setValidating(true);
		dbf.setIgnoringElementContentWhitespace(true);//跳过空白的节点
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
	}
	

	public CorpusDocument parseXML(File file) {
			
		if (!file.getName().endsWith("xml")) {
			return null;
		}
		CorpusDocument newDocument = null;
		try {
			Document document = db.parse(file);
			System.out.println("正在解析: "+file.getName());
			newDocument = new CorpusDocument();
			
			NodeList documentNodeList = document.getChildNodes(); 
			Node baseNode = null;
			for (int i = 0; i < documentNodeList.getLength(); i++) {//找出根节点
				baseNode = documentNodeList.item(i); 
				if (baseNode.getTextContent() != null) {
					break;
				}
			}
			documentNodeList = baseNode.getChildNodes();
			for (int i = 0; i < documentNodeList.getLength(); i++) { 
				Node doumentBlock = documentNodeList.item(i); 
				if (doumentBlock.getNodeName().equalsIgnoreCase("title")) {
					newDocument.setTitle(doumentBlock.getTextContent().replaceAll("\n", " "));
				}else if (doumentBlock.getNodeName().equalsIgnoreCase("abstract")) {
					newDocument.setAbstractText(doumentBlock.getTextContent().replaceAll("\n", " "));
				}else if (doumentBlock.getNodeName().equalsIgnoreCase("body")) {
					NodeList divNodeList = doumentBlock.getChildNodes();
					for (int j = 0; j < divNodeList.getLength(); j++) {
						Node divNode = divNodeList.item(j);
						NodeList pNodeList = divNode.getChildNodes();
						for (int k = 0; k < pNodeList.getLength(); k++) {
							Node pNode = pNodeList.item(k);
							String pString = pNode.getTextContent();
							if (pString == null || pString.length() == 0) {
								continue;
							}
							newDocument.paragraphs.add(pString.replaceAll("\n", " "));
						}
					}
				}
			} 
			System.out.println(" 解析完毕."); 
		} catch (FileNotFoundException e) { 
			System.out.println(e.getMessage()); 
		} catch (SAXException e) { 
			System.out.println(e.getMessage()); 
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return newDocument;
	}
	
}
