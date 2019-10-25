// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static com.microsoft.azure.cognitiveservices.inkrecognizer.model.TestUtils.*;

public class InkDrawingTest {

    private InkDrawing inkDrawing;
    private ObjectNode jsonInkDrawing;
    private final Rectangle boundingRectangle = new Rectangle(138.369995, 17.980000, 37.570000, 28.090000);
    private final String category = "inkDrawing";
    private final String classString = "leaf";
    private final int id = 10;
    private final int parentId = 8;
    private final float[][] rotatedBoundingRectangle = new float[][]{{138.289993f, 18.010000f}, {175.710007f, 17.709999f}, {175.940002f, 45.840000f}, {138.520004f, 46.139999f}};
    private final long[] strokes = new long[]{201, 202};
    private final Point center = new Point(291.08999633789063, 131.6300048828125);
    private final float confidence = 1f;
    private final float rotationAngle = 0.009f;
    private final String shape = "starSimple";
    private final float[][] points = new float[][]{{251.6300048828125f, 85.169998168945313f}, {362.42999267578125f, 79.370002746582031f}, {259.22000122070313f, 230.35000610351563f}};

    private final Alternate alternate = new Alternate(
        "inkDrawing",
        0.8f,
        0.45f,
        new float[][]{{362.42999267578125f, 79.370002746582031f}, {251.6300048828125f, 185.169998168945313f}},
        "ellipse");

    @Before
    public void setUp() {

        JsonNodeFactory factory = JsonNodeFactory.instance;
        jsonInkDrawing = factory.objectNode();

        jsonInkDrawing.put(CATEGORY, category)
            .put(CLASS_TYPE, classString)
            .put(ID, id)
            .put(PARENT_ID, parentId)
            .put(CONFIDENCE, confidence)
            .put(RECOGNIZED_OBJECT, shape)
            .put(ROTATION_ANGLE, rotationAngle);
        jsonInkDrawing.set(BOUNDING_RECTANGLE, addBoundingRectangle(boundingRectangle));
        jsonInkDrawing.set(ROTATED_BOUNDING_RECTANGLE, addPointsArray(rotatedBoundingRectangle));
        jsonInkDrawing.set(STROKE_IDS, addIds(strokes));
        jsonInkDrawing.set(CENTER, addCenter(center));
        jsonInkDrawing.set(POINTS, addPointsArray(points));
        jsonInkDrawing.set(ALTERNATES, addAlternate(alternate));

    }

    @Test
    public void centerTest() throws Exception {

        inkDrawing = new InkDrawing(jsonInkDrawing, null);
        Assert.assertEquals(center.x(), inkDrawing.center().x(), TOLERANCE);
        Assert.assertEquals(center.y(), inkDrawing.center().y(), TOLERANCE);

    }

    @Test
    public void centerMissingTest() throws Exception {

        jsonInkDrawing.remove(CENTER);
        inkDrawing = new InkDrawing(jsonInkDrawing, null);
        Assert.assertEquals(0, inkDrawing.center().x(), TOLERANCE);
        Assert.assertEquals(0, inkDrawing.center().y(), TOLERANCE);

    }

    @Test
    public void confidenceTest() throws Exception {

        inkDrawing = new InkDrawing(jsonInkDrawing, null);
        Assert.assertEquals(confidence, inkDrawing.confidence(), TOLERANCE);

    }

    @Test
    public void confidenceMissingTest() throws Exception {

        jsonInkDrawing.remove(CONFIDENCE);
        inkDrawing = new InkDrawing(jsonInkDrawing, null);
        Assert.assertEquals(1.0f, inkDrawing.confidence(), TOLERANCE);

    }

    @Test
    public void recognizedShapeTest() throws Exception {

        inkDrawing = new InkDrawing(jsonInkDrawing, null);
        // Check if the Shape methods are working as expected
        Assert.assertEquals(Shape.getShapeOrDefault(shape), inkDrawing.recognizedShape());
//        Assert.assertEquals(shape, inkDrawing.recognizedShape().getString());
        Assert.assertEquals(shape, inkDrawing.recognizedShape().toString());
    }

    @Test
    public void recognizedShapeMissingTest() throws Exception {

        jsonInkDrawing.remove(RECOGNIZED_OBJECT);
        inkDrawing = new InkDrawing(jsonInkDrawing, null);
        Assert.assertEquals(Shape.DRAWING, inkDrawing.recognizedShape());

    }

    @Test
    public void rotationAngleTest() throws Exception {

        inkDrawing = new InkDrawing(jsonInkDrawing, null);
        Assert.assertEquals(rotationAngle, inkDrawing.rotationAngle(), TOLERANCE);

    }

    @Test
    public void rotationAngleMissingTest() throws Exception {

        jsonInkDrawing.remove(ROTATION_ANGLE);
        inkDrawing = new InkDrawing(jsonInkDrawing, null);
        Assert.assertEquals(0.0f, inkDrawing.rotationAngle(), TOLERANCE);

    }

    @Test
    public void pointsTest() throws Exception {

        inkDrawing = new InkDrawing(jsonInkDrawing, null);
        Iterator<Point> actualPointsIterator = inkDrawing.points().iterator();
        int i;
        for (i = 0; i < points.length && actualPointsIterator.hasNext(); i++) {
            Point point = actualPointsIterator.next();
            Assert.assertEquals(points[i][0], point.x(), TOLERANCE);
            Assert.assertEquals(points[i][1], point.y(), TOLERANCE);
        }
        Assert.assertFalse(actualPointsIterator.hasNext());
        Assert.assertEquals(i, points.length);

    }

