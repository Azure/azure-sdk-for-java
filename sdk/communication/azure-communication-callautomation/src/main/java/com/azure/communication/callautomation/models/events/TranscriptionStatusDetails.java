// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for TranscriptionStatusDetails.
 */
public final class TranscriptionStatusDetails extends ExpandableStringEnum<TranscriptionStatusDetails> {
    /**
     * Static value subscriptionStarted for TranscriptionStatusDetails.
     */
    public static final TranscriptionStatusDetails SUBSCRIPTION_STARTED = fromString("subscriptionStarted");

    /**
     * Static value streamConnectionReestablished for TranscriptionStatusDetails.
     */
    public static final TranscriptionStatusDetails STREAM_CONNECTION_REESTABLISHED
        = fromString("streamConnectionReestablished");

    /**
     * Static value streamConnectionUnsuccessful for TranscriptionStatusDetails.
     */
    public static final TranscriptionStatusDetails STREAM_CONNECTION_UNSUCCESSFUL
        = fromString("streamConnectionUnsuccessful");

    /**
     * Static value streamUrlMissing for TranscriptionStatusDetails.
     */
    public static final TranscriptionStatusDetails STREAM_URL_MISSING = fromString("streamUrlMissing");

    /**
     * Static value serviceShutdown for TranscriptionStatusDetails.
     */
    public static final TranscriptionStatusDetails SERVICE_SHUTDOWN = fromString("serviceShutdown");

    /**
     * Static value streamConnectionInterrupted for TranscriptionStatusDetails.
     */
    public static final TranscriptionStatusDetails STREAM_CONNECTION_INTERRUPTED
        = fromString("streamConnectionInterrupted");

    /**
     * Static value speechServicesConnectionError for TranscriptionStatusDetails.
     */
    public static final TranscriptionStatusDetails SPEECH_SERVICES_CONNECTION_ERROR
        = fromString("speechServicesConnectionError");

    /**
     * Static value subscriptionStopped for TranscriptionStatusDetails.
     */
    public static final TranscriptionStatusDetails SUBSCRIPTION_STOPPED = fromString("subscriptionStopped");

    /**
     * Static value unspecifiedError for TranscriptionStatusDetails.
     */
    public static final TranscriptionStatusDetails UNSPECIFIED_ERROR = fromString("unspecifiedError");

    /**
     * Creates a new instance of TranscriptionStatusDetails value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public TranscriptionStatusDetails() {
    }

    /**
     * Creates or finds a TranscriptionStatusDetails from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding TranscriptionStatusDetails.
     */
    public static TranscriptionStatusDetails fromString(String name) {
        return fromString(name, TranscriptionStatusDetails.class);
    }

    /**
     * Gets known TranscriptionStatusDetails values.
     *
     * @return known TranscriptionStatusDetails values.
     */
    public static Collection<TranscriptionStatusDetails> values() {
        return values(TranscriptionStatusDetails.class);
    }
}
