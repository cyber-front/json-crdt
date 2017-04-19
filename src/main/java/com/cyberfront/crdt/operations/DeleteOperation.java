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
package com.cyberfront.crdt.operations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

// TODO: Auto-generated Javadoc
/**
 * The Class DeleteOperation.
 */
public class DeleteOperation extends AbstractOperation {
	
	/** The Constant logger. */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(DeleteOperation.class);
	
	/**
	 * Instantiates a new delete operation.
	 *
	 * @param timeStamp the time stamp
	 */
	public DeleteOperation(Long timeStamp) {
		super(null, timeStamp);
	}
	
	/**
	 * Instantiates a new delete operation.
	 *
	 * @param src the src
	 */
	public DeleteOperation(DeleteOperation src) {
		super(src);
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#processOperation(com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public JsonNode processOperation(JsonNode document) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (this == obj) || (obj instanceof DeleteOperation && super.equals(obj));
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#hashCode()
	 */
	@Override
	public int hashCode() {
		return 47 * super.hashCode();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#getType()
	 */
	@Override
	public OperationType getType() {
		return OperationType.DELETE;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#copy()
	 */
	@Override
	public AbstractOperation copy() {
		return new DeleteOperation(this);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#isCreated()
	 */
	@Override
	public boolean isCreated() { return false; }

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#isDeleted()
	 */
	@Override
	public boolean isDeleted() { return true; }
}
