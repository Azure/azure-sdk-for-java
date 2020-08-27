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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Aspect relation's reference parse
 */
public class ReferencePointerParseTest {

    @Mock
    private TextAnalyticsClientImpl textAnalyticsClientImpl;

    @Mock
    private List<DocumentSentiment> documentSentiments;

    @Mock
    private DocumentSentiment documentSentiment;

    @Mock
    private List<SentenceSentiment> sentenceSentiments;

    @Mock
    private SentenceSentiment sentenceSentiment;

    @Mock
    private List<SentenceOpinion> sentenceOpinions;

    @Mock
    private SentenceOpinion sentenceOpinion;


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
        when(documentSentiments.size()).thenReturn(2);
        when(documentSentiments.get(1)).thenReturn(documentSentiment);
        when(documentSentiment.getSentences()).thenReturn(sentenceSentiments);
        when(sentenceSentiments.size()).thenReturn(4);
        when(sentenceSentiments.get(3)).thenReturn(sentenceSentiment);
        when(sentenceSentiment.getOpinions()).thenReturn(sentenceOpinions);
        when(sentenceOpinions.size()).thenReturn(6);
        when(sentenceOpinions.get(5)).thenReturn(sentenceOpinion);
        assertEquals(sentenceOpinion,
            analyzeSentimentAsyncClient.findSentimentOpinion(VALID_OPINION_POINTER, documentSentiments));
    }

    @Test
    public void findSentimentOpinionWithInvalidDocumentIndex() {
        when(documentSentiments.size()).thenReturn(2);
        when(documentSentiments.get(1)).thenReturn(documentSentiment);
        final String referencePointer = "#/documents/2/sentences/1/opinions/1";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            analyzeSentimentAsyncClient.findSentimentOpinion(referencePointer, documentSentiments));
        assertEquals(String.format(INVALID_DOCUMENT_INDEX_EXCEPTION, 2, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void findSentimentOpinionWithInvalidSentenceIndex() {
        when(documentSentiments.size()).thenReturn(2);
        when(documentSentiments.get(1)).thenReturn(documentSentiment);
        when(documentSentiment.getSentences()).thenReturn(sentenceSentiments);
        when(sentenceSentiments.size()).thenReturn(4);
        final String referencePointer = "#/documents/1/sentences/4/opinions/1";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            analyzeSentimentAsyncClient.findSentimentOpinion(referencePointer, documentSentiments));
        assertEquals(String.format(INVALID_SENTENCE_INDEX_EXCEPTION, 4, referencePointer), illegalStateException.getMessage());
    }

    @Test
    public void findSentimentOpinionWithInvalidOpinionIndex() {
        when(documentSentiments.size()).thenReturn(2);
        when(documentSentiments.get(1)).thenReturn(documentSentiment);
        when(documentSentiment.getSentences()).thenReturn(sentenceSentiments);
        when(sentenceSentiments.size()).thenReturn(4);
        when(sentenceSentiments.get(3)).thenReturn(sentenceSentiment);
        when(sentenceSentiment.getOpinions()).thenReturn(sentenceOpinions);
        when(sentenceOpinions.size()).thenReturn(6);
        when(sentenceOpinions.get(5)).thenReturn(sentenceOpinion);
        final String referencePointer = "#/documents/1/sentences/3/opinions/6";
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->
            analyzeSentimentAsyncClient.findSentimentOpinion(referencePointer, documentSentiments));
        assertEquals(String.format(INVALID_OPINION_INDEX_EXCEPTION, 6, referencePointer), illegalStateException.getMessage());
    }
}
