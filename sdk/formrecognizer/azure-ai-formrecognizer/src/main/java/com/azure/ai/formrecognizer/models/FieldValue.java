// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.azure.ai.formrecognizer.models.FieldValueType.COUNTRY_REGION;
import static com.azure.ai.formrecognizer.models.FieldValueType.DATE;
import static com.azure.ai.formrecognizer.models.FieldValueType.FLOAT;
import static com.azure.ai.formrecognizer.models.FieldValueType.LIST;
import static com.azure.ai.formrecognizer.models.FieldValueType.LONG;
import static com.azure.ai.formrecognizer.models.FieldValueType.MAP;
import static com.azure.ai.formrecognizer.models.FieldValueType.PHONE_NUMBER;
import static com.azure.ai.formrecognizer.models.FieldValueType.SELECTION_MARK_STATE;
import static com.azure.ai.formrecognizer.models.FieldValueType.STRING;
import static com.azure.ai.formrecognizer.models.FieldValueType.TIME;

/**
 * Represents the strongly-typed value of a field recognized from the input document and provides
 * methods for converting it to the appropriate type.
 */
@Fluent
public final class FieldValue {
    private final ClientLogger logger = new ClientLogger(FieldValue.class);
    private final FieldValueType valueType;
    private Map<String, FormField> formFieldMap;
    private List<FormField> formFieldList;
    private Float formFieldFloat;
    private Long formFieldLong;
    private LocalDate formFieldDate;
    private LocalTime formFieldTime;
    private SelectionMarkState selectionMarkState;
    private String formFieldString;
    private String formFieldPhoneNumber;
    private String formFieldCountryRegion;

    /**
     * Constructs a FieldValue object
     *
     * @param value The actual value of the field.
     * @param valueType The type of the field.
     */
    @SuppressWarnings("unchecked")
    public FieldValue(final Object value, final FieldValueType valueType) {
        this.valueType = valueType;
        switch (valueType) {
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
            case FLOAT:
                formFieldFloat = (Float) value;
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
            case SELECTION_MARK_STATE:
                selectionMarkState = (SelectionMarkState) value;
                break;
            case COUNTRY_REGION:
                formFieldCountryRegion = (String) value;
                break;
            default:
                throw logger.logExceptionAsError(new IllegalStateException("Unexpected type value: " + valueType));
        }
    }

    /**
     * Gets the type of the value of the field.
     *
     * @return the {@link FieldValueType type} of the field.
     */
    public FieldValueType getValueType() {
        return valueType;
    }

    /**
     * Gets the value of the field as a {@link String}.
     *
     * @return the value of the field as a {@link String}.
     * @throws UnsupportedOperationException if {@link FieldValue#getValueType()} is not {@link FieldValueType#STRING}.
     */
    public String asString() {
        if (STRING != this.getValueType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as "
                + "%s from field value of type %s", STRING, this.getValueType()))));
        }
        return this.formFieldString;
    }

    /**
     * Gets the value of the field as a {@link Long}.
     *
     * @return the value of the field as a {@link Long}.
     * @throws UnsupportedOperationException if {@link FieldValue#getValueType()} is not {@link FieldValueType#LONG}.
     */
    public Long asLong() {
        if (LONG != this.getValueType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as "
                + "%s from field value of type %s", LONG, this.getValueType()))));
        }
        return this.formFieldLong;
    }

    /**
     * Gets the value of the field as a {@link Float}.
     *
     * @return the value of the field as a {@link Float}.
     * @throws UnsupportedOperationException if {@link FieldValue#getValueType()} is not {@link FieldValueType#FLOAT}.
     */
    public Float asFloat() {
        if (FLOAT != this.getValueType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as "
                + "%s from field value of type %s", FLOAT, this.getValueType()))));
        }
        return this.formFieldFloat;
    }

    /**
     * Gets the value of the field as a {@link LocalDate}.
     *
     * @return the value of the field as a {@link LocalDate}.
     * @throws UnsupportedOperationException if {@link FieldValue#getValueType()} is not {@link FieldValueType#DATE}.
     */
    public LocalDate asDate() {
        if (DATE != this.getValueType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as "
                + "%s from field value of type %s", DATE, this.getValueType()))));
        }
        return this.formFieldDate;
    }

    /**
     * Gets the value of the field as a {@link LocalTime}.
     *
     * @return the value of the field as a {@link LocalTime}.
     * @throws UnsupportedOperationException if {@link FieldValue#getValueType()} is not {@link FieldValueType#TIME}.
     */
    public LocalTime asTime() {
        if (TIME != this.getValueType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as "
                + "%s from field value of type %s", TIME, this.getValueType()))));
        }
        return this.formFieldTime;
    }

    /**
     * Gets the value of the field as a phone number.
     *
     * @return the value of the field as a phone number.
     * @throws UnsupportedOperationException if {@link FieldValue#getValueType()} is not
     * {@link FieldValueType#PHONE_NUMBER}.
     */
    public String asPhoneNumber() {
        if (PHONE_NUMBER != this.getValueType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as a"
                + "%s from field value of type %s", PHONE_NUMBER, this.getValueType()))));
        }
        return this.formFieldPhoneNumber;
    }

    /**
     * Gets the value of the field as a {@link List}.
     *
     * @return the value of the field as an unmodifiable {@link List}.
     * @throws UnsupportedOperationException if {@link FieldValue#getValueType()} is not {@link FieldValueType#LIST}.
     */
    public List<FormField> asList() {
        if (LIST != this.getValueType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as a "
                + "%s from field value of type %s", LIST, this.getValueType()))));
        }
        return this.formFieldList;
    }

    /**
     * Gets the value of the field as a {@link Map}.
     *
     * @return the value of the field as an unmodifiable {@link Map}.
     * @throws UnsupportedOperationException if {@link FieldValue#getValueType()} is not {@link FieldValueType#MAP}.
     */
    public Map<String, FormField> asMap() {
        if (MAP != this.getValueType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format("Cannot get field as a "
                + "%s from field value of type %s", MAP, this.getValueType()))));
        }
        return this.formFieldMap;
    }

    /**
     * Gets the value of the field as a selection mark state.
     *
     * @return the value of the field as an unmodifiable selection mark state.
     * @throws UnsupportedOperationException if {@link FieldValue#getValueType()} is not
     * {@link FieldValueType#SELECTION_MARK_STATE}.
     */
    public SelectionMarkState asSelectionMarkState() {
        if (SELECTION_MARK_STATE != this.getValueType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format(
                "Cannot get field as a %s from field value of type %s", SELECTION_MARK_STATE, this.getValueType()))));
        }
        return this.selectionMarkState;
    }

    /**
     * Gets the value of the field as a country or region in the world.
     *
     * @return the value of the field as COUNTRY_REGION.
     * @throws UnsupportedOperationException if {@link FieldValue#getValueType()} is not
     * {@link FieldValueType#COUNTRY_REGION}.
     */
    public String asCountryRegion() {
        if (COUNTRY_REGION != this.getValueType()) {
            throw logger.logExceptionAsError((new UnsupportedOperationException(String.format(
                "Cannot get field as a %s from field value of type %s", COUNTRY_REGION, this.getValueType()))));
        }
        return this.formFieldCountryRegion;
    }
}
