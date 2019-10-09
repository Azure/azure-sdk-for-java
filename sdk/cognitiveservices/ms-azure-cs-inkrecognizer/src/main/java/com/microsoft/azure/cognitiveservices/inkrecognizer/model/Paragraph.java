// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The Paragraph class represents the collection of one or more ink strokes that were recognized as a paragraph
 * @author Microsoft
 * @version 1.0
 */
public class Paragraph extends InkRecognitionUnit {

    private String recognizedText;

    Paragraph(JsonNode paragraphNode, InkRecognitionRoot root) throws Exception {
        super(paragraphNode, root);
    }

    /**
     * Retrieves the recognized text within the paragraph.
     * @return The recognized string.
     */
    public String recognizedText() {
        if (recognizedText == null) {
            recognizedText = "";
            for (InkRecognitionUnit child : children()) {
                if (child.kind().equals(InkRecognitionUnitKind.LINE)) {
                    if (child instanceof Line) {
                        recognizedText += ((Line) child).recognizedText() + "\n";
                    }
                } else {
                    if (child instanceof InkList) {
                        recognizedText += ((InkList) child).recognizedText() + "\n";
                    }
                }
            }
        }
        return recognizedText;
    }

    /**
     * Retrieves the lines in the paragraph.
     * @return The line objects within the paragraph.
     */
    public Iterable<Line> lines() {
        List<Line> lines = new ArrayList<>();
        for (InkRecognitionUnit child : children()) {
            if (child.kind().equals(InkRecognitionUnitKind.LINE)) {
                if (child instanceof Line) {
                    lines.add((Line) child);
                }
            }
        }
        return lines;
    }

}
