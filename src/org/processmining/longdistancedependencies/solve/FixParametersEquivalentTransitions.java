package org.processmining.longdistancedependencies.solve;

import java.util.BitSet;
import java.util.Set;

import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;

import gnu.trove.TIntCollection;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class FixParametersEquivalentTransitions {

	public static TIntCollection fix(int numberOfTransitions, ChoiceData data, Set<Integer> group) {
		TIntList result = new TIntArrayList();
		for (int transitionA : group) {
			BitSet equivalent = getEquivalent(numberOfTransitions, data, transitionA);

			for (int transitionB = equivalent.nextSetBit(0); transitionB >= 0; transitionB = equivalent
					.nextSetBit(transitionB + 1)) {
				result.add(ChoiceData2Functions.getParameterIndexAdjustment(transitionA, transitionB,
						numberOfTransitions)); //adjustment weight

				if (transitionB == Integer.MAX_VALUE) {
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Return the transitions that appear always together with another
	 * transition. We return true if the transition has an equivalent transition
	 * (the other equivalent transition is not reported).
	 * 
	 * @param model
	 * @param data
	 * @param transition
	 * @return
	 */
	public static BitSet getEquivalent(int numberOfTransitions, ChoiceData data, int transition) {
		BitSet result = new BitSet();

		for (int transitionA = 0; transitionA < numberOfTransitions - 1; transitionA++) {
			BitSet redundantWith = new BitSet();
			redundantWith.set(transitionA + 1, numberOfTransitions);

			for (ChoiceIterator it = data.iterator(); it.hasNext();) {
				int[] history = it.next();
				BitSet enabled = it.getEnabledNext();

				if (enabled.get(transition)) {
					for (int transitionB = transitionA + 1; transitionB < numberOfTransitions; transitionB++) {
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