// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for parsed grounding sources returned by Content Understanding.
 *
 * <p>The service encodes source positions as compact strings in the {@link ContentField#getSources()} property.
 * This class hierarchy parses those strings into strongly-typed objects:</p>
 * <ul>
 * <li>{@link DocumentSource} &mdash; {@code D(page,x1,y1,x2,y2,x3,y3,x4,y4)}</li>
 * <li>{@link AudioVisualSource} &mdash; {@code AV(time[,x,y,w,h])}</li>
 * </ul>
 *
 * <p>Use {@link DocumentSource#parse(String)} or {@link AudioVisualSource#parse(String)} to parse
 * a semicolon-delimited string containing one or more segments.</p>
 *
 * @see ContentField#getSources()
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
     * @param source The source string to parse.
     * @return A {@link ContentSource} subclass instance.
     * @throws NullPointerException if {@code source} is null.
     * @throws IllegalArgumentException if {@code source} is empty or has an unrecognized format.
     */
    static ContentSource parseSingle(String source) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        if (source.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'source' cannot be empty."));
        }
        if (source.startsWith("D(")) {
            return DocumentSource.parseSingle(source);
        }
        if (source.startsWith("AV(")) {
            return AudioVisualSource.parseSingle(source);
        }
        throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unrecognized source format: '" + source + "'."));
    }

    /**
     * Parses a semicolon-delimited string containing one or more source segments.
     *
     * <p>Each segment is parsed individually, detecting the source type automatically.</p>
     *
     * @param source The source string (may contain {@code ;} delimiters).
     * @return An unmodifiable list of {@link ContentSource} instances.
     * @throws NullPointerException if {@code source} is null.
     * @throws IllegalArgumentException if {@code source} is empty or any segment has an unrecognized format.
     */
    public static List<ContentSource> parseAll(String source) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        if (source.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'source' cannot be empty."));
        }
        String[] segments = source.split(";");
        List<ContentSource> results = new ArrayList<>(segments.length);
        for (String segment : segments) {
            String trimmed = segment.trim();
            if (!trimmed.isEmpty()) {
                results.add(parseSingle(trimmed));
            }
        }
        return Collections.unmodifiableList(results);
    }

    /**
     * Reconstructs the wire-format source string by joining each element's
     * {@link #getRawValue()} with semicolons.
     *
     * @param sources The content source list.
     * @return A semicolon-delimited string of raw source values.
     * @throws NullPointerException if {@code sources} is null.
     */
    public static String toRawString(List<? extends ContentSource> sources) {
        Objects.requireNonNull(sources, "'sources' cannot be null.");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sources.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(sources.get(i).getRawValue());
        }
        return sb.toString();
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
