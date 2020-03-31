// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Defines values for FieldValueType.
 */
@Immutable
public final class FieldValueType extends ExpandableStringEnum<FieldValueType> {

    /**
     * Static value string for FieldValueType.
     */
    public static final FieldValueType STRING = fromString("string");

    /**
     * Static value date for FieldValueType.
     */
    public static final FieldValueType DATE = fromString("date");

    /**
     * Static value time for FieldValueType.
     */
    public static final FieldValueType TIME = fromString("time");

    /**
     * Static value phoneNumber for FieldValueType.
     */
    public static final FieldValueType PHONE_NUMBER = fromString("phoneNumber");

    /**
     * Static value number for FieldValueType.
     */
    public static final FieldValueType NUMBER = fromString("number");

    /**
     * Static value integer for FieldValueType.
     */
    public static final FieldValueType INTEGER = fromString("integer");

    /**
     * Static value array for FieldValueType.
     */
    public static final FieldValueType ARRAY = fromString("array");

    /**
     * Static value object for FieldValueType.
     */
    public static final FieldValueType OBJECT = fromString("object");

    /**
     * Parses a serialized value to a {@code FieldValueType} instance.
     *
     * @param value the serialized value to parse.
     *
     * @return the parsed FieldValueType object, or null if unable to parse.
     */
    @JsonCreator
    public static FieldValueType fromString(String value) {
        return fromString(value, FieldValueType.class);
    }

    /**
     * @return known {@link FieldValueType} values.
     */
    public static Collection<FieldValueType> values() {
        return values(FieldValueType.class);
    }
}
