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
package com.cyberfront.crdt.sample.simlation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.OperationManager.StatusType;
import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.fasterxml.jackson.databind.ObjectMapper;

// TODO: Auto-generated Javadoc
/**
 * The Node class models a node in a distributed data model.  Each Node has  a collection of objects it manages.
 * At the conclusion of the simulation, each node should, ideally, have the same state and all of the objects in
 * one node are equivalent to the corresponding object in each of the other nodes.
 */
public class Node extends AbstractNode {
	
	/** Constant logger used to produce output during execution of the simulation. */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(Node.class);
	
	/** Constant object mapper used to convert between JSON formatted objects and their equivalent POJO */
	@SuppressWarnings("unused")
	private static final ObjectMapper mapper = new ObjectMapper(); 
	
	/**
	 * Instantiates a new node.
	 */
	public Node() {
		this(UUID.randomUUID());
	}
	
	/**
	 * Instantiates a new node.
	 *
	 * @param id New name of the node
	 * @param usernames Collection of usernames associated with the Node
	 */
	public Node(UUID id) {
		super(id);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseNode#addCRDT(com.cyberfront.cmrdt.manager.CRDTManager)
	 */
	@Override
	protected void addCRDT(SimCRDTManager<? extends AbstractDataType> crdt) {
		super.addCRDT(crdt);
		Executive.getExecutive().registerCrdt(crdt);
	}

	
	/**
	 * Upon receipt of a new object to manage, this will allocate the CRDT for the object and return the create operation managers
	 * so the operation can be replicated at all other nodes.
	 *
	 * @param <T> The type associated with the object being managed
	 * @param object The object being managed
	 * @return A collection of create operations which are intended for use at other nodes for replicating the
	 * create operation
	 */
	public <T extends AbstractDataType> Collection<Message<? extends AbstractDataType>> generateCreateOperation(T object) {
		Collection<Message<? extends AbstractDataType>> rv;

		@SuppressWarnings("unchecked")
		SimCRDTManager<T> crdt = new SimCRDTManager<>(object.getId(), this.getId(), this.getId(), (Class<T>) object.getClass());
		Executive.getExecutive().registerCrdt(crdt);
		this.addCRDT(crdt);
		StatusType status = crdt.isLocallyManaged() ? StatusType.APPROVED : StatusType.PENDING;
		rv = crdt.generateCreate(status, Executive.getExecutive().getTimestamp(), object);
		
		return rv;
	}

	/**
	 * Generate read operation.
	 *
	 * @return the collection
	 */
	public Collection<Message<? extends AbstractDataType>> generateReadOperation() {
		Collection<Message<? extends AbstractDataType>> rv;
		SimCRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		
		if (!crdt.isCreated() || crdt.isDeleted()) {
			rv = new ArrayList<>();
		} else {
			rv = crdt.generateRead(StatusType.APPROVED, Executive.getExecutive().getTimestamp());
		}
		
		return rv;
	}

	/**
	 * Generate update operation.
	 *
	 * @param pChange the change
	 * @return the collection
	 */
	public Collection<Message<? extends AbstractDataType>> generateUpdateOperation(Double pChange) {
		Collection<Message<? extends AbstractDataType>> rv;
		SimCRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		
		if (!crdt.isCreated() || crdt.isDeleted()) {
			rv = new ArrayList<>();
		} else {
			StatusType status = crdt.isLocallyManaged() ? StatusType.APPROVED : StatusType.PENDING;
			rv =  crdt.generateUpdate(status, Executive.getExecutive().getTimestamp(), pChange);
		}

		return rv;
	}

	/**
	 * Generate delete operation.
	 *
	 * @return the collection
	 */
	public Collection<Message<? extends AbstractDataType>> generateDeleteOperation() {
		Collection<Message<? extends AbstractDataType>> rv;
		SimCRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		
		if (!crdt.isCreated()) {
			rv = new ArrayList<>();
		} else {
			StatusType status = crdt.isLocallyManaged() ? StatusType.APPROVED : StatusType.PENDING;
			rv = crdt.generateDelete(status, Executive.getExecutive().getTimestamp());
		}

		return rv;
	}
	
	/**
	 * Forward the given message to the intended recipient CRDT this Node manages.
	 *
	 * @param <T> Generic type of the object the recipient CRDT manages which is tied to the generic type of the message.
	 * @param mgr Operation manager used to provide additional metadata for the operation
	 * @param pReject Probability that the owning CRDT will reject an update or delete operation
	 * @return A collection of messages which result in delivery of the message.  This will be an empty list if the 
	 * recipient CRDT is not the owner of the object being managed.
	 */
	protected <T extends AbstractDataType> Collection<Message <? extends AbstractDataType>> push(Message<T> msg, Double pReject) {
		SimOperationManager<T> mgr = msg.getManager();
		UUID id = mgr.getObjectId();
		SimCRDTManager<? extends AbstractDataType> crdt = this.getDatastore().get(id);

		@SuppressWarnings("unchecked")
		SimCRDTManager<T> castCrdt = null == crdt
				? new SimCRDTManager<>(id, this.getId(), Executive.getExecutive().getOwnerNode(id), mgr.getObjectClass())
				: (SimCRDTManager<T>) crdt;
				
		if (null == crdt) {
			this.addCRDT(castCrdt);
		}
		
		Collection<Message <? extends AbstractDataType>> rv = castCrdt.push(msg, pReject);
		return rv;
	}
	
	public void checkMessageConsistency() {
		for (Map.Entry<UUID, SimCRDTManager<? extends AbstractDataType>> entry : this.getDatastore().entrySet()) {
			entry.getValue().checkMessageConsistency();
		}
	}
	
	public void checkMessageCount() {
		for (Map.Entry<UUID, SimCRDTManager<? extends AbstractDataType>> entry : this.getDatastore().entrySet()) {
			entry.getValue().checkMessageCount();
		}
	}
	
	public void checkOperationValidity() {
		for (Map.Entry<UUID, SimCRDTManager<? extends AbstractDataType>> entry : this.getDatastore().entrySet()) {
			entry.getValue().checkOperationValidity();
		}
	}
}
