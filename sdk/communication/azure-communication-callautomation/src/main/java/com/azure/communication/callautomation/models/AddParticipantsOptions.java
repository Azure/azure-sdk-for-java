// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;


import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
     * (Optional) The display name of the source that is associated with this invite operation when
     * adding a PSTN participant or teams user.  Note: Will not update the display name in the roster.
     */
    private String sourceDisplayName;

    /** (Optional) The identifier of the source of the call for this invite operation. If SourceDisplayName
     * is not set, the display name of the source will be used by default when adding a PSTN participant or teams user.
     */
    private CommunicationIdentifier sourceIdentifier;

    /**
     * The timeout to wait for the invited participant to pickup.
     * The maximum value of this is 180 seconds.
     */
    private Duration invitationTimeout;

    /**
     * Repeatability Headers Configuration
     */
    private RepeatabilityHeaders repeatabilityHeaders;

    /**
     * Constructor
     *
     * @param participants The list of participants to invite.
     */
    public AddParticipantsOptions(List<CommunicationIdentifier> participants) {
        this.participants = participants;
        this.repeatabilityHeaders = new RepeatabilityHeaders(UUID.fromString("0-0-0-0-0"), Instant.MIN);
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
    public String getOperationContext() {
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
     * Get the sourceDisplayName.
     *
     * @return the source display name.
     */
    public String getSourceDisplayName() {
        return sourceDisplayName;
    }

    /**
     * Get the sourceIdentifier.
     *
     * @return the source identifier.
     */
    public CommunicationIdentifier getSourceIdentifier() {
        return sourceIdentifier;
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
     * Get the Repeatability headers configuration.
     *
     * @return the repeatabilityHeaders
     */
    public RepeatabilityHeaders getRepeatabilityHeaders() {
        return repeatabilityHeaders;
    }

    /**
     * Set the repeatability headers
     *
     * @param repeatabilityHeaders The repeatability headers configuration.
     * @return the AddParticipantsOptions object itself.
     */
    public AddParticipantsOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
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
     * Set the sourceDisplayName.
     *
     * @param sourceDisplayName Set the display name of the source that is associated with this invite operation when
     *      adding a PSTN participant or teams user.  Note: Will not update the display name in the roster.
     * @return the AddParticipantsOptions object itself.
     */
    public AddParticipantsOptions setSourceDisplayName(String sourceDisplayName) {
        this.sourceDisplayName = sourceDisplayName;
        return this;
    }

    /**
     * Set the sourceIdentifier.
     *
     * @param sourceIdentifier Set the identifier of the source of the call for this invite operation. If
     *                         SourceDisplayName is not set, the display name of the source will be used by default when
     *                         adding a PSTN participant or teams user.
     * @return the AddParticipantsOptions object itself.
     */
    public AddParticipantsOptions setSourceIdentifier(CommunicationIdentifier sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
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
