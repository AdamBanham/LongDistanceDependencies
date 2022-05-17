package org.processmining.longdistancedependencies;

import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetEditable;

public interface StochasticLabelledPetriNetAdjustmentWeightsEditable
		extends StochasticLabelledPetriNetAdjustmentWeights, StochasticLabelledPetriNetEditable {

	public void setTransitionBaseWeight(int transition, double weight);

	public void setTransitionAdjustmentWeight(int transitionTarget, int transitionHistory, double weight);

}