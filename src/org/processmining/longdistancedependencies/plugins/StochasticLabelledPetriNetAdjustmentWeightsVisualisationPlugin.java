package org.processmining.longdistancedependencies.plugins;

import java.text.DecimalFormat;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeights;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.stochasticlabelledpetrinets.plugins.StochasticLabelledPetriNetVisualisationPlugin;

public class StochasticLabelledPetriNetAdjustmentWeightsVisualisationPlugin
		extends StochasticLabelledPetriNetVisualisationPlugin<StochasticLabelledPetriNetAdjustmentWeights> {

	@Plugin(name = "Stochastic labelled Petri net (adjustment weights) visualisation", returnLabels = {
			"Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
					"stochastic labelled Petri net", "canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Stochastic labelled Petri net visualisation", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, StochasticLabelledPetriNetAdjustmentWeights net,
			ProMCanceller canceller) {
		return visualise(net);
	}

	public void decoratePlace(StochasticLabelledPetriNetAdjustmentWeights net, int place, DotNode dotNode) {

	}

	public void decorateTransition(StochasticLabelledPetriNetAdjustmentWeights net, int transition, DotNode dotNode) {
		DecimalFormat f = new DecimalFormat("0.0000");
		f.setMaximumFractionDigits(4);
		f.setMinimumFractionDigits(0);

		StringBuilder label = new StringBuilder();
		label.append("<");

		label.append(f.format(net.getTransitionBaseWeight(transition)));
		for (int transitionHistory = 0; transitionHistory < net.getNumberOfTransitions(); transitionHistory++) {
			double adjustmentFactor = net.getTransitionAdjustmentWeight(transition, transitionHistory);
			if (adjustmentFactor != 1.0) {

				label.append("*");

				label.append(f.format(adjustmentFactor));
				label.append("^");
				if (net.isTransitionSilent(transitionHistory)) {
					label.append("tau" + transitionHistory);
				} else {
					label.append(net.getTransitionLabel(transitionHistory));
				}
			}
		}

		label.append(">");

		dotNode.setOption("xlabel", label.toString());
	}
}
