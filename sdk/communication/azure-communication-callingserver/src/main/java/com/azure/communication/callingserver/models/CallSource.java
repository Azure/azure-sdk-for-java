// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;


import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Fluent;

/** The CallSource model. */
@Fluent
public final class CallSource {
    /*
     * The alternate identity of the source of the call if dialing out to a
     * pstn number
     */
    private PhoneNumberIdentifier callerId;

    /*
     * The identifier property.
     */
    private final CommunicationIdentifier identifier;

    /**
     * Constructor of the class
     *
     * @param identifier the identifier value to set. Required.
     */
    public CallSource(CommunicationIdentifier identifier) {
        this.identifier = identifier;
    }

    /**
     * Get the callerId property: The alternate identity of the source of the call if dialing out to a pstn number.
     *
     * @return the callerId value.
     */
    public PhoneNumberIdentifier getCallerId() {
        return this.callerId;
    }

    /**
     * Set the callerId property: The alternate identity of the source of the call if dialing out to a pstn number.
     *
     * @param callerId the callerId value to set.
     * @return the CallSource object itself.
     */
    public CallSource setCallerId(PhoneNumberIdentifier callerId) {
        this.callerId = callerId;
        return this;
    }

    /**
     * Get the identifier property: The identifier property.
     *
     * @return the identifier value.
     */
    public CommunicationIdentifier getIdentifier() {
        return this.identifier;
    }

}
