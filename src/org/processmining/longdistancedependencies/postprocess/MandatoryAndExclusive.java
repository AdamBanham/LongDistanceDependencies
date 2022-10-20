package org.processmining.longdistancedependencies.postprocess;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeightsEditable;
import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;

public class MandatoryAndExclusive {
	public static void postProcess(StochasticLabelledPetriNetAdjustmentWeightsEditable net, ChoiceData data,
			List<Set<Integer>> groups) {

		for (Set<Integer> group : groups) {
			/**
			 * All transitions in a group have a dependency on the same
			 * transition V, and transition V appears at most once for every
			 * transition of the group.
			 */
			for (int transitionV = 0; transitionV < net.getNumberOfTransitions(); transitionV++) {
				if (checkTransition(group, transitionV, data) && checkParameters(net, group, transitionV)) {
					//pick a, preferably silent, transition
					int transitionT = preferredTransitionToFix(group, net);
					double parameterValue = net.getTransitionAdjustmentWeight(transitionT, transitionV);

					net.setTransitionAdjustmentWeight(transitionT, transitionV, 1);
					for (int transitionTp : group) {
						if (transitionTp != transitionT) {
							net.setTransitionAdjustmentWeight(transitionTp, transitionV,
									net.getTransitionAdjustmentWeight(transitionTp, transitionV) / parameterValue);
						}
					}
				}
			}
		}
	}

	private static boolean checkParameters(StochasticLabelledPetriNetAdjustmentWeightsEditable net, Set<Integer> group,
			int transitionV) {
		for (int transitionT : group) {
			if (net.getTransitionAdjustmentWeight(transitionT, transitionV) == 1) {
				return false;
			}
		}
		return true;
	}

	private static boolean checkTransition(Set<Integer> group, int transitionV, ChoiceData data) {
		for (ChoiceIterator it = data.iterator(); it.hasNext();) {
			int[] history = it.next();
			BitSet enabled = it.getEnabledNext();

			if (history[transitionV] > 1) {
				for (int transitionT : group) {
					if (enabled.get(transitionT)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public static int preferredTransitionToFix(Set<Integer> group, StochasticLabelledPetriNet model) {
		//prefer to fix taus
		for (Iterator<Integer> it = group.iterator(); it.hasNext();) {
			int transition = it.next();
			if (model.isTransitionSilent(transition)) {
				return transition;
			}
		}

		return group.iterator().next();
	}
}