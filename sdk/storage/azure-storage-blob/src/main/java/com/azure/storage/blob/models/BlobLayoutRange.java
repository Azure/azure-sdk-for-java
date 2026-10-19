// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.HttpRange;

import java.net.URI;

/**
 * Represents a range in a blob layout.
 */
@Immutable
public final class BlobLayoutRange {
    private final HttpRange range;
    private final URI endpoint;

    /**
     * Creates a new {@code BlobLayoutRange}.
     *
     * @param range The {@link HttpRange}.
     * @param endpoint The endpoint that contains the range.
     */
    public BlobLayoutRange(HttpRange range, URI endpoint) {
        this.range = range;
        this.endpoint = endpoint;
    }

    /**
     * Gets the range property.
     *
     * @return The range property.
     */
    public HttpRange getRange() {
        return range;
    }

    /**
     * Gets the endpoint that contains this range.
     *
     * <p>The endpoint is represented as either an absolute URI, such as {@code https://blob.example.net:443/}, or a
     * URI reference containing only an authority, such as {@code //blob.example.net:443}. For an authority-only URI
     * reference, the request's scheme is used.</p>
     *
     * @return The endpoint that contains this range.
     */
    public URI getEndpoint() {
        return endpoint;
    }
}
