// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.PhoneNumberIdentifier;

/**
 * The options for creating a call.
 */
public final class CreateCallOptions {

    /**
     * The alternate caller id of the source.
     */
    private PhoneNumberIdentifier alternateCallerId;

    /**
     * Get the alternate caller id of the source.
     *
     * @return the alternate caller id object itself.
     */
    public PhoneNumberIdentifier getAlternateCallerId() {
        return this.alternateCallerId;
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
     * The subject.
     */
    private String subject;

    /**
     * Get the subject.
     *
     * @return the subject value.
     */
    public String getSubject() {
        return this.subject;
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
     * The callback URI.
     */
    private final String callbackUri;

    /**
     * Get the subject.
     *
     * @return the subject value.
     */
    public String getCallbackUri() {
        return this.callbackUri;
    }

    /**
     * The requested modalities.
     */
    private final CallModality[] requestedModalities;

    /**
     * Get the requested modalities.
     *
     * @return the requested modalities object itself.
     */
    public CallModality[] getRequestedModalities() {
        return this.requestedModalities == null ? new CallModality[0] : this.requestedModalities.clone();
    }

    /**
     * The requested call events to subscribe to.
     */
    private final EventSubscriptionType[] requestedCallEvents;

    /**
     * Get the requested call events to subscribe to.
     *
     * @return the requested call events to subscribe to object itself.
     */
    public EventSubscriptionType[] getRequestedCallEvents() {
        return this.requestedCallEvents.clone();
    }

    /**
     * Initializes a new instance of CreateCallResult.
     *
     * @param callbackUri the callback URI.
     * @param requestedModalities the requested modalities.
     * @param requestedCallEvents the requested call events to subscribe to.
     * @throws IllegalArgumentException if any parameters are null.
     */
    public CreateCallOptions(String callbackUri,
                             CallModality[] requestedModalities,
                             EventSubscriptionType[] requestedCallEvents) {
        if (callbackUri == null) {
            throw new IllegalArgumentException("object callbackUri cannot be null");
        }

        if (requestedModalities == null) {
            throw new IllegalArgumentException("object requestedModalities cannot be null");
        }
        if (requestedCallEvents == null) {
            throw new IllegalArgumentException("object requestedCallEvents cannot be null");
        }

        this.callbackUri = callbackUri;

        this.requestedModalities = requestedModalities.clone();
        this.requestedCallEvents = requestedCallEvents.clone();
    }
}
