package org.processmining.longdistancedependencies.function;

/**
 * Works on an array of double, which are the parameters.
 * 
 * @author sander
 *
 */
public interface Function {
	public double getValue(double[] parameters);

	public double getPartialDerivative(int parameterIndex, double[] parameters);

	public String toLatex();

	public boolean isConstant();
}