// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;


import com.azure.ai.formrecognizer.DocumentAnalysisServiceVersion;
import com.azure.ai.formrecognizer.TestUtils;
import com.azure.ai.formrecognizer.administration.models.AccountProperties;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.ai.formrecognizer.implementation.util.Constants;
import com.azure.ai.formrecognizer.models.FormRecognizerAudience;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.azure.ai.formrecognizer.TestUtils.AZURE_CLIENT_ID;
import static com.azure.ai.formrecognizer.TestUtils.AZURE_FORM_RECOGNIZER_CLIENT_SECRET;
import static com.azure.ai.formrecognizer.TestUtils.AZURE_TENANT_ID;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class DocumentModelAdministrationClientTestBase extends TestBase {
    Duration durationTestMode;

    /**
     * Use duration of nearly zero value for PLAYBACK test mode, otherwise, use default duration value for LIVE mode.
     */
    @Override
    protected void beforeTest() {
        durationTestMode = interceptorManager.isPlaybackMode()
            ? TestUtils.ONE_NANO_DURATION : Constants.DEFAULT_POLL_INTERVAL;
    }

    DocumentModelAdministrationClientBuilder getDocumentModelAdminClientBuilder(HttpClient httpClient,
                                                                                DocumentAnalysisServiceVersion serviceVersion,
                                                                                boolean useKeyCredential) {
        String endpoint = getEndpoint();
        FormRecognizerAudience audience = TestUtils.getAudience(endpoint);
        DocumentModelAdministrationClientBuilder builder = new DocumentModelAdministrationClientBuilder()
            .endpoint(endpoint)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy())
            .audience(audience);

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            if (useKeyCredential) {
                builder.credential(new AzureKeyCredential(TestUtils.AZURE_FORM_RECOGNIZER_API_KEY_CONFIGURATION));
            } else {
                builder.credential(getCredentialByAuthority(endpoint));
            }
        }
        return builder;
    }

    static TokenCredential getCredentialByAuthority(String endpoint) {
        String authority = TestUtils.getAuthority(endpoint);
        if (authority == AzureAuthorityHosts.AZURE_PUBLIC_CLOUD) {
            return new DefaultAzureCredentialBuilder()
                .authorityHost(TestUtils.getAuthority(endpoint))
                .build();
        } else {
            return new ClientSecretCredentialBuilder()
                .tenantId(AZURE_TENANT_ID)
                .clientId(AZURE_CLIENT_ID)
                .clientSecret(AZURE_FORM_RECOGNIZER_CLIENT_SECRET)
                .authorityHost(authority)
                .build();
        }
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
        final long fileLength = new File(TestUtils.LOCAL_FILE_PATH + TestUtils.BLANK_PDF).length();

        if (interceptorManager.isPlaybackMode()) {
            testRunner.accept(new ByteArrayInputStream(TestUtils.TEST_DATA_PNG.getBytes(StandardCharsets.UTF_8)), fileLength);
        } else {
            try {
                testRunner.accept(new FileInputStream(TestUtils.LOCAL_FILE_PATH + TestUtils.BLANK_PDF), fileLength);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Local file not found.", e);
            }
        }
    }

    void buildModelRunner(Consumer<String> testRunner) {
        TestUtils.getTrainingDataContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    void buildModelErrorRunner(Consumer<String> testRunner) {
        TestUtils.getErrorTrainingDataContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    void multipageTrainingRunner(Consumer<String> testRunner) {
        TestUtils.getMultipageTrainingContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    private String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : TestUtils.AZURE_FORM_RECOGNIZER_ENDPOINT_CONFIGURATION;
    }
}
