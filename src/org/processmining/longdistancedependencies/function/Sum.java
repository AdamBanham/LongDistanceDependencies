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

}