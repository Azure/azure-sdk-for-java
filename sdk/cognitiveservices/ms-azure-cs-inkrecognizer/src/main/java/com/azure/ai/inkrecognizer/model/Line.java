package com.azure.ai.inkrecognizer.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The Line class represents the collection of one or more ink strokes that were recognized as a line
 * @author Microsoft
 * @version 1.0
 */
public class Line extends InkRecognitionUnit {

    private final String recognizedText;
    private final List<String> alternates = new ArrayList<>();

    Line(
            JsonNode lineNode,
            InkRecognitionRoot root
    ) throws Exception {

        super(lineNode, root);

        recognizedText = lineNode.has("recognizedText") ? lineNode.get("recognizedText").asText() : "";

        try {
            // Parse the alternates
            if (lineNode.has("alternates")) {
                JsonNode jsonAlternates = lineNode.get("alternates");
                for (JsonNode jsonAlternate : jsonAlternates) {
                    alternates.add(jsonAlternate.get("recognizedString").asText());
                }
            }
        } catch (Exception e) {
            throw new Exception("Error while parsing server response");
        }


    }

    /**
     * Retrieves alternate lines of words as reported by the service.
     * @return A collection of alternate line of words.
     */
    public Iterable<String> alternates() {
        return alternates;
    }

    /**
     * Retrieves the recognized text (with the highest likelihood value) on the line.
     * @return The recognized string.
     */
    public String recognizedText() {
        return recognizedText;
    }

    /**
     * Retrieve the bullet object on the line if the line has one.
     * @return The bullet on the line if one is present.
     */
    public InkBullet bullet() {
        for (InkRecognitionUnit child : children()) {
            if (child.kind().equals(InkRecognitionUnitKind.INK_BULLET)) {
                return (InkBullet) child;
            }
        }
        return null;
    }

    /**
     * Retrieves the ink word objects on the line.
     * @return A collection of the words on the line.
     */
    public Iterable<InkWord> words() {
        List<InkWord> inkWords = new ArrayList<>();
        for (InkRecognitionUnit child : children()) {
            if (child.kind().equals(InkRecognitionUnitKind.INK_WORD)) {
                inkWords.add((InkWord) child);
            }
        }
        return inkWords;
    }

}