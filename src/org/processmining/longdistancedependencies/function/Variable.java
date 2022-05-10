package org.processmining.longdistancedependencies.function;

public class Variable implements Function {

	private final int parameterIndex;

	public Variable(int parameterIndex) {
		this.parameterIndex = parameterIndex;
	}

	public double getValue(double[] parameters) {
		return parameters[parameterIndex];
	}

	public double getPartialDerivative(int parameterIndex, double[] parameters) {
		if (parameterIndex == this.parameterIndex) {
			return 1;
		}
		return 0;
	}

	public String toString() {
		return "par" + parameterIndex;
	}

	public String toLatex() {
		return "p_{" + parameterIndex + "}";
	}
}