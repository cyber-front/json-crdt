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
package com.cyberfront.crdt.unittest.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.cyberfront.crdt.unittest.simulator.CRDTManager;
import com.cyberfront.crdt.unittest.simulator.Node;
import com.cyberfront.crdt.unittest.support.WordFactory;

/**
 * This is a support class which is used to generate various objects related to the DataType class and its derived classes
 */
public class Factory {
	
	/**
	 * An enumeration of the derived classes from DataTyep
	 */
	public enum TYPE {
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleA */
		SIMPLE_A,
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleB */
		SIMPLE_B,
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleC */
		SIMPLE_C,
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleD */
		SIMPLE_D,
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleCollection */
		SIMPLE_COLLECTION,
		
		/** Corresponds to com.cyberfront.cmrdt.data.SimpleReference */
		SIMPLE_REFERENCE
	}

	/**
	 * Gets the instances.
	 *
	 * @param count the count
	 * @return the instances
	 */
	public static Collection<AbstractDataType> getInstances(int count) {
		ArrayList<AbstractDataType> rv = new ArrayList<>();
		
		for (int i=0; i<count; ++i) {
			rv.add(getInstance());
		}
		
		return rv;
	}

	/**
	 * Gets the single instance of Factory.
	 *
	 * @return single instance of Factory
	 */
	public static AbstractDataType getInstance() {
		return getInstance(TYPE.values()[WordFactory.getRandom().nextInt(TYPE.values().length)]);
	}

	/**
	 * Gets the single instance of Factory.
	 *
	 * @param type the type
	 * @return single instance of Factory
	 */
	public static AbstractDataType getInstance(TYPE type) {
		switch (type) {
		case SIMPLE_A:
			return new SimpleA();
		case SIMPLE_B:
			return new SimpleB();
		case SIMPLE_C:
			return new SimpleC();
		case SIMPLE_D:
			return new SimpleD();
		case SIMPLE_COLLECTION:
			return new SimpleCollection();
		case SIMPLE_REFERENCE:
			return new SimpleReference();
		default:
			return null;
		}
	}

	/**
	 * Copy.
	 *
	 * @param element the element
	 * @return the data type
	 */
	public static AbstractDataType copy(AbstractDataType element) {
		TYPE type = element.getType();
		if (null == type) {
			return null;
		}

		switch (type) {
		case SIMPLE_A:
			return new SimpleA((SimpleA) element);
		case SIMPLE_B:
			return new SimpleB((SimpleB) element);
		case SIMPLE_C:
			return new SimpleC((SimpleC) element);
		case SIMPLE_D:
			return new SimpleD((SimpleD) element);
		case SIMPLE_COLLECTION:
			return new SimpleCollection((SimpleCollection) element);
		case SIMPLE_REFERENCE:
			return new SimpleReference((SimpleReference) element);
		default:
			return null;
		}
		
	}

	/**
	 * Pick type.
	 *
	 * @return the type
	 */
	private static TYPE pickType() {
		return TYPE.values()[WordFactory.getRandom().nextInt(TYPE.values().length)];
	}
	
	/**
	 * Gen CRDT.
	 *
	 * @param node the node
	 * @return the CRDT manager<? extends data type>
	 */
	public static CRDTManager<? extends AbstractDataType> genCRDT(Node node) {
		String id = UUID.randomUUID().toString();
		String nodeName = node.getNodeName();
		String userName = node.pickUser();
		
		switch (pickType()) {
		case SIMPLE_A:
			return new CRDTManager<SimpleA>(id, userName, nodeName, SimpleA.class);
		case SIMPLE_B:
			return new CRDTManager<SimpleB>(id, userName, nodeName, SimpleB.class);
		case SIMPLE_C:
			return new CRDTManager<SimpleC>(id, userName, nodeName, SimpleC.class);
		case SIMPLE_D:
			return new CRDTManager<SimpleD>(id, userName, nodeName, SimpleD.class);
		case SIMPLE_COLLECTION:
			return new CRDTManager<SimpleCollection>(id, userName, nodeName, SimpleCollection.class);
		case SIMPLE_REFERENCE:
			return new CRDTManager<SimpleReference>(id, userName, nodeName, SimpleReference.class);
		default:
			return null;
		}
		
	}
}
