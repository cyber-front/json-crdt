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
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.unittest.support.WordFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatchApplicationException;

/**
 * The LastWriteWins class implements a Last Write Wins commutative CRDT.  Operations are stored and recalled in time stamp
 * order.  There is both an add and remove set, where removing an operation takes precedence over adding.  It also contains
 * a list of invalid operations which is used to hold operations which fail during reconstitution of the underlying data element
 */
public class LastWriteWins extends OperationTwoSet {
	
	/**
	 * The Class TrialResult.
	 */
	public static class TrialResult {
		
		private static final boolean LOG_INVALID_OPERATIONS = false;
		
		/** The document. */
		private JsonNode document = null;
		
		/** The operations. */
		private Set<AbstractOperation> operations;
		
		/** The invalid. */
		private Set<AbstractOperation> invalidOperations;
		
		/**
		 * Instantiates a new trial result.
		 *
		 * @param crdt the crdt
		 */
		public TrialResult(LastWriteWins crdt) {
			this.getOperations().addAll(crdt.getOpSet());
		}
		
		/**
		 * Instantiates a new trial result.
		 *
		 * @param trial the trial
		 * @param newOps the new ops
		 */
		private TrialResult(TrialResult trial, Collection<AbstractOperation> newOps) {
			this.getOperations().addAll(trial.getOperations());
			this.getOperations().addAll(newOps);
		}
		
		/**
		 * Instantiates a new trial result.
		 *
		 * @param trial the trial
		 * @param newOp the new op
		 */
		private TrialResult(TrialResult trial, AbstractOperation newOp) {
			this.getOperations().addAll(trial.getOperations());
			this.getOperations().add(newOp);
		}
		
		/**
		 * Instantiates a new trial result.
		 *
		 * @param trial0 the trial 0
		 * @param trial1 the trial 1
		 */
		private TrialResult(TrialResult trial0, TrialResult trial1) {
			this(trial0, trial1.getOperations());
		}
		
		/**
		 * Gets the document.
		 *
		 * @return the document
		 */
		public JsonNode getDocument() {
			if (null == this.document) {
				this.processOperations();
			}
			return this.document;
		}

		/**
		 * Clear results.
		 */
		private void clearResults() {
			this.document = null;
			this.invalidOperations = null;
		}

		/**
		 * Gets the operations.
		 *
		 * @return the operations
		 */
		public Set<AbstractOperation> getOperations() {
			this.clearResults();

			if (null == this.operations) {
				this.operations = new TreeSet<>();
			}

			return this.operations;
		}
		
		/**
		 * Gets the invalid.
		 *
		 * @return the invalid
		 */
		public Set<AbstractOperation> getInvalidOperations() {
			if (null == this.invalidOperations) {
				this.invalidOperations = new TreeSet<>();
			}
			
			return this.invalidOperations;
		}

		/**
		 * Process operations.
		 */
		private void processOperations() {
			this.clearResults();
			
			for (AbstractOperation update : this.getOperations()) {
				try {
					this.document = update.processOperation(this.document);
				} catch (JsonPatchApplicationException e) {
					if (LOG_INVALID_OPERATIONS) {
						logger.info(e);
					}
					this.getInvalidOperations().add(update);
				}
			}
		}
		
		/**
		 * Gets the diff.
		 *
		 * @param source the source
		 * @param target the target
		 * @return the diff
		 */
		public static JsonNode getDiff(TrialResult source, TrialResult target) {
			return JsonDiff.asJson(source.getDocument(), target.getDocument());
		}
		
		/**
		 * Merge.
		 *
		 * @param newOps the new ops
		 * @return the trial result
		 */
		public TrialResult merge(Collection<AbstractOperation> newOps) {
			return new TrialResult(this, newOps);
		}
		
		/**
		 * Merge.
		 *
		 * @param newOp the new op
		 * @return the trial result
		 */
		public TrialResult merge(AbstractOperation newOp) {
			return new TrialResult(this, newOp);
		}

		/**
		 * Merge.
		 *
		 * @param trial the trial
		 * @return the trial result
		 */
		public TrialResult merge(TrialResult trial) {
			return new TrialResult(this, trial);
		}
		
		/**
		 * Gets the segment.
		 *
		 * @return the segment
		 */
		protected String getSegment() {
			StringBuilder sb = new StringBuilder();
			JsonNode document = this.getDocument();
			
			sb.append("\"operations\":" + WordFactory.convert(this.getOperations()) + ",");
			sb.append("\"invalid\":" + WordFactory.convert(this.getInvalidOperations()) + ",");
			sb.append("\"document\":" + (null == document ? "null" : document.toString()));
			
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
	
	private TrialResult trial = null;

	/**
	 * Retrieve a trial result based on the current collection of operations in the CRDT
	 * @return TrailResult instance derived from the operations in this CRDT instance
	 */
	public TrialResult getTrial() {
		if (null == this.trial) {
			this.trial = new TrialResult(this);
		}
		
		return this.trial;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#readValue()
	 */
	@Override
	public JsonNode readValue() {
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
	 * Insert an operation to the ADD set 
	 *
	 * @param op The operation to add to the ADD set
	 */
	@Override
	protected void addOperation(AbstractOperation op) {
		this.trial = null;
		this.getAddSet().add(op);
	}
	
	/**
	 * Insert an operation to the REMOVE set 
	 *
	 * @param op The operation to add to the REMOVE set
	 */
	@Override
	protected void remOperation(AbstractOperation op) {
		this.trial = null;
		this.getRemSet().add(op);
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

}
