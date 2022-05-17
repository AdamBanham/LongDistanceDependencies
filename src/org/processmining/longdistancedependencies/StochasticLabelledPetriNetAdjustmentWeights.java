package org.processmining.longdistancedependencies;

import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;

public interface StochasticLabelledPetriNetAdjustmentWeights extends StochasticLabelledPetriNet {

	/**
	 * 
	 * @param transition
	 * @param a
	 *            history of transition executions that have already occurred in
	 *            this trace. int[] must be a fixed multiset, e.g. transition 1
	 *            is executed 4 times: [0, 4, ...]
	 * @return the weight of the transition.
	 */
	public double getTransitionWeight(int transition, int[] history);

}