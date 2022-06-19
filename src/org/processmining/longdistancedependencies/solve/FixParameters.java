package org.processmining.longdistancedependencies.solve;

import java.util.Iterator;
import java.util.Set;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
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
	 * @param canceller
	 * @return
	 * @throws LpSolveException
	 */
	public static int[] getParametersToFix(ChoiceData data, IvMModel model, Set<Integer> group, ProMCanceller canceller)
			throws LpSolveException {

		int numberOfTransitions = model.getMaxNumberOfNodes();

		TIntList result = new TIntArrayList();

		/**
		 * Strategy 1: fix all parameters that have no meaning for the group
		 */
		Groups.fixParametersNotInGroup(model, group, result);

		/**
		 * Strategy 2: pick an arbitrary transition from the group and fix all
		 * of its parameters.
		 */
		int transition = preferredTransitionToFix(group, model);
		result.add(ChoiceData2Functions.getParameterIndexBase(transition)); //base weight
		for (int transitionT = 0; transitionT < numberOfTransitions; transitionT++) {
			result.add(ChoiceData2Functions.getParameterIndexAdjustment(transition, transitionT, numberOfTransitions)); //adjustment weight
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

		return result.toArray();
	}

	public static int preferredTransitionToFix(Set<Integer> group, IvMModel model) {
		//prefer to fix taus
		for (Iterator<Integer> it = group.iterator(); it.hasNext();) {
			int transition = it.next();
			if (model.isTau(transition)) {
				return transition;
			}
		}

		return group.iterator().next();
	}
}