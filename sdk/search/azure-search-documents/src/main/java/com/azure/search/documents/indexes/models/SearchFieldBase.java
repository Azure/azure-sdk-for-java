// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Base field type for helper classes to more easily create a {@link SearchIndex}.
 */
public abstract class SearchFieldBase {
    private final ClientLogger logger = new ClientLogger(SearchFieldBase.class);
    private final String name;
    private final SearchFieldDataType dataType;

    /**
     * Initializes a new instance of the {@link SearchFieldBase} class.
     * @param name The name of the field, which must be unique within the index or parent field.
     * @param dataType The data type of the field.
     */
    protected SearchFieldBase(String name, SearchFieldDataType dataType) {
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
     * Get the {@link SearchFieldDataType} of the field.
     *
     * @return The data type of the field.
     */
    public SearchFieldDataType getDataType() {
        return dataType;
    }
}
