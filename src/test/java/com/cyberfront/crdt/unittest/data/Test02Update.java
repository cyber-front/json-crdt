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

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

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
		private static final double UPDATE_PROBABILITY = 0.2;
		
		/** Logger to use when displaying state information */
		private Logger logger = LogManager.getLogger(Test03Clone.CloneTest.class);

		private double updateProbability;
		
		public UpdateTest() {
			super();
			this.setUpdateProbability(UPDATE_PROBABILITY);
		}
		
		public UpdateTest(double updateProbability, long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
			super(trialCount, abbreviatedFactor, stressedFactor, abbreviated, stressed);
			this.setUpdateProbability(updateProbability);
		}

		/**
		 * Return the given update probability factor
		 * @return The update probability factor
		 */
		public double getUpdateProbability() {
			return this.updateProbability;
		}

		public void setUpdateProbability(double updateProbability) {
			this.updateProbability = updateProbability;
		}

		/**
		 * This performs the actual create and update test given a count and update probability.  Each create is accompanied by only 
		 * a single update session on the object.
		 *
		 * @param count The number of create operations to perform; there will be one update per create
		 * @param updateProb The probability for a particular field in the generated AbstractDataType to be randomly changed
		 */
		public void test() {
			logger.info("\n** Test02Update: {\"count\":" + this.getTrialCount() + ",\"updateProb\":" + this.getUpdateProbability() + "}");
			for (int i=0; i<this.getTrialCount(); ++i) {
				AbstractDataType tmp = Factory.getInstance();
				assertNotNull(tmp);
				tmp.update(this.getUpdateProbability());
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
