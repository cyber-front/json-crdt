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
package com.cyberfront.crdt;

import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.CreateOperation;
import com.cyberfront.crdt.operations.DeleteOperation;
import com.cyberfront.crdt.operations.ReadOperation;
import com.cyberfront.crdt.operations.UpdateOperation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class GenericCRDTManager is used to manage a Plain Old Java Object (POJO).  Internally changes are represented as a series of
 * JSON operations but to the external interface, the object type being managed is given by the generic parameter T 
 *
 * @param <T> The generic type to manage.  The type will need to be annotated to support Jackson serializing as a JSON object.  See
 * com.cyberfront.crdt.unittest.data.AbstractDataType for an example of this annotation.  Also, the type T should have a constructor which
 * accepts no arguments.
 */
public class GenericCRDTManager <T>
	extends CRDTManager implements Observer {

	/** A logger for writing to the local log output. */
	private static final Logger logger = LogManager.getLogger(GenericCRDTManager.class);

	/** This is the object at the current state of the CRDT.  It is memoized, meaning, when available and nor */
	private T object = null;

	/** The object class. */
	private Class<T> objectClass;
	
	private static final boolean LOG_JSON_PROCESSING_EXCEPTIONS = true; 
	private static final boolean TERMINATE_ON_JSON_PROCESSING_EXCEPTIONS = true; 
	
	/**
	 * Instantiates a new CRDT manager.
	 *
	 * @param objectClass the object class
	 */
	public GenericCRDTManager(Class<T> objectClass) {
		this.getCrdt().addObserver(this);
		this.setObjectClass(objectClass);
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
	/**
	 * Gets the object.
	 *
	 * @return the object
	 */
	public T getObject() {
		if (null == this.object) {
			this.updateObject();
		}
		this.updateObject();
		return this.object;
	}
	
	/**
	 * Set the value of the object the CRDT represents
	 *
	 * @param object The new value of the object the CRDT represents
	 */
	private void setObject(T object) {
		this.object = object;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.CRDTManager#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		this.setObject(null);
	}
	
	/**
	 * Update the object being managed by the CRDT to have the final value associated with the operations currently
	 * held in the CRDT.  If something goes wrong, the value for the stored object is null. 
	 */
	protected void updateObject() {
		JsonNode json = this.getCrdt().getDocument();

		if (null != json) {
			try {
				this.setObject(getMapper().treeToValue(json, this.getObjectClass()));
			} catch (JsonProcessingException e) {
				if (LOG_JSON_PROCESSING_EXCEPTIONS) {
					logger.error(e);
					logger.error("json: " + json.toString());
					logger.error("this.getObjectClass(): " + this.getObjectClass().getName());
					logger.error("crdt: " + this.getCrdt().toString());
					e.printStackTrace();
				}
				
				if (TERMINATE_ON_JSON_PROCESSING_EXCEPTIONS) {
					System.exit(0);
				}

				this.getCrdt().resetObservers();
			}
		} else {
			this.getCrdt().resetObservers();
		}
	}
	
	/**
	 * Allocate and return a new CreateOperation for the given timestamp
	 *
	 * @param timestamp the timestamp
	 * @return The CreateOperations generated
	 * @throws ReflectiveOperationException Exception thrown when something prevents a new instance of T to be
	 * generated, such as having no default constructor
	 */
	public CreateOperation generateCreate(long timestamp) throws ReflectiveOperationException {
		return this.generateCreate(timestamp, this.getObjectClass().newInstance());
	}

	/**
	 * Generate and return a CreateOperation for the given object passed 
	 *
	 * @param timestamp Time stamp associated with the CreateOperation
	 * @param object The object from which to generate the CreateOperation
	 * @return The resulting CreateOperation
	 */
	public CreateOperation generateCreate(long timestamp, T object) {
		return generateCreateOperation(getMapper().valueToTree(object), timestamp);
	}
	
	/**
	 * Generate and return a ReadOperation for the given object passed 
	 *
	 * @param timestamp Time stamp associated with the ReadOperation
	 * @return The resulting ReadOperation
	 */
	protected ReadOperation generateRead(long timestamp) {
		return generateReadOperation(timestamp);
	}
	
	/**
	 * Generate and return an UpdateOperation for the given object passed 
	 *
	 * @param timestamp Time stamp associated with the UpdateOperation
	 * @param object The object from which to generate the UpdateOperation
	 * @return The resulting UpdateOperation
	 */
	public UpdateOperation generateUpdate(long timestamp, T object) {
		return generateUpdateOperation(this.getCrdt().getDocument(), getMapper().valueToTree(object), timestamp);
	}

	/**
	 * Generate and return a DeleteOperation for the given object passed 
	 *
	 * @param timestamp Time stamp associated with the DeleteOperation
	 * @return The resulting DeleteOperation
	 */
	public DeleteOperation generateDelete(long timestamp) {
		return generateDeleteOperation(timestamp);
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		
		hash = 53 * hash + this.getObjectClass().hashCode();

		return hash;
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.crdt.CRDTManager#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object cmp) {
		if (this == cmp) {
			return true;
		}
		
		boolean parent = super.equals(cmp);
		
		if (!parent) {
			return parent;
		}
		
		if (!(cmp instanceof GenericCRDTManager<?>)) {
			return false;
		}
		
		@SuppressWarnings("unchecked")
		GenericCRDTManager<T> genericCmp = (GenericCRDTManager<T>) cmp;
		return this.getObjectClass().equals(genericCmp.getObjectClass());
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#getSegment()
	 */
	@Override
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();

		sb.append(super.getSegment() + ",");
		sb.append("\"objectClass\":\"" + this.getObjectClass().getName() + "\",");
		sb.append("\"object\":" + (null == this.object ? "null" : this.object.toString()));
		
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		this.setObject(null);
	}
}
