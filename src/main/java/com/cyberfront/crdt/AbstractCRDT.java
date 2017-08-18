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
package com.cyberfront.crdt;

import java.util.Observable;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This is an abstract base class for CRDT classes.  It specifies the standard interfaces for all derived CRDT classes
 */
public abstract class AbstractCRDT extends Observable {
	
	/**
	 * This will return true exactly when there is at least one com.cyberfront.crdt.operations.DeleteOperation in the list being maintained by the CRDT 
	 * @return Returns true exactly when there is at least one com.cyberfront.crdt.operations.DeleteOperation in the list of operations
	 */
	public abstract boolean isDeleted();
	
	/**
	 * This will return true exactly when there is at least one com.cyberfront.crdt.operations.CreateOperation in the list being maintained by the CRDT 
	 * @return Returns true exactly when there is at least one com.cyberfront.crdt.operations.CreateOperation in the list of operations
	 */
	public abstract boolean isCreated();
	
	/**
	 * Count the number of CreateOperation instances are associated with this CRDT
	 * @return Number of CreateOperatiopn instances associated with this CRDT  
	 */
	public abstract long countCreated();
	
	/**
	 * Count the number of ReadOperation instances are associated with this CRDT
	 * @return Number of ReadOperatiopn instances associated with this CRDT  
	 */
	public abstract long countRead();
	
	/**
	 * Count the number of UpdateOperation instances are associated with this CRDT
	 * @return Number of UpdateOperatiopn instances associated with this CRDT  
	 */
	public abstract long countUpdate();
	
	/**
	 * Count the number of DeleteOperation instances are associated with this CRDT
	 * @return Number of DeleteOperatiopn instances associated with this CRDT  
	 */
	public abstract long countDelete();
	
	/**
	 * Process the operations and return the resulting JsonNode document
	 * @return The JSON document resulting from processing the operations in the CRDT
	 */
	public abstract JsonNode readValue();

	/**
	 * This is used to create a string representation of the CRDT in support of the toString() method
	 * @return Return the relevant data for the CRDT
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		JsonNode value = this.readValue();
		
		sb.append("\"created\":\"" + this.isCreated() + "\",");
		sb.append("\"deleted\":\"" + this.isDeleted() + "\",");
		sb.append("\"value\":" + (null == value ? "null" : value));

		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + this.getSegment() + "}";
	}
}
