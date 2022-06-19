package org.processmining.longdistancedependencies;

import org.deckfour.xes.classification.XEventClassifier;

public abstract class LongDistanceDependenciesParametersAbstract implements LongDistanceDependenciesParameters {

	private XEventClassifier classifier;
	private boolean assumeLog;
	private boolean debug;
	private int numberofthreads;

	public LongDistanceDependenciesParametersAbstract(XEventClassifier classifier, boolean assumeLog, boolean debug,
			int numberofthreads) {
		this.classifier = classifier;
		this.assumeLog = assumeLog;
		this.debug = debug;
		this.numberofthreads = numberofthreads;
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

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public int getNumberOfThreads() {
		return numberofthreads;
	}

	public void setNumberOfThreads(int numberofthreads) {
		this.numberofthreads = numberofthreads;
	}

}