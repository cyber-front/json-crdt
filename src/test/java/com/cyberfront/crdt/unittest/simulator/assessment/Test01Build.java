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
package com.cyberfront.crdt.unittest.simulator.assessment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.cyberfront.crdt.unittest.data.AbstractDataType;
import com.cyberfront.crdt.unittest.simulator.CRDTManager;
import com.cyberfront.crdt.unittest.simulator.Executive;
import com.cyberfront.crdt.unittest.simulator.Node;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

// TODO: Auto-generated Javadoc
/**
 * The Class Test01Build.
 */
public class Test01Build {
	
	/** The Constant mapper. */
	private static final ObjectMapper mapper = new ObjectMapper();

	/** The logger. */
	private static Logger logger = LogManager.getLogger(Test01Build.class.getName());

	/** The Constant UPDATE_COUNT. */
	static final Long UPDATE_COUNT = 100L;
	
	/** The Constant TEST_COUNT. */
	static final Long TEST_COUNT = 100L;
	
	/** The Constant USER. */
	static final String USER = "user";
	
	/** The Constant SOURCE. */
	static final String SOURCE = "source";

	/**
	 * Test crdt.
	 *
	 * @param <T> the generic type
	 * @param crdt the crdt
	 * @param update_count the update count
	 * @param trial the trial
	 */
	private static <T extends AbstractDataType> void testCrdt(CRDTManager<T> crdt, long update_count, long trial) {
		JsonNode diff;
		T value = null;
		T record = null;

		try {
			value = crdt.getObjectClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error(e);
			e.printStackTrace();
			return;
		}

		crdt.processCreate(Executive.getExecutive().getTimeStamp(),value);

		for (Long i = 0L; i < update_count; ++i) {
			Executive.getExecutive().incrementTimeSTamp();

			record = crdt.getObject();

			if (null == record) {
				logger.info("\npending failure - returned null record:");
				logger.info("\t\t    trial: " + trial);
				logger.info("\t\titeration: " +i);
				logger.info("\t\t    value: " + (null == value ? "null" : value.toString()));
				logger.info("\t\t   record: " + (null == record ? "null" : record.toString()));
				logger.info("\t\t     crdt: " + crdt.toString());
			}
			assertNotNull("Could not locate record; null retrieved", record);

			diff = JsonDiff.asJson(mapper.valueToTree(record), mapper.valueToTree(value));

			if (diff.size() != 0) {
				logger.info("\npending failure - value and record are mismatched:");
				logger.info("\t\t    trial: " + trial);
				logger.info("\t\titeration: " +i);
				logger.info("\t\t    value: " + (null == value ? "null" : value.toString()));
				logger.info("\t\t   record: " + (null == record ? "null" : record.toString()));
				logger.info("\t\t     diff: " + diff.toString());
				logger.info("\t\t     crdt: " + crdt.toString());
			}
			assertTrue("\n value: " + value.toString() + "\nrecord: " + record.toString(), diff.size() == 0);

			value.update(0.2);
			crdt.processUpdate(Executive.getExecutive().getTimeStamp(), value);
		}

		record = crdt.getObject();
		diff = JsonDiff.asJson(mapper.valueToTree(record), mapper.valueToTree(value));

		if (diff.size() != 0) {
			logger.info("\npending failure - value and record are mismatched:");
			logger.info("\t\t    trial: " + trial);
			logger.info("\t\titeration: final");
			logger.info("\t\t    value: " + (null == value ? "null" : value.toString()));
			logger.info("\t\t   record: " + (null == record ? "null" : record.toString()));
			logger.info("\t\t     diff: " + diff.toString());
			logger.info("\t\t     crdt: " + crdt.toString());
		}
		
		assertTrue("\n value: " + value.toString() + "\nrecord: " + record.toString(), diff.size() == 0);
	}

	/**
	 * Builds the CRDT test.
	 *
	 * @param update_count the update count
	 * @param test_count the test count
	 * @param user the user
	 * @param source the source
	 */
	public void buildCRDTTest(Long update_count, Long test_count, String user, String source) {
		logger.info("\n** Test01Build: {\"test_count\":" + test_count + ",\"update_count\":" + update_count
				+ "\",\"user\":\"" + user + "\",\"source\":\"" + source + "\"}");

		for (Long i = 0L; i < test_count; ++i) {
			Executive.getExecutive().clear();
			Node node = new Node(source, 1, 1);
			Executive.getExecutive().addNode(node);
			testCrdt(node.pickCRDT(), update_count, i);
		}

		logger.info("   SUCCESS");
	}

	/**
	 * Builds the CRDT test.
	 */
	@Test
	public void buildCRDTTest() {
		this.buildCRDTTest(UPDATE_COUNT, TEST_COUNT, USER, SOURCE);
	}
}
