package org.processmining.longdistancedependencies;

public interface StochasticLabelledPetriNetAdjustmentWeightsEditable
		extends StochasticLabelledPetriNetAdjustmentWeights {

	public void setTransitionBaseWeight(int transition, double weight);

	public void setTransitionAdjustmentWeight(int transitionTarget, int transitionHistory, double weight);

}