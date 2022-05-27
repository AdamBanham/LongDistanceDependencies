package org.processmining.longdistancedependencies.function;

public class Variable implements Function {

	private final int parameterIndex;
	private final String name;

	public Variable(int parameterIndex, String name) {
		this.parameterIndex = parameterIndex;
		this.name = name;
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
		if (name == null) {
			return "par" + parameterIndex;
		}
		return name;
	}

	public String toLatex() {
		if (name == null) {
			return "p_{" + parameterIndex + "}";
		}
		return name;
	}
}