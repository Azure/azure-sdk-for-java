// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Base field type for helper classes to more easily create a {@link Index}.
 */
public abstract class FieldBase {
    private final ClientLogger logger = new ClientLogger(FieldBase.class);
    private final String name;
    private final DataType dataType;

    /**
     * Initializes a new instance of the {@link FieldBase} class.
     * @param name The name of the field, which must be unique within the index or parent field.
     * @param dataType The data type of the field.
     */
    protected FieldBase(String name, DataType dataType) {
        if (CoreUtils.isNullOrEmpty(name)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The name of the field cannot be null"));
        }
        this.dataType = Objects.requireNonNull(dataType, "'dataType' cannot be null.");
        this.name = name;
    }

    /**
     * Get the name of the field.
     *
     * @return The name of the field.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the {@link DataType} of the field.
     *
     * @return The data type of the field.
     */
    public DataType getDataType() {
        return dataType;
    }
}
