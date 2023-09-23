// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Truncates the terms to a specific length. This token filter is implemented
 * using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.TruncateTokenFilter")
@Fluent
public final class TruncateTokenFilter extends TokenFilter {
    /*
     * The length at which terms will be truncated. Default and maximum is 300.
     */
    @JsonProperty(value = "length")
    private Integer length;

    /**
     * Constructor of {@link TruncateTokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public TruncateTokenFilter(String name) {
        super(name);
    }

    /**
     * Get the length property: The length at which terms will be truncated.
     * Default and maximum is 300.
     *
     * @return the length value.
     */
    public Integer getLength() {
        return this.length;
    }

    /**
     * Set the length property: The length at which terms will be truncated.
     * Default and maximum is 300.
     *
     * @param length the length value to set.
     * @return the TruncateTokenFilter object itself.
     */
    public TruncateTokenFilter setLength(Integer length) {
        this.length = length;
        return this;
    }
}
