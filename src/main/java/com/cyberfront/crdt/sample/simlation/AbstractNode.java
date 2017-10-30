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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.support.Support;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The AbstractNode class establishes a base framework for derived classes to draw upon to manage a collection of CRDT objects
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @Type(value = Node.class, name = "Node")
    })
public abstract class AbstractNode {
	
	/** The node identifier. */
	private final UUID id;
	
	/** The datastore containinf all of the CRDT instances for this node.. */
	private Map<UUID, SimCRDTManager<? extends AbstractDataType>> datastore;

	/**
	 * Instantiates a new abstract node.
	 *
	 * @param id Identifier valud for the new node
	 */
	public AbstractNode(UUID id) {
		this.id = id;
	}

	/**
	 * Retrieve the node identifier.
	 *
	 * @return the node identifier
	 */
	public UUID getId() {
		return id;
	}
	
	/**
	 * Retrieve the data store and return to the calling routine.  It will create new datastore if none exists, though
	 * the new one will be empty.  This routine should never return null, but may return an empty datastore
	 *
	 * @return the datastore
	 */
	public Map<UUID, SimCRDTManager<? extends AbstractDataType>> getDatastore() {
		if (null == datastore) {
			this.datastore = new TreeMap<>();
		}
		
		return this.datastore;
	}

	/**
	 * Clear the contents of the datastores in this node
	 */
	public void clear() {
		for (Map.Entry<UUID, SimCRDTManager<? extends AbstractDataType>> entry : this.getDatastore().entrySet()) {
			entry.getValue().clear();
		}

		this.getDatastore().clear();
	}
	
	/**
	 * Adds a new CRDT to the node
	 *
	 * @param crdt The CRDT to add to the node
	 */
	protected void addCRDT(SimCRDTManager<? extends AbstractDataType> crdt) {
		this.getDatastore().put(crdt.getObjectId(), crdt);
	}

	/**
	 * Pick the ID of a randomly selected CRDT in the datastore
	 *
	 * @return The randomly selected CRDT Identifier value
	 */
	public UUID pickCrdtId() {
		List<UUID> idList = new ArrayList<>(this.getDatastore().keySet());
		return idList.isEmpty() ? null : idList.get(Support.getRandom().nextInt(idList.size()));
	}
	
	/**
	 * Randomly pick a CRDT from the list of them and return to the calling routine
	 *
	 * @return The randomly slected CRDT manager
	 */
	public SimCRDTManager<? extends AbstractDataType> pickCRDT() {
		return this.getDatastore().get(this.pickCrdtId());
	}
	
	/**
	 * TODO: replace this with JAXB / Jackson conversion rather than doing it this way
	 * Convert the data store to a string value
	 *
	 * @return A string representation of the entire datastore.
	 */
	private String datastoreToString() {
		StringBuilder sb = new StringBuilder();
		char separator = '{';
		
		if (this.getDatastore().isEmpty()) {
			sb.append(separator);
		} else {
			for (Map.Entry<UUID, SimCRDTManager<? extends AbstractDataType>> entry : this.getDatastore().entrySet()) {
				sb.append(separator + "\"" + entry.getKey() + "\":" + entry.getValue().toString());
				separator = ',';
			}
		}
		
		sb.append("}");
		
		return sb.toString();
	}
	
	/**
	 * TODO: replace this with JAXB / Jackson conversion rather than doing it this way
	 * Get a partial JSON formated representation of the AbstractNode.  
	 *
	 * @return The JSON formated string representation of this AbstractNode instance
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"nodeId\":\"" + this.getId().toString()  + "\",");
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
}
