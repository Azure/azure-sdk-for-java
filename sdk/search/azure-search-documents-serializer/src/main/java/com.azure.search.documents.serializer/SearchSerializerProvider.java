// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer;

/**
 * Interface to be implemented by an azure-core plugin that wishes to provide a {@link SearchSerializer} implementation.
 */
public interface SearchSerializerProvider {

    /**
     * Creates a new instance of the {@link SearchSerializer} that this JsonSerializerProvider is configured to create.
     *
     * @return A new {@link SearchSerializer} instance.
     */
    SearchSerializer createInstance();
}
