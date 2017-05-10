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

import com.cyberfront.crdt.operations.AbstractOperation.StatusType;
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
//	@SuppressWarnings("unused")
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

//	private Collection<Message<? extends AbstractDataType>> buildMessages(Collection<OperationManager<? extends AbstractDataType>> ops) {
//		Collection<Message<? extends AbstractDataType>> rv = new ArrayList<>();
//		
//		if (null != ops) {
//			for (OperationManager<? extends AbstractDataType> op : ops) {
//				rv.addAll(buildMessages(op));
//			}
//		}
//		
//		return rv;
//	}
	
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

		// TODO Remove before flight
//		logger.info("\n*** Node.buildMessages(OperationManager<? extends AbstractDataType> op)");
//		logger.info("    op: " + (null == op ? "null" : op.toString()));
//		logger.info("    rv: " + WordFactory.convert(rv));
		
		return rv;
	}
	
	private <T extends AbstractDataType> Collection<Message<? extends AbstractDataType>> buildKnownMessages(Collection<OperationManager<T>> ops) {
		Collection<Message<? extends AbstractDataType>> rv = new ArrayList<>();
		
		if (null != ops) {
			for (OperationManager<T> op : ops) {
				rv.addAll(buildKnownMessages(op));
			}
		}
		
		return rv;
	}
	
	private <T extends AbstractDataType> Collection<Message<? extends AbstractDataType>> buildKnownMessages(OperationManager<T> op) {
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
		Collection<Message<? extends AbstractDataType>> rv = null;
		CRDTManager<? extends AbstractDataType> crdt = Factory.genCRDT(this);
		this.addCRDT(crdt);

		OperationManager<? extends AbstractDataType> mgr = crdt.processCreate(Executive.getExecutive().getTimeStamp());

		if (null != mgr) {
			mgr.setStatus(StatusType.APPROVED);
			this.deliver(mgr, 0.0);
			rv = this.buildMessages(mgr);
		} else {
			rv = new ArrayList<>();
		}

		return rv;
	}

	/**
	 * Generate read operation.
	 *
	 * @return the collection
	 */
	public Collection<Message<? extends AbstractDataType>> generateReadOperation() {
		Collection<Message<? extends AbstractDataType>> rv = null;
		CRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		OperationManager<? extends AbstractDataType> mgr = crdt.processRead(Executive.getExecutive().getTimeStamp());
		
		if (null != mgr) {
			mgr.setStatus(StatusType.APPROVED);
			this.deliver(mgr, 0.0);
			rv =  this.buildMessages(mgr);
		} else {
			rv = new ArrayList<>(); 
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
		Collection<Message<? extends AbstractDataType>> rv = null;
		CRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		OperationManager<? extends AbstractDataType> mgr = crdt.processUpdate(Executive.getExecutive().getTimeStamp(), pChange);

		if (null != mgr) {
			StatusType type = crdt.getNodename().equals(this.getNodeName()) ? StatusType.APPROVED : StatusType.PENDING;
			mgr.setStatus(type);
			this.deliver(mgr, 0.0);
			rv =  this.buildMessages(mgr);
		} else {
			rv = new ArrayList<>(); 
		}
		
		return rv;
	}

	/**
	 * Generate delete operation.
	 *
	 * @return the collection
	 */
	public Collection<Message<? extends AbstractDataType>> generateDeleteOperation() {
		Collection<Message<? extends AbstractDataType>> rv = null;
		CRDTManager<? extends AbstractDataType> crdt = this.pickCRDT();
		OperationManager<? extends AbstractDataType> mgr = crdt.processDelete(Executive.getExecutive().getTimeStamp());
		
		if (null != mgr) {
			StatusType type = crdt.getNodename().equals(this.getNodeName()) ? StatusType.APPROVED : StatusType.PENDING;
			mgr.setStatus(type);
			this.deliver(mgr, 0.0);
			rv =  this.buildMessages(mgr);
		} else {
			rv = new ArrayList<>(); 
		}
		
		return rv;
	}

//	private String convert(Map<Long, Message<? extends AbstractDataType>> map) {
//		StringBuilder sb = new StringBuilder();
//		String separator = "[";
//		
//		if (map.isEmpty()) {
//			sb.append(separator);
//		} else {
//			for (Map.Entry<Long, Message<? extends AbstractDataType>> entry : map.entrySet()) {
//			    sb.append(separator + entry.getKey() + ":" + entry.getValue());
//			    separator = ",";
//			}
//		}
//		
//		sb.append(']');
//		return sb.toString();
//	}
	
//	@Override
//	protected String getSegment() {
//		StringBuilder sb = new StringBuilder();
//
//		sb.append(super.getSegment() + ",");
//		sb.append("\"approvedMessages\":" + convert(this.getApprovedMessages()) + ",");
//		sb.append("\"pendingMessages\":" + convert(this.getPendingMessages()) + ",");
//		sb.append("\"rejectedMessages\":" + convert(this.getRejectedMessages()));
//		
//		return sb.toString();
//	}

	/**
	 * Deliver.
	 *
	 * @param <T> the generic type
	 * @param msg the msg
	 */
//	public <T extends AbstractDataType> void deliver(Message<T> msg) {
//		this.deliver(msg.getManager());
//	}

//	private Map<Long, Message<? extends AbstractDataType>> getApprovedMessages() {
//		if (null == this.approvedMessages) {
//			this.approvedMessages = new TreeMap<>();
//		}
//		return this.approvedMessages;
//	}
//	
//	private Map<Long, Message<? extends AbstractDataType>> getRejectedMessages() {
//		if (null == this.rejectedMessages) {
//			this.rejectedMessages = new TreeMap<>();
//		}
//		return this.rejectedMessages;
//	}
//	
//	private Map<Long, Message<? extends AbstractDataType>> getPendingMessages() {
//		if (null == this.pendingMessages) {
//			this.pendingMessages = new TreeMap<>();
//		}
//		return this.pendingMessages;
//	}
	
	/**
	 * Preserve the message in the appropriate list for forensic purposes
	 * @param msg
	 */
//	private void post(Message<? extends AbstractDataType> msg) {
//		switch(msg.getManager().getStatus()){
//		case APPROVED:
//			this.getApprovedMessages().put(Executive.getExecutive().getTimeStamp(), msg);
//			break;
//		case PENDING:
//			this.getPendingMessages().put(Executive.getExecutive().getTimeStamp(), msg);
//			break;
//		case REJECTED:
//			this.getRejectedMessages().put(Executive.getExecutive().getTimeStamp(), msg);
//			break;
//		default:
//			break;
//		}
//	}

	/**
	 * Deliver.
	 *
	 * @param <T> the generic type
	 * @param msg the msg
	 */
	public <T extends AbstractDataType> Collection<Message <? extends AbstractDataType>> deliver(Message<T> msg, Double pReject) {
//		post(msg);
		return this.deliver(msg.getManager(), pReject);
	}
	
	/**
	 * Deliver.
	 *
	 * @param <T> the generic type
	 * @param mgr the mgr
	 */
	protected <T extends AbstractDataType> Collection<Message <? extends AbstractDataType>> deliver(OperationManager<T> mgr, Double pReject) {
		
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
		
		Collection<OperationManager<T>> operations = castCrdt.deliver(mgr, pReject, this);
		for (OperationManager<T> op : operations) {
			castCrdt.deliver(op);
		}
		
		Collection<Message <? extends AbstractDataType>> rv = this.buildKnownMessages(operations);
		
		// TODO Remove before flight
//		logger.info("\n*** Node.deliver(OperationManager<T> mgr, Double pReject)");
//		logger.info("       this: " + this.toString());
//		logger.info("        mgr: " + (null == mgr ? "null" : mgr.toString()));
//		logger.info("    pReject: " + pReject);
//		logger.info("         rv: " + WordFactory.convert(rv));

		return rv;
	}
}
