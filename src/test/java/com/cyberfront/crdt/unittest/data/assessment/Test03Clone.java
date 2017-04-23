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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

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
	
	/** Constant defining the number of AbstractDataType elements to create and clone test in the unit test */
	private static final long TRIAL_COUNT=100;
	
	/** The Constant INSTANCE_COUNT. */
	private static final long INSTANCE_COUNT = 100;

	/** Logger to use when displaying state information */
	private Logger logger = LogManager.getLogger(Test03Clone.class.getName());

	/** The ObjectMapper used to translate between JSON and any of the classes derived from
	 * com.cyberfront.crdt.unittest.data.AbstractDataType */
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Build a collection of instances and return them to the calling routine
	 *
	 * @param count The number of instances to create and deposit into the collection
	 * @return The collection of data objects which was created
	 */
	private static Collection<AbstractDataType> buildInstances(long count) {
		Collection<AbstractDataType> rv = new ArrayList<>();
		
		for (int i=0; i<count; ++i) {
			rv.add(Factory.getInstance());
		}
		
		return rv;
	}
	
	/**
	 * Given a collection of objects return a copy of those objects
	 *
	 * @param source The source list of objects to copy
	 * @return The collection containing the copied data objects
	 */
	private static Collection<AbstractDataType> cloneInstances(Collection<AbstractDataType> source) {
		Collection<AbstractDataType> rv = new ArrayList<>();
		
		for (AbstractDataType element : source) {
			rv.add(Factory.copy(element));
		}
		
		return rv;
	}

	/**
	 * Assess the results of the clone test to ensure the two arrays are an exact copy of each other and that no 
	 * two non-corresponding elements of the two arrays are the same. 
	 * @param iteration Iteration for which this assessment is occurring; used for display purposes only
	 * @param source Source collection
	 * @param clones Clone collection derived from the given source
	 */
	private void assessCloneTest(int iteration, Collection<AbstractDataType> source, Collection<AbstractDataType> clones) {
		int sourceIndex = 0;
		for (AbstractDataType el0 : source) {
			int cloneIndex = 0;
			for (AbstractDataType el1 : clones) {
				boolean expected = sourceIndex == cloneIndex;
				boolean actual = el0.equals(el1);

				if (expected != actual) {
					logger.error("{\"iteration\":" + iteration + ",\"expected\":" + expected + ",\"actual\":" + actual + ",\"originalCount\":" + sourceIndex + ",\"cloneCount\":" + cloneIndex + "}");
					logger.error("{\"el0\":" + el0.toString());
					logger.error("{\"el1\":" + el1.toString());
				}
				assertEquals(expected, actual);

				JsonNode diff = JsonDiff.asJson(mapper.valueToTree(el0), mapper.valueToTree(el1));
				if (actual && diff.size() > 0) {
					logger.error("unexpected difference: " + diff);
				}
				assertTrue("Unexpected Difference:", !actual || diff.size()==0);
				++cloneIndex;
			}
			++sourceIndex;
		}
	}
	
	/**
	 * Perform the clone test and assess the results
	 *
	 * @param count Number of times to run the test 
	 * @param instances Number of instances to create and clone during each test
	 */
	private void cloneDataTest(long count, long instances) {
		logger.info("\n** Test03Clone: {\"count\":" + count + ",\"instances\":" + instances + "}");
		for (int i=0; i<count; ++ i) {
			Collection<AbstractDataType> source = buildInstances(instances);
			Collection<AbstractDataType> clones = cloneInstances(source);
			assessCloneTest(i, source, clones);
		}
		logger.info("   SUCCESS");
	}
	
	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void cloneDataTest() {
		this.cloneDataTest(TRIAL_COUNT, INSTANCE_COUNT);
	}
}
