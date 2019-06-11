// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.polling;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Container of information related to polling and poll response. It will hold current polling status {@link OperationStatus} , T value,
 * retry after duration and various configuration properties.
 *
 * <p><strong>Container of PollResponse and related information.</strong></p>
 * @param <T> type of poll response value
 */
public final class PollResponse<T> {

    private OperationStatus status;
    private T value;
    private Duration retryAfter;
    private Map<Object, Object> properties;

    /**
     * Represent various state of poll operation.
     * The poll operation is considered complete/done when status is one of SUCCESSFULLY_COMPLETED/FAILED/USER_CANCELLED.
     **/
    public enum OperationStatus {
        NOT_STARTED,
        IN_PROGRESS,
        SUCCESSFULLY_COMPLETED,
        FAILED,
        USER_CANCELLED
    }

    /**
     * Create a Poll Response
     *
     * @param status Mandatory operation status
     * @param result the result
     * @param retryAfter How long to wait before next retry.
     * @param properties the map of perperties which user might need in poll operation. This could be used by Poll Operation function
     *                   when it gets called by {@link Poller}, since this Poll Response object will be supplied to poll operation in next polling cycle.
     */
    public PollResponse(OperationStatus status, T result, Duration retryAfter, Map<Object, Object> properties) {
        this(status, result, retryAfter);
        this.properties = properties;
    }

    /**
     * Create a Poll Response
     *
     * @param status Mandatory operation status
     * @param value the value
     * @param retryAfter How long before next retry.
     */
    public PollResponse(OperationStatus status, T value, Duration retryAfter) {
        this(status, value);
        this.retryAfter = retryAfter;
    }

    /**
     * Create a Poll Response
     *
     * @param status Mandatory operation status
     * @param value the value
     **/
    public PollResponse(OperationStatus status, T value) {
        Objects.requireNonNull(status, "The status input parameter cannot be null.");
        this.status = status;
        this.value = value;
    }

    /**
     * @return OperationStatus
     */
    public OperationStatus getStatus() {
        return status;
    }

    /**
     * @param status The status to be set
     */
    public void setStatus(OperationStatus status) {
        this.status = status;
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
     * retry after durtion
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
