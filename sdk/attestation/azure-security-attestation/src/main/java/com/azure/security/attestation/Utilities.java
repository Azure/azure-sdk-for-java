// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;

/**
 * Utility class with helper functions.
 */
public class Utilities {

    /**
     * Generates a new public response type from an internal model type.
     * @param response Response from the generated API
     * @param value Value to be included in the new response
     * @param <T> Type of `value`.
     * @param <R> Ignored.
     * @return Returns a newly created Response type.
     */
    static <T, R> ResponseBase<Void, T> generateResponseFromModelType(Response<R> response, T value) {
        return new ResponseBase<>(response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            value,
            null);
    }
}
