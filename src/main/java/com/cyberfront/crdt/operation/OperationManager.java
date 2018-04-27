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
package com.cyberfront.crdt.operation;

import com.cyberfront.crdt.sample.simulation.SimOperationManager;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is the base class for wrapping AbstractOperations.  It provides the basic functionality for associating various
 * elements of metadata associated with the op bound to the derived manager class.  
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @Type(value = GenericOperationManager.class, name = "GenericOperationManager"),
    @Type(value = SimOperationManager.class, name = "SimOperationManager")
    })
public class OperationManager implements Comparable<OperationManager> {
	/** JSON property name for the status type property for a given manager */
	protected static final String STATUS = "status";

	/** JSON property name for the operation a given manager is managing */
	protected static final String OP = "op";
	
	/**
	 * The Enum StatusType.
	 */
	public enum StatusType {
		
		/** The rejected. */
		REJECTED,
		
		/** The pending. */
		PENDING,
	
		/** The approved. */
		APPROVED
	}

	/** The status associated with the op. */
	private final StatusType status;

	/** The op bound with the metadata. */
	private final Operation op;
	
	/**
	 * Instantiates a new op manager given a status and an op
	 *
	 * @param status The status associated with the operations
	 * @param op The op associated with its metadata
	 */
	@JsonCreator
	public OperationManager(
			@JsonProperty(STATUS) StatusType status,
			@JsonProperty(OP) Operation op) {
		this.status = status;
		this.op = op;
	}

	/**
	 * Copy constructor to copy from a source OperationManager 
	 *
	 * @param src The source object
	 */
	public OperationManager(OperationManager src) {
		this(src.getStatus(), src.getOperation());
	}

	/**
	 * Retrieves the op to associate with the managed metadata
	 *
	 * @return The op associated with its managed metadata
	 */
	@JsonProperty(OP)
	public Operation getOperation() {
		return op;
	}
	
	/**
	 * Retrieve the status associated with the op
	 *
	 * @return The status associated with the op
	 */
	@JsonProperty(STATUS)
	public OperationManager.StatusType getStatus() {
		return this.status;
	}

	/**
	 * Checks if the op is a CreateOperation
	 *
	 * @return true, if the op being managed is a CreateOperation
	 */
	@JsonIgnore
	public boolean isCreated() {
		return this.getOperation().isCreate();
	}

	/**
	 * Checks if the op is a DeleteOperation
	 *
	 * @return true, if the op being managed is a DeleteOperation
	 */
	@JsonIgnore
	public boolean isDeleted() {
		return this.getOperation().isDelete();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj || !(obj instanceof OperationManager) || !super.equals(obj)) { 
			return false;
		}
		
		OperationManager mgr = (OperationManager) obj;
		
		return this.getStatus().equals(mgr.getStatus()) && 
			   this.getOperation().equals(mgr.getOperation());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		
		hash = 23 * hash + this.getOperation().hashCode();
		hash = 29 * hash + this.getStatus().hashCode();
		
		return hash;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(OperationManager src) {
		int opComp = this.getOperation().compareTo(src.getOperation());
		int stComp = this.getStatus().compareTo(src.getStatus());
		return opComp != 0 ? opComp : stComp;
	}
	
	/**
	 * Get the string segment for rendering the OperationManager portions of this class instance 
	 *
	 * @return The string segment containing a rendering of the OperationManager portions of this class instance 
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"op\":" + this.getOperation().toString() + ",");
		sb.append("\"status\":\"" + this.getStatus() + "\",");
		sb.append("\"created\":" + this.isCreated() + ",");
		sb.append("\"deleted\":" + this.isDeleted());
		
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "{" + this.getSegment() + "}";
	}
}
