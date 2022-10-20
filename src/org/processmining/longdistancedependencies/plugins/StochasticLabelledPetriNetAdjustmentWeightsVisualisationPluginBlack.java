package org.processmining.longdistancedependencies.plugins;

import java.awt.Color;
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
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.stochasticlabelledpetrinets.plugins.StochasticLabelledPetriNetVisualisationPlugin;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class StochasticLabelledPetriNetAdjustmentWeightsVisualisationPluginBlack
		extends StochasticLabelledPetriNetVisualisationPlugin<StochasticLabelledPetriNetAdjustmentWeights> {
	@Plugin(name = "Stochastic labelled Petri net (adjustment weights) visualisation - black", returnLabels = {
			"Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
					"stochastic labelled Petri net", "canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Stochastic labelled Petri net visualisation", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, StochasticLabelledPetriNetAdjustmentWeights net,
			ProMCanceller canceller) {
		return visualise(net);
	}

	@Override
	public DotPanel visualise(StochasticLabelledPetriNetAdjustmentWeights net) {
		String backgroundColour = "#011422";
		String modelEdgeColour = "#002fbb";
		String textColour = ColourMap.toHexString(Color.white);
		String dependencyEdgeColour = ColourMap.toHexString(Color.white);

		Dot dot = new Dot();

		dot.setOption("forcelabels", "true");

		TIntObjectMap<DotNode> place2dotNode = new TIntObjectHashMap<>(10, 0.5f, -1);
		TIntObjectMap<DotNode> transition2dotNode = new TIntObjectHashMap<>(10, 0.5f, -1);

		for (int place = 0; place < net.getNumberOfPlaces(); place++) {
			DotNode dotNode = dot.addNode("");
			dotNode.setOption("shape", "circle");
			place2dotNode.put(place, dotNode);

			if (net.isInInitialMarking(place) > 0) {
				dotNode.setOption("style", "filled");
				dotNode.setOption("fillcolor", "#80ff00");
			} else {
				dotNode.setOption("color", modelEdgeColour);
			}

			decoratePlace(net, place, dotNode);
		}

		for (int transition = 0; transition < net.getNumberOfTransitions(); transition++) {
			DotNode dotNode;

			if (net.isTransitionSilent(transition)) {
				dotNode = dot.addNode("" + transition);
				dotNode.setOption("style", "filled");
				dotNode.setOption("fillcolor", "#8EBAE5");
			} else {
				dotNode = dot.addNode(net.getTransitionLabel(transition));
			}

			dotNode.setOption("shape", "box");
			dotNode.setOption("color", modelEdgeColour);
			dotNode.setOption("fontcolor", textColour);

			decorateTransition(net, transition, dotNode);

			for (int place : net.getOutputPlaces(transition)) {
				DotEdge edge = dot.addEdge(dotNode, place2dotNode.get(place));
				edge.setOption("color", modelEdgeColour);
			}

			for (int place : net.getInputPlaces(transition)) {
				DotEdge edge = dot.addEdge(place2dotNode.get(place), dotNode);
				edge.setOption("color", modelEdgeColour);
			}

			transition2dotNode.put(transition, dotNode);
		}

		//add dependency edges
		DecimalFormat f = new DecimalFormat("0.0000");
		f.setMaximumFractionDigits(4);
		f.setMinimumFractionDigits(0);
		for (int transitionA = 0; transitionA < net.getNumberOfTransitions(); transitionA++) {
			for (int transitionB = 0; transitionB < net.getNumberOfTransitions(); transitionB++) {
				double adjustmentFactor = net.getTransitionAdjustmentWeight(transitionA, transitionB);
				if (adjustmentFactor != 1.0) {
					DotEdge edge = dot.addEdge(transition2dotNode.get(transitionB),
							transition2dotNode.get(transitionA));
					edge.setOption("constraint", "false");
					edge.setOption("color", dependencyEdgeColour);
					edge.setOption("fontcolor", textColour);
					edge.setLabel(f.format(adjustmentFactor));
				}
			}
		}

		dot.setOption("bgcolor", backgroundColour);
		DotPanel panel = new DotPanel(dot);
		return panel;
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
		//		for (int transitionHistory = 0; transitionHistory < net.getNumberOfTransitions(); transitionHistory++) {
		//			double adjustmentFactor = net.getTransitionAdjustmentWeight(transition, transitionHistory);
		//			if (adjustmentFactor != 1.0) {
		//
		//				label.append("*");
		//
		//				label.append(f.format(adjustmentFactor));
		//				label.append("^");
		//				if (net.isTransitionSilent(transitionHistory)) {
		//					label.append("tau" + transitionHistory);
		//				} else {
		//					label.append(net.getTransitionLabel(transitionHistory));
		//				}
		//			}
		//		}

		label.append(">");

		dotNode.setOption("xlabel", label.toString());
	}
}