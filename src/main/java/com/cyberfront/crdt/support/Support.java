/*
 * Copyright (c) 2018 Cybernetic Frontiers LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */
package com.cyberfront.crdt.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.thedeanda.lorem.LoremIpsum;

/**
 * This is a helper class for generating a number of random values useful for testing the CRDTs
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class Support {
	
	/** A random number generator used to choose values for testing purposes */
	private static Random rnd = new Random();
	
	/** Number of unique random words from which to draw for phrases */ 
	private static final int WORD_COUNT = 65536;

	/** Minimum word length */
	private static final int MIN_WORD_LEN = 5;

	/** Maximum word length */
	private static final int MAX_WORD_LEN = 25;
	
	/** Logger to use when displaying state information */
	private static final Logger logger = LogManager.getLogger(Support.class);
	
	/** List of random words to use in generating random sequences */
	private static final List<String> WORDS = genUniqueWords(WORD_COUNT);
	
	/** Length of a long sequence of random words from the word list. */
	protected static final int LONG_SEQUENCE_LENGTH = 4;
	
	/** Length of a short sequence of random words from the word list. */
	protected static final int SHORT_SEQUENCE_LENGTH = 2;

	private static final int SHORT_SEQUNCE_LENGTH = 0;

	public static List<String> genUniqueWords(int wordCount) {
		Set<String> wordSet = new TreeSet<>();
		
		while (wordSet.size() < wordCount) {
			wordSet.add(RandomStringUtils.randomAlphabetic(MIN_WORD_LEN, MAX_WORD_LEN+1));
		}

		return new ArrayList<String>(wordSet); 
	}
	
	/**
	 * Randomly select an element from the string list passed to it
	 *
	 * @param list The list from which to select a random element
	 * @return The string which was randomly selected
	 */
	public static String getWord(List<String> list) {
		return list.get(getRandom().nextInt(list.size()));
	}

	/**
	 * Get a word from the available collections of words
	 *
	 * @return The word randomly selected from the list of words
	 */
	public static String getWord() {
		return getWord(WORDS);
	}

	/**
	 * Generate and return a sequence of words of the given type separated by a delimiter
	 *
	 * @param count Number of words to extract from those available to string together as a sequence of words
	 * @param delimiter The delimiter used to separate the selected words
	 * @return The sequence of words selected
	 */
	public static String getSequence(int count, char delimiter) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		
		for (int i=0; i<count; ++i) {
			
			if (!first) {
				sb.append(delimiter);
			}
			sb.append(getWord());

			first = false;
		}
		
		return sb.toString();
	}

	/**
	 * Generate and return a sequence of words chosen with WordFactory.LONG_SEQUENCE with each word separated by
	 * the given delimiter.
	 *
	 * @param delimiter The delimiter to separate each word from the next
	 * @return The sequence of words generated using WordFactory.LONG_SEQUENCE as the pattern 
	 */
	public static String getLongSequence(char delimiter) {
		return getSequence(LONG_SEQUENCE_LENGTH, delimiter);
	}
	
	/**
	 * Generate and return a sequence of words chosen with WordFactory.SHORT_SEQUENCE with each word separated by
	 * the given delimiter.
	 *
	 * @param delimiter The delimiter to separate each word from the next
	 * @return The sequence of words generated using WordFactory.SHORT_SEQUENCE as the pattern 
	 */
	public static String getShortSequence(char delimiter) {
		return getSequence(SHORT_SEQUNCE_LENGTH, delimiter);
	}
	
	/**
	 * Generate and return a sequence of words of the given type separated by a space ' ' as the delimiter
	 *
	 * @param count Number of words to include in the sequence of words
	 * @return The sequence of words selected
	 */
	public static String getSequence(int count) {
		return getSequence(count,' ');
	}
	
	/**
	 * Generate and return a sequence of words chosen with WordFactory.LONG_SEQUENCE with each word separated by
	 * a space ' ' as the delimiter.
	 *
	 * @return The sequence of words generated using WordFactory.LONG_SEQUENCE as the pattern 
	 */
	public static String getLongSequence() {
		return getSequence(Support.LONG_SEQUENCE_LENGTH);
	}
	
	/**
	 * Generate and return a sequence of words chosen with WordFactory.SHORT_SEQUENCE with each word separated by
	 * a space ' ' as the delimiter.
	 *
	 * @return The sequence of words generated using WordFactory.SHORT_SEQUENCE as the pattern 
	 */
	public static String getShortSequence() {
		return getSequence(Support.SHORT_SEQUENCE_LENGTH);
	}
	
	/**
	 * Get a string comprised of a sequence of word chosen from the provided list and separated by the given delimiter
	 * @param count Number of words to pick from the list
	 * @param words Words form which to choose the random values
	 * @param delimiter Delimiter to separate consecutive words
	 * @return The resulting string of words separated by the delimiter
	 */
	public static String getSequence(long count, List<String> words, char delimiter) {
		StringBuilder sb = new StringBuilder();
		
		for (long i = 0; i < count; ++i) {
			if (i>0) {
				sb.append(delimiter);
			}
			
			sb.append(getWord(words));
		}
		
		return sb.toString();
	}
	
	/**
	 * Generate and return a male first, middle and last name
	 *
	 * @return The generated male name
	 */
	public static String getFullMaleName() {
		return LoremIpsum.getInstance().getLastName() + ", " + LoremIpsum.getInstance().getFirstNameMale() + " " + LoremIpsum.getInstance().getFirstNameMale();
	}
	
	/**
	 * Generate and return a female first, middle and last name
	 *
	 * @return The generated female name
	 */
	public static String getFullFemaleName() {
		return LoremIpsum.getInstance().getLastName() + ", " + LoremIpsum.getInstance().getFirstNameFemale() + " " + LoremIpsum.getInstance().getFirstNameFemale();
	}
	
	/**
	 * Generate and return a first, middle and last name; the gender is randomly selected
	 *
	 * @return The generated name
	 */
	public static String getFullName() {
		return getRandom().nextBoolean() ? getFullMaleName() : getFullFemaleName();
	}

	/**
	 * Retrieve the random number generator
	 *
	 * @return The random number generator
	 */
	public static Random getRandom() {
		return rnd;
	}
	
	/**
	 * Generate and return a user name comprised of a first and last name, separated with a period '.'
	 *
	 * @return The randomly generated username
	 */
	public static String getUsername() {
		return LoremIpsum.getInstance().getFirstName() + "." + LoremIpsum.getInstance().getLastName();
	}
	
	/**
	 * Display the stack trace provided
	 * @param stackTrace to display
	 */
	public static void displayStackTrace(StackTraceElement[] stackTrace) {
		for (StackTraceElement ste : stackTrace) {
		    logger.info(ste);
		}
	}

	/**
	 * Generate a string as a JSON formated array based on elements of a collection
	 * 
	 * @param collection Collection of items to form into JSON formated string
	 * @param <T> Type of elements in the collection being converted into a JSON string
	 * @return The JSON formated string representation of the given collection
	 */
	public static <T> String convert(Collection<T> collection) {
		if (null == collection) {
			return "null";
		} else if (collection.isEmpty()) {
			return "[]";
		}
	
		StringBuilder sb = new StringBuilder();
		String delimiter = "[";
	
		for (T element : collection) {
			sb.append(delimiter + element.toString());
			delimiter = ",";
		}
		
		sb.append("]");
	
		return sb.toString();
	}

	/**
	 * Generate a string as a JSON formated array based on elements of a collection
	 * 
	 * @param map Collection of items to form into JSON formated string
	 * @param <K> Type of the key in the Map type
	 * @param <V> Type of the value in the associative memory map
	 * @return The JSON formated string representation of the given collection
	 */
	public static <K, V> String convert(Map<K, V> map) {
		if (null == map) {
			return "null";
		} else if (map.isEmpty()) {
			return "[]";
		}
	
		StringBuilder sb = new StringBuilder();
		String delimiter = "[";
	
		for (Map.Entry<K, V> entry : map.entrySet()) {
			sb.append(delimiter + "{\"KEY\":\"" + entry.getKey().toString() + "\",\"VALUE\":" + entry.getValue().toString() + "}");
			delimiter = ",";
		}
		
		sb.append("]");
	
		return sb.toString();
	}

	/**
	 * Validate the given string can be interpreted as an integer.  Return true exactly when this is matched
	 * @param str String to parse and determine if the value is an integer
	 * @return True exactly when the string contains a representation of an integer
	 */
	public static boolean isInteger(String str) {
		return null != str && str.matches("[-+]?[0-9]+");
	}
	
	/**
	 * Validate the given string can be interpreted as a numeric value.  Return true exactly when this is matched
	 * @param str String to parse and determine if the value is a numeric value
	 * @return True exactly when the string contains a representation of a numeric value
	 */
	public static boolean isNumeric(String str) {
		return null != str && str.matches("[-+]?[0-9]+\\.?[0-9]+([eE][-+]?[0-9]+)?");
	}

	/**
	 * Validate the given string can be interpreted as a boolean value.  Return true exactly when this is matched
	 * @param str String to parse and determine if the value is a boolean value
	 * @return True exactly when the string contains a representation of a boolean value
	 */
	public static boolean isBoolean(String str) {
		return null != str && ("true".equals(str) || "false".equals(str));
	}
}
