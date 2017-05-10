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
import com.cyberfront.crdt.unittest.support.WordFactory;

/**
 * This is a concrete class type derived from AbstractDataType used to test the CRDT.  It manages a String value as its
 * extension to the base type
 */
public class SimpleA extends AbstractDataType {
	
	/** The String value associated with the SimpleA type */
	private String stringValue;

	/**
	 * Instantiates a new SimpleA instance with random values.
	 */
	public SimpleA() {
		super();
		this.setStringValue(WordFactory.getLongSequence('-'));
	}
	
	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 */
	public SimpleA(SimpleA src) {
		super(src);
		this.stringValue = src.stringValue;
	}

	/**
	 * Gets the String value associated with this instance.
	 *
	 * @return the string value
	 */
	public String getStringValue() {
		return stringValue;
	}

	/**
	 * Sets the String value associated with this instance.
	 *
	 * @param value The new String value to set for this instance
	 */
	public void setStringValue(String value) {
		this.stringValue = value;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.unittest.data.AbstractDataType#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"stringValue\":\"" + this.getStringValue() + "\"");
		
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#update(java.lang.Double)
	 */
	@Override
	public void update(Double prob) {
		super.update(prob);
		
		if (WordFactory.getRandom().nextDouble() < prob) {
			this.setStringValue(WordFactory.getLongSequence('-'));
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
		} else if (!(target instanceof SimpleA)) {
			return false;
		} else {
			SimpleA castOther = (SimpleA) target;
			boolean stringDifference = this.getStringValue() == castOther.getStringValue();
			return super.equals(castOther) && stringDifference;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() * 67 + this.getStringValue().hashCode();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#getType()
	 */
	@Override
	public TYPE getType() {
		return TYPE.SIMPLE_A;
	}
}
