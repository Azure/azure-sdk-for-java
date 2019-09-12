// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer.model;

import android.util.DisplayMetrics;
import com.azure.ai.inkrecognizer.InkPointUnit;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The InkBullet class represents the collection of one or more
 * ink strokes that were recognized as a bullet point on a line
 * @author Microsoft
 * @version 1.0
 */
public class InkBullet extends InkRecognitionUnit {

    private final String recognizedText;

    InkBullet(
            JsonNode bulletNode,
            InkRecognitionRoot root,
            InkPointUnit inkPointUnit,
            DisplayMetrics displayMetrics
    ) throws Exception {

        super(bulletNode, root, inkPointUnit, displayMetrics);

        recognizedText = bulletNode.has("recognizedText") ? bulletNode.get("recognizedText").asText() : "";

    }

    /**
     * Retrieves the recognized text. If the bullet
     * isn't recognized as a string (e.g. when the bullet is a
     * complex shape),an empty string is returned.
     * @return The recognized string of the bullet.
     */
    public String recognizedText() {
        return recognizedText;
    }

}
