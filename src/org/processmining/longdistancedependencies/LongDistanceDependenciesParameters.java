package org.processmining.longdistancedependencies;

import org.deckfour.xes.classification.XEventClassifier;

public interface LongDistanceDependenciesParameters {

	public XEventClassifier getClassifier();

	public boolean isAssumeLogIsComplete();

	public boolean isDebug();

}