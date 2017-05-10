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

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.LastWriteWins;
import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.operations.AbstractOperation.OperationType;
import com.cyberfront.crdt.operations.AbstractOperation.StatusType;
import com.cyberfront.crdt.operations.CreateOperation;
import com.cyberfront.crdt.operations.DeleteOperation;
import com.cyberfront.crdt.operations.ReadOperation;
import com.cyberfront.crdt.operations.UpdateOperation;
import com.cyberfront.crdt.unittest.data.AbstractDataType;
import com.cyberfront.crdt.unittest.support.WordFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

// TODO: Auto-generated Javadoc
/**
 * The Class CRDTManager.
 *
 * @param <T> the generic type
 */
public class CRDTManager <T extends AbstractDataType> extends BaseManager<T> implements Comparable<CRDTManager<? extends AbstractDataType>> {

	/** The Constant mapper. */
	private static final ObjectMapper mapper = new ObjectMapper();
	
	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger(CRDTManager.class);

	/** The object. */
	private T object = null;

	/** The crdt. */
	private LastWriteWins crdt;
	
	/** List of operations and the associated metadata this CRDTManager received */
	private Collection<OperationManager<T>> received;

	/**
	 * Instantiates a new CRDT manager.
	 *
	 * @param objectId the object id
	 * @param username the username
	 * @param nodename the nodename
	 * @param objectClass the object class
	 */
	public CRDTManager(String objectId, String username, String nodename, Class<T> objectClass) {
		super(objectId, username, nodename, objectClass);
		this.setCrdt(new LastWriteWins());
	}

	/**
	 * Gets the crdt.
	 *
	 * @return the crdt
	 */
	protected LastWriteWins getCrdt() {
		return crdt;
	}

	/**
	 * Sets the crdt.
	 *
	 * @param crdt the new crdt
	 */
	private void setCrdt(LastWriteWins crdt) {
		this.crdt = crdt;
	}

	/**
	 * Clear.
	 */
	public void clear() {
		this.getCrdt().clear();
		this.setObject(null);
		this.getCrdt().getInvalidOperations().clear();
	}
	
	/**
	 * Checks if is created.
	 *
	 * @return true, if is created
	 */
	public boolean isCreated() {
		return this.getCrdt().isCreated();
	}
	
	/**
	 * Checks if is deleted.
	 *
	 * @return true, if is deleted
	 */
	public boolean isDeleted() {
		return this.getCrdt().isDeleted();
	}

	/**
	 * Deliver.
	 *
	 * @param op the op
	 */
	public void deliver(OperationManager<T> op) {
		switch(op.getStatus()) {
		case APPROVED:
			this.deliver(op.getOperation());
			break;
		case PENDING:
			this.deliver(op.getOperation());
			break;
		case REJECTED:
			this.cancel(op.getOperation());
			break;
		default:
			break;
		}
	}

	private boolean isOwner(OperationManager<T> op) {
		return Executive.getExecutive().getCrdtLookup().get(this.getObjectId()).equals(this.getNodename());
	}
	
	private Collection<OperationManager<T>> deliverCreatePending(OperationManager<T> op, Double pReject) {
		Collection<OperationManager<T>> operations = new ArrayList<>();

		if (StatusType.PENDING != op.getStatus() ||
			OperationType.CREATE != op.getOperation().getType() ||
			!isOwner(op)) {
			return operations;
		}

		OperationManager<T> rejection = new OperationManager<>(op);
		rejection.setStatus(StatusType.REJECTED);
		operations.add(rejection);

		if (WordFactory.getRandom().nextDouble() > pReject || !this.isCreated()) {
			CreateOperation creOp = (CreateOperation) op.getOperation();
			OperationManager<T> create = new OperationManager<T>(this.getObjectId(), this.getUsername(), this.getNodename(), this.getObjectClass(), creOp);
			create.setStatus(StatusType.APPROVED);

			this.deliver(create.getOperation());
			operations.add(create);
		}

		// TODO Remove Before Flight
//		logger.info("\n*** CRDTManager.deliverCreatePending(OperationManager<T> op, Double pReject)");
//		logger.info("    this: " + this.toString());
//		logger.info("    op: " + (null == op ? "null" : op.toString()));
//		logger.info("    pReject: " + (null == pReject ? "null" : pReject.toString()));
//		logger.info("    rejection: " + (null == rejection ? "null" : rejection.toString()));
//		logger.info("    operations: " + (null == operations ? "null" : toString(operations)));

		return operations;
	}
	
