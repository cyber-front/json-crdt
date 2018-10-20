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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is a concrete class type derived from AbstractDataType used to test the CRDT.  It manages a reference to another
 * AbstractDataType derived class instances
 */
public class SimpleReference extends AbstractDataType {
	/** JSON property name for the value stored in any SimpleReference instances */
	protected final static String VALUE = "referenceValue";
	
	/** The reference to an AbstractDataType derived values associated with this SimpleReference instance */
	@JsonProperty(VALUE)
	private final AbstractDataType referenceValue;

	/**
	 * Instantiates a new SimpleReference instance with a collection of random objects
	 * derived from AbstractDataType.
	 */
	public SimpleReference() {
		super();
		this.referenceValue = Factory.getInstance();
	}

	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 */
	public SimpleReference(SimpleReference src) {
		this(src.getId(), src.getVersion(), src.getNotes(), src.getReferenceValue());
	}

	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 * @param pChange Probability an individual field will be changed
	 */
	public SimpleReference(SimpleReference src, double pChange) {
		super(src, pChange);
		this.referenceValue = Support.getRandom().nextDouble() < pChange ? Factory.getInstance() : src.referenceValue.copy(pChange);
	}

	/**
	 * Constructor used to fully specify the elements of the SimpleReference instance
	 * @param id Identifier for the SimpleReference instance
	 * @param version Version number for the specific instance
	 * @param notes Notes associated with the data instance
	 * @param value Value of the Reference stored in conjunction with this instance
	 */
	@JsonCreator
	public SimpleReference(@JsonProperty(ID) UUID id,
			@JsonProperty(VERSION) Long version,
			@JsonProperty(NOTES) String notes,
			@JsonProperty(VALUE) AbstractDataType value) {
		super(id, version, notes);
		this.referenceValue = value.copy();
	}

	/**
	 * Gets the Reference referenceValue associated with this instance.
	 *
	 * @return The Reference values
	 */
	@JsonProperty(VALUE)
	public AbstractDataType getReferenceValue() {
		return referenceValue;
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
	@JsonIgnore
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
	public AbstractDataType copy(Double pChange) {
		return new SimpleReference(this, pChange);
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
	@JsonIgnore
	public DataType getType() {
		return DataType.SIMPLE_REFERENCE;
	}
}
