package com.cyberfront.crdt.sample.manager;

import com.cyberfront.crdt.GenericCRDTManager;
import com.cyberfront.crdt.operations.CreateOperation;
import com.cyberfront.crdt.operations.DeleteOperation;
import com.cyberfront.crdt.operations.OperationManager;
import com.cyberfront.crdt.operations.OperationManager.StatusType;
import com.cyberfront.crdt.operations.ReadOperation;
import com.cyberfront.crdt.operations.UpdateOperation;

public class GenericManager <T> extends GenericCRDTManager<T> {
	@SuppressWarnings("unchecked")
	public GenericManager(T object, long timestamp) {
		super((Class<T>) object.getClass());
		CreateOperation create = this.generateCreate(timestamp, object);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, create);
		this.push(mgr);
	}
	
	public T read(long timestamp) {
		ReadOperation read = this.generateRead(timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, read);
		this.push(mgr);
		return this.getObject(timestamp);
	}

	public void update(T object, long timestamp) {
		UpdateOperation update = this.generateUpdate(timestamp, object);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, update);
		this.push(mgr);
	}
	
	public void delete(long timestamp) {
		DeleteOperation delete = this.generateDelete(timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, delete);
		this.push(mgr);
	}
}
