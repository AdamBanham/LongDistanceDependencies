package org.processmining.longdistancedependencies.function;

import java.util.Objects;

public class Variable implements Function {

	private final int parameterIndex;
	private final String name;

	public Variable(int parameterIndex, String name) {
		this.parameterIndex = parameterIndex;
		this.name = name;
	}

	public double getValue(double[] parameters) {
		return parameters[parameterIndex];
	}

	public double getPartialDerivative(int parameterIndex, double[] parameters) {
		if (parameterIndex == this.parameterIndex) {
			return 1;
		}
		return 0;
	}

	public String toString() {
		if (name == null) {
			return "par" + parameterIndex;
		}
		return name;
	}

	public String toLatex() {
		if (name == null) {
			return "p_{" + parameterIndex + "}";
		}
		return name;
	}

	public boolean isConstant() {
		return false;
	}

	public int hashCode() {
		return Objects.hash(parameterIndex);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable other = (Variable) obj;
		return parameterIndex == other.parameterIndex;
	}

}