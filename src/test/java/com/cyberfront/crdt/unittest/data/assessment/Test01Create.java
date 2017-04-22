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

/**
 * This contains a class used for performing unit tests designed to create a number of 
 * AbstractDataType instances using the factory.  It is successful if it creates these
 * objects and doesn't return null.
 */
public class Test01Create {
	
	/** Constant defining the number of AbstractDataType elements to create in the unit test */
	private static final long COUNT=100L;
	
	/** Logger to use when displaying state information */
	private Logger logger = LogManager.getLogger(Test03Clone.class);

	/**
	 * Perform the create test
	 *
	 * @param count The number of create operations to perform to generate random AbstractDataType instances
	 */
	private void createDataTest(long count) {
		logger.info("\n** Test01Create: {\"count\":" + count + "}");
		for (int i=0; i<count; ++i) {
			AbstractDataType tmp = Factory.getInstance();
			assertNotNull(tmp);
		}
		logger.info("   SUCCESS");
	}

	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void createDataTest() {
		this.createDataTest(COUNT);
	}
}
