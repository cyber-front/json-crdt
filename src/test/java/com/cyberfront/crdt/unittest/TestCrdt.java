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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.sample.manager.GenericManager;
import com.cyberfront.crdt.sample.manager.JsonManager;
import com.cyberfront.crdt.support.Support;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;

/**
 * This contains a class used for performing unit tests designed to create a number of
 * AbstractDataType instances using the factory.  It is successful if it creates these
 * objects and doesn't return null.
 */
public class TestCrdt {
	public static class Json extends AssessmentSupport {
		/** Constant defining the number of states to use in the testing */
		private static final long STATE_COUNT = 1024L;

		/** Logger to use when displaying state information */
		private static final Logger logger = LogManager.getLogger(TestCrdt.Json.class);

		/** Number of states to prepare for the testing suite */
		private final long stateCount;

		/** The ObjectMapper used to translate between JSON and any of the classes derived from
		 * com.cyberfront.crdt.unittest.data.AbstractDataType */
//		@SuppressWarnings("unused")
		private static final ObjectMapper mapper = new ObjectMapper();

		/**
		 * Default constructor which initialized fields to their default values
		 */
		public Json() {
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
		public Json(long stateCount, long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
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
			logger.info("\n** TestCrdt.Json.testRecall: {\"count\":" + this.getTrialCount() + ", \"stateCount\":" + this.getStateCount()+ "}");

			for (int trial=0; trial<this.getTrialCount(); ++trial) {
				StringBuilder sb = new StringBuilder();
				logger.info("   trial " + (trial+1) + " of " + this.getTrialCount() + ".");

				Collection<AbstractDataType> objects = generateObjectSequence(this.stateCount, 0.1);
				sb.append("{\"objects\":" + Support.convert(objects));
				if (this.stateCount != objects.size()) {
					sb.append("}");
					System.out.println(sb.toString());
					assertEquals(this.stateCount, objects.size(), "Object count mismatch: ");
				}

				Collection<JsonNode> documents = generateJsonSequence(objects);
				sb.append(",\n\"documents\":" + Support.convert(documents));
				if (this.stateCount != documents.size()) {
					sb.append("}");
					System.out.println(sb.toString());
					assertEquals(this.stateCount, documents.size(), "Document count mismatch: ");
				}

				Collection<JsonNode> diffs = generateDifferenceSequence(documents);
				sb.append(",\n\"diffs\":" + Support.convert(diffs));
				if (this.stateCount != diffs.size()) {
					sb.append("}");
					System.out.println(sb.toString());
					assertEquals(this.stateCount, diffs.size(), "Diference count mismatch: ");
				}

				Collection<JsonNode> regen = regenerateJsonSequence(diffs);
				sb.append(",\n\"regen\":" + Support.convert(regen));
				if (this.stateCount != regen.size()) {
					sb.append("}");
					System.out.println(sb.toString());
					assertEquals(this.stateCount, regen.size(), "Regeneration count mismatch: ");
				}

				Collection<JsonNode> deviations = super.compareJsonSequence(documents, regen);
				sb.append(",\n\"deviations\":" + Support.convert(deviations));
				if (this.stateCount != deviations.size()) {
					System.out.println(sb.toString());
					assertEquals(this.stateCount, deviations.size(), "Deviation count mismatch: ");
				}

				for (JsonNode deviation : deviations) {
					if (0 != deviation.size()) {
						sb.append(",\n\"deviation\":" + (null == deviation ? "null" : deviation.toString()) + "}");
						System.out.println(sb.toString());
						assertEquals(0, deviation.size(), "Invalid regeneration: ");
					}
				}
			}

			logger.info("   SUCCESS");
		}

		/**
		 * Test the ability of the JsonManager to correctly encode the operations and generate the resulting object.
		 */
		public void testJsonCrdt() {
			logger.info("\n** TestCrdt.Json.testJsonCrdt: {\"count\":" + this.getTrialCount() + ", \"stateCount\":" + this.getStateCount()+ "}");

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

					assertEquals( 0, diff.size(), "Difference Detected: ");

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
			logger.info("\n** TestCrdt.Json.testTransformation: {\"count\":" + this.getTrialCount() + ", \"stateCount\":" + this.getStateCount()+ "}");

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

					assertEquals(0, diff.size(), "Difference Detected: ");

					timestamp += 10;

				}

				mgr.delete(timestamp);

				assertNull(mgr.read(timestamp));
			}
			logger.info("   SUCCESS");
		}
	}

	public static class Generic extends AssessmentSupport {
		/** Constant defining the number of states to use in the testing */
		private static final long STATE_COUNT = 1024L;

		/** Logger to use when displaying state information */
		private static final Logger logger = LogManager.getLogger(TestCrdt.Generic.class);

		/** Number of states to prepare for the testing suite */
		private final long stateCount;

		/** The ObjectMapper used to translate between JSON and any of the classes derived from
		 * com.cyberfront.crdt.unittest.data.AbstractDataType */
//		@SuppressWarnings("unused")
		private static final ObjectMapper mapper = new ObjectMapper();

		/**
		 * Default constructor which initialized fields to their default values
		 */
		public Generic() {
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
		public Generic(long stateCount, long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
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
		 * Test the ability of the JsonManager to correctly encode the operations and generate the resulting object.
		 */
		public void testCreateData() {
			logger.info("\n** TestCrdt.Generic.testCreateData: {\"count\":" + this.getTrialCount() + ", \"stateCount\":" + this.getStateCount()+ "}");

			for (int trial=0; trial<this.getTrialCount(); ++trial) {
				logger.info("   trial " + (trial+1) + " of " + this.getTrialCount() + ".");

				long timeStamp = 0;
				Collection<AbstractDataType> states = super.generateObjectSequence(this.stateCount, 0.1);
				GenericManager<AbstractDataType> mgr = null;
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

					assertEquals(0, diff.size(), "Difference Detected: ");

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
			logger.info("\n** TestCrdt.Generic.testTransformation: {\"count\":" + this.getTrialCount() + ", \"stateCount\":" + this.getStateCount()+ "}");

			for (int trial=0; trial<this.getTrialCount(); ++trial) {
				logger.info("   trial " + (trial+1) + " of " + this.getTrialCount() + ".");

				Collection<AbstractDataType> states = super.generateObjectSequence(this.stateCount, 0.1);
				GenericManager<AbstractDataType> mgr = null;
				long timeStamp = 0;
				for (AbstractDataType source : states) {
					if (null == mgr) {
						mgr = new GenericManager<>(source, timeStamp);
					} else {
						mgr.update(source, timeStamp);
					}

					AbstractDataType target = mgr.read(timeStamp);

					JsonNode srcDoc = mapper.valueToTree(source);
					JsonNode tgtDoc = mapper.valueToTree(target);
					JsonNode diff = JsonDiff.asJson(srcDoc, tgtDoc);

					if (0 != diff.size()) {
						logger.error("timestamp: " + timeStamp);
						logger.error("source: " + (null == source ? "null" : source.toString()));
						logger.error("target: " + (null == target ? "null" : target.toString()));
						logger.error("diff: " + (null == diff ? "null" : diff.toString()));
						logger.error("mgr: " + (null == mgr ? "null" : mgr.toString()));
					}

					assertEquals( 0, diff.size(), "Difference Detected: ");

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
	public void testGenericOperations() {
		Generic test = new Generic();
		test.testCreateData();
	}

	/**
	 * The unit test for transforming the CRDT into an alternate form and transforming it back to its original form.
	 */
	@Test
	public void testGenericTransformations() {
		Generic test = new Generic();
		test.testTransformation();
	}


	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void testJsonOperations() {
		Json test = new Json();
		test.testJsonCrdt();
	}
	
	/**
	 * The unit tester for transforming the CRDT into an alternate form and transforming it back to its original form 
	 */
	@Test
	public void testJsonTransformations() {
		Json test = new Json();
		test.testTransformation();
	}
	
	/**
	 * Test the ability of the CRDT to recall the different states of the object for its saved states.
	 */
	@Test
	public void testJsonRecall() {
		Json test = new Json();
		test.testRecall();
	}
}
