// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The type to store the data associated a long-running-operation that successfully completed synchronously.
 */
final class SynchronouslySucceededLroData {
    @JsonProperty(value = "lroResponseBody")
    private String lroResponseBody;
    @JsonProperty(value = "finalResult")
    private FinalResult finalResult;

    SynchronouslySucceededLroData() {
    }

    /**
     * Creates SynchronouslySucceededLroData.
     *
     * @param lroResponseBody the lro response body
     */
    SynchronouslySucceededLroData(String lroResponseBody) {
        this.lroResponseBody = lroResponseBody;
        if (this.lroResponseBody != null) {
            this.finalResult = new FinalResult(null, lroResponseBody);
        }
    }

    /**
     * @return FinalResult object to access final result of long-running-operation.
     */
    FinalResult getFinalResult() {
        return this.finalResult;
    }
}
