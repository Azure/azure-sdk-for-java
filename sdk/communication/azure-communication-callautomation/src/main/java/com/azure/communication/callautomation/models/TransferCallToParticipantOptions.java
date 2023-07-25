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
    private String callbackUrlOverride;
    
    /**
     *  Participant being transferred away
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
     * Get the callbackUrlOverride.
     *
     * @return the callbackUrlOverride
     */
    public String  getCallbackUrlOverride() {
        return callbackUrlOverride;
    }

    /**
     * Set the operationContext.
     *
     * @param callbackUrlOverride the callbackUrlOverride to set
     * @return the TransferCallToParticipantOptions object itself.
     */
    public TransferCallToParticipantOptions setCallbackUrlOverride(String callbackUrlOverride) {
        this.callbackUrlOverride = callbackUrlOverride;
        return this;
    }
}