    @Test
    public void pointsMissingTest() throws Exception {

        jsonInkDrawing.remove(POINTS);
        inkDrawing = new InkDrawing(jsonInkDrawing, null);
        Iterator<Point> actualPointsIterator = inkDrawing.points().iterator();
        Assert.assertFalse(actualPointsIterator.hasNext());

    }

    @Test
    public void alternatesTest() throws Exception {

        inkDrawing = new InkDrawing(jsonInkDrawing, null);
        Iterator<InkDrawing> actualAlternates = inkDrawing.alternates().iterator();

        Assert.assertTrue(actualAlternates.hasNext());

        InkDrawing actualAlternate = actualAlternates.next();
        Assert.assertEquals(center.x(), actualAlternate.center().x(), TOLERANCE);
        Assert.assertEquals(center.y(), actualAlternate.center().y(), TOLERANCE);
        Assert.assertEquals(alternate.alternateConfidence, actualAlternate.confidence(), TOLERANCE);
        Assert.assertEquals(alternate.alternateRecognizedString.toLowerCase(), actualAlternate.recognizedShape().toString().toLowerCase());
        Assert.assertEquals(alternate.alternateRotationAngle, actualAlternate.rotationAngle(), TOLERANCE);
        Assert.assertFalse(actualAlternate.alternates().iterator().hasNext());

        Iterator<Point> actualAlternatePointsIterator = actualAlternate.points().iterator();
        int i;
        for (i = 0; i < alternate.alternatePoints.length && actualAlternatePointsIterator.hasNext(); i++) {
            Point point = actualAlternatePointsIterator.next();
            Assert.assertEquals(alternate.alternatePoints[i][0], point.x(), TOLERANCE);
            Assert.assertEquals(alternate.alternatePoints[i][1], point.y(), TOLERANCE);
        }
        Assert.assertFalse(actualAlternatePointsIterator.hasNext());
        Assert.assertEquals(i, alternate.alternatePoints.length);

        Assert.assertFalse(actualAlternates.hasNext());

    }

    @Test(expected = Exception.class)
    public void alternatesCategoryMissing() throws Exception {

        // Category is a required field in the server response
        JsonNode jsonAlternates = jsonInkDrawing.get(ALTERNATES).get(0);
        ((ObjectNode) jsonAlternates).remove(CATEGORY);
        inkDrawing = new InkDrawing(jsonInkDrawing, null);

    }

    @Test
    public void alternatesPointsMissing() throws Exception {

        JsonNode jsonAlternates = jsonInkDrawing.get(ALTERNATES).get(0);
        ((ObjectNode) jsonAlternates).remove(POINTS);
        inkDrawing = new InkDrawing(jsonInkDrawing, null);

        Iterator<InkDrawing> actualAlternatesIterator = inkDrawing.alternates().iterator();
        InkDrawing actualAlternate = actualAlternatesIterator.next();
        Iterator<Point> actualPointsIterator = actualAlternate.points().iterator();
        Assert.assertFalse(actualPointsIterator.hasNext());

    }

    @Test
    public void alternateRotationAngleMissingTest() throws Exception {

        JsonNode jsonAlternates = jsonInkDrawing.get(ALTERNATES).get(0);
        ((ObjectNode) jsonAlternates).remove(ROTATION_ANGLE);
        inkDrawing = new InkDrawing(jsonInkDrawing, null);

        Iterator<InkDrawing> actualAlternatesIterator = inkDrawing.alternates().iterator();
        InkDrawing actualAlternate = actualAlternatesIterator.next();
        Assert.assertEquals(0.0f, actualAlternate.rotationAngle(), TOLERANCE);

    }

    @Test
    public void alternateConfidenceMissingTest() throws Exception {

        JsonNode jsonAlternates = jsonInkDrawing.get(ALTERNATES).get(0);
        ((ObjectNode) jsonAlternates).remove(CONFIDENCE);
        inkDrawing = new InkDrawing(jsonInkDrawing, null);

        Iterator<InkDrawing> actualAlternatesIterator = inkDrawing.alternates().iterator();
        InkDrawing actualAlternate = actualAlternatesIterator.next();
        Assert.assertEquals(1.0f, actualAlternate.confidence(), TOLERANCE);

    }

    @Test(expected = Exception.class)
    public void alternateRecognizedStringMissingTest() throws Exception {

        // RecognizedString is a required field in the server response
        JsonNode jsonAlternates = jsonInkDrawing.get(ALTERNATES).get(0);
        ((ObjectNode) jsonAlternates).remove(RECOGNIZED_STRING);
        inkDrawing = new InkDrawing(jsonInkDrawing, null);

    }

    @After
    public void tearDown() {
        jsonInkDrawing = null;
        inkDrawing = null;
    }

    class Alternate {

        final String alternateCategory;
        final float alternateConfidence;
        final float alternateRotationAngle;
        final float[][] alternatePoints;
        final String alternateRecognizedString;

        Alternate(
            String alternateCategory,
            float alternateConfidence,
            float alternateRotationAngle,
            float[][] alternatePoints,
            String alternateRecognizedString) {
            this.alternateCategory = alternateCategory;
            this.alternateConfidence = alternateConfidence;
            this.alternateRotationAngle = alternateRotationAngle;
            this.alternatePoints = alternatePoints;
            this.alternateRecognizedString = alternateRecognizedString;
        }

    }

}
