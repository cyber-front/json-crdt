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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.cyberfront.crdt.unittest.data.AbstractDataType;
import com.cyberfront.crdt.unittest.data.Factory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

// TODO: Auto-generated Javadoc
/**
 * The Class Test04Json.
 */
public class Test04Json {
	
	/** The Constant COUNT. */
	private static final long COUNT=100;

	/** The logger. */
	private Logger logger = LogManager.getLogger(Test03Clone.class.getName());
	
	/** The mapper. */
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Json test.
	 *
	 * @param count the count
	 */
	public void jsonTest(long count) {
		logger.info("\n** Test04Json: {\"count\":" + count + "}");
		
		for (long i=0; i<count; ++i) {
			AbstractDataType el0 = Factory.getInstance();
			AbstractDataType el1 = Factory.copy(el0);
			el1.update(0.2);
			
			JsonNode patch = JsonDiff.asJson(mapper.valueToTree(el0), mapper.valueToTree(el1));

			boolean sizeCompare = patch.size() == 0;
			boolean countCompare = el0.equals(el1);
			assertEquals(sizeCompare, countCompare);
		}

		logger.info("   SUCCESS");
	}

	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void jsonTest() {
		this.jsonTest(COUNT);
	}

}
