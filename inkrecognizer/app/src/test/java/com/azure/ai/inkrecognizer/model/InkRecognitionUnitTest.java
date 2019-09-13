// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer.model;

import android.util.DisplayMetrics;
import com.azure.ai.inkrecognizer.InkPointUnit;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static com.azure.ai.inkrecognizer.model.TestUtils.*;

public class InkRecognitionUnitTest {

    private InkRecognitionUnit unit;
    private ObjectNode jsonUnit;
    private final Rectangle boundingRectangle = new Rectangle(138.369995, 17.980000, 37.570000, 28.090000);
    private final String category = "root";
    private final long[] children = new long[]{1, 2};
    private final String classString = "container";
    private final int id = 0;
    private final int parent = -1;
    private final float[][] rotatedBoundingRectangle = new float[][]{{138.289993f, 18.010000f}, {175.710007f, 17.709999f}, {175.940002f, 45.840000f}, {138.520004f, 46.139999f}};
    private final long[] strokes = new long[]{200, 201, 197, 198, 199, 202, 212, 215, 218};
    private DisplayMetrics displayMetrics;

    @Before
    public void setUp() {

        displayMetrics = new DisplayMetrics();
        displayMetrics.xdpi = XDPI;
        displayMetrics.ydpi = YDPI;

        JsonNodeFactory factory = JsonNodeFactory.instance;
        jsonUnit = factory.objectNode();

        jsonUnit.put(CATEGORY, category)
                .put(CLASS_TYPE, classString)
                .put(ID, id)
                .put(PARENT_ID, parent);
        jsonUnit.set(BOUNDING_RECTANGLE, TestUtils.addBoundingRectangle(boundingRectangle));
        jsonUnit.set(ROTATED_BOUNDING_RECTANGLE, TestUtils.addPointsArray(rotatedBoundingRectangle));
        jsonUnit.set(CHILD_IDS, TestUtils.addIds(children));
        jsonUnit.set(STROKE_IDS, TestUtils.addIds(strokes));

    }

    @Test
    public void kindTest() throws Exception {

        unit = new InkRecognitionUnit(jsonUnit, null, InkPointUnit.MM, displayMetrics);
        Assert.assertEquals(InkRecognitionUnitKind.getInkRecognitionUnitKindOrDefault(category), unit.kind());

    }

    @Test(expected = Exception.class)
    public void kindMissingTest() throws Exception {

        // Category is a required field in the server response
        jsonUnit.remove(CATEGORY);
        unit = new InkRecognitionUnit(jsonUnit, null, InkPointUnit.MM, displayMetrics);

    }

    @Test
    public void strokeIdsTest() throws Exception {

        unit = new InkRecognitionUnit(jsonUnit, null, InkPointUnit.MM, displayMetrics);
        Iterator<Long> actualStrokesIterator = unit.strokeIds().iterator();
        int i;
        for (i = 0; i < strokes.length && actualStrokesIterator.hasNext(); i++) {
            Assert.assertEquals(strokes[i], actualStrokesIterator.next().longValue());
        }
        Assert.assertFalse(actualStrokesIterator.hasNext());
        Assert.assertEquals(i, strokes.length);

    }

    @Test(expected = Exception.class)
    public void strokeIdsMissingTest() throws Exception {

        jsonUnit.remove(STROKE_IDS);
        unit = new InkRecognitionUnit(jsonUnit, null, InkPointUnit.MM, displayMetrics);

    }

    @Test
    public void boundingBoxTest() throws Exception {

        unit = new InkRecognitionUnit(jsonUnit, null, InkPointUnit.MM, displayMetrics);
        Rectangle actualBoundingBox = unit.boundingBox();
        Assert.assertEquals(boundingRectangle.x(), actualBoundingBox.x(), TestUtils.TOLERANCE);
        Assert.assertEquals(boundingRectangle.y(), actualBoundingBox.y(), TestUtils.TOLERANCE);
        Assert.assertEquals(boundingRectangle.width(), actualBoundingBox.width(), TestUtils.TOLERANCE);
        Assert.assertEquals(boundingRectangle.height(), actualBoundingBox.height(), TestUtils.TOLERANCE);

    }

    @Test
    public void boundingBoxMissingTest() throws Exception {

        jsonUnit.remove("boundingRectangle");
        unit = new InkRecognitionUnit(jsonUnit, null, InkPointUnit.MM, displayMetrics);
        Rectangle actualBoundingBox = unit.boundingBox();
        Assert.assertEquals(0.0f, actualBoundingBox.x(), TestUtils.TOLERANCE);
        Assert.assertEquals(0.0f, actualBoundingBox.y(), TestUtils.TOLERANCE);
        Assert.assertEquals(0.0f, actualBoundingBox.width(), TestUtils.TOLERANCE);
        Assert.assertEquals(0.0f, actualBoundingBox.height(), TestUtils.TOLERANCE);

    }

    @Test
    public void rotatedBoundingRectangleTest() throws Exception {

        unit = new InkRecognitionUnit(jsonUnit, null, InkPointUnit.MM, displayMetrics);
        Iterator<Point> actualRotatedBoundingBoxIterator = unit.rotatedBoundingBox().iterator();
        int expectedRotatedBoundingBoxIndex;
        for (expectedRotatedBoundingBoxIndex = 0; actualRotatedBoundingBoxIterator.hasNext(); expectedRotatedBoundingBoxIndex++) {
            Point actualRotatedBoundingBox = actualRotatedBoundingBoxIterator.next();
            Assert.assertEquals(rotatedBoundingRectangle[expectedRotatedBoundingBoxIndex][0], actualRotatedBoundingBox.x(), TestUtils.TOLERANCE);
            Assert.assertEquals(rotatedBoundingRectangle[expectedRotatedBoundingBoxIndex][1], actualRotatedBoundingBox.y(), TestUtils.TOLERANCE);
        }
        Assert.assertEquals(4, expectedRotatedBoundingBoxIndex);

    }

    @Test
    public void rotatedBoundingRectangleMissingTest() throws Exception {

        jsonUnit.remove("rotatedBoundingRectangle");
        unit = new InkRecognitionUnit(jsonUnit, null, InkPointUnit.MM, displayMetrics);
        Iterator<Point> actualRotatedBoundingBoxIterator = unit.rotatedBoundingBox().iterator();
        Assert.assertFalse(actualRotatedBoundingBoxIterator.hasNext());

    }

    @Test
    public void idTest() throws Exception {

        unit = new InkRecognitionUnit(jsonUnit, null, InkPointUnit.MM, displayMetrics);
        Assert.assertEquals(id, unit.id());

    }

    @Test(expected = Exception.class)
    public void idMissingTest() throws Exception {

        jsonUnit.remove("id");
        unit = new InkRecognitionUnit(jsonUnit, null, InkPointUnit.MM, displayMetrics);

    }

    @Test(expected = Exception.class)
    public void badObjectTest() throws Exception {

        unit = new InkRecognitionUnit(null, null, InkPointUnit.MM, displayMetrics);

    }

    @After
    public void tearDown() {
        jsonUnit = null;
        unit = null;
        displayMetrics = null;
    }

}
