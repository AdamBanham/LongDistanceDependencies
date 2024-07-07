package org.processmining.longdistancedependencies.function;

public class FunctionFactoryNonReducing implements FunctionFactory {

	private final int[] fixParameters;
	private final double fixValue;

	public FunctionFactoryNonReducing(double fixValue, int... fixParameters) {
		this.fixValue = fixValue;
		this.fixParameters = fixParameters;
	}

	public Function constant(double value) {
		return new Constant(value);
	}

	public Function variablePower(int parameterIndex, int power) {
		return variablePower(parameterIndex, null, power);
	}

	public Function product(Function... functions) {
		return new Product(functions);
	}

	public Function division(Function functionA, Function functionB) {
		return new Division(functionA, functionB);
	}

	public Function sum(Function... functions) {
		return new Sum(functions);
	}

	public Function variable(int parameterIndex) {
		return variable(parameterIndex, null);
	}

	public Function variablePower(int parameterIndex, String name, int power) {
		if (parameterIndex < fixParameters.length) {
			return constant(1);
		}
		return new VariablePower(parameterIndex, name, power);
	}

	public Function variable(int parameterIndex, String name) {
		if (parameterIndex < fixParameters.length) {
			return constant(fixValue);
		}
		return new Variable(parameterIndex, name);
	}

}