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

import com.cyberfront.crdt.sample.data.AbstractDataType;

/**
 * Interface for applying some basic ownership / management attributes to elements of the CRDT simulation
 * environment.
 *
 * @param <T> the generic type to be managed / represented in the instance
 */
public interface IManager<T extends AbstractDataType> {
	/**
	 * Gets the object class.
	 *
	 * @return the object class
	 */
	public abstract Class<T> getObjectClass();

	/**
	 * Gets the object id.
	 *
	 * @return the object id
	 */
	public abstract String getObjectId();
	
	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public abstract String getUsername();
	
	/**
	 * Gets the nodename.
	 *
	 * @return the nodename
	 */
	public abstract String getNodename();
}
