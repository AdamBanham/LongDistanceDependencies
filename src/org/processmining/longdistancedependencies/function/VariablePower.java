package org.processmining.longdistancedependencies.function;

public class VariablePower implements Function {

	private final int parameterIndex;
	private final int power;

	public VariablePower(int parameterIndex, int power) {
		this.parameterIndex = parameterIndex;
		this.power = power;
	}

	public double getValue(double[] parameters) {
		return Math.pow(parameters[parameterIndex], power);
	}

	public double getPartialDerivative(int parameterIndex, double[] parameters) {
		if (parameterIndex == this.parameterIndex) {
			return power * Math.pow(parameters[parameterIndex], power - 1);
		}
		return 0;
	}

	public String toString() {
		return "par" + parameterIndex + "^" + power;
	}

	public String toLatex() {
		return "p_{" + parameterIndex + "}^{" + power + "}";
	}
}