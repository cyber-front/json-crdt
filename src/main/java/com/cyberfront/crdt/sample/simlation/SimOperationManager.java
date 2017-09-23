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

// TODO: Auto-generated Javadoc
/**
 * The Class OperationManager.
 *
 * @param <T> the generic type
 */
public class SimOperationManager<T extends AbstractDataType>
	extends GenericOperationManager<T> {
	
	/** The object id. */
	private final UUID objectId;
	
	/** The node identifier. */
	private final UUID nodeId;
	
	/**
	 * Instantiates a new operation manager.
	 *
	 * @param objectId the object id
	 * @param nodeId the node identifier
	 * @param objectClass the object class
	 * @param operation the operation
	 */
	public SimOperationManager(StatusType status, AbstractOperation operation, UUID objectId, UUID nodeId, Class<T> objectClass) {
		super(status, operation, objectClass);
		this.objectId = objectId;
		this.nodeId = nodeId;
	}
	
//	/**
//	 * Instantiates a new operation manager.
//	 *
//	 * @param src the src
//	 */
//	private SimOperationManager(SimOperationManager<T> src) {
//		this(src.getStatus(), src.getOperation().copy(), src.getObjectId(), src.getNodeId(), src.getObjectClass());
//	}
//	
//	/**
//	 * Instantiates a new operation manager.
//	 *
//	 * @param src the src
//	 */
//	private SimOperationManager(SimOperationManager<T> src, StatusType status) {
//		this(status, src.getOperation().mimic(), src.getObjectId(), src.getNodeId(), src.getObjectClass());
//	}
//	
	/**
	 * Gets the object id.
	 *
	 * @return the object id
	 */
	public UUID getObjectId(){
		return this.objectId;
	}
	
	/**
	 * Gets the node identifier.
	 *
	 * @return the node identifier
	 */
	public UUID getNodeId() {
		return this.nodeId;
	}

	/**
	 * Copy.
	 *
	 * @return the operation manager
	 */
	public SimOperationManager<T> copy() {
		return new SimOperationManager<>(this.getStatus(), this.getOperation().copy(), this.getObjectId(), this.getNodeId(), this.getObjectClass());
	}

	/**
	 * Copy.
	 *
	 * @return the operation manager
	 */
	public SimOperationManager<T> copy(StatusType status) {
		return new SimOperationManager<>(status, this.getOperation().copy(), this.getObjectId(), this.getNodeId(), this.getObjectClass());
	}

	/**
	 * Mimic.
	 *
	 * @return the operation manager
	 */
	public SimOperationManager<T> mimic() {
		return new SimOperationManager<>(this.getStatus(), this.getOperation().mimic(), this.getObjectId(), this.getNodeId(), this.getObjectClass());
	}

	/**
	 * Mimic.
	 *
	 * @return the operation manager
	 */
	public SimOperationManager<T> mimic(StatusType status) {
		return new SimOperationManager<>(status, this.getOperation().mimic(), this.getObjectId(), this.getNodeId(), this.getObjectClass());
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
		
		return this.getOperation().equals(mgr.getOperation());
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		
		hash = 31 * hash + this.getObjectId().hashCode();
		hash = 37 * hash + this.getNodeId().hashCode();
		hash = 41 * hash + this.getObjectClass().hashCode();
		
		return hash;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#getSegment()
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"objectId\":\"" + this.getObjectId() + "\",");
		sb.append("\"nodeId\":\"" + this.getNodeId() + "\"");
		
		return sb.toString();
	}
}
