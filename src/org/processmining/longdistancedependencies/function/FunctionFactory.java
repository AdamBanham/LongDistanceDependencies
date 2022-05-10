package org.processmining.longdistancedependencies.function;

public interface FunctionFactory {
	public Function constant(double value);

	public Function variablePower(int parameterIndex, int power);

	public Function product(Function... functions);

	public Function division(Function functionA, Function functionB);

	public Function sum(Function... functions);

	public Function variable(int parameterIndex);
}