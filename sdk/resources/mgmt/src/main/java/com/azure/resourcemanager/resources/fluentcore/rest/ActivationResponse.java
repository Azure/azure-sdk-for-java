// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;

import java.time.Duration;

/**
 * REST LRO activation response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content.
 */
public class ActivationResponse<T> extends SimpleResponse<T> {

    private final LongRunningOperationStatus status;
    private final Duration retryAfter;

    /**
     * Creates a {@link ActivationResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers The headers of the HTTP response.
     * @param value The deserialized value of the HTTP response.
     * @param status Mandatory operation status as defined in {@link LongRunningOperationStatus}.
     * @param retryAfter Represents the delay the service has requested until the next polling operation is performed. A
     *     {@code null}, zero or negative value will be taken to mean that the poller should determine on its
     *     own when the next poll operation is to occur.
     */
    public ActivationResponse(HttpRequest request, int statusCode, HttpHeaders headers, T value,
                              LongRunningOperationStatus status, Duration retryAfter) {
        super(request, statusCode, headers, value);
        this.status = status;
        this.retryAfter = retryAfter;
    }

    /**
     * Represents the status of the long-running operation at the time the last polling operation finished successfully.
     * @return A {@link LongRunningOperationStatus} representing the result of the poll operation.
     */
    public LongRunningOperationStatus getStatus() {
        return status;
    }

    /**
     * Returns the delay the service has requested until the next polling operation is performed. A null or negative
     * value will be taken to mean that the poller should determine on its own when the next poll operation is
     * to occur.
     * @return Duration How long to wait before next retry.
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }
}
