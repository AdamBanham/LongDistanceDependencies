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

		for (int pivot = 0; pivot < parameters.length; pivot++) {

			for (int f = 0; f < pivot; f++) {
				sum += functions[f].getValue(parameters);
			}
			sum += functions[pivot].getPartialDerivative(parameterIndex, parameters);
			for (int f = pivot + 1; f < parameters.length; f++) {
				sum += functions[f].getValue(parameters);
			}
		}

		return sum;
	}

}