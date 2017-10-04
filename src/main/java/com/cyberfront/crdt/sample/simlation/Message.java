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
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operations.AbstractOperation.OperationType;
import com.cyberfront.crdt.operations.OperationManager.StatusType;
import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.support.Support;

/**
 * The Message class encapsulates a delivery mechanism for moving an operation generated at one node on a particular CRDT object
 * to another Node so that it may be delivered to the corresponding CRDT at the destination node.  The generic type of the Message
 * corresponds to the type of object for the operation which the Message object is delivering.  
 *
 * @param <T> The generic type of the object the operations is intended to operate upon
 */
public final class Message<T extends AbstractDataType> implements Comparable<Message<? extends AbstractDataType>> {
	/** A logger for writing to the local log output. */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(Message.class);
	
	/** The delivery timestamp associated with the message. */
	private final Long deliveryTime;
	
	/** The destination node identifier. */
	private final UUID dstNodeId;
	
	/** The source node identifier */
	private final UUID srcNodeId;
	
	/** The operation manager to deliver to the destination node */
	private final SimOperationManager<T> mgr;
	
	/**
	 * Instantiates a new message.
	 *
	 * @param srcNodeId Name of the source node for the message
	 * @param dstNodeId Name of the message destination node
	 * @param mgr Operation manager containing the operation details to pass from the source to destination node
	 * @param timestamp The scheduled delivery time stamp
	 */
	public Message(UUID srcNodeId, UUID dstNodeId, SimOperationManager<T> mgr, Long timestamp) {
		this.srcNodeId = srcNodeId;
		this.dstNodeId = dstNodeId;
		this.mgr = mgr.copy();
		this.deliveryTime  = timestamp;
	}
	
	/**
	 * Instantiates a new message given the specific values needed to build the Message class instance
	 *
	 * @param srcNodeId The node identifier value of the node from which the Message originated
	 * @param dstNodeId The node identifier value of the node to which the Message is intended to be delivered
	 * @param mgr The operation manager which is to be delivered to the destination node
	 */
	public Message(UUID srcNodeId, UUID dstNodeId, SimOperationManager<T> mgr) {
		this(srcNodeId,
			dstNodeId,
			 mgr,
			 Executive.getExecutive().getTimestamp() + Support.getRandom().nextInt(65536)
		);
	}
	
	/**
	 * Instantiates a new message by copying a source Message instance
	 *
	 * @param src The source Message to copy
	 */
	private Message(Message<T> src) {
		this(src.getSource(),
			src.getDestination(),
			src.getManager(),
			src.getDeliveryTime());
	}
	
	/**
	 * Gets the destination node identifier
	 *
	 * @return the destination node identifier
	 */
	public UUID getDestination() { return this.dstNodeId; }

	/**
	 * Gets the source node identifier
	 *
	 * @return the source node identifier
	 */
	public UUID getSource() { return this.srcNodeId; }

	/**
	 * Gets the operation manager to be delivered
	 *
	 * @return the operation manager to be delivered
	 */
	public SimOperationManager<T> getManager() { return mgr; }
	
	/**
	 * Gets the time to deliver the message.
	 *
	 * @return The delivery time stamp value
	 */
	public Long getDeliveryTime() { return this.deliveryTime; }

	/**
	 * Build and return a copy of the given message
	 * 
	 * @param msg Message to copy
	 * @return Copy of the given message
	 */
	public Message<T> copy(Message<T> msg) {
		return new Message<>(msg);
	}
	
