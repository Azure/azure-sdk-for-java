// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.PhoneNumberIdentifier;

/** The option of transferring a call. */
public final class TransferCallOptions {
    /*
     * The user to user information.
     */
    private String userToUserInformation;

    /*
     * The caller ID of the transferee if transferring to a pstn number.
     */
    private PhoneNumberIdentifier transfereeCallerId;

    /*
     * The operation context.
     */
    private String operationContext;

    /**
     * Get the userToUserInformation.
     *
     * @return the userToUserInformation value.
     */
    public String getUserToUserInformation() {
        return this.userToUserInformation;
    }

    /**
     * Set the userToUser property.
     *
     * @param userToUserInformation the userToUserInformation value to set.
     * @return the TransferCallOptions object itself.
     */
    public TransferCallOptions setUserToUserInformation(String userToUserInformation) {
        this.userToUserInformation = userToUserInformation;
        return this;
    }

    /**
     * Get the alternateCallerId.
     *
     * @return the alternateCaller value.
     */
    public PhoneNumberIdentifier getTransfereeCallerId() {
        return this.transfereeCallerId;
    }

    /**
     * Set the alternateCallerId property.
     *
     * @param transfereeCallerId the alternateCallerId value to set.
     * @return the TransferCallOptions object itself.
     */
    public TransferCallOptions setTransfereeCallerId(PhoneNumberIdentifier transfereeCallerId) {
        this.transfereeCallerId = transfereeCallerId;
        return this;
    }

    /**
     * Get the operationContext property: The operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Set the operationContext property: The operation context.
     *
     * @param operationContext the operationContext value to set.
     * @return the TransferToParticipantRequest object itself.
     */
    public TransferCallOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
