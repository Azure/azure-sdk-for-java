// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static com.azure.ai.inkrecognizer.model.TestUtils.*;

public class InkWordTest {

    private InkWord inkWord;
    private ObjectNode jsonInkWord;
    private final Rectangle boundingRectangle = new Rectangle(138.369995, 17.980000, 37.570000, 28.090000);
    private final String category = "inkWord";
    private final String children = "none";
    private final String classString = "leaf";
    private final int id = 10;
    private final int parent = 8;
    private final String recognizedText = "12";
    private final float[][] rotatedBoundingRectangle = new float[][]{{138.289993f, 18.010000f}, {175.710007f, 17.709999f}, {175.940002f, 45.840000f}, {138.520004f, 46.139999f}};
    private final long[] strokes = new long[]{201, 202};
    private final String[] alternateRecognizedString = {", 2", "I 2", "l 2", ". 2", "1 2", "| 2", "I 2", ", .", "l ."};

    @Before
    public void setUp() {

        JsonNodeFactory factory = JsonNodeFactory.instance;
        jsonInkWord = factory.objectNode();

        jsonInkWord.put(CATEGORY, category)
            .put(CLASS_TYPE, classString)
            .put(ID, id)
            .put(PARENT_ID, parent)
            .put(RECOGNIZED_TEXT, recognizedText)
            .put(CHILD_IDS, children);
        jsonInkWord.set(BOUNDING_RECTANGLE, TestUtils.addBoundingRectangle(boundingRectangle));
        jsonInkWord.set(ROTATED_BOUNDING_RECTANGLE, TestUtils.addPointsArray(rotatedBoundingRectangle));
        jsonInkWord.set(STROKE_IDS, TestUtils.addIds(strokes));
        jsonInkWord.set(ALTERNATES, TestUtils.addWritingAlternates(alternateRecognizedString, category));

    }

    @Test
    public void recognizedText() throws Exception {

        inkWord = new InkWord(jsonInkWord, null);
        Assert.assertEquals(recognizedText, inkWord.recognizedText());
    }

    @Test
    public void recognizedTextMissingTest() throws Exception {

        // When recognizedText is not set
        jsonInkWord.remove(RECOGNIZED_TEXT);
        inkWord = new InkWord(jsonInkWord, null);
        Assert.assertEquals("", inkWord.recognizedText());

    }

    @Test
    public void alternatesTest() throws Exception {

        inkWord = new InkWord(jsonInkWord, null);
        Iterator<String> actualAlternatesIterator = inkWord.alternates().iterator();
        int i;
        for (i = 0; i < alternateRecognizedString.length && actualAlternatesIterator.hasNext(); i++) {
            Assert.assertEquals(alternateRecognizedString[i], actualAlternatesIterator.next());
        }
        Assert.assertFalse(actualAlternatesIterator.hasNext());
        Assert.assertEquals(i, alternateRecognizedString.length);

    }

    @Test
    public void alternatesMissingTest() throws Exception {

        jsonInkWord.remove(ALTERNATES);
        InkWord inkWord = new InkWord(jsonInkWord, null);
        Iterator<String> actualAlternatesIterator = inkWord.alternates().iterator();
        Assert.assertFalse(actualAlternatesIterator.hasNext());

    }

    @Test
    public void alternateWrongFormatTest() throws Exception {

        jsonInkWord.remove(ALTERNATES);
        jsonInkWord.put(ALTERNATES, "no alternates");
        InkWord inkWord = new InkWord(jsonInkWord, null);
        Iterator<String> actualAlternatesIterator = inkWord.alternates().iterator();
        Assert.assertFalse(actualAlternatesIterator.hasNext());

    }

    @Test(expected = Exception.class)
    public void alternateMissingRecognizedTextTest() throws Exception {

        // RecognizedString is a required field in the server response
        JsonNode jsonAlternates = jsonInkWord.get(ALTERNATES).get(0);
        ((ObjectNode) jsonAlternates).remove(RECOGNIZED_STRING);
        inkWord = new InkWord(jsonInkWord, null);

    }

    @After
    public void tearDown() {
        jsonInkWord = null;
        inkWord = null;
    }

}
