package com.dieharddev.toolbox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;

public interface UtilsCommons extends Conversions {
	default String unquote(String string) {
		String resultString = string;
		if ((resultString.startsWith("\"") == true) || (resultString.startsWith("'") == true)) {
			resultString = resultString.substring(1);
		}
		if ((resultString.endsWith("\"") == true) || (resultString.endsWith("'") == true)) {
			resultString = resultString.substring(0, resultString.length() - 1);
		}
		return resultString;
	}

	default boolean isEmpty(String value) {
		return !isNonEmpty(value);
	}

	default boolean isNonEmpty(String value) {
		return ((value != null) && (value.trim().isEmpty() == false));
	}

	default boolean isNonEmpty(String... values) {
		boolean result = true;
		for (String string : values) {
			result = result && isNonEmpty(string);
		}
		return result;
	}

	default String defaultOnEmpty(String string, String defaultString) {
		if (isNonEmpty(string) == true) {
			return string;
		} else {
			return defaultString;
		}
	}

	default List<String> defaultOnEmpty(List<String> strings, List<String> defaultStrings) {
		if ((strings == null) || (strings.isEmpty() == true)) {
			return defaultStrings;
		} else {
			return strings;
		}
	}

	default String parentheses(String value) {
		return "(" + value + ")";
	}

	default String quoted(String value) {
		return "\"" + value + "\"";
	}

	default List<String> quoted(String[] values) {
		return quoted(toList(values));
	}

	default List<String> quoted(List<String> values) {
		return values.stream().map(new Function<String, String>() {
			@Override
			public String apply(String string) {
				return quoted(string);
			}
		}).collect(Collectors.toList());
	}

	default Set<String> quoted(Set<String> values) {
		return values.stream().map(new Function<String, String>() {
			@Override
			public String apply(String string) {
				return quoted(string);
			}
		}).collect(Collectors.toSet());
	}

	default Set<String> quoted(Collection<String> values) {
		return values.stream().map(new Function<String, String>() {
			@Override
			public String apply(String string) {
				return quoted(string);
			}
		}).collect(Collectors.toSet());
	}

	default String singleQuoted(String value) {
		return "'" + value + "'";
	}
//            default <T> List<T> toList(Set<T> set) {
//                            return new ArrayList<T>(set);
//            }
//
//            default <T> List<T> toList(T... objects) {
//                            return new ArrayList<T>(Arrays.asList(objects));
//            }
//
//            default <T> Set<T> toSet(T... objects) {
//                            return new HashSet<T>(Arrays.asList(objects));
//            }

	default String capitalizeFully(String string) {
		return WordUtils.capitalize(string, ' ');
	}

	default String capitalize(String string) {
		return StringUtils.capitalize(string);
	}

	default <T> String join(T[] array, final String separator, String finalSeparator) {
		if (array.length == 0) {
			return "";
		}
		return join(toList(array), separator, finalSeparator);
	}

	default String join(final Iterable<?> iterable, final String separator, String finalSeparator) {
		if (iterable == null) {
			return null;
		}
		return join(iterable.iterator(), separator, finalSeparator);
	}

	public static final String EMPTY = "";

	/**
	 * 
	 * <p>
	 * 
	 * Joins the elements of the provided {@code Iterable} into a single String
	 * 
	 * containing the provided elements.
	 * 
	 * </p>
	 *
	 * 
	 * 
	 * <p>
	 * 
	 * No delimiter is added before or after the list. A {@code null} separator is
	 * 
	 * the same as an empty String ("").
	 * 
	 * </p>
	 *
	 * 
	 * 
	 * <p>
	 * 
	 * See the examples here: {@link #join(Object[],String)}.
	 * 
	 * </p>
	 *
	 * 
	 * 
	 * @param iterable  the {@code Iterable} providing the values to join together,
	 * 
	 *                  may be null
	 * 
	 * @param separator the separator character to use, null treated as ""
	 * 
	 * @return the joined String, {@code null} if null iterator input
	 * 
	 * @since 2.3
	 * 
	 */
	default String join(final Iterable<?> iterable, final String separator) {
		if (iterable == null) {
			return null;
		}
		return join(iterable.iterator(), separator, null);
	}

	/**
	 * 
	 * <p>
	 * 
	 * Joins the elements of the provided {@code Iterator} into a single String
	 * 
	 * containing the provided elements.
	 * 
	 * </p>
	 *
	 * 
	 * 
	 * <p>
	 * 
	 * No delimiter is added before or after the list. A {@code null} separator is
	 * 
	 * the same as an empty String ("").
	 * 
	 * </p>
	 *
	 * 
	 * 
	 * <p>
	 * 
	 * See the examples here: {@link #join(Object[],String)}.
	 * 
	 * </p>
	 *
	 * 
	 * 
	 * @param iterator  the {@code Iterator} of values to join together, may be null
	 * 
	 * @param separator the separator character to use, null treated as ""
	 * 
	 * @return the joined String, {@code null} if null iterator input
	 * 
	 */
	default String join(final Iterator<?> iterator, final String separator, String finalSeparator) {
		// handle null, zero and one elements before building a buffer
		if (iterator == null) {
			return null;
		}
		if (!iterator.hasNext()) {
			return EMPTY;
		}
		final Object first = iterator.next();
		if (!iterator.hasNext()) {
			@SuppressWarnings("deprecation")
			// ObjectUtils.toString(Object) has been deprecated in 3.2
			final String result = toString(first);
			return result;
		}
		// two or more elements
		final StringBuilder buf = new StringBuilder(256); // Java default is 16,
		// probably too
		// small
		if (first != null) {
			buf.append(first);
		}
		if (finalSeparator == null) {
			while (iterator.hasNext()) {
				if (separator != null) {
					buf.append(separator);
				}
				final Object obj = iterator.next();
				if (obj != null) {
					buf.append(obj);
				}
			}
		} else {
			while (iterator.hasNext()) {
				final Object obj = iterator.next();
				if (iterator.hasNext()) {
					if (separator != null) {
						buf.append(separator);
					}
				} else {
					buf.append(finalSeparator);
				}
				if (obj != null) {
					buf.append(obj);
				}
			}
		}
		return buf.toString();
	}

	default String toString(Throwable aThrowable) {
		Writer result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	default String toString(final Object obj) {
		return obj == null ? "" : obj.toString();
	}
	/////////////////////////////////////////////////////////////////////////
	// Date Strings

	default String getPrettyDateString() {
		return getPrettyDateString(new Date());
	}

	default String getPrettyDateString(Date date) {
		return new SimpleDateFormat("MMM/dd/yyy HH:mm:ss").format(date);
	}

	default String fileTimeStampString() {
		return new DateTime().toString("yyyy.MM.dd-HH~mm~ssa-SSS");
	}

	default String getDateString() {
		return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
	}

	default String getFileDateSuffix() {
		return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
	}

	public final static SimpleDateFormat parsableSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");

	default String getParsableDateString(Date date) {
		return parsableSimpleDateFormat.format(date);
	}

	default Date parsableDateString(String dateString) {
		try {
			return parsableSimpleDateFormat.parse(dateString);
		} catch (ParseException e) {
			throw new RuntimeException("Failed to ParsableDateString, " + quoted(dateString), e);
		}
	}

	default String removeOuterSingleQuotes(String value) {
		if ((value.startsWith("'") == true) && (value.endsWith("'") == true)) {
			return value.substring(1, value.length() - 1);
		} else {
			return value;
		}
	}
}
