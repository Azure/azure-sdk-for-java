// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

public class IndexBatchExceptionTests {
    private static final String KEY_FIELD_NAME = "key";
    private IndexDocumentsResult result;

    @BeforeEach
    public void setup() {
        result = mock(IndexDocumentsResult.class);
    }

    @Test
    public void clientShouldNotRetrySuccessfulBatch() {

        IndexingResult indexingResult1 = mock(IndexingResult.class);
        IndexingResult createSuccessResult = createSucceededResult(indexingResult1, "1");
        IndexingResult indexingResult2 = mock(IndexingResult.class);
        IndexingResult createResult = createResult(indexingResult2, "2");


        Mockito.when(result.getResults()).thenReturn(Arrays.asList(
            createSuccessResult,
            createResult
        ));
        assertRetryBatchEmpty(result);
    }

    @Test
    public void clientShouldNotRetryBatchWithAllNonRetriableFailures() {
        IndexingResult indexingResult1 = mock(IndexingResult.class);
        IndexingResult createFailedResult1 = createFailedResult(indexingResult1, "1", 500);
        IndexingResult indexingResult2 = mock(IndexingResult.class);
        IndexingResult createFailedResult2 = createFailedResult(indexingResult2, "2", 404);
        IndexingResult indexingResult3 = mock(IndexingResult.class);
        IndexingResult createFailedResult3 = createFailedResult(indexingResult3, "3", 400);
        Mockito.when(result.getResults()).thenReturn(Arrays.asList(
            createFailedResult1,
            createFailedResult2,
            createFailedResult3
        ));
        assertRetryBatchEmpty(result);
    }

    @Test
    public void clientShouldNotRetryBatchWithSuccessesAndNonRetriableFailures() {
        IndexingResult indexingResult1 = mock(IndexingResult.class);
        IndexingResult createSucceededResult = createSucceededResult(indexingResult1, "1");
        IndexingResult indexingResult2 = mock(IndexingResult.class);
        IndexingResult createFailedResult1 = createFailedResult(indexingResult2, "2", 500);
        IndexingResult indexingResult3 = mock(IndexingResult.class);
        IndexingResult createFailedResult2 = createFailedResult(indexingResult3, "3", 404);
        IndexingResult indexingResult4 = mock(IndexingResult.class);
        IndexingResult createResult = createResult(indexingResult4, "4");
        IndexingResult indexingResult5 = mock(IndexingResult.class);
        IndexingResult createFailedResult3 = createFailedResult(indexingResult5, "5", 400);

        Mockito.when(result.getResults()).thenReturn(Arrays.asList(
            createSucceededResult,
            createFailedResult1,
            createFailedResult2,
            createResult,
            createFailedResult3
        ));
        assertRetryBatchEmpty(result);
    }

    @Test
    public void clientShouldRetryBatchWithAllRetriableFailures() {
        IndexingResult indexingResult1 = mock(IndexingResult.class);
        IndexingResult createFailedResult1 = createFailedResult(indexingResult1, "1", 422);
        IndexingResult indexingResult2 = mock(IndexingResult.class);
        IndexingResult createFailedResult2 = createFailedResult(indexingResult2, "2", 409);
        IndexingResult indexingResult3 = mock(IndexingResult.class);
        IndexingResult createFailedResult3 = createFailedResult(indexingResult3, "3", 503);
        Mockito.when(result.getResults()).thenReturn(Arrays.asList(
            createFailedResult1,
            createFailedResult2,
            createFailedResult3
        ));

        assertRetryBatchContains(result, Arrays.asList("1", "2", "3"));
    }

    @Test
    public void clientShouldRetryBatchWithSomeRetriableFailures() {
        IndexingResult indexingResult1 = mock(IndexingResult.class);
        IndexingResult createSucceededResult = createSucceededResult(indexingResult1, "1");
        IndexingResult indexingResult2 = mock(IndexingResult.class);
        IndexingResult createFailedResult1 = createFailedResult(indexingResult2, "2", 500);
        IndexingResult indexingResult3 = mock(IndexingResult.class);
        IndexingResult createFailedResult2 = createFailedResult(indexingResult3, "3", 422);
        IndexingResult indexingResult4 = mock(IndexingResult.class);
        IndexingResult createFailedResult3 = createFailedResult(indexingResult4, "4", 404);
        IndexingResult indexingResult5 = mock(IndexingResult.class);
        IndexingResult createFailedResult4 = createFailedResult(indexingResult5, "5", 409);
        IndexingResult indexingResult6 = mock(IndexingResult.class);
        IndexingResult createFailedResult5 = createFailedResult(indexingResult6, "6", 400);
        IndexingResult indexingResult7 = mock(IndexingResult.class);
        IndexingResult createResult = createResult(indexingResult7, "7");
        IndexingResult indexingResult8 = mock(IndexingResult.class);
        IndexingResult createFailedResult6 = createFailedResult(indexingResult8, "8", 503);
        Mockito.when(result.getResults()).thenReturn(Arrays.asList(
            createSucceededResult,
            createFailedResult1,
            createFailedResult2,
            createFailedResult3,
            createFailedResult4,
            createFailedResult5,
            createResult,
            createFailedResult6
        ));

        assertRetryBatchContains(result, Arrays.asList("3", "5", "8"));
    }

