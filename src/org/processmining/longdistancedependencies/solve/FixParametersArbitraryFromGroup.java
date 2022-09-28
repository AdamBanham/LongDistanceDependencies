package org.processmining.longdistancedependencies.solve;

import java.util.Set;

import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.TIntCollection;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class FixParametersArbitraryFromGroup {
	public static TIntCollection fix(IvMModel model, Set<Integer> group, TIntCollection parametersAlreadyFixed) {
		//first, find the transition that would fix the most parameters
		int maxPotentialFix = -1;
		int maxPotentialFixTransition = -1;
		{
			for (int transitionA : group) {

				int potentialFixA = 0;
				int parameterB = ChoiceData2Functions.getParameterIndexBase(transitionA);
				if (!parametersAlreadyFixed.contains(parameterB)) {
					potentialFixA++;
				}
				for (int transitionB = 0; transitionB < model.getMaxNumberOfNodes(); transitionB++) {
					int parameterD = ChoiceData2Functions.getParameterIndexAdjustment(transitionA, transitionB,
							model.getMaxNumberOfNodes());
					if (!parametersAlreadyFixed.contains(parameterD)) {
						potentialFixA++;
					}
				}

				//if we can fix more parameters, or if we are a silent transition, then update the max
				if (potentialFixA > maxPotentialFix || (potentialFixA == maxPotentialFix && model.isTau(transitionA))) {
					potentialFixA = maxPotentialFix;
					maxPotentialFixTransition = transitionA;
				}
			}
		}

		TIntList result = new TIntArrayList();
		if (maxPotentialFixTransition != -1) {
			int transitionA = maxPotentialFixTransition;
			result.add(ChoiceData2Functions.getParameterIndexBase(transitionA)); //base weight
			for (int transitionB = 0; transitionB < model.getMaxNumberOfNodes(); transitionB++) {
				result.add(ChoiceData2Functions.getParameterIndexAdjustment(transitionA, transitionB,
						model.getMaxNumberOfNodes())); //adjustment weight
			}
		}
		return result;
	}
}