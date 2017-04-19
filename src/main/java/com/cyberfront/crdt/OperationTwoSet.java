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

import com.cyberfront.crdt.operations.AbstractOperation;

/**
 * This is an abstract class which defines a Two Set CRDT.  One set contains operations to use, called an ADD set, and the other contains
 * a set of operations which must not be used, called a REMOVE set.  Elements in the REMOVE set have precedence over those in the ADD set.
 * That is, if an element is contained in the REMOVE set it will not be used if it is in the ADD set, regardless of when it was added to the
 * ADD set.
 */
public abstract class OperationTwoSet extends AbstractCRDT {
	
	/** The ADD set. */
	private Collection<AbstractOperation> addSet;

	/** The REMOVE set. */
	private Collection<AbstractOperation> remSet;
	
	/**
	 * This method retrieved the ADD set.
	 *
	 * @return the ADD set
	 */
	public Collection<AbstractOperation> getAddSet() {
		if (null == this.addSet) {
			this.addSet = new TreeSet<>();
		}
		return this.addSet;
	}

	/**
	 * This method retrieved the REMOVE set.
	 *
	 * @return the REMOVE set
	 */
	public Collection<AbstractOperation> getRemSet() {
		if (null == this.remSet) {
			this.remSet = new TreeSet<>();
		}
		return this.remSet;
	}
	
	/**
	 * Insert an operation to the ADD set 
	 *
	 * @param op The operation to add to the ADD set
	 */
	public void addOperation(AbstractOperation op) {
		this.getAddSet().add(op);
	}
	
	/**
	 * Insert an operation to the REMOVE set 
	 *
	 * @param op The operation to add to the REMOVE set
	 */
	public void remOperation(AbstractOperation op) {
		this.getRemSet().add(op);
	}
	
	/**
	 * This private static function returns a set resulting from removing all of the elements on the RHS from the set on the LHS
	 *
	 * @param lhs The left hand side of the difference operator
	 * @param rhs The right hand side of the difference operator
	 * @return The set of elements resulting from removing all of the elements in RHS from LHS
	 */
	private static Collection<AbstractOperation> diff(Collection<AbstractOperation> lhs, Collection<AbstractOperation> rhs) {
		Collection<AbstractOperation> rv = new TreeSet<>();
		rv.addAll(lhs);
		rv.removeAll(rhs);
		return rv;
	}
	
	/**
	 * This method returns the collection of elements in the ADD set after those in the REMOVE set have been
	 * removed.
	 *
	 * @return The operations which are active in this Two Set CRDT
	 */
	public Collection<AbstractOperation> getOperations() {
		return diff(this.getAddSet(), this.getRemSet());
	}

	/**
	 * This protected static method returns a JSON formated string of a collection of operations to the calling routine
	 *
	 * @param coll The collection to represent as a JSON formated array
	 * @return The JSON string representation of the given collection
	 */
	protected static String getArrayString(Collection<AbstractOperation> coll) {
		StringBuilder sb = new StringBuilder();
		String separator = "[";
		
		if (coll.isEmpty()) {
			sb.append(separator);
		} else {
			for (AbstractOperation op : coll) {
				sb.append(separator + op.toString());
				separator = ",";
			}
		}
		
		sb.append(']');
		
		return sb.toString();
	}

	/**
	 * This method removes all elements in both the ADD and REMOVE sets, effectively reseting them to empty.
	 */
	public void clear() {
		this.getAddSet().clear();
		this.getRemSet().clear();
	}

	/**
	 * This method determines the state of the CRDT.  If both the ADD and REMOVE sets are empty, then this is empty.  Alternatively if the ADD
	 * set is s subset of the REMOVE set, this will also result in this indicating that there are no operations, resulting in this being an
	 * empty set. 
	 *
	 * @return True exactly when the set of active operations is empty
	 */
	public boolean isEmpty() {
		return (this.getAddSet().isEmpty() && this.getRemSet().isEmpty()) || this.getOperations().isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"addPending\":" + getArrayString(this.getAddSet()) + ",");
		sb.append("\"remPending\":" + getArrayString(this.getRemSet()) + ",");
		sb.append("\"operations\":" + getArrayString(this.getOperations()));

		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#isCreated()
	 */
	public boolean isCreated() {
		for (AbstractOperation op : this.getOperations()) {
			if (op.isCreated()) {
				return true;
			}
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.manager.AbstractCRDT#isDeleted()
	 */
	public boolean isDeleted() {
		for (AbstractOperation op : this.getOperations()) {
			if (op.isDeleted()) {
				return true;
			}
		}
		
		return false;
	}
}
