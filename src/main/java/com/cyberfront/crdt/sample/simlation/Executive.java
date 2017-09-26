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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.AbstractOperation;
import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.sample.data.Factory;
import com.cyberfront.crdt.support.Support;

/**
 * The Executive class is used to manage the overall execution of the simulation of a distributed CRDT data store. Each of the 
 * distributed nodes are intended to have identical values for the objects managed within each CRDT at the conclusion of the
 * test.
 */
public class Executive {
	
	/** The logger to log elements to the Log4J output */
	@SuppressWarnings("unused")
	private Logger logger = LogManager.getLogger(AbstractOperation.class);

	/** Defines the default number of nodes for a given simulation if not specified. */
	private static final int DEFAULT_NODE_COUNT = 16;
	
	/** Defines the default number of create operations to perform for a given simulation if not specified */
	private static final int DEFAULT_CREATE_COUNT = 256;
	
	/** Defines the default number of read operations to perform for a given simulation if not specified */
	private static final int DEFAULT_READ_COUNT = 1024;
	
	/** Defines the default number of update operations to perform for a given simulation if not specified */
	private static final int DEFAULT_UPDATE_COUNT = 2048;
	
	/** Defines the default number of delete operations to perform for a given simulation if not specified */
	private static final int DEFAULT_DELETE_COUNT = 32;
	
	/**
	 * An enumeration of the types of operations which the executive is managing
	 */
	public enum EventType {
		
		/** Represents a create operation */
		CREATE,
		
		/** Represents a read operation */
		READ,
		
		/** Represents an update operation */
		UPDATE,
		
		/** Represents a delete operation */
		DELETE,
		
		/** Represents delivery of a message to a node */
		DELIVER
	}
	
	/** The main executive for the simulation.  There is only one. */
	private static Executive instance;

	/** The nodes the executive is managing */
	private Map<UUID, Node> nodes;
	
	/** A queue which manages messages awaiting delivery */
	private MessageRouter router;
	
	/** The number of nodes the executive is simulating */
	private long nodeCount;
	
	/** Number of create operations remaining to perform */
	private long createCount;
	
	/** Number of read operations remaining to perform */
	private long readCount;
	
	/** Number of update operations remaining to perform */
	private long updateCount;
	
	/** Number of delete operations remaining to perform */
	private long deleteCount;
	
	/** The probability of an owning node rejecting an update or delete operation */
	private double rejectProbability;
	
	/** The probability of updating an individual field when computing an update to a managed object */
	private double updateProbability;
	
	/** A map relating a CRDT name to the name of the node which manages it */
	private Map<UUID, UUID> crdtLookup;
	
	/**
	 * Instantiates a new executive using the default parameters
	 */
	public Executive() {
		this.setCreateCount(DEFAULT_CREATE_COUNT);
		this.setDeleteCount(DEFAULT_DELETE_COUNT);
		this.setNodeCount(DEFAULT_NODE_COUNT);
		this.setReadCount(DEFAULT_READ_COUNT);
		this.setUpdateCount(DEFAULT_UPDATE_COUNT);
	}

	/**
	 * Retrieve the map of nodes currently under the Executive's management
	 *
	 * @return A map of all the nodes currently being managed
	 */
	public Map<UUID, Node> getNodes() {
		if (null == this.nodes) {
			this.nodes = new TreeMap<>();
		}
		
		return nodes;
	}

	/**
	 * Add a node to the map of them
	 *
	 * @param node Node instance to add to the node map
	 */
	public void addNode(Node node) {
		this.getNodes().put(node.getId(), node);
	}
	
	/**
	 * Retrieve the node of the given name
	 *
	 * @param id Identifier of the node to retrieve
	 * @return The node with the given name
	 */
	public Node getNode(UUID id) {
		return this.getNodes().get(id);
	}
	
	/**
	 * Retrieve the message router associated with this Executive instance
	 *
	 * @return The message router
	 */
	public MessageRouter getRouter() {
		if (null == this.router) {
			this.router = new MessageRouter();
		}
		
		return router;
	}
	
	/**
	 * Retrieve the static executive instance
	 *
	 * @return The static executive instance
	 */
	public static Executive getExecutive() {
		if (null == instance) {
			Executive.instance = new Executive();
		}
		
		return Executive.instance;
	}

