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

import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.unittest.data.AbstractDataType;

// TODO: Auto-generated Javadoc
/**
 * The Class OperationManager.
 *
 * @param <T> the generic type
 */
public class OperationManager<T extends AbstractDataType> extends BaseManager<T> implements Comparable<OperationManager<? extends AbstractDataType>> {

	/** The operation. */
	private AbstractOperation operation;
	
	/**
	 * Instantiates a new operation manager.
	 *
	 * @param objectId the object id
	 * @param username the username
	 * @param nodename the nodename
	 * @param objectClass the object class
	 * @param operation the operation
	 */
	public OperationManager(String objectId, String username, String nodename, Class<T> objectClass, AbstractOperation operation) {
		super(objectId, username, nodename, objectClass);
		this.setOperation(operation);
	}
	
	/**
	 * Instantiates a new operation manager.
	 *
	 * @param src the src
	 */
	public OperationManager(OperationManager<T> src) {
		this(src.getObjectId(), src.getUsername(), src.getNodename(), src.getObjectClass(), src.getOperation());
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
	 * Sets the operation.
	 *
	 * @param operation the new operation
	 */
	private void setOperation(AbstractOperation operation) {
		this.operation = operation;
	}

	/**
	 * Copy.
	 *
	 * @return the operation manager
	 */
	public OperationManager<T> copy() {
		return new OperationManager<>(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(OperationManager<? extends AbstractDataType> o) {
		return super.baseCompare(o);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj || !(obj instanceof OperationManager<?>) || !super.equals(obj)) { 
			return false;
		}
		
		OperationManager<?> mgr = (OperationManager<?>) obj;
		
		return this.getOperation().equals(mgr.getOperation());
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#hashCode()
	 */
	@Override
	public int hashCode() {
		return
				super.hashCode() + 
				this.getOperation().hashCode() * 47;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"operation\":" + this.getOperation());
		
		return sb.toString();
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
}
