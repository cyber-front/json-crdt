package com.cyberfront.crdt.sample.manager;

import com.cyberfront.crdt.CRDTManager;
import com.cyberfront.crdt.operations.CreateOperation;
import com.cyberfront.crdt.operations.DeleteOperation;
import com.cyberfront.crdt.operations.OperationManager;
import com.cyberfront.crdt.operations.OperationManager.StatusType;
import com.cyberfront.crdt.operations.ReadOperation;
import com.cyberfront.crdt.operations.UpdateOperation;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonManager extends CRDTManager {
	public JsonManager() {}
	
	public JsonManager(JsonNode document, long timestamp) {
		CreateOperation create = CRDTManager.generateCreateOperation(document, timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, create);
		this.push(mgr);
	}

	public JsonNode read(long timestamp) {
		ReadOperation read = CRDTManager.generateReadOperation(timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, read);
		this.push(mgr);
		return this.getCrdt().getDocument(timestamp);
	}
	
	public void update(JsonNode document, long timestamp) {
		UpdateOperation update = CRDTManager.generateUpdateOperation(this.getCrdt().getDocument(timestamp), document, timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, update);
		this.push(mgr);
	}
	
	public void delete(long timestamp) {
		DeleteOperation delete = CRDTManager.generateDeleteOperation(timestamp);
		OperationManager mgr = new OperationManager(StatusType.APPROVED, delete);
		this.push(mgr);
	}
}