    @Test
    public void clientShouldNotRetryResultWithUnexpectedStatusCode() {
        IndexingResult indexingResult1 = mock(IndexingResult.class);
        IndexingResult createSucceededResult = createSucceededResult(indexingResult1, "1");
        IndexingResult indexingResult2 = mock(IndexingResult.class);
        IndexingResult createFailedResult1 = createFailedResult(indexingResult2, "2", 502);
        IndexingResult indexingResult3 = mock(IndexingResult.class);
        IndexingResult createFailedResult2 = createFailedResult(indexingResult3, "3", 503);
        Mockito.when(result.getResults()).thenReturn(Arrays.asList(
            createSucceededResult,
            createFailedResult1,
            createFailedResult2
        ));

        assertRetryBatchContains(result, Collections.singletonList("3"));
    }

    private void assertRetryBatchEmpty(IndexDocumentsResult result) {
        Assertions.assertTrue(getRetryBatch(result).getActions().isEmpty());
        Assertions.assertTrue(getTypedRetryBatch(result).getActions().isEmpty());
    }

    private void assertRetryBatchContains(IndexDocumentsResult result, List<String> expectedKeys) {
        Assertions.assertEquals(expectedKeys, getRetryBatch(result).getActions().stream()
            .map(this::getValueFromDocHelper).collect(Collectors.toList()));

        Assertions.assertEquals(expectedKeys, getTypedRetryBatch(result).getActions().stream()
            .map(action -> action.getDocument().getHotelId()).collect(Collectors.toList()));
    }

    public Object getValueFromDocHelper(IndexAction<SearchDocument> action) {
        if (action.getDocument() != null) {
            return action.getDocument().get(KEY_FIELD_NAME);
        }
//        else if (action.getParamMap() != null) {
//            return action.getParamMap().get(KEY_FIELD_NAME);
//        }
        return null;
    }

    private IndexBatchBase<SearchDocument> getRetryBatch(IndexDocumentsResult result) {
        List<String> allKeys = result.getResults().stream().map(IndexingResult::getKey).collect(Collectors.toList());
        IndexBatchException exception = new IndexBatchException(result);

        IndexDocumentsBatch<SearchDocument> originalBatch = new IndexDocumentsBatch<SearchDocument>().addUploadActions(
            allKeys.stream().map(key -> new SearchDocument(new HashMap<String, String>() {{
                    put(KEY_FIELD_NAME, key);
                }})).collect(Collectors.toList())
        );
        return exception.findFailedActionsToRetry(originalBatch, KEY_FIELD_NAME);
    }

    private IndexBatchBase<Hotel> getTypedRetryBatch(IndexDocumentsResult result) {
        List<String> allKeys = result.getResults().stream().map(IndexingResult::getKey).collect(Collectors.toList());
        IndexBatchException exception = new IndexBatchException(result);
        IndexDocumentsBatch<Hotel> originalBatch = new IndexDocumentsBatch<Hotel>().addUploadActions(
            allKeys.stream().map(key -> new Hotel().setHotelId(key)).collect(Collectors.toList())
        );
        return exception.findFailedActionsToRetry(originalBatch, Hotel::getHotelId);
    }

    private IndexingResult createSucceededResult(IndexingResult indexingResult, String key) {
        Mockito.when(indexingResult.getKey()).thenReturn(key);
        Mockito.when(indexingResult.getStatusCode()).thenReturn(200);
        Mockito.when(indexingResult.isSucceeded()).thenReturn(true);

        return indexingResult;
    }

    private IndexingResult createResult(IndexingResult indexingResult, String key) {
        Mockito.when(indexingResult.getKey()).thenReturn(key);
        Mockito.when(indexingResult.getStatusCode()).thenReturn(201);
        Mockito.when(indexingResult.isSucceeded()).thenReturn(true);
        return indexingResult;
    }

    private IndexingResult createFailedResult(IndexingResult indexingResult, String key, int statusCode) {
        Mockito.when(indexingResult.getKey()).thenReturn(key);
        Mockito.when(indexingResult.getErrorMessage()).thenReturn("Something went wrong");
        Mockito.when(indexingResult.getStatusCode()).thenReturn(statusCode);
        Mockito.when(indexingResult.isSucceeded()).thenReturn(false);

        return indexingResult;
    }
}
