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
package com.cyberfront.crdt.unittest.data;

import com.cyberfront.crdt.unittest.data.Factory.TYPE;

/**
 * This is a concrete class type derived from AbstractDataType used to test the CRDT.  It manages a Double value as its
 * extension to the base type
 */
public class SimpleC extends AbstractDataType {
	
	/** The Double value associated with the SimpleC type */
	private Double doubleValue;

	/**
	 * Instantiates a new SimpleC instance with random values.
	 */
	public SimpleC() {
		super();
		this.setDoubleValue(WordFactory.getRandom().nextDouble());
	}

	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 */
	public SimpleC(SimpleC src) {
		super(src);
		this.doubleValue = src.doubleValue;
	}

	/**
	 * Gets the Double value associated with this instance.
	 *
	 * @return the Double value
	 */
	public Double getDoubleValue() {
		return doubleValue;
	}

	/**
	 * Sets the Double value associated with this instance.
	 *
	 * @param value The new Double value to set for this instance
	 */
	public void setDoubleValue(Double value) {
		this.doubleValue = value;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.unittest.data.AbstractDataType#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"doubleValue\":\"" + this.getDoubleValue() + "\"");
		
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#update(java.lang.Double)
	 */
	@Override
	public void update(Double prob) {
		super.update(prob);
		
		if (WordFactory.getRandom().nextDouble() < prob) {
			this.setDoubleValue(WordFactory.getRandom().nextDouble());
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
		} else if (!(target instanceof SimpleC)) {
			return false;
		} else {
			SimpleC castOther = (SimpleC) target;
			boolean doubleDifference = this.getDoubleValue() == castOther.getDoubleValue();
			return super.equals(castOther) && doubleDifference;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() * 73 + this.getDoubleValue().hashCode();		
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#getType()
	 */
	@Override
	public TYPE getType() {
		return TYPE.SIMPLE_C;
	}
}
