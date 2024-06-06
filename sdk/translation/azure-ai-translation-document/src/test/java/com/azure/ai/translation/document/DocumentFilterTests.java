// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.BatchRequest;
import com.azure.ai.translation.document.models.DocumentStatus;
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

public class DocumentFilterTests extends DocumentTranslationClientTestBase {

    @LiveOnly
    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusesFilterByStatus() {
        // create translation job
        TranslationStatus translationStatus = createSingleTranslationJob(2);

        // Add Status filter
        List<String> succeededStatusList = Arrays.asList(DocumentTranslationStatus.SUCCEEDED.getValue());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("statuses",
                succeededStatusList.stream()
                        .map(paramItemValue -> Objects.toString(paramItemValue, ""))
                        .collect(Collectors.joining(",")),
                false);

        try {
            PagedIterable<BinaryData> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId(), requestOptions);
            for (BinaryData d : response) {
                String status = new ObjectMapper().readTree(d.toBytes()).get("status").asText();
                assertTrue(succeededStatusList.contains(status));
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @LiveOnly
    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusesFilterByIds() {
        // create translation job and get all the document IDs
        TranslationStatus translationStatus = createSingleTranslationJob(2);
        List<String> testIds = new ArrayList<>();
        try {
            PagedIterable<DocumentStatus> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId());
            for (DocumentStatus d : response) {
                testIds.add(d.getId());
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        // Add id filter
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("ids", testIds.get(0), false);

        try {
            PagedIterable<BinaryData> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId(), requestOptions);
            for (BinaryData d : response) {
                String id = new ObjectMapper().readTree(d.toBytes()).get("id").asText();
                assertTrue(testIds.contains(id));
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @LiveOnly
    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusesFilterByCreatedAfter() {
        // create translation job and get all the document IDs
        TranslationStatus translationStatus = createSingleTranslationJob(5);
        // add orderBy filter
        List<String> orderBy = Arrays.asList("createdDateTimeUtc asc");
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("orderby",
                orderBy.stream()
                        .map(paramItemValue -> Objects.toString(paramItemValue, ""))
                        .collect(Collectors.joining(",")),
                false);

        List<String> testCreatedOnDateTimes = new ArrayList<>();
        try {
            PagedIterable<BinaryData> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId(), requestOptions);
            for (BinaryData d : response) {
                String createdDateTimeString = new ObjectMapper().readTree(d.toBytes()).get("createdDateTimeUtc")
                        .asText();
                LocalDateTime createdDateTimeUtc = LocalDateTime.parse(createdDateTimeString,
                        DateTimeFormatter.ISO_DATE_TIME);
                testCreatedOnDateTimes.add(createdDateTimeString);
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        // Asserting that only the last document is returned
        requestOptions = new RequestOptions();
        requestOptions.addQueryParam("createdDateTimeUtcStart", testCreatedOnDateTimes.get(4), false);
        try {
            PagedIterable<BinaryData> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId(), requestOptions);
            int itemCount = 0;
            for (BinaryData d : response) {
                itemCount += 1;
            }
            assertTrue(itemCount == 1);
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        // Asserting that the last 3 docs are returned
        requestOptions = new RequestOptions();
        requestOptions.addQueryParam("createdDateTimeUtcStart", testCreatedOnDateTimes.get(2), false);
        try {
            PagedIterable<BinaryData> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId(), requestOptions);
            int itemCount = 0;
            for (BinaryData d : response) {
                itemCount += 1;
            }
            assertTrue(itemCount == 3);
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @LiveOnly
    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusesFilterByCreatedBefore() {
        // create translation job
        TranslationStatus translationStatus = createSingleTranslationJob(5);
        // add orderBy filter
        List<String> orderBy = Arrays.asList("createdDateTimeUtc asc");
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("orderby",
                orderBy.stream()
                        .map(paramItemValue -> Objects.toString(paramItemValue, ""))
                        .collect(Collectors.joining(",")),
                false);

        List<String> testCreatedOnDateTimes = new ArrayList<>();
        try {
            PagedIterable<BinaryData> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId(), requestOptions);
            for (BinaryData d : response) {
                String createdDateTimeString = new ObjectMapper().readTree(d.toBytes()).get("createdDateTimeUtc")
                        .asText();
                LocalDateTime createdDateTimeUtc = LocalDateTime.parse(createdDateTimeString,
                        DateTimeFormatter.ISO_DATE_TIME);
                testCreatedOnDateTimes.add(createdDateTimeString);
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        // Asserting that only the first document is returned
        requestOptions = new RequestOptions();
        requestOptions.addQueryParam("createdDateTimeUtcEnd", testCreatedOnDateTimes.get(0), false);
        try {
            PagedIterable<BinaryData> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId(), requestOptions);
            int itemCount = 0;
            for (BinaryData d : response) {
                itemCount += 1;
            }
            assertTrue(itemCount == 1);
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

        // Asserting that the first 4/5 docs are returned
        requestOptions = new RequestOptions();
        requestOptions.addQueryParam("createdDateTimeUtcEnd", testCreatedOnDateTimes.get(3), false);
        try {
            PagedIterable<BinaryData> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId(), requestOptions);
            int itemCount = 0;
            for (BinaryData d : response) {
                itemCount += 1;
            }
            assertTrue(itemCount == 4);
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @LiveOnly
    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusesOrderByCreatedOn() {
        // create translation job
        TranslationStatus translationStatus = createSingleTranslationJob(3);
        // add orderBy filter
        List<String> orderBy = Arrays.asList("createdDateTimeUtc desc");
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("orderby",
                orderBy.stream()
                        .map(paramItemValue -> Objects.toString(paramItemValue, ""))
                        .collect(Collectors.joining(",")),
                false);

        try {
            PagedIterable<BinaryData> response = getDocumentTranslationClient()
                    .getDocumentsStatus(translationStatus.getId(), requestOptions);
            LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);
            for (BinaryData d : response) {
                String createdDateTimeString = new ObjectMapper().readTree(d.toBytes()).get("createdDateTimeUtc")
                        .asText();
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
        List<TestDocument> testDocs = createDummyTestDocuments(count);
        String sourceUrl = createSourceContainer(testDocs);
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";
        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = getDocumentTranslationClient()
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();
        return translationStatus;
    }
}
