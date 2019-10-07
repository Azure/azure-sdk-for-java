// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The InkRecognitionUnit class represents a single entity recognized by the InkRecognizer service.
 * @author Microsoft
 * @version 1.0
 */
public class InkRecognitionUnit {

    private final Long id;
    private final InkRecognitionUnitKind category;
    private final List<Long> childIds;
    private final Long parentId;
    private final List<Long> strokeIds;
    private final Rectangle boundingBox;
    private final List<Point> rotatedBoundingBox = new ArrayList<>();
    private final InkRecognitionRoot root;

    InkRecognitionUnit(JsonNode jsonNode, InkRecognitionRoot root) throws Exception {

        this.root = root;

        try {

            id = jsonNode.get("id").asLong();
            category = InkRecognitionUnitKind.getInkRecognitionUnitKindOrDefault(jsonNode.get("category").asText());
            parentId = jsonNode.get("parentId").asLong();

            // Parse the strokes
            strokeIds = populateLongFromArray(jsonNode.get("strokeIds"));

            // Parse the children
            if (jsonNode.has("childIds") && jsonNode.get("childIds").isArray()) {
                childIds = populateLongFromArray(jsonNode.get("childIds"));
            } else {
                childIds = new ArrayList<>();
            }

            // Parse the boundingBox coordinates
            if (jsonNode.has("boundingRectangle")) {
                JsonNode jsonBoundingBox = jsonNode.get("boundingRectangle");
                boundingBox = new Rectangle(
                    jsonBoundingBox.get("topX").asDouble(),
                    jsonBoundingBox.get("topY").asDouble(),
                    jsonBoundingBox.get("width").asDouble(),
                    jsonBoundingBox.get("height").asDouble()
                );
            } else {
                boundingBox = new Rectangle();
            }

            // Parse the rotatedBoundingBox coordinates
            if (jsonNode.has("rotatedBoundingRectangle")) {
                JsonNode jsonRotatedBoundingBox = jsonNode.get("rotatedBoundingRectangle");
                for (final JsonNode coordinate : jsonRotatedBoundingBox) {
                    rotatedBoundingBox.add(
                        new Point(
                            coordinate.get("x").asDouble(),
                            coordinate.get("y").asDouble()
                        )
                    );
                }
            }

        } catch (Exception e) {
            throw new Exception("Error while parsing server response");
        }

    }

    /**
     * Retrieves the actual kind of InkRecognitionUnit (e.g INK_WORD etc.).
     * @return The kind of unit
     */
    public InkRecognitionUnitKind kind() {
        return category;
    }

    /**
     * Returns the stroke ids for the strokes that are part of the model unit
     * @return The stroke identifiers
     */
    public Iterable<Long> strokeIds() {
        return strokeIds;
    }

    /**
     * The children of a model unit represent the units contained in a container unit An example is the relationship
     * between a line and the words on the line. The words are the children of the line. "Leaf" units like words which
     * have no children will always return an empty list.
     * @return The children of the model unit
     */
    public Iterable<InkRecognitionUnit> children() {
        return root.recognitionUnitsByIds(childIds);
    }

    /**
     * The parent of a model unit is the unit containing current unit. An example is the relationship between a line and
     * the words on the line. The line is the parent of the words. A top level model unit will return null as the
     * parent.
     * @return The parent of the model unit
     */
    public InkRecognitionUnit parent() {
        return root.recognitionUnitById(parentId);
    }

    /**
     * The bounding box is the rectangular area that contains all the strokes in a model unit.
     * @return The bounding rectangle
     */
    public Rectangle boundingBox() {
        return boundingBox;
    }

    /**
     * The rotated bounding box is the oriented rectangular area that contains all the strokes in a model unit. Its
     * shape is influenced by the detected orientation of the ink in the model unit.
     * @return The coordinate points of the rotated bounding rectangle.
     */
    public Iterable<Point> rotatedBoundingBox() {
        return rotatedBoundingBox;
    }

    /**
     * Returns unique identifier for the recognition unit.
     * @return The identifier for the model unit.
     */
    public long id() {
        return id;
    }

    private List<Long> populateLongFromArray(JsonNode jsonArray) throws Exception {

        List<Long> result = new ArrayList<>();

        if (jsonArray == null || !jsonArray.isArray()) {
            throw new Exception("Error while parsing array");
        }

        for (final JsonNode jsonElement : jsonArray) {
            result.add(jsonElement.asLong());
        }

        return result;

    }


}
