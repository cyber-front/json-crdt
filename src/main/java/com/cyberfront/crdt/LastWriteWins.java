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

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.operations.AbstractOperation.OperationType;
import com.cyberfront.crdt.support.Support;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;										// Use this with jsonpatch
import com.github.fge.jsonpatch.JsonPatchException;				// Use this with jsonpatch
//import com.flipkart.zjsonpatch.JsonPatchApplicationException;		// Use this with zjsonpatch

/**
 * The LastWriteWins class implements a Last Write Wins commutative CRDT.  Operations are stored and recalled in time stamp
 * order.  There is both an add and remove set, where removing an operation takes precedence over adding.  It also contains
 * a list of invalid operations which is used to hold operations which fail during reconstitution of the underlying data element
 */
public class LastWriteWins extends OperationTwoSet {
	
	/**
	 * The Class TrialResult is used to process a collection of operations provided to it.  It is intended to augment the LastWriteWins class
	 * by providing an auxiliary location for storing a single set of operations and to manage access to the resulting JsonNode when the
	 * operations are processed.  It also tracks any invalid operations which are in the set of operations.  Invalid operations are those which
	 * cannot be processed due to a difference in the way the JSON operations are performed on different nodes.  These invalid operations are 
	 * ignored when producing the resulting document, and are stored for later reference if needed.
	 * 
	 * TrialResults are used only for generating final values.  Since READ operations do not change the value of the resulting object, they are 
	 * filtered from collection of operations used to generate the resulting JSON representation of the reconstructed object.
	 */
	public static class TrialResult {
		
		/** Flag to indicate whether invalid operations are to be logged to the console */
//		@SuppressWarnings("unused")
		private static final boolean LOG_JSON_PROCESSING_EXCEPTIONS = false;
		
		/** The set of operations to process; they are processed in timestamp order */
		private final Set<AbstractOperation> operations;
		
		/** The set of invalid operations detected.  Ideally this is empty, but there are reasons why it may not be empty. */
		private Collection<AbstractOperation> invalidOperations;

		/** Latest operation timestamp to include among the applicable operations */
		private final long timestamp;

		/** JsonNode document containing the result of executing the sequence of operations */
		private JsonNode document;
		
		/**
		 * Instantiates a new trial result given a CRDT to process; the operations list is copied from
		 * the CRDT instance to be processed and used to generate the resulting JSON object.
		 *
		 * @param crdt The CRDT to process
		 */
		public TrialResult(LastWriteWins crdt) {
			this(crdt, Long.MAX_VALUE);
		}
		
		/**
		 * Instantiates a new trial result given a CRDT to process; the operations list is copied from
		 * the CRDT instance to be processed and used to generate the resulting JSON object.
		 *
		 * @param timestamp Latest timestamp to process operations
		 * @param crdt The CRDT to process
		 */
		public TrialResult(LastWriteWins crdt, long timestamp) {
			this.timestamp = timestamp;
			this.operations = new TreeSet<>();
			this.operations.addAll(
					crdt.getOpsSet().
					stream().
					filter(op -> (OperationType.READ != op.getType())).
					filter(op -> (op.getTimeStamp() <= this.timestamp)).
					collect(Collectors.toList()));
		}

		/**
		 * Retrieve the document resulting from running the operations in this TrialResult.
		 *
		 * @return The document resulting from running the operations in this TrialResult
		 */
		public JsonNode getDocument() {
			if (null == this.document || null == this.invalidOperations) {
				this.invalidOperations = new ArrayList<>();
				for (AbstractOperation op : this.getOperations()) {
					this.document = this.applyOperation(this.document, op);
				}
			}
			
			return this.document;
		}

		/**
		 * Retrieve the set of operations in this TrialResult.  
		 *
		 * @return A the set of operations in this TrialResult
		 */
		private Set<AbstractOperation> getOperations() {
			return this.operations;
		}
		
		/**
		 * Gets the invalid.
		 *
		 * @return the invalid
		 */
		public Collection<AbstractOperation> getInvalidOperations() {
			if (null == this.invalidOperations) {
				this.getDocument();
			}

			assertNotNull("Invalid Operation List was not allocated", this.invalidOperations);

			return this.invalidOperations;
		}

		/**
		 * Retrieve the latest timestamp for the operations in this TrailResult instance
		 *  
		 * @return Latest timestamp for the operatiions in this TrialResult instance 
		 */
		public long getTimestamp() {
			return this.timestamp;
		}

		private JsonNode applyOperation(JsonNode document, AbstractOperation op) {
			if (null == this.invalidOperations) {
				this.invalidOperations = new TreeSet<>();
			}

			try {
				return op.processOperation(document);
			} catch (JsonPatchException | IOException e) {  // Use this with jsonpatch
//			} catch (JsonPatchApplicationException e) {		// Use this with zjsonpatch
				if (LOG_JSON_PROCESSING_EXCEPTIONS) {
					logger.error(e);
					logger.error(" op: " + op.toString());
					logger.error("doc: " + document);
					for (StackTraceElement el : e.getStackTrace()) {
						logger.error(el);
					}
				}
				this.invalidOperations.add(op);
			}
			return document;
		}
		
		/**
		 * Retrieve a string segment used in the toString() method to build up JSON formatted string used primarily
		 * by the toString() method
		 *
		 * @return The JSON formated string segment
		 */
		protected String getSegment() {
			StringBuilder sb = new StringBuilder();
			
			sb.append("\"operations\":" + Support.convert(this.getOperations()) + ",");
			sb.append("\"invalid\":" + Support.convert(this.getInvalidOperations()) + ",");
			sb.append("\"timestamp\":" + this.getTimestamp() + ",");
			sb.append("\"document\":" + (null == this.document ? "null" : this.document.toString()));
			
			return sb.toString();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "{" + this.getSegment() + "}";
		}
	}

	/** Logger for writing data to the log. */
//	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(LastWriteWins.class);
	
	private TrialResult trial = null;
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#readValue()
	 */
	@Override
	public JsonNode getDocument() {
		return this.getDocument(Long.MAX_VALUE);
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#readValue()
	 */
	@Override
	public JsonNode getDocument(long timestamp) {
		if (null == this.trial || this.trial.getTimestamp() != timestamp) {
			this.trial = new TrialResult(this, timestamp);
		}

		return this.trial.getDocument();
	}
	
	/**
	 * Get the list of invalid operations for the current configuration
	 * @return List of invalid operations
	 */
	public Collection<AbstractOperation> getInvalidOperations() {
		if (null == trial) {
			this.trial = new TrialResult(this);
		}
		
		return this.trial.getInvalidOperations();
	}
	
	/**
	 * Insert an operation to the ADD set 
	 *
	 * @param op The operation to add to the ADD set
	 */
	@Override
	protected void addOperation(AbstractOperation op) {
		if (null != op) {
			super.addOperation(op);
			this.trial = null;
		}
	}
	
	/**
	 * Insert an operation to the REMOVE set 
	 *
	 * @param op The operation to add to the REMOVE set
	 */
	@Override
	protected void remOperation(AbstractOperation op) {
		if (null != op) {
			super.remOperation(op);
			this.trial = null;
		}
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.OperationTwoSet#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"trial\":" + (null == this.trial ? "null" : this.trial.toString()));

		return sb.toString();
	}
}
