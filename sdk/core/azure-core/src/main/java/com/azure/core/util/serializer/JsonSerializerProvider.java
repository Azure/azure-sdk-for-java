// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

/**
 * Interface to be implemented by an azure-core plugin that wishes to provide a {@link JsonSerializer} implementation.
 */
public interface JsonSerializerProvider {

    /**
     * Creates a new instance of the {@link JsonSerializer} that this JsonSerializerProvider is configured to create.
     *
     * @return A new {@link JsonSerializer} instance.
     */
    JsonSerializer createInstance();
}
