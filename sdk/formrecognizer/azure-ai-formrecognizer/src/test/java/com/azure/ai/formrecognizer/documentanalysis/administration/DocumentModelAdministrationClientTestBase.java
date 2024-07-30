// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration;


import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisServiceVersion;
import com.azure.ai.formrecognizer.documentanalysis.TestUtils;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelCopyAuthorization;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ResourceDetails;
import com.azure.ai.formrecognizer.documentanalysis.implementation.util.Constants;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnalysisAudience;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.REMOVE_SANITIZER_ID;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class DocumentModelAdministrationClientTestBase extends TestProxyTestBase {
    private static final String BLANK_PDF_PATH = TestUtils.LOCAL_FILE_PATH + TestUtils.BLANK_PDF;
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
                                                                                DocumentAnalysisServiceVersion serviceVersion) {
        String endpoint = getEndpoint();
        DocumentAnalysisAudience audience = TestUtils.getAudience(endpoint);
        DocumentModelAdministrationClientBuilder builder = new DocumentModelAdministrationClientBuilder()
            .endpoint(endpoint)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .audience(audience);


        if (interceptorManager.isPlaybackMode()) {
            builder.credential(new MockTokenCredential());
            setMatchers();
        } else if (interceptorManager.isRecordMode()) {
            builder.credential(getCredentialByAuthority(endpoint));
            builder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (interceptorManager.isLiveMode()) {
            builder.credential(new AzurePowerShellCredentialBuilder().build());
        }
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.removeSanitizers(REMOVE_SANITIZER_ID);
        }
        return builder;
    }

    private void setMatchers() {
        interceptorManager.addMatchers(Collections.singletonList(new BodilessMatcher()));
    }
    static TokenCredential getCredentialByAuthority(String endpoint) {
        return new DefaultAzureCredentialBuilder()
            .authorityHost(TestUtils.getAuthority(endpoint))
            .build();
    }

    static void validateCopyAuthorizationResult(DocumentModelCopyAuthorization actualResult) {
        assertNotNull(actualResult.getTargetModelId());
        assertNotNull(actualResult.getExpiresOn());
        assertNotNull(actualResult.getTargetResourceRegion());
        assertNotNull(actualResult.getTargetResourceId());
        assertNotNull(actualResult.getTargetResourceId());
    }

    static void validateResourceInfo(ResourceDetails actualResourceDetails) {
        assertNotNull(actualResourceDetails.getCustomDocumentModelLimit());
        assertNotNull(actualResourceDetails.getCustomDocumentModelCount());
    }

    void validateDocumentModelData(DocumentModelDetails actualCustomModel) {
        assertNotNull(actualCustomModel.getCreatedOn());
        assertNotNull(actualCustomModel.getModelId());

        actualCustomModel.getDocumentTypes().forEach((s, docTypeInfo) -> assertNotNull(docTypeInfo.getFieldSchema()));
    }

    void validateClassifierModelData(DocumentClassifierDetails documentClassifierDetails) {
        assertNotNull(documentClassifierDetails.getCreatedOn());
        assertNotNull(documentClassifierDetails.getClassifierId());
        assertNotNull(documentClassifierDetails.getServiceVersion());
    }

    void blankPdfDataRunner(BiConsumer<InputStream, Long> testRunner) {
        final long fileLength = new File(BLANK_PDF_PATH).length();

        try {
            testRunner.accept(new FileInputStream(BLANK_PDF_PATH), fileLength);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Local file not found.", e);
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

    void beginClassifierRunner(Consumer<String> testRunner) {
        TestUtils.getClassifierTrainingDataContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    void selectionMarkTrainingRunner(Consumer<String> testRunner) {
        TestUtils.getSelectionMarkTrainingContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    private String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : TestUtils.AZURE_FORM_RECOGNIZER_ENDPOINT_CONFIGURATION;
    }
}
