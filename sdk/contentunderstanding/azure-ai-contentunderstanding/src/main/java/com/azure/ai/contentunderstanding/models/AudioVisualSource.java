// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a parsed audio/visual grounding source in the format {@code AV(time[,x,y,w,h])}.
 *
 * <p>The time is in milliseconds. The bounding box (x, y, width, height) is optional and
 * present only when spatial information is available (e.g., face detection).</p>
 *
 * @see ContentSource
 */
@Immutable
public final class AudioVisualSource extends ContentSource {
    private static final ClientLogger LOGGER = new ClientLogger(AudioVisualSource.class);
    private static final String PREFIX = "AV(";

    private final int timeMs;
    private final Rectangle boundingBox;

    AudioVisualSource(String source) {
        super(source);
        if (!source.startsWith(PREFIX) || !source.endsWith(")")) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Audio/visual source must start with '" + PREFIX + "' and end with ')': '" + source + "'."));
        }
        String inner = source.substring(PREFIX.length(), source.length() - 1);
        String[] parts = inner.split(",");
        if (parts.length != 1 && parts.length != 5) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Audio/visual source expected 1 or 5 parameters, got " + parts.length + ": '" + source + "'."));
        }
        try {
            this.timeMs = Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Invalid time value in audio/visual source: '" + parts[0] + "'.", e));
        }
        if (parts.length == 5) {
            int xVal, yVal, wVal, hVal;
            try {
                xVal = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                throw LOGGER
                    .logExceptionAsError(new IllegalArgumentException("Invalid x value: '" + parts[1] + "'.", e));
            }
            try {
                yVal = Integer.parseInt(parts[2].trim());
            } catch (NumberFormatException e) {
                throw LOGGER
                    .logExceptionAsError(new IllegalArgumentException("Invalid y value: '" + parts[2] + "'.", e));
            }
            try {
                wVal = Integer.parseInt(parts[3].trim());
            } catch (NumberFormatException e) {
                throw LOGGER
                    .logExceptionAsError(new IllegalArgumentException("Invalid width value: '" + parts[3] + "'.", e));
            }
            try {
                hVal = Integer.parseInt(parts[4].trim());
            } catch (NumberFormatException e) {
                throw LOGGER
                    .logExceptionAsError(new IllegalArgumentException("Invalid height value: '" + parts[4] + "'.", e));
            }
            this.boundingBox = new Rectangle(xVal, yVal, wVal, hVal);
        } else {
            this.boundingBox = null;
        }
    }

    /**
     * Gets the time as a Duration.
     *
     * @return The time as a Duration.
     */
    public Duration getTime() {
        return Duration.ofMillis(timeMs);
    }

    /**
     * Gets the bounding box in pixel coordinates, or {@code null} if no spatial information
     * is available (e.g., audio-only).
     *
     * @return The bounding box, or {@code null}.
     */
    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    /**
     * Parses a single audio/visual source segment.
     *
     * @param source The source string in the format {@code AV(time[,x,y,w,h])}.
     * @return A new {@link AudioVisualSource}.
     * @throws NullPointerException if {@code source} is null.
     * @throws IllegalArgumentException if the source string is not in the expected format.
     */
    static AudioVisualSource parseSingle(String source) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        if (source.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'source' cannot be empty."));
        }
        return new AudioVisualSource(source);
    }

    /**
     * Parses a source string containing one or more audio/visual source segments separated by {@code ;}.
     *
     * @param source The source string (may contain {@code ;} delimiters).
     * @return An unmodifiable list of {@link AudioVisualSource} instances.
     * @throws NullPointerException if {@code source} is null.
     * @throws IllegalArgumentException if any segment is not in the expected format.
     */
    public static List<AudioVisualSource> parse(String source) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        if (source.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'source' cannot be empty."));
        }
        String[] segments = source.split(";");
        List<AudioVisualSource> results = new ArrayList<>(segments.length);
        for (String segment : segments) {
            String trimmed = segment.trim();
            if (!trimmed.isEmpty()) {
                results.add(new AudioVisualSource(trimmed));
            }
        }
        return Collections.unmodifiableList(results);
    }
}
