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
import com.flipkart.zjsonpatch.JsonDiff;		// Use this with zjsonpatch
//import com.flipkart.zjsonpatch.JsonPatch;		// Use this with zjsonpatch
//import com.github.fge.jsonpatch.diff.JsonDiff;	// Use this with jsonpatch

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
	
	/** The object id. */
	private final UUID objectId;
	
	/** This is a reference to the node which owns this CRDT instance. */
	private final UUID ownerId;
	
	/** This is a reference to the node which owns this CRDT instance. */
	private final UUID managerId;
	
	/**
	 * Instantiates a new CRDT manager.
	 *
	 * @param objectId the object id
	 * @param username the username
	 * @param ownerNodeId Reference to the node which owns this CRDT manager instance
	 * @param objectClass the object class
	 */
	public SimCRDTManager(UUID objectId, UUID ownerNodeId, UUID managerNodeId, Class<T> objectClass) {
		super(objectClass);
		this.objectId = objectId;
		this.ownerId = ownerNodeId;
		this.managerId = managerNodeId;
	}

	/**
	 * Gets the object id.
	 *
	 * @return the object id
	 */
	@Override
	public UUID getObjectId(){
		return this.objectId;
	}
	
	/**
	 * Gets the node identifier.
	 *
	 * @return the node identifier
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
	 * Gets the manager.
	 *
	 * @param op the op
	 * @return the manager
	 */
	protected SimOperationManager<T> getManager(StatusType status, AbstractOperation op) {
		return null == op 
				? null 
				: new SimOperationManager<>(status, op, this.getObjectId(), this.getObjectClass());
	}

	public UUID getManagerNodeId() {
		return this.managerId;
	}
	
	public boolean isLocallyManaged() {
		return this.getManagerNodeId().equals(this.getOwnerNodeID());
	}
	
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
	 * Process create.
	 *
	 * @param timestamp the timestamp
	 * @param object the object
	 * @return the operation manager
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
	 * Process read.
	 *
	 * @param timestamp the timestamp
	 * @return the operation manager
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
	 * Process update.
	 *
	 * @param timestamp the timestamp
	 * @param pChange the change
	 * @return the operation manager
	 */
	public Collection<Message<? extends AbstractDataType>> generateUpdate(StatusType status, long timestamp, Double pChange) {
		T obj = this.getObject();
		if (null == obj) {
			return  new ArrayList<>();
		}

		obj.update(pChange);
		SimOperationManager<T> mgr = this.generateUpdate(status, timestamp, obj);
		Collection<Message<? extends AbstractDataType>> rv = this.buildMessages(mgr);

		this.getSent().addAll(rv);

		return rv;
	}
	
	/**
	 * Process update.
	 *
	 * @param timestamp the timestamp
	 * @param update the object
	 * @return the operation manager
	 */
	public SimOperationManager<T> generateUpdate(StatusType status, long timestamp, T update) {
		return (!this.getCrdt().isCreated() || this.getCrdt().isDeleted())
				? null
				: this.getManager(status, generateUpdate(timestamp, update));
	}

	/**
	 * Process delete.
	 *
	 * @param timestamp the timestamp
	 * @return the operation manager
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

	public long getCreateCountDelivered() {
		return Message.filterReceived(this.getReceived(), OperationType.CREATE, true).size();
	}
	
	public long getReadCountDelivered() {
		return Message.filterReceived(this.getReceived(), OperationType.READ, true).size();
	}
	
	public long getUpdateCountDelivered() {
		return Message.filterReceived(this.getReceived(), OperationType.UPDATE, true).size();
	}
	
	public long getDeleteCountDelivered() {
		return Message.filterReceived(this.getReceived(), OperationType.DELETE, true).size();
	}
	
	public long getApprovedCountDelivered() {
		return Message.filterReceived(this.getReceived(), StatusType.APPROVED, true).size();
	}
	
	public long getPendingCountDelivered() {
		return Message.filterReceived(this.getReceived(), StatusType.PENDING, true).size();
	}
	
	public long getRejectedCountDelivered() {
		return Message.filterReceived(this.getReceived(), StatusType.REJECTED, true).size();
	}
	
	public long getCountDelivered() {
		return this.getReceived().size();
	}
	
	public long getCreateCountAdded() {
		return filterOperationsByType(this.getCrdt().copyAddSet(), OperationType.CREATE, true).size();
	}
	
	public long getReadCountAdded() {
		return filterOperationsByType(this.getCrdt().copyAddSet(), OperationType.READ, true).size();
	}
	
	public long getUpdateCountAdded() {
		return filterOperationsByType(this.getCrdt().copyAddSet(), OperationType.UPDATE, true).size();
	}
	
	public long getDeleteCountAdded() {
		return filterOperationsByType(this.getCrdt().copyAddSet(), OperationType.DELETE, true).size();
	}
	
	public long getCreateCountRemoved() {
		return filterOperationsByType(this.getCrdt().copyRemSet(), OperationType.CREATE, true).size();
	}
	
	public long getReadCountRemoved() {
		return filterOperationsByType(this.getCrdt().copyRemSet(), OperationType.READ, true).size();
	}
	
	public long getUpdateCountRemoved() {
		return filterOperationsByType(this.getCrdt().copyRemSet(), OperationType.UPDATE, true).size();
	}
	
	public long getDeleteCountRemoved() {
		return filterOperationsByType(this.getCrdt().copyRemSet(), OperationType.DELETE, true).size();
	}

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
	
	private void validateOperationCount(OperationType type) {
		long messageCount = Message.filterReceived(this.getReceived(), type, true).size();
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
	
	private void validateCreateOperationCount() {
		this.validateOperationCount(OperationType.CREATE);
	}
	
	private void validateReadOperationCount() {
		this.validateOperationCount(OperationType.READ);
	}
	
	private void validateUpdateOperationCount() {
		this.validateOperationCount(OperationType.UPDATE);
	}
	
	private void validateDeleteOperationCount() {
		this.validateOperationCount(OperationType.DELETE);
	}
	
	private void validateOperationCount() {
		this.validateCreateOperationCount();
		this.validateReadOperationCount();
		this.validateUpdateOperationCount();
		this.validateDeleteOperationCount();
	}
	
	private void validateRejectionCount() {
		long rejectionCount = Message.filterReceived(this.getReceived(), StatusType.REJECTED, true).size();
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
