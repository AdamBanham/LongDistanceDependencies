package org.processmining.longdistancedependencies.choicedata;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.strategy.HashingStrategy;

public class ChoiceDataImpl implements ChoiceData {

	TCustomHashMap<int[], int[]> executed;
	THashMap<int[], BitSet> enabled;

	public ChoiceDataImpl() {
		executed = new TCustomHashMap<>(new HashingStrategy<int[]>() {

			private static final long serialVersionUID = -3776184259286538443L;

			public int computeHashCode(int[] object) {
				return Arrays.hashCode(object);
			}

			public boolean equals(int[] o1, int[] o2) {
				return Arrays.equals(o1, o2);
			}
		}, 10, 0.5f);
		enabled = new THashMap<>(10, 0.5f);
	}

	public void addExecution(int[] history, int executeNext, BitSet enabled) {
		int[] nextTransitions = new int[history.length];
		int[] nextTransitionsT = executed.putIfAbsent(history, nextTransitions);
		if (nextTransitionsT != null) {
			nextTransitions = nextTransitionsT;
		} else {
			this.enabled.put(history, enabled);
		}
		nextTransitions[executeNext]++;
	}

	public ChoiceIterator iterator() {
		Iterator<int[]> it = executed.keySet().iterator();
		int size = executed.size();
		return new ChoiceIterator() {

			int[] history;

			public int[] next() {
				history = it.next();
				return history;
			}

			public boolean hasNext() {
				return it.hasNext();
			}

			public int size() {
				return size;
			}

			public int[] getExecutedNext() {
				return executed.get(history);
			}

			public BitSet getEnabledNext() {
				return enabled.get(history);
			}
		};
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		for (ChoiceIterator it = iterator(); it.hasNext();) {
			int[] history = it.next();
			int[] executedNext = it.getExecutedNext();
			BitSet enabledNext = it.getEnabledNext();

			result.append(Arrays.toString(history));
			result.append(" ");
			result.append(Arrays.toString(executedNext));
			result.append(" ");
			result.append(enabledNext);
			result.append("\n");
		}

		return result.toString();
	}
}