	private Collection<OperationManager<T>> deliverReadPending(OperationManager<T> op, Double pReject) {
		Collection<OperationManager<T>> operations = new ArrayList<>();
		
		if (StatusType.PENDING != op.getStatus() ||
			OperationType.READ != op.getOperation().getType() ||
			!isOwner(op)) {
			return operations;
		}

		OperationManager<T> rejection = new OperationManager<>(op);
		rejection.setStatus(StatusType.REJECTED);
		operations.add(rejection);

		if (WordFactory.getRandom().nextDouble() > pReject && this.isCreated() && !this.isDeleted()) {
			ReadOperation readOp = (ReadOperation) op.getOperation();
			OperationManager<T> read = new OperationManager<T>(this.getObjectId(), this.getUsername(), this.getNodename(), this.getObjectClass(), readOp);
			read.setStatus(StatusType.APPROVED);

			this.deliver(read.getOperation());
			operations.add(read);
		}

		// TODO Remove Before Flight
//		logger.info("\n*** CRDTManager.deliverReadPending(OperationManager<T> op, Double pReject)");
//		logger.info("          this: " + this.toString());
//		logger.info("            op: " + (null == op ? "null" : op.toString()));
//		logger.info("       pReject: " + (null == pReject ? "null" : pReject.toString()));
//		logger.info("     rejection: " + (null == rejection ? "null" : rejection.toString()));
//		logger.info("    operations: " + WordFactory.convert(operations));

		return operations;
	}
	
	private Collection<OperationManager<T>> deliverUpdatePending(OperationManager<T> op, Double pReject) {
		Collection<OperationManager<T>> operations = new ArrayList<>();

		if (StatusType.PENDING != op.getStatus() ||
			OperationType.UPDATE != op.getOperation().getType() ||
			!isOwner(op)) {
			return operations;
		}

		OperationManager<T> rejection = new OperationManager<>(op);
		rejection.setStatus(StatusType.REJECTED);

		operations.add(rejection);
		if (WordFactory.getRandom().nextDouble() > pReject) {
			JsonNode source = (null != this.getCrdt().readValue() ? this.getCrdt().readValue() : mapper.createObjectNode());
			this.deliver(op.getOperation());
			JsonNode target = (null != this.getCrdt().readValue() ? this.getCrdt().readValue() : mapper.createObjectNode());
			JsonNode diff = JsonDiff.asJson(source, target);

			if (this.getCrdt().getInvalidOperations().isEmpty() && diff.size() > 0) {
				OperationManager<T> update = new OperationManager<T>(this.getObjectId(), this.getUsername(), this.getNodename(), this.getObjectClass(), new UpdateOperation(diff, Executive.getExecutive().getTimeStamp()));
				update.setStatus(StatusType.APPROVED);

				operations.add(update);
			}
		}

		// TODO Remove Before Flight
//		logger.info("\n*** CRDTManager.deliverUpdatePending(OperationManager<T> op, Double pReject)");
//		logger.info("          this: " + this.toString());
//		logger.info("            op: " + (null == op ? "null" : op.toString()));
//		logger.info("       pReject: " + (null == pReject ? "null" : pReject.toString()));
//		logger.info("     rejection: " + (null == rejection ? "null" : rejection.toString()));
//		logger.info("    operations: " + WordFactory.convert(operations));

		return operations;
	}
	
	private Collection<OperationManager<T>> deliverDeletePending(OperationManager<T> op, Double pReject) {
		Collection<OperationManager<T>> operations = new ArrayList<>();
		
		if (StatusType.PENDING != op.getStatus() ||
			OperationType.DELETE != op.getOperation().getType() ||
			!isOwner(op)) {
			return operations;
		}

		OperationManager<T> rejection = new OperationManager<>(op);
		rejection.setStatus(StatusType.REJECTED);
		operations.add(rejection);

		if (WordFactory.getRandom().nextDouble() > pReject || !this.isCreated()) {
			DeleteOperation delOp = (DeleteOperation) op.getOperation();
			OperationManager<T> delete = new OperationManager<T>(this.getObjectId(), this.getUsername(), this.getNodename(), this.getObjectClass(), delOp);
			delete.setStatus(StatusType.APPROVED);

			this.deliver(delete.getOperation());
			operations.add(delete);
		}

		// TODO Remove Before Flight
//		logger.info("\n*** CRDTManager.deliverDeletePending(OperationManager<T> op, Double pReject)");
//		logger.info("          this: " + this.toString());
//		logger.info("            op: " + (null == op ? "null" : op.toString()));
//		logger.info("       pReject: " + (null == pReject ? "null" : pReject.toString()));
//		logger.info("     rejection: " + (null == rejection ? "null" : rejection.toString()));
//		logger.info("    operations: " + WordFactory.convert(operations));

		return operations;
	}
	
