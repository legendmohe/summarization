package featureselector;

import java.util.ArrayList;
import java.util.HashMap;

import model.FeatureTerm;
import model.Paragraph;
import model.Passage;
import model.Sentence;
import featureweightor.FeatureWeighter;

public class ParagraphFeatureSelector extends FeatureSelector {

	public ParagraphFeatureSelector(FeatureWeighter weighter) {
		super(weighter);
	}

	@Override
	public HashMap<String, HashMap<String, FeatureTerm>> getPassageFeatures(
			ArrayList<Passage> passages) {
		int totalPagagraphNumber = 0;
		HashMap<String, HashMap<String, FeatureTerm>> totalFeatureHashMap = new HashMap<String, HashMap<String,FeatureTerm>>();
		HashMap<String, Integer> dfHashMap = new HashMap<String, Integer>();
		for (Passage passage : passages) {
			HashMap<String, FeatureTerm> featureHashMap = new HashMap<String, FeatureTerm>();
			for (Paragraph paragraph : passage.getParagraphs()) {
				HashMap<String, Integer> tfHashMap = new HashMap<String, Integer>();//每个词在段落中的tf
				for (Sentence sentence : paragraph.getSentences()) {//建立段落wordset，方便统计
					for (String word : sentence.wordsFreqHashMap.keySet()) {
						Integer tf = tfHashMap.get(word);
						if (tf == null) {
							tf = 0;
						}
						tfHashMap.put(word, tf + sentence.wordsFreqHashMap.get(word));
					};
				}
				for (String word : tfHashMap.keySet()) {
					FeatureTerm feature = featureHashMap.get(word);
					if (feature == null) {
						feature = new FeatureTerm();
						feature.setText(word);
						feature.setParagraphID(paragraph.getId());//没有统计句子的ID
						feature.setN11(1);
						feature.setN01(passage.getParagraphs().size() - 1);
						feature.setTf(tfHashMap.get(word));//设置TF
						featureHashMap.put(word, feature);
					}else {
						feature.setN11(feature.getN11() + 1);
						feature.setN01(feature.getN01() - 1);
						feature.setTf(feature.getTf() + tfHashMap.get(word));
					}
					
					Integer df = dfHashMap.get(word);
					if (df == null) {
						df = 0;
					}
					dfHashMap.put(word, df + 1); 
				}
			}
			
			totalPagagraphNumber += passage.getParagraphs().size();//M值
			totalFeatureHashMap.put(passage.getName(), featureHashMap);
		}
		
		for (String passageName : totalFeatureHashMap.keySet()) {
			HashMap<String, FeatureTerm> featureHashMap = totalFeatureHashMap.get(passageName);
			for (String word : featureHashMap.keySet()) {
				FeatureTerm featureTerm = featureHashMap.get(word);
				int df = dfHashMap.get(word) ;
				featureTerm.setN10(df - featureTerm.getN11());
				featureTerm.setN00(totalPagagraphNumber - featureTerm.getN11() - featureTerm.getN10() - featureTerm.getN01());
				
				double weight = this.weighter.getFeatureValue(featureTerm);
				featureTerm.setFiliterWeight(weight);
			}
		}
		return totalFeatureHashMap;
	}

}
