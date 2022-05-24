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
		int c = result.addTransition("c", 1);
		int d = result.addTransition("d", 1);

		result.addPlaceTransitionArc(source, a);
		result.addPlaceTransitionArc(source, b);
		result.addTransitionPlaceArc(a, p1);
		result.addTransitionPlaceArc(b, p1);
		result.addPlaceTransitionArc(p1, c);
		result.addPlaceTransitionArc(p1, d);
		result.addTransitionPlaceArc(c, sink);
		result.addTransitionPlaceArc(d, sink);
		
		result.setTransitionAdjustmentWeight(c, a, 2);

		return result;
	}
}