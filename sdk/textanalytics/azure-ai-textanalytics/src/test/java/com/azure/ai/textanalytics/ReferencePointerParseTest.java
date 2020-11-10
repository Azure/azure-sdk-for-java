// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentSentiment;
import com.azure.ai.textanalytics.implementation.models.SentenceOpinion;
import com.azure.ai.textanalytics.implementation.models.SentenceSentiment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for Aspect relation's reference parse
 */
public class ReferencePointerParseTest {

    @Mock
    private TextAnalyticsClientImpl textAnalyticsClientImpl;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    private AnalyzeSentimentAsyncClient analyzeSentimentAsyncClient = new AnalyzeSentimentAsyncClient(textAnalyticsClientImpl);

    private static final String INVALID_POINTER_EXCEPTION = "'%s' is not a valid opinion pointer.";
    private static final String INVALID_DOCUMENT_INDEX_EXCEPTION = "Invalid document index '%s' in '%s'.";
    private static final String INVALID_SENTENCE_INDEX_EXCEPTION = "Invalid sentence index '%s' in '%s'.";
    private static final String INVALID_OPINION_INDEX_EXCEPTION = "Invalid opinion index '%s' in '%s'.";
    private static final String VALID_OPINION_POINTER = "#/documents/1/sentences/3/opinions/5";

    @Test
    public void parseRefPointerToIndexArrayTest() {
        final int[] indexArray = analyzeSentimentAsyncClient.parseRefPointerToIndexArray(VALID_OPINION_POINTER);
        assertEquals(1, indexArray[0]);
        assertEquals(3, indexArray[1]);
        assertEquals(5, indexArray[2]);
    }

    @Test
    public void parseInvalidNamePatternStringTest() {
        final String referencePointer = "#/a/1/b/2/c/3";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            analyzeSentimentAsyncClient.parseRefPointerToIndexArray(referencePointer));
        assertEquals(String.format(INVALID_POINTER_EXCEPTION, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void parseInvalidNumberFormatStringTest() {
        final String referencePointer = "#/documents/a/sentences/b/opinions/c";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            analyzeSentimentAsyncClient.parseRefPointerToIndexArray(referencePointer));
        assertEquals(String.format(INVALID_POINTER_EXCEPTION, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void parseIncompleteReferencePointerStringTest() {
        final String referencePointer = "#/documents/1/sentences/2";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            analyzeSentimentAsyncClient.parseRefPointerToIndexArray(referencePointer));
        assertEquals(String.format(INVALID_POINTER_EXCEPTION, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void findSentimentOpinion() {
        final SentenceOpinion sentenceOpinion = analyzeSentimentAsyncClient.findSentimentOpinion(VALID_OPINION_POINTER, getDocumentSentiments());
        assertEquals(SentenceOpinion.class, sentenceOpinion.getClass());
    }

    @Test
    public void findSentimentOpinionWithInvalidDocumentIndex() {
        final String referencePointer = "#/documents/2/sentences/1/opinions/1";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            analyzeSentimentAsyncClient.findSentimentOpinion(referencePointer, getDocumentSentiments()));
        assertEquals(String.format(INVALID_DOCUMENT_INDEX_EXCEPTION, 2, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void findSentimentOpinionWithInvalidSentenceIndex() {
        final String referencePointer = "#/documents/1/sentences/4/opinions/1";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            analyzeSentimentAsyncClient.findSentimentOpinion(referencePointer, getDocumentSentiments()));
        assertEquals(String.format(INVALID_SENTENCE_INDEX_EXCEPTION, 4, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void findSentimentOpinionWithInvalidOpinionIndex() {
        final String referencePointer = "#/documents/1/sentences/3/opinions/6";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            analyzeSentimentAsyncClient.findSentimentOpinion(referencePointer, getDocumentSentiments()));
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
            sentenceSentiments.add(new SentenceSentiment().setOpinions(getSentenceOpinions()));
        }
        return sentenceSentiments;
    }

    private List<SentenceOpinion> getSentenceOpinions() {
        final List<SentenceOpinion> sentenceOpinions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            sentenceOpinions.add(new SentenceOpinion());
        }
        return sentenceOpinions;
    }
}
