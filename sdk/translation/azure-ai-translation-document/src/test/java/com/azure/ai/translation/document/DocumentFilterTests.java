// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.BatchRequest;
import com.azure.ai.translation.document.models.DocumentStatus;
import com.azure.ai.translation.document.models.SourceInput;
import com.azure.ai.translation.document.models.Status;
import com.azure.ai.translation.document.models.TargetInput;
import com.azure.ai.translation.document.models.TranslationStatus;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.polling.SyncPoller;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentFilterTests extends DocumentTranslationClientTestBase {

    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusesFilterByStatus() {
        // create translation job
        TranslationStatus translationStatus = createSingleTranslationJob(2);

        // Add Status filter
        List<String> succeededStatusList = Arrays.asList(Status.SUCCEEDED.toString());

        try {
            PagedIterable<DocumentStatus> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId(), null, null, null, succeededStatusList, null, null, null);
            for (DocumentStatus d : response) {
                String status = d.getStatus().toString();
                assertTrue(succeededStatusList.contains(status));
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusesFilterByIds() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();

        // create translation job and get all the document IDs
        TranslationStatus translationStatus = createSingleTranslationJob(2);
        List<String> testIds = new ArrayList<>();
        try {
            PagedIterable<DocumentStatus> response = documentTranslationClient
                    .getDocumentsStatus(translationStatus.getId());
            for (DocumentStatus d : response) {
                testIds.add(d.getId());
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            PagedIterable<DocumentStatus> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId(), null, null, testIds, null, null, null, null);
            for (DocumentStatus d : response) {
                String id = d.getId();
                assertTrue(testIds.contains(id));
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusesFilterByCreatedAfter() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        // create translation job and get all the document IDs
        TranslationStatus translationStatus = createSingleTranslationJob(5);
        List<String> orderBy = Arrays.asList("createdDateTimeUtc asc");
        List<String> testCreatedOnDateTimes = new ArrayList<>();

        try {
            PagedIterable<DocumentStatus> response = documentTranslationClient
                    .getDocumentsStatus(translationStatus.getId(), null, null, null, null, null, null, orderBy);
            for (DocumentStatus d : response) {
                String createdDateTimeString = d.getCreatedDateTimeUtc().toString();
                testCreatedOnDateTimes.add(createdDateTimeString);
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        // Asserting that only the last document is returned
        try {
            PagedIterable<DocumentStatus> response = documentTranslationClient
                    .getDocumentsStatus(translationStatus.getId(), null, null, null, null, getDateTimeOffset(testCreatedOnDateTimes.get(4)), null, null);
            int itemCount = 0;
            for (DocumentStatus d : response) {
                itemCount += 1;
            }
            assertTrue(itemCount == 1);
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        // Asserting that the last 3 docs are returned
        try {
            PagedIterable<DocumentStatus> response = documentTranslationClient
                    .getDocumentsStatus(translationStatus.getId(), null, null, null, null, getDateTimeOffset(testCreatedOnDateTimes.get(2)), null, null);
            int itemCount = 0;
            for (DocumentStatus d : response) {
                itemCount += 1;
            }
            assertTrue(itemCount == 3);
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusesFilterByCreatedBefore() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        // create translation job
        TranslationStatus translationStatus = createSingleTranslationJob(5);
        // add orderBy filter
        List<String> orderBy = Arrays.asList("createdDateTimeUtc asc");
        List<String> testCreatedOnDateTimes = new ArrayList<>();

        try {
            PagedIterable<DocumentStatus> response = documentTranslationClient
                    .getDocumentsStatus(translationStatus.getId(), null, null, null, null, null, null, orderBy);
            for (DocumentStatus d : response) {
                String createdDateTimeString = d.getCreatedDateTimeUtc().toString();
                testCreatedOnDateTimes.add(createdDateTimeString);
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        // Asserting that only the first document is returned
        try {
            PagedIterable<DocumentStatus> response = documentTranslationClient
                    .getDocumentsStatus(translationStatus.getId(), null, null, null, null, null, getDateTimeOffset(testCreatedOnDateTimes.get(0)), null);
            int itemCount = 0;
            for (DocumentStatus d : response) {
                itemCount += 1;
            }
            assertTrue(itemCount == 1);
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        // Asserting that the first 4/5 docs are returned
        try {
            PagedIterable<DocumentStatus> response = documentTranslationClient
                    .getDocumentsStatus(translationStatus.getId(), null, null, null, null, null, getDateTimeOffset(testCreatedOnDateTimes.get(3)), null);
            int itemCount = 0;
            for (DocumentStatus d : response) {
                itemCount += 1;
            }
            assertTrue(itemCount == 4);
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusesOrderByCreatedOn() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        // create translation job
        TranslationStatus translationStatus = createSingleTranslationJob(3);
        // add orderBy filter
        List<String> orderBy = Arrays.asList("createdDateTimeUtc desc");

        try {
            PagedIterable<DocumentStatus> response = documentTranslationClient
                    .getDocumentsStatus(translationStatus.getId(), null, null, null, null, null, null, orderBy);
            LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);
            for (DocumentStatus d : response) {
                String createdDateTimeString = d.getCreatedDateTimeUtc().toString();
                LocalDateTime createdDateTimeUtc = LocalDateTime.parse(createdDateTimeString,
                        DateTimeFormatter.ISO_DATE_TIME);
                assertTrue(createdDateTimeUtc.compareTo(timestamp) < 0 || createdDateTimeUtc.compareTo(timestamp) == 0);
                timestamp = createdDateTimeUtc;
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public TranslationStatus createSingleTranslationJob(int count) {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        List<TestDocument> testDocs = createDummyTestDocuments(count);
        String sourceUrl = createSourceContainer(testDocs);
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";
        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = setPlaybackSyncPollerPollInterval(documentTranslationClient
            .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();
        return translationStatus;
    }

    public OffsetDateTime getDateTimeOffset(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, formatter);
        return zonedDateTime.toOffsetDateTime();
    }
}
