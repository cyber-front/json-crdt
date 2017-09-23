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
package com.cyberfront.crdt.unittest.simulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.sample.simlation.Executive;
import com.cyberfront.crdt.sample.simlation.Node;
import com.cyberfront.crdt.sample.simlation.SimCRDTManager;
import com.cyberfront.crdt.unittest.data.AssessmentSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonDiff;		// Use this with zjsonpatch
//import com.github.fge.jsonpatch.diff.JsonDiff;	// Use this with jsonpatch

/**
 * This test module will assess the behavior of the CRDT through the use of a simulated distributed
 * node controller and update delivery framework.  These are contained in the package
 * com.cyberfront.crdt.unitest.simulator.
 */
public class Test01Simulation {
	public static class SimulationTest extends AssessmentSupport {
		/** Logger to use when displaying state information */
		private static final Logger logger = LogManager.getLogger(Test01Simulation.SimulationTest.class);
		
		/** Number of creation operations to perform on tests related to internodal synchronization quality */
		private static final long CREATE_COUNT = 256;
		
		/** Number of read operations to perform on tests related to internodal synchronization quality */
		private static final long READ_COUNT = 512;
		
		/** Number of updated operations to perform on tests related to internodal synchronization quality */
		private static final long UPDATE_COUNT = 1024;
		
		/** Number of delete operations to perform on tests related to internodal synchronization quality */
		private static final long DELETE_COUNT = 32;
		
		/** Number of nodes to simulate in tests related to internodal synchronization quality */
		private static final long NODE_COUNT = 4;
		
		/** Probability of rejecting an update or delete event once it reaches the "owner" node */
		private static final double REJECTION_PROBABILITY = 0.10d;
		
		/** Probability of rejecting an update or delete event once it reaches the "owner" node */
		private static final double UPDATE_PROBABILITY = 0.20d;
		
		/** Flag used to assess whether each node has the same number of elements */
		private static final boolean ASSESS_COUNT_CONSISTENCY = true;
		
		/** Flag used to assess whether each node has the same elements, where corresponding elements have the same value
		 * as that of the owner node for a particular CRDT
		 */
		private static final boolean ASSESS_CONTENT_CONSISTENCY = true;
		
		/** Flag used to assess whether the each CRDT operation count is consistent */
		private static final boolean ASSESS_OPERATION_COUNT_CONSISTENCY = true;
		
		/**
		 * Flag used to assess whether the CRDT is valid.  When used it will determine whether any invalid operations
		 * occurred, and also check to make sure each CRDT has exactly one create operation and at most one delete operation.
		 */
		private static final boolean ASSESS_VALIDITY = true;
		
		/** The number of times to perform a create operation during the simulation */
		private long createCount;
		
		/** The number of times to perform a read operation during the simulation */
		private long readCount;

		/** The number of times to perform an update operation during the simulation */
		private long updateCount;

		/** The number of times to perform a delete operation during the simulation */
		private long deleteCount;

		/** The number of nodes to create */
		private long nodeCount;

		/** Probability of rejecting an PENDING operation */
		private double rejectionProbability;

		/** Probability of rejecting an updating a field during an update operations */
		private double updateProbability;

		/** Flag set to check the number of CRDT objects is the same on all nodes */
		private boolean assessCountConsistency;

		/** Flag set to check the content consistency of CRDT objects on all nodes */
		private boolean assessContentConsistency;

		/** Flag used to assess whether the each CRDT operation count is consistent */
		private boolean assessOperationCountConsistency;

		/** Flag set to check the content consistency of CRDT objects on all nodes */
		private boolean assessValidity;

		/**
		 * Default constructor which sets all of the fields to the defaults values specified 
		 * in the corresponding constant values.
		 */
		public SimulationTest() {
			super();
			this.setCreateCount(CREATE_COUNT);
			this.setDeleteCount(DELETE_COUNT);
			this.setNodeCount(NODE_COUNT);
			this.setReadCount(READ_COUNT);
			this.setUpdateCount(UPDATE_COUNT);
			this.setRejectionProbability(REJECTION_PROBABILITY);
			this.setUpdateProbability(UPDATE_PROBABILITY);
			this.setAssessCountConsistency(ASSESS_COUNT_CONSISTENCY);
			this.setAssessValidity(ASSESS_VALIDITY);
			this.setAssessContentConsistency(ASSESS_CONTENT_CONSISTENCY);
			this.setAssessOperationCountConsistency(ASSESS_OPERATION_COUNT_CONSISTENCY);
		}

