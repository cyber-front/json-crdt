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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import com.cyberfront.crdt.sample.data.Factory.DataType;
import com.cyberfront.crdt.support.Support;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is a concrete class type derived from AbstractDataType used to test the CRDT.  It manages a collection of
 * AbstractDataType derived class instances
 */
public class SimpleCollection extends AbstractDataType {
	/** JSON property name for the value stored in any SimpleCollection instances */
	protected final static String VALUE = "collectionValue";
	
	/** The Collection of AbstractDataType derived collectionValue associated with this SimpleCollection instance */
	@JsonProperty(VALUE)
	private final Collection<AbstractDataType> collectionValue;

	/**
	 * Instantiates a new SimpleCollection instance with a collection of random objects
	 * derived from AbstractDataType.
	 */
	public SimpleCollection() {
		super();
		this.collectionValue = new ArrayList<>();
		this.collectionValue.addAll(Factory.getInstances(Support.getRandom().nextInt(4)));
	}
	
	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 */
	public SimpleCollection(SimpleCollection src) {
		this(src.getId(), src.getVersion(), src.getNotes(), src.getCollectionValue());
	}

	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 * @param pChange Probability an individual field will be changed
	 */
	public SimpleCollection(SimpleCollection src, double pChange) {
		super(src, pChange);

		this.collectionValue = new ArrayList<>();
		
		for (AbstractDataType element : this.collectionValue) {
			if (null != element) {
				double sample = Support.getRandom().nextDouble();
				
				if (sample > pChange) {
					this.collectionValue.add(element.copy());
				} else if (sample > 2.0 * pChange / 3.0) {
					this.collectionValue.add(Factory.getInstance());
					this.collectionValue.add(element);
				} else if (sample > pChange / 3.0) {
					this.collectionValue.add(element.copy(pChange));
				}
			}
		}
	}

	/**
	 * Constructor used to fully specify the elements of the SimpleCollection instance
	 * @param id Identifier for the SimpleCollection instance
	 * @param version Version number for the specific instance
	 * @param notes Notes associated with the data instance
	 * @param values Value of the Collection stored in conjunction with this instance
	 */
	@JsonCreator
	public SimpleCollection(@JsonProperty(ID) UUID id,
			@JsonProperty(VERSION) Long version,
			@JsonProperty(NOTES) String notes,
			@JsonProperty(VALUE) Collection<AbstractDataType> values) {
		super(id, version, notes);

		this.collectionValue = new ArrayList<>();
		for (AbstractDataType value : values) {
			this.collectionValue.add(value.copy());
		}
	}

	/**
	 * Gets the Collection of collectionValue associated with this instance.
	 *
	 * @return The Collection of collectionValue
	 */
	@JsonProperty(VALUE)
	public Collection<AbstractDataType> getCollectionValue() {
		return collectionValue;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.sample.data.AbstractDataType#copy()
	 */
	@Override
	public AbstractDataType copy() {
		return new SimpleCollection(this);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.unittest.data.AbstractDataType#getSegment()
	 */
	@Override
	@JsonIgnore
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"collectionValue\":" + Support.convert(this.collectionValue));
		
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#update(java.lang.Double)
	 */
	@Override
	public AbstractDataType copy(Double pChange) {
		return new SimpleCollection(this, pChange);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object target) {
		if (target == this) {
			return true;
		}
		
		if (!(target instanceof SimpleCollection)) {
			return false;
		}
		
		SimpleCollection castOther = (SimpleCollection) target;

		if (this.getCollectionValue().size() != castOther.getCollectionValue().size()) {
			return false;
		}

		Iterator<AbstractDataType> myIterator = this.getCollectionValue().iterator();
		Iterator<AbstractDataType> otherIterator = castOther.getCollectionValue().iterator();
		while (myIterator.hasNext()) {
			if (!myIterator.next().equals(otherIterator.next())) {
				return false;
			}
		}

		return super.equals(target);
	}
	
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() * 79 + this.getCollectionValue().hashCode();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#getType()
	 */
	@Override
	@JsonIgnore
	public DataType getType() {
		return DataType.SIMPLE_COLLECTION;
	}
}
