package com.dieharddev.toolbox;

import java.awt.Desktop;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.jcabi.jdbc.JdbcSession;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Builder;
import lombok.Data;

public interface ToolBox extends UtilsCommons, ValueChecks, FileListingUtils, Conversions {
	default File writeLinesToFile(String fileName, List<String> linesList) {
		File file = new File(fileName);
		writeLinesToFile(file, linesList);
		return file;
	}

	default void writeLinesToFile(File file, List<String> linesList) {
		String linesString = join(linesList, "\n");
		writeFile(file, linesString);
	}

	default void writeFile(String fileName, CharSequence data) {
		File file = new File(fileName);
		writeFile(file, data);
	}

	default void writeFile(File file, CharSequence data) {
		if (file.exists() == true) {
			if (file.canWrite() == false) {
				throw new RuntimeException("writeFile(): Do not have permissions to write to existing file, \""
						+ file.getAbsolutePath() + "\"");
			}
		}
		try {
			FileUtils.write(file.getAbsoluteFile(), data);
		} catch (IOException e) {
			new RuntimeException("Could not write file, \"" + file.getAbsolutePath() + "\"", e);
		}
	}

	default byte[] readFileToBytes(String fileName) {
		File file = new File(fileName);
		return readFileToBytes(file);
	}

	default byte[] readFileToBytes(File file) {
		try {
			return FileUtils.readFileToByteArray(file.getAbsoluteFile());
		} catch (IOException e) {
			throw new RuntimeException("readFileToBytes(): Could not read file, \"" + file.getAbsolutePath() + "\"", e);
		}
	}

	default String readFilesToString(List<File> files) {
		return readFilesToString("", files);
	}

	default String readFilesToString(File... files) {
		return readFilesToStringWithSeparator("", files);
	}

	default String readFilesToStringWithSeparator(String separator, File... files) {
		return readFilesToString(separator, toList(files));
	}

	default String readFilesToString(String... fileNames) {
		return readFilesToStringWithSeparator("", fileNames);
	}

	default String readFilesToStringWithSeparator(String separator, String... fileNames) {
		List<File> files = Arrays.stream(fileNames)//
				.map(new Function<String, File>() {
					@Override
					public File apply(String fileName) {
						return new File(fileName);
					}
				}).collect(Collectors.toList());
		return readFilesToString(separator, files);
	}

	default String readFilesToString(String separator, List<File> files) {
		List<String> fileContentsList = new ArrayList<String>();
		for (File file : files) {
			fileContentsList.add(readFileToString(file));
		}
		return String.join(separator, fileContentsList);
	}

	default String readFileToString(File file) {
		return readFileToString(file, Charset.defaultCharset());
	}

	default String readFileToString(File file, Charset charset) {
//                            commonFileReadAssertions(file);
		try {
			return FileUtils.readFileToString(file.getAbsoluteFile(), charset);
		} catch (IOException e) {
			throw new RuntimeException("readFileToString(): Could not read file, \"" + file.getAbsolutePath() + "\"",
					e);
		}
	}
	// //////////////////////////////////////////////////////////////////////////////////////////////////
	// Json Support
	// //////////////////////////////////////////////////////////////////////////////////////////////////

	default String getPrettyJson(Object object) {
		return toPrettyJson(object);
	}

	public static Type ExposedMethodsListType = new TypeToken<List<ToolBoxExposedMethods>>() {
	}.getType();

	default String toJson(Object object, Type typeOfSrc) {
		return new GsonBuilder().serializeNulls().disableHtmlEscaping()
				.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
				.registerTypeAdapter(ToolBoxExposedMethods.class, new MethodSerializer()).create()
				.toJson(object, typeOfSrc);
	}

	default String toJson(Object object) {
		if (object instanceof List<?>) {
			return toJson(object, ExposedMethodsListType);
		}
		return new GsonBuilder().serializeNulls().disableHtmlEscaping()
				.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
				.registerTypeAdapter(ToolBoxExposedMethods.class, new MethodSerializer()).create().toJson(object);
	}

	default String toPrettyJson(Object object, Type typeOfSrc) {
		return new GsonBuilder().serializeNulls().disableHtmlEscaping()
				.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
				.registerTypeAdapter(ToolBoxExposedMethods.class, new MethodSerializer()).setPrettyPrinting().create()
				.toJson(object, typeOfSrc);
	}

