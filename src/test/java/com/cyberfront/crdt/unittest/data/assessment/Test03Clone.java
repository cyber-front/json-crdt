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
package com.cyberfront.crdt.unittest.data.assessment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.cyberfront.crdt.unittest.data.AbstractDataType;
import com.cyberfront.crdt.unittest.data.Factory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

/**
 * This contains a class used for performing unit tests designed to create and clone a number of 
 * AbstractDataType instances using the factory.  It is successful if it creates and updates these
 * objects, neither of which result in null.
 */
public class Test03Clone {
	
	/** The Constant COUNT. */
	private static final long COUNT=100;
	
	/** The Constant INSTANCE_COUNT. */
	private static final long INSTANCE_COUNT = 100;

	/** The logger. */
	private Logger logger = LogManager.getLogger(Test03Clone.class.getName());

	/** The mapper. */
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Builds the instances.
	 *
	 * @param count the count
	 * @return the array list
	 */
	private static ArrayList<AbstractDataType> buildInstances(long count) {
		ArrayList<AbstractDataType> rv = new ArrayList<>();
		
		for (int i=0; i<count; ++i) {
			rv.add(Factory.getInstance());
		}
		
		return rv;
	}
	
	/**
	 * Clone instances.
	 *
	 * @param source the source
	 * @return the array list
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	private static ArrayList<AbstractDataType> cloneInstances(ArrayList<AbstractDataType> source) throws CloneNotSupportedException {
		ArrayList<AbstractDataType> rv = new ArrayList<>();
		
		for (AbstractDataType element : source) {
			rv.add(Factory.copy(element));
		}
		
		return rv;
	}
	
	/**
	 * Clone data test.
	 *
	 * @param count the count
	 * @param instances the instances
	 */
	private void cloneDataTest(long count, long instances) {
		logger.info("\n** Test03Clone: {\"count\":" + count + ",\"instances\":" + instances + "}");
		try {
			for (int i=0; i<count; ++ i) {
				ArrayList<AbstractDataType> corpus = buildInstances(instances);
				ArrayList<AbstractDataType> clones = cloneInstances(corpus);
				int originalCount = 0;
				for (AbstractDataType el0 : corpus) {
					int cloneCount = 0;
					for (AbstractDataType el1 : clones) {
						boolean expected = originalCount == cloneCount;
						boolean actual = el0.equals(el1);
						if (expected != actual) {
							logger.error("{\"iteration\":" + i + ",\"expected\":" + expected + ",\"actual\":" + actual + ",\"originalCount\":" + originalCount + ",\"cloneCount\":" + cloneCount + "}");
							logger.error("{\"el0\":" + el0.toString());
							logger.error("{\"el1\":" + el1.toString());
						}
						assertEquals(expected, actual);
						JsonNode diff = JsonDiff.asJson(mapper.valueToTree(el0), mapper.valueToTree(el1));
						if (actual && diff.size() > 0) {
							logger.error("unexpected difference: " + diff);
						}
						assertTrue("Unexpected Difference:", !actual || diff.size()==0);
						++cloneCount;
					}
					++originalCount;
				}
			}
		} catch (CloneNotSupportedException e) {
			logger.error(e);
			fail("Failed to clone something.");
		}
		logger.info("   SUCCESS");
	}
	
	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void cloneDataTest() {
		this.cloneDataTest(COUNT, INSTANCE_COUNT);
	}
}
