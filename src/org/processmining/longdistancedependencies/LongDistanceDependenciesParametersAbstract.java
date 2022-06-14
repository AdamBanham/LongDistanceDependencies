package org.processmining.longdistancedependencies;

import org.deckfour.xes.classification.XEventClassifier;

public abstract class LongDistanceDependenciesParametersAbstract implements LongDistanceDependenciesParameters {

	private XEventClassifier classifier;
	private boolean assumeLog;

	public LongDistanceDependenciesParametersAbstract(XEventClassifier classifier, boolean assumeLog) {
		this.classifier = classifier;
		this.assumeLog = assumeLog;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public boolean isAssumeLogIsComplete() {
		return assumeLog;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public void setLogAssumption(boolean assumeLog) {
		this.assumeLog = assumeLog;
	}

}