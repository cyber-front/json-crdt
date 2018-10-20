/*
 * Copyright (c) 2018 Cybernetic Frontiers LLC
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
package com.cyberfront.crdt.sample.manager;

import com.cyberfront.crdt.CRDTManager;
import com.cyberfront.crdt.LastWriteWins;
import com.cyberfront.crdt.operation.OperationManager;
import com.cyberfront.crdt.operation.OperationManager.StatusType;
import com.cyberfront.crdt.operation.Operation;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class JsonManager provides a simple mechanism for managing raw JSON content as a CRDT.  It supports the basic CRUD operations on
 * JSON objects.  The create operation results in an empty JSON object, and instantiation of any JSON properties are handled
 * exclusively as updates.
 */
public class JsonManager extends CRDTManager {
	
	/**
	 * Instantiates a new, empty, JSON manager.
	 */
//	public JsonManager() {}
	
	/**
	 * Instantiates a new, empty, JSON manager with the given timestamp  
	 *
	 * @param timestamp the timestamp
	 */
	public JsonManager(long timestamp) {
		Operation create = CRDTManager.generateCreate(timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, create);
		this.push(mgr);
	}

	/**
	 * Constructor specifying the CRDT to manage
	 * @param crdt LastWriteWins CRDT to use in this instance
	 */
	public JsonManager(@JsonProperty(CRDT) LastWriteWins crdt) {
		super(crdt);
	}
	
	/**
	 * Read the contents of the current CRDT instance as of the given timestamp
	 *
	 * @param timestamp The timestamp up to which the CRDT should be read 
	 * @return the json node
	 */
	public JsonNode read(long timestamp) {
		Operation read = CRDTManager.generateRead(timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, read);
		this.push(mgr);
		return this.getCrdt().getDocument(timestamp);
	}
	
	/**
	 * Update the CRDT so that the document has the given value at the given timestamp
	 *
	 * @param document New value of the document at the given timestamp
	 * @param timestamp Effective timestamp of the document
	 */
	public void update(JsonNode document, long timestamp) {
		Operation update = CRDTManager.generateUpdate(this.getCrdt().getDocument(timestamp), document, timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, update);
		this.push(mgr);
	}
	
	/**
	 * Perform a delete operation on the CRDT at the given timestamp
	 *
	 * @param timestamp Timestamp for the delete operation
	 */
	public void delete(long timestamp) {
		Operation delete = CRDTManager.generateDelete(timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, delete);
		this.push(mgr);
	}
}
