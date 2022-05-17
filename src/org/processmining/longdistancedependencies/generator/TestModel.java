package org.processmining.longdistancedependencies.generator;

import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeights;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeightsEditable;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeightsImpl;

public class TestModel {
	public static StochasticLabelledPetriNetAdjustmentWeights generate() {
		StochasticLabelledPetriNetAdjustmentWeightsEditable result = new StochasticLabelledPetriNetAdjustmentWeightsImpl();

		int source = result.addPlace();
		int p1 = result.addPlace();
		int sink = result.addPlace();

		result.addPlaceToInitialMarking(source);

		int a = result.addTransition("a", 1);
		int b = result.addTransition("b", 1);
		int c = result.addTransition(1);

		result.addPlaceTransitionArc(source, a);
		result.addTransitionPlaceArc(a, p1);
		result.addPlaceTransitionArc(p1, b);
		result.addTransitionPlaceArc(b, p1);
		result.addPlaceTransitionArc(p1, c);
		result.addTransitionPlaceArc(c, sink);

		result.setTransitionAdjustmentWeight(b, b, 1.1);

		return result;
	}
}