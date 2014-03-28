package featureselector;

import java.util.ArrayList;
import java.util.HashMap;

import model.FeatureTerm;
import model.Passage;
import model.Sentence;
import featureweightor.FeatureWeighter;

public class SentenceFeatureSelector extends FeatureSelector {

	public SentenceFeatureSelector(FeatureWeighter weighter) {
		super(weighter);
	}

	@Override
	public HashMap<String, HashMap<String, FeatureTerm>> getPassageFeatures(
			ArrayList<Passage> passages) {
		int totalSentence = 0;
		HashMap<String, HashMap<String, FeatureTerm>> totalFeatureHashMap = new HashMap<String, HashMap<String,FeatureTerm>>();
		HashMap<String, Integer> dfHashMap = new HashMap<String, Integer>();
		for (Passage passage : passages) {
			HashMap<String, FeatureTerm> featureHashMap = new HashMap<String, FeatureTerm>();
			for (Sentence sentence : passage.getSentences()) {
				for (String word : sentence.wordsFreqHashMap.keySet()) {
					if (word.length() < 2) {//去除单字
						continue;
					}
					FeatureTerm feature = featureHashMap.get(word);
					if (feature == null) {
						feature = new FeatureTerm();
						feature.setText(word);
						feature.setSentenceID(sentence.id);
						feature.setN11(1);
						feature.setN01(passage.getSentences().size() - 1);
						feature.setTf(sentence.wordsFreqHashMap.get(word));
						featureHashMap.put(word, feature);
					}else {
						feature.setN11(feature.getN11() + 1);
						feature.setN01(feature.getN01() - 1);
						feature.setTf(feature.getTf() + sentence.wordsFreqHashMap.get(word));
					}
					
					Integer df = dfHashMap.get(word);
					if (df == null) {
						df = 0;
					}
					dfHashMap.put(word, df + 1); 
					
				}
			}
			
			totalSentence += passage.getSentences().size();
			totalFeatureHashMap.put(passage.getName(), featureHashMap);
		}
		
		for (String passageName : totalFeatureHashMap.keySet()) {
			HashMap<String, FeatureTerm> featureHashMap = totalFeatureHashMap.get(passageName);
			for (String word : featureHashMap.keySet()) {
				FeatureTerm featureTerm = featureHashMap.get(word);
				int df = dfHashMap.get(word) ;
				featureTerm.setN10(df - featureTerm.getN11());
				featureTerm.setN00(totalSentence - featureTerm.getN11() - featureTerm.getN10() - featureTerm.getN01());
				
				double weight = this.weighter.getFeatureValue(featureTerm);
				featureTerm.setFiliterWeight(weight);
			}
		}
		return totalFeatureHashMap;
	}

}
