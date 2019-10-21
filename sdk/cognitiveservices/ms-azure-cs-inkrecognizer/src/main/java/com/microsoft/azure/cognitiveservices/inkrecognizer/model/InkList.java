// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The InkList class represents the collection of one or more ink strokes that were recognized as a list. This includes
 * multi-line list items
 * @author Microsoft
 * @version 1.0
 */

public class InkList extends InkRecognitionUnit {

    private String recognizedText;

    InkList(JsonNode listNode, InkRecognitionRoot root) throws Exception {
        super(listNode, root);
    }

    /**
     * Retrieves the recognized text of the line under the list.
     * @return The recognized text of all the lines under the list
     */
    public String recognizedText() {
        if (recognizedText == null) {
            recognizedText = "";
            for (InkRecognitionUnit child : children()) {
                if (child instanceof Line) {
                    recognizedText += ((Line) child).recognizedText();
                }
            }
        }
        return recognizedText;
    }

}
