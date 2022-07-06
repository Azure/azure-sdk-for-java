// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.models.RecordingStateResponseInternal;
import com.azure.core.annotation.Immutable;

/** The RecordingStateResponse model. */
@Immutable
public final class RecordingStateResponse {
    /*
     * The recordingState property.
     */
    private final RecordingState recordingState;

    /*
     * The operation id.
     */
    private final String operationId;

    /*
     * The status of the operation
     */
    private final CallingOperationStatus status;

    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    /*
     * The result info for the operation.
     */
    private final CallingOperationResultDetails resultDetails;

    /**
     * Get the recordingState property: The recordingState property.
     *
     * @return the recordingState value.
     */
    public RecordingState getRecordingState() {
        return this.recordingState;
    }

    /**
     * Get the operationId property: The operation id.
     *
     * @return the operationId value.
     */
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * Get the status property: The status of the operation.
     *
     * @return the status value.
     */
    public CallingOperationStatus getStatus() {
        return this.status;
    }

    /**
     * Get the operationContext property: The operation context provided by client.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Get the resultDetails property: The result info for the operation.
     *
     * @return the resultDetails value.
     */
    public CallingOperationResultDetails getResultDetails() {
        return this.resultDetails;
    }

    /**
     * Public constructor.
     *
     * @param recordingStateResponseInternal The internal response.
     */
    public RecordingStateResponse(RecordingStateResponseInternal recordingStateResponseInternal) {
        this.operationContext = recordingStateResponseInternal.getOperationContext();
        this.operationId = recordingStateResponseInternal.getOperationId();
        this.recordingState = RecordingState.fromString(recordingStateResponseInternal.getRecordingState().toString());
        this.status = CallingOperationStatus.fromString(recordingStateResponseInternal.getStatus().toString());
        this.resultDetails = new CallingOperationResultDetails(recordingStateResponseInternal.getResultDetails());
    }
}
