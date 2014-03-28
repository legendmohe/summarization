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

public class FilterAnalyzer extends SummarizationAnalyzer {

	public FeatureSelector featureSelector;;
	
	public FilterAnalyzer(FeatureSelector featureSelector){
		this.featureSelector = featureSelector;
	}

	@Override
	public void analyze(ArrayList<Passage> passages,
			ArrayList<SummarizationResult> results) {
		
		HashMap<String, HashMap<String, FeatureTerm>> totalFeatureHashMap = null;
		if (featureSelector != null) {
			totalFeatureHashMap = featureSelector.getPassageFeatures(passages);
			featureSelector.limitFeatureHashMap(totalFeatureHashMap, SummarizationConstants.passageFeatureLimit);
		}
		for (Passage passage : passages) {
			//标题所含词汇
			HashSet<String> titleWordsHashSet = SummarizationPrepressor.wordSetOfStrings(passage.getName());
			
			double avgLength = 0.0;
			HashMap<String, FeatureTerm> featureHashMap = totalFeatureHashMap.get(passage.getName());
			for (Sentence sentence : passage.getSentences()) {//特征筛选
				sentence.wordsFreqHashMap.keySet().retainAll(featureHashMap.keySet());
				avgLength += sentence.content.length();
			}
			avgLength = avgLength/passage.getSentences().size();
			
			SummarizationResult passageResult = new SummarizationResult();
			passageResult.passageName = passage.getName();
			
			for (Sentence sentence : passage.getSentences()) {
				for (String term : sentence.wordsFreqHashMap.keySet()) {
					FeatureTerm feature = featureHashMap.get(term);
					if (feature == null) {//停用词去掉了
						continue;
					}
					
					double termWeight = 0.0;
					termWeight = Math.log(feature.getFiliterWeight() + 2)/Math.log(2);
					
					int tf = sentence.wordsFreqHashMap.get(term);
					if (titleWordsHashSet.contains(term)) {
						termWeight *= Math.log(tf + 1)/Math.log(2);
					}
					
					sentence.weight += termWeight;
				}
				
				if (sentence.locationInParagraph == 0 || passage.getSentences().size() - 1 == sentence.locationInParagraph)  {
					sentence.weight *= 2.0;
//					sentence.weight = sentence.weight*Math.log(1 + passage.getSentences().size()/(sentence.locationInParagraph + 1.0))/Math.log(2);
				}
				
				double b = 0.4;
				double lengthWeight = 1.0/((1 - b) + b*sentence.content.length()/avgLength);
				
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

}
