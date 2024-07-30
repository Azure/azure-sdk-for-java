// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for MediaStreamingStatus. */
public final class MediaStreamingStatus extends ExpandableStringEnum<MediaStreamingStatus> {
    /** Static value mediaStreamingStarted for MediaStreamingStatus. */
    public static final MediaStreamingStatus MEDIA_STREAMING_STARTED = fromString("mediaStreamingStarted");

    /** Static value mediaStreamingFailed for MediaStreamingStatus. */
    public static final MediaStreamingStatus MEDIA_STREAMING_FAILED = fromString("mediaStreamingFailed");

    /** Static value mediaStreamingStopped for MediaStreamingStatus. */
    public static final MediaStreamingStatus MEDIA_STREAMING_STOPPED = fromString("mediaStreamingStopped");

    /** Static value unspecifiedError for MediaStreamingStatus. */
    public static final MediaStreamingStatus UNSPECIFIED_ERROR = fromString("unspecifiedError");

    /**
     * Creates or finds a MediaStreamingStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding MediaStreamingStatus.
     */
    public static MediaStreamingStatus fromString(String name) {
        return fromString(name, MediaStreamingStatus.class);
    }

    /** @return known MediaStreamingStatus values. */
    public static Collection<MediaStreamingStatus> values() {
        return values(MediaStreamingStatus.class);
    }
}
