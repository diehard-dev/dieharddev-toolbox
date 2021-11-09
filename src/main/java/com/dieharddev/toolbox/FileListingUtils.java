package com.dieharddev.toolbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public interface FileListingUtils extends UtilsCommons, ValueChecks {
	default List<File> listFilesFilesFirstOrder(File directory, String regEx) {
		final Pattern pattern = Pattern.compile(removeOuterSingleQuotes(regEx));
		List<File> filesList = new ArrayList<File>();
		Collection<File> tmpFilesList = FileUtils.listFiles(directory.getAbsoluteFile(), new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				Matcher matcher = pattern.matcher(file.getName());
				return matcher.matches();
			}

			@Override
			public boolean accept(File dir, String name) {
				return false;
			}
		}, null);
		filesList.addAll(tmpFilesList);
		Collection<File> tmpDirectories = FileUtils.listFilesAndDirs(directory.getAbsoluteFile(), new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				return false;
			}

			@Override
			public boolean accept(File dir, String name) {
				return false;
			}
		}, new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getParentFile().equals(directory);
			}

			@Override
			public boolean accept(File dir, String name) {
				return false;
			}
		});
		List<File> tmpDirectoriesList = toList(tmpDirectories.toArray(new File[] {}));
		checkIsEqual(tmpDirectoriesList.get(0).getAbsoluteFile(), directory.getAbsoluteFile());
		tmpDirectoriesList.remove(0);
		for (File directoryFile : tmpDirectoriesList) {
			List<File> filesInDirectoryList = listFilesFilesFirstOrder(directoryFile, regEx);
			filesList.addAll(filesInDirectoryList);
		}
		return filesList;
	}

	default List<File> listFilesMatchingPattern(final String directoryName, final String regEx) {
		return listFilesMatchingPattern(new File(directoryName), regEx, true);
	}

	default List<File> listFilesMatchingPattern(final String directoryName, final String regEx, boolean recursively) {
		return listFilesMatchingPattern(new File(directoryName), regEx, recursively);
	}

	default List<File> listFilesMatchingPattern(final File directory, final String regEx) {
		return listFilesMatchingPattern(directory, regEx, true);
	}

	default List<File> listFilesMatchingPattern(final File directory, final String regEx, boolean recursively) {
		checkDirectoryExists(directory.getAbsoluteFile());
		final Pattern pattern = Pattern.compile(removeOuterSingleQuotes(regEx));
		List<File> filesList = new ArrayList<File>();
		IOFileFilter ioFileFilter = null;
		if (recursively == true) {
			ioFileFilter = TrueFileFilter.INSTANCE;
		}
		Collection<File> tmpList = FileUtils.listFiles(directory.getAbsoluteFile(), new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				Matcher matcher = pattern.matcher(file.getName());
				return matcher.matches();
			}

			@Override
			public boolean accept(File dir, String name) {
				return false;
			}
		}, ioFileFilter);
		filesList.addAll(tmpList);
		return filesList;
	}
}
