// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.core;

import javax.annotation.Nullable;

/**
 * Interface for processing cosmosdb response
 */
public interface ResponseDiagnosticsProcessor {

    /**
     * Gets called after receiving response from CosmosDb.
     * Response Diagnostics are collected from API responses and
     * then set in {@link ResponseDiagnostics} object.
     * <p>
     * In case of missing diagnostics from CosmosDb, responseDiagnostics will be null.
     *
     * @param responseDiagnostics responseDiagnostics object containing CosmosDb response
     *                            diagnostics information
     */
    void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics);
}
