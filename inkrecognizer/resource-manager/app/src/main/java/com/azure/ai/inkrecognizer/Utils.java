// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

import android.util.DisplayMetrics;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;

class Utils {

    private static final float INCH_TO_MM = 25.4f;

    static <T> T getValueOrDefault(T value, T defaultValue) { return value == null ? defaultValue : value; }

    static String createJSONForRequest(
            Iterable<InkStroke> strokes,
            InkPointUnit unit,
            float multiple,
            ApplicationKind applicationKind,
            String language,
            DisplayMetrics displayMetrics
    ) throws Exception {

        Iterator strokesIterator = strokes.iterator();

        if (
                strokesIterator.hasNext()
                        && unit != null
                        && language != null
                        && applicationKind != null
        )
        {

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

                if (stroke.getKind() != null) {
                    jsonStroke.put("kind", stroke.getKind().toString());
                }

                Iterator pointsIterator = stroke.getInkPoints().iterator();
                ArrayNode jsonPoints = factory.arrayNode();

                while (pointsIterator.hasNext()) {
                    InkPoint inkPoint;
                    if(unit.equals(InkPointUnit.PIXEL)) {
                        inkPoint = convertFromPixelToMM((InkPoint) pointsIterator.next(), displayMetrics);
                    } else {
                        inkPoint = (InkPoint) pointsIterator.next();
                    }

                    jsonPoints.add(
                            factory.objectNode()
                                    .put("x", inkPoint.getX())
                                    .put("y", inkPoint.getY())
                    );
                }
                if(unit.equals(InkPointUnit.PIXEL)) {
                    unit = InkPointUnit.MM;
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

    static private InkPoint convertFromPixelToMM(InkPoint inkPoint, DisplayMetrics displayMetrics) {

        float xdpi = displayMetrics.xdpi;
        float ydpi = displayMetrics.ydpi;
        return new InkPoint() {
            @Override
            public float getX() {
                return inkPoint.getX() / xdpi * INCH_TO_MM;
            }

            @Override
            public float getY() {
                return inkPoint.getY() / ydpi * INCH_TO_MM;
            }
        };

    }

}