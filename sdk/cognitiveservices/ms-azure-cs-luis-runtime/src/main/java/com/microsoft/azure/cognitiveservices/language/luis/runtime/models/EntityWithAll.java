package com.microsoft.azure.cognitiveservices.language.luis.runtime.models;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The EntityWithAll model.
 */
public class EntityWithAll extends EntityModel {

	/**
	 * Associated prediction score for the intent (float).
	 */
	@JsonProperty(value = "score", required = true)
	private double score;

	/**
	 * Get the score value.
	 *
	 * @return the score value
	 */
	public double score() {
		return this.score;
	}

	/**
	 * Resolution values for pre-built LUIS entities.
	 */
	@JsonProperty(value = "resolution", required = true)
	private Object resolution;

	/**
	 * Get the resolution value.
	 *
	 * @return the resolution value
	 */
	public Object resolution() {
		return this.resolution;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
