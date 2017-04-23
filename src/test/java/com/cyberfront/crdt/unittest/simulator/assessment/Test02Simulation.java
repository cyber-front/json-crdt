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

/**
 * This test module will assess the behavior of the CRDT through the use of a simulated distributed
 * node controller and update delivery framework.  These are contained in the package
 * com.cyberfront.crdt.unitest.simulator.
 */
public class Test02Simulation {
	
	/** Logger to use when displaying state information */
	private static final Logger logger = LogManager.getLogger(Test02Simulation.class);

	/** The ObjectMapper used to translate between JSON and POJO's */
	private static final ObjectMapper mapper = new ObjectMapper();

	/** Number of test iterations to perform on tests related to internodal synchronization quality */
	private static final long TRIAL_COUNT = 4;
	
	/** Number of creation operations to perform on tests related to internodal synchronization quality */
	private static final long CREATION_COUNT = 64;
	
	/** Number of read operations to perform on tests related to internodal synchronization quality */
	private static final long READ_COUNT = 64;
	
	/** Number of updated operations to perform on tests related to internodal synchronization quality */
	private static final long UPDATE_COUNT = 64;
	
	/** Number of delete operations to perform on tests related to internodal synchronization quality */
	private static final long DELETE_COUNT = 16;
	
	/** Number of nodes to simulate in tests related to internodal synchronization quality */
	private static final long NODE_COUNT = 32;
	
	/** Number of test iterations to perform on tests related to internodal synchronization performance and stability */
	private static final long STRESS_TRIAL_COUNT = 32;
	
	/** Number of creation operations to perform on tests related to internodal synchronization performance and stability */
	private static final long STRESS_CREATION_COUNT = 1024;
	
	/** Number of read operations to perform on tests related to internodal synchronization performance and stability */
	private static final long STRESS_READ_COUNT = 128;
	
	/** Number of update operations to perform on tests related to internodal synchronization performance and stability */
	private static final long STRESS_UPDATE_COUNT = 512;
	
	/** Number of delete operations to perform on tests related to internodal synchronization performance and stability */
	private static final long STRESS_DELETE_COUNT = 128;
	
	/** Number of nodes to simulate in tests related to internodal synchronization performance and stability */
	private static final long STRESS_NODE_COUNT = 256;
	
	/** Probability of rejecting an update or delete event once it reaches the "owner" node */
	private static final double REJECTION_PROBABILITY = 0.10d;

	/** Flag to indicate the type of testing to perform */ 
	private static boolean stressTest = false;
	
	/**
	 * Retrieve the value of the stressTest flag, which is used to determine the focus of the
	 * particular test run.
	 * @return The value of the stressTest flag 
	 */
	public static boolean isStressTest() {
		return stressTest;
	}

	/**
	 * Set the value of the stressTest flag to the given value
	 * @param value The new value to set the stressTest flag
	 */
	private static void setStressTest(boolean value) {
		stressTest = value;
	}
	
	/**
	 * Gets the trial count based on the stress test state.
	 *
	 * @return the trial count
	 */
	private static long getTrialCount() {
		return isStressTest() ? STRESS_TRIAL_COUNT : TRIAL_COUNT;
	}

	/**
	 * Gets the creation count based on the stress test state.
	 *
	 * @return the creation count
	 */
	private static long getCreationCount() {
		return isStressTest() ? STRESS_CREATION_COUNT : CREATION_COUNT;
	}

	/**
	 * Gets the read count based on the stress test state.
	 *
	 * @return the read count
	 */
	private static long getReadCount() {
		return isStressTest() ? STRESS_READ_COUNT : READ_COUNT;
	}

	/**
	 * Gets the update count based on the stress test state.
	 *
	 * @return the update count
	 */
	private static long getUpdateCount() {
		return isStressTest() ? STRESS_UPDATE_COUNT : UPDATE_COUNT;
	}

	/**
	 * Gets the delete count based on the stress test state.
	 *
	 * @return the delete count
	 */
	private static long getDeleteCount() {
		return isStressTest() ? STRESS_DELETE_COUNT : DELETE_COUNT;
	}

	/**
	 * Gets the node count based on the stress test state.
	 *
	 * @return the node count
	 */
	private static long getNodeCount() {
		return isStressTest() ? STRESS_NODE_COUNT : NODE_COUNT;
	}

	/**
	 * Assess data element count based on the stress test state.
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
	 * Determine if the number of CRDTManagers is proper in each Node 
	 */
	private void assessDataElementContent() {
		logger.info("        Test01Simulation.assessDataElementContent()");

		for (Map.Entry<String, Node> entry : Executive.getExecutive().getNodes().entrySet()) {

			Node baseNode = entry.getValue();
			assertNotNull("baseNode found to be null", baseNode);

			for (Entry<String, CRDTManager<? extends AbstractDataType>> baseEntry : entry.getValue().getDatastore().entrySet()) {
				assessDataElementContent(baseEntry.getValue());

			}
		}
	}

