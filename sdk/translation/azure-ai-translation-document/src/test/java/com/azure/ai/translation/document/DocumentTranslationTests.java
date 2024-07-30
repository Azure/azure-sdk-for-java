// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import static com.azure.ai.translation.document.DocumentTranslationClientTestBase.ONE_TEST_DOCUMENTS;
import static com.azure.ai.translation.document.DocumentTranslationClientTestBase.TWO_TEST_DOCUMENTS;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpResponse;

import com.azure.ai.translation.document.models.TranslationStatus;
import com.azure.ai.translation.document.models.BatchRequest;
import com.azure.ai.translation.document.models.DocumentFilter;
import com.azure.ai.translation.document.models.DocumentStatus;
import com.azure.ai.translation.document.models.Glossary;
import com.azure.ai.translation.document.models.SourceInput;
import com.azure.ai.translation.document.models.SupportedFileFormats;
import com.azure.ai.translation.document.models.TargetInput;
import com.azure.core.test.annotation.RecordWithoutRequestBody;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.azure.core.test.TestMode;

import org.junit.jupiter.api.Assertions;
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

        SupportedFileFormats response = null;
        try {
            response = getDTClient(testEndpoint, testApiKey).getSupportedFormats();
        } catch (ClientAuthenticationException e) {
            HttpResponse httpResponse = e.getResponse();
            assertEquals(401, httpResponse.getStatusCode());
        }
    }

    @RecordWithoutRequestBody
    @Test
    public void testSingleSourceSingleTarget() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";

        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);
        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);
        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();

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

        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);
        TargetInput targetInput1 = TestHelper.createTargetInput(targetUrl1, targetLanguageCode1, null, null, null);
        TargetInput targetInput2 = TestHelper.createTargetInput(targetUrl2, targetLanguageCode2, null, null, null);
        TargetInput targetInput3 = TestHelper.createTargetInput(targetUrl3, targetLanguageCode3, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput1);
        targetInputs.add(targetInput2);
        targetInputs.add(targetInput3);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();

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

        SourceInput sourceInput1 = TestHelper.createSourceInput(sourceUrl1, null, null, null);
        TargetInput targetInput1 = TestHelper.createTargetInput(targetUrl1, targetLanguageCode1, null, null, null);
        List<TargetInput> targetInputs1 = new ArrayList<>();
        targetInputs1.add(targetInput1);
        BatchRequest batchRequest1 = new BatchRequest(sourceInput1, targetInputs1);

        SourceInput sourceInput2 = TestHelper.createSourceInput(sourceUrl2, null, null, null);
        TargetInput targetInput2 = TestHelper.createTargetInput(targetUrl2, targetLanguageCode2, null, null, null);
        List<TargetInput> targetInputs2 = new ArrayList<>();
        targetInputs2.add(targetInput2);
        BatchRequest batchRequest2 = new BatchRequest(sourceInput2, targetInputs2);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest1, batchRequest2));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();

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
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, filter, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";
        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();

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
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, filter, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";
        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();

        // Validate the response
        validateTranslationStatus(translationStatus, 1);
    }

    @RecordWithoutRequestBody
    @Test
    public void testSingleSourceSingleTargetListDocuments() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";

        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();
        String translationId = translationStatus.getId();

        PagedIterable<DocumentStatus> documentsStatus = documentTranslationClient
                .getDocumentsStatus(translationId);
        assertNotNull(documentsStatus);
        DocumentStatus firstItem = documentsStatus.iterator().next();

        assertEquals(translationStatus.getStatus().toString(), firstItem.getStatus().toString());
        assertEquals(translationStatus.getSummary().getTotalCharacterCharged(), (long) firstItem.getCharacterCharged());
    }

    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatus() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";

        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();
        String translationId = translationStatus.getId();

        PagedIterable<DocumentStatus> response = documentTranslationClient.getDocumentsStatus(translationId);
        assertNotNull(response);

        String documentId = response.iterator().next().getId();
        DocumentStatus documentStatus = documentTranslationClient.getDocumentStatus(translationId, documentId);
        validateDocumentStatus(documentStatus, targetLanguageCode);
    }

    @RecordWithoutRequestBody
    @Test
    public void testWrongSourceRightTarget() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = "https://idont.ex.ist";
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "es";
        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        while ((poller.poll().getValue().getStatus().toString() == "NotStarted") && (retryCount > 0)) {
            try {
                Thread.sleep(10000);
                retryCount--;
            } catch (InterruptedException ex) {
                Logger.getLogger(DocumentTranslationTests.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String status = poller.poll().getValue().getStatus().toString();
        assertEquals(status, "ValidationFailed");

        String innerErrorCode = poller.poll().getValue().getError().getInnerError().getCode().toString();
        assertEquals(innerErrorCode, "InvalidDocumentAccessLevel");
    }

    @RecordWithoutRequestBody
    @Test
    public void testRightSourceWrongTarget() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = "https://idont.ex.ist";
        String targetLanguageCode = "es";
        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        while ((poller.poll().getValue().getStatus().toString() == "NotStarted") && (retryCount > 0)) {
            try {
                Thread.sleep(10000);
                retryCount--;
            } catch (InterruptedException ex) {
                Logger.getLogger(DocumentTranslationTests.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String status = poller.poll().getValue().getStatus().toString();
        assertEquals("ValidationFailed", status);

        String innerErrorCode = poller.poll().getValue().getError().getInnerError().getCode().toString();
        assertEquals("InvalidTargetDocumentAccessLevel", innerErrorCode);
    }

    @RecordWithoutRequestBody
    @Test
    public void testContainerWithSupportedAndUnsupportedFiles() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        List<TestDocument> documents = new ArrayList<>();
        documents.add(new TestDocument("Document1.txt", "First english test document"));
        documents.add(new TestDocument("File2.jpg", "jpg"));
        String sourceUrl = createSourceContainer(documents);

        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";
        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();

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

        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";
        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();

        // Validate the response
        assertNotNull(translationStatus.getId());
        assertEquals("Failed", translationStatus.getStatus().toString());
        assertEquals(1, translationStatus.getSummary().getTotal());
        assertEquals(0, translationStatus.getSummary().getSuccess());
        assertEquals(1, translationStatus.getSummary().getFailed());

        String errorCode = translationStatus.getError().getCode().toString();
        assertEquals("InvalidRequest", errorCode);

        String innerErrorCode = translationStatus.getError().getInnerError().getCode().toString();
        assertEquals("NoTranslatableText", innerErrorCode);
    }

    @RecordWithoutRequestBody
    @Test
    public void testExistingFileInTargetContainer() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(ONE_TEST_DOCUMENTS);
        String targetLanguageCode = "fr";
        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        while ((poller.poll().getValue().getStatus().toString() == "NotStarted") && (retryCount > 0)) {
            try {
                Thread.sleep(10000);
                retryCount--;
            } catch (InterruptedException ex) {
                Logger.getLogger(DocumentTranslationTests.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String status = poller.poll().getValue().getStatus().toString();
        assertEquals("ValidationFailed", status);

        String innerErrorCode = poller.poll().getValue().getError().getInnerError().getCode().toString();
        assertEquals("TargetFileAlreadyExists", innerErrorCode);
    }

    @RecordWithoutRequestBody
    @Test
    public void testGetDocumentStatusWithInvalidGuid() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String sourceUrl = createSourceContainer(ONE_TEST_DOCUMENTS);
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        String targetUrl = createTargetContainer(null);
        String targetLanguageCode = "fr";

        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, null, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();
        String translationId = translationStatus.getId();

        assertThrows(ResourceNotFoundException.class, () -> {
            documentTranslationClient.getDocumentStatus(translationId, "Foo Bar");
        }, "Expected ResourceNotFoundException was not thrown");

        assertThrows(ResourceNotFoundException.class, () -> {
            documentTranslationClient.getDocumentStatus(translationId, " ");
        }, "Expected ResourceNotFoundException was not thrown");
    }

    @RecordWithoutRequestBody
    @Test
    public void testDocumentTranslationWithGlossary() {
        DocumentTranslationClient documentTranslationClient = getDocumentTranslationClient();
        String documentName = "Document1.txt";
        String documentContent = "First english test document";
        List<TestDocument> documents = new ArrayList<TestDocument>() {
            {
                add(new TestDocument(documentName, documentContent));
            }
        };
        String sourceUrl = createSourceContainer(documents);
        SourceInput sourceInput = TestHelper.createSourceInput(sourceUrl, null, null, null);

        Map<String, String> containerValues = createTargetContainerWithClient(null);
        String targetUrl = containerValues.get("sasUri");
        String targetLanguageCode = "fr";

        // Constructing and uploading glossary on the fly
        String glossaryName = "validGlossary.csv";
        // changing the word test --> glossaryTest
        String glossaryContent = "test, glossaryTest";

        String glossaryUrl = createGlossary(new TestDocument(glossaryName, glossaryContent));
        List<Glossary> glossaries = new ArrayList<>();
        glossaries.add(new Glossary(glossaryUrl, "csv"));

        TargetInput targetInput = TestHelper.createTargetInput(targetUrl, targetLanguageCode, null, glossaries, null);
        List<TargetInput> targetInputs = new ArrayList<>();
        targetInputs.add(targetInput);
        BatchRequest batchRequest = new BatchRequest(sourceInput, targetInputs);

        SyncPoller<TranslationStatus, Void> poller = documentTranslationClient
                .beginStartTranslation(TestHelper.getStartTranslationDetails(batchRequest));

        // Wait until the operation completes
        TranslationStatus translationStatus = poller.waitForCompletion().getValue();

        String targetContainerName = containerValues.get("containerName");
        String response = downloadDocumentStream(targetContainerName, documentName);

        // Validate the response
        Assertions.assertTrue(response.contains("glossaryTest"));
    }

    public static void validateTranslationStatus(TranslationStatus translationStatus, int translationCount) {
        assertNotNull(translationStatus.getId());
        assertEquals("Succeeded", translationStatus.getStatus().toString());
        assertEquals(translationCount, translationStatus.getSummary().getTotal());
        assertEquals(translationCount, translationStatus.getSummary().getSuccess());
        assertEquals(0, translationStatus.getSummary().getFailed());
        assertEquals(0, translationStatus.getSummary().getCancelled());
        assertEquals(0, translationStatus.getSummary().getInProgress());
    }

    private void validateDocumentStatus(DocumentStatus documentStatus, String targetLanguageCode) {
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
