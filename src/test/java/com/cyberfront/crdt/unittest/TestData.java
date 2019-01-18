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
package com.cyberfront.crdt.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.sample.data.Factory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

/**
 * This contains a class used for performing unit tests designed to create a number of 
 * AbstractDataType instances using the factory.  It is successful if it creates these
 * objects and doesn't return null.
 */
public class TestData {
	public static class Create extends AssessmentSupport {
		/** Logger to use when displaying state information */
		private static final Logger logger = LogManager.getLogger(TestData.Create.class);

		/**
		 * Default constructor which initialized fields to their default values
		 */
		public Create() {
			super();
		}
		
		/**
		 * Constructor to deliberately initialize each field to the associated values provided
		 * @param trialCount Trial count to use for the test activity
		 * @param abbreviatedFactor Abbreviation factor to use the basis of this CreateTest instance when the abbreviated flag is set
		 * @param stressedFactor Stressed factor to use when the the stressed flag is set
		 * @param abbreviated Abbreviated flag which indicates when to divide different test parameters by the abbreviatedFactor
		 * @param stressed Stressed flag which indicates when to multiply different test parameters by the stressedFactor
		 */
		public Create(long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
			super(trialCount, abbreviatedFactor, stressedFactor, abbreviated, stressed);
		}

		/**
		 * Test the ability to create a new concrete object derived from AbstractDataType
		 */
		public void test() {
			logger.info("\n** TestData.Create.test: {\"count\":" + this.getTrialCount() + "}");
			for (int i=0; i<this.getTrialCount(); ++i) {
				AbstractDataType tmp = Factory.getInstance();
				assertNotNull(tmp);
			}
			logger.info("   SUCCESS");
		}
	}

	public static class Update extends AssessmentSupport {
		/** Constant to define the default update probability to use */
		private static final double PROBABILITY_CHANGE = 0.2;
		
		/** Logger to use when displaying state information */
		private Logger logger = LogManager.getLogger(TestData.Update.class);
	
		/** Probability of change */
		private double pChange;
		
		/**
		 * Constructor for using the default settings
		 */
		public Update() {
			super();
			this.setProbabilityChange(PROBABILITY_CHANGE);
		}
		
		/**
		 * Constructor to explicitly set each of the test parameters 
		 * @param pChange Probability of change
		 * @param trialCount Number of trials to perform
		 * @param abbreviatedFactor Division factor for performing abbreviated tests
		 * @param stressedFactor Multiplication factor for performing stress tests
		 * @param abbreviated Flag to use abbreviated testing
		 * @param stressed Flag to use stress testing
		 */
		public Update(double pChange, long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
			super(trialCount, abbreviatedFactor, stressedFactor, abbreviated, stressed);
			this.setProbabilityChange(pChange);
		}
	
		/**
		 * Return the given update probability factor
		 * @return The update probability factor
		 */
		public double getProbabilityChange() {
			return this.pChange;
		}
	
		/**
		 * Set the probability of change for update operations
		 * @param pChange New probability of change value
		 */
		public void setProbabilityChange(double pChange) {
			this.pChange = pChange;
		}
	
		/**
		 * This performs the actual create and update test given a count and update probability.  Each create is accompanied by only 
		 * a single update session on the object.
		 */
		public void test() {
			logger.info("\n** TestData.Update.test: {\"count\":" + this.getTrialCount() + ",\"updateProb\":" + this.getProbabilityChange() + "}");
			for (int i=0; i<this.getTrialCount(); ++i) {
				AbstractDataType tmp = Factory.getInstance();
				assertNotNull(tmp);
				tmp = tmp.copy(this.getProbabilityChange());
				assertNotNull(tmp);
			}
			logger.info("   SUCCESS");
		}
	}

	public static class Clone extends AssessmentSupport {
		/** The Constant INSTANCE_COUNT. */
		private static final long INSTANCE_COUNT = 128;
	
		/** Logger to use when displaying state information */
		private Logger logger = LogManager.getLogger(TestData.Clone.class);
	
		/** Number of instances to generate for the test */
		private long instanceCount;
		
		/** Default constructor which initializes the unit test component with the default values */
		public Clone() {
			super();
			this.setInstanceCount(INSTANCE_COUNT);
		}
	