		/**
		 * Constructor to explicitly set all of the fields to the values given in the constructor arguments below
		 * @param createCount Number of times to perform a create operation
		 * @param readCount Number of times to perform a read operation
		 * @param updateCount Number of times to perform an update operation
		 * @param deleteCount Number of times to perform a delete operation
		 * @param nodeCount Number of nodes to create
		 * @param rejectionProbability Probability of rejecting a PENDING operation 
		 * @param updateProbability Probability of changing a field during an update operation
		 * @param trialCount Number of trials to perform
		 * @param abbreviatedFactor Divisor for adjusting counts when doing abbreviated testing
		 * @param stressedFactor Multiplier for sdjusting counts when doing stress testing
		 * @param abbreviated Flag to indicate whether to do abbreviated testing
		 * @param stressed Flag to indicate whether to to stress testing
		 * @param assessCountConsistency Flag to check the object count consistency between nodes
		 * @param assessContentConsistency Flag to check the object value consistency between nodes
		 * @param assessValidity Flag to check the validity of the CRDT values at each node
		 */
		public SimulationTest(long createCount, long readCount, long updateCount, long deleteCount, long nodeCount, double rejectionProbability, double updateProbability, long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed, boolean assessCountConsistency, boolean assessContentConsistency, boolean assessValidity, boolean assessOperationCountConsistency) {
			super(trialCount, abbreviatedFactor, stressedFactor, abbreviated, stressed);
			this.setCreateCount(createCount);
			this.setReadCount(readCount);
			this.setUpdateCount(updateCount);
			this.setDeleteCount(deleteCount);
			this.setNodeCount(nodeCount);
			this.setRejectionProbability(rejectionProbability);
			this.setUpdateProbability(updateProbability);
			this.setAssessCountConsistency(assessCountConsistency);
			this.setAssessContentConsistency(assessContentConsistency);
			this.setAssessValidity(assessValidity);
			this.setAssessOperationCountConsistency(assessOperationCountConsistency);
		}

		/**
		 * Get the number of create operations to perform
		 * @return The number of create operations to perform
		 */
		public long getCreateCount() {
			return this.createCount * this.getStressedFactor() / this.getAbbreviatedFactor();
		}

		/**
		 * Get the number of read operations to perform
		 * @return The number of read operations to perform
		 */
		public long getReadCount() {
			return readCount * this.getStressedFactor() / this.getAbbreviatedFactor();
		}

		/**
		 * Get the number of update operations to perform
		 * @return The number of update operations to perform
		 */
		public long getUpdateCount() {
			return updateCount * this.getStressedFactor() / this.getAbbreviatedFactor();
		}

		/**
		 * Get the number of delete operations to perform
		 * @return The number of delete operations to perform
		 */
		public long getDeleteCount() {
			return deleteCount * this.getStressedFactor() / this.getAbbreviatedFactor();
		}

		/**
		 * Get the number of nodes to use in the simulation
		 * @return The number of nodes to use in the simulation
		 */
		public long getNodeCount() {
			return nodeCount * this.getStressedFactor() / this.getAbbreviatedFactor();
		}

		/**
		 * Get the probability of rejecting a PENDING operation
		 * @return The probability of rejecting a PENDING operation
		 */
		public double getRejectionProbability() {
			return rejectionProbability;
		}

		/**
		 * Get the probability of changing a field during an update operation
		 * @return The probability of changing a field during an update operation
		 */
		public double getUpdateProbability() {
			return updateProbability;
		}

		/**
		 * Return the value of the flag for assessing the count consistency 
		 * @return The flag for assessing the count consistency
		 */
		public boolean isAssessCountConsistency() {
			return assessCountConsistency;
		}

		/**
		 * Return the value of the flag for assessing the content consistency
		 * @return The flag for assessing the content consistency
		 */
		public boolean isAssessContentConsistency() {
			return assessContentConsistency;
		}

		/**
		 * Return the value of the flag for assessing CRDT validity
		 * @return The flag for assessing CRDT validity
		 */
		public boolean isAssessValidity() {
			return assessValidity;
		}

		public boolean isAssessOperationCountConsistency() {
			return assessOperationCountConsistency;
		}

		/**
		 * Set the base number of create operations to perform in a given simulation run 
		 * @param createCount Number of create operations to perform in a given simulation run
		 */
		public void setCreateCount(long createCount) {
			this.createCount = createCount;
		}

