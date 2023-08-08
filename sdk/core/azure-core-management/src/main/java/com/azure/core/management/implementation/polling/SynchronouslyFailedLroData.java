// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import java.util.Map;

/**
 * The type to store the data associated a long-running-operation that is Failed to synchronously.
 */
final class SynchronouslyFailedLroData extends Error {

    SynchronouslyFailedLroData() {
    }

    /**
     * Creates SynchronouslyFailedLroData.
     *
     * @param message the error message
     * @param lroResponseStatusCode the http response status code of long-running init operation
     * @param responseHeaders the http response headers of long-running init operation
     * @param lroResponseBody the http response body of long-running init operation
     */
    SynchronouslyFailedLroData(String message,
                               int lroResponseStatusCode,
                               Map<String, String> responseHeaders,
                               String lroResponseBody) {
        super(message, lroResponseStatusCode, responseHeaders, lroResponseBody);
    }
}
