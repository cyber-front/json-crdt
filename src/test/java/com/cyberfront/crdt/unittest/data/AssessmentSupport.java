package com.cyberfront.crdt.unittest.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.cyberfront.crdt.sample.data.AbstractDataType;
import com.cyberfront.crdt.sample.data.Factory;
import com.cyberfront.crdt.support.Support;
import com.cyberfront.crdt.unittest.data.Test01Create.CreateTest;
import com.cyberfront.crdt.unittest.data.Test02Update.UpdateTest;
import com.cyberfront.crdt.unittest.data.Test03Clone.CloneTest;
import com.cyberfront.crdt.unittest.simulator.Test01Simulation.SimulationTest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.thedeanda.lorem.LoremIpsum;

/**
 * The AssessmentSupport class is a base class for all of the unit test classes.  It manages a number of attributes for test
 * cases which comprise the collection of its derived classes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @Type(value = CreateTest.class, name = "CreateTest"),
    @Type(value = UpdateTest.class, name = "UpdateTest"),
    @Type(value = CloneTest.class, name = "CloneTest"),
    @Type(value = SimulationTest.class, name = "SimulationTest"),
    @Type(value = Test04Json.class, name = "Test04Json")
    })
public class AssessmentSupport {
	/** Constant string for the add operation */ 
	protected static final String ADD = "add"; 

	/** Constant string for the replace operation */ 
	protected static final String REPLACE = "replace"; 

	/** Constant string for the remove operation */ 
	protected static final String REMOVE = "remove"; 

	/** Constant containing the desired operations */ 
	protected static final List<String> OPERATIONS = Arrays.asList(ADD, REPLACE, REMOVE);

	/** Name of the path tag in the JSON operations */ 
	protected static final String PATH = "path"; 

	/** Constant string for the type of operation */ 
	protected static final String TYPE = "type"; 

	/** Name of the value tag in the JSON operations */ 
	protected static final String VALUE = "value";

	/** Name of the operation tag in the JSON operations */ 
	protected static final String OP = "op";

	/** Constant defining the number of AbstractDataType elements to create in the unit test */
	private static final long TRIAL_COUNT = 32L;

	/** Flag used to indicate whether or not to abbreviate the test */
	private static final boolean ABBREVIATED = true;
	
	/** Division factor to apply to abbreviated tests */
	private static final long ABBREVIATED_FACTOR = 8;
	
	/** Flag used to indicate whether or not to perform a stress test */ 
	private static final boolean STRESSED = false;
	
	/** Factor to apply during stress tests */
	private static final long STRESSED_FACTOR = 4;
	
	/** The ObjectMapper used to translate between JSON and any of the classes derived from
	 * com.cyberfront.crdt.unittest.data.AbstractDataType */
	private static ObjectMapper mapper = new ObjectMapper();

	/** Number of trials to conduct in the unit test*/
	private long trialCount;
	
	/** Factor used to enhance the various counts when attempting to test under higher stress levels */ 
	private long stressedFactor;
	
	/** Division factor to reduce the various counts when running abbreviated tests. */
	private long abbreviatedFactor;
	
	/** Flag indicating whether the unit test derived from this class are to be stressed */   
	private boolean stressed;
	
	/** Flag indicating whether the unit test derived from this class are to be abbreviated */   
	private boolean abbreviated;

	/**
	 * Default class constructor which initializes the members to their default values given by the 
	 * corresponding static constant values.
	 */
	public AssessmentSupport() {
		this.setAbbreviated(ABBREVIATED);
		this.setAbbreviatedFactor(ABBREVIATED_FACTOR);
		this.setStressed(STRESSED);
		this.setStressedFactor(STRESSED_FACTOR);
		this.setTrialCount(TRIAL_COUNT);
	}

	/**
	 * Class constructor used to explicitly define the various parameters for the base test class
	 * 
	 * @param trialCount Number of trials to perform over the course of the unit test
	 * @param abbreviatedFactor Division factor to use on the argument when performing an abbreviated test
	 * @param stressedFactor Multiplication factor to use on the arguments when performing a stress test
	 * @param abbreviated Flag indicating whether this is an abbreviated test
	 * @param stressed Flag indicating whether this is a stress test
	 */
	public AssessmentSupport(long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
		this.setAbbreviated(abbreviated);
		this.setAbbreviatedFactor(abbreviatedFactor);
		this.setStressed(stressed);
		this.setStressedFactor(stressedFactor);
		this.setTrialCount(trialCount);
	}

	/**
	 * Retrieve the ObjectMapper primarily used to perform bidirectional transformations between JSON and 
	 * POJO representations of objects  
	 * @return The static ObjectMapper for the AssessmentSupport class and its derived classes
	 */
	protected static ObjectMapper getMapper() {
		return mapper;
	}

	/**
	 * Retrieve the number of trials to perform adjusted for stress and abbreviation factors if eith
	 * of those flags are set to rue.	
	 * 
	 * @return The number of trials to perform
	 */
	public long getTrialCount() {
		return this.trialCount * this.getStressedFactor() / this.getAbbreviatedFactor();
	}

	/**
	 * Retrieve the value of the abbreviated flag
	 * @return The current value of the abbreviated flag
	 */
	public boolean isAbbreviated() {
		return abbreviated;
	}

	/**
	 * Retrieve the abbreviation factor which is used to divide other test parameters when performing abbreviated tests
	 * @return The current value of the abbreviation factor
	 */
	public long getAbbreviatedFactor() {
		return this.isAbbreviated() ? this.abbreviatedFactor : 1;
	}

	/**
	 * Retrieve the value of the stressed flag
	 * @return The current value of the stressed flag
	 */
	public boolean isStressed() {
		return stressed;
	}

	/**
	 * Retrieve the stressing factor which is used to multiply other test parameters when performing stress tests
	 * @return The current value of the abbreviation factor
	 */
	public long getStressedFactor() {
		return this.isStressed() ? this.stressedFactor : 1;
	}

	/**
	 * Set the number of trials to perform
	 * @param trialCount New value of the trialCount
	 */
	public void setTrialCount(long trialCount) {
		this.trialCount = trialCount;
	}

	/**
	 * Set the value of the stress testing factor
	 * @param stressedFactor Value of the stress testing factor
	 */
	public void setStressedFactor(long stressedFactor) {
		this.stressedFactor = stressedFactor;
	}

	/**
	 * Set the value of the abbreviation factor for abbreviated testing
	 * @param abbreviatedFactor Value of the abbreviated testing factor
	 */
	public void setAbbreviatedFactor(long abbreviatedFactor) {
		this.abbreviatedFactor = abbreviatedFactor;
	}

	/**
	 * Set the flag to perform stress testing
	 * @param stressed Flag for performing stress testing; stress testing will occur exactly when the flag is set to true
	 */
	public void setStressed(boolean stressed) {
		this.stressed = stressed;
	}

	/**
	 * Set the flag to perform abbreviated testing
	 * @param abbreviated Flag for performing stress testing; abbreviated testing will occur exactly when the flag is set to true
	 */
	public void setAbbreviated(boolean abbreviated) {
		this.abbreviated = abbreviated;
	}
	
	/**
	 * Generate a sequence of SimpleCollection data objects from an inital state through count updates where each
	 * field has a   
	 * @param count Number of state transitions to generate
	 * @param pChange Probability of changing a given field in the data object being changed
	 * @return A collection of state transitions, the number being that given
	 */
	public Collection<AbstractDataType> generateObjectSequence(long count, double pChange) {
		Collection<AbstractDataType> rv = new ArrayList<>();
		
		AbstractDataType object = Factory.getInstance();
		
		for (long i=0; i<count; ++i) {
			rv.add(object.copy());
			object = object.copy(pChange);
		}
		
		return rv;
	}
	
	/**
	 * Generate a sequence of JSON objects given a sequence of AbstractDataTypes
	 * @param objects Collection of objects containing the AbstractDataType elements
	 * @return Collection of JsonNode equivalents to the given AbstractDataTypes
	 */
	public Collection<JsonNode> generateJsonSequence(Collection<AbstractDataType> objects) {
		Collection<JsonNode> rv = new ArrayList<>();
		
		for (AbstractDataType object : objects) {
			rv.add(getMapper().valueToTree(object));
		}
		
		return rv;
	}
	
	/**
	 * Create a sequence of JSON updates from a sequence of JsonNode documents 
	 * @param documents Collection of documents to compute differences
	 * @return Collection of differences for the sequnce of JsonNode documents provided
	 */
	public Collection<JsonNode> generateDifferenceSequence(Collection<JsonNode> documents) {
		Collection<JsonNode> rv = new ArrayList<>();
		JsonNode previous = getMapper().createObjectNode();
		
		for (JsonNode document : documents) {
			rv.add(JsonDiff.asJson(previous, document));
			previous = document;
		}
		
		return rv;
	}
	
	/**
	 * Given a sequence of differences, reconstitute the sequence of documents iteratively produced by applying each difference in
	 * the order given
	 * @param diffs Set of differences to use in reconstituting the sequence of JsonNode documents
	 * @return Sequence of JsonNode documents resulting from applying the sequence of differences
	 */
	public Collection<JsonNode> regenerateJsonSequence(Collection<JsonNode> diffs) {
		Collection<JsonNode> rv = new ArrayList<>();
		JsonNode previous = getMapper().createObjectNode();
		
		for (JsonNode diff : diffs) {
			try {
				previous = JsonPatch.fromJson(diff).apply(previous);
			} catch (JsonPatchException | IOException e) {
				e.printStackTrace();
				assertTrue(false);
			}
			rv.add(previous.deepCopy());
		}
		
		return rv;
	}
	
	/**
	 * Given a collection of JsonNode documents, perform a reverse marshaling of each to generate the POJO's corresponding 
	 * to the document instances 
	 * @param documents JsonNode documents to use to produce the sequence of POJO's
	 * @return Sequence of POJO's derived from the documents
	 */
	public Collection<AbstractDataType> regenerateObjectSequence(Collection<JsonNode> documents) {
		Collection<AbstractDataType> rv = new ArrayList<>();
		
		for (JsonNode document : documents) {
			try {
				rv.add(getMapper().treeToValue(document, AbstractDataType.class));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				assertTrue(false);
			}
		}
		return rv;
	}
	
	/**
	 * Compare two sequences of JsonNode documents and record the differences in the resulting collection
	 * @param sources Collection of source documents to compare 
	 * @param targets Collection of target documents to compare
	 * @return The sequence of differences resulting in comparing the source and target collection elements
	 */
	public Collection<JsonNode> compareJsonSequence(Collection<JsonNode> sources, Collection<JsonNode> targets) {
		Collection<JsonNode> rv = new ArrayList<>();
		
		Iterator<JsonNode> source = sources.iterator();
		Iterator<JsonNode> target = targets.iterator();
		
		while (target.hasNext() && source.hasNext()) {
			JsonNode s = null;
			JsonNode t = null;
			try {
				s = getMapper().readTree(source.next().toString());
				t = getMapper().readTree(target.next().toString());
			} catch (IOException e) {
				e.printStackTrace();
				assertTrue(false);
			}

			assertNotNull(s);
			assertNotNull(t);
			
			JsonNode d = JsonDiff.asJson(s, t);
			rv.add(d);
			
			if (0 != d.size()) {
				StringBuilder sb = new StringBuilder();
				sb.append("{\"source\":" + (null == s ? "null": s.toString()) + ",");
				sb.append("\"target\":" + (null == t ? "null" : t.toString()) + ",");
				sb.append("\"diff\":" + d.toString() + "}");
				assertEquals(0, d.size(), "Disconnect detected:\n" + sb.toString() + "\n");
			}
		}
		
		return rv;
	}
	
	/**
	 * Generate a time stamp values, with the probability given that some of the will be null values
	 * @param pNull Probability the resulting timestamp instance will be null valued
	 * @return A random timestamp value or null
	 */
	protected static Long genTimestamp(double pNull) {
		return Support.getRandom().nextDouble() < pNull ? null : Support.getRandom().nextLong();
	}
	
	/**
	 * Generate an ID value, with the probability given that some of the will be null values
	 * @param pNull Probability the resulting ID instance will be null valued
	 * @return A random ID value or null
	 */
	protected static UUID genId(double pNull) {
		return Support.getRandom().nextDouble() < pNull ? null : UUID.randomUUID();
	}
	
	/**
	 * Generate a collection of pathnames
	 * @param count Number of pathnames to generate
	 * @return Collection of pathnames generated
	 */
	protected static Collection<String> genPaths(long count) {
		Collection<String> rv = new ArrayList<>();
		
		for (long i=0; i<count; ++i) {
			rv.add(genPath());
		}
		
		return rv;
	}

	/**
	 * Generate a pathname
	 * @return A pathname
	 */
	protected static String genPath() {
		return ("/" + Support.getNouns(Support.getRandom().nextInt(8)+1, '/'));
	}
	
	/**
	 * Generate a collection of JsonNode values
	 * @param count Number of values to generate
	 * @param pNull Probability of a given value being null
	 * @param pBoolean Probability of a given value being a Boolean value 
	 * @param pLong Probability of a given value being a Long value
	 * @param pDouble Probability of a given value being a Double value
	 * @param pString Probability of a given value being a String value
	 * @return A collection of JsonNode values
	 */
	protected static Collection<JsonNode> genValues(long count, double pNull, double pBoolean, double pLong, double pDouble, double pString) {
		Collection<JsonNode> rv = new ArrayList<>();
		
		for (long i=0; i<count; ++i) {
			rv.add(genValue(pBoolean, pLong, pDouble, pString));
		}
		
		return rv;
	}

	/**
	 * Generate a JsonNode value
	 * @param pBoolean Probability of a given value being a Boolean value 
	 * @param pLong Probability of a given value being a Long value
	 * @param pDouble Probability of a given value being a Double value
	 * @param pString Probability of a given value being a String value
	 * @return Resulting JsonNode value
	 */
	private static JsonNode genValue(double pBoolean, double pLong, double pDouble, double pString) {
		double sum = pBoolean + pLong + pDouble + pString;
		double pick = Support.getRandom().nextDouble() * sum;
		if (pick <= pBoolean) {
			return mapper.valueToTree(Support.getRandom().nextBoolean());
		} else if (pick <= pBoolean + pLong) {
			return mapper.valueToTree(Support.getRandom().nextLong());
		} else if (pick <= pBoolean + pLong + pDouble) {
			Double value = Support.getRandom().nextDouble();
			value *= Support.getRandom().nextBoolean() ? -1 : 1;
			value = Support.getRandom().nextBoolean() ? 1 / value : value;
			return mapper.valueToTree(value);
		} else {
			return mapper.valueToTree(LoremIpsum.getInstance().getWords(Support.getRandom().nextInt(12)));
		}
	}
	
	/**
	 * Generate a of mock JsonPatch documents
	 * @param op Operation to include in the document 
	 * @param path Path of the change to include in the document
	 * @param value Value at the path of the document
	 * @return Resulting JsonNode representing the mock JsonPatch document
	 */
	protected static JsonNode generateOperation(String op, String path, JsonNode value) {
		if (!OPERATIONS.parallelStream().anyMatch((op.toLowerCase())::contains)) {
			return null;
		}
		
		ObjectNode node = getMapper().createObjectNode();
		node.put("op", op.toString().toLowerCase());
		node.put(PATH, path);
		
		if (!REMOVE.equalsIgnoreCase(op)) {
			node.set(VALUE, value);
		}
		
		return node;
	}
	
	/**
	 * Generate and return a JsonNode containing a JsonArray of mock JsonPatch documents
	 * @param count Number of operations to produce 
	 * @param pBoolean Probability of a given value being a Boolean value 
	 * @param pLong Probability of a given value being a Long value
	 * @param pDouble Probability of a given value being a Double value
	 * @param pString Probability of a given value being a String value
	 * @return A JsonNode containing a JsonArray of mock JsonPatch documents
	 */
	protected static JsonNode generateOperations(int count, double pBoolean, double pLong, double pDouble, double pString) {
		ArrayNode rv = getMapper().createArrayNode();
		
		for (int i = 0; i < count; ++i) {
			String op = OPERATIONS.get(Support.getRandom().nextInt(OPERATIONS.size()));
			String path = genPath();
			JsonNode value = genValue(pBoolean, pLong, pDouble, pString);
			rv.add(generateOperation(op, path, value));
		}

		return rv;
	}
	
	/**
	 * Generate and return a collection of JsonPatch instances as a collection of JsonNode documents
	 * @param instCount Number of instances to create in the resulting collection
	 * @param diffCount Number of operation elements in each difference element
	 * @param pBoolean Probability of a given value being a Boolean value 
	 * @param pLong Probability of a given value being a Long value
	 * @param pDouble Probability of a given value being a Double value
	 * @param pString Probability of a given value being a String value
	 * @return A collection of JsonPatch instances as a collection of JsonNode documents
	 */
	protected static Collection<JsonNode> generateOperations(int instCount, int diffCount, double pBoolean, double pLong, double pDouble, double pString) {
		Collection<JsonNode> rv = new ArrayList<>();
		
		for (int i = 0; i < instCount; ++i) {
			rv.add(generateOperations(diffCount, pBoolean, pLong, pDouble, pString));
		}

		return rv;
	}

	/**
	 * Generate a sequence of ID's, some of which are null with likelihood given by pNull
	 * @param count Number of ID's to generate
	 * @param pNull Probability a given ID will instead be null
	 * @return List of ID's generated
	 */
	protected static Collection<UUID> genIds(long count, double pNull) {
		Collection<UUID> rv = new ArrayList<>();
		
		for (long i=0; i<count; ++i) {
			rv.add(genId(pNull));
		}
		
		return rv;
	}
	
}
