// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.implementation.models.DocumentSentiment;
import com.azure.ai.textanalytics.implementation.models.SentenceAssessment;
import com.azure.ai.textanalytics.implementation.models.SentenceSentiment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.textanalytics.implementation.Utility.findSentimentAssessment;
import static com.azure.ai.textanalytics.implementation.Utility.parseRefPointerToIndexArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for Target relation's reference parse
 */
public class ReferencePointerParseTest {
    private static final String INVALID_POINTER_EXCEPTION = "'%s' is not a valid assessment pointer.";
    private static final String INVALID_DOCUMENT_INDEX_EXCEPTION = "Invalid document index '%s' in '%s'.";
    private static final String INVALID_SENTENCE_INDEX_EXCEPTION = "Invalid sentence index '%s' in '%s'.";
    private static final String INVALID_OPINION_INDEX_EXCEPTION = "Invalid assessment index '%s' in '%s'.";
    private static final String VALID_OPINION_POINTER = "#/documents/1/sentences/3/assessments/5";

    @Test
    public void parseRefPointerToIndexArrayTest() {
        final int[] indexArray = parseRefPointerToIndexArray(VALID_OPINION_POINTER);
        assertEquals(1, indexArray[0]);
        assertEquals(3, indexArray[1]);
        assertEquals(5, indexArray[2]);
    }

    @Test
    public void parseInvalidNamePatternStringTest() {
        final String referencePointer = "#/a/1/b/2/c/3";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            parseRefPointerToIndexArray(referencePointer));
        assertEquals(String.format(INVALID_POINTER_EXCEPTION, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void parseInvalidNumberFormatStringTest() {
        final String referencePointer = "#/documents/a/sentences/b/assessments/c";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            parseRefPointerToIndexArray(referencePointer));
        assertEquals(String.format(INVALID_POINTER_EXCEPTION, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void parseIncompleteReferencePointerStringTest() {
        final String referencePointer = "#/documents/1/sentences/2";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            parseRefPointerToIndexArray(referencePointer));
        assertEquals(String.format(INVALID_POINTER_EXCEPTION, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void findSentimentAssessmentTest() {
        final SentenceAssessment sentimentAssessment = findSentimentAssessment(
            VALID_OPINION_POINTER, getDocumentSentiments());
        assertEquals(SentenceAssessment.class, sentimentAssessment.getClass());
    }

    @Test
    public void findSentimentOpinionWithInvalidDocumentIndex() {
        final String referencePointer = "#/documents/2/sentences/1/assessments/1";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            findSentimentAssessment(referencePointer, getDocumentSentiments()));
        assertEquals(String.format(INVALID_DOCUMENT_INDEX_EXCEPTION, 2, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void findSentimentOpinionWithInvalidSentenceIndex() {
        final String referencePointer = "#/documents/1/sentences/4/assessments/1";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            findSentimentAssessment(referencePointer, getDocumentSentiments()));
        assertEquals(String.format(INVALID_SENTENCE_INDEX_EXCEPTION, 4, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void findSentimentOpinionWithInvalidOpinionIndex() {
        final String referencePointer = "#/documents/1/sentences/3/assessments/6";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            findSentimentAssessment(referencePointer, getDocumentSentiments()));
        assertEquals(String.format(INVALID_OPINION_INDEX_EXCEPTION, 6, referencePointer), illegalStateException.getMessage());
    }

    private List<DocumentSentiment> getDocumentSentiments() {
        List<DocumentSentiment> documentSentiments = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            documentSentiments.add(new DocumentSentiment()
                .setId(Integer.toString(i))
                .setSentences(getSentenceSentiments()));
        }
        return documentSentiments;
    }

    private List<SentenceSentiment> getSentenceSentiments() {
        List<SentenceSentiment> sentenceSentiments = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            sentenceSentiments.add(new SentenceSentiment().setAssessments(getSentenceAssessments()));
        }
        return sentenceSentiments;
    }

    private List<SentenceAssessment> getSentenceAssessments() {
        final List<SentenceAssessment> sentenceOpinions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            sentenceOpinions.add(new SentenceAssessment());
        }
        return sentenceOpinions;
    }
}
