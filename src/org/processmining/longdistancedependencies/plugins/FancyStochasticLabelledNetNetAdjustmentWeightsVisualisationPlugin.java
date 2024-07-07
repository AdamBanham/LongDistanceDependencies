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
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class FancyStochasticLabelledNetNetAdjustmentWeightsVisualisationPlugin
		extends StochasticLabelledPetriNetAdjustmentWeightsVisualisationPlugin {

	@Plugin(
			name = "(Prettier) Stochastic labelled Petri net "
					+ "(adjustment weights) visualisation",
			returnLabels = {"Dot visualization" }, 
			returnTypes = { JComponent.class }, 
			parameterLabels = {
					"stochastic labelled Petri net", 
					"canceller" }, 
			userAccessible = true, 
			level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(
			affiliation = "QUT",
			author = "Adam Banham",
			email = "adam_banham@hotmail.com")
	@PluginVariant(
			variantLabel = "(Prettier) Stochastic labelled Petri net visualisation",
			requiredParameterLabels = { 0, 1 })
	public JComponent visualise(
			final PluginContext context,
			StochasticLabelledPetriNetAdjustmentWeights net,
			ProMCanceller canceller) {
		DotPanel panel = visualise(net);
		Dot dot = panel.getDot();
		dot.setOption("bgcolor", "none");
		return new DotPanel(dot);
	}
	
	public static final String PlaceFill = "#f2f2f2";
	public static final String StartingPlaceFill = "#80ff00";
	public static final String EndingPlaceFill = "#FF3939";
	public static final String TransitionFill = "#e9c6af";
	public static final String TauFill = "#808080";
	public static final String WeightFill = "#c0bbbb";
	
	public void decoratePlace(
			StochasticLabelledPetriNetAdjustmentWeights net, 
			int place, 
			DotNode dotNode) {
		dotNode.setOption("style", "filled");
		if (net.isInInitialMarking(place) == 1) {
			dotNode.setOption("fillcolor", StartingPlaceFill);
		} else if (net.getOutputTransitions(place).length == 0) {
			dotNode.setOption("fillcolor", EndingPlaceFill);
		} else {
			dotNode.setOption("fillcolor", PlaceFill);
		}
	}
	
	public String getTransitionLabel(
			StochasticLabelledPetriNetAdjustmentWeights net,
			int transition) {
		String label = "";
		if (net.isTransitionSilent(transition)) {
			label = "&#120591;";
		} else {
			label = net.getTransitionLabel(transition);
		}
		return "<FONT>"
			  +	label
			  + "<FONT POINT-SIZE=\"10\">"
			  + " ("
			  + transition
			  + ")"
			  + "</FONT>"
			  + "</FONT>";
	}
	
	public void decorateTransition(StochasticLabelledPetriNetAdjustmentWeights net, int transition, DotNode dotNode) {
		DecimalFormat f = new DecimalFormat("0.000");
		f.setMaximumFractionDigits(3);
		f.setMinimumFractionDigits(3);
		
		String transLabel = getTransitionLabel(net, transition);
		if (net.isTransitionSilent(transition)) {
			dotNode.setOption("fillcolor", TauFill);
		} else {
			dotNode.setOption("fillcolor", TransitionFill);
		}

		StringBuilder label = new StringBuilder();
		label.append("<");
		label.append("<TABLE"
						+ " BORDER=\"0\" "
						+ "><TR>"
						+ "<TD><FONT POINT-SIZE=\"16\" >"
						+ transLabel
						+ "</FONT></TD>"
						+ "</TR>"
						+ "<TR>"
						+ "<TD ALIGN=\"LEFT\">"
						+ "<FONT ALIGN=\"LEFT\" POINT-SIZE=\"10\" >"
						+ "<I>weight:</I>"
						+ "</FONT>"
						+ "</TD>"
						+ "</TR>"
						+ "<TR>"
						+ "<TD BORDER=\"1\" BGCOLOR=\"#c0bbbb\" "
						+ "STYLE=\"ROUNDED,DASHED\" "
						+ "CELLPADDING=\"5\" "
						+ ">"
						+ f.format(net.getTransitionBaseWeight(transition))
						+ "</TD>"
						+ "</TR>"
						+ ""
		);
		
		StringBuilder paramLabel = new StringBuilder();
		for (int transitionHistory = 0; transitionHistory < net.getNumberOfTransitions(); transitionHistory++) {
			double adjustmentFactor = net.getTransitionAdjustmentWeight(transition, transitionHistory);
			if (adjustmentFactor != 1.0) {
				paramLabel.append( ""
						+ "<TR>"
						+ "<TD BORDER=\"1\" STYLE=\"rounded,dashed\" "
						+ "BGCOLOR=\""
						+ WeightFill
						+ "\" CELLPADDING=\"5\" >"
						+ "* "
						+ f.format(adjustmentFactor)
						+ " ^ "
						+ "|"
						+ getTransitionLabel(net, transitionHistory)
						+ " |"
						+ "</TD>"
						+ "</TR>"
				);
			}
		}
		if (paramLabel.length() > 0) {
			label.append( ""
					+ "<TR>"
					+ "<TD ALIGN=\"LEFT\">"
					+ "<FONT ALIGN=\"LEFT\" POINT-SIZE=\"10\" >"
					+ "<I>parameters:</I>"
					+ "</FONT>"
					+ "</TD>"
					+ "</TR>"
			);
			label.append(paramLabel.toString());
		}
		label.append("</TABLE>");
		label.append(">");

		dotNode.setLabel(label.toString());
		dotNode.setOption("style", "filled,rounded");
	}
	
}
