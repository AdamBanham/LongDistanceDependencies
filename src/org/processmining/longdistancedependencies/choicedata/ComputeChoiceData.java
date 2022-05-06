package org.processmining.longdistancedependencies.choicedata;

import java.util.Arrays;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;

public class ComputeChoiceData<T> {
	public ChoiceData compute(IvMLogFiltered log, IvMModel model, ProMCanceller canceller) {
		ChoiceData result = new ChoiceDataImpl();
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();

			int[] nodeHistory = new int[model.getMaxNumberOfNodes()];

			ActivityInstanceIterator aIt = trace.activityInstanceIterator(model);
			while (aIt.hasNext()) {

				if (canceller.isCancelled()) {
					return null;
				}

				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> a = aIt.next();

				if (a != null) {
					IvMMove move = a.getF();
					if (move.isModelSync()) {
						int modelNode = move.getTreeNode();

						result.addExecution(nodeHistory, modelNode);
						nodeHistory = addToMultiset(nodeHistory, modelNode);

					}
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * @param multiset
	 * @param value
	 * @return a copy with one more value
	 */
	public static int[] addToMultiset(int[] multiset, int value) {
		int[] result = Arrays.copyOf(multiset, multiset.length);
		result[value]++;
		return result;
	}
}