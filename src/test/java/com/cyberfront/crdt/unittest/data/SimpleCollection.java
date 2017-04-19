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

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleCollection.
 */
public class SimpleCollection extends AbstractDataType {
	
	/** The collection value. */
	Collection<AbstractDataType> collectionValue;

	/**
	 * Instantiates a new simple collection.
	 */
	public SimpleCollection() {
		super();
		this.setCollectionValue(Factory.getInstances(WordFactory.getRandom().nextInt(4)));
	}
	
	/**
	 * Instantiates a new simple collection.
	 *
	 * @param src the src
	 */
	public SimpleCollection(SimpleCollection src) {
		super(src);
		
		this.setCollectionValue(src.collectionValue);
	}

	/**
	 * Gets the collection value.
	 *
	 * @return the collection value
	 */
	public Collection<AbstractDataType> getCollectionValue() {
		if (null == this.collectionValue) {
			this.collectionValue = new ArrayList<>();
		}
		return collectionValue;
	}

	/**
	 * Sets the collection value.
	 *
	 * @param value the new collection value
	 */
	public void setCollectionValue(Collection<AbstractDataType> value) {
		this.getCollectionValue().clear();
		
		for (AbstractDataType element : value) {
			this.getCollectionValue().add(Factory.copy(element));
		}
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

	/**
	 * Builds the array string.
	 *
	 * @return the string
	 */
	private String buildArrayString() {
		StringBuilder sb = new StringBuilder();
		String delimiter = "[";
		
		for (AbstractDataType element : this.getCollectionValue()) {
			sb.append(delimiter);
			sb.append(element.toString());
			delimiter = ",";
		}
		sb.append(']');
		
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{");
		sb.append(super.toString());
		sb.append("\"collectionValue\":" + this.buildArrayString() + "");
		sb.append("}");
		
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#getType()
	 */
	@Override
	public TYPE getType() {
		return TYPE.SIMPLE_COLLECTION;
	}
}
