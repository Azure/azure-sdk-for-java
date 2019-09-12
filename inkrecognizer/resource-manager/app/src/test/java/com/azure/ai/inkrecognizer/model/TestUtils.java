// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.nio.file.Files;

class TestUtils {

    static final float TOLERANCE = 1e-6f;
    static final String CATEGORY = "category";
    static final String CLASS_TYPE = "class";
    static final String ID = "id";
    static final String PARENT_ID = "parentId";
    static final String RECOGNIZED_TEXT = "recognizedText";
    static final String CHILD_IDS = "childIds";
    static final String BOUNDING_RECTANGLE = "boundingRectangle";
    static final String ROTATED_BOUNDING_RECTANGLE = "rotatedBoundingRectangle";
    static final String STROKE_IDS = "strokeIds";
    static final String CENTER = "center";
    static final String POINTS = "points";
    static final String ALTERNATES = "alternates";
    static final String CONFIDENCE = "confidence";
    static final String RECOGNIZED_OBJECT = "recognizedObject";
    static final String ROTATION_ANGLE = "rotationAngle";
    static final String RECOGNIZED_STRING = "recognizedString";

    static final String ALL_INK_RECOGNITION_UNIT_KINDS_RESPONSE_FILE = "AllInkRecognitionUnitKindsResponse.json";
    static final String MALFORMED_INK_RECOGNITION_UNITS_FILE = "MalformedInkRecognitionUnits.json";
    static final String MALFORMED_RESPONSE_MISSING_ID_FILE = "MalformedResponseMissingId.json";
    static final String MALFORMED_RESPONSE_MISSING_CATEGORY_FILE = "MalformedResponseMissingCategory.json";

    private static final JsonNodeFactory factory = JsonNodeFactory.instance;

    static final float XDPI = 2.0f;
    static final float YDPI = 3.0f;

    static ObjectNode addBoundingRectangle(Rectangle boundingRectangle) {
        return factory.objectNode().put("topX", boundingRectangle.x())
                        .put("topY", boundingRectangle.y())
                        .put("width", boundingRectangle.width())
                        .put("height", boundingRectangle.height());
    }

    static ArrayNode addIds(long[] ids) {
        ArrayNode jsonIds = factory.arrayNode();
        for(long id : ids) {
            jsonIds.add(id);
        }
        return jsonIds;
    }

    static ArrayNode addWritingAlternates(String[] alternates, String category) {
        ArrayNode jsonAlternates = factory.arrayNode();
        for(String alternate : alternates) {
            jsonAlternates.add(factory.objectNode().put("category", category)
                .put("recognizedString", alternate));
        }
        return jsonAlternates;
    }

    static ObjectNode addCenter(Point center) {
        return factory.objectNode().put("x", center.x())
                        .put("y", center.y());
    }

    static ArrayNode addPointsArray(float[][] points) {
        ArrayNode jsonPoints = factory.arrayNode();
        for (float[] point : points) {
            jsonPoints.add(factory.objectNode().put("x", point[0]).put("y", point[1]));
        }
        return jsonPoints;
    }

    static ArrayNode addAlternate(InkDrawingTest.Alternate alternate) {
        ArrayNode jsonAlternates = factory.arrayNode();
        ObjectNode jsonAlternate = factory.objectNode()
            .put("category", alternate.alternateCategory)
            .put("confidence", alternate.alternateConfidence)
            .put("rotationAngle", alternate.alternateRotationAngle)
            .put("recognizedString", alternate.alternateRecognizedString);
        jsonAlternate.set("points", TestUtils.addPointsArray(alternate.alternatePoints));
        jsonAlternates.add(jsonAlternate);
        return jsonAlternates;
    }

    static JsonNode getJsonRecognitionUnits(String fileName) throws Exception {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        String serverResponse = new String(Files.readAllBytes(file.toPath()));

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(serverResponse, JsonNode.class).get("recognitionUnits");
    }

}
