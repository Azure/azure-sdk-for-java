// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.ONE_NANO_DURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Class to test client side validations for Document Analysis clients.
 */
public class DocumentModelAdminClientUnitTest {

    private static DocumentModelAdministrationClient client;
    private static DocumentModelAdministrationAsyncClient asyncClient;
    private static DocumentAnalysisClient analysisClient;
    private static DocumentAnalysisAsyncClient analysisAsyncClient;
    private static final String MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE
        = "'modelId' is required and cannot be null or" + " empty";

    @BeforeAll
    protected static void beforeTest() {
        DocumentAnalysisClientBuilder builder = new DocumentAnalysisClientBuilder().endpoint("https://localhost:8080")
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
            .credential(new AzureKeyCredential("fakeKey"));

        analysisClient = builder.buildClient();
        analysisAsyncClient = builder.buildAsyncClient();

        DocumentModelAdministrationClientBuilder adminBuilder
            = new DocumentModelAdministrationClientBuilder().endpoint("https://localhost:8080")
                .httpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
                .credential(new AzureKeyCredential("fakeKey"));

        client = adminBuilder.buildClient();
        asyncClient = adminBuilder.buildAsyncClient();
    }

    /**
     * Verifies that an exception is thrown for null model ID parameter.
     */
    @SyncAsyncTest
    public void getModelNullModelID() {
        assertThrows(IllegalArgumentException.class, () -> SyncAsyncExtension
            .execute(() -> client.getDocumentModel(null), () -> asyncClient.getDocumentModel(null)));
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void analyzeReceiptDataNullDataSync() {
        Assertions.assertThrows(NullPointerException.class, () -> analysisClient.beginAnalyzeDocument(null, null));
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void analyzeBusinessCardDataNullDataSync() {
        Assertions.assertThrows(NullPointerException.class,
            () -> analysisClient.beginAnalyzeDocument("prebuilt-businessCard", null));
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void analyzeContentResultWithNullData() {
        Assertions.assertThrows(NullPointerException.class,
            () -> analysisAsyncClient.beginAnalyzeDocument("prebuilt-layout", null)
                .setPollInterval(ONE_NANO_DURATION)
                .getSyncPoller());
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void analyzeIDDocumentDataNullDataSync() {
        Assertions.assertThrows(NullPointerException.class,
            () -> analysisClient.beginAnalyzeDocument("prebuilt-idDocument", null));
    }

    /**
     * Verifies an exception thrown for a document using null model id.
     */
    @Test
    public void analyzeCustomDocumentWithNullModelId() {
        Exception ex = Assertions.assertThrows(RuntimeException.class,
            () -> analysisClient.beginAnalyzeDocument(null, BinaryData.fromBytes("HelloWorld".getBytes()))
                .setPollInterval(ONE_NANO_DURATION));
        Assertions.assertEquals(MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void analyzeReceiptDataNullData() {
        assertThrows(NullPointerException.class,
            () -> analysisAsyncClient.beginAnalyzeDocument("prebuilt-receipt", null)
                .setPollInterval(ONE_NANO_DURATION)
                .getSyncPoller());
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void analyzeBusinessCardDataNullData() {
        assertThrows(NullPointerException.class,
            () -> analysisAsyncClient.beginAnalyzeDocument("prebuilt-businessCard", null)
                .setPollInterval(ONE_NANO_DURATION)
                .getSyncPoller());
    }

    /**
     * Verifies that an exception is thrown for null source url input.
     */
    @Test
    public void beginBuildModelNullInput() {
        Exception exception = assertThrows(NullPointerException.class,
            () -> client.beginBuildDocumentModel((String) null, DocumentModelBuildMode.TEMPLATE));
        assertEquals("'blobContainerUrl' cannot be null.", exception.getMessage());
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void analyzeContentResultWithNullDataSync() {
        assertThrows(NullPointerException.class,
            () -> analysisAsyncClient.beginAnalyzeDocument("prebuilt-layout", null)
                .setPollInterval(ONE_NANO_DURATION)
                .getSyncPoller());
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void analyzeIDDocumentDataNullData() {
        assertThrows(NullPointerException.class,
            () -> analysisAsyncClient.beginAnalyzeDocument("prebuilt-idDocument", null)
                .setPollInterval(ONE_NANO_DURATION)
                .getSyncPoller());
    }
}
