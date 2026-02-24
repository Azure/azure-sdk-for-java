// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for parsed grounding sources returned by Content Understanding.
 *
 * <p>The service encodes source positions as compact strings in the {@link ContentField#getSource()} property.
 * This class hierarchy parses those strings into strongly-typed objects:</p>
 * <ul>
 * <li>{@link DocumentSource} &mdash; {@code D(page,x1,y1,x2,y2,x3,y3,x4,y4)}</li>
 * <li>{@link AudioVisualSource} &mdash; {@code AV(time[,x,y,w,h])}</li>
 * <li>{@link TrackletSource} &mdash; {@code AV(...)-AV(...)}</li>
 * </ul>
 *
 * <p>Use {@link #parse(String)} to parse a single segment, or {@link #parseAll(String)} to parse
 * a semicolon-delimited string containing multiple segments.</p>
 *
 * @see ContentField#getGroundingSources()
 */
@Immutable
public abstract class ContentSource {
    private static final ClientLogger LOGGER = new ClientLogger(ContentSource.class);

    private final String rawValue;

    /**
     * Initializes a new instance of {@link ContentSource}.
     *
     * @param rawValue The raw wire-format source string.
     */
    protected ContentSource(String rawValue) {
        this.rawValue = Objects.requireNonNull(rawValue, "'rawValue' cannot be null.");
    }

    /**
     * Gets the original wire-format source string.
     *
     * @return The raw source string.
     */
    public String getRawValue() {
        return rawValue;
    }

    /**
     * Parses a single source segment, automatically detecting the source type.
     *
     * <p>Tracklet pairs ({@code AV(...)-AV(...)}) are automatically detected and returned
     * as {@link TrackletSource} instances.</p>
     *
     * @param source The source string to parse.
     * @return A {@link ContentSource} subclass instance.
     * @throws NullPointerException if {@code source} is null.
     * @throws IllegalArgumentException if {@code source} is empty or has an unrecognized format.
     */
    public static ContentSource parse(String source) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        if (source.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'source' cannot be empty."));
        }
        if (source.startsWith("D(")) {
            return DocumentSource.parse(source);
        }
        if (source.startsWith("AV(")) {
            if (source.indexOf(")-AV(") >= 0) {
                return TrackletSource.parse(source);
            }
            return AudioVisualSource.parse(source);
        }
        throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unrecognized source format: '" + source + "'."));
    }

    /**
     * Parses a semicolon-delimited string containing one or more source segments.
     *
     * <p>Each segment is parsed individually using {@link #parse(String)}.
     * Tracklet pairs within segments are automatically detected.</p>
     *
     * @param source The source string (may contain {@code ;} delimiters).
     * @return An array of {@link ContentSource} instances.
     * @throws NullPointerException if {@code source} is null.
     * @throws IllegalArgumentException if {@code source} is empty or any segment has an unrecognized format.
     */
    public static ContentSource[] parseAll(String source) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        if (source.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'source' cannot be empty."));
        }
        String[] segments = source.split(";");
        List<ContentSource> results = new ArrayList<>(segments.length);
        for (String segment : segments) {
            String trimmed = segment.trim();
            if (!trimmed.isEmpty()) {
                results.add(parse(trimmed));
            }
        }
        return results.toArray(new ContentSource[0]);
    }

    /**
     * Returns the wire-format string representation of this source.
     *
     * @return The raw source string.
     */
    @Override
    public String toString() {
        return rawValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ContentSource)) {
            return false;
        }
        ContentSource other = (ContentSource) obj;
        return Objects.equals(rawValue, other.rawValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rawValue);
    }
}
