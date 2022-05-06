package org.processmining.longdistancedependencies.choicedata;

import java.util.ArrayList;
import java.util.List;

import org.processmining.longdistancedependencies.FixedMultiset;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.longdistancedependencies.function.Constant;
import org.processmining.longdistancedependencies.function.Division;
import org.processmining.longdistancedependencies.function.Function;
import org.processmining.longdistancedependencies.function.Product;
import org.processmining.longdistancedependencies.function.Subtraction;
import org.processmining.longdistancedependencies.function.Sum;
import org.processmining.longdistancedependencies.function.Variable;
import org.processmining.longdistancedependencies.function.VariablePower;

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
	public static List<Function> convert(ChoiceData data, int numberOfTransitions) {
		List<Function> result = new ArrayList<>();

		ChoiceIterator it = data.iterator();
		while (it.hasNext()) {
			int[] history = it.next();
			int[] executedNext = it.getExecutedNext();

			if (FixedMultiset.setSizeLargerThanOne(executedNext)) {

				int transitionIndex = FixedMultiset.next(executedNext, -1);
				while (transitionIndex > 0) {

					Function a; //weight factor from log
					{
						double cardinality = sum(executedNext);
						a = new Constant(executedNext[transitionIndex] / cardinality);
					}
					Function b; //above the division
					{
						b = getTransitionWeightFunction(history, transitionIndex, numberOfTransitions);
					}
					Function c; //below the division
					{
						Function[] elems = new Function[FixedMultiset.setSize(executedNext)];

						int transitionIndexTT = FixedMultiset.next(executedNext, -1);
						int elemIndex = 0;
						while (transitionIndexTT >= 0) {

							elems[elemIndex] = getTransitionWeightFunction(history, transitionIndexTT,
									numberOfTransitions);

							elemIndex++;
							transitionIndexTT = FixedMultiset.next(executedNext, transitionIndexTT);
						}

						c = new Sum(elems);
					}
					Function function = new Subtraction(a, new Division(b, c));
					result.add(function);

					transitionIndex = FixedMultiset.next(executedNext, transitionIndex);
				}
			}
		}

		return result;
	}

	private static Function getTransitionWeightFunction(int[] history, int transitionIndex, int numberOfTransitions) {
		Function b;
		Function[] elems = new Function[FixedMultiset.setSize(history) + 1];

		elems[elems.length - 1] = new Variable(getParameterIndexBase(transitionIndex)); //base weight

		int elemIndex = 0;
		int transitionIndexT = FixedMultiset.next(history, -1);
		while (transitionIndexT >= 0) {

			elems[elemIndex] = new VariablePower(
					getParameterIndexAdjustment(transitionIndex, transitionIndexT, numberOfTransitions),
					history[transitionIndexT]);

			transitionIndexT = FixedMultiset.next(history, transitionIndexT);
			elemIndex++;
		}

		b = new Product(elems);
		return b;
	}

	private static int getParameterIndexBase(int transitionIndex) {
		return transitionIndex;
	}

	private static int getParameterIndexAdjustment(int transitionIndex, int transitionIndexT, int numberOfTransitions) {
		return transitionIndex * (numberOfTransitions + 1) + transitionIndexT;
	}

	public static int sum(int[] array) {
		int result = 0;
		for (int elem : array) {
			result += elem;
		}
		return result;
	}
}