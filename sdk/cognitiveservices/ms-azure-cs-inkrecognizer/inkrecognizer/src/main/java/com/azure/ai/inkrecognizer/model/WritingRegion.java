/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.ai.inkrecognizer.model;

import android.util.DisplayMetrics;
import com.azure.ai.inkrecognizer.InkPointUnit;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The WritingRegion represents a certain part of a writing surface that
 * the user has written at least one word on.
 * @author Microsoft
 * @version 1.0
 */
public class WritingRegion extends InkRecognitionUnit {

    private String recognizedText;

    WritingRegion(
            JsonNode writingRegionNode,
            InkRecognitionRoot root,
            InkPointUnit inkPointUnit,
            DisplayMetrics displayMetrics
    ) throws Exception {
        super(writingRegionNode, root, inkPointUnit, displayMetrics);
    }

    /**
     * Retrieves the recognized text of one or more words in the region.
     * @return The recognized string.
     */
    public String recognizedText() {
        if (recognizedText == null) {
            recognizedText = "";
            for (InkRecognitionUnit child : children()) {
                recognizedText += ((Paragraph)child).recognizedText();
            }
            recognizedText += "\n";
        }
        return recognizedText;
    }

    /**
     * Retrieves all paragraphs in the writing region.
     * @return The paragraph objects within the writing region
     */
    public Iterable<Paragraph> paragraphs() {
        List<Paragraph> paragraphs = new ArrayList<>();
        for (InkRecognitionUnit child : children()) {
            if (child.kind().equals(InkRecognitionUnitKind.PARAGRAPH)) {
                paragraphs.add((Paragraph)child);
            }
        }
        return paragraphs;
    }

}