	default String toPrettyJson(Object object, ExclusionStrategy... exclusionStrategies) {
		return new GsonBuilder().serializeNulls().disableHtmlEscaping()
				.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
				.registerTypeAdapter(ToolBoxExposedMethods.class, new MethodSerializer()).setPrettyPrinting()//
				.setExclusionStrategies(exclusionStrategies)//
				.create().toJson(object);
	}

	default String toPrettyJson(Object object) {
		return new GsonBuilder().serializeNulls().disableHtmlEscaping()
				.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
				.registerTypeAdapter(ToolBoxExposedMethods.class, new MethodSerializer()).setPrettyPrinting()//
				.create().toJson(object);
	}

	default void toPrettyJsonFile(File file, Object object) {
		String jsonString = toPrettyJson(object);
		writeFile(file, jsonString);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	// can use in method only.
	public static @interface ToolBoxExposeMethod {
	};

	public static interface ToolBoxExposedMethods {
	};

	public static class MethodSerializer implements JsonSerializer<Object> {
		@Override
		public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
			Gson gson = new Gson();
			JsonObject tree = (JsonObject) gson.toJsonTree(src);
			try {
				PropertyDescriptor[] properties = Introspector.getBeanInfo(src.getClass()).getPropertyDescriptors();
				for (PropertyDescriptor property : properties) {
					if (property.getReadMethod().getAnnotation(ToolBoxExposeMethod.class) != null) {
						Object result = property.getReadMethod().invoke(src, (Object[]) null);
						tree.add(property.getName(), gson.toJsonTree(result));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return tree;
		}
	}

	public static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
		private final DateFormat dateFormatWithZ;
		private final DateFormat dateFormat;
		// "2016-07-27T15:30:40.864Z"

		private DateTypeAdapter() {
			dateFormatWithZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
			dateFormatWithZ.setTimeZone(TimeZone.getTimeZone("UTC"));
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);
		}

		@Override
		public synchronized JsonElement serialize(Date date, Type type,
				JsonSerializationContext jsonSerializationContext) {
			return new JsonPrimitive(dateFormat.format(date));
		}

		@Override
		public synchronized Date deserialize(JsonElement jsonElement, //
				Type type, //
				JsonDeserializationContext jsonDeserializationContext) {
			try {
				String jsonString = jsonElement.getAsString();
				if (jsonString.endsWith("Z") == true) {
					return dateFormatWithZ.parse(jsonString);
				} else {
					return dateFormat.parse(jsonString);
				}
			} catch (ParseException e) {
				throw new JsonParseException(e);
			}
		}
	}

	default Object fromJsonWithGitLabDateFormat(String jsonString, Class<?> aClass) {
		return new GsonBuilder().disableHtmlEscaping()//
				.serializeNulls()//
				.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)//
				.registerTypeAdapter(ToolBoxExposedMethods.class, new MethodSerializer())//
				.registerTypeAdapter(Date.class, new DateTypeAdapter())//
				.registerTypeAdapter(java.sql.Date.class, new DateTypeAdapter())//
				.create()//
				.fromJson(jsonString, aClass);
	}

