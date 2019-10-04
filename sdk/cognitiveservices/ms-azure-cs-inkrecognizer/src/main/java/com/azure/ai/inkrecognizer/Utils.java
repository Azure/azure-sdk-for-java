package com.azure.ai.inkrecognizer;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;

class Utils {

    static <T> T getValueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    static String createJSONForRequest(
            Iterable<InkStroke> strokes,
            InkPointUnit unit,
            float multiple,
            ApplicationKind applicationKind,
            String language
    ) throws Exception {

        Iterator strokesIterator = strokes.iterator();

        if (
                strokesIterator.hasNext()
                        && unit != null
                        && language != null
                        && applicationKind != null
        ) {

            JsonNodeFactory factory = JsonNodeFactory.instance;
            ObjectNode jsonAnalysisRequest = factory.objectNode();

            ArrayNode jsonStrokes = factory.arrayNode();

            while (strokesIterator.hasNext()) {

                InkStroke stroke = (InkStroke) strokesIterator.next();
                ObjectNode jsonStroke = factory.objectNode();

                jsonStroke.put("id", stroke.getId());

                if (stroke.getLanguage() != null) {
                    jsonStroke.put("language", stroke.getLanguage());
                }

                if (stroke.getKind() != null && stroke.getKind() != InkStrokeKind.UNKNOWN) {
                    jsonStroke.put("kind", stroke.getKind().toString());
                }

                Iterator pointsIterator = stroke.getInkPoints().iterator();
                ArrayNode jsonPoints = factory.arrayNode();

                while (pointsIterator.hasNext()) {
                    InkPoint inkPoint = (InkPoint) pointsIterator.next();
                    jsonPoints.add(
                            factory.objectNode()
                                    .put("x", inkPoint.getX())
                                    .put("y", inkPoint.getY())
                    );
                }

                jsonStroke.set("points", jsonPoints);

                jsonStrokes.add(jsonStroke);

            }

            jsonAnalysisRequest.set("strokes", jsonStrokes);
            jsonAnalysisRequest.put("language", language);
            jsonAnalysisRequest.put("applicationType", applicationKind.toString());
            jsonAnalysisRequest.put("unit", unit.toString());
            jsonAnalysisRequest.put("unitMultiple", multiple);
            return jsonAnalysisRequest.toString();

        } else {

            throw new Exception("Request parameters are invalid");

        }

    }

}