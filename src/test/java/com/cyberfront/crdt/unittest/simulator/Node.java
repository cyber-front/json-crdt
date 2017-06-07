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
package com.cyberfront.crdt.unittest.simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.OperationManager;
import com.cyberfront.crdt.operations.OperationManager.StatusType;
import com.cyberfront.crdt.unittest.data.AbstractDataType;
import com.cyberfront.crdt.unittest.data.Factory;
import com.cyberfront.crdt.unittest.support.WordFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

// TODO: Auto-generated Javadoc
/**
 * The Class Node.
 */
public class Node extends AbstractNode<AbstractDataType> {
	
	/** The Constant logger. */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(Node.class);
	
	/** The Constant mapper. */
	@SuppressWarnings("unused")
	private static final ObjectMapper mapper = new ObjectMapper(); 
	
	/**
	 * Instantiates a new node.
	 */
	public Node() {
		this(WordFactory.getNoun().toUpperCase(), WordFactory.getRandom().nextInt(8)+4);
	}
	
	/**
	 * Instantiates a new node.
	 *
	 * @param nodeName the node name
	 * @param userCount the user count
	 * @param objectCount the object count
	 */
	public Node(String nodeName, int userCount) {
		this(nodeName, generateUsernames(userCount));
	}
	
	/**
	 * Instantiates a new node.
	 *
	 * @param nodeName the node name
	 * @param usernames the usernames
	 * @param objectCount the object count
	 */
	public Node(String nodeName, Collection<String> usernames) {
		super(nodeName, usernames);
	}
	
	/**
	 * Generate usernames.
	 *
	 * @param count the count
	 * @return the collection
	 */
	private static Collection<String> generateUsernames(int count) {
		Collection<String> rv = new ArrayList<>();
		
		for (int i=0; i<count; ++i) {
			rv.add(WordFactory.getUsername());
		}
		
		return rv;
	}

	/**
	 * Gets the user.
	 *
	 * @param i the i
	 * @return the user
	 */
	private String getUser(int i) {
		return this.getUserNames().get(i);
	}
	
	/**
	 * Pick user.
	 *
	 * @return the string
	 */
	public String pickUser() {
		return this.getUser(WordFactory.getRandom().nextInt(this.getUserNames().size()));
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
		getExecutive().addNodename(crdt.getObjectId(), crdt.getNodename());
		getExecutive().addUsername(crdt.getObjectId(), crdt.getUsername());
	}
	
	/**
	 * Gets the executive.
	 *
	 * @return the executive
	 */
	public static Executive getExecutive() {
		return Executive.getExecutive();
	}
	
	/**
	 * Builds the messages.
	 *
	 * @param op the op
	 * @return the collection< message<? extends abstract data type>>
	 */
	private Collection<Message<? extends AbstractDataType>> buildMessages(SimOperationManager<? extends AbstractDataType> op) {
		Collection<Message<? extends AbstractDataType>> rv = new ArrayList<>();
		
		if (null != op) {
			for (Map.Entry<String, Node> entry : getExecutive().getNodes().entrySet()) {
				rv.add(new Message<>(entry.getKey(), op));
			}
		}
		
		return rv;
	}
	
	private <T extends AbstractDataType> Collection<Message<? extends AbstractDataType>> buildKnownMessages(Collection<SimOperationManager<T>> ops) {
		Collection<Message<? extends AbstractDataType>> rv = new ArrayList<>();
		
		if (null != ops) {
			for (SimOperationManager<T> op : ops) {
				rv.addAll(buildKnownMessages(op));
			}
		}
		
		return rv;
	}
	
	private <T extends AbstractDataType> Collection<Message<? extends AbstractDataType>> buildKnownMessages(SimOperationManager<T> op) {
		Collection<Message<? extends AbstractDataType>> rv = new ArrayList<>();
		
		if (null != op) {
			for (Map.Entry<String, Node> entry : getExecutive().getNodes().entrySet()) {
				if (!entry.getKey().equals(this.getNodeName())) {
					rv.add(new Message<>(entry.getKey(), op));
				}
			}
		}
		
		return rv;
	}

	/**
	 * Generate create operation.
	 *
	 * @return the collection
	 * @throws ReflectiveOperationException the reflective operation exception
	 */
	public <T extends AbstractDataType> Collection<Message<? extends AbstractDataType>> generateCreateOperation(T object)
			throws ReflectiveOperationException {
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
	 * Deliver.
	 *
	 * @param <T> the generic type
	 * @param msg the msg
	 */
	public <T extends AbstractDataType> Collection<Message <? extends AbstractDataType>> deliver(Message<T> msg, Double pReject) {
		return this.deliver(msg.getManager(), pReject);
	}
	
	/**
	 * Deliver.
	 *
	 * @param <T> the generic type
	 * @param mgr the mgr
	 */
	protected <T extends AbstractDataType> Collection<Message <? extends AbstractDataType>> deliver(SimOperationManager<T> mgr, Double pReject) {
		String id = mgr.getObjectId();
		
		String ownerUser = getExecutive().getOwnerUser(id); 
		String ownerNode = getExecutive().getOwnerNode(id); 

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
