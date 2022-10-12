package org.processmining.longdistancedependencies.choicedata;

import java.util.BitSet;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.longdistancedependencies.FixedMultiset;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemantics;

public class ComputeChoiceData {
	public static ChoiceData compute(IvMLogFiltered log, StochasticLabelledPetriNet net, ProMCanceller canceller) {
		ChoiceData result = new ChoiceDataImpl();

		StochasticLabelledPetriNetSemantics semantics = net.getDefaultSemantics();

		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			semantics.setInitialState();

			int[] history = FixedMultiset.init(semantics.getNumberOfTransitions());

			for (IvMMove move : trace) {
				if (move.isModelSync() && move.isComplete()) {
					int transition = move.getTreeNode();
					

					BitSet enabled = (BitSet) semantics.getEnabledTransitions().clone();

					result.addExecution(history, transition, enabled);
					
					history = FixedMultiset.copyAdd(history, transition);
					semantics.executeTransition(transition);
				}
			}

			if (canceller.isCancelled()) {
				return null;
			}

		}
		return result;
	}
}