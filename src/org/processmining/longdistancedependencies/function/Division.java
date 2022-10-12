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
		double derA = functionA.getPartialDerivative(parameterIndex, parameters);
		double valueA = functionA.getValue(parameters);
		double derB = functionB.getPartialDerivative(parameterIndex, parameters);
		double valueB = functionB.getValue(parameters);
		return (derA * valueB - valueA * derB) / (valueB * valueB);
	}

	public String toString() {
		return "(" + functionA.toString() + ") / (" + functionB.toString() + ")";
	}

	public String toLatex() {
		return "\\frac{" + functionA.toLatex() + "}{" + functionB.toLatex() + "}";
	}

	public boolean isConstant() {
		return functionA.isConstant() && functionB.isConstant();
	}
}