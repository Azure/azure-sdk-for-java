// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates;

import io.clientcore.core.credential.TokenCredential;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.policy.ExponentialBackoffOptions;
import io.clientcore.core.http.policy.FixedDelayOptions;
import io.clientcore.core.http.policy.HttpRetryOptions;
import com.azure.v2.core.test.TestBase;
import com.azure.v2.core.test.models.BodilessMatcher;
import com.azure.v2.core.test.models.CustomMatcher;
import com.azure.v2.core.test.models.TestRequestMatcher;
import com.azure.v2.core.test.models.TestSanitizer;
import com.azure.v2.core.test.models.TestSanitizerType;
import com.azure.v2.core.test.utils.MockTokenCredential;
import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.core.util.CoreUtils;
import com.azure.v2.identity.AzurePowerShellCredentialBuilder;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.certificates.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.certificates.models.AdministratorContact;
import com.azure.v2.security.keyvault.certificates.models.CertificateContact;
import com.azure.v2.security.keyvault.certificates.models.CertificateContentType;
import com.azure.v2.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.v2.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.v2.security.keyvault.certificates.models.CertificateKeyType;
import com.azure.v2.security.keyvault.certificates.models.CertificateKeyUsage;
import com.azure.v2.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.v2.security.keyvault.certificates.models.CertificatePolicyAction;
import com.azure.v2.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.v2.security.keyvault.certificates.models.LifetimeAction;
import com.azure.v2.security.keyvault.certificates.models.WellKnownIssuerNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class CertificateClientTestBase extends TestBase {
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String AZURE_KEYVAULT_TEST_CERTIFICATE_SERVICE_VERSIONS
        = "AZURE_KEYVAULT_TEST_CERTIFICATE_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV
        = Configuration.getGlobalConfiguration().get(AZURE_KEYVAULT_TEST_CERTIFICATE_SERVICE_VERSIONS);
    private static final String AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL = "ALL";
    private static final String TEST_CERTIFICATE_NAME = "testCert";

    private static final int MAX_RETRIES = 5;
    private static final HttpRetryOptions LIVE_RETRY_OPTIONS
        = new HttpRetryOptions(new ExponentialBackoffOptions().setMaxRetries(MAX_RETRIES)
            .setBaseDelay(Duration.ofSeconds(2))
            .setMaxDelay(Duration.ofSeconds(16)));

    private static final HttpRetryOptions PLAYBACK_RETRY_OPTIONS
        = new HttpRetryOptions(new FixedDelayOptions(MAX_RETRIES, Duration.ofMillis(1)));

    void beforeTestSetup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    CertificateClientBuilder getCertificateClientBuilder(HttpClient httpClient, String testTenantId, String endpoint,
        CertificateServiceVersion serviceVersion) {
        TokenCredential credential;

        if (interceptorManager.isLiveMode()) {
            credential = new AzurePowerShellCredentialBuilder().additionallyAllowedTenants("*").build();
        } else if (interceptorManager.isRecordMode()) {
            credential = new DefaultAzureCredentialBuilder().additionallyAllowedTenants("*").build();
            List<TestSanitizer> customSanitizers = new ArrayList<>();
            customSanitizers.add(
                new TestSanitizer("value", "-----BEGIN PRIVATE KEY-----\\n(.+\\n)*-----END PRIVATE KEY-----\\n",
                    "-----BEGIN PRIVATE KEY-----\\nREDACTED\\n-----END PRIVATE KEY-----\\n",
                    TestSanitizerType.BODY_KEY));
            interceptorManager.addSanitizers(customSanitizers);
        } else {
            credential = new MockTokenCredential();

            List<TestRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
        }

        CertificateClientBuilder builder = new CertificateClientBuilder().vaultUrl(endpoint)
            .serviceVersion(serviceVersion)
            .credential(credential)
            .httpClient(httpClient);

        if (!interceptorManager.isLiveMode()) {
            // Remove `id` and `name` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493");
        }

        if (interceptorManager.isPlaybackMode()) {
            return builder.retryOptions(PLAYBACK_RETRY_OPTIONS);
        } else {
            builder.retryOptions(LIVE_RETRY_OPTIONS);

            return interceptorManager.isRecordMode()
                ? builder.addPolicy(interceptorManager.getRecordPolicy())
                : builder;
        }
    }

    @Test
    public abstract void createCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    static void createCertificateRunner(Consumer<CertificatePolicy> testRunner) {
        final CertificatePolicy certificatePolicy = CertificatePolicy.getDefault();
        testRunner.accept(certificatePolicy);
    }

    @Test
    public abstract void updateCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void getCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void deleteCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void getDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void recoverDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void purgeDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void listCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void listCertificateVersions(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void listDeletedCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    static void updateCertificateRunner(BiConsumer<Map<String, String>, Map<String, String>> testRunner) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");

        final Map<String, String> updatedTags = new HashMap<>();
        tags.put("first tag", "first value");
        tags.put("second tag", "second value");

        testRunner.accept(tags, updatedTags);
    }

    public static Stream<Arguments> getTestParameters() {
        // Combine all possible HttpClient and service version combinations
        List<Arguments> argumentsList = new ArrayList<>();

        TestUtils.getHttpClients().forEach(httpClient ->
            Arrays.stream(CertificateServiceVersion.values()).forEach(serviceVersion ->
                argumentsList.add(Arguments.of(httpClient, serviceVersion))));

        return argumentsList.stream();
    }

    String getEndpoint() {
        final String customEndpointEnv = "AZURE_KEYVAULT_ENDPOINT";
        String customEndpoint = interceptorManager.isPlaybackMode() ? "https://localhost:8080"
            : System.getenv(customEndpointEnv);
        if (CoreUtils.isNullOrEmpty(customEndpoint)) {
            customEndpoint = "https://azure-sdk-tests.vault.azure.net/";
        }

        return customEndpoint;
    }

    static void sleepIfRunningAgainstService(long millis) {
        if (getTestMode() == TestMode.LIVE) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
