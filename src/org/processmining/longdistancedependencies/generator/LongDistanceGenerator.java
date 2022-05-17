package org.processmining.longdistancedependencies.generator;

import java.util.BitSet;
import java.util.Random;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemantics;

public class LongDistanceGenerator {

	private static XFactory factory = new XFactoryNaiveImpl();

	public static XLog generate(StochasticLabelledPetriNet net, int numberOfTraces) {
		StochasticLabelledPetriNetSemantics semantics = net.getDefaultSemantics();

		Random random = new Random(1);
		XLog result = factory.createLog();

		for (int i = 0; i < numberOfTraces; i++) {
			result.add(newTrace(semantics, random));
		}

		return result;
	}

	public static XTrace newTrace(StochasticLabelledPetriNetSemantics semantics, Random random) {
		XTrace result = factory.createTrace();

		semantics.setInitialState();

		while (!semantics.isFinalState()) {
			double totalWeight = semantics.getTotalWeightOfEnabledTransitions();

			double chosenWeight = random.nextDouble(totalWeight);
			BitSet enabledTransitions = semantics.getEnabledTransitions();
			for (int transition = enabledTransitions.nextSetBit(0); transition >= 0; transition = enabledTransitions
					.nextSetBit(transition + 1)) {

				chosenWeight -= semantics.getTransitionWeight(transition);
				if (chosenWeight <= 0) {
					//execute this transition

					if (!semantics.isTransitionSilent(transition)) {
						XEvent event = factory.createEvent();
						event.getAttributes().put(XConceptExtension.KEY_NAME, new XAttributeLiteralImpl(
								XConceptExtension.KEY_NAME, semantics.getTransitionLabel(transition)));
						result.add(event);
					}

					semantics.executeTransition(transition);
					break;
				}
			}
		}

		return result;
	}
}