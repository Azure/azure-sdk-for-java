// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;

/**
 * Converts alphabetic, numeric, and symbolic Unicode characters which are not
 * in the first 127 ASCII characters (the "Basic Latin" Unicode block) into
 * their ASCII equivalents, if such equivalents exist. This token filter is
 * implemented using Apache Lucene.
 */
@Fluent
public final class AsciiFoldingTokenFilter extends TokenFilter {
    /*
     * A value indicating whether the original token will be kept. Default is
     * false.
     */
    private Boolean preserveOriginal;

    /**
     * Constructor of {@link AsciiFoldingTokenFilter}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    public AsciiFoldingTokenFilter(String name) {
        super(name);
    }

    /**
     * Get the preserveOriginal property: A value indicating whether the
     * original token will be kept. Default is false.
     *
     * @return the preserveOriginal value.
     */
    public Boolean isPreserveOriginal() {
        return this.preserveOriginal;
    }

    /**
     * Set the preserveOriginal property: A value indicating whether the
     * original token will be kept. Default is false.
     *
     * @param preserveOriginal the preserveOriginal value to set.
     * @return the AsciiFoldingTokenFilter object itself.
     */
    public AsciiFoldingTokenFilter setPreserveOriginal(Boolean preserveOriginal) {
        this.preserveOriginal = preserveOriginal;
        return this;
    }
}
