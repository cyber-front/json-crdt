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
package com.cyberfront.crdt.unittest.data;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.sample.data.Factory;

/**
 * This contains a class used for performing unit tests designed to create and update a number of 
 * AbstractDataType instances using the factory.  It is successful if it creates and updates these
 * objects, neither of which result in null.
 */
public class Test02Update {
	public static class UpdateTest extends AssessmentSupport {
		/** Constant to define the default update probability to use */
		private static final double PROBABILITY_CHANGE = 0.2;
		
		/** Logger to use when displaying state information */
		private Logger logger = LogManager.getLogger(Test03Clone.CloneTest.class);

		/** Probability of change */
		private double pChange;
		
		/**
		 * Constructor for using the default settings
		 */
		public UpdateTest() {
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
		public UpdateTest(double pChange, long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
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
			logger.info("\n** Test02Update: {\"count\":" + this.getTrialCount() + ",\"updateProb\":" + this.getProbabilityChange() + "}");
			for (int i=0; i<this.getTrialCount(); ++i) {
				AbstractDataType tmp = Factory.getInstance();
				assertNotNull(tmp);
				tmp = tmp.copy(this.getProbabilityChange());
				assertNotNull(tmp);
			}
			logger.info("   SUCCESS");
		}
	}
	
	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void updateDataTest() {
		UpdateTest test = new UpdateTest();
		test.test();
	}
}
