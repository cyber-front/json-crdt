/*
 * Copyright (c) 2017 Cybernetic Frontiers LLC
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
package com.cyberfront.crdt.unittest.data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thedeanda.lorem.LoremIpsum;

/**
 * This is a helper class for generating a number of random values useful for testing the CRDTs
 */
public class WordFactory {
	
	/** A random number generator used to choose values for testing purposes */
	private static Random rnd = new Random();

	/** Logger to use when displaying state information */
	private static final Logger logger = LogManager.getLogger(WordFactory.class);

	/**
	 * An enumeration used to describe different parts of speech
	 */
	public enum WordTypes {
		/** An enumeration value for adjectives */
		ADJECTIVE,
		
		/** An enumeration value for adverbs */
		ADVERB,
		
		/** An enumeration value for nouns */
		NOUN,
		
		/** An enumeration value for verbs */
		VERB
	}
	
	/** A pattern for generating a sequence of words four words*/
	protected static final WordTypes[] LONG_SEQUENCE = {
			WordTypes.ADJECTIVE,
			WordTypes.NOUN,
			WordTypes.VERB,
			WordTypes.ADVERB
	};
	
	/** A pattern for generating a sequence of words two words*/
	protected static final WordTypes[] SHORT_SEQUENCE = {
			WordTypes.NOUN,
			WordTypes.VERB,
	};

	/** List of nouns read from the nouns.txt file */
	private static final List<String> NOUNS = readLines("src/main/resources/nouns.txt");

	/** List of verbs read from the verbs.txt file */
	private static final List<String> VERBS = readLines("src/main/resources/verbs.txt");
	
	/** List of adverbs read from the adverbs.txt file */
	private static final List<String> ADVERBS = readLines("src/main/resources/adverbs.txt");
	
	/** List of adjectives read from the adjectives.txt file */
	private static final List<String> ADJECTIVES = readLines("src/main/resources/adjectives.txt");

	/**
	 * Read the contents of the given file and create a string list containing the elements of the 
	 * file.
	 *
	 * @param filename The name of the file from which to read the elements to populate the returned string list
	 * @return The string list containing all of the lines in the file
	 */
	private static List<String> readLines(String filename) {
		List<String> list = null;
		try {
			list = Files.readAllLines(new File(filename).toPath(), Charset.defaultCharset() );
		} catch (IOException e) {
			logger.error(e);
			logger.error(e.getStackTrace());
		}
		return list;
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
	 * @param type The word type to return
	 * @return The word of the given type which was randomly selected
	 */
	public static String getWord(WordTypes type) {
		switch (type) {
		case NOUN:
			return getNoun();
		case VERB:
			return getVerb();
		case ADVERB:
			return getAdverb();
		case ADJECTIVE:
			return getAdjective();
		default:
			return null;
		}
	}

	/**
	 * Randomly select a noun from the list of nouns 
	 *
	 * @return A randomly selected noun
	 */
	public static String getNoun() {
		return getWord(NOUNS);
	}
	
	/**
	 * Randomly select a verb from the list of verbs 
	 *
	 * @return A randomly selected verb
	 */
	public static String getVerb() {
		return getWord(VERBS);
	}
	
	/**
	 * Randomly select a adverb from the list of adverbs 
	 *
	 * @return A randomly selected adverb
	 */
	public static String getAdverb() {
		return getWord(ADVERBS);
	}
	
	/**
	 * Randomly select a adjective from the list of adjectives 
	 *
	 * @return A randomly selected adjective
	 */
	public static String getAdjective() {
		return getWord(ADJECTIVES);
	}

	/**
	 * Generate and return a sequence of words of the given type separated by a delimiter
	 *
	 * @param types List of word types to to select
	 * @param delimiter The delimiter used to separate the selected words
	 * @return The sequence of words selected
	 */
	public static String getSequence(WordTypes[] types, char delimiter) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		
		for (WordTypes type : types) {
			
			if (!first) {
				sb.append(delimiter);
			}
			sb.append(getWord(type));

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
		return getSequence(WordFactory.LONG_SEQUENCE, delimiter);
	}
	
	/**
	 * Generate and return a sequence of words chosen with WordFactory.SHORT_SEQUENCE with each word separated by
	 * the given delimiter.
	 *
	 * @param delimiter The delimiter to separate each word from the next
	 * @return The sequence of words generated using WordFactory.SHORT_SEQUENCE as the pattern 
	 */
	public static String getShortSequence(char delimiter) {
		return getSequence(WordFactory.SHORT_SEQUENCE, delimiter);
	}
	
	/**
	 * Generate and return a sequence of words of the given type separated by a space ' ' as the delimiter
	 *
	 * @param types List of word types to to select
	 * @return The sequence of words selected
	 */
	public static String getSequence(WordTypes[] types) {
		return getSequence(types,' ');
	}
	
	/**
	 * Generate and return a sequence of words chosen with WordFactory.LONG_SEQUENCE with each word separated by
	 * a space ' ' as the delimiter.
	 *
	 * @return The sequence of words generated using WordFactory.LONG_SEQUENCE as the pattern 
	 */
	public static String getLongSequence() {
		return getSequence(WordFactory.LONG_SEQUENCE);
	}
	
	/**
	 * Generate and return a sequence of words chosen with WordFactory.SHORT_SEQUENCE with each word separated by
	 * a space ' ' as the delimiter.
	 *
	 * @return The sequence of words generated using WordFactory.SHORT_SEQUENCE as the pattern 
	 */
	public static String getShortSequence() {
		return getSequence(WordFactory.SHORT_SEQUENCE);
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
	 * Generate a string as a JSON formated array based on elements of a collection
	 * 
	 * @param collection Collection of items to form into JSON formated string
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
	 * @return The JSON formated string representation of the given collection
	 */
	public static <KEY, VALUE> String convert(Map<KEY, VALUE> map) {
		if (null == map) {
			return "null";
		} else if (map.isEmpty()) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder();
		String delimiter = "[";

		for (Map.Entry<KEY, VALUE> entry : map.entrySet()) {
			sb.append("{\"KEY\":\"" + entry.getKey().toString() + "\",\"VALUE\":" + entry.getValue() + "}" + delimiter);
			delimiter = ",";
		}
		
		sb.append("]");

		return sb.toString();
	}

	public static void displayStackTrace(StackTraceElement[] stackTrace) {
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
		    logger.info(ste);
		}
	}
}
