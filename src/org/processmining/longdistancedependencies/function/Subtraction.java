package org.processmining.longdistancedependencies.function;

public class Subtraction implements Function {

	private final Function[] functions;

	public Subtraction(Function... functions) {
		this.functions = functions;

	}

	public double getValue(double[] parameters) {
		double sum = functions[0].getValue(parameters);
		for (int f = 1; f < functions.length; f++) {
			sum -= functions[f].getValue(parameters);
		}
		return sum;
	}

	public double getPartialDerivative(int parameterIndex, double[] parameters) {
		double sum = functions[0].getPartialDerivative(parameterIndex, parameters);
		for (int f = 1; f < functions.length; f++) {
			sum -= functions[f].getPartialDerivative(parameterIndex, parameters);
		}
		return sum;
	}

}