package org.processmining.longdistancedependencies.choicedata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.processmining.longdistancedependencies.FixedMultiset;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.longdistancedependencies.function.Function;
import org.processmining.longdistancedependencies.function.FunctionFactory;
import org.processmining.longdistancedependencies.function.FunctionFactoryImpl;
import org.processmining.plugins.InductiveMiner.graphs.ConnectedComponents2;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphImplQuadratic;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

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
			int[] fixParameters) {

		FunctionFactory functionFactory = new FunctionFactoryImpl(fixValue, fixParameters);
		List<Function> equations = new ArrayList<>();
		List<Function> values = new ArrayList<>();

		ChoiceIterator it = data.iterator();
		while (it.hasNext()) {
			int[] history = it.next();
			int[] executedNext = it.getExecutedNext();

			if (FixedMultiset.setSizeLargerThanOne(executedNext)) {

				int transitionIndex = FixedMultiset.next(executedNext, -1);
				while (transitionIndex >= 0) {

					Function a; //weight factor from log
					{
						double cardinality = sum(executedNext);
						a = functionFactory.division(functionFactory.constant(executedNext[transitionIndex]),
								functionFactory.constant(cardinality));
					}
					Function b; //above the division
					{
						b = getTransitionWeightFunction(history, transitionIndex, numberOfTransitions, functionFactory);
					}
					Function c; //below the division
					{
						Function[] elems = new Function[FixedMultiset.setSize(executedNext)];

						int transitionIndexTT = FixedMultiset.next(executedNext, -1);
						int elemIndex = 0;
						while (transitionIndexTT >= 0) {

							elems[elemIndex] = getTransitionWeightFunction(history, transitionIndexTT,
									numberOfTransitions, functionFactory);

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
	 * @return
	 */
	public static int[] getParametersToFix(ChoiceData data, int numberOfTransitions) {

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

		List<Set<Integer>> components = ConnectedComponents2.compute(graph);
		System.out.println("components " + components);

		//pick an arbitrary transition and add all of its parameters
		TIntList result = new TIntArrayList();
		for (Set<Integer> component : components) {
			int transition = component.iterator().next();
			result.add(transition); //base weight
			for (int i = 0; i < numberOfTransitions; i++) {
				result.add((transition + 1) * numberOfTransitions + i);
			}
		}

		return result.toArray();
	}

	public static Function getTransitionWeightFunction(int[] history, int transitionIndex, int numberOfTransitions,
			FunctionFactory functionFactory) {
		Function b;
		Function[] elems = new Function[FixedMultiset.setSize(history) + 1];

		elems[0] = functionFactory.variable(getParameterIndexBase(transitionIndex)); //base weight

		int elemIndex = 1;
		int transitionIndexT = FixedMultiset.next(history, -1);
		while (transitionIndexT >= 0) {

			elems[elemIndex] = functionFactory.variablePower(
					getParameterIndexAdjustment(transitionIndex, transitionIndexT, numberOfTransitions),
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

	public static int getParameterIndexAdjustment(int transitionIndex, int transitionIndexT, int numberOfTransitions) {
		return numberOfTransitions + transitionIndex * numberOfTransitions + transitionIndexT;
	}

	public static int sum(int[] array) {
		int result = 0;
		for (int elem : array) {
			result += elem;
		}
		return result;
	}
}