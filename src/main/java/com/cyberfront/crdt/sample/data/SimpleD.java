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
 * This is a concrete class type derived from AbstractDataType used to test the CRDT.  It manages a Boolean value as its
 * extension to the base type
 */
public class SimpleD extends AbstractDataType {
	
	/** The Boolean value associated with the SimpleD type */
	private Boolean booleanValue;

	/**
	 * Instantiates a new SimpleD instance with random values.
	 */
	public SimpleD() {
		super();
		this.setBooleanValue(Support.getRandom().nextBoolean());
	}

	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 */
	public SimpleD(SimpleD src) {
		super(src);
		
		this.booleanValue = src.booleanValue;
	}

	/**
	 * Gets the Boolean value associated with this instance.
	 *
	 * @return the Boolean value
	 */
	public Boolean getBooleanValue() {
		return booleanValue;
	}

	/**
	 * Sets the Boolean value associated with this instance.
	 *
	 * @param value The new Boolean value to set for this instance
	 */
	public void setBooleanValue(Boolean value) {
		this.booleanValue = value;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.sample.data.AbstractDataType#copy()
	 */
	@Override
	public AbstractDataType copy() {
		return new SimpleD(this);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.unittest.data.AbstractDataType#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"booleanValue\":\"" + this.getBooleanValue() + "\"");
		
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#update(java.lang.Double)
	 */
	@Override
	public void update(Double prob) {
		super.update(prob);
		
		if (Support.getRandom().nextDouble() < prob) {
			this.setBooleanValue(Support.getRandom().nextBoolean());
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
		} else if (!(target instanceof SimpleD)) {
			return false;
		} else {
			SimpleD castOther = (SimpleD) target;
			boolean booleanDifference = this.getBooleanValue() == castOther.getBooleanValue();
			return super.equals(castOther) && booleanDifference;
		}
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() * 83 + this.getBooleanValue().hashCode();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#getType()
	 */
	@Override
	public TYPE getType() {
		return TYPE.SIMPLE_D;
	}
}
