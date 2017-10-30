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
package com.cyberfront.crdt.sample.data;

import com.cyberfront.crdt.sample.data.Factory.TYPE;
import com.cyberfront.crdt.support.Support;

/**
 * This is a concrete class type derived from AbstractDataType used to test the CRDT.  It manages a reference to another
 * AbstractDataType derived class instances
 */
public class SimpleReference extends AbstractDataType {
	
	/** The reference to an AbstractDataType derived values associated with this SimpleReference instance */
	AbstractDataType referenceValue;

	/**
	 * Instantiates a new SimpleReference instance with a collection of random objects
	 * derived from AbstractDataType.
	 */
	public SimpleReference() {
		super();
		this.setReferenceValue(Factory.getInstance());
	}

	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 */
	public SimpleReference(SimpleReference src) {
		super(src);
		this.referenceValue = src.referenceValue.copy();
	}

	/**
	 * Gets the Reference value associated with this instance.
	 *
	 * @return The Reference values
	 */
	public AbstractDataType getReferenceValue() {
		if (null == this.referenceValue) {
			throw new NullPointerException();
		}
		return referenceValue;
	}

	/**
	 * Sets the Reference value associated with this instance.
	 *
	 * @param value The new Reference value to set for this instance
	 */
	public void setReferenceValue(AbstractDataType value) {
		if (null == value) {
			throw new NullPointerException();
		}
		
		this.referenceValue = value.copy();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.sample.data.AbstractDataType#copy()
	 */
	@Override
	public AbstractDataType copy() {
		return new SimpleReference(this);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.unittest.data.AbstractDataType#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"referenceValue\":" + this.getReferenceValue().toString());
		
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#update(java.lang.Double)
	 */
	@Override
	public void update(Double prob) {
		super.update(prob);
		
		if (Support.getRandom().nextDouble() < prob) {
			this.getReferenceValue().update(prob);
			this.incrementVersion();
		}
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object target) {
		if (target == this) {
			return true;
		} else if (!(target instanceof SimpleReference)) {
			return false;
		} else {
			SimpleReference castOther = (SimpleReference) target;
			boolean referenceDifference = this.getReferenceValue().equals(castOther.getReferenceValue());

			return super.equals(castOther) && referenceDifference;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() * 89 + this.getReferenceValue().hashCode();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#getType()
	 */
	@Override
	public TYPE getType() {
		return TYPE.SIMPLE_REFERENCE;
	}
}
