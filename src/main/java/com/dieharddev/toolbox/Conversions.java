package com.dieharddev.toolbox;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface Conversions {
	@SuppressWarnings("unchecked")
	default <T> T[] toArray(Class<T> aClass, List<T> aList) {
		T[] resultArray = (T[]) Array.newInstance(aClass, aList.size());
		for (int index = 0; index < resultArray.length; index++) {
			resultArray[index] = aList.get(index);
		}
		return resultArray;
	}

	default <T> T[] toArray(Set<T> aSet) {
		if (aSet.size() == 0) {
			throw new RuntimeException("Set was empty - unsupported");
		}
		return toArray(aSet.stream().collect(Collectors.toList()));
	}

	@SuppressWarnings("unchecked")
	default <T> T[] toArray(List<T> aList) {
		if (aList.size() == 0) {
			throw new RuntimeException("List was empty - unsupported");
		}
		return toArray((Class<T>) aList.get(0).getClass(), aList);
	}

	default int[] toIntArray(List<Integer> aList) {
		int[] result = new int[aList.size()];
		for (int index = 0; index < result.length; index++) {
			result[index] = aList.get(index);
		}
		return result;
	}

	default long[] toLongArray(List<Long> aList) {
		long[] result = new long[aList.size()];
		for (int index = 0; index < result.length; index++) {
			result[index] = aList.get(index);
		}
		return result;
	}

	default double[] toDoubleArray(List<Double> aList) {
		double[] result = new double[aList.size()];
		for (int index = 0; index < result.length; index++) {
			result[index] = aList.get(index);
		}
		return result;
	}

	default boolean[] toBooleanArray(List<Boolean> aList) {
		boolean[] result = new boolean[aList.size()];
		for (int index = 0; index < result.length; index++) {
			result[index] = aList.get(index);
		}
		return result;
	}

	default List<String> toStringList(Set<Object> set) {
		List<String> resultList = new ArrayList<String>();
		for (Object object : set) {
			resultList.add(object.toString());
		}
		return resultList;
	}

	default List<Integer> toIntegerList(int[] values) {
		List<Integer> resultList = new ArrayList<Integer>();
		for (int index = 0; index < values.length; index++) {
			resultList.add(values[index]);
		}
		return resultList;
	}

	default Integer[] toIntegerArray(int[] values) {
		Integer[] result = new Integer[values.length];
		for (int index = 0; index < values.length; index++) {
			result[index] = values[index];
		}
		return result;
	}

	default List<Double> toDoubleList(double[] values) {
		List<Double> resultList = new ArrayList<Double>();
		for (int index = 0; index < values.length; index++) {
			resultList.add(values[index]);
		}
		return resultList;
	}

	default Double[] toDoubleArray(double[] values) {
		Double[] result = new Double[values.length];
		for (int index = 0; index < values.length; index++) {
			result[index] = values[index];
		}
		return result;
	}

	default List<Long> toLongList(long[] values) {
		List<Long> resultList = new ArrayList<Long>();
		for (int index = 0; index < values.length; index++) {
			resultList.add(values[index]);
		}
		return resultList;
	}

	default Long[] toLongArray(long[] values) {
		Long[] result = new Long[values.length];
		for (int index = 0; index < values.length; index++) {
			result[index] = values[index];
		}
		return result;
	}

	default List<Boolean> toBooleanList(boolean[] values) {
		List<Boolean> resultList = new ArrayList<Boolean>();
		for (int index = 0; index < values.length; index++) {
			resultList.add(values[index]);
		}
		return resultList;
	}

	default Boolean[] toBooleanArray(boolean[] values) {
		Boolean[] result = new Boolean[values.length];
		for (int index = 0; index < values.length; index++) {
			result[index] = values[index];
		}
		return result;
	}

	default List<String> toStringList(Object... objects) {
		List<String> resultList = new ArrayList<String>();
		for (Object object : objects) {
			resultList.add(object.toString());
		}
		return resultList;
	}

	default <T> List<T> toList(Set<T> set) {
		return new ArrayList<T>(set);
	}

	default <T> List<T> toList(T... objects) {
		return new ArrayList<T>(Arrays.asList(objects));
	}

	default <T> Set<T> toSet(T... objects) {
		return new HashSet<T>(Arrays.asList(objects));
	}
}
