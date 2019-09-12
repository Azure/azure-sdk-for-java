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

import static com.azure.ai.inkrecognizer.model.TestUtils.*;

public class InkBulletTest {

    private InkBullet inkBullet;
    private ObjectNode jsonInkBullet;
    private final Rectangle boundingRectangle = new Rectangle(138.369995,17.980000,37.570000,28.090000);
    private final String category = "inkBullet";
    private final String children = "none";
    private final String classString = "leaf";
    private final int id = 5;
    private final int parentId = 4;
    private final String recognizedText = "1)";
    private final float[][] rotatedBoundingRectangle = new float[][]{{138.289993f, 18.010000f}, {175.710007f, 17.709999f}, {175.940002f, 45.840000f}, {138.520004f,46.139999f}};
    private final long[] strokes = new long[]{215, 212, 218};
    private DisplayMetrics displayMetrics;

    @Before
    public void setUp() {

        displayMetrics = new DisplayMetrics();
        displayMetrics.xdpi = XDPI;
        displayMetrics.ydpi = YDPI;

        JsonNodeFactory factory = JsonNodeFactory.instance;
        jsonInkBullet = factory.objectNode();

        jsonInkBullet.put(CATEGORY, category)
            .put(CLASS_TYPE, classString)
            .put(ID, id)
            .put(PARENT_ID, parentId)
            .put(RECOGNIZED_TEXT, recognizedText)
            .put(CHILD_IDS, children);
        jsonInkBullet.set(BOUNDING_RECTANGLE, TestUtils.addBoundingRectangle(boundingRectangle));
        jsonInkBullet.set(ROTATED_BOUNDING_RECTANGLE, TestUtils.addPointsArray(rotatedBoundingRectangle));
        jsonInkBullet.set(STROKE_IDS, TestUtils.addIds(strokes));

    }

    @Test
    public void recognizedTextTest() throws Exception {
        inkBullet = new InkBullet(jsonInkBullet, null, InkPointUnit.MM, displayMetrics);
        Assert.assertEquals(recognizedText, inkBullet.recognizedText());
    }

    @Test
    public void recognizedTextMissingTest() throws Exception {

        // When recognizedText is not set
        jsonInkBullet.remove(RECOGNIZED_TEXT);
        inkBullet = new InkBullet(jsonInkBullet, null, InkPointUnit.MM, displayMetrics);
        Assert.assertEquals("", inkBullet.recognizedText());

    }

    @After
    public void tearDown() {
        jsonInkBullet = null;
        inkBullet = null;
        displayMetrics = null;
    }

}
