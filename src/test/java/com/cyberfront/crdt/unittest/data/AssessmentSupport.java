package com.cyberfront.crdt.unittest.data;

import com.cyberfront.crdt.unittest.data.Test01Create.CreateTest;
import com.cyberfront.crdt.unittest.data.Test02Update.UpdateTest;
import com.cyberfront.crdt.unittest.data.Test03Clone.CloneTest;
import com.cyberfront.crdt.unittest.simulator.Test01Simulation.SimulationTest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	/** Constant defining the number of AbstractDataType elements to create in the unit test */
	private static final long TRIAL_COUNT = 128L;

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
	private ObjectMapper mapper = new ObjectMapper();

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
	protected ObjectMapper getMapper() {
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

	public void setStressedFactor(long stressedFactor) {
		this.stressedFactor = stressedFactor;
	}

	public void setAbbreviatedFactor(long abbreviatedFactor) {
		this.abbreviatedFactor = abbreviatedFactor;
	}

	public void setStressed(boolean stressed) {
		this.stressed = stressed;
	}

	public void setAbbreviated(boolean abbreviated) {
		this.abbreviated = abbreviated;
	}
	
	public static String getMessage() {
		StringBuilder sb = new StringBuilder();
		
		return sb.toString();
	}

	/**
	 * Return the flag factor to apply given a flag and factor
	 * @param flag Flag to determine when to apply the factor
	 * @param factor Factor to return when the flag is set to true
	 * @return factor when flag is set, otherwise 1
	 */
//	protected static long getFactor(boolean flag, long factor) {
//		return flag ? factor : 1;
//	}

	/**
	 * Retrieve the abbreviation factor determined by the constants
	 * @return Abbreviation factor defined by the constants
	 */
//	protected static long getAbbreviationFactor() {
//		return getFactor(ABBREVIATED, ABBREVIATED_FACTOR);
//	}
	
	/**
	 * Retrieve the stress factor determined by the constants
	 * @return Stress factor defined by the constants
	 */
//	protected static long getStressFactor() {
//		return getFactor(STRESSED, STRESSED_FACTOR);
//	}
	
	/** 
	 * Retrieve the aggregate stress and 
	 * @param baseValue Base value to use for applying the stress and abbreviation factor
	 * @return value to 
	 */
//	protected static long getFactor(long baseValue) {
//		return baseValue * getStressFactor() / getAbbreviationFactor();
//	}

	/**
	 * Retrieve a number based on the given base value and divided by the factor if and only if abbreviate is true
	 * @param baseValue Base value which is to be adjusted as appropriate by the abbreviation factor 
	 * @param abbreviate Flag to determine whether to abbreviate the trial sequence
	 * @param factor Abbreviation factor to apply if needed
	 * @return Factor adjusted by abbreviation factor if abbreviated
	 */
//	protected static long applyFactor(long baseValue, boolean abbreviate, long factor) {
//		return baseValue / getFactor(abbreviate, factor);
//	}

	/**
	 * Retrieve a number based on the given base value and divided by the factor if and only if abbreviate is true
	 * @param baseValue Base value which is to be adjusted as appropriate by the abbreviation factor 
	 * @return Factor adjusted by abbreviation factor if abbreviated
	 */
//	protected static long applyFactor(long baseValue) {
//		return baseValue / getAbbreviationFactor();
//	}

	/**
	 * Retrieve trial count given adjusted by an abbreviation factor exactly when abbreviate is true 
	 * @param trialCount Base trial count which is to be adjusted as appropriate by the abbreviation factor 
	 * @param abbreviate Flag to determine whether to abbreviate the trial sequence
	 * @param factor Abbreviation factor to apply if needed
	 * @return Trial count adjusted by abbreviation factor if abbreviated
	 */
//	protected static long getTrialCount(long trialCount, boolean abbreviate, long factor) {
//		return applyFactor(trialCount, abbreviate, factor);
//	}
}
