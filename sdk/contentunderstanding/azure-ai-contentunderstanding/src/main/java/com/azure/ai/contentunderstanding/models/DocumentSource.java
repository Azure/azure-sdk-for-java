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
 * Represents a parsed document grounding source in the format {@code D(page,x1,y1,x2,y2,x3,y3,x4,y4)}.
 *
 * <p>The page number is 1-based. The polygon is a quadrilateral defined by four points
 * with coordinates in the document's coordinate space.</p>
 *
 * @see ContentSource
 */
@Immutable
public final class DocumentSource extends ContentSource {
    private static final ClientLogger LOGGER = new ClientLogger(DocumentSource.class);
    private static final String PREFIX = "D(";
    private static final int EXPECTED_PARAM_COUNT = 9;

    private final int pageNumber;
    private final List<PointF> polygon;
    private final RectangleF boundingBox;

    private DocumentSource(String source) {
        super(source);
        if (!source.startsWith(PREFIX) || !source.endsWith(")")) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Document source must start with '" + PREFIX + "' and end with ')': '" + source + "'."));
        }
        String inner = source.substring(PREFIX.length(), source.length() - 1);
        String[] parts = inner.split(",");
        if (parts.length != EXPECTED_PARAM_COUNT) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("Document source expected " + EXPECTED_PARAM_COUNT
                    + " parameters (page + 8 coordinates), got " + parts.length + ": '" + source + "'."));
        }
        try {
            this.pageNumber = Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Invalid page number in document source: '" + parts[0] + "'.", e));
        }
        if (this.pageNumber < 1) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Page number must be >= 1, got " + this.pageNumber + "."));
        }
        List<PointF> points = new ArrayList<>(4);
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            int xIndex = 1 + (i * 2);
            int yIndex = 2 + (i * 2);
            float x, y;
            try {
                x = Float.parseFloat(parts[xIndex].trim());
            } catch (NumberFormatException e) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Invalid x-coordinate at index " + xIndex + ": '" + parts[xIndex] + "'.", e));
            }
            try {
                y = Float.parseFloat(parts[yIndex].trim());
            } catch (NumberFormatException e) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Invalid y-coordinate at index " + yIndex + ": '" + parts[yIndex] + "'.", e));
            }
            points.add(new PointF(x, y));
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        this.polygon = Collections.unmodifiableList(points);
        this.boundingBox = new RectangleF(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Gets the 1-based page number.
     *
     * @return The page number.
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Gets the polygon coordinates as four points defining a quadrilateral region.
     *
     * @return An unmodifiable list of four {@link PointF} values.
     */
    public List<PointF> getPolygon() {
        return polygon;
    }

    /**
     * Gets the axis-aligned bounding rectangle computed from the polygon coordinates.
     * Useful for drawing highlight rectangles over extracted fields.
     *
     * @return The bounding box.
     */
    public RectangleF getBoundingBox() {
        return boundingBox;
    }

    /**
     * Parses a single document source segment.
     *
     * @param source The source string in the format {@code D(page,x1,y1,...,x4,y4)}.
     * @return A new {@link DocumentSource}.
     * @throws NullPointerException if {@code source} is null.
     * @throws IllegalArgumentException if the source string is not in the expected format.
     */
    static DocumentSource parseSingle(String source) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        if (source.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'source' cannot be empty."));
        }
        return new DocumentSource(source);
    }

    /**
     * Parses a source string containing one or more document source segments separated by {@code ;}.
     *
     * @param source The source string (may contain {@code ;} delimiters).
     * @return An unmodifiable list of {@link DocumentSource} instances.
     * @throws NullPointerException if {@code source} is null.
     * @throws IllegalArgumentException if any segment is not in the expected format.
     */
    public static List<DocumentSource> parse(String source) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        if (source.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'source' cannot be empty."));
        }
        String[] segments = source.split(";");
        List<DocumentSource> results = new ArrayList<>(segments.length);
        for (String segment : segments) {
            String trimmed = segment.trim();
            if (!trimmed.isEmpty()) {
                results.add(new DocumentSource(trimmed));
            }
        }
        return Collections.unmodifiableList(results);
    }
}
