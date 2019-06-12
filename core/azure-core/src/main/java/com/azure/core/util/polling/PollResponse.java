// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Container of information related to polling and poll response. It will hold current polling status {@link OperationStatus} , T value,
 * retry after duration and various configuration properties.
 *
 * <p><strong>Container of PollResponse and related information.</strong></p>
 * @param <T> type of poll response value
 *
 * @see OperationStatus
 * @see Poller
 */
public final class PollResponse<T> {

    private final OperationStatus status;
    private final T value;
    private final Duration retryAfter;
    private final Map<Object, Object> properties;

    /**
     * Represent various state of poll operation.
     * The poll operation is considered complete/done when status is one of SUCCESSFULLY_COMPLETED/FAILED/USER_CANCELLED.
     */
    public enum OperationStatus {
        /** Represents the state that polling has not started for Long Running Operation.*/
        NOT_STARTED,

        /** Represent the state that Long Running Operation is in progress and not completed/done .*/
        IN_PROGRESS,

        /** Represent the state that Long Running Operation is completed/done successfully. Long Running Operation is considered complete/done.*/
        SUCCESSFULLY_COMPLETED,

        /** Represent the state that Long Running Operation has failed. Long Running Operation is considered complete/done.*/
        FAILED,

        /** Represent the state that Long Running Operation is cancelled by user. Long Running Operation is considered complete/done.*/
        USER_CANCELLED
    }

    /**
     * Create a Poll Response with status, value, retryAfter and properties
     *
     * <p><strong>Code Sample Creating PollResponse Object</strong></p>
     * {@codesnippet com.azure.core.util.polling.pollresponse.status.value.retryAfter.properties}
     *
     * @param status Mandatory operation status as defined in {@link OperationStatus}
     * @param value The value as a result of poll operation. This can be any custom user defined object.
     * @param retryAfter How long poller should wait before calling next pollOperation.
     * @param properties the map of perperties which user might need in executing poll operation such as service url etc.
     * This could be used by Poll Operation function when it gets called by {@link Poller}, since this Poll Response object
     * will be supplied to poll operation in next polling cycle.
     */
    public PollResponse(OperationStatus status, T value, Duration retryAfter, Map<Object, Object> properties) {
        Objects.requireNonNull(status, "The status input parameter cannot be null.");
        this.status = status;
        this.value = value;
        this.retryAfter = retryAfter;
        this.properties = properties;
    }

    /**
     * Create a Poll Response with status, value and retryAfter
     *
     * <p><strong>Code Sample Creating PollResponse Object</strong></p>
     * {@codesnippet com.azure.core.util.polling.pollresponse.status.value.retryAfter}
     *
     * @param status Mandatory operation status as defined in {@link OperationStatus}
     * @param value The value as a result of poll operation. This can be any custom user defined object.
     * @param retryAfter How long poller should wait before calling next pollOperation.
     */
    public PollResponse(OperationStatus status, T value, Duration retryAfter) {
        this(status, value, retryAfter, null);

    }

    /**
     * Create a Poll Response with status and value
     *
     *<p><strong>Code Sample Creating PollResponse Object</strong></p>
     * {@codesnippet com.azure.core.util.polling.pollresponse.status.value}
     *
     * @param status Mandatory operation status as defined in {@link OperationStatus}
     * @param value The value as a result of poll operation. This can be any custom user defined object.
     */
    public PollResponse(OperationStatus status, T value) {
        this(status, value, null);
    }

    /**
     * status of Long Running Operation at the time of polling.
     * @return {@link OperationStatus} as a result of poll operation.
     */
    public OperationStatus getStatus() {
        return status;
    }

    /**
     * An operation will be done/complete if it is of of the following.
     * <ul>
     *     <li>Successfully Complete</li>
     *     <li>Cancelled</li>
     *     <li>Failed</li>
     * </ul>
     *
     * @return true if operation is done/complete.
     */
    public boolean isDone() {
        return status == OperationStatus.SUCCESSFULLY_COMPLETED
            || status == OperationStatus.FAILED
            || status == OperationStatus.USER_CANCELLED;
    }

    /**
     * Return Result
     *
     * @return T result
     */
    public T getValue() {
        return value;
    }

    /**
     * retry after duration
     * @return Duration how long before next retry.
     */
    public Duration getRetryAfter() {
        return  retryAfter;
    }

    /**
     * various config properties which could be used by poll operation  in {@link Poller}
     * @return Map of properties
     */
    public Map<Object, Object> getProperties() {
        return properties;
    }
}
