// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.search.documents.implementation.models.IndexDocumentsResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.convertToMapStringObject;
import static com.azure.search.documents.TestHelpers.createIndexAction;

@Execution(ExecutionMode.CONCURRENT)
public class IndexBatchExceptionTests {
    private static final String KEY_FIELD_NAME = "key";

    @Test
    public void clientShouldNotRetrySuccessfulBatch() {
        IndexDocumentsResult result = createResults(createSucceededResult(), createResult("2"));

        assertRetryBatchEmpty(result);
    }

    @Test
    public void clientShouldNotRetryBatchWithAllNonRetriableFailures() {
        IndexDocumentsResult result = createResults(createFailedResult("1", 500), createFailedResult("2", 404),
            createFailedResult("3", 400));

        assertRetryBatchEmpty(result);
    }

    @Test
    public void clientShouldNotRetryBatchWithSuccessesAndNonRetriableFailures() {
        IndexDocumentsResult result = createResults(createSucceededResult(), createFailedResult("2", 500),
            createFailedResult("3", 404), createResult("4"), createFailedResult("5", 400));

        assertRetryBatchEmpty(result);
    }

    @Test
    public void clientShouldRetryBatchWithAllRetriableFailures() {
        IndexDocumentsResult result = createResults(createFailedResult("1", 422), createFailedResult("2", 409),
            createFailedResult("3", 503));

        assertRetryBatchContains(result, Arrays.asList("1", "2", "3"));
    }

    @Test
    public void clientShouldRetryBatchWithSomeRetriableFailures() {
        IndexDocumentsResult result = createResults(createSucceededResult(), createFailedResult("2", 500),
            createFailedResult("3", 422), createFailedResult("4", 404), createFailedResult("5", 409),
            createFailedResult("6", 400), createResult("7"), createFailedResult("8", 503));

        assertRetryBatchContains(result, Arrays.asList("3", "5", "8"));
    }

    @Test
    public void clientShouldNotRetryResultWithUnexpectedStatusCode() {
        IndexDocumentsResult result = createResults(createSucceededResult(), createFailedResult("2", 502),
            createFailedResult("3", 503));

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
                .map(action -> action.getAdditionalProperties().get("HotelId"))
                .collect(Collectors.toList()));
    }

    public static Object getValueFromDocHelper(IndexAction action) {
        if (action.getAdditionalProperties() != null) {
            return action.getAdditionalProperties().get(KEY_FIELD_NAME);
        }
        //        else if (action.getParamMap() != null) {
        //            return action.getParamMap().get(KEY_FIELD_NAME);
        //        }
        return null;
    }

    private static IndexDocumentsBatch getRetryBatch(IndexDocumentsResult result) {
        List<String> allKeys = result.getResults().stream().map(IndexingResult::getKey).collect(Collectors.toList());
        IndexBatchException exception = new IndexBatchException(result);

        IndexDocumentsBatch originalBatch
            = new IndexDocumentsBatch(allKeys.stream()
                .map(key -> createIndexAction(IndexActionType.UPLOAD, Collections.singletonMap(KEY_FIELD_NAME, key)))
                .collect(Collectors.toList()));
        return exception.findFailedActionsToRetry(originalBatch, KEY_FIELD_NAME);
    }

    private static IndexDocumentsBatch getTypedRetryBatch(IndexDocumentsResult result) {
        List<String> allKeys = result.getResults().stream().map(IndexingResult::getKey).collect(Collectors.toList());
        IndexBatchException exception = new IndexBatchException(result);
        IndexDocumentsBatch originalBatch = new IndexDocumentsBatch(allKeys.stream()
            .map(key -> createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(new Hotel().setHotelId(key))))
            .collect(Collectors.toList()));
        return exception.findFailedActionsToRetry(originalBatch, "HotelId");
    }

    private static IndexingResult createSucceededResult() {
        return createResult("1", true, 200, null);
    }

    private static IndexingResult createResult(String key) {
        return createResult(key, true, 201, null);
    }

    private IndexingResult createFailedResult(String key, int statusCode) {
        return createResult(key, false, statusCode, "Something went wrong");
    }

    private static IndexDocumentsResult createResults(IndexingResult... results) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
                jsonWriter.writeStartObject().writeArrayField("value", results, JsonWriter::writeJson).writeEndObject();
            }

            try (JsonReader jsonReader = JsonProviders.createReader(outputStream.toByteArray())) {
                return IndexDocumentsResult.fromJson(jsonReader);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static IndexingResult createResult(String key, boolean status, int statusCode, String errorMessage) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
                jsonWriter.writeStartObject()
                    .writeStringField("key", key)
                    .writeBooleanField("status", status)
                    .writeIntField("statusCode", statusCode)
                    .writeStringField("errorMessage", errorMessage)
                    .writeEndObject();
            }

            try (JsonReader jsonReader = JsonProviders.createReader(outputStream.toByteArray())) {
                return IndexingResult.fromJson(jsonReader);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
