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
package com.cyberfront.crdt.sample.manager;

import com.cyberfront.crdt.CRDTManager;
import com.cyberfront.crdt.GenericCRDTManager;
import com.cyberfront.crdt.operation.OperationManager;
import com.cyberfront.crdt.operation.OperationManager.StatusType;
import com.cyberfront.crdt.operation.Operation;

/**
 * The Class GenericManager provides a simple mechanism for managing a POJO as a CRDT.  It supports the basic CRUD
 * operations on POJO's.  Initial instantiation of the manager must include the initial value of the POJO.
 *
 * @param <T> The type of the POJO the CRDT will manage
 */
public class GenericManager <T> extends GenericCRDTManager<T> {
	
	/**
	 * Instantiates a new generic manager given the original value of the POJO to manage and the timestamp of
	 * its creation.
	 *
	 * @param object The POJO to manage
	 * @param timestamp The timestamp of the create operation to perform on the POJO
	 */
	@SuppressWarnings("unchecked")
	public GenericManager(T object, long timestamp) {
		super((Class<T>) object.getClass());
		Operation create = CRDTManager.generateCreate(timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, create);
		this.push(mgr);
		this.update(object, timestamp);
	}
	
	/**
	 * Read the value of the POJO at the given timestamp
	 *
	 * @param timestamp The timestamp for which the Read operation is to be performed
	 * @return The value of the managed POJO at the given timestamp
	 */
	public T read(long timestamp) {
		Operation read = CRDTManager.generateRead(timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, read);
		this.push(mgr);
		return this.getObject(timestamp);
	}

	/**
	 * Update the value of the POJO to the given object value and at the given timestamp 
	 *
	 * @param object The updated object value
	 * @param timestamp The timestamp the update is to be effective
	 */
	public void update(T object, long timestamp) {
		Operation update = this.generateUpdate(timestamp, object);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, update);
		this.push(mgr);
	}
	
	/**
	 * Delete the POJO at the given timestamp value
	 *
	 * @param timestamp The timestamp at which the POJO is deleted
	 */
	public void delete(long timestamp) {
		Operation delete = CRDTManager.generateDelete(timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, delete);
		this.push(mgr);
	}
}
