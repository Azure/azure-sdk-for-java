// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Execution(ExecutionMode.CONCURRENT)
public class IndexBatchExceptionTests {
    private static final String KEY_FIELD_NAME = "key";

    @Test
    public void clientShouldNotRetrySuccessfulBatch() {
        IndexDocumentsResult result
            = new IndexDocumentsResult(Arrays.asList(createSucceededResult("1"), createResult("2")));

        assertRetryBatchEmpty(result);
    }

    @Test
    public void clientShouldNotRetryBatchWithAllNonRetriableFailures() {
        IndexDocumentsResult result = new IndexDocumentsResult(
            Arrays.asList(createFailedResult("1", 500), createFailedResult("2", 404), createFailedResult("3", 400)));

        assertRetryBatchEmpty(result);
    }

    @Test
    public void clientShouldNotRetryBatchWithSuccessesAndNonRetriableFailures() {
        IndexDocumentsResult result
            = new IndexDocumentsResult(Arrays.asList(createSucceededResult("1"), createFailedResult("2", 500),
                createFailedResult("3", 404), createResult("4"), createFailedResult("5", 400)));

        assertRetryBatchEmpty(result);
    }

    @Test
    public void clientShouldRetryBatchWithAllRetriableFailures() {
        IndexDocumentsResult result = new IndexDocumentsResult(
            Arrays.asList(createFailedResult("1", 422), createFailedResult("2", 409), createFailedResult("3", 503)));

        assertRetryBatchContains(result, Arrays.asList("1", "2", "3"));
    }

    @Test
    public void clientShouldRetryBatchWithSomeRetriableFailures() {
        IndexDocumentsResult result
            = new IndexDocumentsResult(Arrays.asList(createSucceededResult("1"), createFailedResult("2", 500),
                createFailedResult("3", 422), createFailedResult("4", 404), createFailedResult("5", 409),
                createFailedResult("6", 400), createResult("7"), createFailedResult("8", 503)));

        assertRetryBatchContains(result, Arrays.asList("3", "5", "8"));
    }

    @Test
    public void clientShouldNotRetryResultWithUnexpectedStatusCode() {
        IndexDocumentsResult result = new IndexDocumentsResult(
            Arrays.asList(createSucceededResult("1"), createFailedResult("2", 502), createFailedResult("3", 503)));

        assertRetryBatchContains(result, Collections.singletonList("3"));
    }

    private static void assertRetryBatchEmpty(IndexDocumentsResult result) {
        Assertions.assertTrue(getRetryBatch(result).getActions().isEmpty());
        Assertions.assertTrue(getTypedRetryBatch(result).getActions().isEmpty());
    }

    private static void assertRetryBatchContains(IndexDocumentsResult result, List<String> expectedKeys) {
        Assertions.assertEquals(expectedKeys,
            getRetryBatch(result).getActions()
                .stream()
                .map(IndexBatchExceptionTests::getValueFromDocHelper)
                .collect(Collectors.toList()));

        Assertions.assertEquals(expectedKeys,
            getTypedRetryBatch(result).getActions()
                .stream()
                .map(action -> action.getDocument().getHotelId())
                .collect(Collectors.toList()));
    }

    public static Object getValueFromDocHelper(IndexAction<SearchDocument> action) {
        if (action.getDocument() != null) {
            return action.getDocument().get(KEY_FIELD_NAME);
        }
        //        else if (action.getParamMap() != null) {
        //            return action.getParamMap().get(KEY_FIELD_NAME);
        //        }
        return null;
    }

    private static IndexBatchBase<SearchDocument> getRetryBatch(IndexDocumentsResult result) {
        List<String> allKeys = result.getResults().stream().map(IndexingResult::getKey).collect(Collectors.toList());
        IndexBatchException exception = new IndexBatchException(result);

        IndexDocumentsBatch<SearchDocument> originalBatch
            = new IndexDocumentsBatch<SearchDocument>().addUploadActions(allKeys.stream()
                .map(key -> new SearchDocument(Collections.singletonMap(KEY_FIELD_NAME, key)))
                .collect(Collectors.toList()));
        return exception.findFailedActionsToRetry(originalBatch, KEY_FIELD_NAME);
    }

    private static IndexBatchBase<Hotel> getTypedRetryBatch(IndexDocumentsResult result) {
        List<String> allKeys = result.getResults().stream().map(IndexingResult::getKey).collect(Collectors.toList());
        IndexBatchException exception = new IndexBatchException(result);
        IndexDocumentsBatch<Hotel> originalBatch = new IndexDocumentsBatch<Hotel>()
            .addUploadActions(allKeys.stream().map(key -> new Hotel().setHotelId(key)).collect(Collectors.toList()));
        return exception.findFailedActionsToRetry(originalBatch, Hotel::getHotelId);
    }

    private static IndexingResult createSucceededResult(String key) {
        return new IndexingResult(key, true, 200);
    }

    private static IndexingResult createResult(String key) {
        return new IndexingResult(key, true, 201);
    }

    private IndexingResult createFailedResult(String key, int statusCode) {
        String json = "{\"key\":\"" + key + "\",\"errorMessage\":\"Something went wrong\",\"statusCode\":" + statusCode
            + ",\"status\":false}";
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return IndexingResult.fromJson(jsonReader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
