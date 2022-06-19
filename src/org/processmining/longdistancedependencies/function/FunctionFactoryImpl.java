package org.processmining.longdistancedependencies.function;

import java.util.ArrayList;
import java.util.List;

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
		return variablePower(parameterIndex, null, power);
	}

	public Function product(Function... functions) {
		List<Function> constants = new ArrayList<>();
		List<Function> B = new ArrayList<>();
		for (Function function : functions) {
			if (function instanceof Constant) {
				constants.add(function);
			} else {
				B.add(function);
			}
		}

		if (constants.size() <= 1) {
			return new Product(functions);
		}

		double constantsFactor = 1;
		for (Function function : constants) {
			constantsFactor *= function.getValue(null);
		}

		if (B.size() == 0) {
			return constant(constantsFactor);
		}

		Function[] Ba = new Function[B.size() + 1];
		B.toArray(Ba);
		Ba[Ba.length - 1] = constant(constantsFactor);
		return new Product(Ba);
	}

	public Function division(Function functionA, Function functionB) {
		if (functionA instanceof Constant && functionB instanceof Constant) {
			return constant(functionA.getValue(null) / functionB.getValue(null));
		}

		return new Division(functionA, functionB);
	}

	public Function sum(Function... functions) {
		List<Function> constants = new ArrayList<>();
		List<Function> B = new ArrayList<>();
		for (Function function : functions) {
			if (function instanceof Constant) {
				constants.add(function);
			} else {
				B.add(function);
			}
		}

		if (constants.size() <= 1) {
			return new Sum(functions);
		}

		double constantsFactor = 0;
		for (Function function : constants) {
			constantsFactor += function.getValue(null);
		}

		if (B.size() == 0) {
			return constant(constantsFactor);
		}

		Function[] Ba = new Function[B.size() + 1];
		B.toArray(Ba);
		Ba[Ba.length - 1] = constant(constantsFactor);
		return new Sum(Ba);
	}

	public Function variable(int parameterIndex) {
		return variable(parameterIndex, null);
	}

	public Function variablePower(int parameterIndex, String name, int power) {
		if (power == 0) {
			return constant(1);
		}
		if (power == 1) {
			return variable(parameterIndex, name);
		}
		if (Arrays.contains(fixParameters, parameterIndex)) {
			return constant(1);
		}
		return new VariablePower(parameterIndex, name, power);
	}

	public Function variable(int parameterIndex, String name) {
		if (Arrays.contains(fixParameters, parameterIndex)) {
			return constant(fixValue);
		}
		return new Variable(parameterIndex, name);
	}

}