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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.unittest.data.AbstractDataType;
import com.cyberfront.crdt.unittest.data.Factory;
import com.cyberfront.crdt.unittest.support.WordFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class Executive.
 */
public class Executive implements ITimeStamp {
	
	/** The logger. */
	@SuppressWarnings("unused")
	private Logger logger = LogManager.getLogger(AbstractOperation.class);

	/** The Constant DEFAULT_NODE_COUNT. */
	private static final int DEFAULT_NODE_COUNT = 16;
	
	/** The Constant DEFAULT_CREATE_COUNT. */
	private static final int DEFAULT_CREATE_COUNT = 256;
	
	/** The Constant DEFAULT_READ_COUNT. */
	private static final int DEFAULT_READ_COUNT = 1024;
	
	/** The Constant DEFAULT_UPDATE_COUNT. */
	private static final int DEFAULT_UPDATE_COUNT = 2048;
	
	/** The Constant DEFAULT_DELETE_COUNT. */
	private static final int DEFAULT_DELETE_COUNT = 32;
	
	/**
	 * The Enum EventType.
	 */
	public enum EventType {
		
		/** The create. */
		CREATE,
		
		/** The read. */
		READ,
		
		/** The update. */
		UPDATE,
		
		/** The delete. */
		DELETE,
		
		/** The deliver. */
		DELIVER
	}
	
	/** The instance. */
	private static Executive instance;

	
	/** The nodes. */
	private Map<String, Node> nodes;
	
	/** The router. */
	private MessageRouter router;
	
	/** The time stamp. */
	private long timestamp;
	
	/** The node count. */
	private long nodeCount;
	
	/** The create count. */
	private long createCount;
	
	/** The read count. */
	private long readCount;
	
	/** The update count. */
	private long updateCount;
	
	/** The delete count. */
	private long deleteCount;
	
	/** The reject probability. */
	private double rejectProbability;
	
	/** The update probability. */
	private double updateProbability;
	
	/** The crdt lookup. */
	private Map<String, String> crdtLookup;
	
	/** The user lookup. */
	private Map<String, String> userLookup;

	/**
	 * Instantiates a new executive.
	 */
	public Executive() {
		this.setNodes(new TreeMap<>());
		this.setRouter(new MessageRouter());
		this.setTimestamp(0L);

		this.setCreateCount(DEFAULT_CREATE_COUNT);
		this.setDeleteCount(DEFAULT_DELETE_COUNT);
		this.setNodeCount(DEFAULT_NODE_COUNT);
		this.setReadCount(DEFAULT_READ_COUNT);
		this.setUpdateCount(DEFAULT_UPDATE_COUNT);
	}

	/**
	 * Gets the nodes.
	 *
	 * @return the nodes
	 */
	public Map<String, Node> getNodes() {
		if (null == this.nodes) {
			this.setNodes(new TreeMap<>());
		}
		
		return nodes;
	}

	/**
	 * Sets the nodes.
	 *
	 * @param nodes the nodes
	 */
	private void setNodes(Map<String, Node> nodes) {
		this.nodes = nodes;
	}

	/**
	 * Adds the node.
	 *
	 * @param node the node
	 */
	public void addNode(Node node) {
		this.getNodes().put(node.getNodeName(), node);
	}
	
	/**
	 * Gets the node.
	 *
	 * @param nodeName the node name
	 * @return the node
	 */
	public Node getNode(String nodeName) {
		return this.getNodes().get(nodeName);
	}
	
	/**
	 * Gets the router.
	 *
	 * @return the router
	 */
	public MessageRouter getRouter() {
		if (null == this.router) {
			this.setRouter(new MessageRouter());
		}
		
		return router;
	}

	/**
	 * Sets the router.
	 *
	 * @param router the new router
	 */
	private void setRouter(MessageRouter router) {
		this.router = router;
	}
	
