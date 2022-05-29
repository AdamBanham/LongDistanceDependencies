package org.processmining.longdistancedependencies;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Converts the Petri net into a stochastic labelled Petri net, but doesn't put
 * any weights. Notice that this method assumes that the Petri net ends in a
 * deadlock, and that every deadlock is accepting.
 * 
 * @author sander
 *
 */
public class AcceptingPetriNet2StochasticLabelledPetriNetAdjustmentWeights {
	public static StochasticLabelledPetriNetAdjustmentWeightsEditable convert(Petrinet oldNet,
			Marking oldInitialMarking) {
		StochasticLabelledPetriNetAdjustmentWeightsEditable result = new StochasticLabelledPetriNetAdjustmentWeightsImpl();

		TObjectIntMap<Place> oldPlace2place = new TObjectIntHashMap<>();
		for (Place oldPlace : oldNet.getPlaces()) {
			int place = result.addPlace();
			oldPlace2place.put(oldPlace, place);
		}

		for (Place oldPlace : oldInitialMarking) {
			result.addPlaceToInitialMarking(oldPlace2place.get(oldPlace), oldInitialMarking.occurrences(oldPlace));
		}

		for (Transition oldTransition : oldNet.getTransitions()) {
			int newTransition;
			if (oldTransition.isInvisible()) {
				newTransition = result.addTransition(0);
			} else {
				newTransition = result.addTransition(oldTransition.getLabel(), 0);
			}

			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : oldNet.getInEdges(oldTransition)) {
				Place oldSource = (Place) edge.getSource();
				result.addPlaceTransitionArc(oldPlace2place.get(oldSource), newTransition);
			}
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : oldNet
					.getOutEdges(oldTransition)) {
				Place oldTarget = (Place) edge.getTarget();
				result.addTransitionPlaceArc(newTransition, oldPlace2place.get(oldTarget));
			}
		}

		return result;
	}
}