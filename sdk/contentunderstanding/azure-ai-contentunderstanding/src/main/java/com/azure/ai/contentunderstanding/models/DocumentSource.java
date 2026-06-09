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
 * Represents a parsed document grounding source in the format {@code D(page,x1,y1,...,xN,yN)}
 * or {@code D(page)} when only a page number is available.
 *
 * <p>The page number is 1-based. When coordinates are present, the polygon defines a region
 * with three or more points in the document's coordinate space. When only a page number is
 * provided (no coordinates), {@link #getPolygon()} and {@link #getBoundingBox()} return
 * {@code null}.</p>
 *
 * @see ContentSource
 */
@Immutable
public final class DocumentSource extends ContentSource {
    private static final ClientLogger LOGGER = new ClientLogger(DocumentSource.class);
    private static final String PREFIX = "D(";

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

        if (parts.length == 1) {
            // Page-only form: D(page)
            this.polygon = null;
            this.boundingBox = null;
            return;
        }

        int coordCount = parts.length - 1;
        if (coordCount < 6 || coordCount % 2 != 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Document source expected page-only (1 param) or page + at least 3 coordinate pairs (7+ params), got "
                    + parts.length + ": '" + source + "'."));
        }

        int pointCount = coordCount / 2;
        List<PointF> points = new ArrayList<>(pointCount);
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int i = 0; i < pointCount; i++) {
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
     * Gets the polygon coordinates defining the region, or {@code null} when only a page
     * number is available (i.e., the source was in the form {@code D(page)}).
     *
     * @return An unmodifiable list of {@link PointF} values, or {@code null}.
     */
    public List<PointF> getPolygon() {
        return polygon;
    }

    /**
     * Gets the axis-aligned bounding rectangle computed from the polygon coordinates,
     * or {@code null} when no polygon is available.
     * Useful for drawing highlight rectangles over extracted fields.
     *
     * @return The bounding box, or {@code null}.
     */
    public RectangleF getBoundingBox() {
        return boundingBox;
    }

    /**
     * Parses a single document source segment.
     *
     * @param source The source string in the format {@code D(page,x1,y1,...,xN,yN)} or {@code D(page)}.
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
