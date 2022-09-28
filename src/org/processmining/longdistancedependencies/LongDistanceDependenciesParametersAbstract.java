package org.processmining.longdistancedependencies;

import org.deckfour.xes.classification.XEventClassifier;

public abstract class LongDistanceDependenciesParametersAbstract implements LongDistanceDependenciesParameters {

	private XEventClassifier classifier;
	private boolean debug;
	private int numberofthreads;
	private boolean performPostProcessing;
	private boolean enableLongDistanceDependencies;
	private double alpha;

	public LongDistanceDependenciesParametersAbstract(XEventClassifier classifier, boolean debug, int numberofthreads,
			boolean performPostProcessing, boolean enableLongDistanceDependencies, double alpha) {
		this.classifier = classifier;
		this.debug = debug;
		this.numberofthreads = numberofthreads;
		this.performPostProcessing = performPostProcessing;
		this.enableLongDistanceDependencies = enableLongDistanceDependencies;
		this.alpha = alpha;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
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

	public boolean isPerformPostProcessing() {
		return performPostProcessing;
	}

	public void setPerformPostProcessing(boolean performPostProcessing) {
		this.performPostProcessing = performPostProcessing;
	}

	public boolean isEnableLongDistanceDependencies() {
		return enableLongDistanceDependencies;
	}

	public void setEnableLongDistanceDependencies(boolean enableLongDistanceDependencies) {
		this.enableLongDistanceDependencies = enableLongDistanceDependencies;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

}