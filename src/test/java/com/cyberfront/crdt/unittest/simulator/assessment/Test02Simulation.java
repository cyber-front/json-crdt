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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

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
 * The Class Test01Simulation.
 */
public class Test02Simulation {
	
	/** The Constant CYCLE_COUNT. */
	private static final long CYCLE_COUNT = 4;
	
	/** The Constant CREATION_COUNT. */
	private static final long CREATION_COUNT = 64;
	
	/** The Constant READ_COUNT. */
	private static final long READ_COUNT = 64;
	
	/** The Constant UPDATE_COUNT. */
	private static final long UPDATE_COUNT = 64;
	
	/** The Constant DELETE_COUNT. */
	private static final long DELETE_COUNT = 16;
	
	/** The Constant NODE_COUNT. */
	private static final long NODE_COUNT = 32;
	
	/** The Constant STRESS_CYCLE_COUNT. */
	private static final long STRESS_CYCLE_COUNT = 512;
	
	/** The Constant STRESS_CREATION_COUNT. */
	private static final long STRESS_CREATION_COUNT = 1024;
	
	/** The Constant STRESS_READ_COUNT. */
	private static final long STRESS_READ_COUNT = 128;
	
	/** The Constant STRESS_UPDATE_COUNT. */
	private static final long STRESS_UPDATE_COUNT = 512;
	
	/** The Constant STRESS_DELETE_COUNT. */
	private static final long STRESS_DELETE_COUNT = 128;
	
	/** The Constant STRESS_NODE_COUNT. */
	private static final long STRESS_NODE_COUNT = 256;
	
	/** The Constant REJECTION_PROBABILITY. */
	private static final double REJECTION_PROBABILITY = 0.10d;

	/** The Constant mapper. */
	private static final ObjectMapper mapper = new ObjectMapper();

	/** Flag to indicate the type of testing to perform */ 
	private static boolean stressTest = false;
	
	/** The logger. */
//	@SuppressWarnings("unused")
	private Logger logger = LogManager.getLogger(Test02Simulation.class);

	public static boolean isStressTest() {
		return stressTest;
	}

	public static void setStressTest(boolean value) {
		stressTest = value;
	}
	
	/**
	 * Gets the cycle count.
	 *
	 * @return the cycle count
	 */
	private static long getCycleCount() {
		return isStressTest() ? STRESS_CYCLE_COUNT : CYCLE_COUNT;
	}

	/**
	 * Gets the creation count.
	 *
	 * @return the creation count
	 */
	private static long getCreationCount() {
		return isStressTest() ? STRESS_CREATION_COUNT : CREATION_COUNT;
	}

	/**
	 * Gets the read count.
	 *
	 * @return the read count
	 */
	private static long getReadCount() {
		return isStressTest() ? STRESS_READ_COUNT : READ_COUNT;
	}

	/**
	 * Gets the update count.
	 *
	 * @return the update count
	 */
	private static long getUpdateCount() {
		return isStressTest() ? STRESS_UPDATE_COUNT : UPDATE_COUNT;
	}

	/**
	 * Gets the delete count.
	 *
	 * @return the delete count
	 */
	private static long getDeleteCount() {
		return isStressTest() ? STRESS_DELETE_COUNT : DELETE_COUNT;
	}

	/**
	 * Gets the node count.
	 *
	 * @return the node count
	 */
	private static long getNodeCount() {
		return isStressTest() ? STRESS_NODE_COUNT : NODE_COUNT;
	}

	/**
	 * Assess data element count.
	 */
	private void assessDataElementCount() {
		logger.info("        Test01Simulation.assessDataElementCount()");
		
		for (Map.Entry<String, Node> entry : Executive.getExecutive().getNodes().entrySet()) {
			
			String name = entry.getKey();
			Node node = entry.getValue();
			long count = node.getDatastore().size();

			StringBuilder sb = new StringBuilder();
			
			sb.append("CRDT count mismatch on node \"");
			sb.append(name);
			sb.append("\"; expected: ");
			sb.append(String.valueOf(getCreationCount()));
			sb.append("; found: ");
			sb.append(String.valueOf(count));
			assertTrue(sb.toString(), getCreationCount() == count);
		}
	}
	
