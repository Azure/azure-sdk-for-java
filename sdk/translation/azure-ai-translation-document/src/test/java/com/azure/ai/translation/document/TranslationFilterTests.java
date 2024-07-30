// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.BatchRequest;
import com.azure.ai.translation.document.models.SourceInput;
import com.azure.ai.translation.document.models.Status;
import com.azure.ai.translation.document.models.TargetInput;
import com.azure.ai.translation.document.models.TranslationStatus;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TranslationFilterTests extends DocumentTranslationClientTestBase {
    static int retryCount = 10;

    @RecordWithoutRequestBody
    @Test
    public void testGetTranslationStatusesFilterByStatus() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        createTranslationJobs(1, 1, Status.SUCCEEDED);
        List<String> cancelledIds = createTranslationJobs(1, 1, Status.CANCELLED);

        // list translations with filter
        List<String> cancelledStatusList = Arrays.asList(Status.CANCELLED.toString(),
                Status.CANCELLING.toString());
        OffsetDateTime testStartTime = LocalDateTime.now(ZoneOffset.UTC).atOffset(ZoneOffset.UTC);

        try {
            PagedIterable<TranslationStatus> translationStatusResult = documentTranslationClient
                    .getTranslationsStatus(null, null, null, cancelledStatusList, testStartTime, null, null);
            for (TranslationStatus translationStatus : translationStatusResult) {
                String status = translationStatus.getStatus().toString();
                assertTrue(cancelledStatusList.contains(status));
                String id = translationStatus.getId();
                assertTrue(cancelledIds.contains(id));
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RecordWithoutRequestBody
    @Test
    public void testGetTranslationStatusesFilterByIds() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        List<String> allIds = createTranslationJobs(2, 1, Status.SUCCEEDED);
        List<String> targetIds = new ArrayList<>();
        targetIds.add(allIds.get(0));

        try {
            PagedIterable<TranslationStatus> translationStatusResult = documentTranslationClient
                    .getTranslationsStatus(null, null, targetIds, null, null, null, null);
            for (TranslationStatus translationStatus : translationStatusResult) {
                String status = translationStatus.getStatus().toString();
                assertTrue(status.equalsIgnoreCase(Status.SUCCEEDED.toString()));
                String id = translationStatus.getId();
                assertTrue(targetIds.contains(id));
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RecordWithoutRequestBody
    @Test
    public void testGetTranslationStatusesFilterByCreatedAfter() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        // timestamp before creating a translation job
        LocalDateTime localTime = LocalDateTime.now(ZoneOffset.UTC);
        OffsetDateTime testStartTime = localTime.atOffset(ZoneOffset.UTC);

        // create test job
        List<String> targetIds = createTranslationJobs(1, 1, Status.SUCCEEDED);

        // list translations with filter
        try {
            PagedIterable<TranslationStatus> translationStatusResult = documentTranslationClient
                    .getTranslationsStatus(null, null, null, null, testStartTime, null, null);
            for (TranslationStatus translationStatus : translationStatusResult) {
                String id = translationStatus.getId();
                assertTrue(targetIds.contains(id));
                String createdDateTimeString = translationStatus.getCreatedDateTimeUtc().toString();
                LocalDateTime createdDateTimeUtc = LocalDateTime.parse(createdDateTimeString,
                        DateTimeFormatter.ISO_DATE_TIME);
                assertTrue(createdDateTimeUtc.isAfter(localTime));
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RecordWithoutRequestBody
    @Test
    public void testGetTranslationStatusesFilterByCreatedBefore() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        // create some translations
        List<String> targetIds = createTranslationJobs(1, 1, Status.SUCCEEDED);
        LocalDateTime timeStamp = LocalDateTime.now(ZoneOffset.UTC);
        OffsetDateTime endDateTime = timeStamp.atOffset(ZoneOffset.UTC);
        createTranslationJobs(1, 1, Status.SUCCEEDED);

        // getting only translations from the last hour
        LocalDateTime recentTimestamp = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);
        OffsetDateTime startDateTime = recentTimestamp.atOffset(ZoneOffset.UTC);

        // add translations with filter
        try {
            PagedIterable<TranslationStatus> translationStatusResult = documentTranslationClient
                    .getTranslationsStatus(null, null, null, null, startDateTime, endDateTime, null);
            boolean idExists = false;
            for (TranslationStatus translationStatus : translationStatusResult) {
                String id = translationStatus.getId();
                if (targetIds.contains(id)) {
                    idExists = true;
                }
                String createdDateTimeString = translationStatus.getCreatedDateTimeUtc().toString();
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

    @RecordWithoutRequestBody
    @Test
    public void testGetTranslationStatusesOrderByCreatedOn() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        // create some translations
        createTranslationJobs(3, 1, Status.SUCCEEDED);
        // getting only translations from the last few hours
        LocalDateTime recentTimestamp = LocalDateTime.now(ZoneOffset.UTC).minusHours(2);
        OffsetDateTime startDateTime = recentTimestamp.atOffset(ZoneOffset.UTC);

        // add translations with filter
        List<String> orderBy = Arrays.asList("createdDateTimeUtc asc");
        try {
            PagedIterable<TranslationStatus> translationStatusResult = documentTranslationClient
                    .getTranslationsStatus(null, null, null, null, startDateTime, null, orderBy);
            LocalDateTime timestamp = LocalDateTime.MIN;
            for (TranslationStatus translationStatus : translationStatusResult) {
                String createdDateTimeString = translationStatus.getCreatedDateTimeUtc().toString();
                LocalDateTime createdDateTimeUtc = LocalDateTime.parse(createdDateTimeString,
                        DateTimeFormatter.ISO_DATE_TIME);
                assertTrue(createdDateTimeUtc.isAfter(timestamp) || createdDateTimeUtc.isEqual(timestamp));
                timestamp = createdDateTimeUtc;
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> createTranslationJobs(int jobsCount, int docsPerJob,
            Status jobTerminalStatus) {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        // create source container
        if (jobTerminalStatus.equals(Status.CANCELLED)) {
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

            SyncPoller<TranslationStatus, Void> poller = setPlaybackSyncPollerPollInterval(documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

            String translationId = poller.poll().getValue().getId();
            translationIds.add(translationId);

            if (jobTerminalStatus.equals(Status.SUCCEEDED)) {
                poller.waitForCompletion();
            } else if (jobTerminalStatus.equals(Status.CANCELLED)) {
                documentTranslationClient.cancelTranslation(translationId);
            }
        }
        // ensure that cancel status has propagated before returning
        if (jobTerminalStatus.equals(Status.CANCELLED)) {
            waitForJobCancellation(translationIds);
        }
        return translationIds;
    }

    public void waitForJobCancellation(List<String> translationIds) {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        for (String translationId : translationIds) {
            TranslationStatus translationStatus = null;
            do {
                sleepIfRunningAgainstService(10000);
                retryCount--;
                translationStatus = documentTranslationClient.getTranslationStatus(translationId);
            } while ((translationStatus != null) && (translationStatus.getSummary().getCancelled() > 0)
                    && (retryCount > 0));
        }
    }

}
