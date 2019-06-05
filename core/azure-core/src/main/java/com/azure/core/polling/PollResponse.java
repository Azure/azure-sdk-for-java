// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.polling;

/**
 * The life cycle of an operation depicted below
 * NOT-STARTED --- IN-PROGRESS
 * ------ Successfully Complete
 * ------ User Cancelled
 * ------ Failed
 **/
public final class PollResponse<T> {

    private OperationStatus status;
    private T result;

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
     **/
    public PollResponse(OperationStatus status, T result) {
        this.status = status;
        this.result = result;
    }

    /**
     * @return OperationStatus
     **/
    public OperationStatus status() {
        return status;
    }

    /**
     * @param status The status to be set
     **/
    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    /**
     * An operation will be done if it is
     * a. Successfully Complete
     * b. Cancelled
     * c. Failed
     *
     * @return true if operation is done.
     **/
    public boolean isDone() {
        return status == OperationStatus.SUCCESSFULLY_COMPLETED
            || status == OperationStatus.FAILED
            || status == OperationStatus.USER_CANCELLED;
    }

    /**
     * Return Result
     *
     * @return T result
     **/
    public T getResult() {
        return result;
    }
}
