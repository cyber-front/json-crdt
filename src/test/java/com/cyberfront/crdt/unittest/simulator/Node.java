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
		this(WordFactory.getNoun().toUpperCase(), WordFactory.getRandom().nextInt(8)+4, 0);
	}
	
	/**
	 * Instantiates a new node.
	 *
	 * @param nodeName the node name
	 * @param userCount the user count
	 * @param objectCount the object count
	 */
	public Node(String nodeName, int userCount, int objectCount) {
		this(nodeName, generateUsernames(userCount), objectCount);
	}
	
	/**
	 * Instantiates a new node.
	 *
	 * @param nodeName the node name
	 * @param usernames the usernames
	 * @param objectCount the object count
	 */
	public Node(String nodeName, Collection<String> usernames, int objectCount) {
		super(nodeName, usernames, objectCount);
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
	protected CRDTManager<? extends AbstractDataType> createCRDT() {
		return Factory.genCRDT(this);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseNode#createDatastore()
	 */
	@Override
	protected Map<String, CRDTManager<? extends AbstractDataType>> createDatastore() {
		return new TreeMap<>();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseNode#addCRDT(com.cyberfront.cmrdt.manager.CRDTManager)
	 */
	@Override
	protected void addCRDT(CRDTManager<? extends AbstractDataType> crdt) {
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
	private Collection<Message<? extends AbstractDataType>> buildMessages(OperationManager<? extends AbstractDataType> op) {
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
	 * Clear.
	 */
	public void clear() {
		for (Map.Entry<String, CRDTManager<? extends AbstractDataType>> entry : this.getDatastore().entrySet()) {
			entry.getValue().clear();
		}

		this.getDatastore().clear();
	}

	/**
	 * Generate create operation.
	 *
	 * @return the collection
	 * @throws ReflectiveOperationException the reflective operation exception
	 */
	public Collection<Message<? extends AbstractDataType>> generateCreateOperation() throws ReflectiveOperationException {
		CRDTManager<? extends AbstractDataType> crdt = Factory.genCRDT(this);
		this.addCRDT(crdt);

		OperationManager<? extends AbstractDataType> mgr = crdt.processCreate(Executive.getExecutive().getTimeStamp());

		if (null != mgr) {
			this.deliver(mgr);
			Collection<Message<? extends AbstractDataType>> rv = this.buildMessages(mgr);

			return rv;
		}

		return new ArrayList<>();
	}

	/**
	 * Generate read operation.
	 *
	 * @return the collection
	 */
	public Collection<Message<? extends AbstractDataType>> generateReadOperation() {
		CRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		OperationManager<? extends AbstractDataType> mgr = crdt.processRead(Executive.getExecutive().getTimeStamp());
		
		if (null != mgr) {
			this.deliver(mgr);
			return this.buildMessages(mgr);
		}
		return new ArrayList<>();
	}

	/**
	 * Generate update operation.
	 *
	 * @param pChange the change
	 * @return the collection
	 */
	public Collection<Message<? extends AbstractDataType>> generateUpdateOperation(Double pChange) {
		CRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		OperationManager<? extends AbstractDataType> mgr = crdt.processUpdate(Executive.getExecutive().getTimeStamp(), pChange);

		if (null != mgr) {
			this.deliver(mgr);
			return this.buildMessages(mgr);
		}
		return new ArrayList<>();
	}

	/**
	 * Generate delete operation.
	 *
	 * @return the collection
	 */
	public Collection<Message<? extends AbstractDataType>> generateDeleteOperation() {
		CRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		OperationManager<? extends AbstractDataType> mgr = crdt.processDelete(Executive.getExecutive().getTimeStamp());
		
		if (null != mgr) {
			this.deliver(mgr);
			return this.buildMessages(mgr);
		}
		return new ArrayList<>();
	}

	/**
	 * Deliver.
	 *
	 * @param <T> the generic type
	 * @param msg the msg
	 */
	public <T extends AbstractDataType> void deliver(Message<T> msg) {
		this.deliver(msg.getManager());
	}

	/**
	 * Deliver.
	 *
	 * @param <T> the generic type
	 * @param mgr the mgr
	 */
	protected <T extends AbstractDataType> void deliver(OperationManager<T> mgr) {
		String id = mgr.getObjectId();
		
		String ownerUser = getExecutive().getOwnerUser(id); 
		String user = (null == ownerUser ? mgr.getUsername() : ownerUser);

		String ownerNode = getExecutive().getOwnerNode(id); 
		String node = (null == ownerNode ? mgr.getNodename() : ownerNode);
		
		CRDTManager<? extends AbstractDataType> crdt = this.getDatastore(id);
		
		@SuppressWarnings("unchecked")
		CRDTManager<T> castCrdt = null == crdt
				? new CRDTManager<>(id, user, node, mgr.getObjectClass())
				: (CRDTManager<T>) crdt;

		if (null == crdt) {
			this.addCRDT(castCrdt);
		}
		
		castCrdt.deliver(mgr);
	}
}
