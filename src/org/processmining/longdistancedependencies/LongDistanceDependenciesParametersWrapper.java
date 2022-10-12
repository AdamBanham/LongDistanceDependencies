package org.processmining.longdistancedependencies;

import org.deckfour.xes.classification.XEventClassifier;

public class LongDistanceDependenciesParametersWrapper implements LongDistanceDependenciesParameters {

	private final LongDistanceDependenciesParameters shadow;

	public LongDistanceDependenciesParametersWrapper(LongDistanceDependenciesParameters parameters) {
		shadow = parameters;
	}

	public boolean isEnableLongDistanceDependencies() {
		return false;
	}

	public XEventClassifier getClassifier() {
		return shadow.getClassifier();
	}

	public boolean isDebug() {
		return shadow.isDebug();
	}

	public int getNumberOfThreads() {
		return shadow.getNumberOfThreads();
	}

	public boolean isPerformPostProcessing() {
		return shadow.isPerformPostProcessing();
	}

	public double getAlpha() {
		return shadow.getAlpha();
	}

	public boolean isApplySymmetries() {
		return shadow.isApplySymmetries();
	}

}