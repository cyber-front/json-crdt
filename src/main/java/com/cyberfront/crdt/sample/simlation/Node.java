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
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.OperationManager;
import com.cyberfront.crdt.operations.OperationManager.StatusType;
import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.sample.data.Factory;
import com.cyberfront.crdt.support.Support;
import com.fasterxml.jackson.databind.ObjectMapper;

// TODO: Auto-generated Javadoc
/**
 * The Node class models a node in a distributed data model.  Each Node has  a collection of objects it manages.
 * At the conclusion of the simulation, each node should, ideally, have the same state and all of the objects in
 * one node are equivalent to the corresponding object in each of the other nodes.
 */
public class Node extends AbstractNode<AbstractDataType> {
	
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
		this(Support.getNoun().toUpperCase(), Support.getRandom().nextInt(8)+4);
	}
	
	/**
	 * Instantiates a new node.
	 *
	 * @param nodeName New name of the node
	 * @param userCount Number of users the node has associatd with it
	 */
	public Node(String nodeName, int userCount) {
		this(nodeName, generateUsernames(userCount));
	}
	
	/**
	 * Instantiates a new node.
	 *
	 * @param nodeName New name of the node
	 * @param usernames Collection of usernames associated with the Node
	 */
	public Node(String nodeName, Collection<String> usernames) {
		super(nodeName, usernames);
	}
	
	/**
	 * Generate a collection of usernames and return them to the calling routing
	 *
	 * @param count Number of usernames to generate
	 * @return The collection of usernames generated
	 */
	private static Collection<String> generateUsernames(int count) {
		Collection<String> rv = new ArrayList<>();
		
		for (int i=0; i<count; ++i) {
			rv.add(Support.getUsername());
		}
		
		return rv;
	}

	/**
	 * Retrieve the ith user
	 *
	 * @param i Index of the user to retrieve
	 * @return The user located at the ith position
	 */
	private String getUser(int i) {
		return this.getUserNames().get(i);
	}
	
	/**
	 * Randomly pick a user from the collection of them
	 *
	 * @return The randomly chosen user
	 */
	public String pickUser() {
		return this.getUser(Support.getRandom().nextInt(this.getUserNames().size()));
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseNode#createCRDT()
	 */
	@Override
	protected SimCRDTManager<? extends AbstractDataType> createCRDT(String id) {
		return Factory.genCRDT(this, id);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseNode#createDatastore()
	 */
	@Override
	protected Map<String, SimCRDTManager<? extends AbstractDataType>> createDatastore() {
		return new TreeMap<>();
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
	 * Given a collection of operations, build the corresponding collection of messages to deliver the operations to each of the nodes 
	 *
	 * @param op Collection of operations to bundle and deliver
	 * @return The collection of messages each for delivery to a single Node destination
	 */
	private Collection<Message<? extends AbstractDataType>> buildMessages(SimOperationManager<? extends AbstractDataType> op) {
		Collection<Message<? extends AbstractDataType>> rv = new ArrayList<>();
		
		if (null != op) {
			for (Map.Entry<String, Node> entry : Executive.getExecutive().getNodes().entrySet()) {
				rv.add(new Message<>(entry.getKey(), op));
			}
		}
		
		return rv;
	}
	
	/**
	 * Builds the known messages derived from the operations presented in the collection
	 *
	 * @param <T> The type of object the operations are intended to modify
	 * @param ops The collection of operations to ensure are applied to the each of the Nodes in the distributed model
	 * @return The collection of messages to deliver the operations to each of the other nodes in the distributed architecture
	 */
	private <T extends AbstractDataType> Collection<Message<? extends AbstractDataType>> buildKnownMessages(Collection<SimOperationManager<T>> ops) {
		Collection<Message<? extends AbstractDataType>> rv = new ArrayList<>();
		
		if (null != ops) {
			for (SimOperationManager<T> op : ops) {
				rv.addAll(buildKnownMessages(op));
			}
		}

		return rv;
	}
	
	/**
	 * Builds the known messages for a single operation
	 *
	 * @param <T> The type of object the operation will affect
	 * @param op The operation for which to generate a collection of messages for all of the nodes
	 * @return The collection of messages resulting from the distribution of the single operation given 
	 */
	private <T extends AbstractDataType> Collection<Message<? extends AbstractDataType>> buildKnownMessages(SimOperationManager<T> op) {
		Collection<Message<? extends AbstractDataType>> rv = new ArrayList<>();
		
		if (null != op) {
			for (Map.Entry<String, Node> entry : Executive.getExecutive().getNodes().entrySet()) {
				if (!entry.getKey().equals(this.getNodeName())) {
					rv.add(new Message<>(entry.getKey(), op));
				}
			}
		}
		
		return rv;
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
		@SuppressWarnings("unchecked")
		SimCRDTManager<T> crdt = new SimCRDTManager<>(object.getId(), this.pickUser(), this.getNodeName(), (Class<T>) object.getClass());
		this.addCRDT(crdt);
		SimOperationManager<T> mgr = crdt.generateCreate(StatusType.APPROVED, Executive.getExecutive().getTimestamp(), object);

		return this.buildMessages(mgr);
	}

	/**
	 * Generate read operation.
	 *
	 * @return the collection
	 */
	public Collection<Message<? extends AbstractDataType>> generateReadOperation() {
		SimCRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		SimOperationManager<? extends AbstractDataType> mgr = crdt.generateRead(StatusType.APPROVED, Executive.getExecutive().getTimestamp());
		
		return this.buildMessages(mgr);
	}

	/**
	 * Generate update operation.
	 *
	 * @param pChange the change
	 * @return the collection
	 */
	public Collection<Message<? extends AbstractDataType>> generateUpdateOperation(Double pChange) {
		SimCRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		OperationManager.StatusType status = crdt.getNodename().equals(this.getNodeName()) ? OperationManager.StatusType.APPROVED : OperationManager.StatusType.PENDING;
		SimOperationManager<? extends AbstractDataType> mgr = crdt.generateUpdate(status, Executive.getExecutive().getTimestamp(), pChange);

		return this.buildMessages(mgr);
	}

	/**
	 * Generate delete operation.
	 *
	 * @return the collection
	 */
	public Collection<Message<? extends AbstractDataType>> generateDeleteOperation() {
		SimCRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		OperationManager.StatusType status = crdt.getNodename().equals(this.getNodeName()) ? OperationManager.StatusType.APPROVED : OperationManager.StatusType.PENDING;
		SimOperationManager<? extends AbstractDataType> mgr = crdt.generateDelete(status, Executive.getExecutive().getTimestamp());
		
		return this.buildMessages(mgr);
	}

	/**
	 * Forward the given message to the intended recipient CRDT this Node manages.
	 *
	 * @param <T> Generic type of the object the recipient CRDT manages which is tied to the generic type of the message.
	 * @param msg Message to delver to the intended recipient CRDT 
	 * @param pReject Probability that the owning CRDT will reject an update or delete operation
	 * @return A collection of messages which result in delivery of the message.  This will be an empty list if the 
	 * recipient CRDT is not the owner of the object being managed.
	 */
	public <T extends AbstractDataType> Collection<Message <? extends AbstractDataType>> deliver(Message<T> msg, Double pReject) {
		return this.deliver(msg.getManager(), pReject);
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
	protected <T extends AbstractDataType> Collection<Message <? extends AbstractDataType>> deliver(SimOperationManager<T> mgr, Double pReject) {
		String id = mgr.getObjectId();
		
		String ownerUser = Executive.getExecutive().getOwnerUser(id); 
		String ownerNode = Executive.getExecutive().getOwnerNode(id); 

		String user = (null == ownerUser ? mgr.getUsername() : ownerUser);
		String node = (null == ownerNode ? mgr.getNodename() : ownerNode);

		SimCRDTManager<? extends AbstractDataType> crdt = this.getDatastore().get(id);
		
		@SuppressWarnings("unchecked")
		SimCRDTManager<T> castCrdt = null == crdt
				? new SimCRDTManager<>(id, user, node, mgr.getObjectClass())
				: (SimCRDTManager<T>) crdt;

		if (null == crdt) {
			this.addCRDT(castCrdt);
		}
		
		Collection<SimOperationManager<T>> operations = castCrdt.deliver(mgr, pReject);

		
		Collection<Message <? extends AbstractDataType>> rv = this.buildKnownMessages(operations);
		
		return rv;
	}
}
