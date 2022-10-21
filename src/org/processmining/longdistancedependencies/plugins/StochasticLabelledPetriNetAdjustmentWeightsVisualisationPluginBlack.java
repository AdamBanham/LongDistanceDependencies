package org.processmining.longdistancedependencies.plugins;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeights;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.stochasticlabelledpetrinets.plugins.StochasticLabelledPetriNetVisualisationPlugin;

import com.kitfox.svg.Ellipse;
import com.kitfox.svg.Path;
import com.kitfox.svg.RenderableElement;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class StochasticLabelledPetriNetAdjustmentWeightsVisualisationPluginBlack
		extends StochasticLabelledPetriNetVisualisationPlugin<StochasticLabelledPetriNetAdjustmentWeights> {
	@Plugin(name = "black stochastic labelled Petri net (adjustment weights)", returnLabels = {
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
		String silentTransitionColour = "#002743";
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

			dotNode.setOption("color", modelEdgeColour);
			if (net.isInInitialMarking(place) > 0) {
				dotNode.setOption("style", "filled");
				dotNode.setOption("fillcolor", "#80ff00");
			}

			decoratePlace(net, place, dotNode);
		}

		for (int transition = 0; transition < net.getNumberOfTransitions(); transition++) {
			DotNode dotNode;

			if (net.isTransitionSilent(transition)) {
				dotNode = dot.addNode("" + transition);
				dotNode.setOption("style", "filled");
				dotNode.setOption("fillcolor", silentTransitionColour);
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
		List<Triple<DotNode, DotNode, Double>> dependencyEdges = new ArrayList<>();
		for (int transitionA = 0; transitionA < net.getNumberOfTransitions(); transitionA++) {
			for (int transitionB = 0; transitionB < net.getNumberOfTransitions(); transitionB++) {
				double adjustmentFactor = net.getTransitionAdjustmentWeight(transitionA, transitionB);
				if (adjustmentFactor != 1.0) {
					dependencyEdges.add(Triple.of(transition2dotNode.get(transitionB),
							transition2dotNode.get(transitionA), adjustmentFactor));

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
		DotPanel panel = new DotPanel(dot) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void paintImage(Graphics2D g) {
				super.paintImage(g);

				g.setColor(Color.green);
				g.drawRect(0, 0, 100, 100);

				g.drawRect(0, 0, (int) getImage().getWidth(), (int) getImage().getHeight());

				for (Triple<DotNode, DotNode, Double> t : dependencyEdges) {
					DotNode source = t.getA();
					DotNode target = t.getB();

					System.out.println("source " + source);
					try {
						SVGElement svgNode = DotPanel.getSVGElementOf(image, source);
						while (svgNode != null && svgNode instanceof RenderableElement) {
							System.out.println(" svgNode " + svgNode);
							Rectangle2D sourceCenter = ((RenderableElement) svgNode).getBoundingBox();
							g.setColor(Color.blue);
							g.drawRect((int) sourceCenter.getMinX(), (int) sourceCenter.getMinY(),
									(int) sourceCenter.getWidth(), (int) sourceCenter.getHeight());
							
							((RenderableElement) svgNode).getXForm().transform(, );

							svgNode = svgNode.getParent();
						}

						//						Point2D user = transformImage2User(
						//								new Point2D.Double(targetCenter.getA(), targetCenter.getB()));
						//g.drawLine(0, 0, (int) (double) user.getX(), (int) -user.getY());
					} catch (SVGException e) {
						e.printStackTrace();
					}
				}
			}

			public static Rectangle2D getCenter(DotNode node, SVGDiagram image) throws SVGException {
				SVGElement element = DotPanel.getSVGElementOf(image, node).getChild(1);
				Rectangle2D bb = null;
				if (element instanceof Ellipse) {
					bb = ((Ellipse) element).getBoundingBox();
				} else if (element instanceof Path) {
					bb = ((Path) element).getBoundingBox();
				} else {
					bb = DotPanel.getSVGElementOf(image, node).getBoundingBox();
				}
				return bb;
			}

		};
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