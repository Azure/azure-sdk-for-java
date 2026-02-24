// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.models.AudioVisualSource;
import com.azure.ai.contentunderstanding.models.ContentSource;
import com.azure.ai.contentunderstanding.models.DocumentSource;
import com.azure.ai.contentunderstanding.models.PointF;
import com.azure.ai.contentunderstanding.models.Rectangle;
import com.azure.ai.contentunderstanding.models.RectangleF;
import com.azure.ai.contentunderstanding.models.TrackletSource;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ContentSource}, {@link DocumentSource}, {@link AudioVisualSource},
 * {@link TrackletSource}, and the geometry types {@link PointF}, {@link RectangleF}, {@link Rectangle}.
 */
public class ContentSourceTest {

    // =================== DocumentSource Parsing ===================

    @Test
    public void documentSourceParseSingleSegment() {
        DocumentSource source = DocumentSource.parse("D(1,0.5712,1.4062,2.1087,1.4088,2.1084,1.5762,0.5709,1.5736)");

        assertEquals(1, source.getPageNumber());
        assertEquals(4, source.getPolygon().size());
        assertPointApproximate(0.5712f, 1.4062f, source.getPolygon().get(0));
        assertPointApproximate(2.1087f, 1.4088f, source.getPolygon().get(1));
        assertPointApproximate(2.1084f, 1.5762f, source.getPolygon().get(2));
        assertPointApproximate(0.5709f, 1.5736f, source.getPolygon().get(3));
    }

    @Test
    public void documentSourceParsePageNumber() {
        DocumentSource source = DocumentSource.parse("D(3,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)");
        assertEquals(3, source.getPageNumber());
    }

    @Test
    public void documentSourceParseRawValuePreserved() {
        String raw = "D(1,0.5712,1.4062,2.1087,1.4088,2.1084,1.5762,0.5709,1.5736)";
        DocumentSource source = DocumentSource.parse(raw);
        assertEquals(raw, source.getRawValue());
        assertEquals(raw, source.toString());
    }

    @Test
    public void documentSourceParseAllMultiRegion() {
        String input
            = "D(1,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0);D(1,2.0,2.0,3.0,2.0,3.0,3.0,2.0,3.0);D(2,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)";
        DocumentSource[] sources = DocumentSource.parseAll(input);

        assertEquals(3, sources.length);
        assertEquals(1, sources[0].getPageNumber());
        assertEquals(1, sources[1].getPageNumber());
        assertEquals(2, sources[2].getPageNumber());
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
        DocumentSource source = DocumentSource.parse("D(1,0.5712,1.4062,2.1087,1.4088,2.1084,1.5762,0.5709,1.5736)");

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
        AudioVisualSource source = AudioVisualSource.parse("AV(5000,100,200,50,60)");

        assertEquals(5000, source.getTimeMs());
        assertNotNull(source.getBoundingBox());
        assertEquals(100, source.getBoundingBox().getX());
        assertEquals(200, source.getBoundingBox().getY());
        assertEquals(50, source.getBoundingBox().getWidth());
        assertEquals(60, source.getBoundingBox().getHeight());
    }

    @Test
    public void audioVisualSourceParseTimeOnly() {
        AudioVisualSource source = AudioVisualSource.parse("AV(5000)");

        assertEquals(5000, source.getTimeMs());
        assertNull(source.getBoundingBox());
    }

