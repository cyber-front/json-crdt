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
import com.cyberfront.crdt.unittest.support.WordFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

/**
 * This test performs assessments on the ability of the code base to create and manage CRDT's
 * to determine whether they have the basic functionality necessary to use them to capture 
 * various CRUD operations.
 */
public class Test01Build {
	/** Logger to use when displaying state information */
	private static final Logger logger = LogManager.getLogger(Test01Build.class);

	/** The ObjectMapper used to translate between JSON and POJO's */
	private static final ObjectMapper mapper = new ObjectMapper();
	
	/** Specifies the number of runs to perform for a given test battery. */
	private static final Long TRIAL_COUNT = 100L;

	/** Specifies the number of time a run is to perform updates on the given CRDT managed data element. */
	private static final Long UPDATE_COUNT = 100L;
	
	/**
	 * Perform the test to determine the ability of the CRDT to handle updates to its base object
	 *
	 * @param <T> The type of object the CRDT is managing
	 * @param crdt The CRDT to use as a test article for the assessment
	 * @param updateCount The number of updates to perform during the trial
	 * @param trial The trial number for this trial
	 */
	private static <T extends AbstractDataType> void testCrdt(CRDTManager<T> crdt, long updateCount, long trial) {
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

		for (Long update = 0L; update < updateCount; ++update) {
			Executive.getExecutive().incrementTimeSTamp();

			record = crdt.getObject();

			if (null == record) {
				logger.info("\npending failure - returned null record:");
				logger.info("\t\t      trial: " + trial);
				logger.info("\t\tupdateCount: " + updateCount);
				logger.info("\t\t     update: " + update);
				logger.info("\t\t      value: " + (null == value ? "null" : value.toString()));
				logger.info("\t\t     record: " + (null == record ? "null" : record.toString()));
				logger.info("\t\t       crdt: " + crdt.toString());
			}
			assertNotNull("Could not locate record; null retrieved", record);

			diff = JsonDiff.asJson(mapper.valueToTree(record), mapper.valueToTree(value));

			if (diff.size() != 0) {
				logger.info("\npending failure - value and record are mismatched:");
				logger.info("\t\t      trial: " + trial);
				logger.info("\t\tupdateCount: " + updateCount);
				logger.info("\t\t     update: " + update);
				logger.info("\t\t      value: " + (null == value ? "null" : value.toString()));
				logger.info("\t\t     record: " + (null == record ? "null" : record.toString()));
				logger.info("\t\t       diff: " + diff.toString());
				logger.info("\t\t       crdt: " + crdt.toString());
			}
			assertTrue("\n value: " + value.toString() + "\nrecord: " + record.toString(), diff.size() == 0);

			value.update(0.2);
			crdt.processUpdate(Executive.getExecutive().getTimeStamp(), value);
		}

		record = crdt.getObject();
		diff = JsonDiff.asJson(mapper.valueToTree(record), mapper.valueToTree(value));

		if (diff.size() != 0) {
			logger.info("\npending failure - value and record are mismatched:");
			logger.info("\t\t      trial: " + trial);
			logger.info("\t\tupdateCount: " + updateCount);
			logger.info("\t\t     update: final");
			logger.info("\t\t      value: " + (null == value ? "null" : value.toString()));
			logger.info("\t\t     record: " + (null == record ? "null" : record.toString()));
			logger.info("\t\t       diff: " + diff.toString());
			logger.info("\t\t       crdt: " + crdt.toString());
		}
		
		assertTrue("\n value: " + value.toString() + "\nrecord: " + record.toString(), diff.size() == 0);
	}

	/**
	 * This performs a series of randomly generated trials on the CRDT logic to ensure
	 * it is able to perform create and update operations consistently.
	 *
	 * @param updateCount The number of updates to perform on each trial
	 * @param trialCount The number of trials to perform
	 */
	private void buildCRDTTest(Long updateCount, Long trialCount) {
		logger.info("\n** Test01Build: {\"trials\":" + trialCount + ",\"update_count\":" + updateCount + "\"}");

		for (Long trial = 0L; trial < trialCount; ++trial) {
			Executive.getExecutive().clear();
			Node node = new Node(WordFactory.getNoun(), 1, 1);
			Executive.getExecutive().addNode(node);
			testCrdt(node.pickCRDT(), updateCount, trial);
		}

		logger.info("   SUCCESS");
	}

	/**
	 * This is the main executive of the CRDT Build test which defines the scale and scope of this 
	 * assessment.
	 */
	@Test
	public void buildCRDTTest() {
		this.buildCRDTTest(UPDATE_COUNT, TRIAL_COUNT);
	}
}
