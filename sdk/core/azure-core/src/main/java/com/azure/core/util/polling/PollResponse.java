// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * PollResponse represents a single response from a service for a long-running polling operation. It provides
 * information such as the current {@link LongRunningOperationStatus status} of the long-running operation, any
 * {@link #getValue value} returned in the poll, as well as other useful information provided by the service.
 *
 * <p><strong>Code Sample Creating PollResponse Object</strong></p>
 * {@codesnippet com.azure.core.util.polling.pollresponse.status.value}
 *
 * <p><strong>Code Sample Creating PollResponse Object with custom status</strong></p>
 * {@codesnippet com.azure.core.util.polling.pollresponse.custom.status.value}
 *
 * @param <T> Type of poll response value.
 * @see LongRunningOperationStatus
 */
public final class PollResponse<T> {

    private final LongRunningOperationStatus status;
    private final T value;
    private final Duration retryAfter;
    private final Map<Object, Object> properties;

    /**
     * Creates a new {@link PollResponse} with status, value, retryAfter and properties.
     *
     * <p><strong>Code Sample Creating PollResponse Object</strong></p>
     * {@codesnippet com.azure.core.util.polling.pollresponse.status.value.retryAfter.properties}
     *
     * @param status Mandatory operation status as defined in {@link LongRunningOperationStatus}.
     * @param value The value as a result of poll operation. This can be any custom user-defined object. Null is also
     *     valid.
     * @param retryAfter Represents the delay the service has requested until the next polling operation is performed. A
     *     {@code null}, zero or negative value will be taken to mean that the poller should determine on its
     *     own when the next poll operation is to occur.
     * @param properties A map of properties provided by the service that will be made available into the next poll
     *     operation.
     * @throws NullPointerException If {@code status} is {@code null}.
     */
    public PollResponse(LongRunningOperationStatus status, T value,
                        Duration retryAfter,
                        Map<Object, Object> properties) {
        Objects.requireNonNull(status, "The status input parameter cannot be null.");
        this.status = status;
        this.value = value;
        this.retryAfter = retryAfter;
        this.properties = properties;
    }

    /**
     * Creates a new {@link PollResponse} with status, value and retryAfter.
     *
     * <p><strong>Code Sample Creating PollResponse Object</strong></p>
     * {@codesnippet com.azure.core.util.polling.pollresponse.status.value.retryAfter}
     *
     * @param status Mandatory operation status as defined in {@link LongRunningOperationStatus}.
     * @param value The value as a result of poll operation. This can be any custom user-defined object. Null is also
     *     valid.
     * @param retryAfter Represents the delay the service has requested until the next polling operation is performed. A
     *     {@code null}, zero or negative value will be taken to mean that the poller should determine on its
     *     own when the next poll operation is to occur.
     * @throws NullPointerException If {@code status} is {@code null}.
     */
    public PollResponse(LongRunningOperationStatus status, T value, Duration retryAfter) {
        this(status, value, retryAfter, null);
    }

    /**
     * Creates a new {@link PollResponse} with status and value.
     *
     * <p><strong>Code Sample Creating PollResponse Object</strong></p>
     * {@codesnippet com.azure.core.util.polling.pollresponse.status.value}
     *
     * @param status Mandatory operation status as defined in {@link LongRunningOperationStatus}.
     * @param value The value as a result of poll operation. This can be any custom user-defined object. Null is also
     *     valid.
     * @throws NullPointerException If {@code status} is {@code null}.
     */
    public PollResponse(LongRunningOperationStatus status, T value) {
        this(status, value, null);
    }

    /**
     * Represents the status of the long-running operation at the time the last polling operation finished successfully.
     * @return A {@link LongRunningOperationStatus} representing the result of the poll operation.
     */
    public LongRunningOperationStatus getStatus() {
        return status;
    }

    /**
     * The value returned as a result of the last successful poll operation. This can be any custom user defined object,
     * or null if no value was returned from the service.
     *
     * @return T result of poll operation.
     */
    public T getValue() {
        return value;
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

    /**
     * A map of properties provided by the service that will be made available into the next poll operation.
     *
     * @return Map of properties that were returned from the service, and which will be made available into the next
     *     poll operation.
     */
    public Map<Object, Object> getProperties() {
        return properties;
    }
}
