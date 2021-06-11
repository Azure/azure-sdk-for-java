// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.ResultInfoConverter;
import com.azure.communication.callingserver.implementation.models.PlayAudioResultEventInternal;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.ResultInfo;
import com.azure.core.util.BinaryData;

/** The play audio result event. */
public final class PlayAudioResultEvent {
    /*
     * The result details.
     */
    private final ResultInfo resultInfo;

    /*
     * The operation context.
     */
    private final String operationContext;

    /*
     * Gets or sets the status of the operation
     */
    private final OperationStatus status;

    /**
     * Get the resultInfo property: The result details.
     *
     * @return the resultInfo value.
     */
    public ResultInfo getResultInfo() {
        return this.resultInfo;
    }

    /**
     * Get the operationContext property: The operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Get the status property: Gets or sets the status of the operation.
     *
     * @return the status value.
     */
    public OperationStatus getStatus() {
        return this.status;
    }

    /**
     * Initializes a new instance of InviteParticipantResultEvent.
     *
     * @param resultInfo the resultInfo value.
     * @param operationContext the operationContext value.
     * @param status the status value.
     */
    public PlayAudioResultEvent(ResultInfo resultInfo, String operationContext, OperationStatus status) {
        this.resultInfo = resultInfo;
        this.operationContext = operationContext;
        this.status = status;
    }

    /**
     * Deserialize {@link PlayAudioResultEvent} event.
     *
     * @param eventData binary data for event
     * @return {@link PlayAudioResultEvent} event.
     */
    public static PlayAudioResultEvent deserialize(BinaryData eventData) {
        if (eventData == null) {
            return null;
        }
        PlayAudioResultEventInternal playAudioResultEventInternal =
            eventData.toObject(PlayAudioResultEventInternal.class);
        return new PlayAudioResultEvent(
            ResultInfoConverter.convert(playAudioResultEventInternal.getResultInfo()),
            playAudioResultEventInternal.getOperationContext(),
            playAudioResultEventInternal.getStatus());
    }
}
