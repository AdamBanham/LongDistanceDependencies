package org.processmining.longdistancedependencies.function;

public class Sum implements Function {

	private final Function[] functions;

	public Sum(Function... functions) {
		this.functions = functions;

	}

	public double getValue(double[] parameters) {
		double sum = 0;
		for (Function function : functions) {
			sum += function.getValue(parameters);
		}
		return sum;
	}

	public double getPartialDerivative(int parameterIndex, double[] parameters) {
		double sum = 0;
		for (Function function : functions) {
			sum += function.getPartialDerivative(parameterIndex, parameters);
		}
		return sum;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append("(");
		for (int f = 0; f < functions.length; f++) {
			result.append(functions[f]);

			if (f < functions.length - 1) {
				result.append(") + (");
			}
		}
		result.append(")");

		return result.toString();
	}

	public String toLatex() {
		StringBuilder result = new StringBuilder();

		for (int f = 0; f < functions.length; f++) {
			result.append(functions[f].toLatex());

			if (f < functions.length - 1) {
				result.append(" + ");
			}
		}

		return result.toString();
	}

	public boolean isConstant() {
		for (Function function : functions) {
			if (!function.isConstant()) {
				return false;
			}
		}
		return true;
	}
}