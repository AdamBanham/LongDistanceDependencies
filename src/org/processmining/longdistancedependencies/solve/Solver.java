package org.processmining.longdistancedependencies.solve;

import java.util.List;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import org.processmining.longdistancedependencies.function.Function;

public class Solver {

	//other solvers:

	//https://scipopt.org/index.php#license

	public static double[] solve(List<Function> equations, double[] values, int numberOfParameters, int[] fixParameters,
			double fixValue) {
		MultivariateJacobianFunction jfunction = new MultivariateJacobianFunction() {

			public Pair<RealVector, RealMatrix> value(RealVector point) {
				double[] pointD = point.toArray();

				RealVector value = new ArrayRealVector(equations.size());
				RealMatrix jacobian = new Array2DRowRealMatrix(equations.size(), numberOfParameters);

				for (int equationIndex = 0; equationIndex < equations.size(); equationIndex++) {
					Function observation = equations.get(equationIndex);

					value.setEntry(equationIndex, observation.getValue(pointD));

					for (int parameterIndex = 0; parameterIndex < numberOfParameters; parameterIndex++) {
						jacobian.setEntry(equationIndex, parameterIndex,
								observation.getPartialDerivative(parameterIndex, pointD));
					}
				}

				System.out.println("point " + point);
				System.out.println("value " + value);
				//System.out.println("jacobian " + jacobian);
				System.out.println();
				return new Pair<RealVector, RealMatrix>(value, jacobian);
			}
		};

		//initial guess: all weights are equal, and no adjustments
		RealVector initialGuess = new ArrayRealVector(numberOfParameters);
		initialGuess.set(1);
		for (int parameter : fixParameters) {
			initialGuess.setEntry(parameter, fixValue);
		}

		System.out.println("Initial guess: " + initialGuess);
		//		RealVector initialGuess = new ArrayRealVector(numberOfParameters);
		//		initialGuess.set(2);

		LeastSquaresProblem problem = new LeastSquaresBuilder()//
				.start(initialGuess)//
				.model(jfunction)//
				.target(values)//
				.lazyEvaluation(false)//
				.maxEvaluations(1000)//
				.maxIterations(1000)//
				.build();
		LeastSquaresOptimizer optimiser = new LevenbergMarquardtOptimizer().withCostRelativeTolerance(1.0e-12)
				.withParameterRelativeTolerance(1.0e-12);
		//LeastSquaresOptimizer optimiser = new GaussNewtonOptimizer(GaussNewtonOptimizer.Decomposition.CHOLESKY);
		Optimum optimum = optimiser.optimize(problem);

		System.out.println("RMS: " + optimum.getRMS());
		System.out.println("evaluations: " + optimum.getEvaluations());
		System.out.println("iterations: " + optimum.getIterations());

		return optimum.getPoint().toArray();
	}
	//	final static double radius = 70.0;
	//	final static Cartesian2D[] observedPoints = new Cartesian2D[] { new Cartesian2D(30.0, 68.0),
	//			new Cartesian2D(50.0, -6.0), new Cartesian2D(110.0, -20.0), new Cartesian2D(35.0, 15.0),
	//			new Cartesian2D(45.0, 97.0) };
	//
	//	public static void main(String[] args) {
	//
	//		// the model function components are the distances to current estimated center,
	//		// they should be as close as possible to the specified radius
	//		MultivariateJacobianFunction distancesToCurrentCenter = new MultivariateJacobianFunction() {
	//			public Pair<RealVector, RealMatrix> value(final RealVector point) {
	//
	//				Cartesian2D center = new Cartesian2D(point.getEntry(0), point.getEntry(1));
	//
	//				RealVector value = new ArrayRealVector(observedPoints.length);
	//				RealMatrix jacobian = new Array2DRowRealMatrix(observedPoints.length, 2);
	//
	//				for (int i = 0; i < observedPoints.length; ++i) {
	//					Cartesian2D o = observedPoints[i];
	//					double modelI = Cartesian2D.distance(o, center);
	//					value.setEntry(i, modelI);
	//					// derivative with respect to p0 = x center
	//					jacobian.setEntry(i, 0, (center.getX() - o.getX()) / modelI);
	//					// derivative with respect to p1 = y center
	//					jacobian.setEntry(i, 1, (center.getX() - o.getX()) / modelI);
	//				}
	//
	//				return new Pair<RealVector, RealMatrix>(value, jacobian);
	//
	//			}
	//		};
	//
	//		// the target is to have all points at the specified radius from the center
	//		double[] prescribedDistances = new double[observedPoints.length];
	//		Arrays.fill(prescribedDistances, radius);
	//
	//		// least squares problem to solve : modeled radius should be close to target radius
	//		LeastSquaresProblem problem = new LeastSquaresBuilder().start(new double[] { 100.0, 50.0 })
	//				.model(distancesToCurrentCenter).target(prescribedDistances).lazyEvaluation(false).maxEvaluations(1000)
	//				.maxIterations(1000).build();
	//		LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().optimize(problem);
	//		Cartesian2D fittedCenter = new Cartesian2D(optimum.getPoint().getEntry(0), optimum.getPoint().getEntry(1));
	//		System.out.println("fitted center: " + fittedCenter.getX() + " " + fittedCenter.getY());
	//		System.out.println("RMS: " + optimum.getRMS());
	//		System.out.println("evaluations: " + optimum.getEvaluations());
	//		System.out.println("iterations: " + optimum.getIterations());
	//	}
}
