package org.processmining.longdistancedependencies.postprocess;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeights;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeightsEditable;
import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;

public class Alternatives {

	public static void postProcess(StochasticLabelledPetriNetAdjustmentWeightsEditable net, ChoiceData data) {
		for (int transitionT = 0; transitionT < net.getNumberOfTransitions(); transitionT++) {
			OptionsIterator it = new OptionsIterator(net, data, transitionT);

			while (it.hasNext()) {
				BitSet set = it.next();
				if (areParametersSuitable(set, net, transitionT)) {
					System.out.println("transition " + transitionT + ", set " + set);

					int transitionV = preferredTransitionToFix(set, net, transitionT);

					double parameter = net.getTransitionAdjustmentWeight(transitionT, transitionV);

					net.setTransitionBaseWeight(transitionT, net.getTransitionBaseWeight(transitionT) * parameter);
					net.setTransitionAdjustmentWeight(transitionT, transitionV, 1);
					for (int transitionU = set.nextSetBit(0); transitionU >= 0; transitionU = set
							.nextSetBit(transitionU + 1)) {
						if (transitionU != transitionV) {
							net.setTransitionAdjustmentWeight(transitionT, transitionU,
									net.getTransitionAdjustmentWeight(transitionT, transitionU) / parameter);
						}
					}
				}
			}
		}
	}

	public static enum Suitability {
		notyet, suitable, notsuitable
	}

	public static class OptionsIterator implements Iterator<BitSet> {

		BitSet state = new BitSet();
		private StochasticLabelledPetriNetAdjustmentWeights net;
		private ChoiceData data;
		private int transitionT;

		public OptionsIterator(StochasticLabelledPetriNetAdjustmentWeights net, ChoiceData data, int transitionT) {
			this.net = net;
			this.data = data;
			this.transitionT = transitionT;
			findNext();
		}

		public BitSet next() {
			BitSet result = (BitSet) state.clone();
			findNext();
			return result;
		}

		private void findNext() {
			Suitability suitability = Suitability.notyet;
			while (suitability != Suitability.suitable && state != null) {

				if (suitability == Suitability.notsuitable) {
					//move to the last sub-node of this tree, such that the next one will jump out again
					state.set(net.getNumberOfTransitions() - 1);
				}

				//move down in the tree and recurse
				int lastSet = state.previousSetBit(net.getNumberOfTransitions());

				if (lastSet != net.getNumberOfTransitions() - 1) {
					state.set(lastSet + 1);
				} else {
					state.clear(lastSet);
					int secondLastSet = state.previousSetBit(lastSet);

					if (secondLastSet == -1) {
						state = null;
						return;
					}

					state.clear(secondLastSet);
					state.set(secondLastSet + 1);
				}

				suitability = isSetSuitable(state, data, transitionT);
				//				System.out.println(state + " " + suitability);
			}
		}

		public boolean hasNext() {
			return state != null;
		}
	}

	public static boolean areParametersSuitable(BitSet set, StochasticLabelledPetriNetAdjustmentWeights net,
			int transitionT) {
		for (int transitionTp = set.nextSetBit(0); transitionTp >= 0; transitionTp = set.nextSetBit(transitionTp + 1)) {

			if (net.getTransitionAdjustmentWeight(transitionT, transitionTp) == 1) {
				return false;
			}

			if (transitionTp == Integer.MAX_VALUE) {
				break;
			}
		}
		return true;
	}

	public static Suitability isSetSuitable(BitSet set, ChoiceData data, int transitionT) {
		Suitability result = Suitability.suitable;

		BitSet atAllExecuted = new BitSet();

		for (ChoiceIterator it = data.iterator(); it.hasNext();) {
			int[] history = it.next();
			int[] next = it.getExecutedNext();

			if (next[transitionT] > 0) {

				int sum = 0;
				for (int transitionTp = set.nextSetBit(0); transitionTp >= 0; transitionTp = set
						.nextSetBit(transitionTp + 1)) {
					sum += history[transitionTp];

					if (history[transitionTp] > 0) {
						atAllExecuted.set(transitionTp);
					}

					if (transitionTp == Integer.MAX_VALUE) {
						break;
					}
				}

				if (sum > 1) {
					return Suitability.notsuitable;
				}

				if (sum == 0) {
					result = Suitability.notyet;
				}
			}
		}

		//every transition must have been executed at least once
		BitSet clone = (BitSet) set.clone();
		clone.andNot(atAllExecuted);
		if (!clone.isEmpty()) {
			return Suitability.notsuitable;
		}

		return result;
	}

	public static int preferredTransitionToFix(BitSet set, StochasticLabelledPetriNetAdjustmentWeights net,
			int transitionT) {
		//prefer to fix transitions that would make other parameters 1 as well
		List<Integer> candidates = new ArrayList<>();
		int max = 0;
		for (int candidate = set.nextSetBit(0); candidate >= 0; candidate = set.nextSetBit(candidate + 1)) {
			int number = getNumberOfResetParameters(net, set, transitionT, candidate);
			if (number > max) {
				candidates.clear();
				max = number;
			}
			if (number == max) {
				candidates.add(candidate);
			}
		}

		//prefer to fix taus
		for (int transition : candidates) {
			if (net.isTransitionSilent(transition)) {
				return transition;
			}
		}

		//return set.previousSetBit(model.getNumberOfTransitions());
		return candidates.get(0);
	}

	public static int getNumberOfResetParameters(StochasticLabelledPetriNetAdjustmentWeights net, BitSet set,
			int transitionT, int candidate) {
		int result = 0;

		double parameter = net.getTransitionAdjustmentWeight(transitionT, candidate);

		if (net.getTransitionBaseWeight(transitionT) * parameter == 1) {
			result++;
		}

		for (int transitionX = set.nextSetBit(0); transitionX >= 0; transitionX = set.nextSetBit(transitionX + 1)) {
			if (net.getTransitionAdjustmentWeight(transitionT, transitionX) / parameter == 1) {
				result++;
			}
		}

		return result;
	}
}