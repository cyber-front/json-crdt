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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.sample.manager.JsonManager;
import com.cyberfront.crdt.support.Support;
import com.cyberfront.crdt.unittest.data.AssessmentSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

/**
 * This contains a class used for performing unit tests designed to create a number of 
 * AbstractDataType instances using the factory.  It is successful if it creates these
 * objects and doesn't return null.
 */
public class Test01Json {
	public static class JsonTest extends AssessmentSupport {
		/** Constant defining the number of states to use in the testing */
		private static final long STATE_COUNT = 1024L;
		
		/** Logger to use when displaying state information */
		private static final Logger logger = LogManager.getLogger(Test01Json.JsonTest.class);
		
		/** Number of states to prepare for the testing suite */
		private final long stateCount;

		/** The ObjectMapper used to translate between JSON and any of the classes derived from
		 * com.cyberfront.crdt.unittest.data.AbstractDataType */
//		@SuppressWarnings("unused")
		private static final ObjectMapper mapper = new ObjectMapper();
		
		/**
		 * Default constructor which initialized fields to their default values
		 */
		public JsonTest() {
			super();
			this.stateCount = STATE_COUNT;
		}
		
		/**
		 * Constructor to deliberately initialize each field to the associated values provided
		 * @param stateCount The number of state transitions for the test
		 * @param trialCount Trial count to use for the test activity
		 * @param abbreviatedFactor Abbreviation factor to use the basis of this CreateTest instance when the abbreviated flag is set
		 * @param stressedFactor Stressed factor to use when the the stressed flag is set
		 * @param abbreviated Abbreviated flag which indicates when to divide different test parameters by the abbreviatedFactor
		 * @param stressed Stressed flag which indicates when to multiply different test parameters by the stressedFactor
		 */
		public JsonTest(long stateCount, long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
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
		 * Test the recall ability a JSON CRDT to ensure the CRDT can reconstruct a JSON object it manages
		 */
		public void testRecall() {
			logger.info("\n** testRecall: {\"count\":" + this.getTrialCount() + ", \"stateCount\":" + this.getStateCount()+ "}");
			
			for (int trial=0; trial<this.getTrialCount(); ++trial) {
				StringBuilder sb = new StringBuilder();
				logger.info("   trial " + (trial+1) + " of " + this.getTrialCount() + ".");

				Collection<AbstractDataType> objects = generateObjectSequence(this.stateCount, 0.1);
				sb.append("{\"objects\":" + Support.convert(objects));
				if (this.stateCount != objects.size()) {
					sb.append("}");
					System.out.println(sb.toString());
					assertEquals("Object count mismatch: ", this.stateCount, objects.size());
				}
				
				Collection<JsonNode> documents = generateJsonSequence(objects);
				sb.append(",\n\"documents\":" + Support.convert(documents));
				if (this.stateCount != documents.size()) {
					sb.append("}");
					System.out.println(sb.toString());
					assertEquals("Document count mismatch: ", this.stateCount, documents.size());
				}

				Collection<JsonNode> diffs = generateDifferenceSequence(documents);
				sb.append(",\n\"diffs\":" + Support.convert(diffs));
				if (this.stateCount != diffs.size()) {
					sb.append("}");
					System.out.println(sb.toString());
					assertEquals("Diference count mismatch: ", this.stateCount, diffs.size());
				}

				Collection<JsonNode> regen = regenerateJsonSequence(diffs);
				sb.append(",\n\"regen\":" + Support.convert(regen));
				if (this.stateCount != regen.size()) {
					sb.append("}");
					System.out.println(sb.toString());
					assertEquals("Regeneration count mismatch: ", this.stateCount, regen.size());
				}

				Collection<JsonNode> deviations = super.compareJsonSequence(documents, regen);
				sb.append(",\n\"deviations\":" + Support.convert(deviations));
				if (this.stateCount != deviations.size()) {
					System.out.println(sb.toString());
					assertEquals("Deviation count mismatch: ", this.stateCount, deviations.size());
				}

				for (JsonNode deviation : deviations) {
					if (0 != deviation.size()) {
						sb.append(",\n\"deviation\":" + (null == deviation ? "null" : deviation.toString()) + "}");
						System.out.println(sb.toString());
						assertEquals("Invalid regeneration: ", 0, deviation.size());
					}
				}
			}
			
			logger.info("   SUCCESS");
		}
		
		/**
		 * Test the ability of the JsonManager to correctly encode the operations and generate the resulting object.
		 */
		public void crdtJsonTest() {
			logger.info("\n** crdtJsonTest: {\"count\":" + this.getTrialCount() + ", \"stateCount\":" + this.getStateCount()+ "}");
			
			for (int trial=0; trial<this.getTrialCount(); ++trial) {
				logger.info("   trial " + (trial+1) + " of " + this.getTrialCount() + ".");

				Collection<AbstractDataType> objects = generateObjectSequence(this.stateCount, 0.1);
				Collection<JsonNode> documents = generateJsonSequence(objects);
				
				long timeStamp = 0;
				JsonManager mgr = new JsonManager(timeStamp);

				for (JsonNode source : documents) {
					mgr.update(source, timeStamp);
					
					JsonNode target = mgr.read(timeStamp);
					try {
						target = mapper.readTree(target.toString());
						source = mapper.readTree(source.toString());
					} catch (IOException e) {
						e.printStackTrace();
						assertTrue(false);
					}
					
					JsonNode diff = JsonDiff.asJson(source, target);
					
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

				Collection<AbstractDataType> objects = generateObjectSequence(this.stateCount, 0.1);
				Collection<JsonNode> states = generateJsonSequence(objects);

				long timestamp = 0;
				JsonManager mgr = new JsonManager(timestamp);
				for (JsonNode source : states) {
					mgr.update(source, timestamp);

					JsonNode mgrDocument = getMapper().valueToTree(mgr);
					String mgrString = mgrDocument.toString();

					JsonNode restoredJsonMgr = null;
					JsonManager restoredMgr = null;

					try {
						restoredJsonMgr = mapper.readerFor(JsonManager.class).readTree(mgrString);
						restoredMgr = mapper.treeToValue(restoredJsonMgr, JsonManager.class);
					} catch (IOException e1) {
						logger.info(e1.toString());
						logger.info("{\"mgr\":" + (null == mgr ? "null" : mgr.toString()) + ",");
						logger.info("\"mgrDocument\":" + (null == mgrDocument ? "null" : mgrDocument.toString()) + ",");
						logger.info("\"mgrString\":" + (null == mgrString ? "null" : mgrString) + ",");
						logger.info("\"restoredJsonMgr\":" + (null == restoredJsonMgr ? "null" : restoredJsonMgr.toString()) + ",");
						logger.info("\"restoredMgr\":" + (null == restoredMgr ? "null" : restoredMgr.toString()) + "}");
					}
					
					assertNotNull(restoredJsonMgr);
					assertNotNull(restoredMgr);
					
					JsonNode target = restoredMgr.read(timestamp);
					
					try {
						target = mapper.readTree(target.toString());
						source = mapper.readTree(source.toString());
					} catch (IOException e) {
						e.printStackTrace();
						assertTrue(false);
					}
					
					JsonNode diff = JsonDiff.asJson(target, source);
					
					if (0 != diff.size()) {
						logger.error("timestamp: " + timestamp);
						logger.error("source: " + (null == source ? "null" : source.toString()));
						logger.error("target: " + (null == target ? "null" : target.toString()));
						logger.error("diff: " + (null == diff ? "null" : diff.toString()));
						logger.error("mgr: " + (null == mgr ? "null" : mgr.toString()));
					}
					
					assertEquals("Difference Detected: ", 0, diff.size());
					
					timestamp += 10;
					
				}
				
				mgr.delete(timestamp);
				
				assertNull(mgr.read(timestamp));
			}
			logger.info("   SUCCESS");
		}
	}

	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void testOperations() {
		JsonTest test = new JsonTest();
		test.crdtJsonTest();
	}
	
	/**
	 * The unit tester for transforming the CRDT into an alternate form and transforming it back to its original form 
	 */
	@Test
	public void testTransformations() {
		JsonTest test = new JsonTest();
		test.testTransformation();
	}
	
	/**
	 * Test the ability of the CRDT to recall the different states of the object for its saved states.
	 */
	@Test
	public void testRecall() {
		JsonTest test = new JsonTest();
		test.testRecall();
	}
}
