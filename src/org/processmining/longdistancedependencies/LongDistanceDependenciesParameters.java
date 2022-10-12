package org.processmining.longdistancedependencies;

import org.deckfour.xes.classification.XEventClassifier;

public interface LongDistanceDependenciesParameters {

	public XEventClassifier getClassifier();

	public boolean isDebug();

	public int getNumberOfThreads();

	public boolean isPerformPostProcessing();
	
	public boolean isEnableLongDistanceDependencies();

	public double getAlpha();
	
	public boolean isApplySymmetries();
}