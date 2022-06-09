// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Fluent;

/**
 * The options for creating a call.
 */
@Fluent
public final class CreateCallOptions {

    /**
     * The alternate caller id of the source.
     */
    private PhoneNumberIdentifier alternateCallerId;

    /**
     * The subject.
     */
    private String subject;

    /**
     * Get the alternate caller id of the source.
     *
     * @return the alternate caller id object itself.
     */
    public PhoneNumberIdentifier getAlternateCallerId() {
        return alternateCallerId;
    }

    /**
     * Set the alternate caller id of the source to be used when target is phone number.
     *
     * @param alternateCallerId the alternate caller id value to set.
     * @return the CreateCallOptions object itself.
     */
    public CreateCallOptions setAlternateCallerId(PhoneNumberIdentifier alternateCallerId) {
        this.alternateCallerId = alternateCallerId;
        return this;
    }

    /**
     * Get the subject.
     *
     * @return the subject value.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the subject.
     *
     * @param subject the subject.
     * @return the CreateCallOptions object itself.
     */
    public CreateCallOptions setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Initializes a new instance of CreateCallOptions.
     *
     * @param alternateCallerId The alternate caller id of the source.
     * @param subject The subject.
     * @throws IllegalArgumentException if any parameters are null.
     */
    public CreateCallOptions(PhoneNumberIdentifier alternateCallerId, String subject) {
        if (alternateCallerId == null) {
            throw new IllegalArgumentException("object phoneNumberIdentifier cannot be null");
        }
        if (subject == null) {
            throw new IllegalArgumentException("object subject cannot be null");
        }
        this.alternateCallerId = alternateCallerId;
        this.subject = subject;
    }
}
