package org.processmining.longdistancedependencies.function;

public class Division implements Function {

	private final Function functionA;
	private final Function functionB;

	public Division(Function functionA, Function functionB) {
		this.functionA = functionA;
		this.functionB = functionB;
	}

	public double getValue(double[] parameters) {
		return functionA.getValue(parameters) / functionB.getValue(parameters);
	}

	public double getPartialDerivative(int parameterIndex, double[] parameters) {
		double valueB = functionB.getValue(parameters);
		return (functionA.getPartialDerivative(parameterIndex, parameters) * valueB
				- functionA.getValue(parameters) * functionB.getPartialDerivative(parameterIndex, parameters))
				/ (valueB * valueB);
	}

	public String toString() {
		return "(" + functionA.toString() + ") / (" + functionB.toString() + ")";
	}
}