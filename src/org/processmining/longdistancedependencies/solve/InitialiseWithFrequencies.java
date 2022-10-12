package org.processmining.longdistancedependencies.solve;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

public class InitialiseWithFrequencies {

	public static double[] guess(ChoiceData choiceData, IvMModel model, Set<Integer> group, int[] parametersToFix,
			int numberOfParameters) {
		//assume for now that there are no parameters fixed
		for (int transition : group) {
			assert !ArrayUtils.contains(parametersToFix, ChoiceData2Functions.getParameterIndexBase(transition));
		}

		System.out.println("apply initialisation");

		double[] result = new double[numberOfParameters];
		Arrays.fill(result, 1);
		
		result[ChoiceData2Functions.getParameterIndexBase(0)] = 1;
		result[ChoiceData2Functions.getParameterIndexBase(1)] = 0.8817;
		result[ChoiceData2Functions.getParameterIndexBase(2)] = 0.8817;
		result[ChoiceData2Functions.getParameterIndexBase(3)] = 0.73118;
		result[ChoiceData2Functions.getParameterIndexBase(4)] = 1;
		result[ChoiceData2Functions.getParameterIndexBase(5)] = 0.61827;
		result[ChoiceData2Functions.getParameterIndexBase(6)] = 1;
		result[ChoiceData2Functions.getParameterIndexBase(7)] = 0.645;
		
		return result;
	}
}