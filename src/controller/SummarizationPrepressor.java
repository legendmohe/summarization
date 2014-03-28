package controller;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import model.CorpusDocument;
import model.FeatureTerm;
import model.Paragraph;
import model.Passage;
import model.Sentence;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import constants.Stopwords;
import constants.SummarizationConstants;
import corpus.CorpusPreprocessor;
import featureweightor.FeatureWeighter;

public class SummarizationPrepressor {
	
	static private Analyzer analyzer = null;
	static private PorterStemFilter stemmer = null;
	static private boolean stemming = false;
	
	public static Analyzer shareAnalyzer() {
		if (analyzer == null) {
			String[] stopwords = null;
			try {
				stopwords = Stopwords.getStopwords(SummarizationConstants.ENGLISH_STOPWORD_PATH);
			} catch (Exception e) {
				e.printStackTrace();
			}
			analyzer = new StopAnalyzer(Version.LUCENE_36, StopFilter.makeStopSet(Version.LUCENE_36, stopwords, true));
		}
		return analyzer;
	}
	
	public static PorterStemFilter openPorterStemFilter(TokenStream tokenStream) {
		if (tokenStream == null) {
			stemmer = null;
			stemming = false;
		}else {
			stemmer = new PorterStemFilter(new LowerCaseFilter(Version.LUCENE_36, tokenStream));
			stemming = true;
		}
		return stemmer;
	}
	
	public static void closeStemming() {
		stemmer = null;
		stemming = false;
	}
	
	public static PorterStemFilter sharePorterStemFilter() {
		return stemmer;
	}
	
	public static boolean isStemming() {
		return stemming;
	}
	
	public static void setStemming(boolean stemming) {
		SummarizationPrepressor.stemming = stemming;
	}
	
