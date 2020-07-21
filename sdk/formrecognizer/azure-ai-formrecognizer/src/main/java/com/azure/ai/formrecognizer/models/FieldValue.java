// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.azure.ai.formrecognizer.models.FieldValueType.DATE;
import static com.azure.ai.formrecognizer.models.FieldValueType.DOUBLE;
import static com.azure.ai.formrecognizer.models.FieldValueType.LIST;
import static com.azure.ai.formrecognizer.models.FieldValueType.LONG;
import static com.azure.ai.formrecognizer.models.FieldValueType.MAP;
import static com.azure.ai.formrecognizer.models.FieldValueType.PHONE_NUMBER;
import static com.azure.ai.formrecognizer.models.FieldValueType.STRING;
import static com.azure.ai.formrecognizer.models.FieldValueType.TIME;

/**
 * The FieldValue model.
 */
@Fluent
public final class FieldValue {
    private final ClientLogger logger = new ClientLogger(FieldValue.class);
    private final FieldValueType type;
    private Map<String, FormField<?>> formFieldMap;
    private List<FormField<?>> formFieldList;
    private Double formFieldDouble;
    private Long formFieldLong;
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
    public FieldValue setFormFieldMap(final Map<String, FormField<?>> formFieldMap) {
        this.formFieldMap = formFieldMap == null ? null : Collections.unmodifiableMap(formFieldMap);
        return this;
    }

    /**
     * Set the list value of the field.
     *
     * @param formFieldList the list of the field.
     *
     * @return the FieldValue object itself.
     */
    public FieldValue setFormFieldList(final List<FormField<?>> formFieldList) {
        this.formFieldList = formFieldList == null ? null
            : Collections.unmodifiableList(formFieldList);
        return this;
    }

    /**
     * Set the float value of the field.
     *
     * @param formFieldDouble the float value of the field.
     *
     * @return the FieldValue object itself.
     */
    public FieldValue setFormFieldDouble(final Double formFieldDouble) {
        this.formFieldDouble = formFieldDouble;
        return this;
    }

    /**
     * Set the integer value of the field.
     *
     * @param formFieldLong the integer value of the field.
     *
     * @return the FieldValue object itself.
     */
    public FieldValue setFormFieldLong(final Long formFieldLong) {
        this.formFieldLong = formFieldLong;
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
        if (STRING != this.getType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as "
                + "%s from field value of type %s", STRING, this.getType()))));
        }
        return this.formFieldString;
    }

    /**
     * Gets the value of the field as a {@link Integer}.
     *
     * @return the value of the field as a {@link Integer}.
     */
    public Long asLong() {
        if (LONG != this.getType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as "
                + "%s from field value of type %s", LONG, this.getType()))));
        }
        return this.formFieldLong;
    }

    /**
     * Gets the value of the field as a {@link Double}.
     *
     * @return the value of the field as a {@link Double}.
     */
    public Double asDouble() {
        if (DOUBLE != this.getType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as "
                + "%s from field value of type %s", DOUBLE, this.getType()))));
        }
        return this.formFieldDouble;
    }

    /**
     * Gets the value of the field as a {@link LocalDate}.
     *
     * @return the value of the field as a {@link LocalDate}.
     */
    public LocalDate asDate() {
        if (DATE != this.getType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as "
                + "%s from field value of type %s", DATE, this.getType()))));
        }
        return this.formFieldDate;
    }

    /**
     * Gets the value of the field as a {@link LocalTime}.
     *
     * @return the value of the field as a {@link LocalTime}.
     */
    public LocalTime asTime() {
        if (TIME != this.getType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as "
                + "%s from field value of type %s", TIME, this.getType()))));
        }
        return this.formFieldTime;
    }

    /**
     * Gets the value of the field as a phone number.
     *
     * @return the value of the field as a phone number.
     */
    public String asPhoneNumber() {
        if (PHONE_NUMBER != this.getType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as a"
                + "%s from field value of type %s", PHONE_NUMBER, this.getType()))));
        }
        return this.formFieldPhoneNumber;
    }

    /**
     * Gets the value of the field as a {@link List}.
     *
     * @return the value of the field as an unmodifiable {@link List}.
     */
    public List<FormField<?>> asList() {
        if (LIST != this.getType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as a "
                + "%s from field value of type %s", LIST, this.getType()))));
        }
        return this.formFieldList;
    }

    /**
     * Gets the value of the field as a {@link Map}.
     *
     * @return the value of the field as an unmodifiable {@link Map}.
     */
    public Map<String, FormField<?>> asMap() {
        if (MAP != this.getType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as a "
                + "%s from field value of type %s", MAP, this.getType()))));
        }
        return this.formFieldMap;
    }
}
