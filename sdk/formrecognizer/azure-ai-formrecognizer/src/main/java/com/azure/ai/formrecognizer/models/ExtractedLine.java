// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;

public class ExtractedLine extends RawItem {
    private List<ExtractedWord> extractedWords;

    public ExtractedLine(BoundingBox boundingBox, String text, List<ExtractedWord> extractedWords) {
        super(boundingBox, text);
        this.extractedWords = extractedWords;
    }

    public List<ExtractedWord> getExtractedWords() {
        return extractedWords;
    }

    public ExtractedLine setExtractedWords(
        final List<ExtractedWord> extractedWords) {
        this.extractedWords = extractedWords;
        return this;
    }
}
