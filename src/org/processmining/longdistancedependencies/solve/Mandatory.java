package org.processmining.longdistancedependencies.solve;

import java.util.BitSet;

import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

public class Mandatory {
	public static BitSet getMandatory(IvMModel model, ChoiceData data, int transition) {
		BitSet result = new BitSet();
		result.set(0, model.getMaxNumberOfNodes());
		for (ChoiceIterator it = data.iterator(); it.hasNext();) {
			int[] history = it.next();
			int[] next = it.getExecutedNext();

			if (next[transition] > 0) {
				for (int transitionB = 0; transitionB < history.length; transitionB++) {
					if (history[transitionB] != 1) {
						result.clear(transitionB);
					}
				}
			}

		}

		return result;
	}
}