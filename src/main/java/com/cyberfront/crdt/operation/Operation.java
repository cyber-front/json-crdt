/*
 * Copyright (c) 2018 Cybernetic Frontiers LLC
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
package com.cyberfront.crdt.operation;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
//import com.flipkart.zjsonpatch.JsonDiff;				// Use this with zjsonpatch

/**
 * The Operation class is intended to be a base / abstract class for the set of operations which would normally be expected to be 
 * performed on a persistence store.  The derived operations are CREATE, READ, UPDATE and DELETE (CRUD).  These CRUD operations each hold
 * JsonDiff operations which are used in aggregate to rebuild a JSON object.
 * 
 * These operations each have a timestamp which is used to order operations.  In the event two operations have the same timestamp, an operation
 * Id is used to break the tie. In a distributed setup, it is theoretically possible for two operations generated at different nodes to have both
 * same time stamp and the same ID number, which will lead to ambiguity if the should both appear in the same CRDT.  As a final tie breaker, the
 * hash value of the two operations will be used.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Operation implements Comparable<Operation> {
	private static final String TYPE = "type"; 
	private static final String OP = "op";
	private static final String ID = "id";
	private static final String TIMESTAMP = "timestamp"; 
	
	/** Logger to use when displaying state information */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(Operation.class);

	/** The ObjectMapper used to create empty JsonNode object to start the chain of JsonDiff derived operations */
	private static final ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * The Enum OperationType lists the types of operations which comprise the types of operations which can 
	 * be performed on JSON objects
	 */
	public enum OperationType {
		
		/** The create operation. */
		CREATE,
		
		/** The delete operation. */
		UPDATE,

		/** The delete operation. */
		DELETE,

		/** The read operation. */
		READ
	}
	
	/** Type of operation this instance represents */
	@JsonProperty(TYPE)
	private final OperationType type;
	
	/** Memoized version of the operation to save on processing. */
	@JsonProperty(OP)
	private final JsonNode op;

	/** An identifier for this Operation Instance to disambiguate operations which may have the same timestamp. */
	@JsonProperty(ID)
	private final UUID id;

	/** The time stamp associated with the operation */
	@JsonProperty(TIMESTAMP)
	private final Long timestamp;

	/**
	 * This constructor initializes elements of this abstract class instance given an operation and a 
	 * timestamp associated with executing the operation.
	 *
	 * @param type Specification of the operation type, but generally limited to CREATE, READ and DELETE 
	 * @param timestamp The effective timestamp associated with the execution of this operation
	 */
	public Operation(OperationType type, Long timestamp) {
		this(UUID.randomUUID(), type, null, timestamp);
	}
	
	/**
	 * This instantiates a new UpdateOperation given an operation in a JsonNode and a timestamp.  The operation
	 * is not validated as conforming to RFC 6902
	 *
	 * @param op The operation, consisting of a JsonNode conforming to RFC 6902.
	 * @param timestamp The effective time stamp of the operation 
	 */
	public Operation(JsonNode op, Long timestamp) {
		this(UUID.randomUUID(), OperationType.UPDATE, op, timestamp);
	}

	/**
	 * This is essentially a copy constructor for duplicating some source AbstractOperation
	 *
	 * @param src The source operation to copy
	 */
	public Operation(Operation src) {
		this(src.getId(), src.getType(), src.getOp(), src.getTimestamp());
	}
	
	/**
	 * Fully specified operation constructor which directs the values of all elements in the class instance
	 * @param id The unique identifier for operations
	 * @param type Enumeration describing the type of operation this instance is to perform 
	 * @param timestamp Timestamp of the operation
	 * @param op Operation details associated with the object; should be instantiated only for UPDATE operations
	 */
	@JsonCreator
	public Operation(@JsonProperty(ID) UUID id,
					 @JsonProperty(TYPE) OperationType type,
					 @JsonProperty(OP) JsonNode op,
					 @JsonProperty(TIMESTAMP) Long timestamp) {
		this.id = id;
		this.type = type;
		this.op = (null == op || op.isNull()) ? null : op;
		this.timestamp = timestamp;
		
		if (!this.validate()) {
			throw new IllegalArgumentException("Operation Failed Validation: " + this.toString());
		}
	}

	/**
	 * Validate the state of this operation instance
	 * @return True exactly when the state of this operation insance is valid
	 */
	private boolean validate() {
		if (null == this.getType() || null == this.getId() || null == this.getTimestamp() || 0 > this.getTimestamp()) {
			return false;
		} 

		return OperationType.UPDATE.equals(this.getType()) ^ (null == this.getOp());
	}
	
	/**
	 * Retrieve the operation ID value
	 * @return The operation ID value
	 */
	@JsonProperty(ID)
	public UUID getId() {
		return this.id;
	}
	
	/**
	 * This method retrieves the enumerated type specification for the derived class instance 
	 *
	 * @return The enumerated type specifier for this instance
	 */
	@JsonProperty(TYPE)
	public OperationType getType() {
		return this.type;
	};

	/**
	 * Retrieve the JSON update operation.  If the operation type isn't UPDATE, this will be null; otherwise it will
	 * be a JsonNode which conforms to RFC 6902. 
	 * @return The JSON Patch, compliant with RFC 6902, for this operation if it's an UPDATE operation type, or null otherwise. 
	 */
	@JsonProperty(OP)
	public JsonNode getOp() {
		return this.op;
	}
	
	/**
	 * Retrieve the effective time stamp for this operation 
	 *
	 * @return the time stamp
	 */
	@JsonProperty(TIMESTAMP)
	public Long getTimestamp() {
		return this.timestamp;
	}
	
	/**
	 * Checks if the operation is a CREATE operation.
	 *
	 * @return true exactly when this instance is a CREATE operation.
	 */
	@JsonIgnore
	public boolean isCreate() {
		return OperationType.CREATE.equals(this.type);
	}
	
	/**
	 * Checks if the operation is a DELETE operation.
	 *
	 * @return true exactly when this instance is a DELETE operation.
	 */
	@JsonIgnore
	public boolean isDelete() {
		return OperationType.DELETE.equals(this.type);
	}
	
	/**
	 * Checks if the operation is a READ operation.
	 *
	 * @return true exactly when this instance is a READ operation.
	 */
	@JsonIgnore
	public boolean isRead() {
		return OperationType.READ.equals(this.type);
	}
	
	/**
	 * Checks if the operation is an update operation.
	 *
	 * @return true exactly when this instance is an update operation.
	 */
	@JsonIgnore
	public boolean isUpdate() {
		return OperationType.UPDATE.equals(this.type);
	}

	/**
	 * Retrieve the ObjectMapper used for the various operations classes
	 * @return Returns the mapper for use by the operation classes
	 */
	private static ObjectMapper getMapper() {
		return mapper;
	}

	/**
	 * Copy a list of operations
	 * @param src Source list of operations
	 * @return Copy of the source list of operations
	 */
	public static Collection<Operation> copy(Collection<Operation> src) {
		Collection<Operation> rv = new TreeSet<>();
		
		for (Operation op : src) {
			rv.add(new Operation(op));
		}
		
		return rv;
	}
	
	/**
	 * Process the operation on the document presented and return the resulting document to the calling routine 
	 *
	 * @param document The document to which the operation will be applied
	 * @return The JSON document which results from applying the operation to the given document
	 * @throws JsonPatchException results when the operation cannot be applied to the provided document
	 * @throws IOException results when the something other than an operation is encoded in one of the derived class instances
	 */
	public JsonNode processOperation(JsonNode document) throws JsonPatchException, IOException {
		switch (this.getType()) {
		case CREATE:
				return getMapper().createObjectNode();
		case READ:
				return document;
		case UPDATE:
			return null == document
			? null
			: JsonPatch.fromJson(this.getOp()).apply(document);		// Use this with jsonpatch
//			: JsonPatch.apply(this.getOp(), document);				// Use this with zjsonpatch
		case DELETE:
				return null;
		default:
			break;
		
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Operation op) {
		int rv = Long.compare(this.getTimestamp(), op.getTimestamp());
		rv = 0 == rv ? ObjectUtils.compare(this.getType(), op.getType()) : rv;
		rv = 0 == rv ? ObjectUtils.compare(this.getId(), op.getId()) : rv;
		rv = 0 == rv ? ObjectUtils.compare(null == this.getOp() ? null : this.getOp().hashCode(), null == op.getOp() ? null : op.getOp().hashCode()) : rv;

		return rv;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj || !(obj instanceof Operation)) {
			return false;
		}

		Operation oper = (Operation) obj;
		
		return this.getTimestamp().equals(oper.getTimestamp()) &&
				Objects.equals(this.getId(), oper.getId()) &&
				Objects.equals(this.getType(), oper.getType()) &&
				Objects.equals(this.getOp(), oper.getOp());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 1;
		
		hash = hash * 13 + (null != this.getId() ? this.getId().hashCode() : 0);
		hash = hash * 17 + (null != this.getType() ? this.getType().hashCode() : 0);
		hash = hash * 19 + (null != this.getOp() ? this.getOp().hashCode() : 0);
		hash = hash * 23 + (null != this.getTimestamp() ? this.getTimestamp().hashCode() : 0);
		
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
		String delimiter = "";

		if (null != this.getId()) {
			sb.append(delimiter + "\"id\":\"" + this.getId() + "\"");
			delimiter = ",";
		}
		
		if (null != this.getTimestamp()) {
			sb.append(delimiter + "\"timestamp\":" + this.getTimestamp());
			delimiter = ",";
		}
		
		if (null != this.getType()) {
			sb.append(delimiter + "\"type\":\"" + this.getType() + "\"");
			delimiter = ",";
		}
		
		if (null != this.getOp()) {
			sb.append(delimiter + "\"op\":" + this.getOp());
			delimiter = ",";
		}

		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + this.getSegment() + "}";
	}
}

