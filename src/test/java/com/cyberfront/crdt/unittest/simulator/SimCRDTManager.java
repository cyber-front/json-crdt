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

import com.cyberfront.crdt.CRDTManager;
import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.operations.AbstractOperation.OperationType;
import com.cyberfront.crdt.operations.CreateOperation;
import com.cyberfront.crdt.operations.DeleteOperation;
import com.cyberfront.crdt.operations.OperationManager.StatusType;
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
 * The Class SimCRDTManager.
 *
 * @param <T> the generic type
 */
public class SimCRDTManager <T extends AbstractDataType>
	extends CRDTManager
	implements Comparable<SimCRDTManager<? extends AbstractDataType>>, IManager<T> {

	/** The Constant mapper. */
	private static final ObjectMapper mapper = new ObjectMapper();
	
	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger(SimCRDTManager.class);

	/** The object. */
	private T object = null;

	/** List of operations and the associated metadata this SimCRDTManager received */
	private Collection<SimOperationManager<T>> received;
	
	/** The object class. */
	private Class<T> objectClass;
	
	/** The object id. */
	private String objectId;
	
	/** The username. */
	private String username;
	
	/** The nodename. */
	private String nodename;

	/**
	 * Instantiates a new CRDT manager.
	 *
	 * @param objectId the object id
	 * @param username the username
	 * @param nodename the nodename
	 * @param objectClass the object class
	 */
	public SimCRDTManager(String objectId, String username, String nodename, Class<T> objectClass) {
		this.setObjectId(objectId);
		this.setUsername(username);
		this.setNodename(nodename);
		this.setObjectClass(objectClass);
	}

	/**
	 * Gets the object class.
	 *
	 * @return the object class
	 */
	@Override
	public Class<T> getObjectClass() {
		return this.objectClass;
	}

	/**
	 * Gets the object id.
	 *
	 * @return the object id
	 */
	@Override
	public String getObjectId(){
		return this.objectId;
	}
	
	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	@Override
	public String getUsername(){
		return this.username;
	}
	
	/**
	 * Gets the nodename.
	 *
	 * @return the nodename
	 */
	@Override
	public String getNodename() {
		return this.nodename;
	}

	private Collection<SimOperationManager<T>> getReceived() {
		if (null == this.received) {
			this.received = new TreeSet<>();
		}

		return this.received;
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
	 * Gets the manager.
	 *
	 * @param op the op
	 * @return the manager
	 */
	protected SimOperationManager<T> getManager(StatusType status, AbstractOperation op) {
		SimOperationManager<T> rv = new SimOperationManager<>(status, op, this.getObjectId(), this.getUsername(), this.getNodename(), this.getObjectClass());
		return rv ;
	}
	
	/**
	 * Sets the object class.
	 *
	 * @param objectClass the new object class
	 */
	private void setObjectClass(Class<T> objectClass) {
		this.objectClass = objectClass;
	}
	
	/**
	 * Sets the object id.
	 *
	 * @param id the new object id
	 */
	private void setObjectId(String id) {
		this.objectId = id;
	}
	
	/**
	 * Sets the username.
	 *
	 * @param user the new username
	 */
	private void setUsername(String user) {
		this.username = user;
	}
	
	/**
	 * Sets the nodename.
	 *
	 * @param source the new nodename
	 */
	private void setNodename(String source) {
		this.nodename = source;
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
	 * Clear.
	 */
	public void clear() {
		super.clear();
		this.setObject(null);
	}

	private boolean isOwner(SimOperationManager<T> op) {
		boolean rv = Executive.getExecutive().getCrdtLookup().get(this.getObjectId()).equals(this.getNodename());
		return rv;
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
	
	private Collection<SimOperationManager<T>> deliverCreatePending(SimOperationManager<T> op, Double pReject) {
		Collection<SimOperationManager<T>> operations = new ArrayList<>();

		if (StatusType.PENDING != op.getStatus() ||
			OperationType.CREATE != op.getOperation().getType() ||
			!isOwner(op)) {
			return operations;
		}

		SimOperationManager<T> rejection = new SimOperationManager<>(op);
		rejection.setStatus(StatusType.REJECTED);
		operations.add(rejection);

		if (WordFactory.getRandom().nextDouble() > pReject || !this.isCreated()) {
			CreateOperation creOp = (CreateOperation) op.getOperation();
			SimOperationManager<T> create = new SimOperationManager<T>(StatusType.APPROVED, creOp, this.getObjectId(), this.getUsername(), this.getNodename(), this.getObjectClass());

			this.deliverOperation(create);
			operations.add(create);
		}

		return operations;
	}
	
	private Collection<SimOperationManager<T>> deliverReadPending(SimOperationManager<T> op, Double pReject) {
		Collection<SimOperationManager<T>> operations = new ArrayList<>();
		
		if (StatusType.PENDING != op.getStatus() ||
			OperationType.READ != op.getOperation().getType() ||
			!isOwner(op)) {
			return operations;
		}

		SimOperationManager<T> rejection = new SimOperationManager<>(op);
		rejection.setStatus(StatusType.REJECTED);
		operations.add(rejection);

		if (WordFactory.getRandom().nextDouble() > pReject && this.isCreated() && !this.isDeleted()) {
			ReadOperation readOp = (ReadOperation) op.getOperation();
			SimOperationManager<T> read = new SimOperationManager<T>(StatusType.APPROVED, readOp, this.getObjectId(), this.getUsername(), this.getNodename(), this.getObjectClass());

			this.deliverOperation(read);
			operations.add(read);
		}

		return operations;
	}
	
	private Collection<SimOperationManager<T>> deliverUpdatePending(SimOperationManager<T> op, Double pReject) {
		Collection<SimOperationManager<T>> operations = new ArrayList<>();

		if (StatusType.PENDING != op.getStatus() ||
			OperationType.UPDATE != op.getOperation().getType() ||
			!isOwner(op)) {
			return operations;
		}

		SimOperationManager<T> rejection = new SimOperationManager<>(op);
		rejection.setStatus(StatusType.REJECTED);

		operations.add(rejection);
		if (WordFactory.getRandom().nextDouble() > pReject) {
			JsonNode source = (null != this.getCrdt().readValue() ? this.getCrdt().readValue() : mapper.createObjectNode());

			this.deliverOperation(op);

			JsonNode target = (null != this.getCrdt().readValue() ? this.getCrdt().readValue() : mapper.createObjectNode());
			JsonNode diff = JsonDiff.asJson(source, target);

			if (this.getCrdt().getInvalidOperations().isEmpty() && diff.size() > 0) {
				UpdateOperation updateOp = new UpdateOperation(diff, Executive.getExecutive().getTimeStamp());
				SimOperationManager<T> update = new SimOperationManager<T>(StatusType.APPROVED, updateOp, this.getObjectId(), this.getUsername(), this.getNodename(), this.getObjectClass());

				operations.add(update);
			}
		}

		return operations;
	}
	
	private Collection<SimOperationManager<T>> deliverDeletePending(SimOperationManager<T> op, Double pReject) {
		Collection<SimOperationManager<T>> operations = new ArrayList<>();
		
		if (StatusType.PENDING != op.getStatus() ||
			OperationType.DELETE != op.getOperation().getType() ||
			!isOwner(op)) {
			return operations;
		}

		SimOperationManager<T> rejection = new SimOperationManager<>(op);
		rejection.setStatus(StatusType.REJECTED);
		operations.add(rejection);

		if (WordFactory.getRandom().nextDouble() > pReject || !this.isCreated()) {
			DeleteOperation delOp = (DeleteOperation) op.getOperation();
			SimOperationManager<T> delete = new SimOperationManager<T>(StatusType.APPROVED, delOp, this.getObjectId(), this.getUsername(), this.getNodename(), this.getObjectClass());

			this.deliverOperation(delete);
			operations.add(delete);
		}

		return operations;
	}
	
	private Collection<SimOperationManager<T>> deliverPending(SimOperationManager<T> op, Double pReject) {
		Collection<SimOperationManager<T>> rv = null;
		
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

	protected void deliverOperation(SimOperationManager<T> op) {
		this.getReceived().add(op);
		super.deliver(op);
	}
	
	public Collection<SimOperationManager<T>> deliver(SimOperationManager<T> op, Double pReject) {
		Collection<SimOperationManager<T>> rv = null;
		this.getReceived().add(op);
		this.setObject(null);
		
		switch (op.getStatus()){
		case PENDING:
			if (!this.isOwner(op)) {
				this.deliverOperation(op);
				rv = new ArrayList<>();
			} else {
				rv = deliverPending(op, pReject);
			}
		case APPROVED:
		case REJECTED:
			this.deliverOperation(op);
		default:
			rv =  new ArrayList<>();
		}
		
		return rv;
	}

	/**
	 * Process create.
	 *
	 * @param timestamp the timestamp
	 * @return the operation manager
	 * @throws ReflectiveOperationException the reflective operation exception
	 */
	public SimOperationManager<T> generateNewCreate(StatusType status, long timestamp) throws ReflectiveOperationException {
		return this.generateNewCreate(status, timestamp, this.getObjectClass().newInstance());
	}

	/**
	 * Process create.
	 *
	 * @param timestamp the timestamp
	 * @param object the object
	 * @return the operation manager
	 */
	public SimOperationManager<T> generateNewCreate(StatusType status, long timestamp, T object) {
		SimOperationManager<T> rv = null;

		if (!this.getCrdt().isCreated()) {
			JsonNode source = mapper.createObjectNode();
			JsonNode target = mapper.valueToTree(object);
			JsonNode diff = JsonDiff.asJson(source, target);
			CreateOperation op = new CreateOperation(diff, timestamp);

			rv = this.getManager(status, op);

			this.setObject(null);
		}

		return rv;			
	}
	
	/**
	 * Process read.
	 *
	 * @param timestamp the timestamp
	 * @return the operation manager
	 */
	public SimOperationManager<T> generateNewRead(StatusType status, long timestamp) {
		SimOperationManager<T> rv = null;

		if (this.getCrdt().isCreated() && !this.getCrdt().isDeleted()) {
			ReadOperation op = new ReadOperation(timestamp);
			rv = this.getManager(status, op);
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
	public SimOperationManager<T> generateNewUpdate(StatusType status, long timestamp, Double pChange) {

		if (!this.isCreated() || this.isDeleted()) {
			return null;
		}

		T obj = this.getObject();
		obj.update(pChange);
		SimOperationManager<T> rv = this.generateNewUpdate(status, timestamp, obj);

		return rv;
	}
	
	/**
	 * Process update.
	 *
	 * @param timestamp the timestamp
	 * @param object the object
	 * @return the operation manager
	 */
	public SimOperationManager<T> generateNewUpdate(StatusType status, long timestamp, T object) {
		SimOperationManager<T> rv = null;
		
		if (this.getCrdt().isCreated() && !this.getCrdt().isDeleted()) {
			JsonNode source = this.getCrdt().readValue();
			JsonNode target = mapper.valueToTree(object);
			JsonNode diff = JsonDiff.asJson(source, target);

			if (diff.size() > 0) {
				UpdateOperation op = new UpdateOperation(diff, timestamp);
				rv = this.getManager(status, op);
				this.setObject(null);
			}
		}

		return rv;
	}

	/**
	 * Process delete.
	 *
	 * @param timestamp the timestamp
	 * @return the operation manager
	 */
	public SimOperationManager<T> generateNewDelete(StatusType status, long timestamp) {
		SimOperationManager<T> rv = null;

		if (this.getCrdt().isCreated() && !this.getCrdt().isDeleted()) {
			DeleteOperation op = new DeleteOperation(timestamp);
			rv = this.getManager(status, op);
			this.setObject(null);
		}

		return rv;
	}
	
	/**
	 * Gets the invalid operation count.
	 *
	 * @return the invalid operation count
	 */
	public int getInvalidOperationCount() {
		int rv = this.getCrdt().getInvalidOperations().size();
		return rv;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SimCRDTManager<? extends AbstractDataType> o) {
		int compId = this.getObjectId().compareTo(o.getObjectId());
		int compUsername = this.getUsername().compareTo(getUsername());
		int compNodename = this.getNodename().compareTo(o.getNodename());
		int rv = compId != 0 ? compId : compNodename != 0 ? compNodename : compUsername; 
		
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
		} else if (null == obj || !(obj instanceof SimCRDTManager<?>) || !super.equals(obj)) {
			rv = false;
		} else {
			SimCRDTManager<?> mgr = (SimCRDTManager<?>) obj;
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
		hash = 41 * hash + this.getNodename().hashCode();
		hash = 43 * hash + this.getUsername().hashCode();
		hash = 47 * hash + this.getObjectClass().hashCode();
		hash = 53 * hash + this.getObjectClass().hashCode();

		return hash;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#getSegment()
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();

		sb.append(super.getSegment() + ",");
		sb.append("\"objectClass\":\"" + this.getObjectClass().getName() + "\",");
		sb.append("\"objectId\":\"" + this.getObjectId() + "\",");
		sb.append("\"username\":\"" + this.getUsername() + "\",");
		sb.append("\"nodename\":\"" + this.getNodename() + "\",");
		sb.append("\"received\":" + WordFactory.convert(this.getReceived()) + ",");
		sb.append("\"object\":" + (null == this.object ? "null" : this.object.toString()));
		
//		this.validateRecived();
		
		return sb.toString();
	}
	
//	private void validateRecived() {
//		long added = this.getCrdt().getAddCount();
//		long removed  = this.getCrdt().getRemCount();
//		long received  = null == this.received ? 0 : this.received.size();
//		
//		if ((added + removed) > received) {
//			logger.info("    added: " + added);
//			logger.info("    removed: " + removed);
//			logger.info("    received: " + received);
//			logger.info("    this: " + this.toString());
//		}
//		
//		assert((added + removed) <= received);
//	}
}
