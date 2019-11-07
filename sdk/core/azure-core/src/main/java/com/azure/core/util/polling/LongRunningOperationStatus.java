// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An enum to represent all possible states that a long-running operation may find itself in.
 * The poll operation is considered complete when the status is one of {@code SUCCESSFULLY_COMPLETED},
 * {@code USER_CANCELLED} or {@code FAILED}.
 */
public final class LongRunningOperationStatus extends ExpandableStringEnum<LongRunningOperationStatus> {

    private boolean completed;

    /** Represents that polling has not yet started for this long-running operation. */
    public static final LongRunningOperationStatus NOT_STARTED = fromString("NOT_STARTED", false);

    /** Represents that the long-running operation is in progress and not yet complete. */
    public static final LongRunningOperationStatus IN_PROGRESS = fromString("IN_PROGRESS", false);

    /** Represent that the long-running operation is completed successfully. */
    public static final LongRunningOperationStatus SUCCESSFULLY_COMPLETED = fromString("SUCCESSFULLY_COMPLETED",
        true);

    /**
     * Represents that the long-running operation has failed to successfully complete, however this is still
     * considered as complete long-running operation, meaning that the {@link PollerFlux} instance will report
     * that it is complete.
     */
    public static final LongRunningOperationStatus FAILED = fromString("FAILED", true);

    /**
     * Represents that the long-running operation is cancelled by user, however this is still
     * considered as complete long-running operation.
     */
    public static final LongRunningOperationStatus USER_CANCELLED = fromString("USER_CANCELLED", true);

    private static Map<String, LongRunningOperationStatus> operationStatusMap;
    static {
        Map<String, LongRunningOperationStatus> opStatusMap = new HashMap<>();
        opStatusMap.put(NOT_STARTED.toString(), NOT_STARTED);
        opStatusMap.put(IN_PROGRESS.toString(), IN_PROGRESS);
        opStatusMap.put(SUCCESSFULLY_COMPLETED.toString(), SUCCESSFULLY_COMPLETED);
        opStatusMap.put(FAILED.toString(), FAILED);
        opStatusMap.put(USER_CANCELLED.toString(), USER_CANCELLED);
        operationStatusMap = Collections.unmodifiableMap(opStatusMap);
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
        LongRunningOperationStatus status = fromString(name, LongRunningOperationStatus.class);

        if (status != null) {
            if (operationStatusMap != null && operationStatusMap.containsKey(name)) {
                LongRunningOperationStatus operationStatus = operationStatusMap.get(name);
                if (operationStatus.isComplete() != isComplete) {
                    throw new IllegalArgumentException(String.format("Cannot set complete status %s for operation"
                        + "status %s", isComplete, name));
                }
            }
            status.completed = isComplete;
        }
        return status;
    }

    /**
     * Returns a boolean to represent if the operation is in a completed state or not.
     * @return True if the operation is in a completed state, otherwise false.
     */
    public boolean isComplete() {
        return completed;
    }
}
