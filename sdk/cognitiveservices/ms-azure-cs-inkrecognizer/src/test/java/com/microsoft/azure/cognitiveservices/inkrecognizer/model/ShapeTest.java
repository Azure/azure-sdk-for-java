// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer.model;

import org.junit.Assert;
import org.junit.Test;

// One test suite such as this is representative of all other enums
public class ShapeTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidShapeTest() {
        Shape shape = Shape.valueOf("invalid");
    }

    @Test(expected = NullPointerException.class)
    public void nullShapeTest() {
        Shape shape = Shape.valueOf(null);
    }

    @Test
    public void invalidShapeDefaultTest() {
        Shape shape = Shape.getShapeOrDefault("invalid");
        Assert.assertEquals(Shape.DRAWING, shape);
    }

    @Test
    public void nullShapeDefaultTest() {
        Shape shape = Shape.getShapeOrDefault(null);
        Assert.assertEquals(Shape.DRAWING, shape);
    }

}
