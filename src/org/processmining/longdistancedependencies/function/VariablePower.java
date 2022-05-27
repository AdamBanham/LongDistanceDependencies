package org.processmining.longdistancedependencies.function;

public class VariablePower implements Function {

	private final int parameterIndex;
	private final int power;
	private final String name;

	public VariablePower(int parameterIndex, String name, int power) {
		this.parameterIndex = parameterIndex;
		this.power = power;
		this.name = name;
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
		if (name == null) {
			return "par" + parameterIndex + "^" + power;
		}
		return name;
	}

	public String toLatex() {
		if (name == null) {
			return "p_{" + parameterIndex + "}^{" + power + "}";
		}
		return name;
	}
}