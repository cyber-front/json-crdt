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

import java.io.IOException;                             // Use this with jsonpatch
import com.github.fge.jsonpatch.JsonPatch;				// Use this with jsonpatch
import com.github.fge.jsonpatch.JsonPatchException;		// Use this with jsonpatch
//import com.flipkart.zjsonpatch.JsonPatch;  			// Use this with zjsonpatch


/**
 * The UpdateOperation encapsulates the creation of a new JSON document in the CRDT.  It should have
 * a time stamp following the CreateOperation timestamp for the CRDT and one which precedes any DeleteOperation,
 * if it exists.  No validation of these object to determine their compliance with RFC 6902 is performed. 
 */
public class UpdateOperation extends AbstractOperation {
	
	/** Logger to use when displaying state information */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(UpdateOperation.class);

	/**
	 * This instantiates a new UpdateOperation given an operation in a JsonNode and a timestamp.  The operation
	 * is not validated as conforming to RFC 6902
	 *
	 * @param op The operation, consisting of a JsonNode conforming to RFC 6902.
	 * @param timeStamp The effective time stamp of the operation 
	 */
	public UpdateOperation(JsonNode op, Long timeStamp) {
		super(op, timeStamp);
	}
	
	/**
	 * Instantiates a copy of the given UpdateOperation
	 *
	 * @param src The source UpdateOperation to copy
	 */
	public UpdateOperation(UpdateOperation src) {
		super(src);
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#processOperation(com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public JsonNode processOperation(JsonNode document) throws JsonPatchException, IOException { 		// Use this with jsonpatch
//	public JsonNode processOperation(JsonNode document) {		                                        // Use this with zjsonpatch
		return null == document
				? null
				: JsonPatch.fromJson(this.getOp()).apply(document);		// Use this with jsonpatch
//				: JsonPatch.apply(this.getOp(), document);				// Use this with zjsonpatch
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (this == obj) || (obj instanceof UpdateOperation && super.equals(obj));
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#getType()
	 */
	@Override
	public OperationType getType() {
		return OperationType.UPDATE;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#copy()
	 */
	@Override
	public AbstractOperation copy() {
		return new UpdateOperation(this);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#copy()
	 */
	@Override
	public AbstractOperation mimic() {
		return new UpdateOperation(this.getOp(), this.getTimeStamp());
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
	public boolean isDeleted() { return false; }
}
