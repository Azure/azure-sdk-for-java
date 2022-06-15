// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.PhoneNumberIdentifier;

/** The option of transferring a call. */
public final class AddParticipantsOptions {
    /*
     * The user to operation context.
     */
    private String operationContext;

    /*
     * The source caller Id that's shown to the PSTN participant being invited.
     * Required only when inviting a PSTN participant.
     */
    private PhoneNumberIdentifier sourceCallerId;

    /*
     * The timeout to wait for the invited participant to pickup.
     * The maximum value of this is 180 seconds.
     */
    private Integer invitationTimeoutInSeconds;

    /**
     * Get the operationContext.
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
     * @return the AddParticipantsOptions object itself.
     */
    public AddParticipantsOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the alternateCallerId.
     *
     * @return the alternateCaller value.
     */
    public PhoneNumberIdentifier getSourceCallerId() {
        return this.sourceCallerId;
    }

    /**
     * Set the alternateCallerId property.
     *
     * @param sourceCallerId the alternateCallerId value to set.
     * @return the TransferCallOptions object itself.
     */
    public AddParticipantsOptions setSourceCallerId(PhoneNumberIdentifier sourceCallerId) {
        this.sourceCallerId = sourceCallerId;
        return this;
    }

    /**
     * Get the operationContext.
     *
     * @return the operationContext value.
     */
    public Integer getInvitationTimeoutInSeconds() {
        return this.invitationTimeoutInSeconds;
    }

    /**
     * Set the operationContext property.
     *
     * @param invitationTimeoutInSeconds the invitationTimeoutInSeconds value to set.
     * @return the AddParticipantsOptions object itself.
     */
    public AddParticipantsOptions setInvitationTimeoutInSeconds(Integer invitationTimeoutInSeconds) {
        this.invitationTimeoutInSeconds = invitationTimeoutInSeconds;
        return this;
    }
}
