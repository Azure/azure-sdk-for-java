// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.DocumentTranslationInput;
import com.azure.ai.translation.document.models.ListDocumentStatusesOptions;
import com.azure.ai.translation.document.models.DocumentStatusResult;
import com.azure.ai.translation.document.models.TranslationSource;
import com.azure.ai.translation.document.models.TranslationTarget;
import com.azure.ai.translation.document.models.TranslationStatus;
import com.azure.ai.translation.document.models.TranslationStatusResult;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentFilterTests extends DocumentTranslationClientTestBase {

    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusesFilterByStatus() {
        // create translation job
        TranslationStatusResult translationStatus = createSingleTranslationJob(2);

        // Add Status filter
        List<String> succeededStatusList = Arrays.asList(TranslationStatus.SUCCEEDED.toString());

        ListDocumentStatusesOptions listDocumentStatusesOptions
            = new ListDocumentStatusesOptions(translationStatus.getId()).setStatuses(succeededStatusList);

        try {
            PagedIterable<DocumentStatusResult> response
                = getDocumentTranslationClient().listDocumentStatuses(listDocumentStatusesOptions);
            for (DocumentStatusResult d : response) {
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
        TranslationStatusResult translationStatus = createSingleTranslationJob(2);
        List<String> testIds = new ArrayList<>();
        try {
            PagedIterable<DocumentStatusResult> response
                = documentTranslationClient.listDocumentStatuses(translationStatus.getId());
            for (DocumentStatusResult d : response) {
                testIds.add(d.getId());
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        ListDocumentStatusesOptions listDocumentStatusesOptions
            = new ListDocumentStatusesOptions(translationStatus.getId()).setDocumentIds(testIds);

        try {
            PagedIterable<DocumentStatusResult> response
                = getDocumentTranslationClient().listDocumentStatuses(listDocumentStatusesOptions);
            for (DocumentStatusResult d : response) {
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
        TranslationStatusResult translationStatus = createSingleTranslationJob(5);
        List<String> orderBy = Arrays.asList("createdDateTimeUtc asc");
        List<String> testCreatedOnDateTimes = new ArrayList<>();

        ListDocumentStatusesOptions listDocumentStatusesOptions
            = new ListDocumentStatusesOptions(translationStatus.getId()).setOrderBy(orderBy);

        try {
            PagedIterable<DocumentStatusResult> response
                = documentTranslationClient.listDocumentStatuses(listDocumentStatusesOptions);
            for (DocumentStatusResult d : response) {
                String createdDateTimeString = d.getCreatedDateTimeUtc().toString();
                testCreatedOnDateTimes.add(createdDateTimeString);
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        ListDocumentStatusesOptions listDocumentStatusesOptions1
            = new ListDocumentStatusesOptions(translationStatus.getId())
                .setCreatedAfter(getDateTimeOffset(testCreatedOnDateTimes.get(4)));

        // Asserting that only the last document is returned
        try {
            PagedIterable<DocumentStatusResult> response
                = documentTranslationClient.listDocumentStatuses(listDocumentStatusesOptions1);
            int itemCount = 0;
            for (DocumentStatusResult ignored : response) {
                itemCount += 1;
            }
            assertEquals(1, itemCount);
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        ListDocumentStatusesOptions listDocumentStatusesOptions2
            = new ListDocumentStatusesOptions(translationStatus.getId())
                .setCreatedAfter(getDateTimeOffset(testCreatedOnDateTimes.get(2)));

        // Asserting that the last 3 docs are returned
        try {
            PagedIterable<DocumentStatusResult> response
                = documentTranslationClient.listDocumentStatuses(listDocumentStatusesOptions2);
            int itemCount = 0;
            for (DocumentStatusResult ignored : response) {
                itemCount += 1;
            }
            assertEquals(3, itemCount);
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
        TranslationStatusResult translationStatus = createSingleTranslationJob(5);
        // add orderBy filter
        List<String> orderBy = Arrays.asList("createdDateTimeUtc asc");
        List<String> testCreatedOnDateTimes = new ArrayList<>();

        ListDocumentStatusesOptions listDocumentStatusesOptions
            = new ListDocumentStatusesOptions(translationStatus.getId()).setOrderBy(orderBy);

        try {
            PagedIterable<DocumentStatusResult> response
                = documentTranslationClient.listDocumentStatuses(listDocumentStatusesOptions);
            for (DocumentStatusResult d : response) {
                String createdDateTimeString = d.getCreatedDateTimeUtc().toString();
                testCreatedOnDateTimes.add(createdDateTimeString);
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        ListDocumentStatusesOptions listDocumentStatusesOptions1
            = new ListDocumentStatusesOptions(translationStatus.getId())
                .setCreatedBefore(getDateTimeOffset(testCreatedOnDateTimes.get(0)));

        // Asserting that only the first document is returned
        try {
            PagedIterable<DocumentStatusResult> response
                = documentTranslationClient.listDocumentStatuses(listDocumentStatusesOptions1);
            int itemCount = 0;
            for (DocumentStatusResult ignored : response) {
                itemCount += 1;
            }
            assertEquals(1, itemCount);
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        ListDocumentStatusesOptions listDocumentStatusesOptions2
            = new ListDocumentStatusesOptions(translationStatus.getId())
                .setCreatedBefore(getDateTimeOffset(testCreatedOnDateTimes.get(3)));

        // Asserting that the first 4/5 docs are returned
        try {
            PagedIterable<DocumentStatusResult> response
                = documentTranslationClient.listDocumentStatuses(listDocumentStatusesOptions2);
            int itemCount = 0;
            for (DocumentStatusResult ignored : response) {
                itemCount += 1;
            }
            assertEquals(4, itemCount);
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
        TranslationStatusResult translationStatus = createSingleTranslationJob(3);
        // add orderBy filter
        List<String> orderBy = Arrays.asList("createdDateTimeUtc desc");

        ListDocumentStatusesOptions listDocumentStatusesOptions
            = new ListDocumentStatusesOptions(translationStatus.getId()).setOrderBy(orderBy);

        try {
            PagedIterable<DocumentStatusResult> response
                = documentTranslationClient.listDocumentStatuses(listDocumentStatusesOptions);
            LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);
            for (DocumentStatusResult d : response) {
                String createdDateTimeString = d.getCreatedDateTimeUtc().toString();
                LocalDateTime createdDateTimeUtc
                    = LocalDateTime.parse(createdDateTimeString, DateTimeFormatter.ISO_DATE_TIME);
                assertTrue(createdDateTimeUtc.isBefore(timestamp) || createdDateTimeUtc.isEqual(timestamp));
                timestamp = createdDateTimeUtc;
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public TranslationStatusResult createSingleTranslationJob(int count) {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        List<TestDocument> testDocs = createDummyTestDocuments(count);
        String sourceUrl = createSourceContainer(testDocs);
        TranslationSource sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";
        TranslationTarget targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TranslationTarget> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        DocumentTranslationInput batchRequest = new DocumentTranslationInput(sourceInput, targetInputs);

        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = setPlaybackSyncPollerPollInterval(
            documentTranslationClient.beginTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

        // Wait until the operation completes
        return poller.waitForCompletion().getValue();
    }

    public OffsetDateTime getDateTimeOffset(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, formatter);
        return zonedDateTime.toOffsetDateTime();
    }
}
