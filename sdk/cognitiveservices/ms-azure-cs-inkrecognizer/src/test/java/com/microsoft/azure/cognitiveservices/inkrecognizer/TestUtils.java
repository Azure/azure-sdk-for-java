// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

class TestUtils {

    static final String TWO_STROKES_REQUEST_FILE = "TwoStrokesRequest.json";
    static final String ALL_INK_RECOGNITION_UNIT_KINDS_REQUEST_FILE = "AllInkRecognitionUnitKindsRequest.json";
    static final String CIRCLE_REQUEST_FILE = "CircleRequest.json";
    static final String[] FILES = {TWO_STROKES_REQUEST_FILE, CIRCLE_REQUEST_FILE};
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static Iterable<InkStroke> loadStrokesFromJSON(String fileName) throws Exception {

        ClassLoader classLoader = TestUtils.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        String request = new String(Files.readAllBytes(file.toPath()));
        JsonNode jsonRequest = OBJECT_MAPPER.readValue(request, JsonNode.class);

        JsonNode jsonStrokes = jsonRequest.get("strokes");
        List<InkStroke> inkStrokes = new ArrayList<>();

        for (JsonNode jsonStroke : jsonStrokes) {

            InkStrokeImplementor inkStrokeImplementor = new InkStrokeImplementor();

            if (jsonStroke.has("points")) {
                inkStrokeImplementor.setInkPoints(loadPoints(jsonStroke.get("points")));
            }

            if (jsonStroke.has("kind")) {
                inkStrokeImplementor.setInkStrokeKind(jsonStroke.get("kind").asText());
            }

            if (jsonStroke.has("id")) {
                inkStrokeImplementor.setId(jsonStroke.get("id").asLong());
            }

            if (jsonStroke.has("language")) {
                inkStrokeImplementor.setLanguage(jsonStroke.get("language").asText());
            }

            inkStrokes.add(inkStrokeImplementor);

        }

        return inkStrokes;

    }

    private static List<InkPoint> loadPoints(JsonNode jsonPoints) {
        List<InkPoint> inkPoints = new ArrayList<>();

        // Process array
        if (jsonPoints.isArray()) {
            for (JsonNode jsonPoint : jsonPoints) {
                inkPoints.add(new InkPointImplementor().setX(jsonPoint.get("x").asDouble())
                    .setY(jsonPoint.get("y").asDouble()));
            }
        } else {
            // or process string
            StringTokenizer st = new StringTokenizer(jsonPoints.asText(), ",");
            while (st.hasMoreTokens()) {
                inkPoints.add(new InkPointImplementor().setX(Float.parseFloat(st.nextToken()))
                    .setY(Float.parseFloat(st.nextToken())));
            }
        }

        return inkPoints;
    }


}
