package org.processmining.longdistancedependencies.function;

import java.util.Objects;

public class Constant implements Function {

	private final double value;

	public Constant(double value) {
		this.value = value;
	}

	public double getValue(double[] parameters) {
		return value;
	}

	public double getPartialDerivative(int parameterIndex, double[] parameters) {
		return 0;
	}

	public String toString() {
		return value + "";
	}

	public String toLatex() {
		return value + "";
	}

	public boolean isConstant() {
		return true;
	}

	public int hashCode() {
		return Objects.hash(value);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Constant other = (Constant) obj;
		return Double.doubleToLongBits(value) == Double.doubleToLongBits(other.value);
	}

}