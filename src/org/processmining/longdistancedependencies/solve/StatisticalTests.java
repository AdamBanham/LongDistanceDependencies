package org.processmining.longdistancedependencies.solve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;

import gnu.trove.TIntCollection;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class StatisticalTests {
	public static TIntCollection remove(int numberOfTransitions, ChoiceData data,
			TIntCollection alreadyRemovedParameters, double alpha) {
		int numberOfTests = getNumberOfTests(numberOfTransitions, alreadyRemovedParameters);

		TIntList result = new TIntArrayList();

		for (int transitionA = 0; transitionA < numberOfTransitions; transitionA++) {
			for (int transitionB = 0; transitionB < numberOfTransitions; transitionB++) {
				int parameter = ChoiceData2Functions.getParameterIndexAdjustment(transitionA, transitionB,
						numberOfTransitions);
				if (!alreadyRemovedParameters.contains(parameter)) {
					if (!isSignificant(transitionA, transitionB, data, numberOfTests, alpha)) {
						System.out.println("  remove " + transitionA + ", " + transitionB + " (par " + parameter + ")");
						result.add(parameter);
					}
				}
			}
		}

		return result;
	}

	public static boolean isSignificant(int transitionA, int transitionB, ChoiceData data, int numberOfTests,
			double alpha) {
		//get maximum occurrence of B before A
		int maxHistoryB = 0;
		{
			for (ChoiceIterator it = data.iterator(); it.hasNext();) {
				int[] history = it.next();
				int[] next = it.getExecutedNext();

				if (next[transitionA] > 0) {
					maxHistoryB = Math.max(maxHistoryB, history[transitionB]);
				}
			}
		}

		if (maxHistoryB == 0) {
			//if A never appears after B, then there's nothing to test
			System.out.println("   never occurs");
			return false;
		}

		//gather data
		long[][] counts = new long[maxHistoryB + 1][2];
		{
			for (ChoiceIterator it = data.iterator(); it.hasNext();) {
				int[] history = it.next();
				int[] next = it.getExecutedNext();

				if (next[transitionA] > 0) {
					int histB = history[transitionB];

					counts[histB][0] += sum(next) - next[transitionA];
					counts[histB][1] += next[transitionA];
				}
			}
		}

		//remove columns that are shorter than 5
		{
			List<Integer> toBeRemoved = new ArrayList<>();
			for (int i = 0; i < counts.length; i++) {
				for (int j = 0; j < counts[i].length; j++) {
					if (counts[i][j] < 5) {
						toBeRemoved.add(i);
					}
				}
			}

			if ((maxHistoryB + 1) - toBeRemoved.size() < 2) {
				//if there is not enough data, the pair is not significant
				System.out.println("   not enough data");
				return false;
			}

			Collections.reverse(toBeRemoved);
			for (int tbr : toBeRemoved) {
				counts = ArrayUtils.remove(counts, tbr);
			}
		}

		//perform the test
		return new ChiSquareTest().chiSquareTest(counts, alpha / numberOfTests);
	}

	public static int getNumberOfTests(int numberOfTransitions, TIntCollection alreadyRemovedParameters) {
		int result = 0;
		for (int transitionA = 0; transitionA < numberOfTransitions; transitionA++) {
			for (int transitionB = 0; transitionB < numberOfTransitions; transitionB++) {
				int parameter = ChoiceData2Functions.getParameterIndexAdjustment(transitionA, transitionB,
						numberOfTransitions);
				if (!alreadyRemovedParameters.contains(parameter)) {
					result++;
				}
			}
		}
		return result;
	}

	private static int sum(int[] arr) {
		int result = 0;
		for (int x : arr) {
			result += x;
		}
		return result;
	}
}