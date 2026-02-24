// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import java.util.Objects;

/**
 * Represents a face tracklet &mdash; a continuous track of a detected face across consecutive video frames.
 * Encoded as {@code AV(startTime,x,y,w,h)-AV(endTime,x,y,w,h)} in the wire format.
 *
 * <p>A tracklet captures where a face appeared at the start and end of one continuous appearance.
 * The {@code -} separator joins the start/end frames within a single tracklet.
 * Multiple tracklets for the same person (reappearing after being absent) are separated by {@code ;}
 * and returned as separate {@link TrackletSource} elements in the
 * {@link ContentField#getGroundingSources()} array.</p>
 *
 * @see ContentSource
 * @see AudioVisualSource
 */
@Immutable
public final class TrackletSource extends ContentSource {
    private static final ClientLogger LOGGER = new ClientLogger(TrackletSource.class);
    private static final String SEPARATOR = ")-AV(";

    private final AudioVisualSource start;
    private final AudioVisualSource end;

    private TrackletSource(String source) {
        super(source);
        int separatorIndex = source.indexOf(SEPARATOR);
        if (separatorIndex < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Expected a tracklet pair in the format 'AV(...)-AV(...)': '" + source + "'."));
        }
        String first = source.substring(0, separatorIndex + 1);
        String second = source.substring(separatorIndex + 2);
        this.start = new AudioVisualSource(first);
        this.end = new AudioVisualSource(second);
    }

    /**
     * Gets the audio/visual source at the start of the tracklet.
     *
     * @return The start source.
     */
    public AudioVisualSource getStart() {
        return start;
    }

    /**
     * Gets the audio/visual source at the end of the tracklet.
     *
     * @return The end source.
     */
    public AudioVisualSource getEnd() {
        return end;
    }

    /**
     * Parses a tracklet pair string.
     *
     * @param source The source string in the format {@code AV(...)-AV(...)}.
     * @return A new {@link TrackletSource}.
     * @throws NullPointerException if {@code source} is null.
     * @throws IllegalArgumentException if the source string is not a valid tracklet pair.
     */
    public static TrackletSource parse(String source) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        if (source.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'source' cannot be empty."));
        }
        return new TrackletSource(source);
    }
}
