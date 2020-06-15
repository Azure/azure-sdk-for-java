// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * The FieldValue model.
 */
@Fluent
public final class FieldValue {
    private final FieldValueType type;
    private Map<String, FormField> formFieldMap;
    private List<FormField> formFieldList;
    private Float formFieldFloat;
    private Integer formFieldInteger;
    private LocalDate formFieldDate;
    private LocalTime formFieldTime;
    private String formFieldString;
    private String formFieldPhoneNumber;

    /**
     * Constructs a FieldValue object
     *
     * @param type The type of the field.
     */
    public FieldValue(final FieldValueType type) {
        this.type = type;
    }

    /**
     * Set the map value of the field.
     *
     * @param formFieldMap the map value of the field.
     *
     * @return the FieldValue object itself.
     */
    public FieldValue setFormFieldMap(final Map<String, FormField> formFieldMap) {
        this.formFieldMap = formFieldMap;
        return this;
    }

    /**
     * Set the list value of the field.
     *
     * @param formFieldList the list of the field.
     *
     * @return the FieldValue object itself.
     */
    public FieldValue setFormFieldList(final List<FormField> formFieldList) {
        this.formFieldList = formFieldList;
        return this;
    }

    /**
     * Set the float value of the field.
     *
     * @param formFieldFloat the float value of the field.
     *
     * @return the FieldValue object itself.
     */
    public FieldValue setFormFieldFloat(final Float formFieldFloat) {
        this.formFieldFloat = formFieldFloat;
        return this;
    }

    /**
     * Set the integer value of the field.
     *
     * @param formFieldInteger the integer value of the field.
     *
     * @return the FieldValue object itself.
     */
    public FieldValue setFormFieldInteger(final Integer formFieldInteger) {
        this.formFieldInteger = formFieldInteger;
        return this;
    }

    /**
     * Set the date value of the field.
     *
     * @param formFieldDate the date value of the field.
     *
     * @return the FieldValue object itself.
     */
    public FieldValue setFormFieldDate(final LocalDate formFieldDate) {
        this.formFieldDate = formFieldDate;
        return this;
    }

    /**
     * Set the time value of the field.
     *
     * @param formFieldTime the time value of the field.
     *
     * @return the FieldValue object itself.
     */
    public FieldValue setFormFieldTime(final LocalTime formFieldTime) {
        this.formFieldTime = formFieldTime;
        return this;
    }

    /**
     * Set the string value of the field.
     *
     * @param formFieldString the string value of the field.
     *
     * @return the FieldValue object itself.
     */
    public FieldValue setFormFieldString(final String formFieldString) {
        this.formFieldString = formFieldString;
        return this;
    }

    /**
     * Set the phone number value of the field.
     *
     * @param formFieldPhoneNumber the phone number value of the field.
     *
     * @return the FieldValue object itself.
     */
    public FieldValue setFormFieldPhoneNumber(final String formFieldPhoneNumber) {
        this.formFieldPhoneNumber = formFieldPhoneNumber;
        return this;
    }

    /**
     * Gets the type of the value of the field.
     *
     * @return the {@link FieldValueType type} of the field.
     */
    public FieldValueType getType() {
        return type;
    }

    /**
     * Gets the value of the field as a {@link String}.
     *
     * @return the value of the field as a {@link String}.
     */
    public String asString() {
        return this.formFieldString;
    }

    /**
     * Gets the value of the field as a {@link Integer}.
     *
     * @return the value of the field as a {@link Integer}.
     */
    public Integer asInteger() {
        return this.formFieldInteger;
    }

    /**
     * Gets the value of the field as a {@link Float}.
     *
     * @return the value of the field as a {@link Float}.
     */
    public Float asFloat() {
        return this.formFieldFloat;
    }

    /**
     * Gets the value of the field as a {@link LocalDate}.
     *
     * @return the value of the field as a {@link LocalDate}.
     */
    public LocalDate asDate() {
        return this.formFieldDate;
    }

    /**
     * Gets the value of the field as a {@link LocalTime}.
     *
     * @return the value of the field as a {@link LocalTime}.
     */
    public LocalTime asTime() {
        return this.formFieldTime;
    }

    /**
     * Gets the value of the field as a phone number.
     *
     * @return the value of the field as a phone number.
     */
    public String asPhoneNumber() {
        return this.formFieldPhoneNumber;
    }

    /**
     * Gets the value of the field as a {@link List}.
     *
     * @return the value of the field as a {@link List}.
     */
    public List<FormField> asList() {
        return this.formFieldList;
    }

    /**
     * Gets the value of the field as a {@link Map}.
     *
     * @return the value of the field as a {@link Map}.
     */
    public Map<String, FormField> asMap() {
        return this.formFieldMap;
    }
}
