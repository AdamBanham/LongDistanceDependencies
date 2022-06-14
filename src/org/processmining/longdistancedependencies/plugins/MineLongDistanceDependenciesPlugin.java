package org.processmining.longdistancedependencies.plugins;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.longdistancedependencies.AcceptingPetriNet2StochasticLabelledPetriNetAdjustmentWeights;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeights;
import org.processmining.longdistancedependencies.StochasticLabelledPetriNetAdjustmentWeightsEditable;
import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;
import org.processmining.longdistancedependencies.choicedata.ComputeChoiceData;
import org.processmining.longdistancedependencies.function.Function;
import org.processmining.longdistancedependencies.solve.Solver;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.plugins.InductiveVisualMinerAlignmentComputation;

public class MineLongDistanceDependenciesPlugin {

	@Plugin(name = "Mine long-distance dependencies (APN)", level = PluginLevel.Regular, returnLabels = {
			"Stochastic labelled Petri net with long-distance dependencies" }, returnTypes = {
					StochasticLabelledPetriNetAdjustmentWeights.class }, parameterLabels = { "Accepting Petri net",
							"Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0, 1 })
	public StochasticLabelledPetriNetAdjustmentWeights mine(UIPluginContext context, AcceptingPetriNet model, XLog xLog)
			throws Exception {
		MiningDialog dialog = new MiningDialog(xLog);
		InteractionResult result = context.showWizard("Mine long-distance dependencies", true, true, dialog);
		context.log("Mining...");
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(false);
			return null;
		}

		StochasticLabelledPetriNetAdjustmentWeightsEditable resultNet = mine(model, xLog, dialog.getClassifier(),
				new ProMCanceller() {
					public boolean isCancelled() {
						return context.getProgress().isCancelled();
					}
				});

		return resultNet;
	}

	//	@Plugin(name = "Mine long-distance dependencies (DFM)", level = PluginLevel.Regular, returnLabels = {
	//			"Stochastic labelled Petri net with long-distance dependencies" }, returnTypes = {
	//					StochasticLabelledPetriNetAdjustmentWeights.class }, parameterLabels = { "Directly follows model",
	//							"Log" }, userAccessible = true)
	//	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	//	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0, 1 })
	//	public StochasticLabelledPetriNetAdjustmentWeights mine(UIPluginContext context, DirectlyFollowsModel model,
	//			XLog xLog) throws Exception {
	//		MiningDialog dialog = new MiningDialog(xLog);
	//		InteractionResult result = context.showWizard("Mine long-distance dependencies", true, true, dialog);
	//		context.log("Mining...");
	//		if (result != InteractionResult.FINISHED) {
	//			context.getFutureResult(0).cancel(false);
	//			return null;
	//		}
	//
	//		return mine(new IvMModel(model), xLog, dialog.getClassifier(), new ProMCanceller() {
	//			public boolean isCancelled() {
	//				return context.getProgress().isCancelled();
	//			}
	//		});
	//	}
	//
	//	@Plugin(name = "Mine long-distance dependencies (PT)", level = PluginLevel.Regular, returnLabels = {
	//			"Stochastic labelled Petri net with long-distance dependencies" }, returnTypes = {
	//					StochasticLabelledPetriNetAdjustmentWeights.class }, parameterLabels = { "Efficient tree",
	//							"Log" }, userAccessible = true)
	//	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	//	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0, 1 })
	//	public StochasticLabelledPetriNetAdjustmentWeights mine(UIPluginContext context, EfficientTree model, XLog xLog)
	//			throws Exception {
	//		MiningDialog dialog = new MiningDialog(xLog);
	//		InteractionResult result = context.showWizard("Mine long-distance dependencies", true, true, dialog);
	//		context.log("Mining...");
	//		if (result != InteractionResult.FINISHED) {
	//			context.getFutureResult(0).cancel(false);
	//			return null;
	//		}
	//
	//		return mine(new IvMModel(model), xLog, dialog.getClassifier(), new ProMCanceller() {
	//			public boolean isCancelled() {
	//				return context.getProgress().isCancelled();
	//			}
	//		});
	//	}

	public static StochasticLabelledPetriNetAdjustmentWeightsEditable mine(AcceptingPetriNet model, XLog xLog,
			XEventClassifier classifier, ProMCanceller canceller) throws Exception {
		StochasticLabelledPetriNetAdjustmentWeightsEditable resultNet = AcceptingPetriNet2StochasticLabelledPetriNetAdjustmentWeights
				.convert(model.getNet(), model.getInitialMarking());
		mine(new IvMModel(model), xLog, classifier, resultNet, canceller);
		return resultNet;
	}

	public static void mine(IvMModel model, XLog xLog, XEventClassifier classifier,
			StochasticLabelledPetriNetAdjustmentWeightsEditable resultNet, ProMCanceller canceller) throws Exception {

		IvMLogFiltered ivmLog = new IvMLogFilteredImpl(
				InductiveVisualMinerAlignmentComputation.align(model, xLog, classifier, canceller));

		//create choice data
		ChoiceData choiceData = ComputeChoiceData.compute(ivmLog, model, canceller);
		System.out.println(choiceData);

		int[] parametersToFix = ChoiceData2Functions.getParametersToFix(choiceData, model.getMaxNumberOfNodes(), model, canceller);
		System.out.println("fixed parameters " + Arrays.toString(parametersToFix));

		//to functions
		Pair<List<Function>, List<Function>> equations = ChoiceData2Functions.convert(choiceData,
				model.getMaxNumberOfNodes(), parametersToFix, model);

		//create target values
		double[] values = new double[equations.getFirst().size()];
		int i = 0;
		for (Function function : equations.getFirst()) {
			values[i] = function.getValue(null);
			i++;
		}

		//solve
		double[] result = Solver.solve(values, equations.getSecond(),
				(1 + model.getMaxNumberOfNodes()) * model.getMaxNumberOfNodes(), parametersToFix,
				ChoiceData2Functions.fixValue);

		System.out.println();
		System.out.println("result:");
		System.out.println(Arrays.toString(result));
		System.out.println(toString(result, model));

		applyToNet(result, resultNet, model);
	}

	public static void applyToNet(double[] parameters, StochasticLabelledPetriNetAdjustmentWeightsEditable resultNet,
			IvMModel model) {
		for (int node : model.getAllNodes()) {
			resultNet.setTransitionBaseWeight(node, parameters[ChoiceData2Functions.getParameterIndexBase(node)]);

			for (int node2 : model.getAllNodes()) {
				resultNet.setTransitionAdjustmentWeight(node, node2, parameters[ChoiceData2Functions
						.getParameterIndexAdjustment(node, node2, model.getMaxNumberOfNodes())]);
			}
		}
	}

	public static String toString(double[] parameters, IvMModel model) {
		StringBuilder result = new StringBuilder();

		for (int node : model.getAllNodes()) {
			if (model.isActivity(node)) {
				result.append(model.getActivityName(node));
			} else {
				result.append("silent step #");
				result.append(node);
			}
			result.append(": base weight ");
			result.append(parameters[ChoiceData2Functions.getParameterIndexBase(node)]);
			result.append(", \tadjustment factors: ");
			for (int node2 : model.getAllNodes()) {
				if (model.isActivity(node2)) {
					result.append(model.getActivityName(node2));
				} else {
					result.append("silent step #");
					result.append(node2);
				}
				result.append(": ");
				result.append(parameters[ChoiceData2Functions.getParameterIndexAdjustment(node, node2,
						model.getMaxNumberOfNodes())]);
				result.append(", ");
			}
			result.append("\n");
		}

		return result.toString();
	}
}