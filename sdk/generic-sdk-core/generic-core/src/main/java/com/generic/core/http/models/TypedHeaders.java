// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.generic.core.http.models;

import com.generic.core.models.Headers;

/**
 * Interface indicating that the implementing class is a typed representation of HTTP headers.
 *
 * @param <T> The type of the implementing class.
 */
public interface TypedHeaders<T extends TypedHeaders<T>> {
    /**
     * Creates a new instance of the implementing class from the provided {@link Headers}.
     *
     * @param <T> The type of the implementing class.
     * @param headers The {@link Headers} to create an instance of the implementing class from.
     * @return A new instance of the implementing class.
     */
    static <T> T fromHeaders(Headers headers) {
        throw new UnsupportedOperationException("Classes implementing TypedHeaders must implement this method.");
    }
}
