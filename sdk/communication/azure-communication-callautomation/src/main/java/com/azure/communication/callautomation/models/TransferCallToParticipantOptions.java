// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.HashMap;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.MicrosoftTeamsUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Fluent;

/**
 * The options for adding participants.
 */
@Fluent
public final class TransferCallToParticipantOptions {
    private final CommunicationIdentifier targetParticipant;
    private final CustomContext customContext;
    private String operationCallbackUrl;

    /**
     *  Transferee is the participant who is transferred away
     */
    private CommunicationIdentifier transferee;



    /**
     * The operational context
     */
    private String operationContext;

    /**
     * Constructor
     *
     * @param targetParticipant {@link CommunicationIdentifier}contains information for TranferTarget.
     */
    public TransferCallToParticipantOptions(CommunicationIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        this.customContext = new CustomContext(new HashMap<String, String>(), new HashMap<String, String>());
    }

    /**
     * Constructor
     *
     * @param targetParticipant {@link CommunicationUserIdentifier}contains information for TranferTarget.
     */
    public TransferCallToParticipantOptions(CommunicationUserIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        this.customContext = new CustomContext(null, new HashMap<String, String>());
    }

    /**
     * Constructor
     *
     * @param targetParticipant {@link PhoneNumberIdentifier}contains information for TranferTarget.
     */
    public TransferCallToParticipantOptions(PhoneNumberIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        this.customContext = new CustomContext(new HashMap<String, String>(), null);
    }

    /**
     * Constructor
     *
     * @param targetParticipant {@link MicrosoftTeamsUserIdentifier}contains information for TranferTarget.
     */
    public TransferCallToParticipantOptions(MicrosoftTeamsUserIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        this.customContext = new CustomContext(null, new HashMap<String, String>());
    }

    /**
     * Get the operationContext.
     *
     * @return the operationContext
     */
    public String  getOperationContext() {
        return operationContext;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the TransferCallToParticipantOptions object itself.
     */
    public TransferCallToParticipantOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get transferee.
     *
     * @return the transferee
     */
    public CommunicationIdentifier getTransferee() {
        return transferee;
    }

    /**
     * Set the transferee.
     *
     * @param transferee the transferee to set
     * @return the TransferCallToParticipantOptions object itself.
     */
    public TransferCallToParticipantOptions setTransferee(CommunicationIdentifier transferee) {
        this.transferee = transferee;
        return this;
    }

    /**
     * Get the call information to transfer target
     * @return a {@link CommunicationIdentifier} with information to transfer target
     */
    public CommunicationIdentifier getTargetParticipant() {
        return targetParticipant;
    }

    /**
     *  get custom context
     * @return custom context
     */
    public CustomContext getCustomContext() {
        return customContext;
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
     * @return the TransferCallToParticipantOptions object itself.
     */
    public TransferCallToParticipantOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }
}