	/**
	 * Gets the executive.
	 *
	 * @return the executive
	 */
	public static Executive getExecutive() {
		if (null == instance) {
			Executive.instance = new Executive();
		}
		
		return Executive.instance;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.ITimeStamp#getTimeStamp()
	 */
	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.ITimeStamp#setTimeStamp(java.lang.long)
	 */
	@Override
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Increment time S tamp.
	 */
	public void incrementTimestamp() {
		this.setTimestamp(this.getTimestamp() + WordFactory.getRandom().nextInt(256));
	}

	/**
	 * Transmit.
	 *
	 * @param messages the messages
	 */
	public void transmit(Collection<Message<? extends AbstractDataType>> messages) {
		for (Message<? extends AbstractDataType> message : messages) {
			this.getRouter().add(message);
		}
	}

	/**
	 * Gets the node count.
	 *
	 * @return the node count
	 */
	public long getNodeCount() {
		return nodeCount;
	}

	/**
	 * Sets the node count.
	 *
	 * @param nodeCount the new node count
	 */
	public void setNodeCount(long nodeCount) {
		this.nodeCount = nodeCount;
	}

	/**
	 * Gets the creates the count.
	 *
	 * @return the creates the count
	 */
	public long getCreateCount() {
		return createCount;
	}

	/**
	 * Sets the creates the count.
	 *
	 * @param createCount the new creates the count
	 */
	public void setCreateCount(long createCount) {
		this.createCount = createCount;
	}

	/**
	 * Gets the read count.
	 *
	 * @return the read count
	 */
	public long getReadCount() {
		return readCount;
	}

	/**
	 * Sets the read count.
	 *
	 * @param readCount the new read count
	 */
	public void setReadCount(long readCount) {
		this.readCount = readCount;
	}

	/**
	 * Gets the update count.
	 *
	 * @return the update count
	 */
	public long getUpdateCount() {
		return updateCount;
	}

	/**
	 * Sets the update count.
	 *
	 * @param updateCount the new update count
	 */
	public void setUpdateCount(long updateCount) {
		this.updateCount = updateCount;
	}

	/**
	 * Gets the delete count.
	 *
	 * @return the delete count
	 */
	public long getDeleteCount() {
		return deleteCount;
	}

	/**
	 * Sets the delete count.
	 *
	 * @param deleteCount the new delete count
	 */
	public void setDeleteCount(long deleteCount) {
		this.deleteCount = deleteCount;
	}

	/**
	 * Gets the delivery count.
	 *
	 * @return the delivery count
	 */
	public int getDeliveryCount() {
		return this.getRouter().getMessageCount();
	}
	
	/**
	 * Event count.
	 *
	 * @return the long
	 */
	private long eventCount() {
		return this.getCreateCount() +
				this.getDeleteCount() +
				this.getDeliveryCount() + 
				this.getReadCount() +
				this.getUpdateCount();
	}
	
	/**
	 * Pick event.
	 *
	 * @return the event type
	 */
	private EventType pickEvent() {
		long pick = WordFactory.getRandom().nextLong() % this.eventCount();

		if (pick < this.getDeliveryCount()) {
			return EventType.DELIVER;
		} else {
			pick -= this.getDeliveryCount();
		}
		
		if (pick < this.getDeleteCount()) {
			return EventType.DELETE;
		} else {
			pick -= this.getDeleteCount();
		}
		
		if (pick < this.getReadCount()) {
			return EventType.READ;
		} else {
			pick -= this.getReadCount();
		}
		
		if (pick < this.getUpdateCount()) {
			return EventType.UPDATE;
		}

		return EventType.CREATE;
	}
	
	/**
	 * Pick node.
	 *
	 * @return the node
	 */
	public Node pickNode() {
		int pick = WordFactory.getRandom().nextInt(this.getNodes().size());
		
		for (Map.Entry<String, Node> entry : this.getNodes().entrySet()) {
			if (pick-- <= 0) {
				return entry.getValue();
			}
		}
		
		return null;
	}
	
	/**
	 * Do create.
	 *
	 * @param node the node
	 * @throws ReflectiveOperationException the reflective operation exception
	 */
	private <T extends AbstractDataType> Collection<Message<? extends AbstractDataType>> doCreate(Node node, T object) throws ReflectiveOperationException {
		Collection<Message<? extends AbstractDataType>> messages = node.generateCreateOperation(object);
		--this.createCount;
		return messages;
	}

	/**
	 * Do read.
	 *
	 * @param node the node
	 */
	private Collection<Message<? extends AbstractDataType>> doRead(Node node) {
		if (node.getDatastore().size() <= 0) {
			return new TreeSet<>();
		}
		
		Collection<Message<? extends AbstractDataType>> messages = node.generateReadOperation();
		
		if (!messages.isEmpty()) {
			--this.readCount;
		}
		
		return messages;
	}
	
	/**
	 * Do update.
	 *
	 * @param node the node
	 * @param pChange the change
	 */
	private Collection<Message<? extends AbstractDataType>> doUpdate(Node node, Double pChange) {
		if (node.getDatastore().size() <= 0) {
			return new TreeSet<>();
		}
		
		Collection<Message<? extends AbstractDataType>> messages = node.generateUpdateOperation(pChange);

		if (!messages.isEmpty()) {
			--this.updateCount;
		}
		
		return messages;
	}
	
	/**
	 * Do delete.
	 *
	 * @param node the node
	 */
	private Collection<Message<? extends AbstractDataType>> doDelete(Node node) {
		if (node.getDatastore().size() <= 0) {
			return new TreeSet<>();
		}
		
		Collection<Message<? extends AbstractDataType>> messages = node.generateDeleteOperation();

		if (!messages.isEmpty()) {
			--this.deleteCount;
		}
		
		return messages;
	}
	
	/**
	 * Do deliver.
	 */
	private Collection<Message<? extends AbstractDataType>> doDeliver() {
		if (this.getRouter().isEmpty()) {
			return new TreeSet<>();
		}

		return this.getRouter().deliverNextMessage(this.getRejectProbability());
	}
	
	/**
	 * Handle event.
	 *
	 * @param type the type
	 * @param node the node
	 * @throws ReflectiveOperationException the reflective operation exception
	 */
	private Collection<Message<? extends AbstractDataType>> handleEvent(EventType type, Node node) throws ReflectiveOperationException {
		switch(type) {
		case CREATE:
			return doCreate(node, Factory.getInstance());
		case READ:
			return doRead(node);
		case UPDATE:
			return doUpdate(node, this.getUpdateProbability());
		case DELETE:
			return doDelete(node);
		case DELIVER:
			return doDeliver();
		default:
			return new TreeSet<>();
		}
	}
	
	/**
	 * Execute.
	 *
	 * @throws ReflectiveOperationException the reflective operation exception
	 */
	public void execute() throws ReflectiveOperationException {
		this.generateNodes();
		while (this.eventCount() > 0) {

			Node node = this.pickNode();
			EventType event = this.pickEvent();

			Collection<Message<? extends AbstractDataType>> messages = this.handleEvent(event, node);
			
			this.transmit(messages);
			this.incrementTimestamp();
		}
	}

	/**
	 * Generate nodes.
	 */
	private void generateNodes() {
		for (int i=0; i<this.getNodeCount(); ++i) {
			Node node = new Node();
			this.getNodes().put(node.getNodeName(), node);
		}
	}

	/**
	 * Gets the reject probability.
	 *
	 * @return the reject probability
	 */
	public double getRejectProbability() {
		return rejectProbability;
	}

	/**
	 * Sets the reject probability.
	 *
	 * @param rejectProbability the new reject probability
	 */
	public void setRejectProbability(double rejectProbability) {
		this.rejectProbability = rejectProbability;
	}

	/**
	 * Gets the update probability.
	 *
	 * @return the update probability
	 */
	public double getUpdateProbability() {
		return this.updateProbability;
	}

	/**
	 * Sets the update probability.
	 *
	 * @param updateProbability the new reject probability
	 */
	public void setUpdateProbability(double updateProbability) {
		this.updateProbability = updateProbability;
	}

	/**
	 * Clear.
	 */
	public void clear() {
		for (Map.Entry<String, Node> entry : this.getNodes().entrySet()) {
			entry.getValue().clear();
		}
		
		this.getNodes().clear();
		this.getRouter().clear();

		this.setTimestamp(0L);
	}

	/**
	 * Sets the counts.
	 *
	 * @param createCount the create count
	 * @param readCount the read count
	 * @param updateCount the update count
	 * @param deleteCount the delete count
	 * @param nodeCount the node count
	 * @param rejectProbability the reject probability
	 * @param updateProbability the update probability
	 */
	public void setCounts(long createCount, long readCount, long updateCount, long deleteCount, long nodeCount, double rejectProbability, double updateProbability) {
		this.setCreateCount(createCount);
		this.setReadCount(readCount);
		this.setUpdateCount(updateCount);
		this.setDeleteCount(deleteCount);
		this.setNodeCount(nodeCount);
		this.setRejectProbability(rejectProbability);
		this.setUpdateProbability(updateProbability);
	}

	/**
	 * Adds the nodename.
	 *
	 * @param id the id
	 * @param nodename the nodename
	 */
	public void addNodename(String id, String nodename) {
		this.getCrdtLookup().put(id, nodename);
	}

	/**
	 * Gets the owner node.
	 *
	 * @param id the id
	 * @return the owner node
	 */
	public String getOwnerNode(String id) {
		return this.getCrdtLookup().get(id);
	}

	/**
	 * Gets the owner user.
	 *
	 * @param id the id
	 * @return the owner user
	 */
	public String getOwnerUser(String id) {
		return this.getUserLookup().get(id);
	}

	/**
	 * Adds the username.
	 *
	 * @param id the id
	 * @param username the username
	 */
	public void addUsername(String id, String username) {
		this.getUserLookup().put(id, username);
	}

	/**
	 * Gets the crdt lookup.
	 *
	 * @return the crdt lookup
	 */
	public Map<String, String> getCrdtLookup() {
		if (null == this.crdtLookup) {
			this.crdtLookup = new TreeMap<>();
		}
		
		return this.crdtLookup;
	}

	/**
	 * Gets the user lookup.
	 *
	 * @return the user lookup
	 */
	public Map<String, String> getUserLookup() {
		if (null == this.userLookup) {
			this.userLookup = new TreeMap<>();
		}
		
		return userLookup;
	}
	
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"createCount\":" + this.getCreateCount() + ",");
		sb.append("\"deleteCount\":" + this.getDeleteCount() + ",");
		sb.append("\"deliveryCount\":" + this.getDeliveryCount() + ",");
		sb.append("\"nodeCount\":" + this.getNodeCount() + ",");
		sb.append("\"readCount\":" + this.getReadCount() + ",");
		sb.append("\"updateCount\":" + this.getUpdateCount() + ",");
		sb.append("\"timestamp\":" + this.getTimestamp() + ",");
		sb.append("\"rejectProbability\":" + this.getRejectProbability() + ",");
		sb.append("\"updateProbability\":" + this.getUpdateProbability() + ",");
		sb.append("\"crdtLookup\":" + WordFactory.convert(this.getCrdtLookup()));
		sb.append("\"userLookup\":" + WordFactory.convert(this.getUserLookup()));
		sb.append("\"nodes\":" + WordFactory.convert(this.getNodes()));
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "{" + this.getSegment() + "}";
	}
}
