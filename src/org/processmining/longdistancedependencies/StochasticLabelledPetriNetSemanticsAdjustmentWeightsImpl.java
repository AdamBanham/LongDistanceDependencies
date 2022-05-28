package org.processmining.longdistancedependencies;

import java.util.Arrays;

import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemanticsImpl;

public class StochasticLabelledPetriNetSemanticsAdjustmentWeightsImpl extends StochasticLabelledPetriNetSemanticsImpl {

	private int[] history;
	private final StochasticLabelledPetriNetAdjustmentWeights net;

	public StochasticLabelledPetriNetSemanticsAdjustmentWeightsImpl(StochasticLabelledPetriNetAdjustmentWeights net) {
		super(net);
		this.net = net;
		history = FixedMultiset.init(net.getNumberOfTransitions());
		Arrays.fill(history, 0);
	}

	@Override
	public void setInitialState() {
		super.setInitialState();
		if (history != null) {
			Arrays.fill(history, 0);
		}
	}

	@Override
	public void executeTransition(int transition) {
		super.executeTransition(transition);
		history[transition]++;
	}

	@Override
	public double getTransitionWeight(int transition) {
		return net.getTransitionWeight(transition, history);
	}

	@Override
	public double getTotalWeightOfEnabledTransitions() {
		double result = 0;
		for (int transition = enabledTransitions.nextSetBit(0); transition >= 0; transition = enabledTransitions
				.nextSetBit(transition + 1)) {
			result += net.getTransitionWeight(transition, history);
		}
		return result;
	}

	@Override
	public byte[] getState() {
		byte[] superState = super.getState();

		byte[] result = new byte[superState.length + history.length * 4];

		System.arraycopy(superState, 0, result, 0, superState.length);
		intToByte(history, result, superState.length);

		return result;
	}

	@Override
	public void setState(byte[] newState) {
		byte[] newSuperState = new byte[newState.length - history.length * 4];
		System.arraycopy(newState, 0, newSuperState, 0, newSuperState.length);

		byteToInt(newState, history, newSuperState.length, history.length);

		super.setState(newSuperState);
	}

	public static void intToByte(int[] source, byte[] target, int targetStart) {
		int idxDst = 0;
		for (int i = 0; i < source.length; i++) {
			target[targetStart + idxDst] = (byte) (source[i]);
			idxDst++;
			target[targetStart + idxDst] = (byte) (source[i] >> 8);
			idxDst++;
			target[targetStart + idxDst] = (byte) (source[i] >> 16);
			idxDst++;
			target[targetStart + idxDst] = (byte) (source[i] >> 24);
			idxDst++;
		}
	}

	public static void byteToInt(byte source[], int[] target, int sourceStart, int targetLength) {
		int v;
		int idxOrg = sourceStart;

		for (int i = 0; i < targetLength; i++) {
			target[i] = 0;

			v = 0x000000FF & source[idxOrg];
			target[i] = target[i] | v;
			idxOrg++;

			v = 0x000000FF & source[idxOrg];
			target[i] = target[i] | (v << 8);
			idxOrg++;

			v = 0x000000FF & source[idxOrg];
			target[i] = target[i] | (v << 16);
			idxOrg++;

			v = 0x000000FF & source[idxOrg];
			target[i] = target[i] | (v << 24);
			idxOrg++;
		}
	}
}