	default <T> T fromJson(String jsonString, Type type) {
		return new GsonBuilder().disableHtmlEscaping()//
				.serializeNulls()//
				.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)//
				.registerTypeAdapter(ToolBoxExposedMethods.class, new MethodSerializer())//
				.create()//
				.fromJson(jsonString, type);
	}

	default <T> T fromJson(String jsonString, Class<T> aClass) {
		return new GsonBuilder().disableHtmlEscaping()//
				.serializeNulls()//
				.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)//
				.registerTypeAdapter(ToolBoxExposedMethods.class, new MethodSerializer())//
				.create()//
				.fromJson(jsonString, aClass);
	}

	default <T> T fromJsonFile(String fileName, Class<T> aClass) {
		String jsonString = readFilesToString(fileName);
		return fromJson(jsonString, aClass);
	}

	default <T> T fromJsonFile(File file, Class<T> aClass) {
		String jsonString = readFilesToString(file);
		return fromJson(jsonString, aClass);
	}

	public static class EncryptionSerializer implements JsonSerializer<EncryptedString> {
		private final PropertyEncryption propertyEncryption;

		public EncryptionSerializer(PropertyEncryption propertyEncryption) {
			super();
			this.propertyEncryption = propertyEncryption;
		}

		@Override
		public JsonElement serialize(EncryptedString encryptedString, Type typeOfSrc,
				JsonSerializationContext context) {
			final JsonObject jsonObject = new JsonObject();
			if (propertyEncryption == null) {
				jsonObject.addProperty("encryptedValue", (String) null);
			} else {
				jsonObject.addProperty("encryptedValue",
						propertyEncryption.encryptProperty(encryptedString.getValue()));
			}
			return jsonObject;
		}
	}

	public static class DecryptionDeserializer implements JsonDeserializer<EncryptedString> {
		private final PropertyEncryption propertyEncryption;

		public DecryptionDeserializer(PropertyEncryption propertyEncryption) {
			super();
			this.propertyEncryption = propertyEncryption;
		}

		@Override
		public EncryptedString deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			String value = "";
			try {
				final JsonObject jsonObject = json.getAsJsonObject();
				final JsonElement jsonEncryptedValue = jsonObject.get("encryptedValue");
				if (jsonEncryptedValue.isJsonNull() == true) {
					value = "";
				} else {
					value = propertyEncryption.decryptProperty(jsonEncryptedValue.getAsString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new EncryptedString(value);
		}
	}

	default Object fromJson(String jsonString, Class<?> aClass, PropertyEncryption propertyEncryption) {
		return new GsonBuilder().serializeNulls().disableHtmlEscaping()//
				.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
				.registerTypeAdapter(ToolBoxExposedMethods.class, new MethodSerializer())
				.registerTypeAdapter(EncryptedString.class, new DecryptionDeserializer(propertyEncryption))
				.disableHtmlEscaping().create().fromJson(jsonString, aClass);
	}

	default String toPrettyJson(Object object, PropertyEncryption propertyEncryption) {
		if (object instanceof List<?>) {
			throw new RuntimeException("Not supported yet...");
			// return toPrettyJson(object, ExposedMethodsListType);
		}
		return new GsonBuilder().serializeNulls().disableHtmlEscaping()
				// .disableHtmlEscaping()
				.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
				.registerTypeAdapter(ToolBoxExposedMethods.class, new MethodSerializer())
				.registerTypeAdapter(EncryptedString.class, new EncryptionSerializer(propertyEncryption))
				.setPrettyPrinting().create().toJson(object);
	}
	// /////////////////////////////////////////////////////////////////////
	// Browser Support
	// /////////////////////////////////////////////////////////////////////

	default String getHost(SocketAddress socketAddress) {
		if (socketAddress instanceof InetSocketAddress) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
			return inetSocketAddress.getHostName();
		} else {
			throw new RuntimeException(
					"Found unsupported SocketAddress, " + quoted(socketAddress.getClass().getName()));
		}
	}

	default int getPort(SocketAddress socketAddress) {
		if (socketAddress instanceof InetSocketAddress) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
			return inetSocketAddress.getPort();
		} else {
			throw new RuntimeException(
					"Found unsupported SocketAddress, " + quoted(socketAddress.getClass().getName()));
		}
	}

	default void openBrowser(String url) {
		openUrl(url);
	}

	default void openUrl(String url) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException("Failed to open browser", e);
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("xdg-open " + url);
			} catch (IOException e) {
				throw new RuntimeException("Failed to open browser", e);
			}
		}
	}
	///////////////////////////////////////////////////////////////////////////////////////////

	@Builder
	@Data
	public static class AMap {
		private final Map<String, String> aMap;
	}

	default Map<String, String> readAMap(String fileName) {
		String jsonString = readFilesToString(fileName);
		AMap aMap = fromJson(jsonString, AMap.class);
		return aMap.getAMap();
	}
	// Lines

	default boolean containsFullLine(String string) {
		final Pattern newlinePattern = Pattern.compile("\\r?\\n");
		Matcher matcher = newlinePattern.matcher(string);
		return matcher.find();
	}

	default String removeEmptyLines(String string) {
		String[] lines = getLines(string);
		List<String> nonEmptyLines = new ArrayList<String>();
		for (String line : lines) {
			if (line.trim().equals("") == false) {
				nonEmptyLines.add(line);
			}
		}
		return String.join("\n", nonEmptyLines);
	}

	default String[] toLines(String string) {
		return getLines(string);
	}

	default String[] getLines(String string) {
		return string.split("\\r?\\n", -1);
	}

	default List<String> toLinesList(String string) {
		String[] lines = getLines(string);
		return new ArrayList<String>(Arrays.asList(lines));
//                            if (lines.length == 0) {
//                                            return new ArrayList<String>();
//                            } else {
//                                            return new ArrayList<String>(Arrays.asList(lines));
//                            }
	}

	default String[] getNonEmptyLines(String string) {
		String[] lines = getLines(string);
		List<String> nonEmptyLines = new ArrayList<String>();
		for (String line : lines) {
			if (line.trim().equals("") == false) {
				nonEmptyLines.add(line);
			}
		}
		return nonEmptyLines.toArray(new String[nonEmptyLines.size()]);
	}

	default String[] splitParts(String string, String separator) {
		return string.split(separator, -1);
	}
	///////////////////////////////////////////////////////////////////////////////////////////
	// SQL
	///////////////////////////////////////////////////////////////////////////////////////////

	public static interface ListGetter<T> {
		T getValue(final ResultSet resultSet) throws SQLException;
	}

	default <T> List<T> getList(//
			String queryNameForErrors, //
			String sqlString, //
			HikariDataSource hikariDataSource, //
			ListGetter<T> mapper) {
		return //
		getList(//
				queryNameForErrors, //
				sqlString, //
				new Object[] {}, //
				hikariDataSource, //
				3000, //
				mapper);
	}

	default <T> List<T> getList(//
			String queryNameForErrors, //
			String sqlString, //
			HikariDataSource hikariDataSource, //
			int fetchSize, //
			ListGetter<T> mapper) {
		return //
		getList(//
				queryNameForErrors, //
				sqlString, //
				new Object[] {}, //
				hikariDataSource, //
				fetchSize, //
				mapper);
	}

	default <T> List<T> getList(//
			String queryNameForErrors, //
			String sqlString, //
			Object[] args, //
			HikariDataSource hikariDataSource, //
			ListGetter<T> mapper) {
		return //
		getList(//
				queryNameForErrors, //
				sqlString, //
				args, //
				hikariDataSource, //
				3000, //
				mapper);
	}

	default <T> List<T> getList(//
			String queryNameForErrors, //
			String sqlString, //
			Object[] args, //
			HikariDataSource hikariDataSource, //
			int fetchSize, //
			ListGetter<T> mapper) {
		try {
			JdbcSession jdbcSession //
					= new JdbcSession(hikariDataSource).sql(sqlString);
			for (Object object : args) {
				jdbcSession.set(object);
			}
			return //
			jdbcSession//
					.select(new NovyyListOutcome<T>(//
							fetchSize, //
							new NovyyListOutcome.Mapping<T>() {
								@Override
								public T map(final ResultSet resultSet) throws SQLException {
									return mapper.getValue(resultSet);
								}
							}));
		} catch (SQLException e) {
			throw new RuntimeException("SQL query failed for " + queryNameForErrors, e);
		}
	}

	default List<Map<String, Object>> queryForList(//
			String queryNameForErrors, //
			String sqlString, //
			HikariDataSource hikariDataSource) {
		return //
		getList(//
				queryNameForErrors, //
				sqlString, //
				hikariDataSource, //
				new ListGetter<Map<String, Object>>() {
					private List<String> columnNames;

					private List<String> getColumnNames(ResultSet resultSet) throws SQLException {
						if (columnNames == null) {
							columnNames = new ArrayList<>();
							ResultSetMetaData rsMetaData = resultSet.getMetaData();
							for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
								columnNames.add(rsMetaData.getColumnName(i));
							}
						}
						return columnNames;
					}

					@Override
					public Map<String, Object> getValue(ResultSet resultSet) throws SQLException {
						Map<String, Object> result = new HashMap<String, Object>();
						for (String columnName : getColumnNames(resultSet)) {
							result.put(columnName, resultSet.getObject(columnName));
						}
						return result;
					}
				});
	}

	default String getStringWithDefault(ResultSet resultSet, String columnName, String defaultValue)
			throws SQLException {
		String value = resultSet.getString(columnName);
		if (resultSet.wasNull() == true) {
			return defaultValue;
		} else {
			return value;
		}
	}
	/////////////

	default byte[] readClassFileToBytes(Class aClass, String fileName) {
		try {
			InputStream fileInputStream = aClass.getResourceAsStream(fileName);
			if (fileInputStream == null) {
				throw new FileNotFoundException("class://" + fileName + " relative to " + aClass.getName());
			}
			return IOUtils.toByteArray(fileInputStream);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read file " + quoted(fileName)
					+ " from class path starting at class, " + quoted(aClass.getName()), e);
		}
	}

	default String readClassFileToString(Class aClass, String fileName) {
		try {
			InputStream fileInputStream = aClass.getResourceAsStream(fileName);
			if (fileInputStream == null) {
				throw new FileNotFoundException("class://" + fileName + " relative to " + aClass.getName());
			}
			return IOUtils.toString(fileInputStream);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read file " + quoted(fileName)
					+ " from class path starting at class, " + quoted(aClass.getName()), e);
		}
	}
	/////////////

	default ThreadFactory getNamedThreadFactory(String namePrefix) {
		return NamedThreadFactory.getNamedThreadFactory(namePrefix);
	}

	default ThreadFactory getNamedThreadFactory(Class<?> aClass, String namePrefix) {
		return getNamedThreadFactory(aClass.getSimpleName() + "-" + namePrefix);
	}

	default int getAvailableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}

	public static class ScheduledExecutor {
		private final ExecutorService executorService;
		private final ScheduledExecutorService scheduledExecutorService;

		public ScheduledExecutor(int coreSize, String threadPoolNamePrefix) {
			super();
			this.executorService //
					= new ThreadPoolExecutor(100, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
							new SynchronousQueue<Runnable>());
			this.scheduledExecutorService //
					= Executors//
							.newScheduledThreadPool(2, NamedThreadFactory//
									.getNamedThreadFactory(threadPoolNamePrefix + "ScheduledExecutorService"));
		}
	}
	/////////////////

	default <T, U> U getSafely(T key, Map<T, U> map) {
		synchronized (map) {
			return map.get(key);
		}
	}

	default <T, U> U getSafely(T key, U defaultValue, Map<T, U> map) {
		synchronized (map) {
			return map.getOrDefault(key, defaultValue);
		}
	}

	default <T> List<T> getSafeCopy(List<T> list) {
		synchronized (list) {
			return new ArrayList<T>(list);
		}
	}

	default <T, U> Map<T, U> getSafeCopy(Map<T, U> map) {
		synchronized (map) {
			return new HashMap<T, U>(map);
		}
	}

	default <T> void updateListValues(Supplier<List<T>> listSupplier, List<T> list) {
		List<T> newValues = listSupplier.get();
		synchronized (list) {
			list.clear();
			list.addAll(newValues);
		}
	}

	default <T> void safeListUpdate(List<T> newValues, List<T> list) {
		synchronized (list) {
			list.clear();
			list.addAll(newValues);
		}
	}

	default <T, U> void safeMapUpdate(Supplier<Map<T, U>> mapSupplier, Map<T, U> map) {
		Map<T, U> newValues = mapSupplier.get();
		synchronized (map) {
			map.clear();
			map.putAll(newValues);
		}
	}

	default <T, U> void safeMapUpdate(Map<T, U> newValues, Map<T, U> map) {
		synchronized (map) {
			map.clear();
			map.putAll(newValues);
		}
	}
	//////////////

	public static class MethodClassSleeper {
		@Builder(builderMethodName = "configure", buildMethodName = "sleepNow")
		private MethodClassSleeper(int hours, int minutes, int seconds, int miliseconds) {
			super();
			ToolBoxInstance.get().sleep(60 * 60 * 1000 * hours//
					+ 60 * 1000 * minutes//
					+ 1000 * seconds//
					+ miliseconds);
		}
	}

	default MethodClassSleeper.MethodClassSleeperBuilder sleeper() {
		return MethodClassSleeper.configure();
	}

	default void sleep(long miliseconds) {
		try {
			Thread.sleep(miliseconds);
		} catch (InterruptedException e) {
			throw new RuntimeException("Sleeping failed", e);
		}
	}
	/////////////////////////

	default ScheduledExecutorService createScheduledExecutorService(Class<?> aClass, int coreThreadCount) {
		return //
		Executors.newScheduledThreadPool(coreThreadCount,
				getNamedThreadFactory(getClass(), aClass.getSimpleName() + "ScheduledExecutorService"));
	}

	default void timeIt(String runnableName, Runnable runnable) {
		Instant start = Instant.now();
		System.out.println("Starting RUNNABLE_NAME".replace("RUNNABLE_NAME", runnableName));
		runnable.run();
		Instant finish = Instant.now();
		Duration duration = Duration.between(start, finish);
		System.out.println("RUNNABLE_NAME execution time: DURATION"//
				.replace("RUNNABLE_NAME", runnableName)//
				.replace("DURATION", duration.toString()));
		long timeElapsed = duration.toMillis();
		System.out.println("RUNNABLE_NAME execution time: TIME_ELAPSED milliseconds"//
				.replace("RUNNABLE_NAME", runnableName)//
				.replace("TIME_ELAPSED", String.valueOf(timeElapsed)));
	}

	default void waitForIt(String objectName, Supplier<Boolean> isInitialized) {
		Instant start = Instant.now();
		System.out.println("Initializing OBJECT_NAME instance".replace("OBJECT_NAME", objectName));
		while (isInitialized.get() == false) {
			ToolBoxInstance.get().sleeper().seconds(5).sleepNow();
			System.out.print(".");
		}
		System.out.println("");
		Instant finish = Instant.now();
		Duration duration = Duration.between(start, finish);
		System.out.println("Initialization of OBJECT_NAME instance execution time: DURATION"//
				.replace("OBJECT_NAME", objectName)//
				.replace("DURATION", duration.toString()));
		long timeElapsed = duration.toMillis();
		System.out.println("Initialization of OBJECT_NAME instance execution time: TIME_ELAPSED milliseconds"//
				.replace("OBJECT_NAME", objectName)//
				.replace("TIME_ELAPSED", String.valueOf(timeElapsed)));
	}
	////////////////////////////////////////////////////////////////////////////////////

	public static InheritableThreadLocal<LogitLevel> _logitLevel = new InheritableThreadLocal<>();

	default void setLogitLevel(LogitLevel logitLevelValue) {
		_logitLevel.set(logitLevelValue);
	}

	default LogitLevel getLogitLevel() {
		LogitLevel logitLevelThreadSpecific = _logitLevel.get();
		if (logitLevelThreadSpecific == null) {
			return LogitLevel.Error;
		} else {
			return logitLevelThreadSpecific;
		}
	}

	default void logitError(String message) {
		switch (getLogitLevel()) {
		case Error:
			System.out.println(message);
			break;
		default:
			// Do nothing
			break;
		}
	}

	default void logitInfo(String message) {
		switch (getLogitLevel()) {
		case Info:
		case Debug:
			System.out.println(message);
			break;
		default:
			// Do nothing
			break;
		}
	}

	default void logitDebug(String message) {
		switch (getLogitLevel()) {
		case Info:
		case Error:
		case Debug:
			System.out.println(message);
			break;
		default:
			// Do nothing
			break;
		}
	}
	///////////
	// TODO add support for MacOS

	default boolean isLocalDevelopmentEnvironment() {
		return new File("c:/").exists();
	}

	/**
	 * 
	 * Explicit argument indices may be used to re-order output. format("%4$2s %3$2s
	 * 
	 * %2$2s %1$2s", "a", "b", "c", "d") returns " d c b a"
	 *
	 * 
	 * 
	 * @param templateString
	 * 
	 * @param objects
	 * 
	 * @return
	 * 
	 */
	default String format(String templateString, Object... objects) {
		StringBuilder sb = new StringBuilder();
		try (Formatter formatter = new Formatter(sb, Locale.US)) {
			return formatter.format(templateString, objects).toString();
		}
	}
	///////////////////

	default ElapsedTime startElapsedTime(String startMessage) {
		ElapsedTime elapsedTime = new ElapsedTime();
		elapsedTime.printTimeSoFar(startMessage);
		return elapsedTime;
	}

	default ElapsedTime startElapsedTime(String startMessage, PrintStream printStream) {
		ElapsedTime elapsedTime = new ElapsedTime(printStream);
		elapsedTime.printTimeSoFar(startMessage);
		return elapsedTime;
	}

	default void elapsedTimePrintln(long startTime, String message) {
		System.out.println(
				"Elapsed Time: " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds - " + message);
	}

	default void elapsedTimePrintlnInFloat(long startTime, String message) {
		System.out.println(
				"Elapsed Time: " + ((System.currentTimeMillis() - startTime) / 1000.0) + " seconds - " + message);
	}

	public static class ElapsedTime {
		public final long startTime;
		private int counter;
		private final PrintStream printStream;

		public ElapsedTime() {
			super();
			this.printStream = System.out;
			this.startTime = System.currentTimeMillis();
		}

		public ElapsedTime(PrintStream printStream) {
			super();
			this.printStream = printStream;
			this.startTime = System.currentTimeMillis();
		}

		public void printTimeSoFar(String message) {
			elapsedTimePrintln(startTime, String.valueOf(counter++) + ":" + message);
		}

		public void printTimeSoFarInFloat(String message) {
			elapsedTimePrintlnInFloat(startTime, String.valueOf(counter++) + ":" + message);
		}

		private void elapsedTimePrintln(long startTime, String message) {
			printStream.println(
					"Elapsed Time: " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds - " + message);
		}

		private void elapsedTimePrintlnInFloat(long startTime, String message) {
			printStream.println(
					"Elapsed Time: " + ((System.currentTimeMillis() - startTime) / 1000.0) + " seconds - " + message);
		}
	}

	default File getValidatedFile(String filePath, String typeOfFile) {
		File file = new File(filePath);
		try {
			file = file.getCanonicalFile().getAbsoluteFile();
		} catch (IOException e) {
			System.err.println("Aborting: Issues with " + typeOfFile + " file, \"" + file.getAbsolutePath());
			e.printStackTrace();
			System.exit(10);
		}
		if (file.exists() == false) {
			System.err.println("Aborting: " + typeOfFile + " file does not exist, \"" + file.getAbsolutePath());
			System.exit(11);
		}
		if (file.isFile() == false) {
			System.err.println("Aborting: " + typeOfFile + " file is not a file, \"" + file.getAbsolutePath());
			System.exit(12);
		}
		if (file.canRead() == false) {
			System.err.println("Aborting: " + typeOfFile + " file is not readable, \"" + file.getAbsolutePath());
			System.exit(13);
		}
		return file;
	}
	////////////

	static final int TEMP_DIR_ATTEMPTS = 10000;

	default File createUniqueFile(String prefix, String suffix, File directory) {
		String baseName = "-" + System.currentTimeMillis();
		for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
			File file = new File(directory, prefix + baseName + getFileDateSuffix() + suffix);
			System.out.println("Trying: " + file.getAbsolutePath());
			if (file.exists() == true) {
				continue;
			}
			try {
				if (file.createNewFile() == false) {
					continue;
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			if (file.canWrite() == false) {
				continue;
			}
			return file;
		}
		throw new IllegalStateException("Failed to create file within " + TEMP_DIR_ATTEMPTS);
	}

	default File createTempDirectory() {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		String baseName = System.currentTimeMillis() + "-";
		for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
			File tempDir = new File(baseDir, baseName + counter);
			if (tempDir.mkdir()) {
				return tempDir;
			}
		}
		throw new IllegalStateException("Failed to create directory within " + TEMP_DIR_ATTEMPTS + " attempts (tried "
				+ baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
	}
	///////////////////////////////////////////////////////////////////////////////////////////

	default void println() {
		System.out.println();
	}

	default void println(Object object) {
		System.out.println(object);
	}

	public static class ToolBoxInstance implements ToolBox {
		private static ToolBox _toolBox;

		public static ToolBox get() {
			if (_toolBox == null) {
				_toolBox = new ToolBox() {
				};
			}
			return _toolBox;
		}
	}

	static ToolBox get() {
		return ToolBoxInstance.get();
	}
}
