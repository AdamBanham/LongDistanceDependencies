package org.processmining.longdistancedependencies;

import java.util.Arrays;

import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemanticsImpl;

public class StochasticLabelledPetriNetSemanticsAdjustmentWeightsImpl extends StochasticLabelledPetriNetSemanticsImpl {

	private int[] history;
	private final StochasticLabelledPetriNetAdjustmentWeights net;

	public StochasticLabelledPetriNetSemanticsAdjustmentWeightsImpl(StochasticLabelledPetriNetAdjustmentWeights net) {
		super(net);
		this.net = net;
		history = FixedMultiset.init(net.getNumberOfTransitions());
		Arrays.fill(history, 0);
	}

	@Override
	public void setInitialState() {
		super.setInitialState();
		if (history != null) {
			Arrays.fill(history, 0);
		}
	}

	@Override
	public void executeTransition(int transition) {
		super.executeTransition(transition);
		history[transition]++;
	}

	@Override
	public double getTransitionWeight(int transition) {
		return net.getTransitionWeight(transition, history);
	}

	@Override
	public double getTotalWeightOfEnabledTransitions() {
		double result = 0;
		for (int transition = enabledTransitions.nextSetBit(0); transition >= 0; transition = enabledTransitions
				.nextSetBit(transition + 1)) {
			result += net.getTransitionWeight(transition, history);
		}
		return result;
	}

}