	/**
	 * Assess data element content.
	 */
	private void assessDataElementContent() {
		logger.info("        Test01Simulation.assessDataElementContent()");

		for (Map.Entry<String, Node> entry : Executive.getExecutive().getNodes().entrySet()) {

			Node baseNode = entry.getValue();
			assertNotNull("baseNode found to be null", baseNode);

			for (Entry<String, CRDTManager<? extends AbstractDataType>> baseEntry : entry.getValue().getDatastore().entrySet()) {
				assessDataElementContent(baseEntry.getKey(), baseEntry.getValue());

			}
		}
	}

	/**
	 * Assess data element content.
	 *
	 * @param <T> the generic type
	 * @param baseKey the base key
	 * @param baseCRDT the base CRDT
	 */
	private <T extends AbstractDataType> void assessDataElementContent(String baseKey, CRDTManager<T> baseCRDT) {
		assertNotNull("baseCRDT found to be null", baseCRDT);

		AbstractDataType baseValue = baseCRDT.getObject();
		if (baseCRDT.isDeleted()) {
			if (null != baseValue) {
				System.out.print(baseCRDT.toString());
			}
			assertNull("Deleted baseValue should be null, but isn't", baseValue);
		} else {
			if (null == baseValue) {
				System.out.print(baseCRDT.toString());
			}
		}

		assertNotNull("baseKey found to be null", baseKey);

		for (Map.Entry<String, Node> compEntry : Executive.getExecutive().getNodes().entrySet()) {
			Node compNode = compEntry.getValue();
			assertNotNull("compNode found to be null", compNode);

			CRDTManager<? extends AbstractDataType> compCRDT = compNode.getDatastore(baseKey);
			assertNotNull("compCRDT found to be null", compCRDT);

			AbstractDataType compValue = compCRDT.getObject();
			assertTrue("compCRDT.isDeleted (" + compCRDT.isDeleted() + ") / baseCRDT.isDeleted (" + baseCRDT.isDeleted() + ") deleted flag mismatch: ", compCRDT.isDeleted() == baseCRDT.isDeleted() );

			if (compCRDT.isDeleted()) {
				assertNull("Deleted compValue should be null, but isn't", compValue);
			} else {
				assertNotNull("Undeleted compValue should not be null, but is", compValue);
				JsonNode diff = JsonDiff.asJson(mapper.valueToTree(baseValue), mapper.valueToTree(compValue));
				assertNotNull("diff found to be null", diff);

				StringBuilder sb = new StringBuilder();
				
				sb.append("Value mismatch discovered in data object with \"base ID\": ");
				sb.append(baseValue.getId());
				sb.append("and \"comp ID\": ");
				sb.append(compValue.getId());
				sb.append("{\n\t{\"baseCRDT\":" + baseCRDT.toString() + ",");
				sb.append("\n\t \"compCRDT\":" + compCRDT.toString() + ",");
				sb.append("\n\t \"baseValue\":" + baseValue.toString() + ",");
				sb.append("\n\t \"compValue\":" + compValue.toString() + "}\n}");
				
				assertTrue(sb.toString(), diff.size() == 0);
			}
		}
	}

