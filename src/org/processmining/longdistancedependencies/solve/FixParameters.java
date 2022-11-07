package org.processmining.longdistancedependencies.solve;

import java.util.Set;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.longdistancedependencies.LongDistanceDependenciesParameters;
import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.TIntCollection;
import gnu.trove.set.hash.TIntHashSet;
import lpsolve.LpSolveException;

public class FixParameters {
	/**
	 * The parameters can be limited by fixing all parameters of one transition
	 * of each connected component. This function takes an transition from each
	 * connected component.
	 * 
	 * This assumes that the base weight parameters are numbered 0..n-1.
	 * 
	 * @param data
	 * @param parameters
	 * @param canceller
	 * @return
	 * @throws LpSolveException
	 */
	public static int[] getParametersToFix(ChoiceData data, IvMModel model, Set<Integer> group,
			LongDistanceDependenciesParameters parameters, ProMCanceller canceller) throws LpSolveException {

		int numberOfTransitions = model.getMaxNumberOfNodes();
		TIntCollection result = new TIntHashSet();

		//Fix all parameters that have no meaning for the group
		result.addAll(Groups.fixParametersNotInGroup(model, group));

		if (!parameters.isEnableLongDistanceDependencies()) {
			//no long-distance dependencies
			result.addAll(FixParametersNoLongDistanceDependencies.fix(numberOfTransitions));
		}

		if (parameters.isApplySymmetries()) {
			// Fix all parameters for which no observations have been made
			result.addAll(FixParametersNeverInHistory.fix(numberOfTransitions, data, group));

			// Fix all parameters where B is mandatory before A anyway
			result.addAll(FixParametersAlwaysOnce.fix(numberOfTransitions, data, group));

			// Fix all parameters where B and C appear the same number of times before A
			result.addAll(FixParametersEquivalentTransitions.fix(numberOfTransitions, data, group));

			// Pick an arbitrary transition from the group and fix all of its parameters.
			result.addAll(FixParametersArbitraryFromGroup.fix(model, group, result));
		}

		if (parameters.getAlpha() < 1) {
			// Fix statistically independent parameters
			result.addAll(StatisticalTests.fix(numberOfTransitions, data, result, parameters.getAlpha()));
		}

		return result.toArray();

	}

}