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

import java.util.Collection;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.AbstractOperation;
import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonPatchApplicationException;

/**
 * The LastWriteWins class implements a Last Write Wins commutative CRDT.  Operations are stored and recalled in time stamp
 * order.  There is both an add and remove set, where removing an operation takes precedence over adding.  It also contains
 * a list of invalid operations which is used to hold operations which fail during reconstitution of the underlying data element
 */
public class LastWriteWins extends OperationTwoSet {
	
	/** Logger for writing data to the log. */
//	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(LastWriteWins.class);
	
	/** The collection of invalid operations. */
	private Collection<AbstractOperation> invalidOperations;
	
	/**
	 * Return the number of invalid operations in this LWW instance
	 *
	 * @return The number of invalid operations detected in this LWW instance
	 */
	public int invalidOperationsCount() {
		return this.getInvalidOperations().size();
	}

	/**
	 * Get the set of invalid operations.  If there are none, an empty list of invalid operations is 
	 * returned.
	 *
	 * @return Returns the set of invalid operations associated with this CRDT
	 */
	public Collection<AbstractOperation> getInvalidOperations() {
		if (null == this.invalidOperations) {
			this.invalidOperations = new TreeSet<>();
		}
		return this.invalidOperations;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#readValue()
	 */
	@Override
	public JsonNode readValue() {
		JsonNode value = null;
		this.getInvalidOperations().clear();
		for (AbstractOperation update : this.getOperations()) {
			try {
				value = update.processOperation(value);
			} catch (JsonPatchApplicationException e) {
				logger.info(e);
				this.getInvalidOperations().add(update);
			}
		}
		
		return value;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#readValue(long)
	 */
	@Override
	public JsonNode readValue(long timestamp) {
		this.getInvalidOperations().clear();
		JsonNode value = null;
		for (AbstractOperation update : this.getOperations()) {
			if (timestamp > update.getTimeStamp()) {
				try {
					value = update.processOperation(value);
				} catch (JsonPatchApplicationException e) {
					logger.info(e);
					this.getInvalidOperations().add(update);
				}
			}
		}
		
		return value;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.OperationTwoSet#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		this.getInvalidOperations().clear();
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.OperationTwoSet#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"invalid\":" + getArrayString(this.getInvalidOperations()));

		return sb.toString();
	}

}
