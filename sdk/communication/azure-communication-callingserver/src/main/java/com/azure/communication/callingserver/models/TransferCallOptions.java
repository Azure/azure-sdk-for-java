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
     * The alternate identity of the source of the call if dialing out to a
     * pstn number
     */
    private PhoneNumberIdentifier alternateCallerId;

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
    public PhoneNumberIdentifier getAlternateCallerId() {
        return this.alternateCallerId;
    }

    /**
     * Set the alternateCallerId property.
     *
     * @param alternateCallerId the alternateCallerId value to set.
     * @return the TransferCallOptions object itself.
     */
    public TransferCallOptions setAlternateCallerId(PhoneNumberIdentifier alternateCallerId) {
        this.alternateCallerId = alternateCallerId;
        return this;
    }
}