	/**
	 * Retrieve the current timestamps value.  This is primarily a convenience function since the MessaegRouter
	 * is prmarily responsible for 
	 * @return The current timestamp for the simulation
	 */
	public long getTimestamp() {
		return this.getRouter().getTimestamp();
	}

	/**
	 * Queue up the messages in the collection for delivery to the intended recipient node 
	 *
	 * @param messages Collection of messages to queue up for delivery
	 */
	public  void transmit(Collection<Message<? extends AbstractDataType>> messages) {
		this.getRouter().getMessages().addAll(messages);
	}

	/**
	 * Retrieve the number of nodes the Executive is managing
	 *
	 * @return The number of nodes the Executive is managing
	 */
	public long getNodeCount() {
		return this.nodeCount;
	}

	/**
	 * Sets the node count.  This doesn't actually allocate the nodes, only describes the number of nodes
	 * which should be allocated.
	 *
	 * @param nodeCount The new node count
	 */
	public void setNodeCount(long nodeCount) {
		this.nodeCount = nodeCount;
	}

	/**
	 * Retrieves the number of create operations the Executive is to perform during the course of the 
	 * simulation execution 
	 *
	 * @return The create count
	 */
	public long getCreateCount() {
		return this.createCount;
	}

	/**
	 * Sets the number of create operations the Executive is to perform during the course of the
	 * simulation execution 
	 *
	 * @param createCount The new create count
	 */
	public void setCreateCount(long createCount) {
		this.createCount = createCount;
	}

	/**
	 * Retrieves the number of read operations the Executive is to perform during the course of the 
	 * simulation execution 
	 *
	 * @return The read count
	 */
	public long getReadCount() {
		return readCount;
	}

	/**
	 * Sets the number of read operations the Executive is to perform during the course of the
	 * simulation execution 
	 *
	 * @param readCount The new read count
	 */
	public void setReadCount(long readCount) {
		this.readCount = readCount;
	}

	/**
	 * Retrieves the number of update operations the Executive is to perform during the course of the 
	 * simulation execution 
	 *
	 * @return The update count
	 */
	public long getUpdateCount() {
		return updateCount;
	}

	/**
	 * Sets the number of update operations the Executive is to perform during the course of the
	 * simulation execution 
	 *
	 * @param updateCount The new update count
	 */
	public void setUpdateCount(long updateCount) {
		this.updateCount = updateCount;
	}

	/**
	 * Retrieves the number of delete operations the Executive is to perform during the course of the 
	 * simulation execution 
	 *
	 * @return The delete count
	 */
	public long getDeleteCount() {
		return deleteCount;
	}

	/**
	 * Sets the number of delete operations the Executive is to perform during the course of the
	 * simulation execution 
	 *
	 * @param deleteCount The new delete count
	 */
	public void setDeleteCount(long deleteCount) {
		this.deleteCount = deleteCount;
	}

	/**
	 * Retrieve the number of messages remaining in the delivery queue
	 *
	 * @return The delivery count
	 */
	public int getDeliveryCount() {
		return this.getRouter().getMessageCount();
	}
	
	/**
	 * Compute and retrieve the total number of operations remaining in execution of the simulation.  This number
	 * could grow as the number of operations to deliver are added to the message router
	 *
	 * @return The total number of pending operation remaining to complete the simulation 
	 */
	private long eventCount() {
		return this.getCreateCount() +
				this.getDeleteCount() +
				this.getDeliveryCount() + 
				this.getReadCount() +
				this.getUpdateCount();
	}
	
