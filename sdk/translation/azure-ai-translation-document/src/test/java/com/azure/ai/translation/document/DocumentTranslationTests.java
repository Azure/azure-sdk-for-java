// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.DocumentTranslationInput;
import com.azure.ai.translation.document.models.DocumentFilter;
import com.azure.ai.translation.document.models.DocumentStatusResult;
import com.azure.ai.translation.document.models.TranslationGlossary;
import com.azure.ai.translation.document.models.TranslationSource;
import com.azure.ai.translation.document.models.TranslationTarget;
import com.azure.ai.translation.document.models.TranslationStatusResult;
import com.azure.core.models.ResponseError;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DocumentTranslationTests extends DocumentTranslationClientTestBase {
    static int retryCount = 10;

    @RecordWithoutRequestBody
    @Test
    public void testClientCannotAuthenticateWithFakeApiKey() {
        String testEndpoint = "https://t7d8641d8f25ec940-doctranslation.cognitiveservices.azure.com";
        String testApiKey = "fakeApiKey";

        ClientAuthenticationException e = assertThrows(ClientAuthenticationException.class,
            () -> getDTClient(testEndpoint, testApiKey).getSupportedFormats());

        assertEquals(401, e.getResponse().getStatusCode());
    }

    @RecordWithoutRequestBody
    @Test
    public void testSingleSourceSingleTarget() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";

        TranslationSource sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);
        TranslationTarget targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TranslationTarget> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        DocumentTranslationInput batchRequest = new DocumentTranslationInput(sourceInput, targetInputs);
        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = setPlaybackSyncPollerPollInterval(
            documentTranslationClient.beginTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

        // Wait until the operation completes
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();

        // Validate the response
        validateTranslationStatus(translationStatus, 1);
    }

    @RecordWithoutRequestBody
    @Test
    public void testSingleSourceMultipleTargets() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        String targetUrl1 = createTargetContainer(null);
        String targetLanguageCode1 = "fr";

        String targetUrl2 = createTargetContainer(null);
        String targetLanguageCode2 = "es";

        String targetUrl3 = createTargetContainer(null);
        String targetLanguageCode3 = "ar";

        TranslationSource sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);
        TranslationTarget targetInput1
            = TestHelper.createTargetInput(targetUrl1, targetLanguageCode1, null, null, null);
        TranslationTarget targetInput2
            = TestHelper.createTargetInput(targetUrl2, targetLanguageCode2, null, null, null);
        TranslationTarget targetInput3
            = TestHelper.createTargetInput(targetUrl3, targetLanguageCode3, null, null, null);
        List<TranslationTarget> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput1);
        targetInputs.add(targetInput2);
        targetInputs.add(targetInput3);
        DocumentTranslationInput batchRequest = new DocumentTranslationInput(sourceInput, targetInputs);

        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = setPlaybackSyncPollerPollInterval(
            documentTranslationClient.beginTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

        // Wait until the operation completes
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();

        // Validate the response
        validateTranslationStatus(translationStatus, 3);
    }

    @RecordWithoutRequestBody
    @Test
    public void testMultipleSourcesSingleTarget() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl1 = createSourceContainer(ONE_TEST_DOCUMENTS);
        String targetUrl1 = createTargetContainer(null);
        String targetLanguageCode1 = "fr";

        String sourceUrl2 = createSourceContainer(ONE_TEST_DOCUMENTS);
        String targetUrl2 = createTargetContainer(null);
        String targetLanguageCode2 = "es";

        TranslationSource sourceInput1 = TestHelper.createSourceInput(sourceUrl1, null, null, null);
        TranslationTarget targetInput1
            = TestHelper.createTargetInput(targetUrl1, targetLanguageCode1, null, null, null);
        List<TranslationTarget> targetInputs1 = new ArrayList<>();
        targetInputs1.add(targetInput1);
        DocumentTranslationInput batchRequest1 = new DocumentTranslationInput(sourceInput1, targetInputs1);

        TranslationSource sourceInput2 = TestHelper.createSourceInput(sourceUrl2, null, null, null);
        TranslationTarget targetInput2
            = TestHelper.createTargetInput(targetUrl2, targetLanguageCode2, null, null, null);
        List<TranslationTarget> targetInputs2 = new ArrayList<>();
        targetInputs2.add(targetInput2);
        DocumentTranslationInput batchRequest2 = new DocumentTranslationInput(sourceInput2, targetInputs2);

        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller
            = setPlaybackSyncPollerPollInterval(documentTranslationClient
                .beginTranslation(TestHelper.getStartTranslationDetails(batchRequest1, batchRequest2)));

        // Wait until the operation completes
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();

        // Validate the response
        validateTranslationStatus(translationStatus, 2);
    }

    @RecordWithoutRequestBody
    @Test
    public void testSingleSourceSingleTargetWithPrefix() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(TWO_TEST_DOCUMENTS);
        DocumentFilter filter = new DocumentFilter();
        filter.setPrefix("File");
        TranslationSource sourceInput = TestHelper.createSourceInput(sourceUrl, filter, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";
        TranslationTarget targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TranslationTarget> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        DocumentTranslationInput batchRequest = new DocumentTranslationInput(sourceInput, targetInputs);

        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = setPlaybackSyncPollerPollInterval(
            documentTranslationClient.beginTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

        // Wait until the operation completes
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();

        // Validate the response
        validateTranslationStatus(translationStatus, 1);
    }

    @RecordWithoutRequestBody
    @Test
    public void testSingleSourceSingleTargetWithSuffix() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        DocumentFilter filter = new DocumentFilter();
        filter.setSuffix("txt");
        TranslationSource sourceInput = TestHelper.createSourceInput(sourceUrl, filter, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";
        TranslationTarget targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TranslationTarget> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        DocumentTranslationInput batchRequest = new DocumentTranslationInput(sourceInput, targetInputs);

        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = setPlaybackSyncPollerPollInterval(
            documentTranslationClient.beginTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

        // Wait until the operation completes
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();

        // Validate the response
        validateTranslationStatus(translationStatus, 1);
    }

    @RecordWithoutRequestBody
    @Test
    public void testSingleSourceSingleTargetListDocuments() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
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
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();
        String translationId = translationStatus.getId();

        PagedIterable<DocumentStatusResult> documentsStatus
            = documentTranslationClient.listDocumentStatuses(translationId);
        assertNotNull(documentsStatus);
        DocumentStatusResult firstItem = documentsStatus.iterator().next();

        assertEquals(translationStatus.getStatus().toString(), firstItem.getStatus().toString());
        assertEquals(translationStatus.getSummary().getTotalCharactersChargedCount(),
            (long) firstItem.getCharacterCharged());
    }

    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatus() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
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
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();
        String translationId = translationStatus.getId();

        PagedIterable<DocumentStatusResult> response = documentTranslationClient.listDocumentStatuses(translationId);
        assertNotNull(response);

        String documentId = response.iterator().next().getId();
        DocumentStatusResult documentStatus = documentTranslationClient.getDocumentStatus(translationId, documentId);
        validateDocumentStatus(documentStatus, targetLanguageCode);
    }

    @RecordWithoutRequestBody
    @Test
    public void testWrongSourceRightTarget() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = "https://idont.ex.ist";
        TranslationSource sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "es";
        TranslationTarget targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TranslationTarget> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        DocumentTranslationInput batchRequest = new DocumentTranslationInput(sourceInput, targetInputs);

        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = setPlaybackSyncPollerPollInterval(
            documentTranslationClient.beginTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

        while ((Objects.equals(poller.poll().getValue().getStatus().toString(), "NotStarted")) && (retryCount > 0)) {
            sleepIfRunningAgainstService(10000);
            retryCount--;
        }
        String status = poller.poll().getValue().getStatus().toString();
        assertEquals("ValidationFailed", status);
        ResponseError responseError = poller.poll().getValue().getError();
        String errorCode = responseError.getCode();
        assertEquals("InvalidRequest", errorCode);
        String errorMessage = responseError.getMessage();
        assertEquals("Cannot access source document location with the current permissions.", errorMessage);
    }

    @RecordWithoutRequestBody
    @Test
    public void testRightSourceWrongTarget() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        TranslationSource sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = "https://idont.ex.ist";
        String targetLanguageCode = "es";
        TranslationTarget targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TranslationTarget> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        DocumentTranslationInput batchRequest = new DocumentTranslationInput(sourceInput, targetInputs);

        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = setPlaybackSyncPollerPollInterval(
            documentTranslationClient.beginTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

        while ((Objects.equals(poller.poll().getValue().getStatus().toString(), "NotStarted")) && (retryCount > 0)) {
            sleepIfRunningAgainstService(10000);
            retryCount--;
        }
        String status = poller.poll().getValue().getStatus().toString();
        assertEquals("ValidationFailed", status);

        ResponseError responseError = poller.poll().getValue().getError();
        String errorCode = responseError.getCode();
        assertEquals("InvalidRequest", errorCode);
        String errorMessage = responseError.getMessage();
        assertEquals("Cannot access target document location with the current permissions.", errorMessage);
    }

    @RecordWithoutRequestBody
    @Test
    public void testContainerWithSupportedAndUnsupportedFiles() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        List<TestDocument> documents = new ArrayList<>();
        documents.add(new TestDocument("Document1.txt", "First english test document"));
        documents.add(new TestDocument("File2.jpg", "jpg"));
        String sourceUrl = createSourceContainer(documents);

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
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();

        // Validate the response
        validateTranslationStatus(translationStatus, 1);
    }

    @RecordWithoutRequestBody
    @Test
    public void testEmptyDocumentError() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        List<TestDocument> documents = new ArrayList<>();
        documents.add(new TestDocument("Document1.txt", ""));
        String sourceUrl = createSourceContainer(documents);

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
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();

        // Validate the response
        assertNotNull(translationStatus.getId());
        assertEquals("Failed", translationStatus.getStatus().toString());
        assertEquals(1, translationStatus.getSummary().getTotalCount());
        assertEquals(0, translationStatus.getSummary().getSuccessCount());
        assertEquals(1, translationStatus.getSummary().getFailedCount());

        ResponseError responseError = translationStatus.getError();
        String errorCode = responseError.getCode();
        assertEquals("InvalidRequest", errorCode);
        String errorMessage = responseError.getMessage();
        assertEquals("The document does not have any translatable text.", errorMessage);
    }

    @RecordWithoutRequestBody
    @Test
    public void testExistingFileInTargetContainer() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        TranslationSource sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(ONE_TEST_DOCUMENTS);
        String targetLanguageCode = "fr";
        TranslationTarget targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TranslationTarget> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        DocumentTranslationInput batchRequest = new DocumentTranslationInput(sourceInput, targetInputs);

        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = setPlaybackSyncPollerPollInterval(
            documentTranslationClient.beginTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

        while ((Objects.equals(poller.poll().getValue().getStatus().toString(), "NotStarted")) && (retryCount > 0)) {
            sleepIfRunningAgainstService(10000);
            retryCount--;
        }
        String status = poller.poll().getValue().getStatus().toString();
        assertEquals("ValidationFailed", status);

        // String innerErrorCode =
        // poller.poll().getValue().getError().getError().getCode();
        // assertEquals("TargetFileAlreadyExists", innerErrorCode);
    }

    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusWithInvalidGuid() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
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
        TranslationStatusResult translationStatus = poller.waitForCompletion().getValue();
        String translationId = translationStatus.getId();

        assertThrows(ResourceNotFoundException.class,
            () -> documentTranslationClient.getDocumentStatus(translationId, "Foo Bar"),
            "Expected ResourceNotFoundException was not thrown");

        assertThrows(ResourceNotFoundException.class,
            () -> documentTranslationClient.getDocumentStatus(translationId, " "),
            "Expected ResourceNotFoundException was not thrown");
    }

    @RecordWithoutRequestBody
    @Test
    public void testDocumentTranslationWithGlossary() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String documentName = "Document1.txt";
        String documentContent = "First english test document";
        List<TestDocument> documents = Arrays.asList(new TestDocument(documentName, documentContent));
        String sourceUrl = createSourceContainer(documents);
        TranslationSource sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        Map<String, String> containerValues = createTargetContainerWithClient(null);
        String targetUrl = containerValues.get("containerUrl");
        String targetLanguageCode = "fr";

        // Constructing and uploading glossary on the fly
        String glossaryName = "validGlossary.csv";
        // changing the word test --> glossaryTest
        String glossaryContent = "test, glossaryTest";

        String glossaryUrl = createGlossary(new TestDocument(glossaryName, glossaryContent));
        List<TranslationGlossary> glossaries = new ArrayList<>();
        glossaries.add(new TranslationGlossary(glossaryUrl, "csv"));

        TranslationTarget targetInput
            = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, glossaries, null);
        List<TranslationTarget> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        DocumentTranslationInput batchRequest = new DocumentTranslationInput(sourceInput, targetInputs);

        SyncPoller<TranslationStatusResult, TranslationStatusResult> poller = setPlaybackSyncPollerPollInterval(
            documentTranslationClient.beginTranslation(TestHelper.getStartTranslationDetails(batchRequest)));

        // Wait until the operation completes
        poller.waitForCompletion();

        String targetContainerName = containerValues.get("containerName");
        String response = downloadDocumentStream(targetContainerName, documentName);

        // Validate the response
        Assertions.assertTrue(response.contains("glossaryTest"));
    }

    public static void validateTranslationStatus(TranslationStatusResult translationStatus, int translationCount) {
        assertNotNull(translationStatus.getId());
        assertEquals("Succeeded", translationStatus.getStatus().toString());
        assertEquals(translationCount, translationStatus.getSummary().getTotalCount());
        assertEquals(translationCount, translationStatus.getSummary().getSuccessCount());
        assertEquals(0, translationStatus.getSummary().getFailedCount());
        assertEquals(0, translationStatus.getSummary().getCancelledCount());
        assertEquals(0, translationStatus.getSummary().getInProgressCount());
    }

    private void validateDocumentStatus(DocumentStatusResult documentStatus, String targetLanguageCode) {
        assertEquals("Succeeded", documentStatus.getStatus().toString());
        assertNotNull(documentStatus.getId());
        assertNotNull(documentStatus.getSourcePath());
        assertNotNull(documentStatus.getPath());
        if (getTestMode() == TestMode.LIVE) {
            assertEquals(targetLanguageCode, documentStatus.getTo());
        }
        assertNotEquals(new Date(), documentStatus.getCreatedDateTimeUtc());
        assertNotEquals(new Date(), documentStatus.getLastActionDateTimeUtc());
        assertEquals(1, documentStatus.getProgress());
    }
}
