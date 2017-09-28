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
package com.cyberfront.crdt.sample.simlation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.GenericCRDTManager;
import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.operations.AbstractOperation.OperationType;
import com.cyberfront.crdt.operations.OperationManager.StatusType;
import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.support.Support;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.diff.JsonDiff;	// Use this with jsonpatch
// TODO: Auto-generated Javadoc
//import com.flipkart.zjsonpatch.JsonPatch;		// Use this with zjsonpatch
//import com.flipkart.zjsonpatch.JsonDiff;		// Use this with zjsonpatch

/**
 * The Class SimCRDTManager is used to manage a Plain Old Java Object (POJO).  Internally changes are represented as a series of
 * JSON operations but to the external interface, the object type being managed is given by the generic parameter T 
 *
 * @param <T> The type of object this SimCRDTManager is managing
 */
public class SimCRDTManager<T extends AbstractDataType>
	extends GenericCRDTManager<T>
	implements Comparable<SimCRDTManager<T>>, IManager<T> {

	/** The Constant logger. */
//	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SimCRDTManager.class);

	/** List of operations and the associated metadata this SimCRDTManager received */
	private Collection<Message<? extends AbstractDataType>> received;
	
	/** List of operations and the associated metadata this SimCRDTManager sent */
	private Collection<Message<? extends AbstractDataType>> sent;
	
	/** The id of the object this CRDT manager is associated with. */
	private final UUID objectId;
	
	/** The UUID associated with the node which owns this CRDT instance and which is responsible for approving / rejecting operations. */
	private final UUID ownerId;
	
	/** This is a reference to the node which owns this CRDT instance and is responsible for getting messages delivered to this instance. */
	private final UUID managerId;
	
	/**
	 * Instantiates a new CRDT manager.
	 *
	 * @param objectId ID of the object being shadowed with the CRDT
	 * @param ownerNodeId Reference to the node which owns this CRDT manager instance
	 * @param managerNodeId Reference to the node which manages this CRDT manager instance
	 * @param objectClass A Class reference used to transform between JSON and POJO representations of the object being
	 * managed 
	 */
	public SimCRDTManager(UUID objectId, UUID ownerNodeId, UUID managerNodeId, Class<T> objectClass) {
		super(objectClass);
		this.objectId = objectId;
		this.ownerId = ownerNodeId;
		this.managerId = managerNodeId;
	}

	/**
	 * Gets the object identifier
	 *
	 * @return the object identifier
	 */
	@Override
	public UUID getObjectId(){
		return this.objectId;
	}
	
	/**
	 * Gets the identifier of the owner node which is responsible for approving / rejecting operations presented to it which originate from
	 * other nodes
	 *
	 * @return The identifier of the owner node
	 */
	@Override
	public UUID getOwnerNodeID() {
		return this.ownerId;
	}

	/**
	 * Retrieve the list of messages this SimCRDTManager received
	 * 
	 * @return List of message instances this SimCRDTManager received
	 */
	public Collection<Message<? extends AbstractDataType>> getReceived() {
		if (null == this.received) {
			this.received = new ArrayList<>();
		}

		return this.received;
	}

	/**
	 * Retrieve the list of messages this SimCRDTManager sent
	 * 
	 * @return List of message instances this SimCRDTManager sent
	 */
	public Collection<Message<? extends AbstractDataType>> getSent() {
		if (null == this.sent) {
			this.sent = new ArrayList<>();
		}

		return this.sent;
	}

	/**
	 * Generate an operation manager for this CRDT manager to deliver to other nodes.
	 *
	 * @param status Status of the operation
	 * @param op Operation to manage in the resulting operation manager
	 * @return The manager resulting from the production of the new object
	 */
	protected SimOperationManager<T> getManager(StatusType status, AbstractOperation op) {
		return null == op 
				? null 
				: new SimOperationManager<>(status, op, this.getObjectId(), this.getObjectClass());
	}

	/**
	 * Gets the manager node id.
	 *
	 * @return The manager node id
	 */
	public UUID getManagerNodeId() {
		return this.managerId;
	}
	
	/**
	 * Checks if is the CRDT manager is locally managed; that is the owner and manager nodes are the same
	 *
	 * @return true, if is locally managed
	 */
	public boolean isLocallyManaged() {
		return this.getManagerNodeId().equals(this.getOwnerNodeID());
	}
	
	/**
	 * Deliver a operation manager with a PENDING CREATE operation 
	 *
	 * @param mgr Operation Manager to deliver to this CRDT manager
	 * @param pReject Probability of rejecting an update
	 * @return The collection of operation managers resulting from processing the one provided.  This primarily be a list
	 * of no more than two operation, one a REJECT notice and the other an APPROVED notice
	 */
	private Collection<SimOperationManager<T>> deliverCreatePending(SimOperationManager<T> mgr, Double pReject) {
		Collection<SimOperationManager<T>> operations = new ArrayList<>();

		assertTrue(OperationType.CREATE == mgr.getOperation().getType());

		this.push(mgr);
		operations.add(mgr.copy(StatusType.REJECTED));

		if (Support.getRandom().nextDouble() > pReject && this.getCrdt().getInvalidOperations().isEmpty()) {
			operations.add(mgr.mimic(StatusType.APPROVED));
		}
		
		return operations;
	}
	
	/**
	 * Deliver a operation manager with a PENDING READ operation 
	 *
	 * @param mgr Operation Manager to deliver to this CRDT manager
	 * @param pReject Probability of rejecting an update
	 * @return The collection of operation managers resulting from processing the one provided.  This primarily be a list
	 * of no more than two operation, one a REJECT notice and the other an APPROVED notice
	 */
	private Collection<SimOperationManager<T>> deliverReadPending(SimOperationManager<T> mgr, Double pReject) {
		Collection<SimOperationManager<T>> operations = new ArrayList<>();
		
		assertTrue(OperationType.READ == mgr.getOperation().getType());
		
		this.push(mgr);
		operations.add(mgr.copy(StatusType.REJECTED));

		if (Support.getRandom().nextDouble() > pReject && this.getCrdt().getInvalidOperations().isEmpty()) {
			operations.add(mgr.mimic(StatusType.APPROVED));
		}

		return operations;
	}
	
	/**
	 * Deliver a operation manager with a PENDING UPDATE operation 
	 *
	 * @param mgr Operation Manager to deliver to this CRDT manager
	 * @param pReject Probability of rejecting the UPDATE operation
	 * @return The collection of operation managers resulting from processing the one provided.  This primarily be a list
	 * of no more than two operation, one a REJECT notice and the other an APPROVED notice
	 */
	private Collection<SimOperationManager<T>> deliverUpdatePending(SimOperationManager<T> mgr, Double pReject) {
		Collection<SimOperationManager<T>> operations = new ArrayList<>();

		assertTrue(OperationType.UPDATE == mgr.getOperation().getType());
		
		this.push(mgr);
		
		operations.add(mgr.copy(StatusType.REJECTED));

		if (Support.getRandom().nextDouble() > pReject && this.getCrdt().getInvalidOperations().isEmpty()) {
			JsonNode source = (null != this.getCrdt().getDocument() ? this.getCrdt().getDocument() : getMapper().createObjectNode());

			JsonNode target = (null != this.getCrdt().getDocument() ? this.getCrdt().getDocument() : getMapper().createObjectNode());
			JsonNode diff = JsonDiff.asJson(source, target);

			if (0 == this.getInvalidOperationCount() && 0 < diff.size()) {
				operations.add(mgr.mimic(StatusType.APPROVED));
			}
		}

		return operations;
	}
	
	/**
	 * Deliver a operation manager with a PENDING DELETE operation 
	 *
	 * @param mgr Operation Manager to deliver to this CRDT manager
	 * @param pReject Probability of rejecting the delete operation at the manager node
	 * @return The collection of operation managers resulting from processing the one provided.  This primarily be a list
	 * of no more than two operation, one a REJECT notice and the other an APPROVED notice
	 */
	private Collection<SimOperationManager<T>> deliverDeletePending(SimOperationManager<T> mgr, Double pReject) {
		Collection<SimOperationManager<T>> operations = new ArrayList<>();
		
		assertTrue(OperationType.DELETE == mgr.getOperation().getType());
		
		this.push(mgr);
		
		operations.add(mgr.copy(StatusType.REJECTED));

		if (Support.getRandom().nextDouble() > pReject || !this.isCreated() && this.getCrdt().getInvalidOperations().isEmpty()) {
			operations.add(mgr.mimic(StatusType.APPROVED));
		}

		return operations;
	}
	
	/**
	 * Deliver a pending operation manager to a locally manageed CRDT instance 
	 *
	 * @param op Operation Manager to deliver to this CRDT manager
	 * @param pReject Probability of rejecting the operation at the manager node
	 * @return The collection of operation managers resulting from processing the one provided.  This primarily be a list
	 * of no more than two operation, one a REJECT notice and the other an APPROVED notice
	 */
	private Collection<SimOperationManager<T>> deliverPending(SimOperationManager<T> op, Double pReject) {
		Collection<SimOperationManager<T>> rv = null;

		assertEquals(StatusType.PENDING, op.getStatus());
		assertTrue(isLocallyManaged());

		switch (op.getOperation().getType()) {
		case CREATE:
			rv = this.deliverCreatePending(op, pReject);
			break;
		case READ:
			rv = this.deliverReadPending(op, pReject);
			break;
		case UPDATE:
			rv = this.deliverUpdatePending(op, pReject);
			break;
		case DELETE:
			rv = this.deliverDeletePending(op, pReject);
			break;
		default:
			rv = new ArrayList<>();
			break;
		}
		return rv;
	}

	/**
	 * Builds the known messages derived from the operations presented in the collection
	 *
	 * @param mgrs The collection of operations to ensure are applied to the each of the Nodes in the distributed model
	 * @return The collection of messages to deliver the operations to each of the other nodes in the distributed architecture
	 */
	private Collection<Message<? extends AbstractDataType>> buildMessages(Collection<SimOperationManager<T>> mgrs) {
		Collection<Message<? extends AbstractDataType>> rv = new ArrayList<>();
		
		if (null != mgrs) {
			for (SimOperationManager<T> mgr : mgrs) {
				rv.addAll(buildMessages(mgr));
			}
		}

		return rv;
	}
	
	/**
	 * Builds the known messages for a single operation
	 *
	 * @param mgr The operation for which to generate a collection of messages for all of the nodes
	 * @return The collection of messages resulting from the distribution of the single operation given 
	 */
	private Collection<Message<? extends AbstractDataType>> buildMessages(SimOperationManager<T> mgr) {
		Collection<Message<? extends AbstractDataType>> rv = new ArrayList<>();
		
		if (null != mgr) {
			for (Map.Entry<UUID, Node> entry : Executive.getExecutive().getNodes().entrySet()) {
				long timestamp = Executive.getExecutive().getTimestamp() + 
						Support.getRandom().nextInt(65536);
				rv.add(new Message<>(this.getOwnerNodeID(), entry.getKey(), mgr, timestamp));
			}
		}
		
		return rv;
	}

	/**
	 * Push the message given into the CRDT so its operation can be extracted and presented to
	 * actual CRDT
	 * 
	 * @param msg Messsage to process at this node
	 * @param pReject Probability of rejecting the delivered operation if this CRDT manager is locally managed 
	 * @return The collection of messages to deliver to the each of the other nodes and which are derived from 
	 * processing the message delivered in this call to the method
	 */
	public Collection<Message<? extends AbstractDataType>> push(Message<T> msg, Double pReject) {
		SimOperationManager<T> mgr = msg.getManager();
		Collection<Message<? extends AbstractDataType>> rv = null;
		this.getReceived().add(msg);

		switch (mgr.getStatus()){
		case PENDING:
			if (!this.isLocallyManaged()) {
				this.push(mgr);
				rv = new ArrayList<>();
			} else {
				Collection<SimOperationManager<T>> mgrList = this.deliverPending(mgr, pReject);
				rv = this.buildMessages(mgrList);
			}
			break;
		case APPROVED:
		case REJECTED:
			this.push(mgr);
		default:
			rv =  new ArrayList<>();
		}
		
		this.getSent().addAll(rv);

		return rv;
	}

	/**
	 * Generate a CREATE operation and wrap it with a collection of messages such that each operation is delivered to
	 * each of the recipient nodes
	 *
	 * @param status Status of the operation which should be PENDING for operations not generated on locally managed
	 * CRDTs, or APPROVED for those that are
	 * @param timestamp Timestamp for marking the operation
	 * @param object The new object to use as the basis for the CREATE operations
	 * @return The list of messages generated as a result of producing a new CREATE operation which needs to be 
	 * moved to each of the other nodes.
	 */
	public Collection<Message<? extends AbstractDataType>> generateCreate(StatusType status, long timestamp, T object) {
		if (this.getCrdt().isCreated() || this.getCrdt().isDeleted()) {
			return  new ArrayList<>();
		}
		
		SimOperationManager<T> mgr = this.getManager(status, super.generateCreate(timestamp, object));
		Collection<Message<? extends AbstractDataType>> rv = this.buildMessages(mgr);
		this.getSent().addAll(rv);
		return rv;
	}
	
	/**
	 * Generate a READ operation and wrap it with a collection of messages such that each operation is delivered to
	 * each of the recipient nodes
	 *
	 * @param status Status of the operation which should be PENDING for operations not generated on locally managed
	 * CRDTs, or APPROVED for those that are
	 * @param timestamp Timestamp for marking the operation
	 * @return The list of messages generated as a result of producing a new READ operation which needs to be 
	 * moved to each of the other nodes.
	 */
	public Collection<Message<? extends AbstractDataType>> generateRead(StatusType status, long timestamp) {
		if (!this.getCrdt().isCreated() || this.getCrdt().isDeleted()) {
			return new ArrayList<>();
		}
		
		SimOperationManager<T> mgr = this.getManager(status, generateReadOperation(timestamp));
		Collection<Message<? extends AbstractDataType>> rv = this.buildMessages(mgr);

		this.getSent().addAll(rv);

		return rv;
	}

	/**
	 * Generate a UPDATE operation and wrap it with a collection of messages such that each operation is delivered to
	 * each of the recipient nodes
	 *
	 * @param status Status of the operation which should be PENDING for operations not generated on locally managed
	 * CRDTs, or APPROVED for those that are
	 * @param timestamp Timestamp for marking the operation
	 * @param pChange The probability of changing a particular field in the managed object
	 * @return The list of messages generated as a result of producing a new UPDATE operation which needs to be 
	 * moved to each of the other nodes.
	 */
	public Collection<Message<? extends AbstractDataType>> generateUpdate(StatusType status, long timestamp, Double pChange) {
		T obj = this.getObject();
		if (null == obj) {
			return  new ArrayList<>();
		}

		obj.update(pChange);
		return this.generateUpdate(status, timestamp, obj);
	}
	
	/**
	 * Generate a UPDATE operation and wrap it with a collection of messages such that each operation is delivered to
	 * each of the recipient nodes
	 *
	 * @param status Status of the operation which should be PENDING for operations not generated on locally managed
	 * CRDTs, or APPROVED for those that are
	 * @param timestamp Timestamp for marking the operation
	 * @param update The object which is used to generate the difference for the update operation payload
	 * @return The list of messages generated as a result of producing a new UPDATE operation which needs to be 
	 * moved to each of the other nodes.
	 */
	public Collection<Message<? extends AbstractDataType>> generateUpdate(StatusType status, long timestamp, T update) {
		if (!this.getCrdt().isCreated() || this.getCrdt().isDeleted()) {
			return  new ArrayList<>();
		}
		
		SimOperationManager<T> mgr = this.getManager(status, this.generateUpdate(timestamp, update));
		Collection<Message<? extends AbstractDataType>> rv = this.buildMessages(mgr);
		this.getSent().addAll(rv);

		return rv;
	}

	/**
	 * Generate a DELETE operation and wrap it with a collection of messages such that each operation is delivered to
	 * each of the recipient nodes
	 *
	 * @param status Status of the operation which should be PENDING for operations not generated on locally managed
	 * CRDTs, or APPROVED for those that are
	 * @param timestamp Timestamp for marking the operation
	 * @return The list of messages generated as a result of producing a new DELETE operation which needs to be 
	 * moved to each of the other nodes.
	 */
	public Collection<Message<? extends AbstractDataType>> generateDelete(StatusType status, long timestamp) {
		if (!this.getCrdt().isCreated() || this.getCrdt().isDeleted()) {
			return  new ArrayList<>();
		}

		SimOperationManager<T> mgr = this.getManager(status, generateDelete(timestamp));
		Collection<Message<? extends AbstractDataType>> rv = this.buildMessages(mgr);

		this.getSent().addAll(rv);
		
		return rv;
	}
	
	/**
	 * Gets the invalid operation count.
	 *
	 * @return the invalid operation count
	 */
	public int getInvalidOperationCount() {
		return this.getCrdt().getInvalidOperations().size();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SimCRDTManager<T> o) {
		int compId = this.getObjectId().compareTo(o.getObjectId());
		int compNodeId = this.getOwnerNodeID().compareTo(o.getOwnerNodeID());
		int rv = compId != 0 ? compId : compNodeId; 
		
		return rv;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean rv;
		
		if (this == obj) {
			rv = true;
		} else if (null == obj || !(obj instanceof SimCRDTManager) || !super.equals(obj)) {
			rv = false;
		} else {
			@SuppressWarnings("unchecked")
			SimCRDTManager<? extends AbstractDataType> mgr = (SimCRDTManager<? extends AbstractDataType>) obj;
			rv = this.getObjectClass().equals(mgr.getObjectClass()) && this.getCrdt().equals(mgr.getCrdt());
		}
		
		return rv;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		
		hash = 37 * hash + this.getObjectId().hashCode();
		hash = 41 * hash + this.getOwnerNodeID().hashCode();
		hash = 43 * hash + this.getObjectClass().hashCode();
		hash = 47 * hash + this.getObjectClass().hashCode();

		return hash;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#getSegment()
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();

		sb.append(super.getSegment() + ",");
		sb.append("\"received\":" + (null == this.sent ? "null" : Support.convert(this.getSent())) + Support.convert(this.getReceived()) + ",");
		sb.append("\"sent\":" + (null == this.sent ? "null" : Support.convert(this.getSent())) + ",");
		sb.append("\"isLocal\":" + this.isLocallyManaged() + ",");
		sb.append("\"objectId\":\"" + this.getObjectId() + "\",");
		sb.append("\"ownerNodeId\":\"" + this.getOwnerNodeID() + "\",");
		sb.append("\"managerNodeId\":\"" + this.getManagerNodeId() + "\"");
		
		return sb.toString();
	}
	
	/**
	 * Check operation validity.
	 */
	public void checkOperationValidity() {
		boolean created = this.isCreated();
		boolean deleted = this.isDeleted();
		JsonNode document = this.getCrdt().getDocument();

		if (created && !deleted && null == document) {
			logger.info(Executive.getExecutive().toString());
			throw new IllegalStateException("Created, non-deleted value should not be null, but is: " + this.toString());
		} else if (deleted && null != document) {
			logger.info(Executive.getExecutive().toString());
			throw new IllegalStateException("Deleted value should be null, but is not: " + this.toString());
		}
	}
	
	/**
	 * Check message consistency.
	 */
	public void checkMessageConsistency() {
		Message.checkConsistency(this.getReceived());
		Message.checkConsistency(this.getSent());
	}
	
	private String buildReport() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"createCountDelivered\":" + this.getCreateCountDelivered() + ",");
		sb.append("\"readCountDelivered\":" + this.getReadCountDelivered() + ",");
		sb.append("\"updateCountDelivered\":" + this.getUpdateCountDelivered() + ",");
		sb.append("\"deleteCountDelivered\":" + this.getDeleteCountDelivered() + ",");
		
		sb.append("\"approvedCountDelivered\":" + this.getApprovedCountDelivered() + ",");
		sb.append("\"pendingCountDelivered\":" + this.getPendingCountDelivered() + ",");
		sb.append("\"rejectedCountDelivered\":" + this.getRejectedCountDelivered() + ",");
		sb.append("\"totalDeliveryCount\":" + this.getCountDelivered() + ",");

		sb.append("\"createAddCount\":" + this.getCreateCountAdded() + ",");
		sb.append("\"readAddCount\":" + this.getReadCountAdded() + ",");
		sb.append("\"updateAddCount\":" + this.getUpdateCountAdded() + ",");
		sb.append("\"deleteAddCount\":" + this.getDeleteCountAdded() + ",");
		sb.append("\"totalAddCount\":" + this.getCrdt().getAddCount() + ",");
		
		sb.append("\"createRemCount\":" + this.getCreateCountRemoved() + ",");
		sb.append("\"readRemCount\":" + this.getReadCountRemoved() + ",");
		sb.append("\"updateRemCount\":" + this.getUpdateCountRemoved() + ",");
		sb.append("\"deleteRemCount\":" + this.getDeleteCountRemoved() + ",");
		sb.append("\"totalRemCount\":" + this.getCrdt().getRemCount() + ",");
		
		sb.append("\t\"nodeId\":\"" + this.getOwnerNodeID().toString() + "\",");
		sb.append("\t\"objectId\":\"" + this.getObjectId().toString() + "\"");
		
