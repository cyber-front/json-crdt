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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This is an abstract base class for CRDT classes 
 */
public abstract class AbstractCRDT {
	
	/**
	 * This will return true exactly when there is at least one DeleteOperation in the list being maintained by the CRDT 
	 *
	 * @return Returns true exactly when there is at least one DeleteOperation in the list of operations
	 */
	public abstract boolean isDeleted();
	
	/**
	 * This will return true exactly when there is at least one CreateOperation in the list being maintained by the CRDT 
	 *
	 * @return Returns true exactly when there is at least one CreateOperation in the list of operations
	 */
	public abstract boolean isCreated();
	
	/**
	 * Process the operations and return the resulting JsonNode document
	 *
	 * @return The JSON document resulting from processing the operations in the CRDT
	 */
	public abstract JsonNode readValue();
	
	/**
	 * Process the operations up to the given timestamp and return the resulting JSON document
	 *
	 * @param timestamp The timestamp up to which to process the operations
	 * @return The JSON document resulting from processing the operations up to the given timestamp
	 */
	public abstract JsonNode readValue(long timestamp);

	/**
	 * This is used to create a string representation of the CRDT in support of the toString() method
	 *
	 * @return Return the relevant data for the CRDT
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"created\":\"" + this.isCreated() + "\",");
		sb.append("\"deleted\":\"" + this.isDeleted() + "\",");
		sb.append("\"value\":" + this.readValue().toString());

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
