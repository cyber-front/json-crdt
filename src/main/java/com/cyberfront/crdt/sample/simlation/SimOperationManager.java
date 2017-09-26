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
package com.cyberfront.crdt.sample.simlation;

import java.util.UUID;

import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.operations.GenericOperationManager;
import com.cyberfront.crdt.sample.data.AbstractDataType;

/**
 * The SimOperationManager class wraps JSON operations with some management code to ensure proper delivery and processing
 * of the operations specifically tailored for the simulation test harness.
 *
 * @param <T> The type of object to which the operations is applied.  In this case T should extend the AbstractDataType
 * class
 */
public class SimOperationManager<T extends AbstractDataType>
	extends GenericOperationManager<T> {
	
	/** Identifier of the object which is  */
	private final UUID objectId;

	/**
	 * Instantiates a new operation manager.
	 *
	 * @param status Status of the operation being managed, either APPROVED, PENDING or REJECTED
	 * @param objectId the object id
	 * @param objectClass the object class
	 * @param operation the operation
	 */
	public SimOperationManager(StatusType status, AbstractOperation operation, UUID objectId, Class<T> objectClass) {
		super(status, operation, objectClass);
		this.objectId = objectId;
	}
	
	/**
	 * Gets the ID reference value of the object for which the operation applies
	 *
	 * @return The ID reference for the object for which the operation applies 
	 */
	public UUID getObjectId(){
		return this.objectId;
	}
	
	/**
	 * Generate and return a near copy of this class instance including the operation identifier
	 *
	 * @return A copy of this class instance
	 */
	public SimOperationManager<T> copy() {
		return new SimOperationManager<>(this.getStatus(), this.getOperation().copy(), this.getObjectId(), this.getObjectClass());
	}

	/**
	 * Generate and return a copy of this class instance including the operation identifier but with the given status type
	 *
	 * @param status Status value for the new copy which should be used in lieu of that in this instance
	 * @return A copy of the operation manager as provided to this routine.
	 */
	public SimOperationManager<T> copy(StatusType status) {
		return new SimOperationManager<>(status, this.getOperation().copy(), this.getObjectId(), this.getObjectClass());
	}

	/**
	 * Generate and return a near copy of this class instance including the operation identifier,  In this case a new
	 * operation identifier is set
	 *
	 * @return a reference to the new operations based on this one
	 */
	public SimOperationManager<T> mimic() {
		return new SimOperationManager<>(this.getStatus(), this.getOperation().mimic(), this.getObjectId(), this.getObjectClass());
	}

	/**
	 * Generate and return a near copy of this class instance including the operation identifier,  In this case a new
	 * operation identifier is set, and a new status is given through the argument list
	 *
	 * @param status Status value for the new copy which should be used in lieu of that in this instance
	 * @return a reference to the new operations based on this one
	 */
	public SimOperationManager<T> mimic(StatusType status) {
		return new SimOperationManager<>(status, this.getOperation().mimic(), this.getObjectId(), this.getObjectClass());
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj || !(obj instanceof SimOperationManager<?>) || !super.equals(obj)) { 
			return false;
		}
		
		SimOperationManager<?> mgr = (SimOperationManager<?>) obj;
		
		return this.getObjectId().equals(mgr.getObjectId()) && this.getOperation().equals(mgr.getOperation());
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		
		hash = 31 * hash + this.getObjectId().hashCode();
		hash = 37 * hash + this.getObjectClass().hashCode();
		
		return hash;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#getSegment()
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"objectId\":\"" + this.getObjectId() + "\"");
		
		return sb.toString();
	}
}
