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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.cyberfront.crdt.unittest.data.Factory.TYPE;
import com.cyberfront.crdt.unittest.support.WordFactory;

/**
 * This is a concrete class type derived from AbstractDataType used to test the CRDT.  It manages a collection of
 * AbstractDataType derived class instances
 */
public class SimpleCollection extends AbstractDataType {
	
	/** The Collection of AbstractDataType derived values associated with this SimpleCollection instance */
	Collection<AbstractDataType> collectionValue;

	/**
	 * Instantiates a new SimpleCollection instance with a collection of random objects
	 * derived from AbstractDataType.
	 */
	public SimpleCollection() {
		super();
		this.setCollectionValue(Factory.getInstances(WordFactory.getRandom().nextInt(4)));
	}
	
	/**
	 * Copy constructor which uses `src` as the source content for the new instance
	 *
	 * @param src Source data from which to create the new instance
	 */
	public SimpleCollection(SimpleCollection src) {
		super(src);
		
		this.setCollectionValue(src.collectionValue);
	}

	/**
	 * Gets the Collection of values associated with this instance.
	 *
	 * @return The Collection of values
	 */
	public Collection<AbstractDataType> getCollectionValue() {
		if (null == this.collectionValue) {
			this.collectionValue = new ArrayList<>();
		}
		return collectionValue;
	}

	/**
	 * Sets the Collection of value associated with this instance.
	 *
	 * @param value The new Collection of values to set for this instance
	 */
	public void setCollectionValue(Collection<AbstractDataType> value) {
		this.getCollectionValue().clear();
		
		for (AbstractDataType element : value) {
			this.getCollectionValue().add(Factory.copy(element));
		}
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.unittest.data.AbstractDataType#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"collectionValue\":" + WordFactory.convert(this.getCollectionValue()));
		
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#update(java.lang.Double)
	 */
	@Override
	public void update(Double prob) {
		super.update(prob);
		Collection<AbstractDataType> temp = new ArrayList<>();
		
		for (AbstractDataType element : this.collectionValue) {
			double sample = WordFactory.getRandom().nextDouble();
			
			if (sample > prob) {
				temp.add(element);
				this.incrementVersion();
			} else if (sample > 2.0 * prob / 3.0) {
				element.update(prob);
				temp.add(element);
				this.incrementVersion();
			} else if (sample > prob / 3.0) {
				temp.add(Factory.getInstance());
				temp.add(element);
				this.incrementVersion();
			}
		}
		
		this.setCollectionValue(temp);
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
	public TYPE getType() {
		return TYPE.SIMPLE_COLLECTION;
	}
}
