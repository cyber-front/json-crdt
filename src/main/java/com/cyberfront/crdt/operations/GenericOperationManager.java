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
package com.cyberfront.crdt.operations;

import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.operations.OperationManager;

/**
 * The Class GenericOperationManager provides a mechanism for managing java types as a CRDT by wrapping an object by a collection
 * of operations on that object.
 *
 * @param <T> The generic type to manage.  The type will need to be annotated to support Jackson serializing as a JSON object.  See
 * com.cyberfront.crdt.unittest.data.AbstractDataType for an example of this annotation.  Also, the type T should have a constructor which
 * accepts no arguments.
 */
public class GenericOperationManager<T>
	extends OperationManager {
	
	/** The object class to instantiate new objects. */
	private Class<T> objectClass;
	
	/**
	 * Instantiates a new operation manager.
	 * @param status Status field value for the new operation
	 * @param operation Operation to manage
	 * @param objectClass Underlying Class type of the object being managed
	 */
	public GenericOperationManager(StatusType status, AbstractOperation operation, Class<T> objectClass) {
		super(status, operation);
		this.setObjectClass(objectClass);
	}
	
	/**
	 * A constructor which copies the elements of the src argument
	 *
	 * @param src Source object to copy
	 */
	public GenericOperationManager(GenericOperationManager<T> src) {
		super(src);
		this.setObjectClass(src.getObjectClass());
	}
	
	/**
	 * Gets the Class for the type being managed.
	 *
	 * @return The Class for the managed type
	 */
	public Class<T> getObjectClass() {
		return this.objectClass;
	}

	/**
	 * Sets the Class for the type being managed.
	 *
	 * @param objectClass the Class for the managed type
	 */
	private void setObjectClass(Class<T> objectClass) {
		this.objectClass = objectClass;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj || !(obj instanceof GenericOperationManager<?>) || !super.equals(obj)) { 
			return false;
		}
		
		GenericOperationManager<?> mgr = (GenericOperationManager<?>) obj;
		
		return this.getOperation().equals(mgr.getOperation());
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		
		hash = 43 * hash + this.getObjectClass().hashCode();
		
		return hash;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.getSegment() + ",");
		sb.append("\"objectClass\":\"" + this.getObjectClass().getName() + "\",");
		
		return sb.toString();
	}
}
