package org.processmining.longdistancedependencies;

public class FixedMultiset {

	public static int[] init(int size) {
		return new int[size];
	}

	/**
	 * 
	 * @param multiset
	 * @param previous,
	 *            may be -1
	 * @return The next non-0 element, or -1 if there is no such element.
	 */
	public static int next(int[] multiset, int previous) {
		previous++;
		while (previous < multiset.length) {
			if (multiset[previous] > 0) {
				return previous;
			}
			previous++;
		}
		return -1;
	}

	public static int setSize(int[] multiset) {
		int result = 0;
		for (int elem : multiset) {
			if (elem > 0) {
				result++;
			}
		}
		return result;
	}

	public static boolean setSizeLargerThanOne(int[] multiset) {
		int next = next(multiset, -1);
		if (next < 0) {
			return false;
		}
		next = next(multiset, next);
		return next >= 0;
	}
}