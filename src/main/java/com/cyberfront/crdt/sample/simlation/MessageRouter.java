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

import com.cyberfront.crdt.sample.data.AbstractDataType;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageRouter.
 */
public class MessageRouter {
	
	/** The messages. */
	private PriorityQueue<Message<? extends AbstractDataType>> messages;
	
	/**
	 * Instantiates a new message router.
	 */
	public MessageRouter() {
		this.setMessages(new PriorityQueue<>());
	}

	/**
	 * Gets the messages.
	 *
	 * @return the messages
	 */
	public PriorityQueue<Message<? extends AbstractDataType>> getMessages() {
		return messages;
	}

	/**
	 * Sets the messages.
	 *
	 * @param messages the new messages
	 */
	private void setMessages(PriorityQueue<Message<? extends AbstractDataType>> messages) {
		this.messages = messages;
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
	 * Gets the message count.
	 *
	 * @return the message count
	 */
	public int getMessageCount() {
		return this.getMessages().size();
	}
	
	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
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
		if (this.isEmpty()) {
			return new TreeSet<>();
		}
			
		Message<? extends AbstractDataType> msg = this.getMessages().poll();
		Node node = getExecutive().getNode(msg.getDestination());
		return node.deliver(msg, pReject);
	}

	/**
	 * Adds the.
	 *
	 * @param message the message
	 */
	public void add(Message<? extends AbstractDataType> message) {
		this.getMessages().add(message);
	}
	
	/**
	 * Clear.
	 */
	public void clear() {
		this.getMessages().clear();
	}
}
