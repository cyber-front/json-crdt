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

import java.util.UUID;

import com.cyberfront.crdt.sample.data.Factory.DataType;
import com.cyberfront.crdt.support.Support;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is a concrete class type derived from AbstractDataType used to test the CRDT.  It manages a Boolean booleanValue as its
 * extension to the base type
 */
public class SimpleBoolean extends AbstractDataType {
	/** JSON property name for the value stored in any SimpleBoolean instances */
	protected final static String VALUE = "booleanValue";
	
	/** The Boolean booleanValue associated with the SimpleBoolean type */
	@JsonProperty(VALUE)
	private final Boolean booleanValue;

	/**
	 * Instantiates a new SimpleBoolean instance with random values.
	 */
	public SimpleBoolean() {
		super();
		this.booleanValue = Support.getRandom().nextBoolean();
	}

	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 */
	public SimpleBoolean(SimpleBoolean src) {
		this(src.getId(), src.getVersion(), src.getNotes(), src.getBooleanValue());
	}

	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 * @param pChange Probability an individual field will be changed
	 */
	public SimpleBoolean(SimpleBoolean src, double pChange) {
		super(src, pChange);
		this.booleanValue = Support.getRandom().nextDouble() < pChange ? Support.getRandom().nextBoolean() : src.getBooleanValue();
	}

	/**
	 * Constructor used to fully specify the elements of the SimpleBoolean instance
	 * @param id Identifier for the SimpleBoolean instance
	 * @param version Version number for the specific instance
	 * @param notes Notes associated with the data instance
	 * @param value Value of the Boolean stored in conjunction with this instance
	 */
	@JsonCreator
	public SimpleBoolean(@JsonProperty(ID) UUID id,
			@JsonProperty(VERSION) Long version,
			@JsonProperty(NOTES) String notes,
			@JsonProperty(VALUE) Boolean value) {
		super(id, version, notes);
		this.booleanValue = value;
	}

	/**
	 * Gets the Boolean booleanValue associated with this instance.
	 *
	 * @return the Boolean booleanValue
	 */
	@JsonProperty(VALUE)
	public Boolean getBooleanValue() {
		return booleanValue;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.sample.data.AbstractDataType#copy()
	 */
	@Override
	public AbstractDataType copy() {
		return new SimpleBoolean(this);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.unittest.data.AbstractDataType#getSegment()
	 */
	@Override
	@JsonIgnore
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
	public AbstractDataType copy(Double pChange) {
		return new SimpleBoolean(this, pChange);
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object target) {
		if (target == this) {
			return true;
		} else if (!(target instanceof SimpleBoolean)) {
			return false;
		} else {
			SimpleBoolean castOther = (SimpleBoolean) target;
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
	@JsonIgnore
	public DataType getType() {
		return DataType.SIMPLE_BOOLEAN;
	}
}
