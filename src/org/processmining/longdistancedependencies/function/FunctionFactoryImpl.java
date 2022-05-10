package org.processmining.longdistancedependencies.function;

import com.util.Arrays;

public class FunctionFactoryImpl implements FunctionFactory {

	private final int[] fixParameters;
	private final double fixValue;

	public FunctionFactoryImpl(double fixValue, int... fixParameters) {
		this.fixValue = fixValue;
		this.fixParameters = fixParameters;
	}

	public Function constant(double value) {
		return new Constant(value);
	}

	public Function variablePower(int parameterIndex, int power) {
		if (power == 1) {
			return variable(parameterIndex);
		}
		return new VariablePower(parameterIndex, power);
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
		if (Arrays.contains(fixParameters, parameterIndex)) {
			return constant(fixValue);
		}
		return new Variable(parameterIndex);
	}

}
