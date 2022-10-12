package org.processmining.longdistancedependencies.solve;

import java.util.BitSet;
import java.util.List;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import org.processmining.longdistancedependencies.choicedata.Equation;
import org.processmining.longdistancedependencies.function.Function;

public class Solver {

	//other solvers:

	//https://scipopt.org/index.php#license

	/**
	 * 
	 * @param values
	 *            of the equations
	 * @param equations
	 * @param occurrences
	 *            of the equations
	 * @param numberOfParameters
	 * @param fixParameters
	 * @param initialParameterValues
	 * @return
	 */
	public static double[] solve(List<Equation> equations, int numberOfParameters, int[] fixParameters,
			double[] initialParameterValues) {
		MultivariateJacobianFunction jfunction = new MultivariateJacobianFunction() {

			public Pair<RealVector, RealMatrix> value(RealVector point) {
				double[] pointD = point.toArray();

				RealVector value = new ArrayRealVector(equations.size());
				RealMatrix jacobian = new Array2DRowRealMatrix(equations.size(), numberOfParameters);

				for (int equationIndex = 0; equationIndex < equations.size(); equationIndex++) {
					Function observation = equations.get(equationIndex).getFunction();

					value.setEntry(equationIndex, observation.getValue(pointD));

					for (int parameterIndex = 0; parameterIndex < numberOfParameters; parameterIndex++) {
						jacobian.setEntry(equationIndex, parameterIndex,
								observation.getPartialDerivative(parameterIndex, pointD));
					}
				}

				//				System.out.println();
				//				System.out.println("point " + point);
				//				System.out.println("value " + value);
				//System.out.println("jacobian " + jacobian);
				return new Pair<RealVector, RealMatrix>(value, jacobian);
			}
		};

		final BitSet fixedParametersb = new BitSet();
		for (int parameter : fixParameters) {
			fixedParametersb.set(parameter);
		}

		ParameterValidator validator = new ParameterValidator() {
			public RealVector validate(RealVector params) {
				//				System.out.println("validate " + params);
				for (int i = 0; i < params.getDimension(); i++) {
					if (params.getEntry(i) < 0) {
						params.setEntry(i, 0);
					} else if (fixedParametersb.get(i)) {
						params.setEntry(i, 1);
					}
				}
				return params;
			}
		};

		//set the initial guess
		RealVector initialGuess = new ArrayRealVector(numberOfParameters);
		for (int parameter = 0; parameter < numberOfParameters; parameter++) {
			initialGuess.setEntry(parameter, initialParameterValues[parameter]);
		}

		//		System.out.println("Initial guess: " + initialGuess);
		//		System.out.println("target " + Arrays.toString(values));
		//		RealVector initialGuess = new ArrayRealVector(numberOfParameters);
		//		initialGuess.set(2);

		//set the weights
		RealMatrix weight = new Array2DRowRealMatrix(equations.size(), equations.size());
		for (int equation = 0; equation < equations.size(); equation++) {
			weight.setEntry(equation, equation, equations.get(equation).getOccurrences());
		}

		LeastSquaresProblem problem = new LeastSquaresBuilder()//
				.start(initialGuess)//
				.model(jfunction)//
				.target(Equation.getValues(equations))//
				.weight(weight)//
				.parameterValidator(validator)//
				.lazyEvaluation(false)//
				.maxEvaluations(1000000)//
				.maxIterations(1000000)//
				.build();
		LeastSquaresOptimizer optimiser = new LevenbergMarquardtOptimizer().withCostRelativeTolerance(1.0e-10)
				.withParameterRelativeTolerance(1.0e-10);
		//LeastSquaresOptimizer optimiser = new GaussNewtonOptimizer(GaussNewtonOptimizer.Decomposition.CHOLESKY);
		Optimum optimum = optimiser.optimize(problem);

		//		System.out.println("RMS: " + optimum.getRMS());
		//		System.out.println("evaluations: " + optimum.getEvaluations());
		//		System.out.println("iterations: " + optimum.getIterations());

		return optimum.getPoint().toArray();
	}
}
