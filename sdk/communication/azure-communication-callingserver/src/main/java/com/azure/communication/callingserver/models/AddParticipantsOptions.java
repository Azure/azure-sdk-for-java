// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;


import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.util.List;

/**
 * The options for adding participants.
 */
@Fluent
public final class AddParticipantsOptions {
    /**
     * The list of participants to invite.
     */
    private final List<CommunicationIdentifier> participants;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * The source caller Id that's shown to the PSTN participant being invited.
     * Required only when inviting a PSTN participant.
     */
    private PhoneNumberIdentifier sourceCallerId;

    /**
     * The timeout to wait for the invited participant to pickup.
     * The maximum value of this is 180 seconds.
     */
    private Duration invitationTimeout;

    /**
     * Constructor
     *
     * @param participants The list of participants to invite.
     */
    public AddParticipantsOptions(List<CommunicationIdentifier> participants) {
        this.participants = participants;
    }

    /**
     * Get the participants.
     *
     * @return the list of participants to be added
     */
    public List<CommunicationIdentifier> getParticipants() {
        return participants;
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
     * Get the sourceCallerId.
     *
     * @return the sourceCallerId phone identifier.
     */
    public PhoneNumberIdentifier getSourceCallerId() {
        return sourceCallerId;
    }

    /**
     * Get the invitationTimeoutInSeconds.
     *
     * @return the Invitation Timeout In Seconds
     */
    public Duration getInvitationTimeout() {
        return invitationTimeout;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the AddParticipantsOptions object itself.
     */
    public AddParticipantsOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set the sourceCallerId.
     *
     * @param sourceCallerId Set the source caller Id that's shown to the PSTN participant being invited.
     *                       Required only when inviting a PSTN participant.
     * @return the AddParticipantsOptions object itself.
     */
    public AddParticipantsOptions setSourceCallerId(PhoneNumberIdentifier sourceCallerId) {
        this.sourceCallerId = sourceCallerId;
        return this;
    }

    /**
     * Set the invitationTimeoutInSeconds.
     *
     * @param invitationTimeout Set the timeout to wait for the invited participant to pickup.
     *                                   The maximum value of this is 180 seconds.
     * @return the AddParticipantsOptions object itself.
     */
    public AddParticipantsOptions setInvitationTimeout(Duration invitationTimeout) {
        this.invitationTimeout = invitationTimeout;
        return this;
    }
}
