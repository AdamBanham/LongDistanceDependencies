package org.processmining.longdistancedependencies.choicedata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.longdistancedependencies.FixedMultiset;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.longdistancedependencies.function.Function;
import org.processmining.longdistancedependencies.function.FunctionFactory;
import org.processmining.longdistancedependencies.function.FunctionFactoryImpl;
import org.processmining.plugins.InductiveMiner.graphs.ConnectedComponents2;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphImplQuadratic;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lpsolve.LpSolveException;

/**
 * Parameters:
 * 
 * 0..n-1 base weight per transition
 * 
 * n..2n-1 adjustment weights for transition 0
 * 
 * 2n..3n-1 adjustment weights for transition 1 ...
 * 
 * @author sander
 *
 */
public class ChoiceData2Functions {

	public static final double fixValue = 1;

	public static Pair<List<Function>, List<Function>> convert(ChoiceData data, int numberOfTransitions,
			int[] fixParameters, IvMModel model) {

		FunctionFactory functionFactory = new FunctionFactoryImpl(fixValue, fixParameters);
		List<Function> equations = new ArrayList<>();
		List<Function> values = new ArrayList<>();

		ChoiceIterator it = data.iterator();
		while (it.hasNext()) {
			int[] history = it.next();
			int[] executedNext = it.getExecutedNext();

			if (FixedMultiset.setSizeLargerThanOne(executedNext)) {

				int transitionIndex = FixedMultiset.next(executedNext, -1);
				transitionIndex = FixedMultiset.next(executedNext, transitionIndex); //optimisation: one equality does not add any information
				while (transitionIndex >= 0) {

					Function a; //weight factor from log
					{
						double cardinality = sum(executedNext);
						a = functionFactory.division(functionFactory.constant(executedNext[transitionIndex]),
								functionFactory.constant(cardinality));
					}
					Function b; //above the division
					{
						b = getTransitionWeightFunction(history, transitionIndex, numberOfTransitions, functionFactory,
								model);
					}
					Function c; //below the division
					{
						Function[] elems = new Function[FixedMultiset.setSize(executedNext)];

						int transitionIndexTT = FixedMultiset.next(executedNext, -1);
						int elemIndex = 0;
						while (transitionIndexTT >= 0) {

							elems[elemIndex] = getTransitionWeightFunction(history, transitionIndexTT,
									numberOfTransitions, functionFactory, model);

							elemIndex++;
							transitionIndexTT = FixedMultiset.next(executedNext, transitionIndexTT);
						}

						c = functionFactory.sum(elems);
					}
					Function function = functionFactory.division(b, c);
					equations.add(function);
					values.add(a);

					transitionIndex = FixedMultiset.next(executedNext, transitionIndex);
				}
			}
		}

		return Pair.create(values, equations);
	}

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
	public static int[] getParametersToFix(ChoiceData data, int numberOfTransitions, IvMModel model,
			boolean assumeLogIsComplete, ProMCanceller canceller) throws LpSolveException {

		//find groups of dependent transitions
		List<Set<Integer>> groups;
		{
			Graph<Integer> graph = new GraphImplQuadratic<>(Integer.class);
			ChoiceIterator it = data.iterator();
			while (it.hasNext()) {
				it.next();
				int[] executedNext = it.getExecutedNext();

				//the transitions from executedNext appear together and must be merged
				int transitionIndex = FixedMultiset.next(executedNext, -1);
				int transitionIndexT = FixedMultiset.next(executedNext, transitionIndex);
				while (transitionIndexT >= 0) {

					graph.addEdge((Integer) transitionIndex, (Integer) transitionIndexT, 1);

					transitionIndex = transitionIndexT;
					transitionIndexT = FixedMultiset.next(executedNext, transitionIndexT);
				}
			}
			groups = ConnectedComponents2.compute(graph);
			System.out.println("components " + groups);
		}

		TIntList result = new TIntArrayList();

		/**
		 * Strategy 1: pick an arbitrary transition and fix all of its
		 * parameters.
		 */
		for (Set<Integer> component : groups) {
			int transition = preferredTransitionToFix(component, model);
			result.add(getParameterIndexBase(transition)); //base weight
			for (int transitionT = 0; transitionT < numberOfTransitions; transitionT++) {
				result.add(getParameterIndexAdjustment(transition, transitionT, numberOfTransitions)); //adjustment weight
			}
		}

		//		/**
		//		 * Strategy 2: find transitions that are mandatory and single for
		//		 * transition.
		//		 */
		//		{
		//			boolean[][] removed = new boolean[numberOfTransitions][numberOfTransitions];
		//			ChoiceIterator it = data.iterator();
		//			while (it.hasNext()) {
		//				int[] history = it.next();
		//				int[] executedNext = it.getExecutedNext();
		//
		//				for (int transition = 0; transition < numberOfTransitions; transition++) {
		//					if (executedNext[transition] >= 1) {
		//						for (int transitionT = 0; transitionT < numberOfTransitions; transitionT++) {
		//							if (history[transitionT] != 1) {
		//								removed[transition][transitionT] = true;
		//							}
		//						}
		//					}
		//				}
		//
		//			}
		//
		//			for (int transition = 0; transition < numberOfTransitions; transition++) {
		//				for (int transitionT = 0; transitionT < numberOfTransitions; transitionT++) {
		//					if (!removed[transition][transitionT]) {
		//						//transitionT is mandatory and single for transition; fix the corresponding parameter to 1
		//						result.add(getParameterIndexAdjustment(transition, transitionT, numberOfTransitions));
		//					}
		//				}
		//			}
		//		}

		if (assumeLogIsComplete) {
			result.addAll(FixParametersSequentialXor.getParametersToFix(model, data, canceller));
		} else {
			
		}

		return result.toArray();

	}

