// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.polling;

import java.time.Duration;
import java.util.Map;

/**
 * This class is container of information related to polling and poll response.
 * 
 *
 * <p><strong>Container of PollResponse and related information.</strong></p>
 * @param <T>
 */
public final class PollResponse<T> {

    private OperationStatus status;
    private T value;
    private Duration retryAfter;
    private Map<Object, Object> properties;

    public enum OperationStatus {
        NOT_STARTED,
        IN_PROGRESS,
        SUCCESSFULLY_COMPLETED,
        FAILED,
        USER_CANCELLED
    }

    /**
     * Constructor
     *
     * @param status : operation status
     * @param result : the result
     * @param retryAfter : How long before next retry.
     * @param properties : the map of perperties
     */
    public PollResponse(OperationStatus status, T result, Duration retryAfter, Map<Object, Object> properties) {
        this(status, result, retryAfter);
        this.properties = properties;
    }
    /**
     * Constructor
     *
     * @param status : operation status
     * @param value : the value
     * @param retryAfter : How long before next retry.
     */
    public PollResponse(OperationStatus status, T value, Duration retryAfter) {
        this(status, value);
        this.retryAfter = retryAfter;
    }

    /**
     * Constructor
     *
     * @param status : operation status
     * @param value : the value
     */
    public PollResponse(OperationStatus status, T value) {
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
     * An operation will be done if it is
     * <ul>
     *     <li>Successfully Complete</li>
     *     <li>Cancelled</li>
     *     <li>Failed</li>
     * </ul>
     *
     * @return true if operation is done.
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
     *
     * @return Duration how long before next retry.
     */
    public Duration getRetryAfter() {
        return  retryAfter;
    }

    /**
     *
     * @return Map of properties
     */
    public Map<Object, Object> getProperties() {
        return properties;
    }
}
