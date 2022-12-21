package org.processmining.longdistancedependencies.solve;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.longdistancedependencies.LongDistanceDependenciesParameters;
import org.processmining.longdistancedependencies.LongDistanceDependenciesParametersWrapper;
import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;
import org.processmining.longdistancedependencies.choicedata.Equation;
import org.processmining.longdistancedependencies.plugins.MineLongDistanceDependenciesPlugin;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import lpsolve.LpSolveException;

public class InitialiseWithFrequencies {

	public static double[] guess(ChoiceData choiceData, IvMModel model, Set<Integer> group, int numberOfParameters,
			LongDistanceDependenciesParameters parameters, ProMCanceller canceller) throws LpSolveException {

		if (parameters.isEnableLongDistanceDependencies()) {
			//if the long-distance dependencies are enabled, we guess the base weights first, separately, as an initialisation

			MineLongDistanceDependenciesPlugin.debug(parameters, " start initial guess run");

			LongDistanceDependenciesParameters parametersGuess = new LongDistanceDependenciesParametersWrapper(
					parameters);
			int[] parametersToFixGuess = FixParameters.getParametersToFix(choiceData, model, group, parametersGuess,
					canceller);
			double[] initialParameterGuess = new double[numberOfParameters];
			Arrays.fill(initialParameterGuess, 1);
			List<Equation> equationsGuess = ChoiceData2Functions.convert(choiceData, model.getMaxNumberOfNodes(),
					parametersToFixGuess, model);
			double[] groupResult = Solver.solve(equationsGuess, numberOfParameters, parametersToFixGuess,
					initialParameterGuess);

			MineLongDistanceDependenciesPlugin.debug(parameters, " result of initial guess run: " + groupResult);

			return groupResult;
		}

		double[] result = new double[numberOfParameters];
		Arrays.fill(result, 1);
		return result;
	}
}