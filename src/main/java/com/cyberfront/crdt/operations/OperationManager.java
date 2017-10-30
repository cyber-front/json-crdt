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

import com.cyberfront.crdt.sample.simlation.SimOperationManager;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

/**
 * This is the base class for wrapping AbstractOperations.  It provides the basic functionality for associating various
 * elements of metadata associated with the operation bound to the derived manager class.  
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @Type(value = GenericOperationManager.class, name = "GenericOperationManager"),
    @Type(value = SimOperationManager.class, name = "SimOperationManager")
    })
public class OperationManager implements Comparable<OperationManager> {
	
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

	/** The status associated with the operation. */
	private final StatusType status;

	/** The operation bound with the metadata. */
	private final AbstractOperation operation;
	
	/**
	 * Instantiates a new operation manager given a status and an operation
	 *
	 * @param status The status associated with the operations
	 * @param operation The operation associated with its metadata
	 */
	public OperationManager(StatusType status, AbstractOperation operation) {
		this.status = status;
		this.operation = operation;
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
	 * Retrieves the operation to associate with the managed metadata
	 *
	 * @return The operation associated with its managed metadata
	 */
	public AbstractOperation getOperation() {
		return operation;
	}
	
	/**
	 * Retrieve the status associated with the operation
	 *
	 * @return The status associated with the operation
	 */
	public OperationManager.StatusType getStatus() {
		return this.status;
	}

	/**
	 * Checks if the operation is a CreateOperation
	 *
	 * @return true, if the operation being managed is a CreateOperation
	 */
	public boolean isCreated() {
		return this.getOperation().isCreated();
	}

	/**
	 * Checks if the operation is a DeleteOperation
	 *
	 * @return true, if the operation being managed is a DeleteOperation
	 */
	public boolean isDeleted() {
		return this.getOperation().isDeleted();
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
		
		return this.getOperation().equals(mgr.getOperation());
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
		
		sb.append("\"operation\":" + this.getOperation().toString() + ",");
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
