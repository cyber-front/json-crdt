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

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.operations.AbstractOperation.OperationType;
import com.cyberfront.crdt.support.Support;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatchException;				// Use this with jsonpatch
//import com.flipkart.zjsonpatch.JsonPatchApplicationException;	// Use this with zjsonpatch

/**
 * The LastWriteWins class implements a Last Write Wins commutative CRDT.  Operations are stored and recalled in time stamp
 * order.  There is both an add and remove set, where removing an operation takes precedence over adding.  It also contains
 * a list of invalid operations which is used to hold operations which fail during reconstitution of the underlying data element
 */
public class LastWriteWins extends OperationTwoSet implements Observer {
	
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
		private static final boolean LOG_JSON_PROCESSING_EXCEPTIONS = false;
		
		/** The document which results from processing all of the operations in the Trial. */
		private JsonNode document = null;
		
		/** The set of operations to process; they are processed in timestamp order */
		private Set<AbstractOperation> operations;
		
		/** The set of invalid operations detected.  Ideally this is empty, but there are reasons why it may not be empty. */
		private Set<AbstractOperation> invalidOperations;
		
		/**
		 * Instantiates a new trial result given a CRDT to process; the operations list is copied from
		 * the CRDT instance to be processed and used to generate the resulting JSON object.
		 *
		 * @param crdt The CRDT to process
		 */
		public TrialResult(LastWriteWins crdt) {
			this.getOperations().addAll(crdt.getOpsSet().stream()
														.filter(op -> (OperationType.READ != op.getType()))
														.collect(Collectors.toList()));
		}

		/**
		 * Instantiates a new TrialResult instance from an existing TrialResult and a new collection of operations.
		 * The resulting operations are the union of those in the provided trial and those in the newOps collection passed
		 *
		 * @param trial The trial containing the initial set of operations to copy into this TrialResult.
		 * @param newOps The new operations to add to those given in trial
		 */
		private TrialResult(TrialResult trial, Collection<AbstractOperation> newOps) {
			this.getOperations().addAll(trial.getOperations());
			this.getOperations().addAll(newOps.stream()
											  .filter(op -> (OperationType.READ != op.getType()))
											  .collect(Collectors.toList()));
		}
		
		/**
		 * Instantiates a new TrialResult instance from an existing TrialResult and a single operation.
		 * The resulting operations are the union of those in the provided trial and those in the newOp passed
		 *
		 * @param trial The trial containing the initial set of operations to copy into this TrialResult.
		 * @param newOp The new operation to add to those given in trial
		 */
		private TrialResult(TrialResult trial, AbstractOperation newOp) {
			this.getOperations().addAll(trial.getOperations());
			if (OperationType.READ != newOp.getType()) {
				this.getOperations().add(newOp);
			}
		}
		
		/**
		 * Instantiates a new TrialResult from the merger of two TrialResults
		 *
		 * @param trial0 The first TrialResult to use as the basis for the new TrialResult
		 * @param trial1 The second TrialResult to use as the basis for the new TrialResult
		 */
		private TrialResult(TrialResult trial0, TrialResult trial1) {
			this(trial0, trial1.getOperations());
		}
		
		/**
		 * Retrieve the document resulting from running the operations in this TrialResult.
		 *
		 * @return The document resulting from running the operations in this TrialResult
		 */
		public JsonNode getDocument() {
			if (null == this.document) {
				this.processOperations();
			}

			return this.document;
		}

		/**
		 * Clear the document and invalid operations to initialize processing the TrialResult
		 */
		private void clearResults() {
			this.document = null;
			this.invalidOperations = new TreeSet<>();
		}

		/**
		 * Retrieve the set of operations in this TrialResult.  
		 *
		 * @return A the set of operations in this TrialResult
		 */
		public Set<AbstractOperation> getOperations() {
			if (null == this.operations) {
				this.operations = new TreeSet<>();
			}
			
			this.clearResults();
			return this.operations;
		}
		
		/**
		 * Gets the invalid.
		 *
		 * @return the invalid
		 */
		public Set<AbstractOperation> getInvalidOperations() {
			Set<AbstractOperation> rv = new TreeSet<>();
			if (null != this.invalidOperations) {
				rv.addAll(this.invalidOperations);
			}
			return rv;
		}

		/**
		 * Process the set of operations compute both the resulting JSON document and the set of 
		 * invalid operations.
		 */
		private void processOperations() {
			this.clearResults();
			
			for (AbstractOperation op : this.getOperations()) {
				try {
					this.document = op.processOperation(this.document);
				} catch (JsonPatchException | IOException e) {	// Use this with jsonpatch
//				} catch (JsonPatchApplicationException e) {		// Use this with zjsonpatch
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
		}
		
		/**
		 * Create a new TrialResult which is the result of merging this one with a collection of operations
		 *
		 * @param newOps Set of new operations to merge with this TrialResult 
		 * @return The new TrialResult which merges this with the operations in the list
		 */
		public TrialResult merge(Collection<AbstractOperation> newOps) {
			return new TrialResult(this, newOps);
		}
		
		/**
		 * Create a new TrialResult which is the result of merging this one with a single operation
		 *
		 * @param newOp New operation to merge with this TrialResult 
		 * @return The new TrialResult which merges this with the operation
		 */
		public TrialResult merge(AbstractOperation newOp) {
			return new TrialResult(this, newOp);
		}

		/**
		 * Create a new TrialResult which is the result of merging this one with another TrialResult
		 *
		 * @param trial The other TrialResult to merge with this TrialResult
		 * @return The resulting TrialResult instance to merge with this TrialResult
		 */
		public TrialResult merge(TrialResult trial) {
			return new TrialResult(this, trial);
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
	private static final Logger logger = LogManager.getLogger(LastWriteWins.class);
	
	/** The TrialResult associated with this LastWriteWins instance. */
	private TrialResult trial = null;
	
	/**
	 * Default constructor used to initialize this as its own observer for changes to the 
	 * set entries for the CRDT
	 */
	public LastWriteWins() {
		this.addObserver(this);
	}

	/**
	 * Retrieve a trial result based on the current collection of operations in the CRDT
	 * @return TrailResult instance derived from the operations in this CRDT instance
	 */
	private TrialResult getTrial() {
//		if (null == this.trial) {
			this.trial = new TrialResult(this);
//		}
		
		return this.trial;
	}
	
	/**
	 * Clear the trial value so if can be regenerated on the next read operation
	 */
	public void clearTrial() {
		this.trial = null;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#readValue()
	 */
	@Override
	public JsonNode getDocument() {
		return this.getTrial().getDocument();
	}
	
	/**
	 * Get the list of invalid operations for the current configuration
	 * @return List of invalid operations
	 */
	public Collection<AbstractOperation> getInvalidOperations() {
		return this.getTrial().getInvalidOperations();
	}

	/**
	 * Initiate a reset by invoking the observers which should cause a complete reset across all 
	 * uses of the CRDT.
	 */
	public void resetObservers() {
		this.setChanged();
		this.notifyObservers();
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
			resetObservers();
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
			resetObservers();
		}
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.OperationTwoSet#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"trial\":" + this.getTrial().toString());

		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		this.clearTrial();
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.OperationTwoSet#clear()
	 */
	public void clear() {
		super.clear();
		this.clearTrial();
		this.resetObservers();
	}
}
