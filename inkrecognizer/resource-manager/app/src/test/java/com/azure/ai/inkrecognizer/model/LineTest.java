// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer.model;

import android.util.DisplayMetrics;
import com.azure.ai.inkrecognizer.InkPointUnit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.Iterator;

import static com.azure.ai.inkrecognizer.model.TestUtils.*;

public class LineTest {

    private Line line;
    private ObjectNode jsonLine;
    private final Rectangle boundingRectangle = new Rectangle(138.369995,17.980000,37.570000,28.090000);
    private final String category = "line";
    private final long[] children = new long[]{9,10};
    private final String classString = "container";
    private final int id = 8;
    private final int parent = 7;
    private final String recognizedText = "12";
    private final float[][] rotatedBoundingRectangle = new float[][]{{138.289993f, 18.010000f}, {175.710007f, 17.709999f}, {175.940002f, 45.840000f}, {138.520004f,46.139999f}};
    private final long[] strokes = new long[]{201, 202, 199, 200};
    private final String[] alternateRecognizedString = { ", 2", "I 2", "l 2", ". 2", "1 2", "| 2", "I 2", ", .", "l ." };
    private DisplayMetrics displayMetrics;

    @Before
    public void setUp() {

        displayMetrics = new DisplayMetrics();
        displayMetrics.xdpi = XDPI;
        displayMetrics.ydpi = YDPI;

        JsonNodeFactory factory = JsonNodeFactory.instance;
        jsonLine = factory.objectNode();

        jsonLine.put(CATEGORY, category)
                .put(CLASS_TYPE, classString)
                .put(ID, id)
                .put(PARENT_ID, parent)
                .put(RECOGNIZED_TEXT, recognizedText);
        jsonLine.set(BOUNDING_RECTANGLE, TestUtils.addBoundingRectangle(boundingRectangle));
        jsonLine.set(ROTATED_BOUNDING_RECTANGLE, TestUtils.addPointsArray(rotatedBoundingRectangle));
        jsonLine.set(CHILD_IDS, TestUtils.addIds(children));
        jsonLine.set(STROKE_IDS, TestUtils.addIds(strokes));
        jsonLine.set(ALTERNATES, TestUtils.addWritingAlternates(alternateRecognizedString, category));

    }

    @Test
    public void recognizedText() throws Exception {

        line = new Line(jsonLine, null, InkPointUnit.MM, displayMetrics);
        Assert.assertEquals(recognizedText, line.recognizedText());

    }

    @Test
    public void recognizedTextMissingTest() throws Exception {

        // When recognizedText is not set
        jsonLine.remove(RECOGNIZED_TEXT);
        line = new Line(jsonLine, null, InkPointUnit.MM, displayMetrics);
        Assert.assertEquals("", line.recognizedText());

    }

    @Test
    public void alternatesTest() throws Exception {

        line = new Line(jsonLine, null, InkPointUnit.MM, displayMetrics);
        Iterator<String> actualAlternatesIterator = line.alternates().iterator();
        int i;
        for (i=0 ; i<alternateRecognizedString.length && actualAlternatesIterator.hasNext(); i++) {
            Assert.assertEquals(alternateRecognizedString[i], actualAlternatesIterator.next());
        }
        Assert.assertFalse(actualAlternatesIterator.hasNext());
        Assert.assertEquals(i, alternateRecognizedString.length);

    }

    @Test
    public void alternatesMissingTest() throws Exception {

        jsonLine.remove(ALTERNATES);
        Line line = new Line(jsonLine, null, InkPointUnit.MM, displayMetrics);
        Iterator<String> actualAlternatesIterator = line.alternates().iterator();
        Assert.assertFalse(actualAlternatesIterator.hasNext());

    }

    @Test
    public void alternateWrongFormatTest() throws Exception {

        jsonLine.remove(ALTERNATES);
        jsonLine.put(ALTERNATES, "no alternates");
        Line line = new Line(jsonLine, null, InkPointUnit.MM, displayMetrics);
        Iterator<String> actualAlternatesIterator = line.alternates().iterator();
        Assert.assertFalse(actualAlternatesIterator.hasNext());

    }

    @Test(expected = Exception.class)
    public void alternateMissingRecognizedTextTest() throws Exception {

        // RecognizedString is a required field in the server response
        JsonNode jsonAlternates = jsonLine.get(ALTERNATES).get(0);
        ((ObjectNode)jsonAlternates).remove(RECOGNIZED_STRING);
        line = new Line(jsonLine, null, InkPointUnit.MM, displayMetrics);

    }

    @After
    public void tearDown() {
        jsonLine = null;
        line = null;
        displayMetrics = null;
    }

}
