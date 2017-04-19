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

// TODO: Auto-generated Javadoc
/**
 * The Class DataType.
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
	
	/** The Constant logger. */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AbstractDataType.class);
	
	/** The Constant mapper. */
	private static final ObjectMapper mapper = new ObjectMapper();

	/** The id. */
	private String id;
	
	/** The notes. */
	
	/** Notes associated with the data instance. */
	private String notes;
	
	/** The description. */
	private String description;
	
	/** The version. */
	private Long version;
	
	/**
	 * Instantiates a new data type.
	 */
	public AbstractDataType() {
		this.setDescription(LoremIpsum.getInstance().getWords(5, 10));
		this.setId(UUID.randomUUID().toString());
		this.setNotes(LoremIpsum.getInstance().getWords(5, 10));
		this.setVersion(0L);
	}
	
	/**
	 * Instantiates a new data type.
	 *
	 * @param src the src
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
	 * Sets the notes.
	 *
	 * @param notes the new notes
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
	 * Sets the description.
	 *
	 * @param description the new description
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
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public Long getVersion() {
		return version;
	}

	/**
	 * Increment version.
	 */
	protected void incrementVersion() {
		++this.version;
	}
	
	/**
	 * Sets the version.
	 *
	 * @param version the new version
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
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"id\":\"" + this.getId() + "\",");
		sb.append("\"type\":\"" + this.getClass().getName() + "\",");
		sb.append("\"description\":\"" + this.getDescription() + "\",");
		sb.append("\"notes\":\"" + this.getNotes() + "\",");
		sb.append("\"version\":" + this.getVersion() + ",");
		
		return sb.toString();
	}
	
	/**
	 * Gets the mapper.
	 *
	 * @return the mapper
	 */
	protected static ObjectMapper getMapper() {
		return mapper;
	}
	
	/**
	 * To json.
	 *
	 * @return the json node
	 */
	public JsonNode toJson() {
		return getMapper().valueToTree(this);
	}
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public abstract TYPE getType();
}
