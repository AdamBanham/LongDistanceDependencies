package org.processmining.longdistancedependencies;

import org.processmining.directlyfollowsmodelminer.mining.variants.DFMMiningParametersDefault;

public class LongDistanceDependenciesParametersDefault extends LongDistanceDependenciesParametersAbstract {

	private final static boolean assumeLogDefault = false;

	public LongDistanceDependenciesParametersDefault() {
		super(DFMMiningParametersDefault.defaultClassifier, assumeLogDefault);
	}
}