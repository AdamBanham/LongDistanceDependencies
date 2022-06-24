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
	public int addTransition(double baseWeight) {
		return super.addTransition(baseWeight);
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

	@Override
	public double getTransitionBaseWeight(int transition) {
		return transitionBaseWeights.get(transition);
	}

	@Override
	public double getTransitionAdjustmentWeight(int transitionOn, int transitionHistory) {
		return transitionAdjustmentFactors.get(transitionOn).get(transitionHistory);
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		for (int transition = 0; transition < getNumberOfTransitions(); transition++) {
			if (!isTransitionSilent(transition)) {
				result.append(getTransitionLabel(transition));
			} else {
				result.append("silent step #");
				result.append(transition);
			}
			result.append(": base weight ");
			result.append(getTransitionBaseWeight(transition));
			result.append(", \tadjustment factors: ");
			for (int transitionB = 0; transitionB < getNumberOfTransitions(); transitionB++) {
				if (!isTransitionSilent(transitionB)) {
					result.append(getTransitionLabel(transitionB));
				} else {
					result.append("silent step #");
					result.append(transitionB);
				}
				result.append(": ");
				result.append(getTransitionAdjustmentWeight(transition, transitionB));
				result.append(", ");
			}
			result.append("\n");
		}

		return result.toString();
	}
}