	private Collection<OperationManager<T>> deliverPending(OperationManager<T> op, Double pReject) {
		switch (op.getOperation().getType()) {
		case CREATE:
			return this.deliverCreatePending(op, pReject);
		case READ:
			return this.deliverReadPending(op, pReject);
		case UPDATE:
			return this.deliverUpdatePending(op, pReject);
		case DELETE:
			return this.deliverDeletePending(op, pReject);
		default:
			return new ArrayList<>();
		
		}
	}
	
	public Collection<OperationManager<T>> deliver(OperationManager<T> op, Double pReject, Node node) {
		this.getReceived().add(op);

		// TODO Remove before flight
//		logger.info("\n*** CRDTManager.deliver(OperationManager<T> op, Double pReject)");
//		logger.info("       node: " + (null == node ? "null" : node.toString()));
//		logger.info("       this: " + this.toString());
//		logger.info("         op: " + (null == op ? "null" : op.toString()));
//		logger.info("    pReject: " + (null == pReject ? "null" : pReject.toString()));
		
		switch (op.getStatus()){
		case APPROVED:
			this.deliver(op.getOperation());
			return new ArrayList<>();
		case PENDING:
			if (!this.isOwner(op)) {
				this.deliver(op.getOperation());
				return new ArrayList<>();
			} else {
				return deliverPending(op, pReject);
			}
		case REJECTED:
			this.cancel(op.getOperation());
			return new ArrayList<>();
		default:
			return new ArrayList<>();
		}
	}
	
	/**
	 * Deliver.
	 *
	 * @param op the op
	 */
	private void deliver(AbstractOperation op){
		this.getCrdt().addOperation(op);
		this.setObject(null);
	}

	/**
	 * Cancel.
	 *
	 * @param op the op
	 */
	public void cancel(OperationManager<T> op){
		this.cancel(op.getOperation());
	}

	/**
	 * Cancel.
	 *
	 * @param op the op
	 */
	private void cancel(AbstractOperation op){
		this.getCrdt().remOperation(op);
	}

	/**
	 * Gets the manager.
	 *
	 * @param op the op
	 * @return the manager
	 */
	protected OperationManager<T> getManager(AbstractOperation op) {
		return new OperationManager<>(this.getObjectId(), this.getUsername(), this.getNodename(), this.getObjectClass(), op);
	}
	
	/**
	 * Update object.
	 */
	private void updateObject() {
		JsonNode json = this.getCrdt().readValue();

		if (null != json) {
			try {
				this.setObject(mapper.treeToValue(json, this.getObjectClass()));
			} catch (JsonProcessingException e) {
				logger.error(e);
				for (StackTraceElement el : e.getStackTrace()) {
					logger.error(el);
				}
				this.setObject(null);
				this.getCrdt().getInvalidOperations().clear();
			}
		} else {
			this.setObject(null);
			this.getCrdt().getInvalidOperations().clear();
		}
	}
	
	/**
	 * Process create.
	 *
	 * @param timestamp the timestamp
	 * @return the operation manager
	 * @throws ReflectiveOperationException the reflective operation exception
	 */
	public OperationManager<T> processCreate(long timestamp) throws ReflectiveOperationException {
		return this.processCreate(timestamp, this.getObjectClass().newInstance());
	}

	/**
	 * Process create.
	 *
	 * @param timestamp the timestamp
	 * @param object the object
	 * @return the operation manager
	 */
	public OperationManager<T> processCreate(long timestamp, T object) {
		OperationManager<T> rv = null;

		if (!this.getCrdt().isCreated()) {
			JsonNode source = mapper.createObjectNode();
			JsonNode target = mapper.valueToTree(object);
			JsonNode diff = JsonDiff.asJson(source, target);
			CreateOperation op = new CreateOperation(diff, timestamp);

			rv = this.getManager(op);

			this.deliver(rv);
			this.setObject(null);
			this.getCrdt().getInvalidOperations().clear();
		}

		// TODO Remove before flight
//		logger.info("\n*** CRDTManager.processCreate(long timestamp, T object)");
//		logger.info("    timestamp: " + timestamp);
//		logger.info("    object: " + (null == object ? "null" : object.toString()));
//		logger.info("    rv: " + (null == rv ? "null" : rv.toString()));

		return rv;			
	}
	
