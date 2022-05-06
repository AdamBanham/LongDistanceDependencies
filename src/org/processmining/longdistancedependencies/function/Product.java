package org.processmining.longdistancedependencies.function;

public class Product implements Function {

	private final Function[] functions;

	public Product(Function... functions) {
		this.functions = functions;
	}

	public double getValue(double[] parameters) {
		double product = 1;
		for (Function function : functions) {
			product *= function.getValue(parameters);
		}
		return product;
	}

	public double getPartialDerivative(int parameterIndex, double[] parameters) {
		double sum = 0;

		for (int pivot = 0; pivot < functions.length; pivot++) {

			for (int f = 0; f < pivot; f++) {
				sum += functions[f].getValue(parameters);
			}
			sum += functions[pivot].getPartialDerivative(parameterIndex, parameters);
			for (int f = pivot + 1; f < functions.length; f++) {
				sum += functions[f].getValue(parameters);
			}
		}

		return sum;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append("(");
		for (int f = 0; f < functions.length; f++) {
			result.append(functions[f]);

			if (f < functions.length - 1) {
				result.append(") * (");
			}
		}
		result.append(")");

		return result.toString();
	}
}