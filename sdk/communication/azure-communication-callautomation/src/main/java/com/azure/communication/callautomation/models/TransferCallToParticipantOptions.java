// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, String> sipHeaders;
    private final Map<String, String> voipHeaders;



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
        this.voipHeaders = new HashMap<String, String>();
        this.sipHeaders = null;
    }

    /**
     * Constructor
     *
     * @param targetParticipant {@link CommunicationUserIdentifier}contains information for TranferTarget.
     */
    public TransferCallToParticipantOptions(CommunicationUserIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
        this.voipHeaders = new HashMap<String, String>();
        this.sipHeaders = null;
    }

    /**
     * Constructor
     *
     * @param targetParticipant {@link CommunicationUserIdentifier}contains information for TranferTarget.
     * @param voipHeaders custom headers to voip target
     */
    public TransferCallToParticipantOptions(CommunicationUserIdentifier targetParticipant, Map<String, String> voipHeaders) {
        this.targetParticipant = targetParticipant;
        this.voipHeaders = voipHeaders == null ? new HashMap<String, String>() : voipHeaders;
        this.sipHeaders = null;
    }

    /**
     * Constructor
     *
     * @param targetParticipant {@link PhoneNumberIdentifier}contains information for TranferTarget.
     * @param sipHeaders custom headers to PSTN target
     */
    public TransferCallToParticipantOptions(PhoneNumberIdentifier targetParticipant, Map<String, String> sipHeaders) {
        this.targetParticipant = targetParticipant;
        this.voipHeaders = null;
        this.sipHeaders = sipHeaders == null ? new HashMap<String, String>() : sipHeaders;
    }

    /**
     * Constructor
     *
     * @param targetParticipant {@link MicrosoftTeamsUserIdentifier}contains information for TranferTarget.
     * @param voipHeaders custom headers to voip target
     */
    public TransferCallToParticipantOptions(MicrosoftTeamsUserIdentifier targetParticipant, Map<String, String> voipHeaders) {
        this.targetParticipant = targetParticipant;
        this.voipHeaders = voipHeaders == null ? new HashMap<String, String>() : voipHeaders;
        this.sipHeaders = null;
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
     * Get the call information to transfer target
     * @return a {@link CommunicationIdentifier} with information to transfer target
     */
    public CommunicationIdentifier getTargetParticipant() {
        return targetParticipant;
    }

    /**
     *  Get custom headers for voip target
     * @return the customHeaders for voip target
     */
    public Map<String, String> getVoipHeaders() {
        return voipHeaders;
    }

    /**
     *  Get custom headers for PSTN target
     * @return custom headers for PSTN target
     */
    public Map<String, String> getSipHeaders() {
        return sipHeaders;
    }
}
