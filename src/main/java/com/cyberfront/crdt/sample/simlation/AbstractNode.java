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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.support.Support;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractNode.
 *
 * @param <T> the generic type
 */
public abstract class AbstractNode<T extends AbstractDataType> {
	
	/** The node name. */
	private String nodeName;
	
	/** The user names. */
	private List<String> userNames;
	
	/** The crdt ids. */
	private List<String> crdtIds;

	/** The datastore. */
	private Map<String, SimCRDTManager<? extends T>> datastore;

	/**
	 * Instantiates a new abstract node.
	 *
	 * @param nodeName the node name
	 */
	public AbstractNode(String nodeName) {
		this.setNodeName(nodeName);
	}

	/**
	 * Instantiates a new abstract node.
	 *
	 * @param nodeName the node name
	 * @param userNames the user names
	 */
	public AbstractNode(String nodeName, Collection<String> userNames) {
		this.setNodeName(nodeName);
		this.getUserNames().addAll(userNames);
	}

	/**
	 * Gets the node name.
	 *
	 * @return the node name
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * Sets the node name.
	 *
	 * @param nodeName the new node name
	 */
	private void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	/**
	 * Gets the user names.
	 *
	 * @return the user names
	 */
	public List<String> getUserNames() {
		if (null == this.userNames) {
			this.userNames = new ArrayList<>();
		}

		return userNames;
	}
	
	/**
	 * Gets the datastore.
	 *
	 * @return the datastore
	 */
	public Map<String, SimCRDTManager<? extends T>> getDatastore() {
		if (null == datastore) {
			this.datastore = this.createDatastore();
		}
		
		return this.datastore;
	}

	/**
	 * Clear.
	 */
	public void clear() {
		for (Map.Entry<String, SimCRDTManager<? extends T>> entry : this.getDatastore().entrySet()) {
			entry.getValue().clear();
		}

		this.getDatastore().clear();
	}
	
	/**
	 * Gets the crdt ids.
	 *
	 * @return the crdt ids
	 */
	protected List<String> getCrdtIds() {
		if (null == this.crdtIds) {
			this.crdtIds = new ArrayList<>();
		}
		
		return this.crdtIds;
	}
	
	/**
	 * Adds the CRDT.
	 *
	 * @param crdt the crdt
	 */
	protected void addCRDT(SimCRDTManager<? extends T> crdt) {
		this.getDatastore().put(crdt.getObjectId(), crdt);
		this.getCrdtIds().add(crdt.getObjectId());
	}
	
	/**
	 * Adds the username.
	 *
	 * @param username the username
	 */
	public void addUsername(String username) {
		this.getUserNames().add(username);
	}
	
	/**
	 * Adds the usernames.
	 *
	 * @param usernames the usernames
	 */
	public void addUsernames(Collection<String> usernames) {
		this.getUserNames().addAll(usernames);
	}

	/**
	 * Gets the datastore.
	 *
	 * @param id the id
	 * @return the datastore
	 */
//	public SimCRDTManager<? extends T> getDatastore(String id) {
//		return this.getDatastore().get(id);
//	}
//	
	/**
	 * Pick crdt id.
	 *
	 * @return the string
	 */
	public String pickCrdtId() {
		int index = Support.getRandom().nextInt(this.getCrdtIds().size());
		return this.getCrdtIds().get(index);
	}
	
	/**
	 * Pick CRDT.
	 *
	 * @return the CRDTManager
	 */
	public SimCRDTManager<? extends T> pickCRDT() {
		return this.getDatastore().get(this.pickCrdtId());
	}
	
	/**
	 * Datastore to string.
	 *
	 * @return the string
	 */
	private String datastoreToString() {
		StringBuilder sb = new StringBuilder();
		char separator = '{';
		
		if (this.getDatastore().isEmpty()) {
			sb.append(separator);
		} else {
			for (Map.Entry<String, SimCRDTManager<? extends T>> entry : this.getDatastore().entrySet()) {
				sb.append(separator + "\"" + entry.getKey() + "\":" + entry.getValue().toString());
				separator = ',';
			}
		}
		
		sb.append("}");
		
		return sb.toString();
	}
	
	/**
	 * Usernames to string.
	 *
	 * @return the string
	 */
	private String usernamesToString() {
		StringBuilder sb = new StringBuilder();
		String separator = "[";
		
		for (String user : this.getUserNames()) {
			sb.append(separator + "\"" + user + "\"");
			separator = ",";
		}
		sb.append("]");
		
		return sb.toString();
	}

	/**
	 * Gets the segment.
	 *
	 * @return the segment
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"nodeName\":\"" + this.getNodeName()  + "\",");
		sb.append("\"userNames\":" + this.usernamesToString() + ",");
		sb.append("\"datastore\":" + this.datastoreToString());
		
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{");
		sb.append(this.getSegment());
		sb.append("}");
		
		return sb.toString();
	}

	/**
	 * Creates the CRDT.
	 *
	 * @param id The ID value for the new CRDT
	 * @return the CRDTManager
	 */
	protected abstract SimCRDTManager<? extends T> createCRDT(String id);
	
	/**
	 * Creates the datastore.
	 *
	 * @return the map
	 */
	protected abstract Map<String, SimCRDTManager<? extends T>> createDatastore();
}
