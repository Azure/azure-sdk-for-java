// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import org.springframework.lang.Nullable;

/**
 * Interface for processing cosmosDB response
 */
public interface ResponseDiagnosticsProcessor {

    /**
     * Gets called after receiving response from CosmosDb. Response Diagnostics are collected from API responses and
     * then set in {@link ResponseDiagnostics} object.
     * <p>
     * In case of missing diagnostics from CosmosDb, responseDiagnostics will be null.
     * <p>
     * NOTE: Since processResponseDiagnostics() API will get called in every cosmos spring data implementation API to
     * capture the diagnostics details, it is highly recommended to not have any long running / CPU intensive work in
     * the implementation of this API.
     *
     * @param responseDiagnostics responseDiagnostics object containing CosmosDb response diagnostics information
     */
    void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics);
}
