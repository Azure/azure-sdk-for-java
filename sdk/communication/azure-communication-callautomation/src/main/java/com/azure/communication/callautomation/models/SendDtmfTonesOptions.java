// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.util.List;

/** The SendDtmfTonesOptions model. */
@Fluent
public final class SendDtmfTonesOptions {

    /**
     * Constructor to build SendDtmfTonesOptions object
     *
     * @param tones List of Tones to be sent.
     * @param targetParticipant The target communication identifier.
     */
    public SendDtmfTonesOptions(List<DtmfTone> tones, CommunicationIdentifier targetParticipant) {
        this.tones = tones;
        this.targetParticipant = targetParticipant;
    }

    /**
     * A list of Tones to be sent.
     */
    private final List<DtmfTone> tones;

    /**
     * The target communication identifier.
     */
    private final CommunicationIdentifier targetParticipant;

    /**
     * The operation context
     */
    private String operationContext;

    /**
     * The overridden call back URL override for operation.
     */
    private String operationCallbackUrl;

    /**
     * Get the list of Tones to be sent.
     *
     * @return the list of Tones to be sent.
     */
    public List<DtmfTone> getTones() {
        return tones;
    }

    /**
     * Get the targetParticipant property.
     *
     * @return the targetParticipant value.
     */
    public CommunicationIdentifier getTargetParticipant() {
        return targetParticipant;
    }

    /**
     * Get the operationContext property.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Set the operationContext property.
     *
     * @param operationContext the operationContext value to set.
     * @return the PlayOptionsInternal object itself.
     */
    public SendDtmfTonesOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the overridden call back URL override for operation.
     *
     * @return the operationCallbackUrl
     */
    public String getOperationCallbackUrl() {
        return operationCallbackUrl;
    }

    /**
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     *
     * @param operationCallbackUrl the operationCallbackUrl to set
     * @return the SendDtmfTonesOptions object itself.
     */
    public SendDtmfTonesOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }
}
