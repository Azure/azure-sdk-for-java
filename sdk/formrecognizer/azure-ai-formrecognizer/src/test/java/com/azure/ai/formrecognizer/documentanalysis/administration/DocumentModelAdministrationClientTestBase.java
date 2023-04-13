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
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.AZURE_CLIENT_ID;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.AZURE_FORM_RECOGNIZER_CLIENT_SECRET;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.AZURE_TENANT_ID;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.URL_REGEX;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class DocumentModelAdministrationClientTestBase extends TestProxyTestBase {
    public static final String REDACTED_VALUE = "REDACTED";
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
        DocumentAnalysisAudience audience = TestUtils.getAudience(endpoint);
        DocumentModelAdministrationClientBuilder builder = new DocumentModelAdministrationClientBuilder()
            .endpoint(endpoint)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .audience(audience);

        interceptorManager.addSanitizers(Arrays.asList(
            new TestProxySanitizer("targetModelLocation", REDACTED_VALUE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("targetResourceId", REDACTED_VALUE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("resourceLocation", URL_REGEX, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY)));
        if (useKeyCredential) {
            if (interceptorManager.isPlaybackMode()) {
                builder.credential(new AzureKeyCredential(INVALID_KEY));
                setMatchers();
            } else if (interceptorManager.isRecordMode()) {
                builder.credential(new AzureKeyCredential(TestUtils.AZURE_FORM_RECOGNIZER_API_KEY_CONFIGURATION));
                builder.addPolicy(interceptorManager.getRecordPolicy());
            }
        } else {
            if (interceptorManager.isPlaybackMode()) {
                builder.credential(new TokenCredential() {
                    @Override
                    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
                        return Mono.just(new AccessToken("mockToken", OffsetDateTime.now().plusHours(2)));
                    }
                });
                setMatchers();
            } else if (interceptorManager.isRecordMode()) {
                builder.credential(getCredentialByAuthority(endpoint));
                builder.addPolicy(interceptorManager.getRecordPolicy());
            }
        }
        return builder;
    }

    private void setMatchers() {
        interceptorManager.addMatchers(Arrays.asList(new BodilessMatcher()));
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
        assertNotNull(documentClassifierDetails.getApiVersion());
    }

    void blankPdfDataRunner(BiConsumer<InputStream, Long> testRunner) {
        final long fileLength = new File(TestUtils.LOCAL_FILE_PATH + TestUtils.BLANK_PDF).length();

        try {
            testRunner.accept(new FileInputStream(TestUtils.LOCAL_FILE_PATH + TestUtils.BLANK_PDF), fileLength);
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
