// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.models.AudioVisualSource;
import com.azure.ai.contentunderstanding.models.ContentSource;
import com.azure.ai.contentunderstanding.models.DocumentSource;
import com.azure.ai.contentunderstanding.models.PointF;
import com.azure.ai.contentunderstanding.models.Rectangle;
import com.azure.ai.contentunderstanding.models.RectangleF;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ContentSource}, {@link DocumentSource}, {@link AudioVisualSource},
 * and the geometry types {@link PointF}, {@link RectangleF}, {@link Rectangle}.
 */
public class ContentSourceTest {

    // =================== DocumentSource Parsing ===================

    @Test
    public void documentSourceParseSingleSegment() {
        List<DocumentSource> sources
            = DocumentSource.parse("D(1,0.5712,1.4062,2.1087,1.4088,2.1084,1.5762,0.5709,1.5736)");
        assertEquals(1, sources.size());
        DocumentSource source = sources.get(0);

        assertEquals(1, source.getPageNumber());
        assertEquals(4, source.getPolygon().size());
        assertPointApproximate(0.5712f, 1.4062f, source.getPolygon().get(0));
        assertPointApproximate(2.1087f, 1.4088f, source.getPolygon().get(1));
        assertPointApproximate(2.1084f, 1.5762f, source.getPolygon().get(2));
        assertPointApproximate(0.5709f, 1.5736f, source.getPolygon().get(3));
    }

    @Test
    public void documentSourceParsePageNumber() {
        List<DocumentSource> sources = DocumentSource.parse("D(3,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)");
        assertEquals(3, sources.get(0).getPageNumber());
    }

    @Test
    public void documentSourceParseRawValuePreserved() {
        String raw = "D(1,0.5712,1.4062,2.1087,1.4088,2.1084,1.5762,0.5709,1.5736)";
        List<DocumentSource> sources = DocumentSource.parse(raw);
        assertEquals(raw, sources.get(0).getRawValue());
        assertEquals(raw, sources.get(0).toString());
    }

    @Test
    public void documentSourceParseMultiRegion() {
        String input
            = "D(1,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0);D(1,2.0,2.0,3.0,2.0,3.0,3.0,2.0,3.0);D(2,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)";
        List<DocumentSource> sources = DocumentSource.parse(input);

        assertEquals(3, sources.size());
        assertEquals(1, sources.get(0).getPageNumber());
        assertEquals(1, sources.get(1).getPageNumber());
        assertEquals(2, sources.get(2).getPageNumber());
    }

    @Test
    public void documentSourceParseWrongPrefixThrows() {
        assertThrows(IllegalArgumentException.class, () -> DocumentSource.parse("AV(5000)"));
    }

    @Test
    public void documentSourceParseMalformedThrows() {
        assertThrows(IllegalArgumentException.class, () -> DocumentSource.parse("D(1,2,3)"));
    }

    @Test
    public void documentSourceParseNullThrows() {
        assertThrows(NullPointerException.class, () -> DocumentSource.parse(null));
    }

