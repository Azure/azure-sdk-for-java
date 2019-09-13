// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

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
    static private final ObjectMapper objectMapper = new ObjectMapper();

    static final float XDPI = 2.0f;
    static final float YDPI = 3.0f;

    static Iterable<InkStroke> loadStrokesFromJSON(String fileName) throws Exception {

        ClassLoader classLoader = TestUtils.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        String request = new String(Files.readAllBytes(file.toPath()));
        JsonNode jsonRequest = objectMapper.readValue(request, JsonNode.class);

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

    static private List<InkPoint> loadPoints(JsonNode jsonPoints) {
        List<InkPoint> inkPoints = new ArrayList<>();

        // Process array
        if (jsonPoints.isArray()) {
            for (JsonNode jsonPoint : jsonPoints) {
                inkPoints.add(new InkPointImplementor().setX(jsonPoint.get("x").asDouble())
                        .setY(jsonPoint.get("y").asDouble()));
            }
        }
        // or process string
        else {
            StringTokenizer st = new StringTokenizer(jsonPoints.asText(), ",");
            while (st.hasMoreTokens()) {
                inkPoints.add(new InkPointImplementor().setX(Float.parseFloat(st.nextToken()))
                        .setY(Float.parseFloat(st.nextToken())));
            }
        }

        return inkPoints;
    }


}
