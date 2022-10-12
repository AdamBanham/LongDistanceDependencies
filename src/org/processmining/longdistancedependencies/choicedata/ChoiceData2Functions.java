package org.processmining.longdistancedependencies.choicedata;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.processmining.longdistancedependencies.FixedMultiset;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.longdistancedependencies.function.Function;
import org.processmining.longdistancedependencies.function.FunctionFactory;
import org.processmining.longdistancedependencies.function.FunctionFactoryImpl;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

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
			BitSet enabledNext = it.getEnabledNext();

			if (enabledNext.cardinality() >= 2) { //if only one transition is enabled, there is nothing to equate

				int transitionIndex = enabledNext.nextSetBit(0);

				transitionIndex = enabledNext.nextSetBit(transitionIndex + 1); //optimisation: one equality does not add any information
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
						Function[] elems = new Function[enabledNext.cardinality()];

						int transitionIndexTT = enabledNext.nextSetBit(0);
						int elemIndex = 0;
						while (transitionIndexTT >= 0) {

							elems[elemIndex] = getTransitionWeightFunction(history, transitionIndexTT,
									numberOfTransitions, functionFactory, model);

							elemIndex++;
							transitionIndexTT = enabledNext.nextSetBit(transitionIndexTT + 1);
						}

						c = functionFactory.sum(elems);
					}
					Function function = functionFactory.division(b, c);
					if (!function.isConstant() || !a.isConstant()) {
						equations.add(function);
						values.add(a);
					}

					transitionIndex = enabledNext.nextSetBit(transitionIndex + 1);
				}
			}
		}

		return Pair.create(values, equations);
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
		return "\\parB_{" + model.getActivityName(transitionIndex) + " " + transitionIndex + "}";
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