	/**
	 * Gets a segment for the class instance to show the value of the various fields
	 *
	 * @return The String segment containing the value of the various fields formatted as a JSON string
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();

		sb.append("\"source\":\"" + this.getSource() + "\",");
		sb.append("\"destination\":\"" + this.getDestination() + "\",");
		sb.append("\"deliveryTime\":" + this.getDeliveryTime() + ",");
		sb.append("\"manager\":" + this.getManager().toString());
		
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Message<? extends AbstractDataType> o) {
		int compareDelTime = Long.compare(this.getDeliveryTime(), o.getDeliveryTime());
		int compareGenTime = Long.compare(this.getManager().getOperation().getTimeStamp(), o.getManager().getOperation().getTimeStamp());
		
		return compareDelTime == 0 ? compareGenTime : compareDelTime;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		
		if (this == obj) {
			return true;
		}
		
		if (! (obj instanceof Message)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		Message<? extends AbstractDataType> msg = (Message<? extends AbstractDataType>) obj;
		
		boolean rv = this.getDestination().equals(msg.getDestination());
		rv = rv && this.getSource().equals(msg.getSource());
		rv = rv && this.getManager().equals(msg.getManager());
		rv = rv && Long.compare(this.getDeliveryTime(),  msg.getDeliveryTime()) == 0;
		
		return rv;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 1;
		
		hash = hash * 11 + this.getDestination().hashCode();
		hash = hash * 13 + this.getSource().hashCode();
		hash = hash * 17 + this.getDeliveryTime().hashCode();
		hash = hash * 19 + this.getManager().hashCode();
		
		return hash;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append('{');
		sb.append(this.getSegment());
		sb.append('}');
		
		return sb.toString();
	}

	/**
	 * Filter the given messages with the criteria presented and return a list containing the filtered collection of Message instances
	 *
	 * @param collection Input collection of messages to filter
	 * @param opType The operation type used as the basis of the filter
	 * @param criteriaType When true return all Messages with an operation of the given type; when false return all Message instances with 
	 * an operation different than the given type
	 * @return The collection of Message instance retrieved as part of the filter operation
	 */
	public static Collection<Message<? extends AbstractDataType>> filterMessages(Collection<Message<? extends AbstractDataType>> collection, OperationType opType, boolean criteriaType) {
		return collection.stream()
				.filter(op -> (opType == op.getManager().getOperation().getType()) == criteriaType)
				.collect(Collectors.toList());
	}

	/**
	 * Filter the given messages with the criteria presented and return a list containing the filtered collection of Message instances
	 *
	 * @param collection Input collection of messages to filter
	 * @param stType The status type used as the basis of the filter
	 * @param criteriaStatus When true return all Messages with an operation of the given status; when false return all Message instances with 
	 * an operation different than the given status
	 * @return The collection of Message instance retrieved as part of the filter operation
	 */
	public static Collection<Message<? extends AbstractDataType>> filterMessages(Collection<Message<? extends AbstractDataType>> collection, StatusType stType, boolean criteriaStatus) {
		return collection.stream()
				.filter(op -> (stType == op.getManager().getStatus()) == criteriaStatus)
				.collect(Collectors.toList());
	}

	/**
	 * Filter the given messages with the criteria presented and return a list containing the filtered collection of Message instances
	 *
	 * @param collection Input collection of messages to filter
	 * @param opType The operation type used as the basis of the filter
	 * @param criteriaType When true return all Messages with an operation of the given type; when false return all Message instances with 
	 * an operation different than the given type
	 * @param stType The status type used as the basis of the filter
	 * @param criteriaStatus When true return all Messages with an operation of the given status; when false return all Message instances with 
	 * an operation different than the given status
	 * @return The collection of Message instance retrieved as part of the filter operation
	 */
	public static Collection<Message<? extends AbstractDataType>> filterMessages(Collection<Message<? extends AbstractDataType>> collection, OperationType opType, boolean criteriaType, StatusType stType, boolean criteriaStatus) {
		return collection.stream()
				.filter(op -> (stType == op.getManager().getStatus()) == criteriaType && (opType == op.getManager().getOperation().getType()) == criteriaStatus)
				.collect(Collectors.toList());
	}
	
	/**
	 * Check consistency.
	 *
	 * @param msg the msg
	 */
	public static void checkConsistency(Message<? extends AbstractDataType> msg) {
		StatusType type = msg.getManager().getStatus();
		UUID sourceId = msg.getSource();
		UUID authId = Executive.getExecutive().getOwnerNode(msg.getManager().getObjectId());
		
		boolean authoritative = type == StatusType.APPROVED || type == StatusType.REJECTED;
		if (authoritative != (sourceId == authId) && OperationType.READ != msg.getManager().getOperation().getType()) {
			String text = "Message with status: " + type.toString();
			text += " but with message sourceID: " + sourceId + " and authId: " + authId + "\n" + msg;
			throw new IllegalStateException(text);
		}
	}
	
	/**
	 * Check consistency.
	 *
	 * @param messages the messages
	 */
	public static void checkConsistency(Collection<Message<? extends AbstractDataType>> messages) {
		for(Message<? extends AbstractDataType> msg : messages) {
			Message.checkConsistency(msg);
		}
	}
}
