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
import java.util.PriorityQueue;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.support.Support;

/**
 * The MessageRouter class is responsible for message delivery to the correct node in the distributed environment.  Messages are inserted 
 * into the message priority queue asynchronously 
 */
public class MessageRouter {
	/** A logger for writing to the local log output. */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(MessageRouter.class);

	/** Timestamp of the most recent message delivery, or the current message being delivered and processed. */
	private long timestamp = 0L;
	
	/** A priority queue ordered byt message time stamp */
	private PriorityQueue<Message<? extends AbstractDataType>> messages;
	
	/**
	 * Retrieve priority queue containing the messages which are pending delivery
	 *
	 * @return the messages pending delivery
	 */
	private PriorityQueue<Message<? extends AbstractDataType>> getMessages() {
		if (null == this.messages) {
			this.messages = new PriorityQueue<>();
		}
		
		return this.messages;
	}

	/**
	 * Retrieve the current time stamp value which equates to the timestamp of the most recent message delivered or 
	 * being delivered
	 * @return The current timestamp value
	 */
	public long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Set the current timestamp to the value given
	 * @param timestamp New timestamp value
	 */
	private void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the number of messages pending delivery
	 *
	 * @return the number of messages pending delivery
	 */
	public int getMessageCount() {
		return this.getMessages().size();
	}
	
	/**
	 * Returns true exactly when the message queue is empty
	 *
	 * @return true, if the message queue is empty
	 */
	public boolean isEmpty() {
		return this.getMessages().isEmpty();
	}
	
	/**
	 * Deliver the next message with a probability the owning node will reject the update the message contains.
	 * Rejections are for Create, Update and Delete operations, though in practice, Creates are only performed on 
	 * the owning node, so those don't actually get rejected. 
	 *
	 * @param pReject Probability the owning node will reject the  message payload
	 * @return The collection of messages to forward to other nodes in response to handling the
	 * given message and its embedded operation
	 */
	public Collection<Message <? extends AbstractDataType>> deliverNextMessage(Double pReject) {
		Collection<Message <? extends AbstractDataType>> rv;
		if (this.isEmpty()) {
			rv =  new TreeSet<>();
		} else {
			Message<? extends AbstractDataType> msg = this.getMessages().poll();
			this.setTimestamp(msg.getDeliveryTime());
			Node node = Executive.getExecutive().getNode(msg.getDestination());
			rv = node.push(msg, pReject);
		}

		return rv;
	}

	/**
	 * Add a collection of messages to the message queue.
	 *
	 * @param messages the collection of messages to add to the message queue
	 */
	public void add(Collection<Message<? extends AbstractDataType>> messages) {
		this.getMessages().addAll(messages);
	}
	
	/**
	 * Adds a single message to the message queue
	 *
	 * @param message The message to add to the message queue
	 */
	public void add(Message<AbstractDataType> message) {
		this.getMessages().add(message);
	}
	
	/**
	 * Clear the message queue and reset the timestamp to 0
	 */
	public void clear() {
		this.setTimestamp(0L);
		this.getMessages().clear();
	}

	/**
	 * Generate and return the JSON formated segment for the elements comprising this MessageRouter instance  
	 * @return the JSON formated segment for the elements comprising this MessageRouter instance
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"timestamp\":" + this.getTimestamp() + ",");
		sb.append("\"messages\":" + Support.convert(this.getMessages()));
		
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + this.getSegment() + "}";
	}
	
	/**
	 * Check the message consistency of all the messages pending delivery.
	 */
	public void checkMessageConsistency() {
		Message.checkConsistency(this.getMessages());
	}
}
