package org.processmining.longdistancedependencies.function;

public class Constant implements Function {

	private final double value;

	public Constant(double value) {
		this.value = value;
	}

	public double getValue(double[] parameters) {
		return value;
	}

	public double getPartialDerivative(int parameterIndex, double[] parameters) {
		return 0;
	}

	public String toString() {
		return value + "";
	}
}