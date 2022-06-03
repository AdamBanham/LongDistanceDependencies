package org.processmining.longdistancedependencies.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeights;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeightsEditable;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeightsImpl;

@Plugin(name = "Stochastic labelled Petri net with adjustment weights", parameterLabels = {
		"Filename" }, returnLabels = { "Stochastic labelled Petri net with adjustment weights" }, returnTypes = {
				StochasticLabelledPetriNetAdjustmentWeights.class })
@UIImportPlugin(description = "Stochastic labelled Petri net with adjustment weights files", extensions = { "slpna" })
public class StochasticLabelledPetriNetAdjustmentWeightsImportPlugin extends AbstractImportPlugin {
	public StochasticLabelledPetriNetAdjustmentWeights importFromStream(PluginContext context, InputStream input,
			String filename, long fileSizeInBytes) throws Exception {
		return read(input);
	}

	public static StochasticLabelledPetriNetAdjustmentWeightsEditable read(InputStream input)
			throws NumberFormatException, IOException {

		StochasticLabelledPetriNetAdjustmentWeightsEditable result = new StochasticLabelledPetriNetAdjustmentWeightsImpl();

		BufferedReader r = new BufferedReader(new InputStreamReader(input));

		int numberOfPlaces = Integer.parseInt(getNextLine(r));
		for (int place = 0; place < numberOfPlaces; place++) {
			result.addPlace();

			int inInitialMarking = Integer.parseInt(getNextLine(r));
			if (inInitialMarking > 0) {
				result.addPlaceToInitialMarking(place, inInitialMarking);
			}
		}

		int numberOfTransitions = Integer.parseInt(getNextLine(r));
		for (int transition = 0; transition < numberOfTransitions; transition++) {
			String line = getNextLine(r);
			double baseWeight = Double.valueOf(getNextLine(r));
			if (line.startsWith("silent")) {
				result.addTransition(baseWeight);
			} else if (line.startsWith("label ")) {
				result.addTransition(line.substring(6), baseWeight);
			} else {
				throw new RuntimeException("invalid transition");
			}

			//incoming places
			{
				int numberOfIncomingPlaces = Integer.parseInt(getNextLine(r));
				for (int p = 0; p < numberOfIncomingPlaces; p++) {
					int place = Integer.parseInt(getNextLine(r));
					result.addPlaceTransitionArc(place, transition);
				}
			}

			//outgoing places
			{
				int numberOfOutgoingPlaces = Integer.parseInt(getNextLine(r));
				for (int p = 0; p < numberOfOutgoingPlaces; p++) {
					int place = Integer.parseInt(getNextLine(r));
					result.addTransitionPlaceArc(transition, place);
				}
			}
		}

		//adjustment weights
		{
			for (int transition = 0; transition < numberOfTransitions; transition++) {
				for (int transitionT = 0; transitionT < numberOfTransitions; transitionT++) {
					double adjustmentWeight = Double.valueOf(getNextLine(r));
					result.setTransitionAdjustmentWeight(transition, transitionT, adjustmentWeight);
				}
			}
		}

		return result;
	}

	public static String getNextLine(BufferedReader r) throws IOException {
		String line = r.readLine();
		while (line != null && line.startsWith("#")) {
			line = r.readLine();
		}
		return line;
	}
}