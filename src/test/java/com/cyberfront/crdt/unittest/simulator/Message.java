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

import com.cyberfront.crdt.unittest.data.AbstractDataType;
import com.cyberfront.crdt.unittest.support.WordFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class Message.
 *
 * @param <T> the generic type
 */
public class Message<T extends AbstractDataType> implements Comparable<Message<? extends AbstractDataType>> {
	
	/** The priority. */
	private Long priority;
	
	/** The destination. */
	private String destination;
	
	/** The mgr. */
	private OperationManager<T> mgr;
	
	/**
	 * Instantiates a new message.
	 *
	 * @param destination the destination
	 * @param mgr the mgr
	 * @param priority the priority
	 */
	public Message(String destination, OperationManager<T> mgr, Long priority) {
		this.setPriority(priority);
		this.setDestination(destination);
		this.setManager(mgr.copy());
	}
	
	/**
	 * Instantiates a new message.
	 *
	 * @param destination the destination
	 * @param mgr the mgr
	 */
	public Message(String destination, OperationManager<T> mgr) {
		this(destination, mgr, WordFactory.getRandom().nextLong());
	}
	
	/**
	 * Instantiates a new message.
	 *
	 * @param src the src
	 */
	public Message(Message<T> src) {
		this.setDestination(src.getDestination());
		this.setManager(src.getManager());
		this.setPriority(this.getPriority());
	}
	
	/**
	 * Gets the destination.
	 *
	 * @return the destination
	 */
	public String getDestination() { return destination; }
	
	/**
	 * Gets the manager.
	 *
	 * @return the manager
	 */
	public OperationManager<T> getManager() { return mgr; }
	
	/**
	 * Gets the priority.
	 *
	 * @return the priority
	 */
	public Long getPriority() { return priority; }

	/**
	 * Sets the destination.
	 *
	 * @param destination the new destination
	 */
	private void setDestination(String destination) { this.destination = destination; }
	
	/**
	 * Sets the manager.
	 *
	 * @param mgr the new manager
	 */
	private void setManager(OperationManager<T> mgr) { this.mgr = mgr; }
	
	/**
	 * Sets the priority.
	 *
	 * @param priority the new priority
	 */
	private void setPriority(Long priority) { this.priority = priority; }

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Message<? extends AbstractDataType> o) {
		int comparePri = Long.compare(this.getPriority(), o.getPriority());
		int compareTim = Long.compare(this.getManager().getOperation().getTimeStamp(), o.getManager().getOperation().getTimeStamp());
		
		return comparePri == 0 ? compareTim : comparePri;
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
		rv = rv && this.getManager().equals(msg.getManager());
		rv = rv && Long.compare(this.getPriority(),  msg.getPriority()) == 0;
		
		return rv;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 1;
		
		hash = hash * 11 + this.getDestination().hashCode();
		hash = hash * 13 + this.getPriority().hashCode();
		hash = hash * 17 + this.getManager().hashCode();
		
		return hash;
	}
	
	/**
	 * Gets the segment.
	 *
	 * @return the segment
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();

		sb.append("\"destination\":\"" + this.getDestination() + "\",");
		sb.append("\"priority\":" + this.getPriority() + ",");
		sb.append("\"manager\":" + this.getManager().toString());
		
		return sb.toString();
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
}
