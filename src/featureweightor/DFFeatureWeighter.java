package featureweightor;

import model.FeatureTerm;

public class DFFeatureWeighter extends FeatureWeighter {

	@Override
	public double getFeatureValue(FeatureTerm featureTerm) {
		return featureTerm.getN11();
	}

}
