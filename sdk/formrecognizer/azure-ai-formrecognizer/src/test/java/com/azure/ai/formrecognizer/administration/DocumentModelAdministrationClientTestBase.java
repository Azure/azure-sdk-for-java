// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;


import com.azure.ai.formrecognizer.DocumentAnalysisServiceVersion;
import com.azure.ai.formrecognizer.TestUtils;
import com.azure.ai.formrecognizer.administration.models.AccountProperties;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.azure.ai.formrecognizer.TestUtils.AZURE_FORM_RECOGNIZER_API_KEY_CONFIGURATION;
import static com.azure.ai.formrecognizer.TestUtils.AZURE_FORM_RECOGNIZER_ENDPOINT_CONFIGURATION;
import static com.azure.ai.formrecognizer.TestUtils.BLANK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.LOCAL_FILE_PATH;
import static com.azure.ai.formrecognizer.TestUtils.ONE_NANO_DURATION;
import static com.azure.ai.formrecognizer.TestUtils.TEST_DATA_PNG;
import static com.azure.ai.formrecognizer.implementation.util.Constants.DEFAULT_POLL_INTERVAL;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class DocumentModelAdministrationClientTestBase extends TestBase {
    Duration durationTestMode;

    /**
     * Use duration of nearly zero value for PLAYBACK test mode, otherwise, use default duration value for LIVE mode.
     */
    @Override
    protected void beforeTest() {
        durationTestMode = interceptorManager.isPlaybackMode() ? ONE_NANO_DURATION : DEFAULT_POLL_INTERVAL;
    }

    DocumentModelAdministrationClientBuilder getDocumentModelAdminClientBuilder(HttpClient httpClient,
                                                                                DocumentAnalysisServiceVersion serviceVersion) {
        DocumentModelAdministrationClientBuilder builder = new DocumentModelAdministrationClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            builder.credential(new AzureKeyCredential(AZURE_FORM_RECOGNIZER_API_KEY_CONFIGURATION));
        }
        return builder;
    }

    static void validateCopyAuthorizationResult(CopyAuthorization actualResult) {
        assertNotNull(actualResult.getTargetModelId());
        assertNotNull(actualResult.getExpiresOn());
        assertNotNull(actualResult.getTargetResourceRegion());
        assertNotNull(actualResult.getTargetResourceId());
        assertNotNull(actualResult.getTargetResourceId());
    }

    static void validateAccountProperties(AccountProperties actualAccountProperties) {
        assertNotNull(actualAccountProperties.getDocumentModelLimit());
        assertNotNull(actualAccountProperties.getDocumentModelCount());
    }

    void validateDocumentModelData(DocumentModel actualCustomModel) {
        assertNotNull(actualCustomModel.getCreatedOn());
        assertNotNull(actualCustomModel.getModelId());

        actualCustomModel.getDocTypes().forEach((s, docTypeInfo) -> assertNotNull(docTypeInfo.getFieldSchema()));
    }

    void blankPdfDataRunner(BiConsumer<InputStream, Long> testRunner) {
        final long fileLength = new File(LOCAL_FILE_PATH + BLANK_PDF).length();

        if (interceptorManager.isPlaybackMode()) {
            testRunner.accept(new ByteArrayInputStream(TEST_DATA_PNG.getBytes(StandardCharsets.UTF_8)), fileLength);
        } else {
            try {
                testRunner.accept(new FileInputStream(LOCAL_FILE_PATH + BLANK_PDF), fileLength);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Local file not found.", e);
            }
        }
    }

    void buildModelRunner(Consumer<String> testRunner) {
        TestUtils.getTrainingDataContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    void multipageTrainingRunner(Consumer<String> testRunner) {
        TestUtils.getMultipageTrainingContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : AZURE_FORM_RECOGNIZER_ENDPOINT_CONFIGURATION;
    }
}