    @Test
    public void documentSourceParseEmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> DocumentSource.parse(""));
    }

    @Test
    public void documentSourceParseBoundingBoxComputed() {
        List<DocumentSource> sources
            = DocumentSource.parse("D(1,0.5712,1.4062,2.1087,1.4088,2.1084,1.5762,0.5709,1.5736)");
        DocumentSource source = sources.get(0);

        RectangleF bbox = source.getBoundingBox();
        // Min X = 0.5709, Min Y = 1.4062, Max X = 2.1087, Max Y = 1.5762
        assertEquals(0.5709f, bbox.getX(), 0.0001f);
        assertEquals(1.4062f, bbox.getY(), 0.0001f);
        assertEquals(2.1087f - 0.5709f, bbox.getWidth(), 0.001f);
        assertEquals(1.5762f - 1.4062f, bbox.getHeight(), 0.001f);
    }

    @Test
    public void documentSourceParseInvalidPageNumberThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> DocumentSource.parse("D(0,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)"));
    }

    // =================== AudioVisualSource Parsing ===================

    @Test
    public void audioVisualSourceParseWithBoundingBox() {
        List<AudioVisualSource> sources = AudioVisualSource.parse("AV(5000,100,200,50,60)");
        assertEquals(1, sources.size());
        AudioVisualSource source = sources.get(0);

        assertEquals(Duration.ofMillis(5000), source.getTime());
        assertNotNull(source.getBoundingBox());
        assertEquals(100, source.getBoundingBox().getX());
        assertEquals(200, source.getBoundingBox().getY());
        assertEquals(50, source.getBoundingBox().getWidth());
        assertEquals(60, source.getBoundingBox().getHeight());
    }

    @Test
    public void audioVisualSourceParseTimeOnly() {
        List<AudioVisualSource> sources = AudioVisualSource.parse("AV(5000)");
        AudioVisualSource source = sources.get(0);

        assertEquals(Duration.ofMillis(5000), source.getTime());
        assertNull(source.getBoundingBox());
    }

    @Test
    public void audioVisualSourceParseRawValuePreserved() {
        String raw = "AV(5000,100,200,50,60)";
        List<AudioVisualSource> sources = AudioVisualSource.parse(raw);
        assertEquals(raw, sources.get(0).getRawValue());
        assertEquals(raw, sources.get(0).toString());
    }

    @Test
    public void audioVisualSourceParseWrongPrefixThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> AudioVisualSource.parse("D(1,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)"));
    }

    @Test
    public void audioVisualSourceParseInvalidParamCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> AudioVisualSource.parse("AV(5000,100,200)"));
    }

    @Test
    public void audioVisualSourceParseNullThrows() {
        assertThrows(NullPointerException.class, () -> AudioVisualSource.parse(null));
    }

    @Test
    public void audioVisualSourceParseMultiSegment() {
        String input = "AV(0,100,200,50,60);AV(1000,105,205,50,60)";
        List<AudioVisualSource> sources = AudioVisualSource.parse(input);

        assertEquals(2, sources.size());
        assertEquals(Duration.ofMillis(0), sources.get(0).getTime());
        assertEquals(Duration.ofMillis(1000), sources.get(1).getTime());
    }

    @Test
    public void audioVisualSourceGetTimeDuration() {
        AudioVisualSource source = AudioVisualSource.parse("AV(5000)").get(0);
        assertEquals(Duration.ofMillis(5000), source.getTime());
    }

    @Test
    public void audioVisualSourceGetTimeZero() {
        AudioVisualSource source = AudioVisualSource.parse("AV(0)").get(0);
        assertEquals(Duration.ZERO, source.getTime());
    }

    // =================== ContentSource.toRawString ===================

    @Test
    public void contentSourceToRawStringSingleElement() {
        List<DocumentSource> sources = DocumentSource.parse("D(1,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)");
        assertEquals("D(1,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)", ContentSource.toRawString(sources));
    }

    @Test
    public void contentSourceToRawStringMultipleElements() {
        List<DocumentSource> sources
            = DocumentSource.parse("D(1,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0);D(2,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)");
        String result = ContentSource.toRawString(sources);
        assertEquals("D(1,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0);D(2,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)", result);
    }

    @Test
    public void contentSourceToRawStringNullThrows() {
        assertThrows(NullPointerException.class, () -> ContentSource.toRawString(null));
    }

    @Test
    public void contentSourceToRawStringEmptyList() {
        assertEquals("", ContentSource.toRawString(Arrays.asList()));
    }

    // =================== Geometry Types ===================

    @Test
    public void pointFEqualsAndHashCode() {
        PointF a = new PointF(1.5f, 2.5f);
        PointF b = new PointF(1.5f, 2.5f);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void pointFNotEquals() {
        PointF a = new PointF(1.5f, 2.5f);
        PointF b = new PointF(1.5f, 3.0f);
        assertNotEquals(a, b);
    }

    @Test
    public void pointFToString() {
        PointF p = new PointF(1.5f, 2.5f);
        assertEquals("(1.5, 2.5)", p.toString());
    }

    @Test
    public void rectangleFEqualsAndHashCode() {
        RectangleF a = new RectangleF(1.0f, 2.0f, 3.0f, 4.0f);
        RectangleF b = new RectangleF(1.0f, 2.0f, 3.0f, 4.0f);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void rectangleEqualsAndHashCode() {
        Rectangle a = new Rectangle(100, 200, 50, 60);
        Rectangle b = new Rectangle(100, 200, 50, 60);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void rectangleNotEquals() {
        Rectangle a = new Rectangle(100, 200, 50, 60);
        Rectangle b = new Rectangle(101, 200, 50, 60);
        assertNotEquals(a, b);
    }

    // =================== Real Recording Data ===================

    @Test
    public void documentSourceParseRealInvoiceSource() {
        List<DocumentSource> sources
            = DocumentSource.parse("D(1,0.5712,1.4062,2.1087,1.4088,2.1084,1.5762,0.5709,1.5736)");
        DocumentSource source = sources.get(0);

        assertEquals(1, source.getPageNumber());
        assertEquals(4, source.getPolygon().size());

        assertEquals(0.5712f, source.getPolygon().get(0).getX(), 0.0001f);
        assertEquals(1.4062f, source.getPolygon().get(0).getY(), 0.0001f);
        assertEquals(2.1087f, source.getPolygon().get(1).getX(), 0.0001f);
        assertEquals(1.4088f, source.getPolygon().get(1).getY(), 0.0001f);
        assertEquals(2.1084f, source.getPolygon().get(2).getX(), 0.0001f);
        assertEquals(1.5762f, source.getPolygon().get(2).getY(), 0.0001f);
        assertEquals(0.5709f, source.getPolygon().get(3).getX(), 0.0001f);
        assertEquals(1.5736f, source.getPolygon().get(3).getY(), 0.0001f);
    }

    // =================== Helpers ===================

    private static void assertPointApproximate(float expectedX, float expectedY, PointF actual) {
        assertEquals(expectedX, actual.getX(), 0.0001f, "X coordinate mismatch");
        assertEquals(expectedY, actual.getY(), 0.0001f, "Y coordinate mismatch");
    }
}
