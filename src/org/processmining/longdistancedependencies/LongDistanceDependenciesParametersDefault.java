package org.processmining.longdistancedependencies;

import org.processmining.directlyfollowsmodelminer.mining.variants.DFMMiningParametersDefault;

public class LongDistanceDependenciesParametersDefault extends LongDistanceDependenciesParametersAbstract {

	public final static boolean debugDefault = false;
	public final static int numberOfThreadsDefault = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
	public final static boolean performPostProcessingDefault = true;
	public final static boolean enableLongDistanceDependenciesDefault = true;
	public final static double alphaDefault = 0.05;

	public LongDistanceDependenciesParametersDefault() {
		super(DFMMiningParametersDefault.defaultClassifier, debugDefault, numberOfThreadsDefault,
				performPostProcessingDefault, enableLongDistanceDependenciesDefault, alphaDefault);
	}
}