	/**
	 * Assess invalid operations.
	 */
	private void assessInvalidOperations() {
		logger.info("        Test01Simulation.assessInvalidOperations()");
		
		Node baseNode = Executive.getExecutive().pickNode();
		assertNotNull("baseNode found to be null", baseNode);

		for (Entry<String, CRDTManager<? extends AbstractDataType>> baseEntry : baseNode.getDatastore().entrySet()) {
			AbstractDataType val = baseEntry.getValue().getObject();

			if (!baseEntry.getValue().isCreated() || baseEntry.getValue().isDeleted()) {
				// TODO Remove before flight
				if (null != val) {
					logger.info("\n*** Test02Simluation.assessInvalidOperations()");
					logger.info("    baseEntry.getValue(): " + baseEntry.getValue().toString());
				}
				assertNull("CRDT expected to be null, but isnt", val);
			} else {
				assertNotNull("CRDT expected to be not null, but is", val);
			}

			int invalidOperations = baseEntry.getValue().getInvalidOperationCount();
			
			if (invalidOperations > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("    Node : " + baseNode.getNodeName() + "\n");
				sb.append("    count : " + baseEntry.getValue().getInvalidOperationCount() + "\n");
				logger.info("    Residual invalid operations detected: \n" + sb.toString());
			}
		}
	}

	/**
	 * Assess simulation.
	 */
	private void assessSimulation() {
		assessDataElementCount();
		assessInvalidOperations();
		if (!isStressTest()) {
			assessDataElementContent();
		}
	}
	
	/**
	 * Simulate test.
	 *
	 * @param cycleCount the cycle count
	 * @param createCount the create count
	 * @param readCount the read count
	 * @param updateCount the update count
	 * @param deleteCount the delete count
	 * @param nodeCount the node count
	 * @param rejectProbability the reject probability
	 */
	public void simulateTest(long cycleCount, long createCount, long readCount, long updateCount, long deleteCount, long nodeCount, double rejectProbability) {
		logger.info("\n** simulateTest: {\"cycleCount\":" + cycleCount +
						",\"createCount\":" + createCount + 
						",\"readCount\":" + readCount + 
						",\"updateCount\":" + updateCount + 
						",\"deleteCount\":" + deleteCount + 
						",\"nodeCount\":" + nodeCount + 
						",\"rejectProbability\":" + rejectProbability + 
						"}");
		Executive executive = Executive.getExecutive();
		String crud = "";
		
		crud += createCount > 0 ? 'C' : 'x';
		crud += readCount > 0 ? 'R' : 'x';
		crud += updateCount > 0 ? 'U' : 'x';
		crud += deleteCount > 0 ? 'D' : 'x';
		
		for (long i=0; i<cycleCount; ++i) {
			logger.info("   simulateTest: " + crud + "; run " + (i+1) + " of " + cycleCount + ".");
			
			executive.clear();
			executive.setCounts(createCount, readCount, updateCount, deleteCount, nodeCount, rejectProbability);
			try {
				executive.execute();
			} catch (ReflectiveOperationException e) {
				logger.error(e);
				e.printStackTrace();
			}
			assessSimulation();
		}
		logger.info("   SUCCESS");
	}
	
	/**
	 * Simulate create.
	 */
	@Test
	public void simulateCreate() {
		setStressTest(false);
		this.simulateTest(getCycleCount(), getCreationCount(), 0, 0, 0, getNodeCount(), REJECTION_PROBABILITY);
	}

	/**
	 * Simulate read.
	 */
	@Test
	public void simulateRead() {
		setStressTest(false);
		this.simulateTest(getCycleCount(), getCreationCount(), getReadCount(), 0, 0, getNodeCount(), REJECTION_PROBABILITY);
	}

	/**
	 * Simulate update.
	 */
	@Test
	public void simulateUpdate() {
		setStressTest(false);
		this.simulateTest(getCycleCount(), getCreationCount(), getReadCount(), getUpdateCount(), 0, getNodeCount(), REJECTION_PROBABILITY);
	}

	/**
	 * Simulate delete.
	 */
	@Test
	public void simulateDelete() {
		setStressTest(false);
		this.simulateTest(getCycleCount(), getCreationCount(), getReadCount(), getUpdateCount(), getDeleteCount(), getNodeCount(), REJECTION_PROBABILITY);
	}

	/**
	 * Simulate stress test.
	 */
	@Test
	public void simulateStressTest() {
		setStressTest(true);
		this.simulateTest(getCycleCount(), getCreationCount(), getReadCount(), getUpdateCount(), getDeleteCount(), getNodeCount(), REJECTION_PROBABILITY);
	}
}
