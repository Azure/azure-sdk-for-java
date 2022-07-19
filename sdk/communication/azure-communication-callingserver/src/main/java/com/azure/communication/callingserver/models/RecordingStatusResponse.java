// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.accesshelpers.RecordingStatusResponseConstructorProxy;
import com.azure.communication.callingserver.implementation.models.RecordingStatusResponseInternal;
import com.azure.core.annotation.Immutable;

/** The response payload of start call recording operation. */
@Immutable
public final class RecordingStatusResponse {
    static {
        RecordingStatusResponseConstructorProxy.setAccessor(RecordingStatusResponse::new);
    }
    /*
     * The recording id of the started recording
     */
    private final String recordingId;

    private final RecordingStatus recordingStatus;

    /**
     * Get the recordingId property: The recording id of the started recording.
     *
     * @return the recordingId value.
     */
    public String getRecordingId() {
        return this.recordingId;
    }

    /**
     * Get the RecordingStatus property: The recording status of the recording.
     *
     * @return the recordingStatus value.
     */
    public RecordingStatus getRecordingStatus() {
        return this.recordingStatus;
    }

    /**
     * Public constructor.
     *
     */
    public RecordingStatusResponse() {
        this.recordingId = null;
        this.recordingStatus = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param  recordingStatusResponseInternal The response from the service
     */
    RecordingStatusResponse(RecordingStatusResponseInternal recordingStatusResponseInternal) {
        this.recordingId = recordingStatusResponseInternal.getRecordingId();
        this.recordingStatus = RecordingStatus.fromString(recordingStatusResponseInternal.getRecordingStatus().toString());
    }
}
