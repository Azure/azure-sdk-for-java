// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines enum values for CustomFormModelStatus.
 */
public final class CustomFormModelStatus extends ExpandableStringEnum<CustomFormModelStatus> {

    /**
     * Static value Creating for CustomFormModelStatus.
     */
    public static final CustomFormModelStatus CREATING = fromString("creating");

    /**
     * Static value ready for CustomFormModelStatus.
     */
    public static final CustomFormModelStatus READY = fromString("ready");

    /**
     * Static value invalid for CustomFormModelStatus.
     */
    public static final CustomFormModelStatus INVALID = fromString("invalid");

    /**
     * Parses a serialized value to a {@code CustomFormModelStatus} instance.
     *
     * @param value the serialized value to parse.
     *
     * @return the parsed CustomFormModelStatus object, or null if unable to parse.
     */
    public static CustomFormModelStatus fromString(String value) {
        return fromString(value, CustomFormModelStatus.class);
    }
}
