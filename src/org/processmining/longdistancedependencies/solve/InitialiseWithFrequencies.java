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
		
		for (int transition : group) {
			
		}
		
		return result;
	}
}