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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.LastWriteWins;
import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.operations.CreateOperation;
import com.cyberfront.crdt.operations.DeleteOperation;
import com.cyberfront.crdt.operations.ReadOperation;
import com.cyberfront.crdt.operations.UpdateOperation;
import com.cyberfront.crdt.unittest.data.AbstractDataType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

/**
 * This is a manager class for the LastWriteWins CRDT.  It references a specific Java type which can be processed to / from JSON objects
 *
 * @param <T> The type of object being managed
 */
public class CRDTManager<T extends AbstractDataType> extends BaseManager<T> implements Comparable<CRDTManager<? extends AbstractDataType>> {

	/** This is the ObjectMapper used to transform the managed object and convert it to / from JSON */
	private static final ObjectMapper mapper = new ObjectMapper();
	
	/** This is used to generate log entries for the class */
	private static final Logger logger = LogManager.getLogger(CRDTManager.class);

	/** This is the result of running the current set of operations in the LastWritWins CRDT.  This memoizes the 
	 * operations so, as long as the operations remain unchanged, the object can be referenced directly */
	private T object = null;

	/** The list of timestamped operations associated with the object */
	private LastWriteWins crdt;

	/**
	 * Instantiates a new CRDT manager.
	 *
	 * @param objectId A unique identifier for this CRDT Manager
	 * @param user The user which notionally "owns" this CRDT
	 * @param source The node in a notionally distributed architecture wh
	 * @param objectClass the object class
	 */
	public CRDTManager(String objectId, String username, String nodename, Class<T> objectClass) {
		super(objectId, username, nodename, objectClass);
		this.setCrdt(new LastWriteWins());
	}

	/**
	 * Retrieve the CRDT instance this CRDTManager is managing
	 *
	 * @return The CRDT instance this CRDTManager is managing
	 */
	protected LastWriteWins getCrdt() {
		return crdt;
	}

	/**
	 * Establish the CRDT instance this CRDTManager is managing
	 *
	 * @param New CRDT to manage
	 */
	private void setCrdt(LastWriteWins crdt) {
		this.crdt = crdt;
	}

	/**
	 * Clear all of the operations associated with the CRDT this instance manages
	 */
	public void clear() {
		this.getCrdt().clear();
		this.setObject(null);
		this.getCrdt().getInvalidOperations().clear();
	}
	
	/**
	 * This checks to see if the CRDT contains a CreateOperation
	 *
	 * @return True if and only if this CRDT contains a CreateOperation
	 */
	public boolean isCreated() {
		return this.getCrdt().isCreated();
	}
	
	/**
	 * This checks to see if the CRDT contains a DeleteOperation
	 *
	 * @return True if and only if this CRDT contains a DeleteOperation
	 */
	public boolean isDeleted() {
		return this.getCrdt().isDeleted();
	}

	/**
	 * Deliver an OperationManager to this CRDTManager by delivering the embedded AbstractOperation instance to the 
	 * CRDT this instance manages
	 *
	 * @param op The OperationManager containing the operation to deliver to the CRDT this instance manages
	 */
	public void deliver(OperationManager<T> op){
		this.deliver(op.getOperation());
	}

	/**
	 * Deliver an AbstractOperation to the CRDT this instance manages
	 *
	 * @param op The AbstractOperation to deliver to the CRDT this instance manages
	 */
	private void deliver(AbstractOperation op){
		this.getCrdt().addOperation(op);
		this.setObject(null);
	}

	/**
	 * Cancel an OperationManager to this CRDTManager by canceling the embedded AbstractOperation instance to the 
	 * CRDT this instance manages
	 *
	 * @param op The OperationManager containing the operation to cancel from the CRDT this instance manages
	 */
	public void cancel(OperationManager<T> op){
		this.cancel(op.getOperation());
	}

	/**
	 * Cancel an AbstractOperation to the CRDT this instance manages
	 *
	 * @param op The AbstractOperation to cancel from the CRDT this instance manages
	 */
	private void cancel(AbstractOperation op){
		this.getCrdt().remOperation(op);
	}

	/**
	 * Generate and return an OperationManager instance given a AbstractOperation
	 *
	 * @param op The operation to use as the basis to generate the OperationManager instance
	 * @return The OperationManager instance built from the give AbstractOperation instance
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
	 * This method generates a CreateOperation and embeds it in the returned OperationManager.  The object managed is
	 * generated as a starting base for the sequence of operations. 
	 *
	 * @param timestamp The timestamp corresponding to the time the object was created
	 * @return The OperationManager which contains the CreateOperation generated
	 * @throws ReflectiveOperationException when the initial object cannot be generated
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
			JsonNode json = JsonDiff.asJson(mapper.createObjectNode(), mapper.valueToTree(object));
			CreateOperation op = new CreateOperation(json, timestamp);

			rv = this.getManager(op);
			this.deliver(rv);
			this.setObject(null);
			this.getCrdt().getInvalidOperations().clear();
		}

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
	 * Generate an update of the currently managed object with the given probability of change applied to 
	 * each mutable field in the object
	 *
	 * @param timestamp The timestamp the update is to occur
	 * @param pChange Probability of changing a mutable field in the managed object
	 * @return The OperationManager which wraps the UpdateOperation
	 */
	public OperationManager<T> processUpdate(long timestamp, Double pChange) {
		if (!this.isCreated() || this.isDeleted()) {
			return null;
		}

		T obj = this.getObject();
		obj.update(pChange);
		return this.processUpdate(timestamp, obj);
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
			this.setObject(object);
			JsonNode jsonValue = this.getCrdt().readValue();
			JsonNode jsonObject = mapper.valueToTree(object);

			JsonNode jsonDiff = JsonDiff.asJson(jsonValue, jsonObject);
			if (jsonDiff.size() > 0) {
				UpdateOperation op = new UpdateOperation(jsonDiff, timestamp);
				rv = this.getManager(op);
				this.deliver(rv);
				this.setObject(null);
				this.getCrdt().getInvalidOperations().clear();
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
		sb.append("\"crdt\":" + (null == this.getCrdt() ? "null" : this.getCrdt().toString()) + ",");
		sb.append("\"object\":" + (null == this.object ? "null" : this.object.toString()));
		
		return sb.toString();
	}
}
