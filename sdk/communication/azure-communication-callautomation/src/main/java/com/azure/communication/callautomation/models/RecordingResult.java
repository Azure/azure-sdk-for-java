// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.time.OffsetDateTime;
import java.util.List;

import com.azure.communication.callautomation.implementation.accesshelpers.RecordingResultResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.Error;
import com.azure.communication.callautomation.implementation.models.RecordingResultResponse;
import com.azure.core.annotation.Immutable;

/** The response payload of get call recording result operation. */
@Immutable
public final class RecordingResult {
    static {
        RecordingResultResponseConstructorProxy.setAccessor(RecordingResult::new);
    }
    /*
     * The recording id of the started recording
     */
    private final String recordingId;

    /*
     * Container for chunks
     */
    private final RecordingStorageInfo recordingStorageInfo;

    /*
     * The errors property.
     */
    private final List<Error> errors;

    /*
     * The recordingStartTime property.
     */
    private final OffsetDateTime recordingStartTime;

    /*
     * The recordingDurationMs property.
     */
    private final Long recordingDurationMs;

    /*
     * The sessionEndReason property.
     */
    private final CallSessionEndReasonInfo sessionEndReason;

    /*
     * The recordingExpirationTime property.
     */
    private final OffsetDateTime recordingExpirationTime;

    /**
     * Creates an instance of RecordingResult class.
     */
    public RecordingResult() {
        this.recordingId = null;
        this.errors = null;
        this.recordingDurationMs = null;
        this.recordingStartTime = null;
        this.recordingExpirationTime = null;
        this.sessionEndReason = null;
        this.recordingStorageInfo = null;
    }

    /**
     * Get the recordingId property: The recordingId property.
     * 
     * @return the recordingId value.
     */
    public String getRecordingId() {
        return this.recordingId;
    }

    /**
     * Get the recordingStorageInfo property: Container for chunks.
     * 
     * @return the recordingStorageInfo value.
     */
    public RecordingStorageInfo getRecordingStorageInfo() {
        return this.recordingStorageInfo;
    }

    /**
     * Get the errors property: The errors property.
     * 
     * @return the errors value.
     */
    public List<Error> getErrors() {
        return this.errors;
    }

    /**
     * Get the recordingStartTime property: The recordingStartTime property.
     * 
     * @return the recordingStartTime value.
     */
    public OffsetDateTime getRecordingStartTime() {
        return this.recordingStartTime;
    }

    /**
     * Get the recordingDurationMs property: The recordingDurationMs property.
     * 
     * @return the recordingDurationMs value.
     */
    public Long getRecordingDurationMs() {
        return this.recordingDurationMs;
    }

    /**
     * Get the sessionEndReason property: The sessionEndReason property.
     * 
     * @return the sessionEndReason value.
     */
    public CallSessionEndReasonInfo getSessionEndReason() {
        return this.sessionEndReason;
    }

    /**
     * Get the recordingExpirationTime property: The recordingExpirationTime property.
     * 
     * @return the recordingExpirationTime value.
     */
    public OffsetDateTime getRecordingExpirationTime() {
        return this.recordingExpirationTime;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param  recordingResultResponse The response from the service
     */
    RecordingResult(RecordingResultResponse recordingResultResponse) {
        RecordingStorageInfo storageInfo = new RecordingStorageInfo();
        this.recordingId = recordingResultResponse.getRecordingId();
        this.errors = recordingResultResponse.getErrors();
        this.recordingDurationMs = recordingResultResponse.getRecordingDurationMs();
        this.recordingStartTime = recordingResultResponse.getRecordingStartTime();
        this.recordingExpirationTime = recordingResultResponse.getRecordingExpirationTime();
        this.recordingStorageInfo
            = storageInfo.setRecordingChunks(recordingResultResponse.getRecordingStorageInfo().getRecordingChunks());
        this.sessionEndReason
            = CallSessionEndReasonInfo.fromString(recordingResultResponse.getSessionEndReason().toString());
    }
}
