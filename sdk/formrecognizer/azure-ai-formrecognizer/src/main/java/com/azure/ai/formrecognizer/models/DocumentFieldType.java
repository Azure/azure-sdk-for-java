// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DocumentFieldType. */
public final class DocumentFieldType extends ExpandableStringEnum<DocumentFieldType> {
    /** Static value string for DocumentFieldType. */
    public static final DocumentFieldType STRING = fromString("string");

    /** Static value date for DocumentFieldType. */
    public static final DocumentFieldType DATE = fromString("date");

    /** Static value time for DocumentFieldType. */
    public static final DocumentFieldType TIME = fromString("time");

    /** Static value phoneNumber for DocumentFieldType. */
    public static final DocumentFieldType PHONE_NUMBER = fromString("phoneNumber");

    /** Static value number for DocumentFieldType. */
    public static final DocumentFieldType FLOAT = fromString("number");

    /** Static value integer for DocumentFieldType. */
    public static final DocumentFieldType INTEGER = fromString("integer");

    /** Static value selectionMark for DocumentFieldType. */
    public static final DocumentFieldType SELECTION_MARK = fromString("selectionMark");

    /** Static value countryRegion for DocumentFieldType. */
    public static final DocumentFieldType COUNTRY_REGION = fromString("countryRegion");

    /** Static value signature for DocumentFieldType. */
    public static final DocumentFieldType SIGNATURE = fromString("signature");

    /** Static value array for DocumentFieldType. */
    public static final DocumentFieldType LIST = fromString("array");

    /** Static value object for DocumentFieldType. */
    public static final DocumentFieldType MAP = fromString("object");

    /**
     * Creates or finds a DocumentFieldType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentFieldType.
     */
    public static DocumentFieldType fromString(String name) {
        return fromString(name, DocumentFieldType.class);
    }

    /** @return known DocumentFieldType values. */
    public static Collection<DocumentFieldType> values() {
        return values(DocumentFieldType.class);
    }
}
