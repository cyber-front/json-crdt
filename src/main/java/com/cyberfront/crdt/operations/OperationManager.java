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

import com.cyberfront.crdt.unittest.simulator.SimOperationManager;

// TODO: Auto-generated Javadoc
/**
 * The Class OperationManager.
 */
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

	/** The status. */
	private StatusType status = StatusType.PENDING;

	/** The operation. */
	private AbstractOperation operation;
	
	/**
	 * Instantiates a new operation manager.
	 *
	 * @param status the status
	 * @param op the op
	 */
	public OperationManager(StatusType status, AbstractOperation op) {
		this.setStatus(status);
		this.setOperation(op);
	}

	/**
	 * Instantiates a new operation manager.
	 *
	 * @param src the src
	 */
	public OperationManager(OperationManager src) {
		this.setStatus(src.getStatus());
		this.setOperation(src.getOperation());
	}

	/**
	 * Gets the operation.
	 *
	 * @return the operation
	 */
	public AbstractOperation getOperation() {
		return operation;
	}
	
	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public OperationManager.StatusType getStatus() {
		return this.status;
	}

	/**
	 * Sets the operation.
	 *
	 * @param operation the new operation
	 */
	private void setOperation(AbstractOperation operation) {
		this.operation = operation;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(OperationManager.StatusType status) {
		this.status = status;
	}

	/**
	 * Checks if is created.
	 *
	 * @return true, if is created
	 */
	public boolean isCreated() { return this.getOperation().isCreated(); }

	/**
	 * Checks if is deleted.
	 *
	 * @return true, if is deleted
	 */
	public boolean isDeleted() { return this.getOperation().isDeleted(); }

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj || !(obj instanceof SimOperationManager<?>) || !super.equals(obj)) { 
			return false;
		}
		
		SimOperationManager<?> mgr = (SimOperationManager<?>) obj;
		
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
	
	/**
	 * Gets the segment.
	 *
	 * @return the segment
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

	@Override
	public int compareTo(OperationManager src) {
		int opComp = this.getOperation().compareTo(src.getOperation());
		int stComp = this.getStatus().compareTo(src.getStatus());
		return opComp != 0 ? opComp : stComp;
	}

}
