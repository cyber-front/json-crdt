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
package com.cyberfront.crdt.unittest.basiccrdt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.sample.data.SimpleCollection;
import com.cyberfront.crdt.sample.manager.GenericManager;
import com.cyberfront.crdt.unittest.data.AssessmentSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

/**
 * This contains a class used for performing unit tests designed to create a number of 
 * AbstractDataType instances using the factory.  It is successful if it creates these
 * objects and doesn't return null.
 */
public class Test01Generic {
	public static class GenericTest extends AssessmentSupport {
		/** Constant defining the number of states to use in the testing */
		private static final long STATE_COUNT = 32L;
		
		/** Logger to use when displaying state information */
		private static final Logger logger = LogManager.getLogger(Test01Generic.GenericTest.class);

		/** Number of states to prepare for the testing suite */
		private final long stateCount;

		/** The ObjectMapper used to translate between JSON and any of the classes derived from
		 * com.cyberfront.crdt.unittest.data.AbstractDataType */
//		@SuppressWarnings("unused")
		private static final ObjectMapper mapper = new ObjectMapper();
		
		/**
		 * Default constructor which initialized fields to their default values
		 */
		public GenericTest() {
			super();
			this.stateCount = STATE_COUNT;
		}
		
		/**
		 * Constructor to deliberately initialize each field to the associated values provided
		 * @param stateCount Number of state transitions to model for each trial
		 * @param trialCount Trial count to use for the test activity
		 * @param abbreviatedFactor Abbreviation factor to use the basis of this CreateTest instance when the abbreviated flag is set
		 * @param stressedFactor Stressed factor to use when the the stressed flag is set
		 * @param abbreviated Abbreviated flag which indicates when to divide different test parameters by the abbreviatedFactor
		 * @param stressed Stressed flag which indicates when to multiply different test parameters by the stressedFactor
		 */
		public GenericTest(long stateCount, long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
			super(trialCount, abbreviatedFactor, stressedFactor, abbreviated, stressed);
			this.stateCount = stateCount;
		}
		
		/**
		 * Get the number of state transitions for this test suite
		 * @return The number of state transition for this test suite
		 */
		public long getStateCount() {
			return this.stateCount * this.getStressedFactor() / this.getAbbreviatedFactor();
		}
		
		/**
		 * Generate a collection of state transitions for testing the JsonManager class
		 * @param count Number of state transitions to generate
		 * @return A collection of state transitions, the number being that given
		 */
		private static Collection<AbstractDataType> getStates(long count) {
			Collection<AbstractDataType> rv = new ArrayList<>();
			AbstractDataType object = new SimpleCollection();
			
			for (long i=0; i<count; ++i) {
				rv.add(object.copy());
				object.update(0.1);
			}
			
			return rv;
		}
		
		/**
		 * Test the ability of the JsonManager to correctly encode the operations and generate the resulting object.
		 */
		public void createDataTest() {
			logger.info("\n** createDataTest: {\"count\":" + this.getTrialCount() + ", \"stateCount\":" + this.getStateCount()+ "}");
			
			for (int trial=0; trial<this.getTrialCount(); ++trial) {
				logger.info("   trial " + (trial+1) + " of " + this.getTrialCount() + ".");

				Collection<AbstractDataType> states = getStates(this.stateCount);
				GenericManager<AbstractDataType> mgr = null;
				long timeStamp = 0;
				for (AbstractDataType source : states) {
					if (null == mgr) {
						mgr = new GenericManager<>(source, timeStamp);
					} else {
						mgr.update(source, timeStamp);
					}
					
					AbstractDataType target = mgr.read(timeStamp);
					
					JsonNode diff = JsonDiff.asJson(mapper.valueToTree(source), mapper.valueToTree(target));
					
					if (0 != diff.size()) {
						logger.error("timestamp: " + timeStamp);
						logger.error("source: " + (null == source ? "null" : source.toString()));
						logger.error("target: " + (null == target ? "null" : target.toString()));
						logger.error("diff: " + (null == diff ? "null" : diff.toString()));
						logger.error("mgr: " + (null == mgr ? "null" : mgr.toString()));
					}
					
					assertEquals("Difference Detected: ", 0, diff.size());
					
					timeStamp += 10;
					
				}
				mgr.delete(timeStamp);
				
				assertNull(mgr.read(timeStamp));
			}
			logger.info("   SUCCESS");
		}

		/**
		 * Test the ability of the CRDT to have an alternate representation and then to check the ability to reformat it as 
		 * into its original form and then function correctly.
		 */
		public void testTransformation() {
			logger.info("\n** testTransformation: {\"count\":" + this.getTrialCount() + ", \"stateCount\":" + this.getStateCount()+ "}");
			
			for (int trial=0; trial<this.getTrialCount(); ++trial) {
				logger.info("   trial " + (trial+1) + " of " + this.getTrialCount() + ".");

				Collection<AbstractDataType> states = getStates(this.stateCount);
				GenericManager<AbstractDataType> mgr = null;
				long timeStamp = 0;
				for (AbstractDataType source : states) {
					if (null == mgr) {
						mgr = new GenericManager<>(source, timeStamp);
					} else {
						mgr.update(source, timeStamp);
					}
					
//					JsonNode target = mgr.read(timeStamp);
//					try {
//						target = mapper.readTree(target.toString());
//						source = mapper.readTree(source.toString());
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					JsonNode diff = JsonDiff.asJson(target, source);
//					
//					if (0 != diff.size()) {
//						logger.error("timestamp: " + timeStamp);
//						logger.error("source: " + (null == source ? "null" : source.toString()));
//						logger.error("target: " + (null == target ? "null" : target.toString()));
//						logger.error("diff: " + (null == diff ? "null" : diff.toString()));
//						logger.error("mgr: " + (null == mgr ? "null" : mgr.toString()));
//					}
//					
//					assertEquals("Difference Detected: ", 0, diff.size());
					
					timeStamp += 10;
					
				}
				
				mgr.delete(timeStamp);
				
				assertNull(mgr.read(timeStamp));
			}
			logger.info("   SUCCESS");
		}
	}
	
	
	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void testOperations() {
		GenericTest test = new GenericTest();
		test.createDataTest();
	}
	
	/**
	 * The unit test for transforming the CRDT into an alternate form and transforming it back to its original form. 
	 */
	@Test
	public void testTransformations() {
		GenericTest test = new GenericTest();
		test.setTrialCount(0);
		test.testTransformation();
	}
}
