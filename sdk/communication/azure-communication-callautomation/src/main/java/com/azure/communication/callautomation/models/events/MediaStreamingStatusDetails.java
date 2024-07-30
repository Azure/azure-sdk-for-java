// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for MediaStreamingStatusDetails. */
public final class MediaStreamingStatusDetails extends ExpandableStringEnum<MediaStreamingStatusDetails> {
    /** Static value subscriptionStarted for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails SUBSCRIPTION_STARTED = fromString("subscriptionStarted");

    /** Static value streamConnectionReestablished for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails STREAM_CONNECTION_REESTABLISHED =
            fromString("streamConnectionReestablished");

    /** Static value streamConnectionUnsuccessful for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails STREAM_CONNECTION_UNSUCCESSFUL =
            fromString("streamConnectionUnsuccessful");

    /** Static value streamUrlMissing for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails STREAM_URL_MISSING = fromString("streamUrlMissing");

    /** Static value serviceShutdown for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails SERVICE_SHUTDOWN = fromString("serviceShutdown");

    /** Static value streamConnectionInterrupted for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails STREAM_CONNECTION_INTERRUPTED =
            fromString("streamConnectionInterrupted");

    /** Static value speechServicesConnectionError for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails SPEECH_SERVICES_CONNECTION_ERROR =
            fromString("speechServicesConnectionError");

    /** Static value subscriptionStopped for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails SUBSCRIPTION_STOPPED = fromString("subscriptionStopped");

    /** Static value unspecifiedError for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails UNSPECIFIED_ERROR = fromString("unspecifiedError");

    /** Static value authenticationFailure for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails AUTHENTICATION_FAILURE = fromString("authenticationFailure");

    /** Static value badRequest for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails BAD_REQUEST = fromString("badRequest");

    /** Static value tooManyRequests for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails TOO_MANY_REQUESTS = fromString("tooManyRequests");

    /** Static value forbidden for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails FORBIDDEN = fromString("forbidden");

    /** Static value serviceTimeout for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails SERVICE_TIMEOUT = fromString("serviceTimeout");

    /** Static value initialWebSocketConnectionFailed for MediaStreamingStatusDetails. */
    public static final MediaStreamingStatusDetails INITIAL_WEB_SOCKET_CONNECTION_FAILED =
            fromString("initialWebSocketConnectionFailed");

    /**
     * Creates or finds a MediaStreamingStatusDetails from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding MediaStreamingStatusDetails.
     */
    public static MediaStreamingStatusDetails fromString(String name) {
        return fromString(name, MediaStreamingStatusDetails.class);
    }

    /** @return known MediaStreamingStatusDetails values. */
    public static Collection<MediaStreamingStatusDetails> values() {
        return values(MediaStreamingStatusDetails.class);
    }
}
