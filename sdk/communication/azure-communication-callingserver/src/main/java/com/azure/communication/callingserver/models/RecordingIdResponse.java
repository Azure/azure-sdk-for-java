// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.models.RecordingIdResponseInternal;
import com.azure.core.annotation.Immutable;

/** The response payload of start call recording operation. */
@Immutable
public final class RecordingIdResponse {
    /*
     * The recording id of the started recording
     */
    private final String recordingId;

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
     * Get the recordingId property: The recording id of the started recording.
     *
     * @return the recordingId value.
     */
    public String getRecordingId() {
        return this.recordingId;
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
     * Public constructor
     *
     * @param recordingIdResponseInternal The internal response.
     */
    public RecordingIdResponse(RecordingIdResponseInternal recordingIdResponseInternal) {
        this.operationContext = recordingIdResponseInternal.getOperationContext();
        this.recordingId = recordingIdResponseInternal.getRecordingId();
        this.operationId = recordingIdResponseInternal.getOperationId();
        this.resultDetails = new CallingOperationResultDetails(recordingIdResponseInternal.getResultDetails());
        this.status = CallingOperationStatus.fromString(recordingIdResponseInternal.getStatus().toString());
    }
}
