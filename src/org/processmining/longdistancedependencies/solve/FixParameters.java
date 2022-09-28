package org.processmining.longdistancedependencies.solve;

import java.util.Set;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.longdistancedependencies.LongDistanceDependenciesParameters;
import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;
import org.processmining.longdistancedependencies.plugins.MineLongDistanceDependenciesPlugin;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.TIntCollection;
import gnu.trove.list.array.TIntArrayList;
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

		TIntCollection result = new TIntArrayList();

		/**
		 * Fix all parameters that have no meaning for the group
		 */
		result.addAll(Groups.fixParametersNotInGroup(model, group));
		MineLongDistanceDependenciesPlugin.debug(parameters,
				" remove parameters after groupless              " + result);

		/**
		 * Fix all parameters for which no observations have been made
		 */
		result.addAll(FixParametersNeverInHistory.fix(numberOfTransitions, data, group));
		MineLongDistanceDependenciesPlugin.debug(parameters,
				" remove parameters after not observed           " + result);

		/**
		 * Fix all parameters where B is mandatory before A anyway
		 */
		result.addAll(FixParametersAlwaysOnce.fix(numberOfTransitions, data, group));
		MineLongDistanceDependenciesPlugin.debug(parameters,
				" remove parameters after always once            " + result);

		/**
		 * Fix equivalent transitions
		 */
		result.addAll(FixParametersEquivalentTransitions.fix(numberOfTransitions, data, group));
		MineLongDistanceDependenciesPlugin.debug(parameters,
				" remove parameters after equivalent transitions " + result);

		/**
		 * Pick an arbitrary transition from the group and fix all of its
		 * parameters.
		 */
		result.addAll(FixParametersArbitraryFromGroup.fix(model, group, result));
		MineLongDistanceDependenciesPlugin.debug(parameters,
				" remove parameters after arbitrary group        " + result);

		/**
		 * Fix statistically independent parameters
		 */
		result.addAll(StatisticalTests.remove(numberOfTransitions, data, result, parameters.getAlpha()));
		MineLongDistanceDependenciesPlugin.debug(parameters, " remove parameters after tests                  " + result);

		/**
		 * Special case: no long-distance dependencies
		 */
		if (!parameters.isEnableLongDistanceDependencies()) {
			for (int transitionA = 0; transitionA < numberOfTransitions; transitionA++) {
				for (int transitionB = 0; transitionB < numberOfTransitions; transitionB++) {
					result.add(ChoiceData2Functions.getParameterIndexAdjustment(transitionA, transitionB,
							numberOfTransitions)); //adjustment weight
				}
			}
		}

		//		if (assumeLogIsComplete) {
		//			/**
		//			 * Strategy 2: find transitions that are mandatory and single for
		//			 * transition.
		//			 */
		//			{
		//				boolean[][] removed = new boolean[numberOfTransitions][numberOfTransitions];
		//				ChoiceIterator it = data.iterator();
		//				while (it.hasNext()) {
		//					int[] history = it.next();
		//					int[] executedNext = it.getExecutedNext();
		//
		//					for (int transition = 0; transition < numberOfTransitions; transition++) {
		//						if (executedNext[transition] >= 1) {
		//							for (int transitionT = 0; transitionT < numberOfTransitions; transitionT++) {
		//								if (history[transitionT] != 1) {
		//									removed[transition][transitionT] = true;
		//								}
		//							}
		//						}
		//					}
		//
		//				}
		//
		//				for (int transition = 0; transition < numberOfTransitions; transition++) {
		//					for (int transitionT = 0; transitionT < numberOfTransitions; transitionT++) {
		//						if (!removed[transition][transitionT]) {
		//							//transitionT is mandatory and single for transition; fix the corresponding parameter to 1
		//							result.add(getParameterIndexAdjustment(transition, transitionT, numberOfTransitions));
		//						}
		//					}
		//				}
		//			}
		//
		//			FixParametersSequentialXor.getParametersToFix(model, data, canceller, result);
		//		} else {
		//
		//		}

		result = new TIntHashSet(result);

		return result.toArray();

	}

}