// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;

/**
 * Removes words that are too long or too short. This token filter is
 * implemented using Apache Lucene.
 */
@Fluent
public final class LengthTokenFilter extends TokenFilter {
    /*
     * The minimum length in characters. Default is 0. Maximum is 300. Must be
     * less than the value of max.
     */
    private Integer minLength;

    /*
     * The maximum length in characters. Default and maximum is 300.
     */
    private Integer maxLength;

    /**
     * Constructor of {@link LengthTokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public LengthTokenFilter(String name) {
        super(name);
    }

    /**
     * Get the minLength property: The minimum length in characters. Default is
     * 0. Maximum is 300. Must be less than the value of max.
     *
     * @return the minLength value.
     */
    public Integer getMinLength() {
        return this.minLength;
    }

    /**
     * Set the minLength property: The minimum length in characters. Default is
     * 0. Maximum is 300. Must be less than the value of max.
     *
     * @param minLength the minLength value to set.
     * @return the LengthTokenFilter object itself.
     */
    public LengthTokenFilter setMinLength(Integer minLength) {
        this.minLength = minLength;
        return this;
    }

    /**
     * Get the maxLength property: The maximum length in characters. Default
     * and maximum is 300.
     *
     * @return the maxLength value.
     */
    public Integer getMaxLength() {
        return this.maxLength;
    }

    /**
     * Set the maxLength property: The maximum length in characters. Default
     * and maximum is 300.
     *
     * @param maxLength the maxLength value to set.
     * @return the LengthTokenFilter object itself.
     */
    public LengthTokenFilter setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
        return this;
    }
}
