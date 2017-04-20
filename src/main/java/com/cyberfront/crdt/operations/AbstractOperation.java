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
package com.cyberfront.crdt.operations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonDiff;

/**
 * The AbstractOperation class is intended to be a base / abstract class for the set of operations which would normally be expected to be 
 * performed on a persistence store.  The derived operations are CREATE, READ, UPDATE and DELETE (CRUD).  These CRUD operations each hold
 * JsonDiff operations which are used in aggregate to rebuild a JSON object.
 * 
 * These operations each have a timestamp which is used to order operations.  In the event two operations have the same timestamp, an operation
 * Id is used to break the tie. In a distributed setup, it is theoretically possible for two operations generated at different nodes to have both
 * same time stamp and the same ID number, which will lead to ambiguity if the should both appear in the same CRDT.  As a final tie breaker, the
 * hash value of the two operations will be used.
 */
public abstract class AbstractOperation implements Comparable<AbstractOperation> {
	
	/** Logger to use when displaying state information */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AbstractOperation.class);

	/**
	 * The Enum OperationType lists the types of operations which comprise the types of operations which can 
	 * be performed on JSON objects
	 */
	public enum OperationType {
		
		/** The create operation. */
		CREATE,
		
		/** The read operation. */
		READ,
		
		/** The update operation. */
		UPDATE,
		
		/** The delete operation. */
		DELETE	
	}
	
	/** The time stamp associated with the operation */
	private Long timeStamp;
	
	/** The ID associated with the operations */
	private Long operationId;
	
	/** The JSON operation associated with this operation */
	private JsonNode op;
	
	/** The operation counter used to assign each operator a unique ID. */
	private static Long operationCounter = 0L;

	/**
	 * This constructor initializes elements of this abstract class instance given an operation and a 
	 * timestamp associated with executing the operation.
	 *
	 * @param op The JSON operation associated with this AbstractOperation 
	 * @param timeStamp The effective timestamp associated with the execution of this operation
	 */
	public AbstractOperation(JsonNode op, Long timeStamp) {
		this.setTimeStamp(timeStamp);
		this.setOp(op);
		this.setOperationId();
	}
	
	/**
	 * This is essentially a copy constructor for duplicating some source AbstractOperation
	 *
	 * @param src The source operation to copy
	 */
	protected AbstractOperation(AbstractOperation src) {
		this.setOperationId(src.getOperationId());
		this.setTimeStamp(src.getTimeStamp());
		this.setOp(src.getOp());
	}

	/**
	 * Retrieve the effective time stamp for this operation 
	 *
	 * @return the time stamp
	 */
	public Long getTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * Private method which is used to set the time stamp.  It is private to prevent external use, since it would
	 * be unfortunate if it were to overwrite an existing timestamp. 
	 *
	 * @param timeStamp the new time stamp
	 */
	private void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	/**
	 * This is a private static method which calculates and returns the next operation ID
	 *
	 * @return The next operation id value
	 */
	private static Long getNextOperationId() {
		return AbstractOperation.operationCounter++;
	}

	/**
	 * This method retrieves the operation identifier for this operation instance
	 *
	 * @return The operation id for this operation instance
	 */
	public Long getOperationId() {
		return this.operationId;
	}

	/**
	 * This is a private method which sets the operation ID value to the next operation ID.
	 */
	private void setOperationId() {
		this.setOperationId(AbstractOperation.getNextOperationId());
	}

	/**
	 * This private method sets the operation ID to the value provides. 
	 *
	 * @param operationId The new operation value
	 */
	private void setOperationId(Long operationId) {
		this.operationId = operationId;
	}

	/**
	 * Retrieve the JSON operation this AbstractOperation holds 
	 *
	 * @return The JSON operation held in this AbstractOperation
	 */
	public JsonNode getOp() {
		return op;
	}

	/**
	 * This is a private method which sets the operation to the specified value.
	 *
	 * @param op The new operation to associate with this AbstractOperation instance
	 */
	private void setOp(JsonNode op) {
		this.op = op;
	}

	/**
	 * This method is used to support the toString method by generating a string representation
	 * of this AbstractOperation instance
	 *
	 * @return The string representation of this AbstractOperation
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		sb.append("\"type\":\"" + this.getType().toString() + "\",");
		sb.append("\"timeStamp\":" + this.getTimeStamp() + ",");
		sb.append("\"operationId\":" + this.getOperationId() + ",");
		sb.append("\"op\":" + (null == op ? "null" : this.getOp().toString()));

		return sb.toString();
	}
	
	/**
	 * Process operation.
	 *
	 * @param document the document
	 * @return the json node
	 */
	public abstract JsonNode processOperation(JsonNode document);
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public abstract OperationType getType();

	/**
	 * Copy.
	 *
	 * @return the abstract operation
	 */
	public abstract AbstractOperation copy();
	
	/**
	 * Checks if is created.
	 *
	 * @return true, if is created
	 */
	public abstract boolean isCreated();
	
	/**
	 * Checks if is deleted.
	 *
	 * @return true, if is deleted
	 */
	public abstract boolean isDeleted();

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AbstractOperation o) {
		int rv = Long.compare(this.getTimeStamp(), o.getTimeStamp());
		rv = 0 == rv ? Long.compare(this.getOperationId(), o.getOperationId()) : rv;
		rv = 0 == rv ? Integer.compare(this.hashCode(), o.hashCode()) : rv; 
		
		return rv;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj || !(obj instanceof AbstractOperation)) {
			return false;
		}

		AbstractOperation oper = (AbstractOperation) obj;
		
		return this.hashCode() == oper.hashCode() && 
				Long.compare(this.getOperationId(), oper.getOperationId()) == 0	&&
				Long.compare(this.getTimeStamp(), oper.getTimeStamp()) == 0	&&
				JsonDiff.asJson(this.getOp(), oper.getOp()).size() == 0;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 1;
		
		hash = hash * 13 + (null != this.getOperationId() ? this.getOperationId().hashCode() : 0);
		hash = hash * 19 + (null != this.getTimeStamp() ? this.getTimeStamp().hashCode() : 0);
		hash = hash * 23 + (null != this.getOp() ? this.getOp().hashCode() : 0);
		
		return hash;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + this.getSegment() + "}";
	}
}
