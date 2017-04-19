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
package com.cyberfront.crdt.unittest.support;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thedeanda.lorem.LoremIpsum;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating Word objects.
 */
public class WordFactory {
	
	/** The rnd. */
	private static Random rnd = new Random();

	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger(WordFactory.class);

	/**
	 * The Enum WordTypes.
	 */
	public enum WordTypes {
		
		/** The adjective. */
		ADJECTIVE,
		
		/** The adverb. */
		ADVERB,
		
		/** The noun. */
		NOUN,
		
		/** The verb. */
		VERB
	}
	
	/** The Constant LONG_SEQUENCE. */
	protected static final WordTypes[] LONG_SEQUENCE = {
			WordTypes.ADJECTIVE,
			WordTypes.NOUN,
			WordTypes.VERB,
			WordTypes.ADVERB
	};
	
	/** The Constant SHORT_SEQUENCE. */
	protected static final WordTypes[] SHORT_SEQUENCE = {
			WordTypes.NOUN,
			WordTypes.VERB,
	};

	/** The Constant NOUNS. */
	private static final List<String> NOUNS = readLines("src/main/resources/nouns.txt");

	/** The Constant VERBS. */
	private static final List<String> VERBS = readLines("src/main/resources/verbs.txt");
	
	/** The Constant ADVERBS. */
	private static final List<String> ADVERBS = readLines("src/main/resources/adverbs.txt");
	
	/** The Constant ADJECTIVES. */
	private static final List<String> ADJECTIVES = readLines("src/main/resources/adjectives.txt");

	/**
	 * Read lines.
	 *
	 * @param filename the filename
	 * @return the list
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
	 * Gets the word.
	 *
	 * @param list the list
	 * @return the word
	 */
	public static String getWord(List<String> list) {
		return list.get(getRandom().nextInt(list.size()));
	}

	/**
	 * Gets the word.
	 *
	 * @param type the type
	 * @return the word
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
	 * Gets the noun.
	 *
	 * @return the noun
	 */
	public static String getNoun() {
		return getWord(NOUNS);
	}
	
	/**
	 * Gets the verb.
	 *
	 * @return the verb
	 */
	public static String getVerb() {
		return getWord(VERBS);
	}
	
	/**
	 * Gets the adverb.
	 *
	 * @return the adverb
	 */
	public static String getAdverb() {
		return getWord(ADVERBS);
	}
	
	/**
	 * Gets the adjective.
	 *
	 * @return the adjective
	 */
	public static String getAdjective() {
		return getWord(ADJECTIVES);
	}

	/**
	 * Gets the sequence.
	 *
	 * @param types the types
	 * @param delimiter the delimiter
	 * @return the sequence
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
	 * Gets the long sequence.
	 *
	 * @param delimiter the delimiter
	 * @return the long sequence
	 */
	public static String getLongSequence(char delimiter) {
		return getSequence(WordFactory.LONG_SEQUENCE, delimiter);
	}
	
	/**
	 * Gets the short sequence.
	 *
	 * @param delimiter the delimiter
	 * @return the short sequence
	 */
	public static String getShortSequence(char delimiter) {
		return getSequence(WordFactory.SHORT_SEQUENCE, delimiter);
	}
	
	/**
	 * Gets the sequence.
	 *
	 * @param types the types
	 * @return the sequence
	 */
	public static String getSequence(WordTypes[] types) {
		return getSequence(types,' ');
	}
	
	/**
	 * Gets the long sequence.
	 *
	 * @return the long sequence
	 */
	public static String getLongSequence() {
		return getSequence(WordFactory.LONG_SEQUENCE);
	}
	
	/**
	 * Gets the short sequence.
	 *
	 * @return the short sequence
	 */
	public static String getShortSequence() {
		return getSequence(WordFactory.SHORT_SEQUENCE);
	}
	
	/**
	 * Gets the full male name.
	 *
	 * @return the full male name
	 */
	public static String getFullMaleName() {
		return LoremIpsum.getInstance().getLastName() + ", " + LoremIpsum.getInstance().getFirstNameMale() + " " + LoremIpsum.getInstance().getFirstNameMale();
	}
	
	/**
	 * Gets the full female name.
	 *
	 * @return the full female name
	 */
	public static String getFullFemaleName() {
		return LoremIpsum.getInstance().getLastName() + ", " + LoremIpsum.getInstance().getFirstNameFemale() + " " + LoremIpsum.getInstance().getFirstNameFemale();
	}
	
	/**
	 * Gets the full name.
	 *
	 * @return the full name
	 */
	public static String getFullName() {
		return getRandom().nextBoolean() ? getFullMaleName() : getFullFemaleName();
	}

	/**
	 * Gets the random.
	 *
	 * @return the random
	 */
	public static Random getRandom() {
		return rnd;
	}
	
	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public static String getUsername() {
		return LoremIpsum.getInstance().getFirstName() + "." + LoremIpsum.getInstance().getLastName();
	}
}
