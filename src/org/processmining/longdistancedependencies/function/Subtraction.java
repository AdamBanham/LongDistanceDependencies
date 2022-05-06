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

	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append("(");
		for (int f = 0; f < functions.length; f++) {
			result.append(functions[f]);

			if (f < functions.length - 1) {
				result.append(") - (");
			}
		}
		result.append(")");

		return result.toString();
	}
}