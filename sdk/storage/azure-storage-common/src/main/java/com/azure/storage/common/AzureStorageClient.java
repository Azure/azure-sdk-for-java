// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.http.HttpPipeline;

/**
 * Generic interface for clients in storage client libraries.
 */
public interface AzureStorageClient {

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    HttpPipeline getHttpPipeline();

}
