package com.cyberfront.crdt.unittest.support;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestSupport {
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

	private long trialCount;
	private long stressedFactor;
	private long abbreviatedFactor;
	
	private boolean stressed;
	private boolean abbreviated;

	public TestSupport() {
		this.setAbbreviated(ABBREVIATED);
		this.setAbbreviatedFactor(ABBREVIATED_FACTOR);
		this.setStressed(STRESSED);
		this.setStressedFactor(STRESSED_FACTOR);
		this.setTrialCount(TRIAL_COUNT);
	}

	public TestSupport(long trialCount, long abbreviatedFactor, long stressedFactor, boolean abbreviated, boolean stressed) {
		this.setAbbreviated(abbreviated);
		this.setAbbreviatedFactor(abbreviatedFactor);
		this.setStressed(stressed);
		this.setStressedFactor(stressedFactor);
		this.setTrialCount(trialCount);
	}

	protected ObjectMapper getMapper() {
		return mapper;
	}

	/**
	 * Retrieve the number of trials to perform constrained by the ABBREVIATION_FACTOR if the ABREVIATION flag is true	
	 * @return The number of trials to perform
	 */
	public long getTrialCount() {
		return this.trialCount * this.getStressedFactor() / this.getAbbreviatedFactor();
	}
	
	public boolean isAbbreviated() {
		return abbreviated;
	}

	public long getAbbreviatedFactor() {
		return this.isAbbreviated() ? this.abbreviatedFactor : 1;
	}

	public boolean isStressed() {
		return stressed;
	}

	public long getStressedFactor() {
		return this.isStressed() ? this.stressedFactor : 1;
	}

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
