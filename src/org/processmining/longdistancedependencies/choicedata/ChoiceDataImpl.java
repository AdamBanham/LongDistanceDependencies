package org.processmining.longdistancedependencies.choicedata;

import java.util.Arrays;
import java.util.Iterator;

import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.strategy.HashingStrategy;

public class ChoiceDataImpl extends TCustomHashMap<int[], int[]> implements ChoiceData {

	public ChoiceDataImpl() {
		super(new HashingStrategy<int[]>() {

			private static final long serialVersionUID = -3776184259286538443L;

			public int computeHashCode(int[] object) {
				return Arrays.hashCode(object);
			}

			public boolean equals(int[] o1, int[] o2) {
				return Arrays.equals(o1, o2);
			}
		}, 10, 0.5f);
	}

	public void addExecution(int[] history, int executeNext) {
		int[] nextTransitions = new int[history.length];
		int[] nextTransitionsT = putIfAbsent(history, nextTransitions);
		if (nextTransitionsT != null) {
			nextTransitions = nextTransitionsT;
		}
		nextTransitions[executeNext]++;
	}

	public ChoiceIterator iterator() {
		Iterator<int[]> it = super.keySet().iterator();
		int size = size();
		return new ChoiceIterator() {

			int[] thisNextTransitions;

			public int[] next() {
				int[] history = it.next();
				thisNextTransitions = get(history);
				return history;
			}

			public boolean hasNext() {
				return it.hasNext();
			}

			public int size() {
				return size;
			}

			public int[] getExecutedNext() {
				return thisNextTransitions;
			}
		};
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		for (ChoiceIterator it = iterator(); it.hasNext();) {
			int[] history = it.next();
			int[] executedNext = it.getExecutedNext();

			result.append(Arrays.toString(history));
			result.append(" ");
			result.append(Arrays.toString(executedNext));
			result.append("\n");
		}

		return result.toString();
	}
}