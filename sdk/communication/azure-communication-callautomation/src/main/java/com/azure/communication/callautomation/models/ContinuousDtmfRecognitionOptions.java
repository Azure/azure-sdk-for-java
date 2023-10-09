// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

/** The ContinuousDtmfRecognitionOptions model. */
@Fluent
public final class ContinuousDtmfRecognitionOptions {

    /**
     * Constructor to build ContinuousDtmfRecognitionOptions object
     *
     * @param targetParticipant The target communication identifier.
     */
    public ContinuousDtmfRecognitionOptions(CommunicationIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
    }

    /**
     * The target communication identifier.
     */
    private final CommunicationIdentifier targetParticipant;

    /**
     * The operation context
     */
    private String operationContext;

    /**
     * The call back URI override.
     */
    private String overrideCallbackUrl;


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
    public ContinuousDtmfRecognitionOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the call back URI override.
     *
     * @return the overrideCallbackUrl
     */
    public String getOverrideCallbackUrl() {
        return overrideCallbackUrl;
    }

    /**
     * Set the call back URI override.
     *
     * @param overrideCallbackUrl The call back URI override to set
     * @return the PlayOptions object itself.
     */
    public ContinuousDtmfRecognitionOptions setOverrideCallbackUrl(String overrideCallbackUrl) {
        this.overrideCallbackUrl = overrideCallbackUrl;
        return this;
    }
}
