package org.processmining.longdistancedependencies.choicedata;

import java.util.Iterator;

public interface ChoiceData {

	public void addExecution(int[] history, int executeNext);

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
	}

}