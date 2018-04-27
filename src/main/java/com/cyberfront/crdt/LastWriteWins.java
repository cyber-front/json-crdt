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

import java.io.IOException;										// Use this with jsonpatch
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operation.Operation;
import com.cyberfront.crdt.operation.Operation.OperationType;
import com.cyberfront.crdt.support.Support;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
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
		private final Set<Operation> operations;
		
		/** The set of invalid operations detected.  Ideally this is empty, but there are reasons why it may not be empty. */
		private final Collection<Operation> invalidOperations;

		/** Latest operation timestamp to include among the applicable operations */
		private final long timestamp;

		/** JsonNode document containing the result of executing the sequence of operations */
		private final JsonNode document;
		
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
					filter(op -> (op.getTimestamp() <= this.timestamp)).
					collect(Collectors.toList()));
			
			JsonNode doc = null;
			this.invalidOperations = new TreeSet<>();
			
			for (Operation op : this.operations) {
				try {
					doc = op.processOperation(doc);
				} catch (JsonPatchException | IOException e) {
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
			}
			
			this.document = doc;
		}

		/**
		 * Retrieve the document resulting from running the operations in this TrialResult.
		 *
		 * @return The document resulting from running the operations in this TrialResult
		 */
		public JsonNode getDocument() {
			return this.document;
		}

		/**
		 * Retrieve the set of operations in this TrialResult.  
		 *
		 * @return A the set of operations in this TrialResult
		 */
		public Collection<Operation> getOperations() {
			return Operation.copy(this.operations);
		}
		
		/**
		 * Gets the invalid.
		 *
		 * @return the invalid
		 */
		public Collection<Operation> getInvalidOperations() {
			return Operation.copy(this.invalidOperations);
		}
		
		/**
		 * Retrieves the collection of effective operations, those which can actually be processed
		 * @return The collection of operations which can be processed
		 */
		public Collection<Operation> getEffectiveOperations() {
			Collection<Operation> ops = this.getOperations();
			ops.removeAll(this.invalidOperations);
			return ops;
		}

		/**
		 * Retrieve the latest timestamp for the operations in this TrailResult instance
		 *  
		 * @return Latest timestamp for the operatiions in this TrialResult instance 
		 */
		public long getTimestamp() {
			return this.timestamp;
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
			sb.append("\"effective\":" + Support.convert(this.getEffectiveOperations()) + ",");
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
	
	/** Trial contains the state of the object being managed at a particular point in time. */ 
	private TrialResult trial = null;

	/**
	 * Default constructor
	 */
	public LastWriteWins() { }
	
	/**
	 * Copy constructor to extract the contents of the given CRDT to populate this one 
	 * @param crdt Source CRDT to copy
	 */
	public LastWriteWins(LastWriteWins crdt) { super(crdt); }
	
	/**
	 * Constructor specifying the add and remove sets comprising a CRDT 
	 * @param addset Add set to use in this CRDT
	 * @param remset Remove set to use in this CRDT
	 */
	@JsonCreator
	public LastWriteWins(@JsonProperty(ADDSET) Collection<Operation> addset,
						 @JsonProperty(REMSET) Collection<Operation> remset) {
		super(addset, remset);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#readValue()
	 */
	@Override
	@JsonIgnore
	public JsonNode getDocument() {
		return this.getDocument(Long.MAX_VALUE);
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#readValue()
	 */
	@Override
	@JsonIgnore
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
	@JsonIgnore
	public Collection<Operation> getInvalidOperations() {
		if (null == trial) {
			this.trial = new TrialResult(this);
		}
		
		return this.trial.getInvalidOperations();
	}
	
	/**
	 * Get the list of effective operations for the current configuration
	 * @return List of effective operations
	 */
	@JsonIgnore
	public Collection<Operation> getEffectiveOperations() {
		if (null == trial) {
			this.trial = new TrialResult(this);
		}
		
		return this.trial.getEffectiveOperations();
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#isCreated()
	 */
	@Override
	@JsonIgnore
	public boolean isCreated() {
		return doesTypeExist(this.getEffectiveOperations(), OperationType.CREATE);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#isRead()
	 */
	@Override
	@JsonIgnore
	public boolean isRead() {
		return doesTypeExist(this.getEffectiveOperations(), OperationType.READ);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#isUpdated()
	 */
	@Override
	@JsonIgnore
	public boolean isUpdated() {
		return doesTypeExist(this.getEffectiveOperations(), OperationType.UPDATE);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#isDeleted()
	 */
	@Override
	@JsonIgnore
	public boolean isDeleted() {
		return doesTypeExist(this.getEffectiveOperations(), OperationType.DELETE);
	}
	
	/**
	 * Insert an operation to the ADD set 
	 *
	 * @param op The operation to add to the ADD set
	 */
	@Override
	protected void addOperation(Operation op) {
		if (null != op) {
			super.addOperation(op);
			this.trial = null;
		}
	}
	
	/**
	 * Add a collection of operations to the current ADD set
	 * 
	 * @param operations Operations to add to the ADD set
	 */
	protected void addOperation(Collection<Operation> operations) {
		if (null != operations) {
			for (Operation op : operations) {
				this.addOperation(op);
			}
		}
	}
	
	/**
	 * Insert an operation to the REMOVE set 
	 *
	 * @param op The operation to add to the REMOVE set
	 */
	@Override
	protected void remOperation(Operation op) {
		if (null != op) {
			super.remOperation(op);
			this.trial = null;
		}
	}

	/**
	 * Add a collection of operations to the current REMOVE set
	 * 
	 * @param operations Operations to add to the REMOVE set
	 */
	protected void remOperation(Collection<Operation> operations) {
		if (null != operations) {
			for (Operation op : operations) {
				this.remOperation(op);
			}
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
