package org.processmining.longdistancedependencies.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.commons.math3.util.Pair;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginContextID;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.RecursiveCallException;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.plugin.events.ProgressEventListener.ListenerList;
import org.processmining.framework.plugin.impl.FieldSetException;
import org.processmining.framework.providedobjects.ProvidedObjectManager;
import org.processmining.longdistancedependencies.choicedata.ChoiceData;
import org.processmining.longdistancedependencies.choicedata.ChoiceData2Functions;
import org.processmining.longdistancedependencies.choicedata.ComputeChoiceData;
import org.processmining.longdistancedependencies.function.Function;
import org.processmining.longdistancedependencies.solve.Solver;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.plugins.InductiveVisualMinerAlignmentComputation;
import org.processmining.xeslite.plugin.OpenLogFileLiteImplPlugin;

public class LongDistancePlugin {
	public static void main(String[] args) throws FileNotFoundException, Exception {
		//File logFile = new File("/home/sander/Documents/svn/53 - long distance dependencies/bpic12-a.xes.gz");
		//File modelFile = new File("/home/sander/Documents/svn/53 - long distance dependencies/bpic12a-DFM-80.apnml");

		File logFile = new File("/home/sander/Documents/svn/53 - long distance dependencies/testlog3 300.xes.xes.gz");
		File modelFile = new File("/home/sander/Documents/svn/53 - long distance dependencies/Accepting Petri net of testlog3.apnml");

		//
		//		//		AcceptingPetriNet model = AcceptingPetriNetFactory.createAcceptingPetriNet();
		//		//		model.importFromStream(new FakeContext(), new FileInputStream(modelFile));
		//		//IvMModel model = new IvMModel(EfficientTreeImportPlugin.importFromFile(modelFile));
		//IvMModel model = new IvMModel(DfmImportPlugin.readFile(new FileInputStream(modelFile)));
		AcceptingPetriNet aNet = AcceptingPetriNetFactory.createAcceptingPetriNet();
		aNet.importFromStream(new FakeContext(), new FileInputStream(modelFile));
		IvMModel model = new IvMModel(aNet);

		XLog log = (XLog) new OpenLogFileLiteImplPlugin().importFile(new FakeContext(), logFile);

		//		StochasticLabelledPetriNetAdjustmentWeights net = TestModel.generate();
		//		XLog log = LongDistanceGenerator.generate(net, 100000);
		//
		//		{
		//			XLogWriterIncremental writer = new XLogWriterIncremental(logFile);
		//			for (XTrace trace : log) {
		//				writer.writeTrace(trace);
		//
		//			}
		//			writer.close();
		//		}

		//align
		ProMCanceller canceller = new ProMCanceller() {
			public boolean isCancelled() {
				return false;
			}
		};
		XEventClassifier classifier = MiningParameters.getDefaultClassifier();
		IvMLogFiltered ivmLog = new IvMLogFilteredImpl(
				InductiveVisualMinerAlignmentComputation.align(model, log, classifier, canceller));

		//create choice data
		ChoiceData choiceData = ComputeChoiceData.compute(ivmLog, model, canceller);
		System.out.println(choiceData);

		//find fixed parameters
		int[] parametersToFix = ChoiceData2Functions.getParametersToFix(choiceData, model.getMaxNumberOfNodes(), model);
		System.out.println("fixing parameters " + Arrays.toString(parametersToFix));

		//to functions
		Pair<List<Function>, List<Function>> equations = ChoiceData2Functions.convert(choiceData,
				model.getMaxNumberOfNodes(), parametersToFix, model);
		System.out.println("equations: ");
		for (int i = 0; i < equations.getFirst().size(); i++) {
			System.out.println(
					equations.getFirst().get(i).toLatex() + " ={}& " + equations.getSecond().get(i).toLatex() + "\\\\");
		}

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

	public static class FakeContext implements PluginContext {

		public PluginManager getPluginManager() {
			// TODO Auto-generated method stub
			return null;
		}

		public ProvidedObjectManager getProvidedObjectManager() {
			// TODO Auto-generated method stub
			return null;
		}

		public ConnectionManager getConnectionManager() {
			return new ConnectionManager() {

				public void setEnabled(boolean isEnabled) {
					// TODO Auto-generated method stub

				}

				public boolean isEnabled() {
					// TODO Auto-generated method stub
					return false;
				}

				public <T extends Connection> T getFirstConnection(Class<T> connectionType, PluginContext context,
						Object... objects) throws ConnectionCannotBeObtained {
					// TODO Auto-generated method stub
					return null;
				}

				public <T extends Connection> Collection<T> getConnections(Class<T> connectionType,
						PluginContext context, Object... objects) throws ConnectionCannotBeObtained {
					// TODO Auto-generated method stub
					return null;
				}

				public org.processmining.framework.plugin.events.ConnectionObjectListener.ListenerList getConnectionListeners() {
					// TODO Auto-generated method stub
					return null;
				}

				public Collection<ConnectionID> getConnectionIDs() {
					// TODO Auto-generated method stub
					return null;
				}

				public Connection getConnection(ConnectionID id) throws ConnectionCannotBeObtained {
					// TODO Auto-generated method stub
					return null;
				}

				public void clear() {
					// TODO Auto-generated method stub

				}

				public <T extends Connection> T addConnection(T connection) {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}

		public PluginContextID createNewPluginContextID() {
			// TODO Auto-generated method stub
			return null;
		}

		public void invokePlugin(PluginDescriptor plugin, int index, Object... objects) {
			// TODO Auto-generated method stub

		}

		public void invokeBinding(PluginParameterBinding binding, Object... objects) {
			// TODO Auto-generated method stub

		}

		public Class<? extends PluginContext> getPluginContextType() {
			// TODO Auto-generated method stub
			return null;
		}

		public <T, C extends Connection> Collection<T> tryToFindOrConstructAllObjects(Class<T> type,
				Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
			// TODO Auto-generated method stub
			return null;
		}

		public <T, C extends Connection> T tryToFindOrConstructFirstObject(Class<T> type, Class<C> connectionType,
				String role, Object... input) throws ConnectionCannotBeObtained {
			// TODO Auto-generated method stub
			return null;
		}

		public <T, C extends Connection> T tryToFindOrConstructFirstNamedObject(Class<T> type, String name,
				Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContext createChildContext(String label) {
			// TODO Auto-generated method stub
			return null;
		}

		public Progress getProgress() {
			return new Progress() {

				public void setValue(int value) {
					// TODO Auto-generated method stub

				}

				public void setMinimum(int value) {
					// TODO Auto-generated method stub

				}

				public void setMaximum(int value) {
					// TODO Auto-generated method stub

				}

				public void setIndeterminate(boolean makeIndeterminate) {
					// TODO Auto-generated method stub

				}

				public void setCaption(String message) {
					// TODO Auto-generated method stub

				}

				public boolean isIndeterminate() {
					// TODO Auto-generated method stub
					return false;
				}

				public boolean isCancelled() {
					// TODO Auto-generated method stub
					return false;
				}

				public void inc() {
					// TODO Auto-generated method stub

				}

				public int getValue() {
					// TODO Auto-generated method stub
					return 0;
				}

				public int getMinimum() {
					// TODO Auto-generated method stub
					return 0;
				}

				public int getMaximum() {
					// TODO Auto-generated method stub
					return 0;
				}

				public String getCaption() {
					// TODO Auto-generated method stub
					return null;
				}

				public void cancel() {
					// TODO Auto-generated method stub

				}
			};
		}

		public ListenerList getProgressEventListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		public org.processmining.framework.plugin.events.PluginLifeCycleEventListener.List getPluginLifeCycleEventListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContextID getID() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getLabel() {
			// TODO Auto-generated method stub
			return null;
		}

		public org.processmining.framework.util.Pair<PluginDescriptor, Integer> getPluginDescriptor() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContext getParentContext() {
			// TODO Auto-generated method stub
			return null;
		}

		public java.util.List<PluginContext> getChildContexts() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginExecutionResult getResult() {
			// TODO Auto-generated method stub
			return null;
		}

		public ProMFuture<?> getFutureResult(int i) {
			return new ProMFuture<Object>(Object.class, "") {
				protected Object doInBackground() throws Exception {
					return null;
				}
			};
		}

		public Executor getExecutor() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isDistantChildOf(PluginContext context) {
			// TODO Auto-generated method stub
			return false;
		}

		public void setFuture(PluginExecutionResult resultToBe) {
			// TODO Auto-generated method stub

		}

		public void setPluginDescriptor(PluginDescriptor descriptor, int methodIndex)
				throws FieldSetException, RecursiveCallException {
			// TODO Auto-generated method stub

		}

		public boolean hasPluginDescriptorInPath(PluginDescriptor descriptor, int methodIndex) {
			// TODO Auto-generated method stub
			return false;
		}

		public void log(String message, MessageLevel level) {
			// TODO Auto-generated method stub

		}

		public void log(String message) {
			// TODO Auto-generated method stub

		}

		public void log(Throwable exception) {
			// TODO Auto-generated method stub

		}

		public org.processmining.framework.plugin.events.Logger.ListenerList getLoggingListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		public PluginContext getRootContext() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean deleteChild(PluginContext child) {
			// TODO Auto-generated method stub
			return false;
		}

		public <T extends Connection> T addConnection(T c) {
			// TODO Auto-generated method stub
			return null;
		}

		public void clear() {
			// TODO Auto-generated method stub

		}

	}
}