		/**
		 * Set the base number of read operations to perform in a given simulation run 
		 * @param readCount Number of read operations to perform in a given simulation run
		 */
		public void setReadCount(long readCount) {
			this.readCount = readCount;
		}

		/**
		 * Set the base number of update operations to perform in a given simulation run 
		 * @param updateCount Number of update operations to perform in a given simulation run
		 */
		public void setUpdateCount(long updateCount) {
			this.updateCount = updateCount;
		}

		/**
		 * Set the base number of delete operations to perform in a given simulation run 
		 * @param deleteCount Number of delete operations to perform in a given simulation run
		 */
		public void setDeleteCount(long deleteCount) {
			this.deleteCount = deleteCount;
		}

		/**
		 * Set the base number of nodes to manage during a simulation run 
		 * @param nodeCount Base number of nodes to manage during a simulation run
		 */
		public void setNodeCount(long nodeCount) {
			this.nodeCount = nodeCount;
		}

		/**
		 * Set the probability of rejecting a PENDING operation
		 * @param rejectionProbability The probability of rejecting a PENDING operation
		 */
		public void setRejectionProbability(double rejectionProbability) {
			this.rejectionProbability = rejectionProbability;
		}

		/**
		 * Set the probability of updating a field during an update operation
		 * @param rejectionProbability The probability of updating a field during an update operation
		 */
		public void setUpdateProbability(double updateProbability) {
			this.updateProbability = updateProbability;
		}

		/**
		 * Set the flag which determines whether or not to perform count consistency assessments
		 * @param assessCountConsistency The new value for the flag which determines whether or not to perform count consistency assessments
		 */
		public void setAssessCountConsistency(boolean assessCountConsistency) {
			this.assessCountConsistency = assessCountConsistency;
		}

		/**
		 * Set the flag which determines whether or not to perform content consistency assessments
		 * @param assessContentConsistency The new value for the flag which determines whether or not to perform content consistency assessments
		 */
		public void setAssessContentConsistency(boolean assessContentConsistency) {
			this.assessContentConsistency = assessContentConsistency;
		}

		/**
		 * Set the flag which determines whether or not to perform validity assessments
		 * @param assessValidity The new value for the flag which determines whether or not to perform validity assessments
		 */
		public void setAssessValidity(boolean assessValidity) {
			this.assessValidity = assessValidity;
		}

		public void setAssessOperationCountConsistency(boolean assessOperationCountConsistency) {
			this.assessOperationCountConsistency = assessOperationCountConsistency;
		}
		
		/**
		 * Perform the count consistency check to ensure each node has the same number of elements, which should
		 * be equal to the total number of CreateOperations which were performed.
		 */
		private void assessCountConsistency() {
			logger.info("        Test01Simulation.assessCountConsistency()");
			
			for (Map.Entry<UUID, Node> entry : Executive.getExecutive().getNodes().entrySet()) {
				
				UUID id = entry.getKey();
				Node node = entry.getValue();
				long count = node.getDatastore().size();

				StringBuilder sb = new StringBuilder();

				if (this.getCreateCount() != count) {
					sb.append("CRDT count mismatch on node \"");
					sb.append(id);
					sb.append("\"; expected: ");
					sb.append(String.valueOf(this.getCreateCount()));
					sb.append("; found: ");
					sb.append(String.valueOf(count));
					
					logger.info("{\"node-id\":\"" + node.getId().toString() + "\",");
					logger.info("\"executive\":" + Executive.getExecutive().toString() + "}");

					assertTrue(sb.toString(), this.getCreateCount() == count);
				}
			}
		}
		
		/**
		 * Determine if the number of CRDTManagers is proper in each Node 
		 */
		private void assessContentConsistency() {
			logger.info("        Test01Simulation.assessContentConsistency()");

			Executive.getExecutive().checkOperationValidity();
			
			for (Map.Entry<UUID, Node> entry : Executive.getExecutive().getNodes().entrySet()) {
				Node baseNode = entry.getValue();
				assertNotNull("baseNode found to be null", baseNode);

				for (Entry<UUID, SimCRDTManager<? extends AbstractDataType>> baseEntry : entry.getValue().getDatastore().entrySet()) {
					SimCRDTManager<? extends AbstractDataType> crdt = baseEntry.getValue();
					if (crdt.getOwnerNodeID().equals(baseNode.getId())) {
						assessContentConsistency(baseEntry.getValue());
					}
				}
			}
		}
		
