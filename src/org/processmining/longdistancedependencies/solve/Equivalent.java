package org.processmining.longdistancedependencies.solve;

import java.util.BitSet;

import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

public class Equivalent {
	/**
	 * Return the transitions that appear always together with another
	 * transition. We return true if the transition has an equivalent transition
	 * (and one equivalent transition is not reported).
	 * 
	 * @param model
	 * @param data
	 * @param transition
	 * @return
	 */
	public static BitSet getEquivalent(IvMModel model, ChoiceData data, int transition) {
		BitSet result = new BitSet();

		for (int transitionA = 0; transitionA < model.getMaxNumberOfNodes() - 1; transitionA++) {
			BitSet redundantWith = new BitSet();
			redundantWith.set(transitionA + 1, model.getMaxNumberOfNodes());

			for (ChoiceIterator it = data.iterator(); it.hasNext();) {
				int[] history = it.next();
				int[] next = it.getExecutedNext();

				if (next[transition] > 0) {
					for (int transitionB = transitionA + 1; transitionB < model.getMaxNumberOfNodes(); transitionB++) {
						if (history[transitionA] != history[transitionB]) {
							redundantWith.clear(transitionB);
						}
					}
				}
			}

			if (redundantWith.cardinality() > 0) {
				result.set(transitionA);
			}
		}

		return result;
	}
}