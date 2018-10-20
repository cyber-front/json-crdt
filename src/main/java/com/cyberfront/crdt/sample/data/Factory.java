/*
 * Copyright (c) 2018 Cybernetic Frontiers LLC
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
package com.cyberfront.crdt.sample.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.cyberfront.crdt.sample.simulation.Node;
import com.cyberfront.crdt.sample.simulation.SimCRDTManager;
import com.cyberfront.crdt.support.Support;

/**
 * This is a support class which is used to generate various objects related to the DataType class and its derived classes
 */
public class Factory {
	
	/**
	 * An enumeration of the derived classes from DataTyep
	 */
	public enum DataType {
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleString */
		SIMPLE_STRING,
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleInteger */
		SIMPLE_INTEGER,
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleDouble */
		SIMPLE_DOUBLE,
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleBoolean */
		SIMPLE_BOOLEAN,
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleCollection */
		SIMPLE_COLLECTION,
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleReference */
		SIMPLE_REFERENCE
	}

	/**
	 * Generates and returns a collection of `count` concrete instances of AbstractDataType
	 *
	 * @param count The number of instances to generate 
	 * @return The instances generated
	 */
	public static Collection<AbstractDataType> getInstances(int count) {
		ArrayList<AbstractDataType> rv = new ArrayList<>();
		
		for (int i=0; i<count; ++i) {
			rv.add(getInstance());
		}
		
		return rv;
	}

	/**
	 * Gets the single concrete instance of AbstractDataType
	 *
	 * @return the single concrete instance of AbstractDataType 
	 */
	public static AbstractDataType getInstance() {
		return getInstance(pickType());
	}

	/**
	 * Gets the single instance of a type corresponding to the given type
	 *
	 * @param type TYPE enumeration value corresponding to the concrete type to instantiate and return
	 * @return single concrete instance of a AbstractDataType
	 */
	public static AbstractDataType getInstance(DataType type) {
		switch (type) {
		case SIMPLE_STRING:
			return new SimpleString();
		case SIMPLE_INTEGER:
			return new SimpleInteger();
		case SIMPLE_DOUBLE:
			return new SimpleDouble();
		case SIMPLE_BOOLEAN:
			return new SimpleBoolean();
		case SIMPLE_COLLECTION:
			return new SimpleCollection();
		case SIMPLE_REFERENCE:
			return new SimpleReference();
		default:
			return null;
		}
	}

	/**
	 * Pick type at random
	 *
	 * @return The randomly selected type
	 */
	private static DataType pickType() {
		return DataType.values()[Support.getRandom().nextInt(DataType.values().length)];
	}
	
	/**
	 * Generate and return a CRDTManager for the given `node`.   
	 *
	 * @param ownerNode The node for which the resulting CRDTManager is to be the approver for operations performed elsewhere
	 * @param managerNode The node which is locally managed (i.e. the local node) 
	 * @param id The identifier for the new CRDT
	 * @return The CRDTManager with the managed type 
	 */
	public static SimCRDTManager<? extends AbstractDataType> genCRDT(Node ownerNode, Node managerNode, UUID id) {
		return genCRDT(ownerNode, managerNode, pickType(), id);
	}
		
	/**
	 * Generate and return a CRDTManager for the given `node` and of the given `type`   
	 *
	 * @param ownerNode The node for which the resulting CRDTManager is to be the approver for operations performed elsewhere
	 * @param managerNode The node which is locally managed (i.e. the local node) 
	 * @param type The enumeration corresponding to the concrete type of AbstractDataType to generate 
	 * @param id The identifier for the new CRDT
	 * @return The CRDTManager with the managed type 
	 */
	public static SimCRDTManager<? extends AbstractDataType> genCRDT(Node ownerNode, Node managerNode, DataType type, UUID id) {
		switch (type) {
		case SIMPLE_STRING:
			return new SimCRDTManager<>(id, ownerNode.getId(), managerNode.getId(), SimpleString.class);
		case SIMPLE_INTEGER:
			return new SimCRDTManager<>(id, ownerNode.getId(), managerNode.getId(), SimpleInteger.class);
		case SIMPLE_DOUBLE:
			return new SimCRDTManager<>(id, ownerNode.getId(), managerNode.getId(), SimpleDouble.class);
		case SIMPLE_BOOLEAN:
			return new SimCRDTManager<>(id, ownerNode.getId(), managerNode.getId(), SimpleBoolean.class);
		case SIMPLE_COLLECTION:
			return new SimCRDTManager<>(id, ownerNode.getId(), managerNode.getId(), SimpleCollection.class);
		case SIMPLE_REFERENCE:
			return new SimCRDTManager<>(id, ownerNode.getId(), managerNode.getId(), SimpleReference.class);
		default:
			return null;
		}
	}
}