		private void assessOperationCountConsistency() {
			logger.info("        Test01Simulation.assessOperationCountConsistency()");
			
			Executive.getExecutive().checkMessageConsistency();
			Executive.getExecutive().checkMessageCount();
		}

		/**
		 * For a given CRDT instance, ensure all of the nodes have the same CRDT and that
		 * the state is consistent across all of the nodes.
		 *
		 * @param <T> The type the CRDTManager manages
		 * @param crdt The CRDT which should be compared for synchronization errors with the corresponding CRDTs
		 * in all other Node instances.
		 */
		private <T extends AbstractDataType> void assessContentConsistency(SimCRDTManager<T> crdt) {
			long addCount = crdt.getCrdt().getAddCount();
			long remCount = crdt.getCrdt().getRemCount();
			long opCount = crdt.getCrdt().getOpsSet().size();
			
			assertTrue("crdt has unmatch remove operations: " + crdt.toString(), opCount == addCount - remCount);
			
			// If all of the operations have been rejected, then just return 
			if (opCount == 0) {
				return;
			}
			
			AbstractDataType baseValue = crdt.getObject();

			for (Map.Entry<UUID, Node> compEntry : Executive.getExecutive().getNodes().entrySet()) {
				Node compNode = compEntry.getValue();
				assertNotNull("compNode found to be null", compNode);

				SimCRDTManager<? extends AbstractDataType> compCRDT = compNode.getDatastore().get(crdt.getObjectId());
				assertNotNull("compCRDT found to be null", compCRDT);

				AbstractDataType compValue = compCRDT.getObject();
				assertTrue("compCRDT.isDeleted (" + compCRDT.isDeleted() + ") / baseCRDT.isDeleted (" + crdt.isDeleted() + ") deleted flag mismatch: ", compCRDT.isDeleted() == crdt.isDeleted() );

				if (compCRDT.isDeleted()) {
					assertNull("Deleted compValue should be null, but isn't", compValue);
				} else {
					assertNotNull("Undeleted compValue should not be null, but is", compValue);

					JsonNode source = this.getMapper().valueToTree(baseValue);
					JsonNode target = this.getMapper().valueToTree(compValue);
					JsonNode diff = JsonDiff.asJson(source, target);
					assertNotNull("diff found to be null", diff);

					StringBuilder sb = new StringBuilder();
					sb.append("Value mismatch discovered in between synchronized objects\n");
					sb.append("{\"basevalue\":" + crdt.toString() + ",\"compValue\":" + compCRDT.toString() + "}");
					assertTrue(sb.toString(), diff.size() == 0);
				}
			}

			return;
		}

