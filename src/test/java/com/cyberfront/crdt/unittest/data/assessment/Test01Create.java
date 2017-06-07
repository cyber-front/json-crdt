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

import static org.junit.Assert.assertNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.cyberfront.crdt.unittest.data.AbstractDataType;
import com.cyberfront.crdt.unittest.data.Factory;
import com.cyberfront.crdt.unittest.support.TestSupport;

/**
 * This contains a class used for performing unit tests designed to create a number of 
 * AbstractDataType instances using the factory.  It is successful if it creates these
 * objects and doesn't return null.
 */
public class Test01Create {
	public static class CreateTest extends TestSupport {
		/** Logger to use when displaying state information */
		private static final Logger logger = LogManager.getLogger(Test01Create.CreateTest.class);

		/**
		 * Default constructor which initialized fields to their default values
		 */
		public CreateTest() {
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
		public CreateTest(long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
			super(trialCount, abbreviatedFactor, stressedFactor, abbreviated, stressed);
		}

		/**
		 * Perform the create test
		 *
		 * @param count The number of create operations to perform to generate random AbstractDataType instances
		 */
		public void createDataTest() {
			logger.info("\n** Test01Create: {\"count\":" + this.getTrialCount() + "}");
			for (int i=0; i<this.getTrialCount(); ++i) {
				AbstractDataType tmp = Factory.getInstance();
				assertNotNull(tmp);
			}
			logger.info("   SUCCESS");
		}
	}
	
	
	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void test() {
		CreateTest test = new CreateTest();
		test.createDataTest();
	}
}
