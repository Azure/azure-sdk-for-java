// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.BatchRequest;
import com.azure.ai.translation.document.models.SourceInput;
import com.azure.ai.translation.document.models.TargetInput;
import com.azure.ai.translation.document.models.TranslationStatus;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.core.util.polling.SyncPoller;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TranslationFilterTests extends DocumentTranslationClientTestBase {
    static int retryCount = 10;

    @LiveOnly
    @RecordWithoutRequestBody
    @Test
    public void testGetTranslationStatusesFilterByStatus() {
        createTranslationJobs(1, 1, DocumentTranslationStatus.SUCCEEDED);
        List<String> cancelledIds = createTranslationJobs(1, 1, DocumentTranslationStatus.CANCELLED);

        // list translations with filter
        List<String> cancelledStatusList = Arrays.asList(DocumentTranslationStatus.CANCELLED.getValue(),
                DocumentTranslationStatus.CANCELLING.getValue());

        LocalDateTime testStartTime = LocalDateTime.now(ZoneOffset.UTC);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("createdDateTimeUtcStart", String.valueOf(testStartTime), false);
        requestOptions.addQueryParam("statuses",
                cancelledStatusList.stream()
                        .map(paramItemValue -> Objects.toString(paramItemValue, ""))
                        .collect(Collectors.joining(",")),
                false);

        try {
            PagedIterable<BinaryData> translationStatusResult = getDocumentTranslationClient()
                    .getTranslationsStatus(requestOptions);
            for (BinaryData d : translationStatusResult) {
                String status = new ObjectMapper().readTree(d.toBytes()).get("status").asText();
                assertTrue(cancelledStatusList.contains(status));
                String id = new ObjectMapper().readTree(d.toBytes()).get("id").asText();
                assertTrue(cancelledIds.contains(id));
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @LiveOnly
    @RecordWithoutRequestBody
    @Test
    public void testGetTranslationStatusesFilterByIds() {
        List<String> allIds = createTranslationJobs(2, 1, DocumentTranslationStatus.SUCCEEDED);
        List<String> targetIds = new ArrayList<>();
        targetIds.add(allIds.get(0));

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("ids",
                targetIds.stream()
                        .map(paramItemValue -> Objects.toString(paramItemValue, ""))
                        .collect(Collectors.joining(",")),
                false);

        try {
            PagedIterable<BinaryData> translationStatusResult = getDocumentTranslationClient()
                    .getTranslationsStatus(requestOptions);
            for (BinaryData d : translationStatusResult) {
                String status = new ObjectMapper().readTree(d.toBytes()).get("status").asText();
                assertTrue(status.equalsIgnoreCase(DocumentTranslationStatus.SUCCEEDED.toString()));
                String id = new ObjectMapper().readTree(d.toBytes()).get("id").asText();
                assertTrue(targetIds.contains(id));
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @LiveOnly
    @RecordWithoutRequestBody
    @Test
    public void testGetTranslationStatusesFilterByCreatedAfter() {
        // timestamp before creating a translation job
        LocalDateTime testStartTime = LocalDateTime.now(ZoneOffset.UTC);

        // create test job
        List<String> targetIds = createTranslationJobs(1, 1, DocumentTranslationStatus.SUCCEEDED);

        // list translations with filter
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("createdDateTimeUtcStart", String.valueOf(testStartTime), false);

        try {
            PagedIterable<BinaryData> translationStatusResult = getDocumentTranslationClient()
                    .getTranslationsStatus(requestOptions);
            for (BinaryData d : translationStatusResult) {
                String id = new ObjectMapper().readTree(d.toBytes()).get("id").asText();
                assertTrue(targetIds.contains(id));
                String createdDateTimeString = new ObjectMapper().readTree(d.toBytes()).get("createdDateTimeUtc")
                        .asText();
                LocalDateTime createdDateTimeUtc = LocalDateTime.parse(createdDateTimeString,
                        DateTimeFormatter.ISO_DATE_TIME);
                assertTrue(createdDateTimeUtc.isAfter(testStartTime));
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @LiveOnly
    @RecordWithoutRequestBody
    @Test
    public void testGetTranslationStatusesFilterByCreatedBefore() {
        // create some translations
        List<String> targetIds = createTranslationJobs(1, 1, DocumentTranslationStatus.SUCCEEDED);
        LocalDateTime timeStamp = LocalDateTime.now(ZoneOffset.UTC);
        createTranslationJobs(1, 1, DocumentTranslationStatus.SUCCEEDED);

        // getting only translations from the last hour
        LocalDateTime recentTimestamp = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);

        // add translations with filter
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("createdDateTimeUtcStart", String.valueOf(recentTimestamp), false);
        requestOptions.addQueryParam("createdDateTimeUtcEnd", String.valueOf(timeStamp), false);
        try {
            PagedIterable<BinaryData> translationStatusResult = getDocumentTranslationClient()
                    .getTranslationsStatus(requestOptions);
            boolean idExists = false;
            for (BinaryData d : translationStatusResult) {
                String id = new ObjectMapper().readTree(d.toBytes()).get("id").asText();
                if (targetIds.contains(id)) {
                    idExists = true;
                }
                String createdDateTimeString = new ObjectMapper().readTree(d.toBytes()).get("createdDateTimeUtc")
                        .asText();
                LocalDateTime createdDateTimeUtc = LocalDateTime.parse(createdDateTimeString,
                        DateTimeFormatter.ISO_DATE_TIME);
                assertTrue(createdDateTimeUtc.isBefore(timeStamp));
            }
            assertTrue(idExists);
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @LiveOnly
    @RecordWithoutRequestBody
    @Test
    public void testGetTranslationStatusesOrderByCreatedOn() {
        // create some translations
        createTranslationJobs(3, 1, DocumentTranslationStatus.SUCCEEDED);
        // getting only translations from the last few hours
        LocalDateTime recentTimestamp = LocalDateTime.now(ZoneOffset.UTC).minusHours(2);

        // add translations with filter
        List<String> orderBy = Arrays.asList("createdDateTimeUtc asc");
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("orderby",
                orderBy.stream()
                        .map(paramItemValue -> Objects.toString(paramItemValue, ""))
                        .collect(Collectors.joining(",")),
                false);
        requestOptions.addQueryParam("createdDateTimeUtcStart", String.valueOf(recentTimestamp), false);

        try {
            PagedIterable<BinaryData> translationStatusResult = getDocumentTranslationClient()
                    .getTranslationsStatus(requestOptions);
            LocalDateTime timestamp = LocalDateTime.MIN;
            for (BinaryData d : translationStatusResult) {
                String createdDateTimeString = new ObjectMapper().readTree(d.toBytes()).get("createdDateTimeUtc")
                        .asText();
                LocalDateTime createdDateTimeUtc = LocalDateTime.parse(createdDateTimeString,
                        DateTimeFormatter.ISO_DATE_TIME);
                assertTrue(createdDateTimeUtc.compareTo(timestamp) > 0 || createdDateTimeUtc.compareTo(timestamp) == 0);
                timestamp = createdDateTimeUtc;
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> createTranslationJobs(int jobsCount, int docsPerJob,
            DocumentTranslationStatus jobTerminalStatus) {

        // create source container
        if (jobTerminalStatus.equals(DocumentTranslationStatus.CANCELLED)) {
            docsPerJob = 20; // in order to avoid job completing before canceling
        }
        List<TestDocument> testDocuments = createDummyTestDocuments(docsPerJob);
        String sourceUrl = createSourceContainer(testDocuments);
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        // create a translation job
        List<String> translationIds = new ArrayList<>();
        for (int i = 1; i <= jobsCount; i++) {
            String targetUrl = createTargetContainer(null);
            String targetLanguageCode = "fr";

            TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
            List<TargetInput> targetInputs = new ArrayList<>();
            targetInputs.add(targetInput);
            BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

            SyncPoller<TranslationStatus, Void> poller = getDocumentTranslationClient()
                    .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

            String translationId = poller.poll().getValue().getId();
            translationIds.add(translationId);

            if (jobTerminalStatus.equals(DocumentTranslationStatus.SUCCEEDED)) {
                poller.waitForCompletion();
            } else if (jobTerminalStatus.equals(DocumentTranslationStatus.CANCELLED)) {
                getDocumentTranslationClient().cancelTranslation(translationId);
            }
        }
        // ensure that cancel status has propagated before returning
        if (jobTerminalStatus.equals(DocumentTranslationStatus.CANCELLED)) {
            waitForJobCancellation(translationIds);
        }
        return translationIds;
    }

    public void waitForJobCancellation(List<String> translationIds) {
        for (String translationId : translationIds) {
            TranslationStatus translationStatus = null;
            do {
                try {
                    Thread.sleep(10000);
                    retryCount--;
                    translationStatus = getDocumentTranslationClient().getTranslationStatus(translationId);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DocumentTranslationTests.class.getName()).log(Level.SEVERE, null, ex);
                }
            } while ((translationStatus != null) && (translationStatus.getSummary().getCancelled() > 0)
                    && (retryCount > 0));
        }
    }

}