		/**
		 * Determine the number of invalid operations contained in the CRDT.  Invalid operations are those which 
		 * cannot be processed because the underlying JSON document is in a fundamentally inconsistent state from the
		 * update which is being applied.  In such cases, those updates are ignored, but are set aside for later
		 * assessment, namely to be counted ensuring consistency across the set of CRDT's.
		 */
		private void assessValidity() {
			logger.info("        Test01Simulation.assessValidity()");
			
			Node baseNode = Executive.getExecutive().pickNode();
			assertNotNull("baseNode found to be null", baseNode);

			for (Entry<UUID, SimCRDTManager<? extends AbstractDataType>> baseEntry : baseNode.getDatastore().entrySet()) {
				AbstractDataType val = baseEntry.getValue().getObject();
				boolean created = baseEntry.getValue().isCreated();
				boolean deleted = baseEntry.getValue().isDeleted();
				
				String errMsg = (!created || deleted) == (null == val)
						? "\nWRONG"
						: "\n{\"" + baseEntry.getKey() + "\":" + baseEntry.getValue() + "}";

				if (!created) {
					assertNull("CRDT has no create operations but is not null" + errMsg, val);
				} else if (deleted) {
					if (null != val) {
						logger.info(Executive.getExecutive().toString());
					}
					
					assertNull("CRDT was deleted but is not null" + errMsg, val);
				} else {
					assertNotNull("CRDT is created and not deleted, but is null" + errMsg, val);
				}

				int invalidOperations = baseEntry.getValue().getInvalidOperationCount();
				
				if (invalidOperations > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append("\n*** Residual invalid operations detected:\n");
					sb.append("{\"Count\":" + baseEntry.getValue().getInvalidOperationCount() + ",");
					sb.append("\"NodeID\":\"" + baseNode.getId() + "\",");
					sb.append("\"ObjectId\":\"" + baseEntry.getValue().getObjectId().toString() + "\",");
					sb.append("\"crdt\":" + baseEntry.getValue().toString() + "}");
					logger.info(sb.toString());
				}
				
				assertEquals("Residual operations remain", 0, invalidOperations);
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
			if (this.isAssessValidity()) {
				this.assessValidity();
			}
			
			if (this.isAssessCountConsistency()) {
				this.assessCountConsistency();
			}

			if (this.isAssessContentConsistency()) {
				this.assessContentConsistency();
			}
			
			if (this.isAssessOperationCountConsistency()) {
				this.assessOperationCountConsistency();
			}
		}
		
		/**
		 * Run the simulation with the parameters specified in this instance and assess the outcome of the run 
		 */
		public void test() {
			logger.info("\n** SimulationTest: {\"trialCount\":" + this.getTrialCount() +
							",\"createCount\":" + this.getCreateCount() + 
							",\"readCount\":" + this.getReadCount() + 
							",\"updateCount\":" + this.getUpdateCount() + 
							",\"deleteCount\":" + this.getDeleteCount() + 
							",\"nodeCount\":" + this.getNodeCount() + 
							",\"rejectProbability\":" + this.getRejectionProbability() + 
							",\"updateProbability\":" + this.getUpdateProbability() + 
							"}");
			Executive executive = Executive.getExecutive();
			String crud = "";
			
			crud += createCount > 0 ? 'C' : 'x';
			crud += readCount > 0 ? 'R' : 'x';
			crud += updateCount > 0 ? 'U' : 'x';
			crud += deleteCount > 0 ? 'D' : 'x';
			
			for (long trial=0; trial<this.getTrialCount(); ++trial) {
				logger.info("\n   simulateTest: " + crud + "; trial " + (trial+1) + " of " + this.getTrialCount() + ".");
				
				executive.clear();
				executive.setCreateCount(this.getCreateCount());
				executive.setReadCount(this.getReadCount());
				executive.setUpdateCount(this.getUpdateCount());
				executive.setDeleteCount(this.getDeleteCount());
				executive.setNodeCount(this.getNodeCount());
				executive.setRejectProbability(this.getRejectionProbability());
				executive.setUpdateProbability(this.getUpdateProbability());
				
				executive.execute();
				
				assessSimulation();
			}
			logger.info("   SUCCESS");
		}

//	public static Collection<AbstractOperation> filterOperationsByTimestamp(Collection<AbstractOperation> opList, long timestamp, boolean criteria) {
//		return opList.stream()
//				.filter(op -> (timestamp <= op.getTimeStamp()) == criteria)
//				.collect(Collectors.toList());
//	}
	}

	/**
	 * This test will perform a qualitative assessment of the ability to perform create
	 * operations on a CRDT and have those operations propagate across all the nodes in
	 * the simulated distributed environment. 
	 */
	@Test
	public void simulateCreate() {
		SimulationTest test = new SimulationTest();
		test.setReadCount(0);
		test.setUpdateCount(0);
		test.setDeleteCount(0);
		test.setRejectionProbability(0.0);
		test.test();
	}

	/**
	 * This test will perform a qualitative assessment of the ability to perform create and read
	 * operations on a CRDT and have those operations propagate across all the nodes in
	 * the simulated distributed environment. 
	 */
	@Test
	public void simulateRead() {
		SimulationTest test = new SimulationTest();
		test.setUpdateCount(0);
		test.setDeleteCount(0);
		test.setRejectionProbability(0.0);
		test.test();
	}

	/**
	 * This test will perform a qualitative assessment of the ability to perform create, read and update
	 * operations on a CRDT and have those operations propagate across all the nodes in
	 * the simulated distributed environment. 
	 */
	@Test
	public void simulateUpdate() {
		SimulationTest test = new SimulationTest();
		test.setDeleteCount(0);
		test.test();
	}

	/**
	 * This test will perform a qualitative assessment of the ability to perform create, read, update and delete
	 * operations on a CRDT and have those operations propagate across all the nodes in
	 * the simulated distributed environment. 
	 */
	@Test
	public void simulateDelete() {
		SimulationTest test = new SimulationTest();
		test.test();
	}

	/**
	 * This test will perform a behavioral and stability assessment of the ability to perform create, read and update
	 * operations on a CRDT and have those operations propagate across all the nodes in
	 * the simulated distributed environment. 
	 */
	@Test
	public void simulateStressTest() {
		SimulationTest test = new SimulationTest();
		test.setStressed(false);
		test.setAssessContentConsistency(false);
		test.test();
	}
}
