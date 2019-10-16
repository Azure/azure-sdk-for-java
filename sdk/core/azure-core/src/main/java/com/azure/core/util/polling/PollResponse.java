// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.ExpandableStringEnum;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * PollResponse represents a single response from a service for a long-running polling operation. It provides
 * information such as the current {@link OperationStatus status} of the long-running operation, any
 * {@link #getValue value} returned in the poll, as well as other useful information provided by the service.
 *
 * <p><strong>Code Sample Creating PollResponse Object</strong></p>
 * {@codesnippet com.azure.core.util.polling.pollresponse.status.value}
 *
 * <p><strong>Code Sample Creating PollResponse Object with custom status</strong></p>
 * {@codesnippet com.azure.core.util.polling.pollresponse.custom.status.value}
 *
 * @param <T> Type of poll response value.
 * @see OperationStatus
 * @see Poller
 */
public final class PollResponse<T> {

    private final OperationStatus status;
    private final T value;
    private final Duration retryAfter;
    private final Map<Object, Object> properties;

    /**
     * An enum to represent all possible states that a long-running operation may find itself in.
     * The poll operation is considered complete when the status is one of {@code SUCCESSFULLY_COMPLETED},
     * {@code USER_CANCELLED} or {@code FAILED}.
     */
    public static final class OperationStatus extends ExpandableStringEnum<OperationStatus> {

        private boolean completed;

        /** Represents that polling has not yet started for this long-running operation. */
        public static final OperationStatus NOT_STARTED = fromString("NOT_STARTED", false);

        /** Represents that this long-running operation is in progress and not yet complete. */
        public static final OperationStatus IN_PROGRESS = fromString("IN_PROGRESS", false);

        /** Represent that this long-running operation is completed successfully. */
        public static final OperationStatus SUCCESSFULLY_COMPLETED = fromString("SUCCESSFULLY_COMPLETED",
            true);

        /**
         * Represents that this long-running operation has failed to successfully complete, however this is still
         * considered as complete long-running operation, meaning that the {@link Poller} instance will report that it
         * is complete.
         */
        public static final OperationStatus FAILED = fromString("FAILED", true);

        /**
         * Represents that this long-running operation is cancelled by user, however this is still
         * considered as complete long-running operation.
         */
        public static final OperationStatus USER_CANCELLED = fromString("USER_CANCELLED", true);

        private static Map<String, OperationStatus> operationStatusMap;
        static {
            Map<String, OperationStatus> opStatusMap = new HashMap<>();
            opStatusMap.put(NOT_STARTED.toString(), NOT_STARTED);
            opStatusMap.put(IN_PROGRESS.toString(), IN_PROGRESS);
            opStatusMap.put(SUCCESSFULLY_COMPLETED.toString(), SUCCESSFULLY_COMPLETED);
            opStatusMap.put(FAILED.toString(), FAILED);
            opStatusMap.put(USER_CANCELLED.toString(), USER_CANCELLED);
            operationStatusMap = Collections.unmodifiableMap(opStatusMap);
        }

        /**
         * Creates or finds a {@link OperationStatus} from its string representation.
         * @param name a name to look for
         * @param isComplete a status to indicate if the operation is complete or not.
         * @throws IllegalArgumentException if invalid {@code isComplete} is provided for a pre-configured
         * {@link OperationStatus} with {@code name}
         * @return the corresponding {@link OperationStatus}
         */
        public static OperationStatus fromString(String name, boolean isComplete) {
            OperationStatus status = fromString(name, OperationStatus.class);

            if (status != null) {
                if (operationStatusMap != null && operationStatusMap.containsKey(name)) {
                    OperationStatus operationStatus = operationStatusMap.get(name);
                    if (operationStatus.isComplete() != isComplete) {
                        throw new IllegalArgumentException(String.format("Cannot set complete status %s for operation"
                            + "status %s", isComplete, name));
                    }
                }
                status.completed = isComplete;
            }
            return status;
        }

        public boolean isComplete() {
            return completed;
        }
    }

    /**
     * Creates a new {@link PollResponse} with status, value, retryAfter and properties.
     *
     * <p><strong>Code Sample Creating PollResponse Object</strong></p>
     * {@codesnippet com.azure.core.util.polling.pollresponse.status.value.retryAfter.properties}
     *
     * @param status Mandatory operation status as defined in {@link OperationStatus}.
     * @param value The value as a result of poll operation. This can be any custom user-defined object. Null is also
     *     valid.
     * @param retryAfter Represents the delay the service has requested until the next polling operation is performed. A
     *     {@code null}, zero or negative value will be taken to mean that the {@link Poller} should determine on its
     *     own when the next poll operation is to occur.
     * @param properties A map of properties provided by the service that will be made available into the next poll
     *     operation.
     * @throws NullPointerException If {@code status} is {@code null}.
     */
    public PollResponse(OperationStatus status, T value, Duration retryAfter, Map<Object, Object> properties) {
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
     * @param status Mandatory operation status as defined in {@link OperationStatus}.
     * @param value The value as a result of poll operation. This can be any custom user-defined object. Null is also
     *     valid.
     * @param retryAfter Represents the delay the service has requested until the next polling operation is performed. A
     *     {@code null}, zero or negative value will be taken to mean that the {@link Poller} should determine on its
     *     own when the next poll operation is to occur.
     * @throws NullPointerException If {@code status} is {@code null}.
     */
    public PollResponse(OperationStatus status, T value, Duration retryAfter) {
        this(status, value, retryAfter, null);
    }

    /**
     * Creates a new {@link PollResponse} with status and value.
     *
     * <p><strong>Code Sample Creating PollResponse Object</strong></p>
     * {@codesnippet com.azure.core.util.polling.pollresponse.status.value}
     *
     * @param status Mandatory operation status as defined in {@link OperationStatus}.
     * @param value The value as a result of poll operation. This can be any custom user-defined object. Null is also
     *     valid.
     * @throws NullPointerException If {@code status} is {@code null}.
     */
    public PollResponse(OperationStatus status, T value) {
        this(status, value, null);
    }

    /**
     * Represents the status of the long-running operation at the time the last polling operation finished successfully.
     * @return A {@link OperationStatus} representing the result of the poll operation.
     */
    public OperationStatus getStatus() {
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
     * value will be taken to mean that the {@link Poller} should determine on its own when the next poll operation is
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
