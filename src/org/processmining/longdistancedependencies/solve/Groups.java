package org.processmining.longdistancedependencies.solve;

import java.util.BitSet;
import java.util.List;
import java.util.Set;

import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData.ChoiceIterator;
import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;
import org.processmining.plugins.InductiveMiner.graphs.ConnectedComponents2;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphImplQuadratic;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.TIntCollection;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class Groups {

	public static List<Set<Integer>> getGroups(ChoiceData data) {
		//find groups of dependent transitions

		Graph<Integer> graph = new GraphImplQuadratic<>(Integer.class);
		
		ChoiceIterator it = data.iterator();
		while (it.hasNext()) {
			it.next();
			BitSet enabledNext = it.getEnabledNext();

			//the transitions from enabledNext appear together and must be merged
			for (int transitionA = enabledNext.nextSetBit(0); transitionA >= 0; transitionA = enabledNext
					.nextSetBit(transitionA + 1)) {
				for (int transitionB = enabledNext.nextSetBit(0); transitionB >= 0; transitionB = enabledNext
						.nextSetBit(transitionB + 1)) {
					graph.addEdge((Integer) transitionA, (Integer) transitionB, 1);
				}
			}
		}

		return ConnectedComponents2.compute(graph);

	}

	public static TIntCollection fixParametersNotInGroup(IvMModel model, Set<Integer> group) {
		TIntList result = new TIntArrayList();
		for (int transition = 0; transition < model.getMaxNumberOfNodes(); transition++) {
			if (!group.contains(transition)) {
				result.add(ChoiceData2Functions.getParameterIndexBase(transition)); //base weight
				for (int transitionT = 0; transitionT < model.getMaxNumberOfNodes(); transitionT++) {
					result.add(ChoiceData2Functions.getParameterIndexAdjustment(transition, transitionT,
							model.getMaxNumberOfNodes())); //adjustment weight
				}
			}
		}
		return result;
	}

	public static void copyResultsForGroup(IvMModel model, double[] source, double[] target, Set<Integer> group) {
		for (int transition = 0; transition < model.getMaxNumberOfNodes(); transition++) {
			if (group.contains(transition)) {
				int b = ChoiceData2Functions.getParameterIndexBase(transition);
				target[b] = source[b]; //base weight
				for (int transitionT = 0; transitionT < model.getMaxNumberOfNodes(); transitionT++) {
					int a = ChoiceData2Functions.getParameterIndexAdjustment(transition, transitionT,
							model.getMaxNumberOfNodes());
					target[a] = source[a]; //adjustment weight
				}
			}
		}
	}
}