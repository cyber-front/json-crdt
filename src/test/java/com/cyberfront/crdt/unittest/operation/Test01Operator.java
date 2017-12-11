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
package com.cyberfront.crdt.unittest.operation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.cyberfront.crdt.operation.Operation.OperationType;
import com.cyberfront.crdt.operation.Operation;
import com.cyberfront.crdt.unittest.data.AssessmentSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This contains a class used for performing unit tests designed to create a number of 
 * AbstractDataType instances using the factory.  It is successful if it creates these
 * objects and doesn't return null.
 */
public class Test01Operator {
	public static class UpdateTest extends AssessmentSupport {
		/** Logger to use when displaying state information */
		private static final Logger logger = LogManager.getLogger(Test01Operator.UpdateTest.class);

		/** Constant object mapper used to convert between JSON formatted objects and their equivalent POJO */
//		@SuppressWarnings("unused")
		private static final ObjectMapper mapper = new ObjectMapper(); 
		
		private static final OperationType[] TYPE_VALUES = {
				OperationType.CREATE,
				OperationType.UPDATE,
				OperationType.DELETE,
				OperationType.READ,
				null
		};

		/**
		 * Default constructor which initialized fields to their default values
		 */
		public UpdateTest() {
			super();
		}
		
		/**
		 * Constructor to deliberately initialize each field to the associated values provided
		 * @param trialCount Trial count to use for the test activity
		 * @param abbreviatedFactor Abbreviation factor to use the basis of this CreateTest instance when the abbreviated flag is set
		 * @param stressedFactor Stressed factor to use when the the stressed flag is set
		 * @param abbreviated Abbreviated flag which indicates when to divide different test parameters by the abbreviatedFactor
		 * @param stressed Stressed flag which indicates when to multiply different test parameters by the stressedFactor
		 */
		public UpdateTest(long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
			super(trialCount, abbreviatedFactor, stressedFactor, abbreviated, stressed);
		}
		
		private static Collection<Long> genTimestamps(long count, double pNull) {
			Collection<Long> rv = new ArrayList<>();
			
			for (long i=0; i<count; ++i) {
				rv.add(genTimestamp(pNull));
			}
			
			return rv;
		}
		
		private String display(OperationType type, Long timestamp, JsonNode operation) {
			String delim = "{";
			StringBuilder args = new StringBuilder();
			if (null != type) {
				args.append(delim + "\"type\":\"" + type.toString() + "\"");
				delim = ",";
			}
			
			if (null != timestamp) {
				args.append(delim + "\"timestamp\":" + timestamp.toString());
				delim = ",";
			}

			if (null != operation) {
				args.append(delim + "\"operation\":" + operation.toString());
			} else if ("{".equals(delim)) {
				args.append(delim);
			}

			args.append("}");

			return args.toString();
		}

		private boolean evaluateNonUpdate(UUID id, OperationType type, Long timestamp, JsonNode operation) {
			Operation op = null;

			if (null == id || null == type || null == timestamp || timestamp < 0 || null != operation) {
				try {
					op = new Operation(id, type, operation, timestamp);  
					logger.info("   -- Failure: " + op.toString());
					assertNull("Should have generated Exception", op.toString());
				} catch (IllegalArgumentException ex) {
					return false;
				}
			} else {
				try {
					op = new Operation(id, type, operation, timestamp);  
				} catch (IllegalArgumentException ex) {
					logger.info(ex.toString());
					logger.info("   -- Failure: " + this.display(type, timestamp, operation));
					assertNull("Should not have generated Exception");
				}
			}
			
			return null != op;
		}
		
		private boolean evaluateUpdate(UUID id, OperationType type, Long timestamp, JsonNode operation) {
			Operation op = null;
			

			if (null == id || null == type || null == timestamp || timestamp < 0 || null == operation) {
				try {
					op = new Operation(UUID.randomUUID(), type, operation, timestamp);  
					logger.info("   -- Failure: " + op.toString());
					assertNull("Should have generated Exception", op);
				} catch (IllegalArgumentException ex) {
					return false;
				}
			} else {
				try {
					op = new Operation(id, type, operation, timestamp);  
				} catch (IllegalArgumentException ex) {
					logger.info(ex.toString());
					logger.info("   -- Failure: " + this.display(type, timestamp, operation));
					assertTrue("Should not have generated Exception", false);
				}
			}
			
			return null != op;
		}
		
		private boolean evaluate(UUID id, OperationType type, Long timestamp, JsonNode operation) {
			
			if (null == type || null == timestamp || null == id || 0 > timestamp) {
				try {
					Operation op = new Operation(id, type, operation, timestamp);  
					logger.info("   -- Failure: " + op.toString());
					assertNull("Should have generated Exception", op);
				} catch (IllegalArgumentException ex) {
					return false;
				}
			}
			
			boolean valid = (OperationType.UPDATE.equals(type)) ?
					this.evaluateUpdate(id, type, timestamp, operation) :
					this.evaluateNonUpdate(id, type, timestamp, operation);

			if (valid) {
				Operation op = null;
				JsonNode doc = null;
				Operation restored = null;
				
				try {
					op = new Operation(id, type, operation, timestamp);
					doc = mapper.valueToTree(op);
					assertNotNull(op.toString(), doc);
					restored = mapper.treeToValue(doc, Operation.class);
					assertNotNull(doc.toString(), restored);
				} catch (JsonProcessingException e) {
					logger.error(e);
					logger.info("\n   original - " + (null == op ? "null" :op.toString()));
					logger.info("JSON format - " + (null == doc ? "null" : doc.toString()));
					logger.info("   restored - " + (null == restored ? "null" : restored.toString()));
					assertTrue(false);
				}
			}
					
			return valid;
		}
		
		/**
		 * Test the ability to create a new concrete object derived from AbstractDataType
		 */
		public void operationTest() {
			logger.info("\n** Test01Operator: {\"count\":" + this.getTrialCount() + "}");
			
			for (long i = 0; i<getTrialCount(); ++i) {
				logger.info("   trial " + (i+1) + " of " + this.getTrialCount());
				Collection<Long> timestamps = genTimestamps(32, 0.1);
				Collection<JsonNode> operations = generateOperations(64, 8, 1.0, 1.0, 1.0, 1.0);
				Collection<UUID> ids = genIds(4, 0.2);

				for (UUID id : ids ) {
					for (OperationType type : TYPE_VALUES) {
						for (Long timestamp : timestamps) {
							for (JsonNode operation : operations) {
								evaluate(id, type, timestamp, operation);
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * The main unit test routine used to perform the actual test execution 
	 */
	@Test
	public void test() {
		UpdateTest test = new UpdateTest();
		test.operationTest();
	}
}
