// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.security.attestation.models.AttestationResponse;
import com.azure.security.attestation.models.AttestationToken;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

/**
 * Utility class with helper functions.
 */
class Utilities {

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

    /**
     * Generates a new public response type from an internal model type.
     * @param response Response from the generated API
     * @param value Value to be included in the new response
     * @param <T> Type of `value`.
     * @param <R> Ignored.
     * @return Returns a newly created Response type.
     */
    static <T, R> AttestationResponse<T> generateAttestationResponseFromModelType(
        Response<R> response, AttestationToken token, T value) {
        return new AttestationResponse<>(response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            value,
            token);
    }


    /**
     * Convert a base64 encoded string into a byte stream.
     * @param base64 - Base64 encoded string to be decoded
     * @return stream of bytes encoded in the base64 encoded string.
     */
    static InputStream base64ToStream(String base64) {
        byte[] decoded = Base64.getDecoder().decode(base64);
        return new ByteArrayInputStream(decoded);
    }
}
