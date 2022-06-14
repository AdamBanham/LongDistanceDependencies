package org.processmining.longdistancedependencies.choicedata;

import java.util.BitSet;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class FixParametersSequentialXor {

	public static int[] getParametersToFix(IvMModel model, ChoiceData choiceData, ProMCanceller canceller)
			throws LpSolveException {
		TIntArrayList result = new TIntArrayList();
		for (int transitionA = 0; transitionA < model.getMaxNumberOfNodes(); transitionA++) {
			System.out.println("transition " + transitionA);
			getParametersToFixForTransition(model, transitionA, result, choiceData, canceller);
		}

		return result.toArray();
	}

	public static int getNumberOfInSetVariables(int[] candidates) {
		return candidates.length * candidates.length;
	}

	public static int getNumberOfFixVariables(int[] candidates) {
		return candidates.length;
	}

	public static int getParameterInSet(int[] candidates, int candidateIndex, int set) {
		return candidates.length + //first the fix parameters
				candidateIndex * candidates.length + //candidate row
				set + //set column
				1; //lpsolve offset
	}

	public static int getParameterFix(int[] candidates, int candidateIndex) {
		return candidateIndex + 1;
	}

	public static void getParametersToFixForTransition(IvMModel model, int transitionA, TIntList result,
			ChoiceData choiceData, ProMCanceller canceller) throws LpSolveException {

		//first, select all transitions that appear in the history of A, and never twice
		int[] candidates = getTransitionsBefore(model, transitionA, choiceData);
		//		int[] transition2candidate = new int[model.getMaxNumberOfNodes()];
		//		{
		//			Arrays.fill(transition2candidate, -1);
		//			for (int candidateIndex = 0; candidateIndex < candidates.length; candidateIndex++) {
		//				transition2candidate[candidates[candidateIndex]] = candidateIndex;
		//			}
		//		}

		if (candidates.length == 0) {
			return;
		}

		//		System.out.println(" candidates " + Arrays.toString(candidates));

		LpSolve solver = LpSolve.makeLp(0, getNumberOfInSetVariables(candidates) + getNumberOfFixVariables(candidates));

		solver.setMaxim();
		solver.setDebug(false);
		solver.setVerbose(0);

		//set parameters binary
		for (int candidateIndexA = 0; candidateIndexA < candidates.length; candidateIndexA++) {
			solver.setBinary(getParameterFix(candidates, candidateIndexA), true);

			for (int set = 0; set < candidates.length; set++) {
				solver.setBinary(getParameterInSet(candidates, candidateIndexA, set), true);
			}
		}

		//set objective function
		{
			for (int candidateIndex = 0; candidateIndex < candidates.length; candidateIndex++) {
				solver.setObj(getParameterFix(candidates, candidateIndex),
						model.isTau(candidates[candidateIndex]) ? 1.01 : 1);
			}
		}

		if (canceller.isCancelled()) {
			return;
		}

		solver.setAddRowmode(true);

		//constraints: each set contains at most one fixed candidate
		{
			for (int candidateIndexA = 0; candidateIndexA < candidates.length; candidateIndexA++) {
				for (int candidateIndexB = candidateIndexA
						+ 1; candidateIndexB < candidates.length; candidateIndexB++) {
					for (int set = 0; set < candidates.length; set++) {
						int[] columns = new int[4];

						columns[0] = getParameterFix(candidates, candidateIndexA);
						columns[1] = getParameterInSet(candidates, candidateIndexA, set);
						columns[2] = getParameterFix(candidates, candidateIndexB);
						columns[3] = getParameterInSet(candidates, candidateIndexB, set);

						double[] weights = new double[4];
						weights[0] = 1;
						weights[1] = 1;
						weights[2] = 1;
						weights[3] = 1;

						solver.addConstraintex(columns.length, weights, columns, LpSolve.LE, 3);
					}
				}
			}
		}

		//constraints: set exclusive
		{
			ChoiceIterator it = choiceData.iterator();
			while (it.hasNext()) {
				int[] history = it.next();
				int[] next = it.getExecutedNext();

				if (next[transitionA] > 0) {
					//					System.out.println("  pre-fix: " + Arrays.toString(history));
					double[] weights = new double[candidates.length];
					for (int candidateIndex = 0; candidateIndex < candidates.length; candidateIndex++) {
						weights[candidateIndex] = history[candidates[candidateIndex]];
					}

					for (int set = 0; set < candidates.length; set++) {
						int columns[] = new int[candidates.length];
						for (int candidateIndex = 0; candidateIndex < candidates.length; candidateIndex++) {
							columns[candidateIndex] = getParameterInSet(candidates, candidateIndex, set);
						}

						solver.addConstraintex(columns.length, weights, columns, LpSolve.EQ, 1);
					}
				}
			}
		}

		//constraints: if fixed, then part of set
		{
			for (int candidateIndexA = 0; candidateIndexA < candidates.length; candidateIndexA++) {

				//ensure that the fixed parameters selected in earlier steps do not clash
				int transitionB = candidates[candidateIndexA];
				int parameterIndex = ChoiceData2Functions.getParameterIndexAdjustment(transitionA, transitionB,
						model.getMaxNumberOfNodes());
				if (result.contains(parameterIndex)) {
					//If the adjustment parameter is already fixed, then the solver should treat it as such.
					int[] columns = new int[1];
					double[] weights = new double[1];
					columns[0] = getParameterFix(candidates, candidateIndexA);
					weights[0] = 1;

					solver.addConstraintex(columns.length, weights, columns, LpSolve.EQ, 1);
				} else {
					//If the adjustment parameter is not fixed yet, the transition must be part of a set.
					int[] columns = new int[candidates.length + 1];
					double[] weights = new double[candidates.length + 1];

					for (int set = 0; set < candidates.length; set++) {
						columns[set] = getParameterInSet(candidates, candidateIndexA, set);
						weights[set] = -1;
					}

					columns[columns.length - 1] = getParameterFix(candidates, candidateIndexA);
					weights[weights.length - 1] = 1;

					solver.addConstraintex(columns.length, weights, columns, LpSolve.LE, 0);
				}
			}
		}

		solver.setAddRowmode(false);

		if (canceller.isCancelled()) {
			return;
		}

		//solver.printLp();

		System.out.println("  solving sequential-xor parameter fix");

		solver.solve();

		//solver.printSolution(candidates.length);

		double[] solution = solver.getPtrVariables();
		System.out.println("  score " + solver.getObjective());
		//System.out.println(" solution " + Arrays.toString(solution));
		System.out.print("  fix parameters for transition " + transitionA + ": ");
		for (int candidateIndex = 0; candidateIndex < candidates.length; candidateIndex++) {
			if (solution[candidateIndex] > 0) {
				int transitionB = candidates[candidateIndex];
				System.out.print(transitionB + ", ");
				result.add(ChoiceData2Functions.getParameterIndexAdjustment(transitionA, transitionB,
						model.getMaxNumberOfNodes()));
			}
		}

		System.out.println();
	}

	public static int[] getTransitionsBefore(IvMModel model, int transitionA, ChoiceData choiceData) {
		BitSet appeared = new BitSet();

		BitSet appearedTwice = new BitSet();
		ChoiceIterator it = choiceData.iterator();
		while (it.hasNext()) {
			int[] history = it.next();
			int[] next = it.getExecutedNext();

			if (next[transitionA] > 0) {
				for (int transitionB = 0; transitionB < model.getMaxNumberOfNodes(); transitionB++) {
					if (history[transitionB] > 0) {
						appeared.set(transitionB);
					}
					if (history[transitionB] > 1) {
						appearedTwice.set(transitionB);
					}
				}

			}
		}
		appeared.andNot(appearedTwice);

		int[] result = new int[appeared.cardinality()];
		int j = 0;
		for (int i = appeared.nextSetBit(0); i >= 0; i = appeared.nextSetBit(i + 1)) {
			result[j] = i;
			j++;
			if (i == Integer.MAX_VALUE) {
				break; // or (i+1) would overflow
			}
		}
		return result;
	}
}