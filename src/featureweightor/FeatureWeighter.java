package featureweightor;

import model.FeatureTerm;

public abstract class FeatureWeighter {
	abstract public double getFeatureValue(FeatureTerm featureTerm);
}
