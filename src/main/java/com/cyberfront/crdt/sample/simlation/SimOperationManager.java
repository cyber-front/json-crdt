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
	private String objectId;
	
	/** The username. */
	private String username;
	
	/** The nodename. */
	private String nodename;
	
	/**
	 * Instantiates a new operation manager.
	 *
	 * @param objectId the object id
	 * @param username the username
	 * @param nodename the nodename
	 * @param objectClass the object class
	 * @param operation the operation
	 */
	public SimOperationManager(StatusType status, AbstractOperation operation, String objectId, String username, String nodename, Class<T> objectClass) {
		super(status, operation, objectClass);
		this.setObjectId(objectId);
		this.setUsername(username);
		this.setNodename(nodename);
	}
	
	/**
	 * Instantiates a new operation manager.
	 *
	 * @param src the src
	 */
	public SimOperationManager(SimOperationManager<T> src) {
		super(src);
		this.setObjectId(src.getObjectId());
		this.setUsername(src.getUsername());
		this.setNodename(src.getNodename());
	}
	
	/**
	 * Gets the object id.
	 *
	 * @return the object id
	 */
	public String getObjectId(){
		return this.objectId;
	}
	
	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername(){
		return this.username;
	}
	
	/**
	 * Gets the nodename.
	 *
	 * @return the nodename
	 */
	public String getNodename() {
		return this.nodename;
	}

	/**
	 * Sets the object id.
	 *
	 * @param id the new object id
	 */
	private void setObjectId(String id) {
		this.objectId = id;
	}
	
	/**
	 * Sets the username.
	 *
	 * @param user the new username
	 */
	private void setUsername(String user) {
		this.username = user;
	}
	
	/**
	 * Sets the nodename.
	 *
	 * @param source the new nodename
	 */
	private void setNodename(String source) {
		this.nodename = source;
	}

	/**
	 * Copy.
	 *
	 * @return the operation manager
	 */
	public SimOperationManager<T> copy() {
		return new SimOperationManager<>(this);
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
		hash = 37 * hash + this.getNodename().hashCode();
		hash = 41 * hash + this.getUsername().hashCode();
		hash = 43 * hash + this.getObjectClass().hashCode();
		
		return hash;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#getSegment()
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"objectId\":\"" + this.getObjectId() + "\",");
		sb.append("\"username\":\"" + this.getUsername() + "\",");
		sb.append("\"nodename\":\"" + this.getNodename() + "\"");
		
		return sb.toString();
	}
}
