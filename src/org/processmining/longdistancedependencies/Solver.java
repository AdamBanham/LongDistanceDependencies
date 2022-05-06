package org.processmining.longdistancedependencies;

public class Solver {
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
