package org.processmining.longdistancedependencies.choicedata;

import java.util.List;

import org.processmining.longdistancedependencies.function.Function;

public class Equation {

	private Function function;
	private double value;
	private int occurrences;

	public Equation(double value, Function function, int occurrences) {
		this.value = value;
		this.function = function;
		this.occurrences = occurrences;
	}

	public Function getFunction() {
		return function;
	}

	public int getOccurrences() {
		return occurrences;
	}

	public double getValue() {
		return value;
	}

	public String toString() {
		return value + "=" + function.toString() + "@" + occurrences;
	}

	public static double[] getValues(List<Equation> equations) {
		double[] result = new double[equations.size()];
		for (int i = 0; i < equations.size(); i++) {
			result[i] = equations.get(i).getValue();
		}

		return result;
	}
}