// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Recognized field value.
 */
@Fluent
public final class FieldValue {
     private String value;
    private double valueNumber;
    private int valueInteger;
    // TODO: date values
    // TODO: private List<Element> elementList;

    /*
     * Text content of the extracted field.
     */
    @JsonProperty(value = "text")
    private String text;

    /*
     * Bounding box of the field value, if appropriate.
     */
    @JsonProperty(value = "boundingBox")
    private List<Float> boundingBox;

    /*
     * Confidence score.
     */
    @JsonProperty(value = "confidence")
    private Float confidence;

    /**
     * Get the valueString property: String value.
     *
     * @return the valueString value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the valueString property: String value.
     *
     * @param value the valueString value to set.
     * @return the FieldValue object itself.
     */
    public FieldValue setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Get the valueNumber property: Floating point value.
     *
     * @return the valueNumber value.
     */
    public double getValueNumber() {
        return this.valueNumber;
    }

    /**
     * Set the valueNumber property: Floating point value.
     *
     * @param valueNumber the valueNumber value to set.
     * @return the FieldValue object itself.
     */
    public FieldValue setValueNumber(double valueNumber) {
        this.valueNumber = valueNumber;
        return this;
    }

    /**
     * Get the valueInteger property: Integer value.
     *
     * @return the valueInteger value.
     */
    public int getValueInteger() {
        return this.valueInteger;
    }

    /**
     * Set the valueInteger property: Integer value.
     *
     * @param valueInteger the valueInteger value to set.
     * @return the FieldValue object itself.
     */
    public FieldValue setValueInteger(int valueInteger) {
        this.valueInteger = valueInteger;
        return this;
    }

    /**
     * Get the text property: Text content of the extracted field.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Set the text property: Text content of the extracted field.
     *
     * @param text the text value to set.
     * @return the FieldValue object itself.
     */
    public FieldValue setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the boundingBox property: Bounding box of the field value, if
     * appropriate.
     *
     * @return the boundingBox value.
     */
    public List<Float> getBoundingBox() {
        return this.boundingBox;
    }

    /**
     * Set the boundingBox property: Bounding box of the field value, if
     * appropriate.
     *
     * @param boundingBox the boundingBox value to set.
     * @return the FieldValue object itself.
     */
    public FieldValue setBoundingBox(List<Float> boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    /**
     * Get the confidence property: Confidence score.
     *
     * @return the confidence value.
     */
    public Float getConfidence() {
        return this.confidence;
    }

    /**
     * Set the confidence property: Confidence score.
     *
     * @param confidence the confidence value to set.
     * @return the FieldValue object itself.
     */
    public FieldValue setConfidence(Float confidence) {
        this.confidence = confidence;
        return this;
    }
}