	public static Passage extractPassageFromFile(String filePath) {
		File file = null;
		Passage passage = null;
		try {
			file = SummarizationUtil.fileFromPath(filePath, false);
			String sourceString = SummarizationUtil.contentOfFileContent(file, false, "GBK");
			passage = SummarizationPrepressor.extractPassageFromFile(sourceString, file.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return passage;
	}
	
	public static Passage extractPassageFromFile(ArrayList<String> paragraphs, String sourceName){
		System.out.print("开始读取目标文档:" + sourceName);
		
		if (paragraphs == null || paragraphs.size() == 0) {
			System.err.println("source empty:" + sourceName);
			return null;
		}
		Passage resultPassage = null;
		try {
			resultPassage = new Passage();
			resultPassage.setName(sourceName);
			
			int passageLength = 0;
			int sentenceID = 0;
			int paragraphID = 0;
			for (int i = 0; i < paragraphs.size(); i++) {
				Paragraph paragraph = new Paragraph();
				paragraph.setId(paragraphID);
				
				StringReader stringReader = new StringReader(paragraphs.get(i));
				@SuppressWarnings("resource")
				MySentenceTokenizer sentenceTokenizer = new MySentenceTokenizer(stringReader);//不去逗号
				
				int locationInParagraph = 0;
				while (sentenceTokenizer.incrementToken()) {
					CharTermAttribute attribute = sentenceTokenizer.getAttribute(CharTermAttribute.class);
					String content = attribute.toString().trim();
					Sentence sentence = SummarizationPrepressor.extractSentenceFromString(paragraphID, sentenceID, content);
					
					if (sentence.wordsFreqHashMap.size() < SummarizationConstants.MinimalFeatureCount) {
						continue;//除掉太短的句子（噪音？）
					}
					
					sentence.locationInParagraph = locationInParagraph;
					resultPassage.getSentences().add(sentence);
					paragraph.getSentences().add(sentence);
					
					locationInParagraph++;//句子的位置递增
					sentenceID++;//id 递增
					passageLength += sentence.content.length();
				}
				//没有句子的段要去掉
				if (paragraph.getSentences().size() < 1) {
					continue;
				}
				
				paragraphID++;
				resultPassage.getParagraphs().add(paragraph);
			}
			resultPassage.setLength(passageLength);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("读取结束");
		
		return resultPassage;
	}
	
	public static Passage extractPassageFromFile(String sourceString, String sourceName) {
		ArrayList<String> paragraphs = new ArrayList<String>();
		paragraphs.add(sourceString);//若语料没有分段，则全部当作第一段，paragraphID为0
		Passage newPassage = SummarizationPrepressor.extractPassageFromFile(paragraphs, sourceName);
		return newPassage;
	}

	public static Sentence extractSentenceFromString(int paragraphID, int sentenceID,
			String content) throws IOException {
		Sentence sentence = new Sentence();
		sentence.id = sentenceID;
		sentence.paragraphID = paragraphID;
		sentence.content = content;
		sentence.punctuation = content.charAt(content.length() - 1);
		
		TokenStream tokenStream = shareAnalyzer().tokenStream("", new StringReader(sentence.content));
		if (stemming) {
			tokenStream = SummarizationPrepressor.openPorterStemFilter(tokenStream);
		}
		CharTermAttribute wordAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		
		while (tokenStream.incrementToken()) {
			String word = wordAttribute.toString();
			Integer freq = sentence.wordsFreqHashMap.get(word);
			if (freq == null) {
				freq = 1;
			}
			sentence.wordsFreqHashMap.put(word, freq + 1);
		}
		return sentence;
	}
	
	public static ArrayList<Passage> extactPassageFromFolder(String folderPath, String suffix) {
		
		ArrayList<Passage> passages = null;
		try {
			passages = new ArrayList<Passage>();
			File[] files = SummarizationUtil.filesFromPath(folderPath);
			for (File file : files) {
				if (!file.getName().endsWith(suffix)) {
					continue;
				}
				String sourceString = SummarizationUtil.contentOfFileContent(file, false, "GBK");
				Passage passage = SummarizationPrepressor.extractPassageFromFile(sourceString, file.getName());
				if (passage != null) {
					passages.add(passage);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return passages;
	}
	
	public static ArrayList<Passage> extractEvaluatePassageFromFolder(String folderPath) {
		
		ArrayList<Passage> passages = null;
		try {
			CorpusPreprocessor processor = new CorpusPreprocessor();
			passages = new ArrayList<Passage>();
			File[] files = SummarizationUtil.filesFromPath(folderPath);
			for (File file : files) {
				CorpusDocument newDocument = processor.parseXML(file);
				if (newDocument == null) {
					continue;
				}
				if (newDocument.getAbstractText() == null || newDocument.getAbstractText().length() == 0) {
					System.err.println("no abstract:" + newDocument.getDocName());
					continue;
				}
				if (newDocument.paragraphs == null || newDocument.paragraphs.size() == 0) {
					System.err.println("no content:" + newDocument.getDocName());
					continue;
				}
				Passage passage = SummarizationPrepressor.extractPassageFromFile(newDocument.paragraphs, newDocument.getTitle());
				if (passage != null) {
					passage.setOriginalAbstractString(newDocument.getAbstractText());
					passages.add(passage);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return passages;
	}
	
	public static HashSet<String> wordSetOfStrings(String targetString) {
		if (targetString == null || targetString.length() == 0) {
			return null;
		}
		HashSet<String> resultWordHashSet = new HashSet<String>();
		TokenStream abstractStream = SummarizationPrepressor.shareAnalyzer().tokenStream("", new StringReader(targetString));
		if (SummarizationPrepressor.isStemming()) {
			abstractStream = SummarizationPrepressor.openPorterStemFilter(abstractStream);
		}
		CharTermAttribute wordAttribute = abstractStream.addAttribute(CharTermAttribute.class);
		try {
			while (abstractStream.incrementToken()) {
				resultWordHashSet.add(wordAttribute.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return resultWordHashSet;
	}
}