		/**
		 * Constructor for the clone test where the arguments are specifically passed to be instantiated
		 * @param instanceCount Number of insteances to generate for a particular test
		 * @param trialCount Number of trials to perform over the course of the unit test
		 * @param abbreviatedFactor Division factor to use on the argument when performing an abbreviated test
		 * @param stressedFactor Multiplication factor to use on the arguments when performing a stress test
		 * @param abbreviated Flag indicating whether this is an abbreviated test
		 * @param stressed Flag indicating whether this is a stress test
		 */
		public Clone(long instanceCount, long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
			super(trialCount, abbreviatedFactor, stressedFactor, abbreviated, stressed);
			this.setInstanceCount(instanceCount);
		}
	
		/**
		 * Retrieve the number of instances to create while executing the test and return to the calling routine
		 * @return The instance count
		 */
		public long getInstanceCount() {
			return instanceCount * this.getStressedFactor() / this.getAbbreviatedFactor();
		}
	
		/**
		 * Set the instance count value to that specified
		 * @param instanceCount New value for the instance counter
		 */
		protected void setInstanceCount(long instanceCount) {
			this.instanceCount = instanceCount;
		}
		
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
				rv.add(element.copy());
			}
			
			return rv;
		}
	
		/**
		 * Assess the results of the clone test to ensure the two arrays are an exact copy of each other and that no 
		 * two non-corresponding elements of the two arrays are the same. 
		 * @param iteration Iteration for which this assessment is occurring; used for display purposes only
		 * @param value Source collection
		 * @param clones Clone collection derived from the given source
		 */
		private void assessCloneTest(int iteration, Collection<AbstractDataType> value, Collection<AbstractDataType> clones) {
			int sourceIndex = 0;
			for (AbstractDataType el0 : value) {
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
	
					JsonNode source = getMapper().valueToTree(el0);
					JsonNode target = getMapper().valueToTree(el1);
					JsonNode diff = JsonDiff.asJson(source, target);
	
					if (actual && diff.size() > 0) {
						logger.error("unexpected difference: " + diff);
					}
					assertTrue(!actual || diff.size()==0, "Unexpected Difference:");
					++cloneIndex;
				}
				++sourceIndex;
			}
		}
		
		/**
		 * Perform the clone test with the predefined collection of settings and assess the results
		 */
		public void test() {
			logger.info("\n** TestData.Clone.test: {\"count\":" + this.getTrialCount() + ",\"instances\":" + this.getInstanceCount() + "}");
			for (int i=0; i<this.getTrialCount(); ++ i) {
				Collection<AbstractDataType> source = buildInstances(this.getInstanceCount());
				Collection<AbstractDataType> clones = cloneInstances(source);
				assessCloneTest(i, source, clones);
			}
			logger.info("   SUCCESS");
		}
	}

	//import com.flipkart.zjsonpatch.JsonDiff;		// Use this with zjsonpatch
	
	/**
	 * This contains a class used for performing unit tests designed to create an element and a copy and then assess the
	 * performance and ability of object and its clone to be compared using JsonDiff.  It is successful if it creates
	 * and updates these objects, neither of which result in null.
	 */
	public class Encoding extends AssessmentSupport {
		
		/** Logger to use when displaying state information */
		private Logger logger = LogManager.getLogger(TestData.Encoding.class.getName());
		
		/** The ObjectMapper used to translate between JSON and any of the classes derived from
		 * com.cyberfront.crdt.unittest.data.AbstractDataType */
		private ObjectMapper mapper = new ObjectMapper();
		
		/**
		 * Perform the actual test the specified number of times, whereby each test compares an object created and its clone
		 * as JSON objects using JsonDiff. 
		 */
		public void test() {
			logger.info("\n** TestData.Encoding.test: {\"count\":" + this.getTrialCount() + "}");
			
			for (long i=0; i<this.getTrialCount(); ++i) {
				AbstractDataType el0 = Factory.getInstance();
				AbstractDataType el1 = el0.copy();
				el1 = el1.copy(0.2);
				
				JsonNode source = mapper.valueToTree(el0);
				JsonNode target = mapper.valueToTree(el1);
	
				JsonNode diff = JsonDiff.asJson(source, target);
	
				boolean sizeCompare = diff.size() == 0;
				boolean countCompare = el0.equals(el1);
				assertEquals(sizeCompare, countCompare);
			}
	
			logger.info("   SUCCESS");
		}
	
		/**
		 * The main unit test routine used to perform the actual test execution 
		 */
	}

	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void testCreate() {
		Create test = new Create();
		test.test();
	}

	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void testUpdate() {
		Update test = new Update();
		test.test();
	}

	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void testClone() {
		Clone test = new Clone();
		test.test();
	}

	@Test
	public void testEncoding() {
		Encoding test = new Encoding();
		test.test();
	}
}