	/**
	 * For a given CRDT instance, ensure all of the nodes have the same CRDT and that
	 * the state is consistent across all of the nodes.
	 *
	 * @param <T> The type the CRDTManager manages
	 * @param crdt The CRDT which should be compared for synchronization errors with the corresponding CRDTs
	 * in all other Node instances.
	 */
	private <T extends AbstractDataType> void assessDataElementContent(CRDTManager<T> crdt) {
		assertNotNull("baseCRDT found to be null", crdt);

		AbstractDataType baseValue = crdt.getObject();
		if (crdt.isDeleted()) {
			if (null != baseValue) {
				System.out.print(crdt.toString());
			}
			assertNull("Deleted baseValue should be null, but isn't", baseValue);
		} else {
			if (null == baseValue) {
				System.out.print(crdt.toString());
			}
		}

		for (Map.Entry<String, Node> compEntry : Executive.getExecutive().getNodes().entrySet()) {
			Node compNode = compEntry.getValue();
			assertNotNull("compNode found to be null", compNode);

			CRDTManager<? extends AbstractDataType> compCRDT = compNode.getDatastore(crdt.getObjectId());
			assertNotNull("compCRDT found to be null", compCRDT);

			AbstractDataType compValue = compCRDT.getObject();
			assertTrue("compCRDT.isDeleted (" + compCRDT.isDeleted() + ") / baseCRDT.isDeleted (" + crdt.isDeleted() + ") deleted flag mismatch: ", compCRDT.isDeleted() == crdt.isDeleted() );

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
				sb.append("{\n\t{\"baseCRDT\":" + crdt.toString() + ",");
				sb.append("\n\t \"compCRDT\":" + compCRDT.toString() + ",");
				sb.append("\n\t \"baseValue\":" + baseValue.toString() + ",");
				sb.append("\n\t \"compValue\":" + compValue.toString() + "}\n}");
				
				assertTrue(sb.toString(), diff.size() == 0);
			}
		}
	}

	/**
	 * Determine the number of invalid operations contained in the CRDT.  Invalid operations are those which 
	 * cannot be processed because the underlying JSON document is in a fundamentally inconsistent state from the
	 * update which is being applied.  In such cases, those updates are ignored, but are set aside for later
	 * assessment, namely to be counted ensuring consistency across the set of CRDT's.
	 */
	private void assessInvalidOperations() {
		logger.info("        Test01Simulation.assessInvalidOperations()");
		
		Node baseNode = Executive.getExecutive().pickNode();
		assertNotNull("baseNode found to be null", baseNode);

		for (Entry<String, CRDTManager<? extends AbstractDataType>> baseEntry : baseNode.getDatastore().entrySet()) {
			AbstractDataType val = baseEntry.getValue().getObject();

			if (!baseEntry.getValue().isCreated() || baseEntry.getValue().isDeleted()) {
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
	 * Perform the assessments on the simulation run based on the type of test being performed.
	 * All assessments will ensure the consistency of the CRDT count across the nodes and will
	 * determine, based on examination of a single node, whether there are any invalid operations.
	 * If the test is not performing a detailed qualitative assessment of the CRDT, then an
	 * assessment of the synchronization state of the CRDTs will also be performed across all
	 * nodes.  This last is time consuming.
	 */
	private void assessSimulation() {
		assessDataElementCount();
		assessInvalidOperations();
		if (!isStressTest()) {
			assessDataElementContent();
		}
	}
	
	/**
	 * Run the simulation with the given parameters.
	 *
	 * @param trialCount The number of iterations to perform for this test
	 * @param createCount The number of creation operations to perform for this test
	 * @param readCount The number of read operations to perform for this test
	 * @param updateCount The number of update operations to perform for this test
	 * @param deleteCount The number of delete operations to perform for this test
	 * @param nodeCount The number of nodes to simulate in this test
	 * @param rejectProbability The probability the authoritative element will reject an update or delete operation
	 */
	private void simulateTest(long trialCount, long createCount, long readCount, long updateCount, long deleteCount, long nodeCount, double rejectProbability) {
		logger.info("\n** simulateTest: {\"trialCount\":" + trialCount +
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
		
		for (long trial=0; trial<trialCount; ++trial) {
			logger.info("   simulateTest: " + crud + "; trial " + (trial+1) + " of " + trialCount + ".");
			
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
	 * This test will perform a qualitative assessment of the ability to perform create
	 * operations on a CRDT and have those operations propagate across all the nodes in
	 * the simulated distributed environment. 
	 */
	@Test
	public void simulateCreate() {
		setStressTest(false);
		this.simulateTest(getTrialCount(), getCreationCount(), 0, 0, 0, getNodeCount(), 0.0);
	}

	/**
	 * This test will perform a qualitative assessment of the ability to perform create and read
	 * operations on a CRDT and have those operations propagate across all the nodes in
	 * the simulated distributed environment. 
	 */
	@Test
	public void simulateRead() {
		setStressTest(false);
		this.simulateTest(getTrialCount(), getCreationCount(), getReadCount(), 0, 0, getNodeCount(), 0.0);
	}

	/**
	 * This test will perform a qualitative assessment of the ability to perform create, read and update
	 * operations on a CRDT and have those operations propagate across all the nodes in
	 * the simulated distributed environment. 
	 */
	@Test
	public void simulateUpdate() {
		setStressTest(false);
		this.simulateTest(getTrialCount(), getCreationCount(), getReadCount(), getUpdateCount(), 0, getNodeCount(), REJECTION_PROBABILITY);
	}

	/**
	 * This test will perform a qualitative assessment of the ability to perform create, read, update and delete
	 * operations on a CRDT and have those operations propagate across all the nodes in
	 * the simulated distributed environment. 
	 */
	@Test
	public void simulateDelete() {
		setStressTest(false);
		this.simulateTest(getTrialCount(), getCreationCount(), getReadCount(), getUpdateCount(), getDeleteCount(), getNodeCount(), REJECTION_PROBABILITY);
	}

	/**
	 * This test will perform a behavioral and stability assessment of the ability to perform create, read and update
	 * operations on a CRDT and have those operations propagate across all the nodes in
	 * the simulated distributed environment. 
	 */
	@Test
	public void simulateStressTest() {
		setStressTest(true);
		this.simulateTest(getTrialCount(), getCreationCount(), getReadCount(), getUpdateCount(), getDeleteCount(), getNodeCount(), REJECTION_PROBABILITY);
	}
}
