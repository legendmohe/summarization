package featureselector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import model.FeatureTerm;
import model.Passage;
import featureweightor.FeatureWeighter;

public abstract class FeatureSelector {
	
	FeatureWeighter weighter;
	
	public FeatureSelector(FeatureWeighter weighter){
		this.weighter = weighter;
	}
	
	abstract public HashMap<String, HashMap<String, FeatureTerm>> getPassageFeatures(
			ArrayList<Passage> passages);
	
	public void limitFeatureHashMap(HashMap<String, HashMap<String, FeatureTerm>> featureHashMap, int limit) {
		System.out.println("每篇文章取" + limit + "个主题词");
		for (String passageName : featureHashMap.keySet()) {
			HashMap<String, FeatureTerm> passageFeatureHashMap = new HashMap<String, FeatureTerm>();
			ArrayList<Map.Entry<String, FeatureTerm>> featureArrayList = new ArrayList<Map.Entry<String,FeatureTerm>>();
			featureArrayList.addAll(featureHashMap.get(passageName).entrySet());
			Collections.sort(featureArrayList, new Comparator<Map.Entry<String, FeatureTerm>>() {

				@Override
				public int compare(Entry<String, FeatureTerm> o1,
						Entry<String, FeatureTerm> o2) {
					if (o2.getValue().getFiliterWeight() < o1.getValue().getFiliterWeight()) {
						return -1;
					}else if(o2.getValue().getFiliterWeight() > o1.getValue().getFiliterWeight()){
						return 1;
					}
					return 0;
				}
			});
			int count = 0;
			for (Entry<String, FeatureTerm> entry : featureArrayList) {
//				System.out.println(entry.getKey());
				passageFeatureHashMap.put(entry.getKey(), entry.getValue());
				if (++count >= limit) {
					break;
				}
			}
			featureHashMap.put(passageName, passageFeatureHashMap);
		}
	}
}
