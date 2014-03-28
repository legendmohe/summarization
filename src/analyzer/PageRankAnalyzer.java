package analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import model.FeatureTerm;
import model.Passage;
import model.Sentence;
import model.SummarizationResult;
import constants.SummarizationConstants;
import controller.SummarizationPrepressor;
import featureselector.FeatureSelector;

public class PageRankAnalyzer extends SummarizationAnalyzer {
	
	public double precision;
	public FeatureSelector featureSelector;
	HashMap<String, HashMap<String, FeatureTerm>> totalFeatureHashMap;
	
	public PageRankAnalyzer(double precision, FeatureSelector featureSelector) {
		this.precision = precision;
		this.featureSelector = featureSelector;
	}
	
	@Override
	public void analyze(ArrayList<Passage> passages,
			ArrayList<SummarizationResult> results) {
		if (featureSelector != null) {//如果开了filter
			this.totalFeatureHashMap = featureSelector.getPassageFeatures(passages);
			featureSelector.limitFeatureHashMap(this.totalFeatureHashMap, SummarizationConstants.passageFeatureLimit);
		}
		
		for (Passage passage : passages) {
			if (featureSelector != null) {//特征筛选
				HashMap<String, FeatureTerm> featureHashMap = this.totalFeatureHashMap.get(passage.getName());
				for (Sentence sentence : passage.getSentences()) {
					sentence.wordsFreqHashMap.keySet().retainAll(featureHashMap.keySet());
				}
			}
			
			//计算每个句子的PageRank
			PageRankObject[] resultRankMatrix = processPageRank(passage);
			//生成结果
			SummarizationResult passageResult = new SummarizationResult();
			passageResult.passageName = passage.getName();
			for (int i = 0; i < passage.getSentences().size(); i++) {
				Sentence sentence = passage.getSentences().get(i);
				sentence.weight = resultRankMatrix[i].weight;
				if (sentence.locationInParagraph <= 0) {
					
//					sentence.weight *= 2.0;
//					sentence.weight = sentence.weight*Math.log(passage.getSentences(sentence.paragraphID).size()/(sentence.locationInParagraph + 1.0) + 1.0);
				}
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

	public PageRankObject[] processPageRank(Passage passage) {
		//初始化排序矩阵
		int linkSize = passage.getSentences().size();
		double initRank = 1.0/linkSize;
		PageRankObject[] initRankMatrixArrayList = new PageRankObject[linkSize];
		for (int i = 0; i < initRankMatrixArrayList.length; i++) {
			initRankMatrixArrayList[i] = new PageRankObject(initRank, passage.getSentences().get(i).id);
		}
		//获得转移矩阵
		HashMap<Integer, HashMap<Integer, Double>> transitionMatrix = this.generateTransitionMatrix(passage);
		
		//迭代计算PageRank
		PageRankObject[] resultRankMatrix = initRankMatrixArrayList;
		PageRankObject[] preResultMatrix = null;
		int iterCount = 0;
		do {
			iterCount++;
			
			preResultMatrix = resultRankMatrix;
			resultRankMatrix = this.doPageRank(resultRankMatrix, transitionMatrix);
		} while (!this.reachPrecision(preResultMatrix, resultRankMatrix) && iterCount < SummarizationConstants.pageRankMaxIterationCount);
		System.out.println("迭代" + iterCount + "次");
		return resultRankMatrix;
	}
	
	public PageRankObject[] doPageRank(PageRankObject[] rankMatrix, HashMap<Integer, HashMap<Integer, Double>> transitionMatrix) {
		PageRankObject[] resultRankMatrix = new PageRankObject[rankMatrix.length];//注意要初始化长度
		for (int i = 0; i < rankMatrix.length; i++) {
			
			double weight = 0.0;
			int id = rankMatrix[i].sentenceID;
			HashMap<Integer, Double> couplingHashMap = transitionMatrix.get(id);
			//仅计算那些有转移概率的句子
			for (Integer couplingId : couplingHashMap.keySet()) {
				double rank = rankMatrix[couplingId].weight;
				double trans = couplingHashMap.get(couplingId);
				weight += rank*trans;
//				System.out.println("rank:" + rank + " trans:" + trans + " weight:" + rank*trans);
			}
			weight = (1 - SummarizationConstants.pageRankResistance)/rankMatrix.length + SummarizationConstants.pageRankResistance*weight;
			//生成新的矩阵
			resultRankMatrix[i] = new PageRankObject(weight, rankMatrix[i].sentenceID);
		}
		return resultRankMatrix;
	}
	
	public HashMap<Integer, HashMap<Integer, Double>> generateTransitionMatrix(Passage passage) {
		HashMap<Integer, HashMap<Integer, Double>> transitionMatrix = new HashMap<Integer, HashMap<Integer,Double>>();
		//特征词
		HashMap<String, FeatureTerm> featureHashMap = null;
		if (this.totalFeatureHashMap != null) {
			featureHashMap = this.totalFeatureHashMap.get(passage.getName());
		}
		
		//标题所含词汇
		HashSet<String> titleWordsHashSet = SummarizationPrepressor.wordSetOfStrings(passage.getName());
				
		HashMap<Integer, Double> outLinkHashMap = new HashMap<Integer, Double>();
		ArrayList<Sentence> sentences = passage.getSentences();
		for (Sentence currentSentence : sentences) {
			//初始化链
			HashMap<Integer, Double> currentHashMap = new HashMap<Integer, Double>();
			transitionMatrix.put(currentSentence.id, currentHashMap);
			
			for (Sentence targetSentence : sentences) {
				if (targetSentence.id == currentSentence.id) {
					continue;
				}
				double jointStrength = this.jointStrengthBetweenSentences(currentSentence, targetSentence, featureHashMap, titleWordsHashSet);
				if (jointStrength == 0) {//稀疏
					continue;
				}
				
				currentHashMap.put(targetSentence.id, jointStrength);
				
				Double outLinkSum = outLinkHashMap.get(currentSentence.id);//统计target的链出数量，为了后面的归一化。
				if (outLinkSum == null) {
					outLinkSum = 0.0;
				}
				outLinkHashMap.put(currentSentence.id, jointStrength + outLinkSum);
			}
		}
		
		for (Sentence currentSentence : sentences) {
			
			HashMap<Integer, Double> currentHashMap = transitionMatrix.get(currentSentence.id);
			
			for (Sentence targetSentence : sentences) {
				if (targetSentence.id == currentSentence.id) {
					continue;
				}
				Double jointStrength = currentHashMap.get(targetSentence.id);
				if (jointStrength == null) {//稀疏
					continue;
				}
				double outLink = outLinkHashMap.get(targetSentence.id);
				double transitionProbablity = jointStrength/outLink;
				currentHashMap.put(targetSentence.id, transitionProbablity);
			}
		}
		
		return transitionMatrix;
	}
	
	public double jointStrengthBetweenSentences(Sentence sentence1, Sentence sentence2, HashMap<String, FeatureTerm> featureHashMap, HashSet<String> titleWordsHashSet) {
		
		//求交集
		HashSet<String> targetWordHashSet = new HashSet<String>();
		targetWordHashSet.addAll(sentence2.wordsFreqHashMap.keySet());
		targetWordHashSet.retainAll(sentence1.wordsFreqHashMap.keySet());
		
		double jointStrength = 0.0;
		for (String word : targetWordHashSet) {
//			jointStrength += 1.0;
//			
//			FeatureTerm featureTerm = featureHashMap.get(word);
			double tf1 = sentence1.wordsFreqHashMap.get(word);
			double tf2 = sentence2.wordsFreqHashMap.get(word);
//			
//			//效果一般
			if (titleWordsHashSet.contains(word)) {
				jointStrength += Math.log(tf1 + tf2)/Math.log(2);
			}
		}
		
//		jointStrength = targetWordHashSet.size();
//		double jointStrength = 0.0;
//		for (String word : targetWordHashSet) {
//			FeatureTerm featureTerm = featureHashMap.get(word);
//			jointStrength += featureTerm.getFiliterWeight();
//		}
		int size1 = sentence1.wordsFreqHashMap.keySet().size();
		int size2 = sentence2.wordsFreqHashMap.keySet().size();
		jointStrength += targetWordHashSet.size()*1.0/(size1 + size2);
		
		return jointStrength;
	}
	
	private boolean reachPrecision(PageRankObject[] preMatrix, PageRankObject[] resultMatrix) {
		for (int i = 0; i < resultMatrix.length; i++) {
			 if (Math.abs(preMatrix[i].weight - resultMatrix[i].weight) > this.precision) {
				return false;
			}
		}
		return true;
	}
	
	private class PageRankObject{
		public double weight;
		public int sentenceID;
		public PageRankObject(double weight, int sentenceID){
			this.weight = weight;
			this.sentenceID = sentenceID;
		}
	}
}