	/**
	 * Process read.
	 *
	 * @param timestamp the timestamp
	 * @return the operation manager
	 */
	public OperationManager<T> processRead(long timestamp) {
		OperationManager<T> rv = null;

		if (this.getCrdt().isCreated() && !this.getCrdt().isDeleted()) {
			ReadOperation op = new ReadOperation(timestamp);
			rv = this.getManager(op);
			this.deliver(rv);
		}

		return rv;
	}

	/**
	 * Process update.
	 *
	 * @param timestamp the timestamp
	 * @param pChange the change
	 * @return the operation manager
	 */
	public OperationManager<T> processUpdate(long timestamp, Double pChange) {

		if (!this.isCreated() || this.isDeleted()) {
			return null;
		}

		T obj = this.getObject();
		obj.update(pChange);
		OperationManager<T> rv = this.processUpdate(timestamp, obj);

		// TODO Remove before flight
//		logger.info("\n*** CRDTManager.processUpdate(long timestamp, Double pChange)");
//		logger.info("         this: " + this.toString());
//		logger.info("    timestamp: " + timestamp);
//		logger.info("      pChange: " + pChange);
//		logger.info("          obj: " + (null == obj ? "null" : obj.toString()));

		return rv;
	}
	
	/**
	 * Process update.
	 *
	 * @param timestamp the timestamp
	 * @param object the object
	 * @return the operation manager
	 */
	public OperationManager<T> processUpdate(long timestamp, T object) {
		OperationManager<T> rv = null;
		
		if (this.getCrdt().isCreated() && !this.getCrdt().isDeleted()) {
//			this.setObject(object);
			JsonNode source = this.getCrdt().readValue();
			JsonNode target = mapper.valueToTree(object);
			JsonNode diff = JsonDiff.asJson(source, target);

			if (diff.size() > 0) {
				UpdateOperation op = new UpdateOperation(diff, timestamp);
				rv = this.getManager(op);

				this.deliver(rv);
				this.setObject(null);
				this.getCrdt().getInvalidOperations().clear();
			}
		}
		
		// TODO Remove before flight
//		logger.info("\n*** CRDTManager.processUpdet(long timestamp, T object)");
//		logger.info("    timestamp: " + timestamp);
//		logger.info("    object: " + (null == object ? "null" : object.toString()));
//		logger.info("    rv: " + (null == rv ? "null" : rv.toString()));
		
		return rv;
	}

	/**
	 * Process delete.
	 *
	 * @param timestamp the timestamp
	 * @return the operation manager
	 */
	public OperationManager<T> processDelete(long timestamp) {
		OperationManager<T> rv = null;

		if (this.getCrdt().isCreated() && !this.getCrdt().isDeleted()) {
			DeleteOperation op = new DeleteOperation(timestamp);
			rv = this.getManager(op);
			this.deliver(rv);
			this.setObject(null);
			this.getCrdt().getInvalidOperations().clear();
		}

		return rv;
	}
	
	/**
	 * Gets the object.
	 *
	 * @return the object
	 */
	public T getObject() {
		if (null == this.object) {
			this.updateObject();
		}
		return this.object;
	}

	/**
	 * Sets the object.
	 *
	 * @param object the new object
	 */
	private void setObject(T object) {
		this.object = object;
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
	public int compareTo(CRDTManager<? extends AbstractDataType> o) {
		return super.baseCompare(o);
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj || !(obj instanceof CRDTManager<?>) || !super.equals(obj)) {
			return false;
		}
		
		CRDTManager<?> mgr = (CRDTManager<?>) obj;
		
		return this.getObjectClass().equals(mgr.getObjectClass()) && this.getCrdt().equals(mgr.getCrdt());
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		
		hash = 47 * hash + this.getCrdt().hashCode();
		hash = 53 * hash + this.getObjectClass().hashCode();
		
		return hash;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#getSegment()
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();

		sb.append(super.getSegment() + ",");
		sb.append("\"received\":" + WordFactory.convert(this.getReceived()) + ",");
		sb.append("\"crdt\":" + (null == this.getCrdt() ? "null" : this.getCrdt().toString()) + ",");
		sb.append("\"object\":" + (null == this.object ? "null" : this.object.toString()));
		
		return sb.toString();
	}

	private Collection<OperationManager<T>> getReceived() {
		if (null == this.received) {
			this.received = new TreeSet<>();
		}
		return this.received;
	}
}
