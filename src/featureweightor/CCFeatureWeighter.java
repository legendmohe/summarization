package featureweightor;

import model.FeatureTerm;

public class CCFeatureWeighter extends FeatureWeighter {

	@Override
	public double getFeatureValue(FeatureTerm featureTerm) {
		return featureTerm.getN11();
	}

}
