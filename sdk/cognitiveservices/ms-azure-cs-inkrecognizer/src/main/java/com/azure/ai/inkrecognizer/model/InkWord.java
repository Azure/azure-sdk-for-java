// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The InkWord class represents a collection of one or more ink strokes that were recognized as a word.
 * @author Microsoft
 * @version 1.0
 */
public class InkWord extends InkRecognitionUnit {

    private final String recognizedText;
    private final List<String> alternates = new ArrayList<>();

    InkWord(JsonNode wordNode, InkRecognitionRoot root) throws Exception {

        super(wordNode, root);

        recognizedText = wordNode.has("recognizedText") ? wordNode.get("recognizedText").asText() : "";

        try {
            // Parse the alternates
            if (wordNode.has("alternates")) {
                JsonNode jsonAlternates = wordNode.get("alternates");
                for (JsonNode jsonAlternate : jsonAlternates) {
                    alternates.add(jsonAlternate.get("recognizedString").asText());
                }

            }
        } catch (Exception e) {
            throw new Exception("Error while parsing server response");
        }

    }

    /**
     * Retrieves the collection of alternative recognized texts.
     * @return A collection of recognized strings.
     */
    public List<String> alternates() {
        return alternates;
    }

    /**
     * Retrieves the recognized text with the highest likelihood value.
     * @return The recognized string.
     */
    public String recognizedText() {
        return recognizedText;
    }

}
