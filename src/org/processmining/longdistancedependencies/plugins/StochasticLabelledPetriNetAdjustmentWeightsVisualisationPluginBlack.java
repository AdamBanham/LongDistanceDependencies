package org.processmining.longdistancedependencies.plugins;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeights;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeightsEditable;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.listeners.GraphChangedListener;
import org.processmining.stochasticlabelledpetrinets.plugins.StochasticLabelledPetriNetVisualisationPlugin;

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

	public static void main(String[] args) throws NumberFormatException, FileNotFoundException, IOException {
		File file = new File(
				"/home/sander/Documents/svn/53 - long distance dependencies/experiments/3-discoveredstochasticmodels/Road_Traffic_Fine_Management_Process.xes.gz-IMf-Ldd-0.slpna");
		StochasticLabelledPetriNetAdjustmentWeightsEditable net = StochasticLabelledPetriNetAdjustmentWeightsImportPlugin
				.read(new FileInputStream(file));

		JFrame frame = new JFrame("Stochastic dependencies");
		frame.setSize(400, 400);
		frame.add(new StochasticLabelledPetriNetAdjustmentWeightsVisualisationPluginBlack().visualise(net));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	String backgroundColour = "#011422";
	String modelEdgeColour = "#002fbb";
	String silentTransitionColour = "#002743";
	String textColour = ColourMap.toHexString(Color.white);
	Color dependencyEdgeColourTarget = Color.white;
	Color dependencyEdgeColourSourceNegative = Color.red;
	Color dependencyEdgeColourSourcePositive = Color.green;

	double bSize = -60; //distance of the control point from the line
	double arrowHeadLength = 10; //length of the arrowhead
	double arrowHeadWidth = 10; //width of the arrowhead

	public class EdgedDotPanel extends DotPanel {
		private static final long serialVersionUID = -6827858862553993557L;

		private final List<Triple<DotNode, DotNode, Double>> dependencyEdges;

		//the following values are precomputed as to not keep the drawing loop too busy (after the first draw)
		private double[][] As = null;
		private double[][] At = null;
		private double[][] Ab = null;
		private double[][] Al = null;
		private double[][] Ar = null;
		private boolean[] Apositive = null;

		public EdgedDotPanel(Dot dot, List<Triple<DotNode, DotNode, Double>> dependencyEdges) {
			super(dot);

			this.dependencyEdges = dependencyEdges;

			addGraphChangedListener(new GraphChangedListener() {
				public void graphChanged(GraphChangedReason reason, Object newState) {
					As = null;
					At = null;
					Ab = null;
					Al = null;
					Ar = null;
					Apositive = null;
				}
			});
		}

		private void compute() {

			As = new double[dependencyEdges.size()][];
			At = new double[dependencyEdges.size()][];
			Ab = new double[dependencyEdges.size()][];
			Al = new double[dependencyEdges.size()][];
			Ar = new double[dependencyEdges.size()][];
			Apositive = new boolean[dependencyEdges.size()];
			int i = 0;
			for (Triple<DotNode, DotNode, Double> triple : dependencyEdges) {
				DotNode source = triple.getA();
				DotNode target = triple.getB();

				try {
					Rectangle2D bbSource = getBoundingBox(source, image);
					Rectangle2D bbTarget = getBoundingBox(target, image);

					double[] middleSource = new double[] { bbSource.getCenterX(), bbSource.getCenterY() };
					double[] middleTarget = new double[] { bbTarget.getCenterX(), bbTarget.getCenterY() };
					double[] initialB = StochasticLabelledPetriNetAdjustmentWeightsVisualisationPluginBlack
							.add(halfway(middleSource, middleTarget), scale(normal(middleSource, middleTarget), bSize)); //bezier control point

					double[] s = getClosestAnchor(bbSource, initialB);
					double[] t = getClosestAnchor(bbTarget, initialB);
					double[] b = StochasticLabelledPetriNetAdjustmentWeightsVisualisationPluginBlack.add(halfway(s, t),
							scale(normal(s, t), bSize)); //bezier control point

					//point l is the left arrowhead point; point r is the right arrowhead point
					double[] l;
					double[] r;
					{
						//point p is the first entry of the arrowhead towards t
						double[] p = StochasticLabelledPetriNetAdjustmentWeightsVisualisationPluginBlack.add(t,
								scale(normalise(subtract(t, b)), -arrowHeadLength));

						//find the normal of p -- t
						double[] nbt = normal(p, t);

						l = StochasticLabelledPetriNetAdjustmentWeightsVisualisationPluginBlack.add(p,
								scale(nbt, 0.5 * arrowHeadWidth));

						r = StochasticLabelledPetriNetAdjustmentWeightsVisualisationPluginBlack.add(p,
								scale(nbt, -0.5 * arrowHeadWidth));
					}

					boolean positive = triple.getC() > 1;

					As[i] = s;
					At[i] = t;
					Ab[i] = b;
					Al[i] = l;
					Ar[i] = r;
					Apositive[i] = positive;

				} catch (SVGException e) {
					e.printStackTrace();
				}

				i++;
			}

		}

		@Override
		protected void paintImage(Graphics2D g) {
			super.paintImage(g);

			if (As == null) {
				compute();
			}

			GeneralPath pathLine = new GeneralPath();
			GeneralPath pathArrowHead = new GeneralPath();
			for (int i = 0; i < As.length; i++) {

				double[] s = As[i];
				double[] t = At[i];
				double[] b = Ab[i];
				double[] l = Al[i];
				double[] r = Ar[i];
				boolean positive = Apositive[i];

				//line path
				{
					pathLine.reset();
					pathLine.moveTo(s[0], s[1]);
					pathLine.curveTo(b[0], b[1], b[0], b[1], t[0], t[1]);
				}

				//arrowhead path
				{
					pathArrowHead.reset();
					pathArrowHead.moveTo(t[0], t[1]);
					pathArrowHead.lineTo(l[0], l[1]);
					pathArrowHead.lineTo(r[0], r[1]);
				}

				//paint
				Color sourceColour = positive ? dependencyEdgeColourSourcePositive : dependencyEdgeColourSourceNegative;
				GradientPaint paint = new GradientPaint((int) s[0], (int) s[1], sourceColour, (int) t[0], (int) t[1],
						dependencyEdgeColourTarget);
				g.setPaint(paint);

				//g.setCaolor(dependencyEdgeColour);
				g.draw(pathLine);

				g.fill(pathArrowHead);

				g.setColor(Color.black);
				g.draw(pathArrowHead);

				//g.drawRect((int) r[0] - 1, (int) r[1] - 1, 2, 2);
				//g.drawLine((int) t[0], (int) t[1], (int) r[0], (int) r[1]);
			}
		}

		public Rectangle2D getBoundingBox(DotNode dotNode, SVGDiagram image) throws SVGException {
			SVGElement svgNode = DotPanel.getSVGElementOf(image, dotNode).getChild(1);
			Rectangle2D bb = ((RenderableElement) svgNode).getBoundingBox();

			Point2D a = new Point2D.Double(bb.getMinX(), bb.getMinY());
			Point2D b = new Point2D.Double(bb.getMaxX(), bb.getMaxY());
			Point2D ra = transformElement2Image(a, svgNode);
			Point2D rb = transformElement2Image(b, svgNode);

			return new Rectangle2D.Double(ra.getX(), ra.getY(), rb.getX() - ra.getX(), rb.getY() - ra.getY());
		}
	}

	@Override
	public DotPanel visualise(StochasticLabelledPetriNetAdjustmentWeights net) {
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

					//					DotEdge edge = dot.addEdge(transition2dotNode.get(transitionB),
					//							transition2dotNode.get(transitionA));
					//					edge.setOption("constraint", "false");
					//					edge.setOption("color", dependencyEdgeColour);
					//					edge.setOption("fontcolor", textColour);
					//					edge.setLabel(f.format(adjustmentFactor));
				}
			}
		}

		dot.setOption("bgcolor", backgroundColour);
		return new EdgedDotPanel(dot, dependencyEdges);
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

	public static double[] add(double[] a, double[] b) {
		return new double[] { a[0] + b[0], a[1] + b[1] };
	}

	public static double[] subtract(double[] a, double[] b) {
		return new double[] { a[0] - b[0], a[1] - b[1] };
	}

	public static double[] scale(double[] a, double f) {
		return new double[] { a[0] * f, a[1] * f };
	}

	public static double[] halfway(double[] a, double[] b) {
		double mx = (a[0] + b[0]) / 2;
		double my = (a[1] + b[1]) / 2;
		return new double[] { mx, my };
	}

	public static double[] normal(double[] a, double[] b) {
		double dx = b[0] - a[0];
		double dy = b[1] - a[1];
		return normalise(new double[] { -dy, dx });
	}

	public static double[] normalise(double[] a) {
		double length = Math.sqrt(a[0] * a[0] + a[1] * a[1]);
		return new double[] { a[0] / length, a[1] / length };
	}

	public static double[] getClosestAnchor(Rectangle2D rectangle, double[] point) {
		return new double[] { // 
				Math.max(rectangle.getCenterX() - rectangle.getWidth() / 2,
						Math.min(point[0], rectangle.getCenterX() + rectangle.getWidth() / 2)), //
				Math.max(rectangle.getCenterY() - rectangle.getHeight() / 2,
						Math.min(point[1], rectangle.getCenterY() + rectangle.getHeight() / 2)) };
	}
}