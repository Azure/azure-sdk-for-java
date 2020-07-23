// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.time.LocalDate;
import java.time.LocalTime;
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
    private Map<String, FormField> formFieldMap;
    private List<FormField> formFieldList;
    private Double formFieldDouble;
    private Long formFieldLong;
    private LocalDate formFieldDate;
    private LocalTime formFieldTime;
    private String formFieldString;
    private String formFieldPhoneNumber;

    /**
     * Constructs a FieldValue object
     *
     * @param value The actual value of the field.
     * @param type The type of the field.
     */
    @SuppressWarnings("unchecked")
    public FieldValue(final Object value, final FieldValueType type) {
        this.type = type;
        switch (type) {
            case STRING:
                formFieldString = (String) value;
                break;
            case DATE:
                formFieldDate = (LocalDate) value;
                break;
            case TIME:
                formFieldTime = (LocalTime) value;
                break;
            case PHONE_NUMBER:
                formFieldPhoneNumber = (String) value;
                break;
            case DOUBLE:
                formFieldDouble = (Double) value;
                break;
            case LONG:
                formFieldLong = (Long) value;
                break;
            case LIST:
                formFieldList = (List<FormField>) value;
                break;
            case MAP:
                formFieldMap = (Map<String, FormField>) value;
                break;
            default:
                throw logger.logExceptionAsError(new IllegalStateException("Unexpected type value: " + type));
        }
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
     * @throws UnsupportedOperationException if {@link FieldValue#getType()} is not {@link FieldValueType#STRING}.
     */
    public String asString() {
        if (STRING != this.getType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as "
                + "%s from field value of type %s", STRING, this.getType()))));
        }
        return this.formFieldString;
    }

    /**
     * Gets the value of the field as a {@link Long}.
     *
     * @return the value of the field as a {@link Long}.
     * @throws UnsupportedOperationException if {@link FieldValue#getType()} is not {@link FieldValueType#LONG}.
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
     * @throws UnsupportedOperationException if {@link FieldValue#getType()} is not {@link FieldValueType#DOUBLE}.
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
     * @throws UnsupportedOperationException if {@link FieldValue#getType()} is not {@link FieldValueType#DATE}.
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
     * @throws UnsupportedOperationException if {@link FieldValue#getType()} is not {@link FieldValueType#TIME}.
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
     * @throws UnsupportedOperationException if {@link FieldValue#getType()} is not {@link FieldValueType#PHONE_NUMBER}.
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
     * @throws UnsupportedOperationException if {@link FieldValue#getType()} is not {@link FieldValueType#LIST}.
     */
    public List<FormField> asList() {
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
     * @throws UnsupportedOperationException if {@link FieldValue#getType()} is not {@link FieldValueType#MAP}.
     */
    public Map<String, FormField> asMap() {
        if (MAP != this.getType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as a "
                + "%s from field value of type %s", MAP, this.getType()))));
        }
        return this.formFieldMap;
    }
}
