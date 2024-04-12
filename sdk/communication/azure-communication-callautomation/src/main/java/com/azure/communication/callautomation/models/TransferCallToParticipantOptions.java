// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.HashMap;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.MicrosoftTeamsAppIdentifier;
import com.azure.communication.common.MicrosoftTeamsUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Fluent;

/**
 * The options for adding participants.
 */
@Fluent
public final class TransferCallToParticipantOptions {
    /**
     * The identity of the target where call should be transferred to.
     */
    private final CommunicationIdentifier targetParticipant;
    private final CustomCallingContext customCallingContext;
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
     * The source caller ID number which is a phone number that will be used when inviting a pstn target.
     * Required only when this is an incoming voip call and there will be a transfer call request to a PSTN target.
     */
    private PhoneNumberIdentifier sourceCallerIdNumber;
    
    /**
     * Constructor
     *
     * @param targetParticipant {@link CommunicationIdentifier} contains information for TransferTarget(to whom the call is transferred).
     */
    public TransferCallToParticipantOptions(CommunicationIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        this.customCallingContext = new CustomCallingContext(new HashMap<>(), new HashMap<>());
    }

    /**
     * Constructor
     *
     * @param targetParticipant {@link CommunicationUserIdentifier} contains information for TransferTarget(to whom the call is transferred).
     */
    public TransferCallToParticipantOptions(CommunicationUserIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        this.customCallingContext = new CustomCallingContext(null, new HashMap<>());
    }

    /**
     * Constructor
     *
     * @param targetParticipant {@link PhoneNumberIdentifier} contains information for TransferTarget(to whom the call is transferred).
     */
    public TransferCallToParticipantOptions(PhoneNumberIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        this.customCallingContext = new CustomCallingContext(new HashMap<>(), null);
    }

    /**
     * Constructor
     *
     * @param targetParticipant {@link MicrosoftTeamsUserIdentifier} contains information for TransferTarget(to whom the call is transferred).
     */
    public TransferCallToParticipantOptions(MicrosoftTeamsUserIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        this.customCallingContext = new CustomCallingContext(null, new HashMap<>());
    }

    /**
     * Constructor
     *
     * @param targetParticipant {@link MicrosoftTeamsAppIdentifier} contains information for TransferTarget(to whom the call is transferred).
     */
    public TransferCallToParticipantOptions(MicrosoftTeamsAppIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        this.customCallingContext = new CustomCallingContext(null, new HashMap<>());
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
     * Get the participant who is being transferred away.
     *
     * @return the transferee
     */
    public CommunicationIdentifier getTransferee() {
        return transferee;
    }

    /**
     * Set the participant who is being transferred away.
     *
     * @param transferee the participant who is being transferred away
     * @return the TransferCallToParticipantOptions object itself.
     */
    public TransferCallToParticipantOptions setTransferee(CommunicationIdentifier transferee) {
        this.transferee = transferee;
        return this;
    }

    /**
     * Get the transfer target to whom the call is transferred
     * @return a {@link CommunicationIdentifier} with information to transfer target
     */
    public CommunicationIdentifier getTargetParticipant() {
        return targetParticipant;
    }

    /**
     *  get custom context
     * @return custom context
     */
    public CustomCallingContext getCustomCallingContext() {
        return customCallingContext;
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
    
     /**
     * Get the sourceCallerIdNumber.
     *
     * @return the sourceCallerIdNumber
     */
    public PhoneNumberIdentifier getSourceCallerIdNumber() {
        return sourceCallerIdNumber;
    }
    
    /**
     * Set the sourceCallerIdNumber.
     *
     * @param sourceCallerIdNumber the sourceCallerIdNumber to set
     * @return the TransferCallToParticipantOptions object itself.
     */
    public TransferCallToParticipantOptions setSourceCallerIdNumber(PhoneNumberIdentifier sourceCallerIdNumber) {
        this.sourceCallerIdNumber = sourceCallerIdNumber;
        return this;
    }
}
