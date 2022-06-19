package org.processmining.longdistancedependencies;

import org.processmining.directlyfollowsmodelminer.mining.variants.DFMMiningParametersDefault;

public class LongDistanceDependenciesParametersDefault extends LongDistanceDependenciesParametersAbstract {

	public final static boolean assumeLogDefault = false;
	public final static boolean debugDefault = false;
	public final static int numberOfThreadsDefault = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

	public LongDistanceDependenciesParametersDefault() {
		super(DFMMiningParametersDefault.defaultClassifier, assumeLogDefault, debugDefault, numberOfThreadsDefault);
	}
}