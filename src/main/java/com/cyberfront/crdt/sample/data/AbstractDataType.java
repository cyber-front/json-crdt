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
package com.cyberfront.crdt.sample.data;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.sample.data.Factory.DataType;
import com.cyberfront.crdt.support.Support;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @Type(value = SimpleString.class, name = "SimpleString"),
    @Type(value = SimpleInteger.class, name = "SimpleInteger"),
    @Type(value = SimpleDouble.class, name = "SimpleDouble"),
    @Type(value = SimpleBoolean.class, name = "SimpleBoolean"),
    @Type(value = SimpleCollection.class, name = "SimpleCollection"),
    @Type(value = SimpleReference.class, name = "SimpleReference") })

public abstract class AbstractDataType {
	/** JSON property name for the id stored in any AbstractDataType instances */
	protected final static String ID = "id";

	/** JSON property name for the version stored in any AbstractDataType instances */
	protected final static String VERSION = "version";

	/** JSON property name for the notes stored in any AbstractDataType instances */
	protected final static String NOTES = "notes";

	/** JSON property name for the description stored in any AbstractDataType instances */
	protected final static String DESCRIPTION = "description";
	
	/** Logger to use when displaying state information */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AbstractDataType.class);
	
	/** The ObjectMapper used to translate between JSON and any of the classes derived from
	 * com.cyberfront.crdt.unittest.data.AbstractDataType */
	private static final ObjectMapper mapper = new ObjectMapper();

	/** A unique identifier for the object */
	@JsonProperty(ID)
	private final UUID id;
	
	/** Notes associated with the data instance. */
	@JsonProperty(NOTES)
	private final String notes;
	
	/** A description of the object. */
	@JsonProperty(DESCRIPTION)
	private final String description;
	
	/** The version, relating to the number of times the object was revised */
	@JsonProperty(VERSION)
	private final Long version;
	
	/**
	 * Create a new object, setting random values to most of the fields, though
	 * the version is set initially to 0 since it hasn't been changed.
	 */
	public AbstractDataType() {
		this(UUID.randomUUID(), 0L, LoremIpsum.getInstance().getWords(5, 10));
	}
	
	/**
	 * Instantiates a new AbstractDataType by copying a source instance 
	 *
	 * @param src The course AbstractDataType to copy into this instance
	 */
	public AbstractDataType(AbstractDataType src) {
		this(src.id, src.version, src.notes);
	}

	/**
	 * Instantiates a new AbstractDataType by copying a source instance 
	 *
	 * @param src The course AbstractDataType to copy into this instance
	 * @param pChange Probability of changing either of the strings
	 */
	public AbstractDataType(AbstractDataType src, double pChange) {
		this.id = src.id;
		this.version = src.version + 1;
		this.notes = Support.getRandom().nextDouble() < pChange  ? LoremIpsum.getInstance().getWords(5, 10) : src.notes;
		this.description = this.getClass().toString();
	}

	/**
	 * Constructor for specifying each of the elements of the AbstractDataType
	 * 
	 * @param id Identifier for the AbstractDataType instance
	 * @param version Version for the AbstractDataType instance
	 * @param notes Notes associated with the AbstractDataType instance
	 */
	public AbstractDataType(UUID id, Long version, String notes) {
		this.id = id;
		this.version = version;
		this.notes = notes;
		this.description = this.getClass().toString();
	}
	
	/**
	 * Gets the notes.
	 *
	 * @return the notes
	 */
	@JsonProperty(NOTES)
	public String getNotes() {
		return notes;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	@JsonProperty(DESCRIPTION)
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@JsonProperty(ID)
	public UUID getId() {
		return id;
	}
	
	/**
	 * Gets the version number.
	 *
	 * @return the version number
	 */
	@JsonProperty(VERSION)
	public Long getVersion() {
		return version;
	}
	
	/**
	 * Update the instance such that each field is changed with probability given by probability pChange
	 * 
	 * @param pChange Probability an individual field will be changed
	 * @return Updated copy of this instance with individual fields updated based on the given probability pChange
	 */
	public abstract AbstractDataType copy(Double pChange);

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
	@JsonIgnore
	public abstract DataType getType();
	

	/**
	 * Create a copy of this AbstractDataType derived object
	 * @return a copy of this AbstractDataType derived object
	 */
	public abstract AbstractDataType copy();

	/**
	 * Generate a string representation of this AbstractDataType
	 * @return String representation of this AbstractDataType
	 */
	@JsonIgnore
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"id\":\"" + this.getId() + "\",");
		sb.append("\"objectClass\":\"" + this.getClass().getName() + "\",");
		sb.append("\"version\":\"" + this.getVersion() + "\",");
		sb.append("\"type\":\"" + this.getType() + "\",");
		sb.append("\"description\":\"" + this.getDescription() + "\",");
		sb.append("\"notes\":\"" + this.getNotes() + "\"");
		
		return sb.toString();
	}

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
		return "{" + this.getSegment() + "}";
	}
}
