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
import java.io.IOException;				                // Use this with jsonpatch
import com.github.fge.jsonpatch.JsonPatch;				// Use this with jsonpatch
import com.github.fge.jsonpatch.JsonPatchException;		// Use this with jsonpatch
//import com.flipkart.zjsonpatch.JsonPatch;					// Use this with zjsonpatch

/**
 * The CreateOperation encapsulates the creation of a new JSON document in the CRDT.  It should have
 * the lowest timestamp of any operation contained in the CRDT and the JsonNode should be derived exclusively of
 * "add" elements.  When processed, it will ignore any operations with an earlier timestamp and therefore should
 * only be used exactly once in each CRDT instance.  Further, these operations should be JSON objects adhering to
 * RFC 6902.  That said, no validation of these object to determine their compliance with RFC 6902 is performed. 
 */
public class CreateOperation extends AbstractOperation {
	
	/** Logger to use when displaying state information */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CreateOperation.class);

	/**
	 * This instantiates a new CreateOperation given an operation in a JsonNode and a timestamp.  The operation
	 * is not validated as conforming to RFC 6902 or for containing only "add" operations
	 *
	 * @param op The operation, consisting of a JsonNode with only "add" operations and conforming to RFC 6902.
	 * @param timeStamp The effective time stamp of the operation 
	 */
	public CreateOperation(JsonNode op, Long timeStamp) {
		super(op, timeStamp);
	}
	
	/**
	 * Instantiates a copy of the given CreateOperation
	 *
	 * @param src The source CreateOperation to copy
	 */
	public CreateOperation(CreateOperation src) {
		super(src);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#processOperation(com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public JsonNode processOperation(JsonNode document) throws JsonPatchException, IOException {		// Use this with jsonpatch 
//	public JsonNode processOperation(JsonNode document) {                                               // Use this with zjsonpatch
		return JsonPatch.fromJson(this.getOp()).apply(getMapper().createObjectNode());		// Use this with jsonpatch 
//		return JsonPatch.apply(this.getOp(), getMapper().createObjectNode());				// Use this with zjsonpatch
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#getType()
	 */
	@Override
	public OperationType getType() {
		return OperationType.CREATE;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (this == obj) || (obj instanceof CreateOperation && super.equals(obj));
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#copy()
	 */
	@Override
	public AbstractOperation copy() {
		return new CreateOperation(this);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#copy()
	 */
	@Override
	public AbstractOperation mimic() {
		return new CreateOperation(this.getOp(), this.getTimeStamp());
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#isCreated()
	 */
	@Override
	public boolean isCreated() { return true; }

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.operations.AbstractOperation#isDeleted()
	 */
	@Override
	public boolean isDeleted() { return false; }
}
