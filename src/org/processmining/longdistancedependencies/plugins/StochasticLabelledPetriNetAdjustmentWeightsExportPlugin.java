package org.processmining.longdistancedependencies.plugins;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeights;

@SuppressWarnings("deprecation")
@Plugin(name = "Stochastic labelled Petri net with adjustment weights exporter", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Stochastic labelled Petri net with adjustment weights", "File" }, userAccessible = true)
@UIExportPlugin(description = "Stochastic labelled Petri net", extension = "slpna")
public class StochasticLabelledPetriNetAdjustmentWeightsExportPlugin {
	@PluginVariant(variantLabel = "Dfg export (Directly follows graph)", requiredParameterLabels = { 0, 1 })
	public void exportDefault(UIPluginContext context, StochasticLabelledPetriNetAdjustmentWeights net, File file)
			throws IOException {
		export(net, file);
	}

	public static void export(StochasticLabelledPetriNetAdjustmentWeights net, File file) throws IOException {
		PrintWriter w = null;
		try {
			w = new PrintWriter(file);
			w.println("# number of places");
			w.println(net.getNumberOfPlaces());

			w.println("# initial marking");
			for (int place = 0; place < net.getNumberOfPlaces(); place++) {
				w.println(net.isInInitialMarking(place));
			}

			w.println("# number of transitions");
			w.println(net.getNumberOfTransitions());
			for (int transition = 0; transition < net.getNumberOfTransitions(); transition++) {
				w.println("# transition " + transition);
				if (net.isTransitionSilent(transition)) {
					w.println("silent");
				} else {
					w.println("label " + StringEscapeUtils.escapeJava(net.getTransitionLabel(transition)));
				}
				w.println("# base weight ");
				w.println(net.getTransitionBaseWeight(transition));

				w.println("# number of input places");
				w.println(net.getInputPlaces(transition).length);
				for (int place : net.getInputPlaces(transition)) {
					w.println(place);
				}

				w.println("# number of output places");
				w.println(net.getOutputPlaces(transition).length);
				for (int place : net.getOutputPlaces(transition)) {
					w.println(place);
				}

				w.println("# adjustment weights");
				for (int transitionT = 0; transitionT < net.getNumberOfTransitions(); transitionT++) {
					w.println(net.getTransitionAdjustmentWeight(transition, transitionT));
				}
			}
		} finally {
			if (w != null) {
				w.close();
			}
		}
	}
}
