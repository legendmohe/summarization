package analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import controller.SummarizationPrepressor;
import featureweightor.FeatureWeighter;

import model.FeatureTerm;
import model.Passage;
import model.Sentence;
import model.SummarizationResult;

public class CCAnalyzer extends SummarizationAnalyzer {

	public FeatureWeighter weightor;
	
	public CCAnalyzer(FeatureWeighter weightor){
		this.weightor = weightor;
	}

	@Override
	public void analyze(ArrayList<Passage> passages,
			ArrayList<SummarizationResult> results) {
		
		HashMap<String, Integer> dfHashMap = new HashMap<String, Integer>();//某个词的总共现次数
		HashMap<String, Integer> lengthHashMap = new HashMap<String, Integer>();//某篇文章的总共现次数
		HashMap<String, HashMap<String, HashMap<String, Integer>>> passageCoocHashMap = new HashMap<String, HashMap<String,HashMap<String,Integer>>>();
		HashMap<String, Double> avgSentenceLengthHashMap = new HashMap<String, Double>();
		int totalCooc = 0;
		for (Passage passage : passages) {
			
			int passageCoocCount = 0;
			HashMap<String, HashMap<String, Integer>> coocHashMap = generateWordsCoocHashMap(passage);
			for (String term : coocHashMap.keySet()) {
				int sumCount = 0;
				for (Integer count : coocHashMap.get(term).values()) {
					sumCount += count;
				}
				passageCoocCount += sumCount;
				//累计某个词的总共现次数
				Integer count = dfHashMap.get(term);
				if (count == null) {
					count = 0;
				}
				dfHashMap.put(term, count + sumCount);
			}
			totalCooc += passageCoocCount;
			
			lengthHashMap.put(passage.getName(), passageCoocCount);
			passageCoocHashMap.put(passage.getName(), coocHashMap);
			
			int totalLength = 0;
			for (Sentence sentence : passage.getSentences()) {
				totalLength += sentence.content.length();
			}
			avgSentenceLengthHashMap.put(passage.getName(), totalLength*1.0/passage.getSentences().size());
		}
		HashMap<String, HashMap<String, FeatureTerm>> passageFeatureHashMap = new HashMap<String, HashMap<String,FeatureTerm>>();
		for (Passage passage : passages) {
			
			HashMap<String, FeatureTerm> featureHashMap = new HashMap<String, FeatureTerm>();
			
			HashMap<String, HashMap<String, Integer>> coocHashMap = passageCoocHashMap.get(passage.getName());
			for (String term : coocHashMap.keySet()) {
				int sumCount = 0;
				for (Integer count : coocHashMap.get(term).values()) {
					sumCount += count;
				}
				
				FeatureTerm newFeature = new FeatureTerm();
				newFeature.setN11(sumCount);
				newFeature.setN10(dfHashMap.get(term) - sumCount);
				newFeature.setN01(lengthHashMap.get(passage.getName()) - sumCount);
				newFeature.setN00(totalCooc - newFeature.getN11() - newFeature.getN10() - newFeature.getN01());
				newFeature.setText(term);
				featureHashMap.put(term, newFeature);
			}
			
			passageFeatureHashMap.put(passage.getName(), featureHashMap);
		}
		
		for (Passage passage : passages) {
			
			//标题所含词汇
			HashSet<String> titleWordsHashSet = SummarizationPrepressor.wordSetOfStrings(passage.getName());
			
			HashMap<String, FeatureTerm> featureHashMap = passageFeatureHashMap.get(passage.getName());
			SummarizationResult passageResult = new SummarizationResult();
			passageResult.passageName = passage.getName();
			
			Double avgLengthDouble = avgSentenceLengthHashMap.get(passage.getName());
			
			for (Sentence sentence : passage.getSentences()) {
				for (String term : sentence.wordsFreqHashMap.keySet()) {
					FeatureTerm feature = featureHashMap.get(term);
					if (feature == null) {//停用词去掉了
						continue;
					}
					
					double weight = 1.0;
					weight = this.weightor.getFeatureValue(feature);//计算每个句子的权值
					int tf = sentence.wordsFreqHashMap.get(term);
					int df = feature.getN11() + feature.getN10();
					
					weight += Math.log(weight + 1.0);
//					weight *= Math.log(tf + 10.0)*df;
					
					if (titleWordsHashSet.contains(term)) {
						weight *= Math.log(tf + 1);
					}
					
					sentence.weight += weight;
				}
				
				if (sentence.locationInParagraph <= 0) {
					sentence.weight = sentence.weight*Math.log(10 + passage.getSentences().size()/(sentence.locationInParagraph + 1.0));
				}
				
				if (sentence.locationInParagraph == 0 || passage.getSentences().size() - 1 == sentence.locationInParagraph)  {
//					sentence.weight *= 2.0;
//					sentence.weight = sentence.weight*Math.log(1 + passage.getSentences().size()/(sentence.locationInParagraph + 1.0))/Math.log(2);
				}
				
				double lengthWeight = 1.0/((1 - 0.5) + 0.5*sentence.content.length()/avgLengthDouble);
				sentence.weight = sentence.weight*lengthWeight;
			}
			passageResult.sentences.addAll(passage.getSentences());
			Collections.sort(passageResult.sentences, new Comparator<Sentence>() {

				@Override
				public int compare(Sentence o1, Sentence o2) {
					if (o2.weight < o1.weight) {
						return -1;
					}else if(o2.weight > o1.weight){
						return 1;
					}
					return 0;
				}
			});
			
			results.add(passageResult);
		}
	}

