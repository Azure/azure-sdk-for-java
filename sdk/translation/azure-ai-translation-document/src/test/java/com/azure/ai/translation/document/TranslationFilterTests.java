// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.DocumentTranslationInput;
import com.azure.ai.translation.document.models.TranslationSource;
import com.azure.ai.translation.document.models.TranslationStatus;
import com.azure.ai.translation.document.models.TranslationStatusResult;
import com.azure.ai.translation.document.models.TranslationTarget;
import com.azure.ai.translation.document.models.ListTranslationStatusesOptions;
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
        createTranslationJobs(1, 1, TranslationStatus.SUCCEEDED);
        List<String> cancelledIds = createTranslationJobs(1, 1, TranslationStatus.CANCELLED);

        // list translations with filter
        List<String> cancelledStatusList
            = Arrays.asList(TranslationStatus.CANCELLED.toString(), TranslationStatus.CANCELLING.toString());
        OffsetDateTime testStartTime = testResourceNamer.now();

        ListTranslationStatusesOptions listTranslationStatusesOptions
            = new ListTranslationStatusesOptions().setStatuses(cancelledStatusList).setCreatedAfter(testStartTime);

        try {
            PagedIterable<TranslationStatusResult> translationStatusResult
                = documentTranslationClient.listTranslationStatuses(listTranslationStatusesOptions);
            for (TranslationStatusResult translationStatus : translationStatusResult) {
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
        List<String> allIds = createTranslationJobs(2, 1, TranslationStatus.SUCCEEDED);
        List<String> targetIds = new ArrayList<>();
        targetIds.add(allIds.get(0));

        ListTranslationStatusesOptions listTranslationStatusesOptions
            = new ListTranslationStatusesOptions().setTranslationIds(targetIds);

        try {
            PagedIterable<TranslationStatusResult> translationStatusResult
                = documentTranslationClient.listTranslationStatuses(listTranslationStatusesOptions);
            for (TranslationStatusResult translationStatus : translationStatusResult) {
                String status = translationStatus.getStatus().toString();
                assertTrue(status.equalsIgnoreCase(TranslationStatus.SUCCEEDED.toString()));
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
        LocalDateTime localTime = testResourceNamer.now().toLocalDateTime();
        OffsetDateTime testStartTime = localTime.atOffset(ZoneOffset.UTC);

        // create test job
        List<String> targetIds = createTranslationJobs(1, 1, TranslationStatus.SUCCEEDED);

        ListTranslationStatusesOptions listTranslationStatusesOptions
            = new ListTranslationStatusesOptions().setCreatedAfter(testStartTime);

        // list translations with filter
        try {
            PagedIterable<TranslationStatusResult> translationStatusResult
                = documentTranslationClient.listTranslationStatuses(listTranslationStatusesOptions);
            for (TranslationStatusResult translationStatus : translationStatusResult) {
                String id = translationStatus.getId();
                assertTrue(targetIds.contains(id));
                String createdDateTimeString = translationStatus.getCreatedOn().toString();
                LocalDateTime createdDateTimeUtc
                    = LocalDateTime.parse(createdDateTimeString, DateTimeFormatter.ISO_DATE_TIME);
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
        List<String> targetIds = createTranslationJobs(1, 1, TranslationStatus.SUCCEEDED);
        LocalDateTime timeStamp = testResourceNamer.now().toLocalDateTime();
        OffsetDateTime endDateTime = timeStamp.atOffset(ZoneOffset.UTC);
        createTranslationJobs(1, 1, TranslationStatus.SUCCEEDED);

        // getting only translations from the last hour
        LocalDateTime recentTimestamp = testResourceNamer.now().toLocalDateTime().minusHours(1);
        OffsetDateTime startDateTime = recentTimestamp.atOffset(ZoneOffset.UTC);

        ListTranslationStatusesOptions listTranslationStatusesOptions
            = new ListTranslationStatusesOptions().setCreatedAfter(startDateTime).setCreatedBefore(endDateTime);

        // add translations with filter
        try {
            PagedIterable<TranslationStatusResult> translationStatusResult
                = documentTranslationClient.listTranslationStatuses(listTranslationStatusesOptions);
            boolean idExists = false;
            for (TranslationStatusResult translationStatus : translationStatusResult) {
                String id = translationStatus.getId();
                if (targetIds.contains(id)) {
                    idExists = true;
                }
                String createdDateTimeString = translationStatus.getCreatedOn().toString();
                LocalDateTime createdDateTimeUtc
                    = LocalDateTime.parse(createdDateTimeString, DateTimeFormatter.ISO_DATE_TIME);
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
        createTranslationJobs(3, 1, TranslationStatus.SUCCEEDED);
        // getting only translations from the last few hours
        LocalDateTime recentTimestamp = testResourceNamer.now().toLocalDateTime().minusHours(2);
        OffsetDateTime startDateTime = recentTimestamp.atOffset(ZoneOffset.UTC);

        // add translations with filter
        List<String> orderBy = Arrays.asList("createdDateTimeUtc asc");

        ListTranslationStatusesOptions listTranslationStatusesOptions
            = new ListTranslationStatusesOptions().setCreatedAfter(startDateTime).setOrderby(orderBy);

        try {
            PagedIterable<TranslationStatusResult> translationStatusResult
                = documentTranslationClient.listTranslationStatuses(listTranslationStatusesOptions);
            LocalDateTime timestamp = LocalDateTime.MIN;
            for (TranslationStatusResult translationStatus : translationStatusResult) {
                String createdDateTimeString = translationStatus.getCreatedOn().toString();
                LocalDateTime createdDateTimeUtc
                    = LocalDateTime.parse(createdDateTimeString, DateTimeFormatter.ISO_DATE_TIME);
                assertTrue(createdDateTimeUtc.isAfter(timestamp) || createdDateTimeUtc.isEqual(timestamp));
                timestamp = createdDateTimeUtc;
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> createTranslationJobs(int jobsCount, int docsPerJob, TranslationStatus jobTerminalStatus) {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        // create source container
        if (jobTerminalStatus.equals(TranslationStatus.CANCELLED)) {
            docsPerJob = 20; // in order to avoid job completing before canceling
        }
        List<TestDocument> testDocuments = createDummyTestDocuments(docsPerJob);
        String sourceUrl = createSourceContainer(testDocuments);
        TranslationSource sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        // create a translation job
        List<String> translationIds = new ArrayList<>();
        for (int i = 1; i <= jobsCount; i++) {
            String targetUrl = createTargetContainer(null);
            String targetLanguageCode = "fr";

            TranslationTarget targetInput
                = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
            List<TranslationTarget> targetInputs = new ArrayList<>();
            targetInputs.add(targetInput);
            DocumentTranslationInput batchRequest = new DocumentTranslationInput(sourceInput, targetInputs);

            SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = setPlaybackSyncPollerPollInterval(
                documentTranslationClient.beginTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

            String translationId = poller.poll().getValue().getId();
            translationIds.add(translationId);

            if (jobTerminalStatus.equals(TranslationStatus.SUCCEEDED)) {
                poller.waitForCompletion();
            } else if (jobTerminalStatus.equals(TranslationStatus.CANCELLED)) {
                documentTranslationClient.cancelTranslation(translationId);
            }
        }
        // ensure that cancel status has propagated before returning
        if (jobTerminalStatus.equals(TranslationStatus.CANCELLED)) {
            waitForJobCancellation(translationIds);
        }
        return translationIds;
    }

    public void waitForJobCancellation(List<String> translationIds) {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        for (String translationId : translationIds) {
            TranslationStatusResult translationStatus;
            do {
                sleepIfRunningAgainstService(10000);
                retryCount--;
                translationStatus = documentTranslationClient.getTranslationStatus(translationId);
            } while ((translationStatus != null)
                && (translationStatus.getSummary().getCancelledCount() > 0)
                && (retryCount > 0));
        }
    }

}
