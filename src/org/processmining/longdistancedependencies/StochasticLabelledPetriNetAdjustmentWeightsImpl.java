package org.processmining.longdistancedependencies;

import java.util.ArrayList;
import java.util.List;

import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetImpl;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemantics;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

public class StochasticLabelledPetriNetAdjustmentWeightsImpl extends StochasticLabelledPetriNetImpl
		implements StochasticLabelledPetriNetAdjustmentWeightsEditable {

	private TDoubleArrayList transitionBaseWeights;
	private List<TDoubleList> transitionAdjustmentFactors;

	public StochasticLabelledPetriNetAdjustmentWeightsImpl() {
		transitionBaseWeights = new TDoubleArrayList();
		transitionAdjustmentFactors = new ArrayList<>();
	}

	@Override
	public void setTransitionBaseWeight(int transition, double weight) {
		transitionBaseWeights.set(transition, weight);
	}

	@Override
	public int addTransition(String label, double baseWeight) {
		super.addTransition(label, baseWeight);
		transitionBaseWeights.add(baseWeight);

		int newNumberOfTransitions = transitionBaseWeights.size();

		for (TDoubleList list : transitionAdjustmentFactors) {
			list.add(1);
		}

		TDoubleArrayList newArray = new TDoubleArrayList(newNumberOfTransitions);
		for (int i = 0; i < newNumberOfTransitions; i++) {
			newArray.add(1);
		}
		transitionAdjustmentFactors.add(newArray);

		return newNumberOfTransitions - 1;
	}

	@Override
	public double getTransitionWeight(int transition, int[] history) {
		double baseWeight = transitionBaseWeights.get(transition);
		TDoubleList adjustmentFactors = transitionAdjustmentFactors.get(transition);

		for (int tIndex = 0; tIndex < history.length; tIndex++) {
			if (history[tIndex] > 0 && adjustmentFactors.get(tIndex) != 1) {
				baseWeight *= Math.pow(adjustmentFactors.get(tIndex), history[tIndex]);
			}
		}
		return baseWeight;
	}

	@Override
	public void setTransitionAdjustmentWeight(int transitionTarget, int transitionHistory, double weight) {
		transitionAdjustmentFactors.get(transitionTarget).set(transitionHistory, weight);
	}

	@Override
	public StochasticLabelledPetriNetSemantics getDefaultSemantics() {
		return new StochasticLabelledPetriNetSemanticsAdjustmentWeightsImpl(this);
	}
}