	public static HashMap<String, HashMap<String, Integer>> generateWordsCoocHashMap(Passage passage) {
		HashMap<String, HashMap<String, Integer>> coocHashMap = new HashMap<String, HashMap<String,Integer>>();
		//
		for (Sentence sentence : passage.getSentences()) {
			if (sentence.wordsFreqHashMap.size() < 2) {//句子中无词或少于两个
				continue;
			}
			//
			HashSet<String> termSet = new HashSet<String>();
			termSet.addAll(sentence.wordsFreqHashMap.keySet());
			for (String term1 : termSet) {
				HashMap<String, Integer> term1CoocHashMap = coocHashMap.get(term1);
				if (term1CoocHashMap == null) {
					term1CoocHashMap = new HashMap<String, Integer>();
					coocHashMap.put(term1, term1CoocHashMap);
				}
				for (String term2 : termSet) {
					
					if (term1.equals(term2)) {//
						continue;
					}
					
					Integer countInteger = term1CoocHashMap.get(term2);
					if (countInteger == null) {
						countInteger = 0;
					}
					term1CoocHashMap.put(term2, countInteger + 1);
				}
			}
		}
		
		return coocHashMap;
	}
	
	public static HashMap<Integer, HashMap<Integer, Integer>> generateSentencesCouplingHashMap(Passage passage) {
		HashMap<Integer, HashMap<Integer, Integer>> couplingHashMap = new HashMap<Integer, HashMap<Integer,Integer>>();
		ArrayList<Sentence> sentences = passage.getSentences();
		for (Sentence currentSentence : sentences) {
			//word 的集合
			HashSet<String> currentWordSet = new HashSet<String>();
			currentWordSet.addAll(currentSentence.wordsFreqHashMap.keySet());
			//初始化链
			couplingHashMap.put(currentSentence.id, new HashMap<Integer, Integer>());
			
			for (Sentence targetSentence : sentences) {
				if (targetSentence.id == currentSentence.id) {
					continue;
				}
				
				HashMap<Integer, Integer> targetHashMap = couplingHashMap.get(targetSentence.id);
				if (targetHashMap != null) {
					if (targetHashMap.keySet().contains(currentSentence.id)) {
						//已经算过, 直接保存耦合次数
						HashMap<Integer, Integer> currentHashMap = couplingHashMap.get(currentSentence.id);
						currentHashMap.put(targetSentence.id, targetHashMap.get(currentSentence.id));
					}
				}
				
				//target word 的集合
				HashSet<String> targetWordHashSet = new HashSet<String>();
				targetWordHashSet.addAll(targetSentence.wordsFreqHashMap.keySet());

				//求交集
				targetWordHashSet.retainAll(currentWordSet);
				
				//保存耦合次数
				HashMap<Integer, Integer> currentHashMap = couplingHashMap.get(currentSentence.id);
				currentHashMap.put(targetSentence.id, targetWordHashSet.size());
			}
		}
		
		return couplingHashMap;
	}
	
	public static HashMap<Integer, HashMap<Integer, Integer>> generateSentencesBooleanCouplingHashMap(Passage passage) {
		HashMap<Integer, HashMap<Integer, Integer>> couplingHashMap = new HashMap<Integer, HashMap<Integer,Integer>>();
		ArrayList<Sentence> sentences = passage.getSentences();
		for (Sentence currentSentence : sentences) {
			//初始化链
			HashMap<Integer, Integer> currentHashMap = new HashMap<Integer, Integer>();
			couplingHashMap.put(currentSentence.id, currentHashMap);
			
			for (Sentence targetSentence : sentences) {
				if (targetSentence.id == currentSentence.id) {
					continue;
				}
				currentHashMap.put(targetSentence.id, 1);
			}
		}
		
		return couplingHashMap;
	}
}
