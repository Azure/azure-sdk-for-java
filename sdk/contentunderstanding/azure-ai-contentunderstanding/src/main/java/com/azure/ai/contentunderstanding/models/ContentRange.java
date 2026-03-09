// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents a range of content to analyze. Use this type with the
 * {@link com.azure.ai.contentunderstanding.ContentUnderstandingClient#beginAnalyzeBinary(String, com.azure.core.util.BinaryData, ContentRange, String, ProcessingLocation)}
 * overload for a self-documenting API.
 *
 * <p>For documents, ranges use 1-based page numbers (e.g., {@code "1-3"}, {@code "5"}, {@code "9-"}).
 * For audio/video, ranges use integer milliseconds (e.g., {@code "0-5000"}, {@code "5000-"}).
 * Multiple ranges can be combined with commas (e.g., {@code "1-3,5,9-"}).</p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * // Document pages
 * ContentRange range = ContentRange.pages(1, 3);           // "1-3"
 * ContentRange single = ContentRange.page(5);              // "5"
 * ContentRange openEnd = ContentRange.pagesFrom(9);        // "9-"
 *
 * // Audio/video time ranges
 * ContentRange time = ContentRange.timeRange(
 *     Duration.ZERO, Duration.ofMillis(5000));              // "0-5000"
 * ContentRange timeOpen = ContentRange.timeRangeFrom(
 *     Duration.ofMillis(5000));                             // "5000-"
 *
 * // Combine multiple ranges
 * ContentRange combined = ContentRange.combine(
 *     ContentRange.pages(1, 3),
 *     ContentRange.page(5),
 *     ContentRange.pagesFrom(9));                           // "1-3,5,9-"
 *
 * // Or construct from a raw string
 * ContentRange raw = new ContentRange("1-3,5,9-");
 * }</pre>
 */
@Immutable
public final class ContentRange {
    private static final ClientLogger LOGGER = new ClientLogger(ContentRange.class);

    private final String value;

    /**
     * Initializes a new instance of {@link ContentRange} from a raw range string.
     *
     * @param value The range string value.
     * @throws NullPointerException if {@code value} is null.
     */
    public ContentRange(String value) {
        this.value = Objects.requireNonNull(value, "'value' cannot be null.");
    }

    /**
     * Creates a {@link ContentRange} for a single document page (1-based).
     *
     * @param pageNumber The 1-based page number.
     * @return A {@link ContentRange} representing the single page, e.g. {@code "5"}.
     * @throws IllegalArgumentException if {@code pageNumber} is less than 1.
     */
    public static ContentRange page(int pageNumber) {
        if (pageNumber < 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Page number must be >= 1."));
        }
        return new ContentRange(String.valueOf(pageNumber));
    }

    /**
     * Creates a {@link ContentRange} for a contiguous range of document pages (1-based, inclusive).
     *
     * @param start The 1-based start page number (inclusive).
     * @param end The 1-based end page number (inclusive).
     * @return A {@link ContentRange} representing the page range, e.g. {@code "1-3"}.
     * @throws IllegalArgumentException if {@code start} is less than 1, or {@code end} is less than {@code start}.
     */
    public static ContentRange pages(int start, int end) {
        if (start < 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Start page must be >= 1."));
        }
        if (end < start) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("End page must be >= start page."));
        }
        return new ContentRange(start + "-" + end);
    }

    /**
     * Creates a {@link ContentRange} for all document pages from a starting page to the end (1-based).
     *
     * @param startPage The 1-based start page number (inclusive).
     * @return A {@link ContentRange} representing the open-ended page range, e.g. {@code "9-"}.
     * @throws IllegalArgumentException if {@code startPage} is less than 1.
     */
    public static ContentRange pagesFrom(int startPage) {
        if (startPage < 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Start page must be >= 1."));
        }
        return new ContentRange(startPage + "-");
    }

    /**
     * Creates a {@link ContentRange} for a time range in milliseconds (for audio/video content).
     *
     * @param startMs The start time in milliseconds (inclusive).
     * @param endMs The end time in milliseconds (inclusive).
     * @return A {@link ContentRange} representing the time range, e.g. {@code "0-5000"}.
     * @throws IllegalArgumentException if {@code startMs} is negative, or {@code endMs} is less than {@code startMs}.
     */
    static ContentRange timeRange(long startMs, long endMs) {
        if (startMs < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Start time must be >= 0."));
        }
        if (endMs < startMs) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("End time must be >= start time."));
        }
        return new ContentRange(startMs + "-" + endMs);
    }

    /**
     * Creates a {@link ContentRange} for all content from a starting time to the end (for audio/video content).
     *
     * @param startMs The start time in milliseconds (inclusive).
     * @return A {@link ContentRange} representing the open-ended time range, e.g. {@code "5000-"}.
     * @throws IllegalArgumentException if {@code startMs} is negative.
     */
    static ContentRange timeRangeFrom(long startMs) {
        if (startMs < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Start time must be >= 0."));
        }
        return new ContentRange(startMs + "-");
    }

    /**
     * Creates a {@link ContentRange} for a time range using {@link Duration} values (for audio/video content).
     *
     * @param start The start time (inclusive).
     * @param end The end time (inclusive).
     * @return A {@link ContentRange} representing the time range, e.g. {@code "0-5000"}.
     * @throws NullPointerException if {@code start} or {@code end} is null.
     */
    public static ContentRange timeRange(Duration start, Duration end) {
        Objects.requireNonNull(start, "'start' cannot be null.");
        Objects.requireNonNull(end, "'end' cannot be null.");
        return timeRange(start.toMillis(), end.toMillis());
    }

    /**
     * Creates a {@link ContentRange} for all content from a starting time to the end using a {@link Duration} value.
     *
     * @param start The start time (inclusive).
     * @return A {@link ContentRange} representing the open-ended time range, e.g. {@code "5000-"}.
     * @throws NullPointerException if {@code start} is null.
     */
    public static ContentRange timeRangeFrom(Duration start) {
        Objects.requireNonNull(start, "'start' cannot be null.");
        return timeRangeFrom(start.toMillis());
    }

    /**
     * Combines multiple {@link ContentRange} values into a single comma-separated range.
     *
     * @param ranges The ranges to combine.
     * @return A {@link ContentRange} representing the combined ranges, e.g. {@code "1-3,5,9-"}.
     * @throws NullPointerException if {@code ranges} is null.
     * @throws IllegalArgumentException if {@code ranges} is empty.
     */
    public static ContentRange combine(ContentRange... ranges) {
        Objects.requireNonNull(ranges, "'ranges' cannot be null.");
        if (ranges.length == 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("At least one range must be provided."));
        }
        StringJoiner joiner = new StringJoiner(",");
        for (ContentRange range : ranges) {
            joiner.add(range.value);
        }
        return new ContentRange(joiner.toString());
    }

    /**
     * Returns the wire-format string representation of this range.
     *
     * @return The range string.
     */
    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ContentRange)) {
            return false;
        }
        ContentRange other = (ContentRange) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
