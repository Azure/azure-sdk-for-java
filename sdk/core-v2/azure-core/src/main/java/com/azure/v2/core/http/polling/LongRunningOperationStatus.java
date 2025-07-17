// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.polling;

import io.clientcore.core.utils.ExpandableEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enum to represent all possible states that a long-running operation may find itself in.
 * The poll operation is considered complete when the status is one of {@code SUCCESSFULLY_COMPLETED},
 * {@code USER_CANCELLED} or {@code FAILED}.
 */
public final class LongRunningOperationStatus implements ExpandableEnum<String> {

    /** Represents that polling has not yet started for this long-running operation. */
    public static final LongRunningOperationStatus NOT_STARTED = fromString("NOT_STARTED", false);

    /** Represents that the long-running operation is in progress and not yet complete. */
    public static final LongRunningOperationStatus IN_PROGRESS = fromString("IN_PROGRESS", false);

    /** Represent that the long-running operation is completed successfully. */
    public static final LongRunningOperationStatus SUCCESSFULLY_COMPLETED = fromString("SUCCESSFULLY_COMPLETED", true);

    /**
     * Represents that the long-running operation has failed to successfully complete, however this is still
     * considered as complete long-running operation, meaning that the poller instance will report
     * that it is complete.
     */
    public static final LongRunningOperationStatus FAILED = fromString("FAILED", true);

    /**
     * Represents that the long-running operation is cancelled by user, however this is still
     * considered as complete long-running operation.
     */
    public static final LongRunningOperationStatus USER_CANCELLED = fromString("USER_CANCELLED", true);

    private static final Map<String, LongRunningOperationStatus> OPERATION_STATUS_MAP;

    static {
        Map<String, LongRunningOperationStatus> opStatusMap = new HashMap<>();
        opStatusMap.put(NOT_STARTED.toString(), NOT_STARTED);
        opStatusMap.put(IN_PROGRESS.toString(), IN_PROGRESS);
        opStatusMap.put(SUCCESSFULLY_COMPLETED.toString(), SUCCESSFULLY_COMPLETED);
        opStatusMap.put(FAILED.toString(), FAILED);
        opStatusMap.put(USER_CANCELLED.toString(), USER_CANCELLED);
        OPERATION_STATUS_MAP = Collections.unmodifiableMap(opStatusMap);
    }

    private final String name;
    private final boolean completed;

    /**
     * Returns a boolean to represent if the operation is in a completed state or not.
     * @return True if the operation is in a completed state, otherwise false.
     */
    public boolean isComplete() {
        return completed;
    }

    private LongRunningOperationStatus(String name, boolean isComplete) {
        this.name = name;
        this.completed = isComplete;
    }

    /**
     * Returns the long running operation status.
     * @return the long running operation status.
     */
    public String getValue() {
        return this.name;
    }

    /**
     * Creates or finds a {@link LongRunningOperationStatus} from its string representation.
     * @param name a name to look for
     * @param isComplete a status to indicate if the operation is complete or not.
     * @throws IllegalArgumentException if {@code name} matches a pre-configured {@link LongRunningOperationStatus} but
     * {@code isComplete} doesn't match its pre-configured complete status.
     * @return the corresponding {@link LongRunningOperationStatus}
     */
    public static LongRunningOperationStatus fromString(String name, boolean isComplete) {
        if (name == null) {
            return null;
        }

        // Get the known status first and validate as it's a smaller lookup map.
        LongRunningOperationStatus operationStatus
            = (OPERATION_STATUS_MAP != null) ? OPERATION_STATUS_MAP.get(name) : null;
        if (operationStatus != null) {
            if (operationStatus.isComplete() != isComplete) {
                throw new IllegalArgumentException(
                    String.format("Cannot set complete status %s for operation status %s", isComplete, name));
            }
        }

        return new LongRunningOperationStatus(name, isComplete);
    }

}
