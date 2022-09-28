package org.processmining.longdistancedependencies.solve;

import java.util.List;
import java.util.Set;

import org.processmining.longdistancedependencies.FixedMultiset;
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
		List<Set<Integer>> groups;
		{
			Graph<Integer> graph = new GraphImplQuadratic<>(Integer.class);
			ChoiceIterator it = data.iterator();
			while (it.hasNext()) {
				it.next();
				int[] executedNext = it.getExecutedNext();

				//the transitions from executedNext appear together and must be merged
				int transitionIndex = FixedMultiset.next(executedNext, -1);
				int transitionIndexT = FixedMultiset.next(executedNext, transitionIndex);
				while (transitionIndexT >= 0) {

					graph.addEdge((Integer) transitionIndex, (Integer) transitionIndexT, 1);

					transitionIndex = transitionIndexT;
					transitionIndexT = FixedMultiset.next(executedNext, transitionIndexT);
				}
			}
			groups = ConnectedComponents2.compute(graph);
		}
		return groups;
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