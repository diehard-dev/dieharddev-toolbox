package com.dieharddev.toolbox;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ValueChecks extends UtilsCommons {
	default String checkContainsSubstringParameter(String subString, String string, String parameterName) {
		if (defaultOnEmpty(string, "").contains(defaultOnEmpty(subString, "")) == false) {
			throw new InvalidParameterException(
					"Parameter " + quoted(parameterName) + " must contain substring, '" + subString + "'");
		}
		return string;
	}

	default String checkNotContainsSubstringParameter(String subString, String string, String parameterName) {
		if (defaultOnEmpty(string, "").contains(defaultOnEmpty(subString, "")) == true) {
			throw new InvalidParameterException(
					"Parameter " + quoted(parameterName) + " must NOT contain substring, '" + subString + "'");
		}
		return string;
	}

	default String checkNotEmptyParameter(String string, String parameterName) {
		if (isNonEmpty(string) == false) {
			throw new InvalidParameterException("Parameter " + quoted(parameterName) + " must not be empty");
		}
		return string;
	}

	default <T> T checkParameterType(T reference, String parameterName, Class<?>... classes) {
		if (classes.length == 0) {
			throw new NullPointerException(
					"Parameter " + quoted(parameterName) + " not classes were specefied in test.");
		}
		List<String> classStringsList //
				= toList(classes).stream().map(new Function<Class<?>, String>() {
					@Override
					public String apply(Class<?> aClass) {
						return quoted(aClass.getName());
					}
				}).collect(Collectors.toList());
		String classsesNamesString = String.join(", ", classStringsList);
		for (Class<?> class1 : classes) {
			if (class1.isInstance(reference) == true) {
				return reference;
			}
		}
		throw new NullPointerException(
				"Parameter " + quoted(parameterName) + " was not an instance of " + classsesNamesString);
	}

	default <T> T checkInValueSetParameter(T reference, String parameterName, Set<T> valuesSet) {
		if (valuesSet.contains(reference) == false) {
			throw new NullPointerException(
					"Parameter " + quoted(parameterName) + " must not be one of " + join(valuesSet, ","));
		}
		return reference;
	}

	default <T> T checkNotNullParameter(T reference, String parameterName) {
		if (reference == null) {
			throw new NullPointerException("Parameter " + quoted(parameterName) + " must not be null");
		}
		return reference;
	}

	default <T> List<T> checkNotNullsInListParameter(List<T> aList, String parameterName) {
		checkNotNullParameter(aList, parameterName);
		for (int index = 0; index < aList.size(); index++) {
			checkNotNullParameter(aList.get(index), "parameterName[" + index + "]");
		}
		return aList;
	}

	default <T> List<T> checkNotEmptyList(List<T> aList) {
		if (aList == null) {
			throw new NullPointerException();
		}
		if (aList.size() == 0) {
			throw new InvalidParameterException("List must not be empty");
		}
		return aList;
	}

	default <T> List<T> checkNotEmptyListParameter(List<T> aList, String parameterName) {
		if (aList == null) {
			throw new NullPointerException("Parameter " + parameterName + " must not be null");
		}
		if (aList.size() == 0) {
			throw new InvalidParameterException("Parameter " + parameterName + " must not be an empty list");
		}
		return aList;
	}

	default <T> void checkNotEmptyArrayParameter(T[] anArray, String parameterName) {
		if (anArray == null) {
			throw new NullPointerException("Parameter " + parameterName + " must not be null");
		}
		if (anArray.length == 0) {
			throw new InvalidParameterException("Parameter " + parameterName + " must not be an empty array");
		}
	}

	default <T> TreeSet<T> checkNotEmptyCollection(TreeSet<T> treeSet) {
		if (treeSet == null) {
			throw new NullPointerException();
		}
		if (treeSet.size() == 0) {
			throw new InvalidParameterException("List must not be empty");
		}
		return treeSet;
	}

	default <T> T checkNotNullAndConditionForParameter(T object, String parameterName, Function<T, Boolean> condition,
			String errorMessageFormat) {
		if (object == null) {
			throw new NullPointerException("Parameter " + parameterName + " must not be null");
		}
		if (condition.apply(object) == false) {
			throw new InvalidParameterException(MessageFormat.format(errorMessageFormat, parameterName, object));
		}
		return object;
	}

	default <T, R> Map<T, R> checkMapForKeysParameter(Map<T, R> map, List<T> keys, String parameterName) {
		if (map == null) {
			throw new NullPointerException("Parameter " + parameterName + " must not be null");
		}
		List<String> missingKeys = new ArrayList<String>();
		for (T key : keys) {
			if (map.containsKey(key) == false) {
				missingKeys.add(key.toString());
			}
		}
		if (missingKeys.isEmpty() == false) {
			throw new InvalidParameterException("Parameter " + parameterName
					+ ": Map must contain values for each key - missing values for the following keys: "
					+ String.join(",", missingKeys));
		}
		return map;
	}
	// //////////////////////////////////////////////////////////////////////////
	// File/Directory Checks
	// //////////////////////////////////////////////////////////////////////////

	default File checkFileWritable(File file) {
		if (file == null) {
			throw new NullPointerException();
		}
		if (file.getAbsoluteFile().canWrite() == false) {
			if (file.getAbsoluteFile().isDirectory()) {
				throw new RuntimeException("Can write to directory: " + quoted(file.getAbsolutePath()));
			} else {
				throw new RuntimeException("Can write to file: " + quoted(file.getAbsolutePath()));
			}
		}
		return file;
	}

	default List<File> checkFilesExist(List<File> files) {
		for (File file : files) {
			checkFileWritable(file);
		}
		return files;
	}

	default File[] checkFilesExist(File... files) {
		for (File file : files) {
			checkFileWritable(file);
		}
		return files;
	}

	default File checkFileDoesNotExist(File file) {
		if (file == null) {
			throw new NullPointerException();
		}
		if (file.getAbsoluteFile().exists() == true) {
			throw new RuntimeException("File DOES exist: \"" + file.getAbsolutePath() + "\"");
		}
		return file;
	}

	default File checkFileExists(File file) {
		if (file == null) {
			throw new NullPointerException();
		}
		if (file.getAbsoluteFile().exists() == false) {
			throw new RuntimeException("File does not exist: " + quoted(file.getAbsolutePath()));
		}
		if (file.getAbsoluteFile().isFile() == false) {
			throw new RuntimeException("File is not normal file: " + quoted(file.getAbsolutePath()));
		}
		return file;
	}

	default File checkFileReadable(File file) {
		if (file == null) {
			throw new NullPointerException();
		}
		if (file.getAbsoluteFile().canRead() == false) {
			if (file.getAbsoluteFile().isDirectory()) {
				throw new RuntimeException("Can NOT read a directory: " + quoted(file.getAbsolutePath()));
			} else {
				throw new RuntimeException("Can NOT read file: " + quoted(file.getAbsolutePath()));
			}
		}
		return file;
	}

	default File checkDirectoryExists(File directory) {
		if (directory == null) {
			throw new NullPointerException();
		}
		if (directory.getAbsoluteFile().exists() == false) {
			throw new RuntimeException("Directory does not exist: " + quoted(directory.getAbsolutePath()));
		}
		if (directory.getAbsoluteFile().isDirectory() == false) {
			throw new RuntimeException("File is not directory: " + quoted(directory.getAbsolutePath()));
		}
		return directory;
	}

	default List<File> checkDirectoriesExist(List<File> directories) {
		for (File file : directories) {
			checkDirectoryExists(file);
		}
		return directories;
	}

	default File[] checkDirectoriesExist(File... directories) {
		for (File file : directories) {
			checkDirectoryExists(file);
		}
		return directories;
	}

	default List<String> checkDirectoriesAsStringsExist(List<String> directories) {
		for (String file : directories) {
			checkDirectoryExists(new File(file));
		}
		return directories;
	}

	default String[] checkDirectoriesExist(String... directories) {
		for (String file : directories) {
			checkDirectoryExists(new File(file));
		}
		return directories;
	}

	default File checkAndCanonicalizeFileParameter(File file, String fileName) {
		if (file == null) {
			throw new NullPointerException("File " + quoted(fileName) + " must not be null");
		}
		try {
			file = file.getCanonicalFile().getAbsoluteFile();
		} catch (IOException e) {
			throw new RuntimeException("Could not canonicalize file: " + quoted(file.getAbsolutePath()), e);
		}
		return file;
	}

	default File checkPerisenceDirectoryParameter(File persistenceDirectory, String directoryFilePath) {
		File canonicalPersistenceDirectory = checkAndCanonicalizeFileParameter(persistenceDirectory, directoryFilePath);
		canonicalPersistenceDirectory.mkdirs();
		checkDirectoriesExist(canonicalPersistenceDirectory);
		checkFileWritable(canonicalPersistenceDirectory);
		return canonicalPersistenceDirectory;
	}

	default File checkPerisenceFileParameter(File persistenceFile, String fileName) {
		File canonicalPersistenceFile = checkAndCanonicalizeFileParameter(persistenceFile, fileName);
		canonicalPersistenceFile.getParentFile().mkdirs();
		checkDirectoriesExist(canonicalPersistenceFile.getParent());
		if (canonicalPersistenceFile.exists() == true) {
			checkFileWritable(canonicalPersistenceFile);
		} else {
			try {
				canonicalPersistenceFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(
						"Could not create persistence file: " + quoted(canonicalPersistenceFile.getAbsolutePath()), e);
			}
		}
		if (canonicalPersistenceFile.exists() == false) {
			throw new RuntimeException(
					"Could not create persistence file: " + quoted(canonicalPersistenceFile.getAbsolutePath()));
		}
		return canonicalPersistenceFile;
	}

	default <T> void checkIsEqual(T o1, T o2) {
		if (((o1 == null) || (o2 == null)) //
				&& !((o1 == null) && (o2 == null))) {
			throw new InvalidCondition();
		}
		checkTrue(o1.equals(o2));
	}

	default void checkTrue(boolean condition) {
		if (condition == false) {
			throw new InvalidCondition();
		}
	}

	default void checkFalse(boolean condition) {
		if (condition == true) {
			throw new InvalidCondition();
		}
	}

	@SuppressWarnings("serial")
	public static class InvalidCondition extends RuntimeException {
		public InvalidCondition() {
			super();
		}

		public InvalidCondition(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public InvalidCondition(String message, Throwable cause) {
			super(message, cause);
		}

		public InvalidCondition(String message) {
			super(message);
		}

		public InvalidCondition(Throwable cause) {
			super(cause);
		}
	}
	///////////////////////////////////////////////////////////////////////////////////////////

	public static class ValueChecksInstance implements ValueChecks {
		private static ValueChecks valueChecks;

		public static ValueChecks get() {
			if (valueChecks == null) {
				valueChecks = new ValueChecks() {
				};
			}
			return valueChecks;
		}
	}

	static ValueChecks get() {
		return ValueChecksInstance.get();
	}
}
