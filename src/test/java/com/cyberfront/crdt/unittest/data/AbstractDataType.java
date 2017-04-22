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

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.unittest.data.Factory.TYPE;
import com.cyberfront.crdt.unittest.support.WordFactory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thedeanda.lorem.LoremIpsum;

/**
 * This is the abstract base class for the test data elements which are coded as JSON objects in the CRDT 
 * elements under test
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @Type(value = SimpleA.class, name = "SimpleA"),
    @Type(value = SimpleB.class, name = "SimpleB"),
    @Type(value = SimpleC.class, name = "SimpleC"),
    @Type(value = SimpleD.class, name = "SimpleD"),
    @Type(value = SimpleCollection.class, name = "SimpleCollection"),
    @Type(value = SimpleReference.class, name = "SimpleReference") })

public abstract class AbstractDataType {
	
	/** Logger to use when displaying state information */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AbstractDataType.class);
	
	/** The ObjectMapper used to translate between JSON and any of the classes derived from
	 * com.cyberfront.crdt.unittest.data.AbstractDataType */
	private static final ObjectMapper mapper = new ObjectMapper();

	/** A unique identifier for the object */
	private String id;
	
	/** Notes associated with the data instance. */
	private String notes;
	
	/** A description of the objec. */
	private String description;
	
	/** The version, relating to the number of times the object was revised */
	private Long version;
	
	/**
	 * Create a new object, setting random values to most of the fields, though
	 * the version is set initially to 0 since it hasn't been changed.
	 */
	public AbstractDataType() {
		this.setDescription(LoremIpsum.getInstance().getWords(5, 10));
		this.setId(UUID.randomUUID().toString());
		this.setNotes(LoremIpsum.getInstance().getWords(5, 10));
		this.setVersion(0L);
	}
	
	/**
	 * Instantiates a new AbstractDataType by copying a source instance 
	 *
	 * @param src The course AbstractDataType to copy into this instance
	 */
	protected AbstractDataType(AbstractDataType src) {
		this.description = src.description;
		this.id = src.id;
		this.notes = src.notes;
		this.version = src.version;
	}
	
	/**
	 * Gets the notes.
	 *
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Sets the notes to the given value.
	 *
	 * @param notes The new notes
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description to the given value.
	 *
	 * @param description The new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id to the given value.
	 *
	 * @param id The new id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Gets the version number.
	 *
	 * @return the version number
	 */
	public Long getVersion() {
		return version;
	}

	/**
	 * Increments the version counter to the next element; this should be
	 * called whenever the object is updated.
	 */
	protected void incrementVersion() {
		this.setVersion(this.getVersion() + 1);
	}
	
	/**
	 * Sets the version number to the given value.
	 *
	 * @param version The new version
	 */
	public void setVersion(Long version) {
		this.version = version;
	}

	/**
	 * Update the instance such that each field is changed with probability given by prob
	 * 
	 * @param prob Probability an individual field will be changed
	 */
	public void update(Double prob) {
		if (WordFactory.getRandom().nextDouble() < prob) {
			this.setDescription(LoremIpsum.getInstance().getWords(5, 10));
			this.incrementVersion();
		}

		if (WordFactory.getRandom().nextDouble() < prob) {
			this.setNotes(LoremIpsum.getInstance().getWords(5, 10));
			this.incrementVersion();
		}
	}

	/**
	 * Gets the object mapper.
	 *
	 * @return the object mapper
	 */
	protected static ObjectMapper getMapper() {
		return mapper;
	}
	
	/**
	 * Converts this object to it equivalent JSON representation
	 *
	 * @return The JSON representation of this object
	 */
	public JsonNode toJson() {
		return getMapper().valueToTree(this);
	}
	
	/**
	 * Gets the type of this object
	 *
	 * @return The type of this object
	 */
	public abstract TYPE getType();

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (null == other || !(other instanceof AbstractDataType)) {
			return false;
		} else {
			AbstractDataType castOther = (AbstractDataType) other;

			boolean descriptionDiff = this.getDescription().equals(castOther.getDescription());
			boolean idDiff = this.getId().equals(castOther.getId());
			boolean notesDiff = this.getNotes().equals(castOther.getNotes());
			boolean versionDiff = this.getVersion() == castOther.getVersion();

			return descriptionDiff && idDiff && notesDiff && versionDiff;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 1;
		
		hash = hash * 17 + this.getDescription().hashCode();
		hash = hash * 19 + this.getId().hashCode();
		hash = hash * 23 + this.getNotes().hashCode();
		hash = hash * 29 + this.getVersion().hashCode();

		return hash;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return mapper.valueToTree(this).asText();
	}
}
