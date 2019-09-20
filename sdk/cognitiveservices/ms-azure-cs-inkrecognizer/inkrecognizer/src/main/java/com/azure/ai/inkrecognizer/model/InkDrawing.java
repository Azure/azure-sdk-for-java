/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.ai.inkrecognizer.model;

import android.util.DisplayMetrics;
import com.azure.ai.inkrecognizer.InkPointUnit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The InkShape class represents the collection of one
 * or more ink strokes that were recognized as a drawing/shape
 * @author Microsoft
 * @version 1.0
 */
public class InkDrawing extends InkRecognitionUnit {

    private final Point center;
    private final float confidence;
    private final Shape shape;
    private final float rotationAngle;
    private final List<InkDrawing> alternates = new ArrayList<>();
    private final List<Point> points = new ArrayList<>();

    InkDrawing(
            JsonNode drawingNode,
            InkRecognitionRoot root,
            InkPointUnit inkPointUnit,
            DisplayMetrics displayMetrics
    ) throws Exception {

        super(drawingNode, root, inkPointUnit, displayMetrics);

        try {

            center = drawingNode.has("center") ? populateCenter(drawingNode.get("center"), inkPointUnit, displayMetrics) : new Point();
            confidence = drawingNode.has("confidence") ? (float)drawingNode.get("confidence").asDouble() : 1.0f;
            shape = Shape.getShapeOrDefault(drawingNode.has("recognizedObject") ? drawingNode.get("recognizedObject").asText() : null);
            rotationAngle = drawingNode.has("rotationAngle") ? (float)drawingNode.get("rotationAngle").asDouble() : 0.0f;
            populatePoints(drawingNode.get("points"), inkPointUnit, displayMetrics);

            // Parse the alternates
            if (drawingNode.has("alternates")) {

                JsonNode alternates = drawingNode.get("alternates");

                for (JsonNode alternate : alternates) {

                    if (alternate.get("category").asText().equals("inkDrawing")) {

                        JsonNode alternateDrawing = drawingNode.deepCopy();

                        if (alternate.has("confidence")) {
                            ((ObjectNode)alternateDrawing).put("confidence", alternate.get("confidence").asDouble());
                        } else {
                            ((ObjectNode)alternateDrawing).remove("confidence");
                        }

                        if (alternate.has("rotationAngle")) {
                            ((ObjectNode)alternateDrawing).put("rotationAngle", alternate.get("rotationAngle").asDouble());
                        } else {
                            ((ObjectNode)alternateDrawing).remove("rotationAngle");
                        }

                        if (alternate.has("points")) {
                            ((ObjectNode)alternateDrawing).set("points", alternate.get("points"));
                        } else {
                            ((ObjectNode)alternateDrawing).remove("points");
                        }

                        ((ObjectNode)alternateDrawing).put("recognizedObject", alternate.get("recognizedString").asText());
                        ((ObjectNode)alternateDrawing).remove("alternates");

                        this.alternates.add(new InkDrawing(alternateDrawing, root, inkPointUnit, displayMetrics));

                    }

                }

            }

        } catch (Exception e) {
            throw new Exception("Error while parsing server response");
        }

    }

    /**
     * Retrieves the center point of the bounding rectangle of the unit.
     * @return The point object representing the center.
     */
    public Point center() {
        return center;
    }

    /**
     * Retrieves a number between 0 and 1 which indicates the confidence level in the result.
     * @return A number between 0 and 1.
     */
    public float confidence() {
        return confidence;
    }

    /**
     * Retrieves the shape that was recognized. If the drawing isn't one of the known geometric shapes, the value DRAWING is returned.
     * @return The Shape enum representing the geometric shape that was recognized.
     */
    public Shape recognizedShape() {
        return shape;
    }

    /**
     * Retrieves the angular orientation of an object relative to the horizontal axis.
     * @return A number representing the rotation angle.
     */
    public float rotationAngle() {
        return rotationAngle;
    }

    /**
     * Retrieves the alternates to the shape reported as the recognized shape when the confidence isn't 1.
     * @return A collection of InkDrawing objects.
     */
    public Iterable<InkDrawing> alternates() {
        return alternates;
    }

    /**
     * Array of point objects that represent points that are relevant to the type of shape. For example,
     * for a triangle, points would include the x,y coordinates of
     * the vertices of the recognized triangle. The points represent the coordinates of points used to create the
     * perfectly drawn shape that is closest to the original input. They may not exactly match.
     * @return Array of point objects
     */
    public Iterable<Point> points() {
        return points;
    }

    private Point populateCenter(JsonNode jsonCenter, InkPointUnit inkPointUnit, DisplayMetrics displayMetrics) {
        return new Point(
                Utils.translatePoints(jsonCenter.get("x").asDouble(), inkPointUnit, displayMetrics),
                Utils.translatePoints(jsonCenter.get("y").asDouble(), inkPointUnit, displayMetrics)
        );
    }

    private void populatePoints(JsonNode jsonPoints, InkPointUnit inkPointUnit, DisplayMetrics displayMetrics) {
        if (jsonPoints != null) {
            for (JsonNode jsonPoint : jsonPoints) {
                points.add(
                        new Point(
                                Utils.translatePoints(jsonPoint.get("x").asDouble(), inkPointUnit, displayMetrics),
                                Utils.translatePoints(jsonPoint.get("y").asDouble(), inkPointUnit, displayMetrics)
                        )
                );
            }
        }
    }

}
