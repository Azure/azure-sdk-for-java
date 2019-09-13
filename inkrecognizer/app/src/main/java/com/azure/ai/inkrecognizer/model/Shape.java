// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The Shape enum represents different shapes that can be reported by the ink recognizer
 * service. Any unrecognized shape will be reported as "DRAWING"
 * @author Microsoft
 * @version 1.0
 */
public enum Shape {

    /**
     * An unidentified drawing.
     */
    DRAWING("drawing"),

    /**
     * A square.
     */
    SQUARE("square"),

    /**
     * A rectangle.
     */
    RECTANGLE("rectangle"),

    /**
     * A circle.
     */
    CIRCLE("circle"),

    /**
     * An ellipse.
     */
    ELLIPSE("ellipse"),

    /**
     * A triangle.
     */
    TRIANGLE("triangle"),

    /**
     * An isosceles triangle.
     */
    ISOSCELES_TRIANGLE("isoscelesTriangle"),

    /**
     * An equilateral triangle.
     */
    EQUILATERAL_TRIANGLE("equilateralTriangle"),

    /**
     * A right triangle.
     */
    RIGHT_TRIANGLE("rightTriangle"),

    /**
     * A quadrilateral.
     */
    QUADRILATERAL("quadrilateral"),

    /**
     * A diamond.
     */
    DIAMOND("diamond"),

    /**
     * A trapezoid.
     */
    TRAPEZOID("trapezoid"),

    /**
     * A parallelogram.
     */
    PARALLELOGRAM("parallelogram"),

    /**
     * A pentagon.
     */
    PENTAGON("pentagon"),

    /**
     * A hexagon.
     */
    HEXAGON("hexagon"),

    /**
     * A block arrow (like arrow used in flow charts).
     */
    BLOCK_ARROW("blockArrow"),

    /**
     * A heart.
     */
    HEART("heart"),

    /**
     * A simple star.
     */
    STAR_SIMPLE("starSimple"),

    /**
     * A more complicated star drawing with intersecting lines.
     */
    STAR_CROSSED("starCrossed"),

    /**
     * A cloud.
     */
    CLOUD("cloud"),

    /**
     * A geometric line.
     */
    LINE("line"),

    /**
     * A curve.
     */
    CURVE("curve"),

    /**
     * A polyline.
     */
    POLYLINE("polyline");

    private String shapeString;
    private static final Map<String, Shape> map = new HashMap<>();

    static {
        for (Shape shape : Shape.values()) {
            map.put(shape.toString(), shape);
        }
    }

    Shape(String shapeString) {
        this.shapeString = shapeString;
    }

    static Shape getShapeOrDefault(String shapeString) {
        if (map.containsKey(shapeString)) {
            return map.get(shapeString);
        } else {
            return Shape.DRAWING;
        }
    }

    @Override
    public String toString() {
        return shapeString;
    }

}
