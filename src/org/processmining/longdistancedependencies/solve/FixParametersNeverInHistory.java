package org.processmining.longdistancedependencies.solve;

import java.util.BitSet;
import java.util.Set;

import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;

import gnu.trove.TIntCollection;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class FixParametersNeverInHistory {

	public static TIntCollection fix(int numberOfTransitions, ChoiceData data, Set<Integer> group) {
		TIntList result = new TIntArrayList();
		for (int transitionA : group) {
			for (int transitionB = 0; transitionB < numberOfTransitions; transitionB++) {
				int parameter = ChoiceData2Functions.getParameterIndexAdjustment(transitionA, transitionB,
						numberOfTransitions);

				if (!inHistory(data, transitionA, transitionB)) {
					result.add(parameter);
				}
			}
		}
		return result;
	}

	public static boolean inHistory(ChoiceData data, int transitionA, int transitionB) {
		for (ChoiceIterator it = data.iterator(); it.hasNext();) {
			int[] history = it.next();
			BitSet enabled = it.getEnabledNext();

			if (enabled.get(transitionA) && history[transitionB] > 0) {
				return true;
			}
		}
		return false;
	}
}