    @Test
    public void audioVisualSourceParseRawValuePreserved() {
        String raw = "AV(5000,100,200,50,60)";
        AudioVisualSource source = AudioVisualSource.parse(raw);
        assertEquals(raw, source.getRawValue());
        assertEquals(raw, source.toString());
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
    public void audioVisualSourceParseAllMultiSegment() {
        String input = "AV(0,100,200,50,60);AV(1000,105,205,50,60)";
        AudioVisualSource[] sources = AudioVisualSource.parseAll(input);

        assertEquals(2, sources.length);
        assertEquals(0, sources[0].getTimeMs());
        assertEquals(1000, sources[1].getTimeMs());
    }

    @Test
    public void audioVisualSourceGetTimeDuration() {
        AudioVisualSource source = AudioVisualSource.parse("AV(5000)");
        assertEquals(Duration.ofMillis(5000), source.getTime());
    }

    @Test
    public void audioVisualSourceGetTimeZero() {
        AudioVisualSource source = AudioVisualSource.parse("AV(0)");
        assertEquals(Duration.ZERO, source.getTime());
    }

    // =================== TrackletSource Parsing ===================

    @Test
    public void trackletSourceParseSplitsPair() {
        TrackletSource tracklet = TrackletSource.parse("AV(0,100,200,50,60)-AV(1000,105,205,50,60)");

        assertEquals(0, tracklet.getStart().getTimeMs());
        assertEquals(100, tracklet.getStart().getBoundingBox().getX());
        assertEquals(1000, tracklet.getEnd().getTimeMs());
        assertEquals(105, tracklet.getEnd().getBoundingBox().getX());
    }

    @Test
    public void trackletSourceParsePreservesRawValue() {
        String raw = "AV(0,100,200,50,60)-AV(1000,105,205,50,60)";
        TrackletSource tracklet = TrackletSource.parse(raw);
        assertEquals(raw, tracklet.getRawValue());
        assertEquals(raw, tracklet.toString());
    }

    @Test
    public void trackletSourceParseInvalidFormatThrows() {
        assertThrows(IllegalArgumentException.class, () -> TrackletSource.parse("AV(5000)"));
    }

    @Test
    public void trackletSourceParseNullThrows() {
        assertThrows(NullPointerException.class, () -> TrackletSource.parse(null));
    }

    // =================== ContentSource.parse Dispatch ===================

    @Test
    public void contentSourceParseDocumentPrefix() {
        ContentSource source = ContentSource.parse("D(1,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)");
        assertInstanceOf(DocumentSource.class, source);
    }

    @Test
    public void contentSourceParseAudioVisualPrefix() {
        ContentSource source = ContentSource.parse("AV(5000,100,200,50,60)");
        assertInstanceOf(AudioVisualSource.class, source);
    }

    @Test
    public void contentSourceParseTrackletPair() {
        ContentSource source = ContentSource.parse("AV(0,100,200,50,60)-AV(1000,105,205,50,60)");
        assertInstanceOf(TrackletSource.class, source);

        TrackletSource tracklet = (TrackletSource) source;
        assertEquals(0, tracklet.getStart().getTimeMs());
        assertEquals(1000, tracklet.getEnd().getTimeMs());
    }

    @Test
    public void contentSourceParseUnknownPrefixThrows() {
        assertThrows(IllegalArgumentException.class, () -> ContentSource.parse("R(1,2,3)"));
    }

    @Test
    public void contentSourceParseNullThrows() {
        assertThrows(NullPointerException.class, () -> ContentSource.parse(null));
    }

    // =================== ContentSource.parseAll ===================

    @Test
    public void contentSourceParseAllMultiRegionDocument() {
        String input = "D(1,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0);D(2,0.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0)";
        ContentSource[] sources = ContentSource.parseAll(input);

        assertEquals(2, sources.length);
        assertInstanceOf(DocumentSource.class, sources[0]);
        assertInstanceOf(DocumentSource.class, sources[1]);
    }

    @Test
    public void contentSourceParseAllSingleSegment() {
        ContentSource[] sources = ContentSource.parseAll("AV(5000)");
        assertEquals(1, sources.length);
        assertInstanceOf(AudioVisualSource.class, sources[0]);
    }

    @Test
    public void contentSourceParseAllMultiTracklet() {
        String input = "AV(0,100,200,50,60)-AV(1000,105,205,50,60);AV(5000,200,180,50,60)-AV(7000,210,190,50,60)";
        ContentSource[] sources = ContentSource.parseAll(input);

        assertEquals(2, sources.length);
        assertInstanceOf(TrackletSource.class, sources[0]);
        assertInstanceOf(TrackletSource.class, sources[1]);

        TrackletSource t1 = (TrackletSource) sources[0];
        TrackletSource t2 = (TrackletSource) sources[1];
        assertEquals(0, t1.getStart().getTimeMs());
        assertEquals(1000, t1.getEnd().getTimeMs());
        assertEquals(5000, t2.getStart().getTimeMs());
        assertEquals(7000, t2.getEnd().getTimeMs());
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
        DocumentSource source = DocumentSource.parse("D(1,0.5712,1.4062,2.1087,1.4088,2.1084,1.5762,0.5709,1.5736)");

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
