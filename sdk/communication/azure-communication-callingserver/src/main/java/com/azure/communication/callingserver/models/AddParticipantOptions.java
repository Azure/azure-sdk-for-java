// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.PhoneNumberIdentifier;

/** The option of transferring a call. */
public final class AddParticipantOptions {
    /*
     * The user to operation context.
     */
    private String operationContext;

    /*
     * The alternate identity of the source of the call if dialing out to a
     * pstn number
     */
    private PhoneNumberIdentifier alternateCallerId;

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
     * @return the AddParticipantOptions object itself.
     */
    public AddParticipantOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the alternateCallerId.
     *
     * @return the alternateCaller value.
     */
    public PhoneNumberIdentifier getAlternateCallerId() {
        return this.alternateCallerId;
    }

    /**
     * Set the alternateCallerId property.
     *
     * @param alternateCallerId the alternateCallerId value to set.
     * @return the TransferCallOptions object itself.
     */
    public AddParticipantOptions setAlternateCallerId(PhoneNumberIdentifier alternateCallerId) {
        this.alternateCallerId = alternateCallerId;
        return this;
    }
}
