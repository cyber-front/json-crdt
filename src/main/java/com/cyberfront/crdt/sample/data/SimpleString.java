/*
 * Copyright (c) 2018 Cybernetic Frontiers LLC
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

import java.util.UUID;

import com.cyberfront.crdt.sample.data.Factory.DataType;
import com.cyberfront.crdt.support.Support;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This is a concrete class type derived from AbstractDataType used to test the CRDT.  It manages a String stringValue as its
 * extension to the base type
 */
public class SimpleString extends AbstractDataType {
	/** JSON property name for the value stored in any SimpleString instances */
	protected final static String VALUE = "stringValue";
	
	/** The String stringValue associated with the SimpleString type */
	@JsonProperty(VALUE)
	private final String stringValue;

	/**
	 * Instantiates a new SimpleString instance with random values.
	 */
	public SimpleString() {
		super();
		this.stringValue = Support.getLongSequence('-');
	}
	
	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 */
	public SimpleString(SimpleString src) {
		this(src.getId(), src.getVersion(), src.getNotes(), src.getStringValue());
	}
	
	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 * @param pChange Probability an individual field will be changed
	 */
	public SimpleString(SimpleString src, double pChange) {
		super(src, pChange);
		this.stringValue = Support.getRandom().nextDouble() < pChange ? Support.getLongSequence('-') : src.stringValue;
	}

	/**
	 * Constructor used to fully specify the elements of the SimpleString instance
	 * @param id Identifier for the SimpleString instance
	 * @param version Version number for the specific instance
	 * @param notes Notes associated with the data instance
	 * @param value Value of the String stored in conjunction with this instance
	 */
	@JsonCreator
	public SimpleString(@JsonProperty(ID) UUID id,
			@JsonProperty(VERSION) Long version,
			@JsonProperty(NOTES) String notes,
			@JsonProperty(VALUE) String value) {
		super(id, version, notes);
		this.stringValue = value;
	}

	/**
	 * Gets the String stringValue associated with this instance.
	 *
	 * @return the string stringValue
	 */
	@JsonProperty(VALUE)
	public String getStringValue() {
		return stringValue;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.sample.data.AbstractDataType#copy()
	 */
	@Override
	public AbstractDataType copy() {
		return new SimpleString(this);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.unittest.data.AbstractDataType#getSegment()
	 */
	@Override
	@JsonIgnore
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
	public AbstractDataType copy(Double pChange) {
		return new SimpleString(this, pChange);
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object target) {
		if (target == this) {
			return true;
		} else if (!(target instanceof SimpleString)) {
			return false;
		} else {
			SimpleString castOther = (SimpleString) target;
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
	@JsonIgnore
	public DataType getType() {
		return DataType.SIMPLE_STRING;
	}
}
