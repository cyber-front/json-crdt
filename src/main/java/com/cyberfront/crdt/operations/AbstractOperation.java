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

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;		// Use this with jsonpatch
//import com.flipkart.zjsonpatch.JsonDiff;				// Use this with zjsonpatch

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

	/** The ObjectMapper used to create empty JsonNode object to start the chain of JsonDiff derived operations */
	private static final ObjectMapper mapper = new ObjectMapper();
	
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
	private final Long timeStamp;
	
	/** The ID associated with the operations */
	private final Long operationId;
	
	/** The JSON operation associated with this operation */
	private final JsonNode op;
	
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
		this(op, timeStamp, operationCounter++);
	}
	
	/**
	 * Fully specified operation constructor which directs the values of all elements in the class instance
	 * @param op Operation details associated with the object
	 * @param timeStamp Timestamp of the operation
	 * @param operationId Id for the operation
	 */
	private AbstractOperation(JsonNode op, Long timeStamp, Long operationId) {
		this.timeStamp = timeStamp;
		this.operationId = operationId;
		this.op = op;
	}

	/**
	 * This is essentially a copy constructor for duplicating some source AbstractOperation
	 *
	 * @param src The source operation to copy
	 */
	protected AbstractOperation(AbstractOperation src) {
		this(src.op, src.timeStamp, src.operationId);
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
	 * This method retrieves the operation identifier for this operation instance
	 *
	 * @return The operation id for this operation instance
	 */
	public Long getOperationId() {
		return this.operationId;
	}

	/**
	 * Retrieve the JSON operation this AbstractOperation holds 
	 *
	 * @return The JSON operation held in this AbstractOperation
	 */
	public JsonNode getOp() {
		return this.op;
	}
	
	/**
	 * Retrieve the ObjectMapper used for the various operations classes
	 * @return Returns the mapper for use by the operation classes
	 */
	protected static ObjectMapper getMapper() {
		return mapper;
	}
	
	/**
	 * Process the operation on the document presented and return the resulting document to the calling routine 
	 *
	 * @param document The document to which the operation will be applied
	 * @return The JSON document which results from applying the operation to the given document
	 * @throws JsonPatchException results when the operation cannot be applied to the provided document
	 * @throws IOException results when the something other than an operation is encoded in one of the derived class instances
	 */
	public abstract JsonNode processOperation(JsonNode document) throws JsonPatchException, IOException;  // Use this with jsonpatch
//	public abstract JsonNode processOperation(JsonNode document);               // Use this with zjsonpatch
	
	/**
	 * This abstract method retrieves the enumerated type specification for the derived class instance 
	 *
	 * @return The enumerated type specifier for this instance
	 */
	public abstract OperationType getType();

	/**
	 * Generate and return a copy of this instance
	 *
	 * @return Returns a copy of this AbstractOperation
	 */
	public abstract AbstractOperation copy();
	
	/**
	 * Generate and return a near copy of this instance.  All elements will be the same except the ID value
	 *
	 * @return Returns a near copy of this AbstractOperation
	 */
	public abstract AbstractOperation mimic();
	
	/**
	 * Checks if the operation is a CreateOperation operation.
	 *
	 * @return true exactly when this instance is a CreateOperation
	 */
	public abstract boolean isCreated();
	
	/**
	 * Checks if the operation is a DeleteOperation operation.
	 *
	 * @return true exactly when this instance is a DeleteOperation
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
				this.getOp().equals(oper.getOp());
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
		hash = hash * 29 + (null != this.getType() ? this.getType().hashCode() : 0);
		
		return hash;
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + this.getSegment() + "}";
	}
	
	public static Collection<AbstractOperation> copy(Collection<AbstractOperation> opList) {
		Collection<AbstractOperation> rv = new TreeSet<>();
		
		for (AbstractOperation op : opList) {
			rv.add(op.copy());
		}
		
		return rv;
	}
}