//		sb.append("\"executive\":" + Executive.getExecutive().toString());
		sb.append("}");
		
		return sb.toString();
	}

	/**
	 * Gets the creates the count delivered.
	 *
	 * @return the creates the count delivered
	 */
	public long getCreateCountDelivered() {
		return Message.filterMessages(this.getReceived(), OperationType.CREATE, true).size();
	}
	
	/**
	 * Gets the read count delivered.
	 *
	 * @return the read count delivered
	 */
	public long getReadCountDelivered() {
		return Message.filterMessages(this.getReceived(), OperationType.READ, true).size();
	}
	
	/**
	 * Gets the update count delivered.
	 *
	 * @return the update count delivered
	 */
	public long getUpdateCountDelivered() {
		return Message.filterMessages(this.getReceived(), OperationType.UPDATE, true).size();
	}
	
	/**
	 * Gets the delete count delivered.
	 *
	 * @return the delete count delivered
	 */
	public long getDeleteCountDelivered() {
		return Message.filterMessages(this.getReceived(), OperationType.DELETE, true).size();
	}
	
	public long getApprovedCountDelivered() {
		return Message.filterMessages(this.getReceived(), StatusType.APPROVED, true).size();
	}
	
	/**
	 * Gets the pending count delivered.
	 *
	 * @return the pending count delivered
	 */
	public long getPendingCountDelivered() {
		return Message.filterMessages(this.getReceived(), StatusType.PENDING, true).size();
	}
	
	/**
	 * Gets the rejected count delivered.
	 *
	 * @return the rejected count delivered
	 */
	public long getRejectedCountDelivered() {
		return Message.filterMessages(this.getReceived(), StatusType.REJECTED, true).size();
	}
	
	/**
	 * Gets the count delivered.
	 *
	 * @return the count delivered
	 */
	public long getCountDelivered() {
		return this.getReceived().size();
	}
	
	/**
	 * Gets the creates the count added.
	 *
	 * @return the creates the count added
	 */
	public long getCreateCountAdded() {
		return filterOperationsByType(this.getCrdt().copyAddSet(), OperationType.CREATE, true).size();
	}
	
	/**
	 * Gets the read count added.
	 *
	 * @return the read count added
	 */
	public long getReadCountAdded() {
		return filterOperationsByType(this.getCrdt().copyAddSet(), OperationType.READ, true).size();
	}
	
	/**
	 * Gets the update count added.
	 *
	 * @return the update count added
	 */
	public long getUpdateCountAdded() {
		return filterOperationsByType(this.getCrdt().copyAddSet(), OperationType.UPDATE, true).size();
	}
	
	/**
	 * Gets the delete count added.
	 *
	 * @return the delete count added
	 */
	public long getDeleteCountAdded() {
		return filterOperationsByType(this.getCrdt().copyAddSet(), OperationType.DELETE, true).size();
	}
	
	/**
	 * Gets the creates the count removed.
	 *
	 * @return the creates the count removed
	 */
	public long getCreateCountRemoved() {
		return filterOperationsByType(this.getCrdt().copyRemSet(), OperationType.CREATE, true).size();
	}
	
	/**
	 * Gets the read count removed.
	 *
	 * @return the read count removed
	 */
	public long getReadCountRemoved() {
		return filterOperationsByType(this.getCrdt().copyRemSet(), OperationType.READ, true).size();
	}
	
	/**
	 * Gets the update count removed.
	 *
	 * @return the update count removed
	 */
	public long getUpdateCountRemoved() {
		return filterOperationsByType(this.getCrdt().copyRemSet(), OperationType.UPDATE, true).size();
	}
	
	/**
	 * Gets the delete count removed.
	 *
	 * @return the delete count removed
	 */
	public long getDeleteCountRemoved() {
		return filterOperationsByType(this.getCrdt().copyRemSet(), OperationType.DELETE, true).size();
	}

	/**
	 * Validate delivery count.
	 */
	private void validateDeliveryCount() {
		long deliveryCount = this.getCountDelivered();
		long addCount = this.getCrdt().getAddCount();
		long remCount = this.getCrdt().getRemCount();
		long opCount = addCount + remCount;
		
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"deliveryCount\":" + deliveryCount + ",");
		sb.append("\"addCount\":" + addCount + ",");
		sb.append("\"remCount\":" + remCount + ",");
		sb.append("\"opCount\":" + opCount);
		sb.append("}");
		
		if (deliveryCount != addCount + remCount) {
			logger.info(this.buildReport());
			throw new IllegalStateException("Inconsistent Delivery Count: " + sb.toString());
		} 
	}
	
	/**
	 * Validate operation count.
	 *
	 * @param type the type
	 */
	private void validateOperationCount(OperationType type) {
		long messageCount = Message.filterMessages(this.getReceived(), type, true).size();
		long addCount = filterOperationsByType(this.getCrdt().copyAddSet(), type, true).size();
		long remCount = filterOperationsByType(this.getCrdt().copyRemSet(), type, true).size();
		long opCount = addCount + remCount;

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"type\":\"" + type.toString() + "\",");
		sb.append("\"messageCount\":" + messageCount + ",");
		sb.append("\"addCount\":" + addCount + ",");
		sb.append("\"remCount\":" + remCount + ",");
		sb.append("\"opCount\":" + opCount);
		sb.append("}");
		
		if (messageCount != opCount) {
			logger.info(this.buildReport());
			throw new IllegalStateException("Inconsistent Delivery / Operation Count: " + sb.toString());
		} 
	}
	
	/**
	 * Validate create operation count.
	 */
	private void validateCreateOperationCount() {
		this.validateOperationCount(OperationType.CREATE);
	}
	
	/**
	 * Validate read operation count.
	 */
	private void validateReadOperationCount() {
		this.validateOperationCount(OperationType.READ);
	}
	
	/**
	 * Validate update operation count.
	 */
	private void validateUpdateOperationCount() {
		this.validateOperationCount(OperationType.UPDATE);
	}
	
	/**
	 * Validate delete operation count.
	 */
	private void validateDeleteOperationCount() {
		this.validateOperationCount(OperationType.DELETE);
	}
	
	/**
	 * Validate operation count.
	 */
	private void validateOperationCount() {
		this.validateCreateOperationCount();
		this.validateReadOperationCount();
		this.validateUpdateOperationCount();
		this.validateDeleteOperationCount();
	}
	
	/**
	 * Validate rejection count.
	 */
	private void validateRejectionCount() {
		long rejectionCount = Message.filterMessages(this.getReceived(), StatusType.REJECTED, true).size();
		long remCount = this.getCrdt().getRemCount();

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"rejectionCount\":" + rejectionCount + ",");
		sb.append("\"remCount\":" + remCount);
		sb.append("}");
		
		if (rejectionCount != remCount) {
			logger.info(this.buildReport());
			throw new IllegalStateException("Inconsistent Rejection Operation Count: " + sb.toString());
		} 
	}
	
	/**
	 * Check message count.
	 */
	public void checkMessageCount() {
		this.validateDeliveryCount();
		this.validateOperationCount();
		this.validateRejectionCount();
	}

	public static Collection<AbstractOperation> filterOperationsByType(Collection<AbstractOperation> opList, OperationType opType, boolean criteria) {
	return opList.stream()
			.filter(op -> (opType == op.getType()) == criteria)
			.collect(Collectors.toList());
	}
}
