// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URL;

/**
 * Type holding long-running-operation final result or uri to fetch the final result.
 */
final class FinalResult {
    @JsonIgnore
    private final ClientLogger logger = new ClientLogger(FinalResult.class);

    @JsonProperty(value = "resultUri")
    private URL resultUri;
    @JsonProperty(value = "result")
    private String result;

    FinalResult() {
    }

    /**
     * Creates FinalResult.
     *
     * @param resultFetchUri the uri path to fetch the final result
     * @param result the result of long-running-operation
     */
    FinalResult(URL resultFetchUri, String result) {
        if (resultFetchUri == null && result == null) {
            throw logger
                .logExceptionAsError(new IllegalArgumentException("Either resultFetchUri or result is required"));
        }
        this.resultUri = resultFetchUri;
        this.result = result;
    }

    /**
     * @return the uri path to fetch the final result of long-running-operation
     */
    URL getResultUri() {
        return this.resultUri;
    }

    /**
     * @return the result of long-running-operation
     */
    String getResult() {
        return this.result;
    }
}
