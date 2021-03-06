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
package com.cyberfront.crdt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * This is an abstract base class for CRDT classes.  It specifies the standard interfaces for all derived CRDT classes
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @Type(value = OperationTwoSet.class, name = "OperationTwoSet"),
    @Type(value = LastWriteWins.class, name = "LastWriteWins") })
public abstract class AbstractCRDT {
	
	/**
	 * This will return true exactly when there is at least one operation with a type of OperationType.CREATE in the list being maintained by the CRDT 
	 * @return Returns true exactly when there is at least one operation with a type of OperationType.CREATE in the list of operations
	 */
	@JsonIgnore
	public abstract boolean isCreated();
	
	/**
	 * This will return true exactly when there is at least one operation with a type of OperationType.READ in the list being maintained by the CRDT 
	 * @return Returns true exactly when there is at least one operation with a type of OperationType.READ in the list of operations
	 */
	@JsonIgnore
	public abstract boolean isRead();
	
	/**
	 * This will return true exactly when there is at least one operation with a type of OperationType.UPDATE in the list being maintained by the CRDT 
	 * @return Returns true exactly when there is at least one operation with a type of OperationType.UPDATE in the list of operations
	 */
	@JsonIgnore
	public abstract boolean isUpdated();
	
	/**
	 * This will return true exactly when there is at least one operation with a type of OperationType.DELETE in the list being maintained by the CRDT 
	 * @return Returns true exactly when there is at least one operation with a type of OperationType.DELETE in the list of operations
	 */
	@JsonIgnore
	public abstract boolean isDeleted();
	
	/**
	 * Count the number of CREATE Operation instances are associated with this CRDT
	 * @return Number of CREATE Operation instances associated with this CRDT  
	 */
	public abstract long countCreated();
	
	/**
	 * Count the number of READ Operation instances are associated with this CRDT
	 * @return Number of READ Operation instances associated with this CRDT  
	 */
	public abstract long countRead();
	
	/**
	 * Count the number of UPDATE Operation instances are associated with this CRDT
	 * @return Number of UPDATE Operation instances associated with this CRDT  
	 */
	public abstract long countUpdate();
	
	/**
	 * Count the number of DELETE Operation instances are associated with this CRDT
	 * @return Number of DELETE Operation instances associated with this CRDT  
	 */
	public abstract long countDelete();
	
	/**
	 * Process the operations and return the resulting JsonNode document
	 * @return The JSON document resulting from processing the operations in the CRDT
	 */
	@JsonIgnore
	public abstract JsonNode getDocument();
	
	/**
	 * Process the operations and return the resulting JsonNode document
	 * @param timestamp Timestamp effective for getting the document in question
	 * @return The JSON document resulting from processing the operations in the CRDT
	 */
	public abstract JsonNode getDocument(long timestamp);

	/**
	 * This is used to create a string representation of the CRDT in support of the toString() method
	 * @return Return the relevant data for the CRDT
	 */
	@JsonIgnore
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		JsonNode document = this.getDocument();
		
		sb.append("\"created\":\"" + this.isCreated() + "\",");
		sb.append("\"deleted\":\"" + this.isDeleted() + "\",");
		sb.append("\"document\":" + (null == document ? "null" : document));

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