	public static int preferredTransitionToFix(Iterable<Integer> transitions, IvMModel model) {
		//prefer to fix taus
		for (int transition : transitions) {
			if (model.isTau(transition)) {
				return transition;
			}
		}

		return transitions.iterator().next();
	}

	public static Function getTransitionWeightFunction(int[] history, int transitionIndex, int numberOfTransitions,
			FunctionFactory functionFactory, IvMModel model) {
		Function b;
		Function[] elems = new Function[FixedMultiset.setSize(history) + 1];

		elems[0] = functionFactory.variable(getParameterIndexBase(transitionIndex),
				getParameterNameBase(transitionIndex, model)); //base weight

		int elemIndex = 1;
		int transitionIndexT = FixedMultiset.next(history, -1);
		while (transitionIndexT >= 0) {

			elems[elemIndex] = functionFactory.variablePower(
					getParameterIndexAdjustment(transitionIndex, transitionIndexT, numberOfTransitions),
					getParameterNameAdjustment(transitionIndex, transitionIndexT, numberOfTransitions, model),
					history[transitionIndexT]);

			transitionIndexT = FixedMultiset.next(history, transitionIndexT);
			elemIndex++;
		}

		b = functionFactory.product(elems);
		return b;
	}

	public static int getParameterIndexBase(int transitionIndex) {
		return transitionIndex;
	}

	public static String getParameterNameBase(int transitionIndex, IvMModel model) {
		return "\\parB_{" + model.getActivityName(transitionIndex) + "}";
	}

	public static int getParameterIndexAdjustment(int transitionIndex, int transitionIndexT, int numberOfTransitions) {
		return numberOfTransitions + transitionIndex * numberOfTransitions + transitionIndexT;
	}

	public static String getParameterNameAdjustment(int transitionIndex, int transitionIndexT, int numberOfTransitions,
			IvMModel model) {
		return "\\parD_{" + model.getActivityName(transitionIndex) + "," + model.getActivityName(transitionIndexT)
				+ "}";
	}

	public static int sum(int[] array) {
		int result = 0;
		for (int elem : array) {
			result += elem;
		}
		return result;
	}
}