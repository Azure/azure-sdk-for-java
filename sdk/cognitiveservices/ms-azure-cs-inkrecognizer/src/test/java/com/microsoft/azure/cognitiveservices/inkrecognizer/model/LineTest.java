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

public class LineTest {

    private Line line;
    private ObjectNode jsonLine;
    private final Rectangle boundingRectangle = new Rectangle(138.369995, 17.980000, 37.570000, 28.090000);
    private final String category = "line";
    private final long[] children = new long[]{9, 10};
    private final String classString = "container";
    private final int id = 8;
    private final int parent = 7;
    private final String recognizedText = "12";
    private final float[][] rotatedBoundingRectangle = new float[][]{{138.289993f, 18.010000f}, {175.710007f, 17.709999f}, {175.940002f, 45.840000f}, {138.520004f, 46.139999f}};
    private final long[] strokes = new long[]{201, 202, 199, 200};
    private final String[] alternateRecognizedString = {", 2", "I 2", "l 2", ". 2", "1 2", "| 2", "I 2", ", .", "l ."};

    @Before
    public void setUp() {

        JsonNodeFactory factory = JsonNodeFactory.instance;
        jsonLine = factory.objectNode();

        jsonLine.put(TestUtils.CATEGORY, category)
            .put(TestUtils.CLASS_TYPE, classString)
            .put(TestUtils.ID, id)
            .put(TestUtils.PARENT_ID, parent)
            .put(TestUtils.RECOGNIZED_TEXT, recognizedText);
        jsonLine.set(TestUtils.BOUNDING_RECTANGLE, TestUtils.addBoundingRectangle(boundingRectangle));
        jsonLine.set(TestUtils.ROTATED_BOUNDING_RECTANGLE, TestUtils.addPointsArray(rotatedBoundingRectangle));
        jsonLine.set(TestUtils.CHILD_IDS, TestUtils.addIds(children));
        jsonLine.set(TestUtils.STROKE_IDS, TestUtils.addIds(strokes));
        jsonLine.set(TestUtils.ALTERNATES, TestUtils.addWritingAlternates(alternateRecognizedString, category));

    }

    @Test
    public void recognizedText() throws Exception {

        line = new Line(jsonLine, null);
        Assert.assertEquals(recognizedText, line.recognizedText());

    }

    @Test
    public void recognizedTextMissingTest() throws Exception {

        // When recognizedText is not set
        jsonLine.remove(TestUtils.RECOGNIZED_TEXT);
        line = new Line(jsonLine, null);
        Assert.assertEquals("", line.recognizedText());

    }

    @Test
    public void alternatesTest() throws Exception {

        line = new Line(jsonLine, null);
        Iterator<String> actualAlternatesIterator = line.alternates().iterator();
        int i;
        for (i = 0; i < alternateRecognizedString.length && actualAlternatesIterator.hasNext(); i++) {
            Assert.assertEquals(alternateRecognizedString[i], actualAlternatesIterator.next());
        }
        Assert.assertFalse(actualAlternatesIterator.hasNext());
        Assert.assertEquals(i, alternateRecognizedString.length);

    }

    @Test
    public void alternatesMissingTest() throws Exception {

        jsonLine.remove(TestUtils.ALTERNATES);
        Line line = new Line(jsonLine, null);
        Iterator<String> actualAlternatesIterator = line.alternates().iterator();
        Assert.assertFalse(actualAlternatesIterator.hasNext());

    }

    @Test
    public void alternateWrongFormatTest() throws Exception {

        jsonLine.remove(TestUtils.ALTERNATES);
        jsonLine.put(TestUtils.ALTERNATES, "no alternates");
        Line line = new Line(jsonLine, null);
        Iterator<String> actualAlternatesIterator = line.alternates().iterator();
        Assert.assertFalse(actualAlternatesIterator.hasNext());

    }

    @Test(expected = Exception.class)
    public void alternateMissingRecognizedTextTest() throws Exception {

        // RecognizedString is a required field in the server response
        JsonNode jsonAlternates = jsonLine.get(TestUtils.ALTERNATES).get(0);
        ((ObjectNode) jsonAlternates).remove(TestUtils.RECOGNIZED_STRING);
        line = new Line(jsonLine, null);

    }

    @After
    public void tearDown() {
        jsonLine = null;
        line = null;
    }

}
