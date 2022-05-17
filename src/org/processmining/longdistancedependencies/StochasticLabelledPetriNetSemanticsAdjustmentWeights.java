package org.processmining.longdistancedependencies;

import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemantics;

public interface StochasticLabelledPetriNetSemanticsAdjustmentWeights extends StochasticLabelledPetriNetSemantics {
	/**
	 * 
	 * @param transition
	 * @return the weight of the transition. This might depend on the state.
	 */
	public double getTransitionWeight(int transition);

	/**
	 * 
	 * @param enabledTransitions
	 * @return the sum of the weight of the enabled transitions
	 */
	public double getTotalWeightOfEnabledTransitions();
}