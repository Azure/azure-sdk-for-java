/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt;

/**
 * The different states that a long running operation can be in.
 */
public final class OperationState {
    /**
     * The provisioning state of the operation resource if the operation is still in progress.
     */
    public static final String IN_PROGRESS = "InProgress";

    /**
     * The provisioning state of the operation resource if the operation is successful.
     */
    public static final String SUCCEEDED = "Succeeded";

    /**
     * The provisioning state of the operation resource if the operation is unsuccessful.
     */
    public static final String FAILED = "Failed";

    /**
     * The provisioning state of the operation resource if the operation is canceled.
     */
    public static final String CANCELED = "Canceled";

    /**
     * Get whether or not the provided operation state represents a completed state.
     * @param operationState The operation state to check.
     * @return Whether or not the provided operation state represents a completed state.
     */
    public static boolean isCompleted(String operationState) {
        return operationState == null
                || operationState.length() == 0
                || SUCCEEDED.equalsIgnoreCase(operationState)
                || isFailedOrCanceled(operationState);
    }

    /**
     * Get whether or not the provided operation state represents a failed or canceled state.
     * @param operationState The operation state to check.
     * @return Whether or not the provided operation state represents a failed or canceled state.
     */
    public static boolean isFailedOrCanceled(String operationState) {
        return FAILED.equalsIgnoreCase(operationState)
                || CANCELED.equalsIgnoreCase(operationState);
    }
}
