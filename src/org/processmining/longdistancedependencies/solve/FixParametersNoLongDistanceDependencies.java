package org.processmining.longdistancedependencies.solve;

import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;

import gnu.trove.TIntCollection;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class FixParametersNoLongDistanceDependencies {

	public static TIntCollection fix(int numberOfTransitions) {
		TIntList result = new TIntArrayList();
		for (int transitionA = 0; transitionA < numberOfTransitions; transitionA++) {
			for (int transitionB = 0; transitionB < numberOfTransitions; transitionB++) {
				result.add(ChoiceData2Functions.getParameterIndexAdjustment(transitionA, transitionB,
						numberOfTransitions)); //adjustment weight
			}
		}
		return result;
	}

}
