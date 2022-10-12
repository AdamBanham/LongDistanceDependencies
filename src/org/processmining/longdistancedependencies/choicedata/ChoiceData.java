package org.processmining.longdistancedependencies.choicedata;

import java.util.BitSet;
import java.util.Iterator;

public interface ChoiceData {

	/**
	 * The input objects will NOT be copied and must not be changed after
	 * adding.
	 * 
	 * @param history
	 * @param executeNext
	 * @param enabled
	 */
	public void addExecution(int[] history, int executeNext, BitSet enabled);

	public ChoiceIterator iterator();

	/**
	 * next() returns the next history.
	 * 
	 * @author sander
	 *
	 */
	public interface ChoiceIterator extends Iterator<int[]> {
		public int size();

		public int[] getExecutedNext();

		public BitSet getEnabledNext();
	}

}