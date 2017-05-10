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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.unittest.data.AbstractDataType;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseManager.
 *
 * @param <T> the generic type
 */
public abstract class BaseManager<T extends AbstractDataType> {
	/** The Constant logger. */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BaseManager.class);

	/** The object class. */
	private Class<T> objectClass;
	
	/** The object id. */
	private String objectId;
	
	/** The username. */
	private String username;
	
	/** The nodename. */
	private String nodename;
	
	/**
	 * Instantiates a new base manager.
	 *
	 * @param id the id
	 * @param username the username
	 * @param nodename the nodename
	 * @param objectClass the object class
	 */
	public BaseManager(String id, String username, String nodename, Class<T> objectClass) {
		this.setObjectId(id);
		this.setUsername(username);
		this.setNodename(nodename);
		this.setObjectClass(objectClass);
	}
	
	/**
	 * Gets the object class.
	 *
	 * @return the object class
	 */
	public Class<T> getObjectClass() {
		return this.objectClass;
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
	 * Sets the object class.
	 *
	 * @param objectClass the new object class
	 */
	private void setObjectClass(Class<T> objectClass) {
		this.objectClass = objectClass;
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
	 * Base compare.
	 *
	 * @param o the o
	 * @return the int
	 */
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	protected int baseCompare(BaseManager<? extends AbstractDataType> o) {
		int compId = this.getObjectId().compareTo(o.getObjectId());
		int compUsername = this.getUsername().compareTo(getUsername());
		int compNodename = this.getNodename().compareTo(o.getNodename());
		
		return compId != 0 ? compId : compNodename != 0 ? compNodename : compUsername;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.ComparableManager#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj || !(obj instanceof BaseManager<?>) || !super.equals(obj)) { 
			return false;
		}
		
		BaseManager<?> mgr = (BaseManager<?>) obj;
		
		return this.getObjectClass().getName().compareTo(mgr.getObjectClass().getName()) == 0;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.ComparableManager#hashCode()
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
	
	/**
	 * Gets the segment.
	 *
	 * @return the segment
	 */
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.ComparableManager#getSegment()
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"objectClass\":\"" + this.getObjectClass().getName() + "\",");
		sb.append("\"objectId\":\"" + this.getObjectId() + "\",");
		sb.append("\"username\":\"" + this.getUsername() + "\",");
		sb.append("\"nodename\":\"" + this.getNodename() + "\"");
		
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + this.getSegment() + "}";
	}
	
//	protected static <T> String toString(Collection<T> elements) {
//		StringBuilder sb = new StringBuilder();
//		String delimiter = "[";
//		
//		for (T element : elements) {
//			sb.append(delimiter + element.toString());
//			delimiter = ",";
//		}
//
//		sb.append("]");
//		return sb.toString();
//	}
}