	/**
	 * Randomly pick an event class based on the number of each of the remaining types of operations
	 * which the Executive is required to complete
	 *
	 * @return The event type of the next operation to perform
	 */
	private EventType pickEvent() {
		long pick = Support.getRandom().nextLong() % this.eventCount();

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
	 * Randomly pick a node from the collection of nodes
	 *
	 * @return The randomly chosen node
	 */
	public Node pickNode() {
		if (this.getNodes().size() == 0) {
			return null;
		}
		
		int pick = Support.getRandom().nextInt(this.getNodes().size());
		
		for (Map.Entry<UUID, Node> entry : this.getNodes().entrySet()) {
			if (pick-- <= 0) {
				return entry.getValue();
			}
		}
		
		return null;
	}
	
	/**
	 * Create a new object with the specified node as the owner.  Generate and return the message set required to ensure all 
	 * the other nodes can duplicate the create operation
	 *
	 * @param <T> The generic type of the object to manage 
	 * @param node The owning node for the new object 
	 * @param object Object for the Node to manage as an owning node
	 * @return The collection of messages generated in response to the creation of the new object in the specified node 
	 */
	private Collection<Message<? extends AbstractDataType>> doCreate(Node node, AbstractDataType object) {
		Collection<Message<? extends AbstractDataType>> messages = node.generateCreateOperation(object);

		if (!messages.isEmpty()) {
			--this.createCount;
		}
		
		return messages;
	}

	/**
	 * Perform a read operation on a random object which exists in the given node.  If the node in question does not
	 * have any objects to read, no further action is taken.  Otherwise, the read operation is used to generate a collection
	 * of messages to duplicate the read operation at all of the other nodes 
	 *
	 * @param node The node from which to read
	 * @return The collection of messages needed to duplicate the read operation elsewhere
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
	 * Perform an update operation on a random object which exists in the given node.  If the node in question does not
	 * have any objects to update, no further action is taken.  Otherwise, the update operation is used to generate a collection
	 * of messages to duplicate the update operation at all of the other nodes 
	 *
	 * @param node The node from which to update
	 * @param pChange Probability of changing a node field value
	 * @return The collection of messages needed to pass the update operation throughout the simulated distributed
	 * environment
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
	 * Perform an delete operation on a random object which exists in the given node.  If the node in question does not
	 * have any objects to delete, no further action is taken.  Otherwise, the delete operation is used to generate a collection
	 * of messages to duplicate the delete operation at all of the other nodes 
	 *
	 * @param node The node from which to update
	 * @return The collection of messages needed to pass the delete operation throughout the simulated distributed
	 * environment
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
	 * Deliver the next message in the delivery queue to the node which is the intended recipient.  Delivery of this next message
	 * may result in generation of a number of additional messages which are passed back to the calling routine.
	 *
	 * @return The collection of messages which results from delivery of the next message in the delivery queue
	 */
	private Collection<Message<? extends AbstractDataType>> doDeliver() {
		Collection<Message<? extends AbstractDataType>> rv;

		if (this.getRouter().isEmpty()) {
			rv =  new TreeSet<>();
		} else {
			rv = this.getRouter().deliverNextMessage(this.getRejectProbability());
		}

		return rv;
	}
	
	/**
	 * Handle an event of the type provided.  Handling the event may result in a collection of messages which need to
	 * be delivered
	 *
	 * @param type Type of event to handle next
	 * @param node Node affected by handling of the event
	 * @return Collection of messages to deliver 
	 */
	private Collection<Message<? extends AbstractDataType>> handleEvent(EventType type, Node node) {
		Collection<Message<? extends AbstractDataType>> rv;

		switch(type) {
		case CREATE:
			rv =  doCreate(node, Factory.getInstance());
			break;
		case READ:
			rv = doRead(node);
			break;
		case UPDATE:
			rv = doUpdate(node, this.getUpdateProbability());
			break;
		case DELETE:
			rv = doDelete(node);
			break;
		case DELIVER:
			rv = doDeliver();
			break;
		default:
			rv = new TreeSet<>();
		}
		
		return rv;
	}
	
	/**
	 * Execute the simulation with the settings given. 
	 */
	public void execute() {
		this.generateNodes();
		
		while (this.eventCount() > 0) {
			Node node = this.pickNode();
			EventType event = this.pickEvent();
			Collection<Message<? extends AbstractDataType>> messages = this.handleEvent(event, node);
			this.transmit(messages);
		}
	}

	/**
	 * Generate the nodes the simulation will use to perform its execution.
	 */
	private void generateNodes() {
		for (int i=0; i<this.getNodeCount(); ++i) {
			Node node = new Node();
			this.getNodes().put(node.getId(), node);
		}
	}

	/**
	 * Retrieve the probability of rejecting an update or delete operation which occured at a non-owning node
	 *
	 * @return The reject probability
	 */
	public double getRejectProbability() {
		return rejectProbability;
	}

	/**
	 * Set the probability of rejecting an update or delete operation which occurred at a non-owning node
	 *
	 * @param rejectProbability The new update or delete operation rejection probability
	 */
	public void setRejectProbability(double rejectProbability) {
		this.rejectProbability = rejectProbability;
	}

	/**
	 * Gets the probability of updating an update of a specific field in an object being managed
	 *
	 * @return The update probability
	 */
	public double getUpdateProbability() {
		return this.updateProbability;
	}

	/**
	 * Sets the probability of updating an update of a specific field in an object being managed
	 *
	 * @param updateProbability The new probability for updating a field in a managed object
	 */
	public void setUpdateProbability(double updateProbability) {
		this.updateProbability = updateProbability;
	}

	/**
	 * Initialize the simulation Executive for a new run.
	 */
	public void clear() {
		for (Map.Entry<UUID, Node> entry : this.getNodes().entrySet()) {
			entry.getValue().clear();
		}
		
		this.getNodes().clear();
		this.getRouter().clear();
		this.getCrdtLookup().clear();
	}

	/**
	 * Registers the node and username for the owner of the CRDT and the object it manages.  If the entry already
	 * exists in the CRDT registry, it is not added (that is it does not override a previous entry)
	 *
	 * @param crdt The CRDT to register to look up owner name and user name associated with the CRDT
	 */
	public void registerCrdt(SimCRDTManager<? extends AbstractDataType> crdt) {
		if (null == this.getCrdtLookup().get(crdt.getObjectId())) {
			this.getCrdtLookup().put(crdt.getObjectId(), crdt.getManagerNodeId());
		}
	}

	/**
	 * Gets the owner node of the CRDT with the given ID
	 *
	 * @param id The id of the CRDT for which we're trying to get the owner node
	 * @return The ID value for the Node which owns the CRDT with the given ID value
	 */
	public UUID getOwnerNode(UUID id) {
		return this.getCrdtLookup().get(id);
	}

	/**
	 * Return the CRDT Node Lookup map to the calling routing
	 *
	 * @return The CRDT owner node lookup map
	 */
	private Map<UUID, UUID> getCrdtLookup() {
		if (null == this.crdtLookup) {
			this.crdtLookup = new TreeMap<>();
		}
		
		return this.crdtLookup;
	}

	/**
	 * Return a segment needed to generate the serialized version of the Executive.  The format is JSON-like
	 * @return The segment containing a string serialization of the Executiv in a JSON-like format.
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"createCount\":" + this.getCreateCount() + ",");
		sb.append("\"deleteCount\":" + this.getDeleteCount() + ",");
		sb.append("\"deliveryCount\":" + this.getDeliveryCount() + ",");
		sb.append("\"nodeCount\":" + this.getNodeCount() + ",");
		sb.append("\"readCount\":" + this.getReadCount() + ",");
		sb.append("\"updateCount\":" + this.getUpdateCount() + ",");
		sb.append("\"rejectProbability\":" + this.getRejectProbability() + ",");
		sb.append("\"updateProbability\":" + this.getUpdateProbability() + ",");
		sb.append("\"router\":" + this.getRouter().toString() + ",");
		sb.append("\"crdtLookup\":" + Support.convert(this.getCrdtLookup()) + ",");
		sb.append("\"nodes\":" + Support.convert(this.getNodes()));
		
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + this.getSegment() + "}";
	}
	
	public void checkMessageConsistency() {
		for (Map.Entry<UUID, Node> node : this.getNodes().entrySet()) {
			node.getValue().checkMessageConsistency();
		}
		
		this.getRouter().checkMessageConsistency();
	}
	
	public void checkMessageCount() {
		for (Map.Entry<UUID, Node> node : this.getNodes().entrySet()) {
			node.getValue().checkMessageCount();
		}
	}
	
	public void checkOperationValidity() {
		for (Map.Entry<UUID, Node> node : this.getNodes().entrySet()) {
			node.getValue().checkOperationValidity();;
		}
	}
}
