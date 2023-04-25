// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class KeyEncryptionKeyClientTestBase extends TestProxyTestBase {
    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";
    protected boolean isHsmEnabled = false;
    protected boolean runManagedHsmTest = false;

    @Override
    protected String getTestName() {
        return "";
    }

    void beforeTestSetup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        TokenCredential credential;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_ID");
            String clientKey = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_SECRET");
            String tenantId = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_TENANT_ID");

            Objects.requireNonNull(clientId, "The client id cannot be null");
            Objects.requireNonNull(clientKey, "The client key cannot be null");
            Objects.requireNonNull(tenantId, "The tenant id cannot be null");

            credential = new ClientSecretCredentialBuilder()
                .clientSecret(clientKey)
                .clientId(clientId)
                .tenantId(tenantId)
                .additionallyAllowedTenants("*")
                .build();
        } else {
            credential = new MockTokenCredential();

            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(
            new UserAgentPolicy(null, SDK_NAME, SDK_VERSION, Configuration.getGlobalConfiguration().clone()));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        RetryStrategy strategy = new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16));

        policies.add(new RetryPolicy(strategy));

        if (credential != null) {
            // If in playback mode, disable the challenge resource verification.
            policies.add(new KeyVaultCredentialPolicy(credential, interceptorManager.isPlaybackMode()));
        }
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isRecordMode()) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .build();
    }

    @Test
    public abstract void wrapUnwrapSymmetricAK128(HttpClient httpClient, CryptographyServiceVersion serviceVersion);

    @Test
    public abstract void wrapUnwrapSymmetricAK128Local();

    @Test
    public abstract void wrapUnwrapSymmetricAK192(HttpClient httpClient, CryptographyServiceVersion serviceVersion);

    @Test
    public abstract void wrapUnwrapSymmetricAK192Local();

    public String getEndpoint() {
        final String endpoint = runManagedHsmTest
            ? Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT", "https://localhost:8080")
            : Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ENDPOINT", "https://localhost:8080");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }

    static void assertRestException(Runnable exceptionThrower, int expectedStatusCode) {
        assertRestException(exceptionThrower, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Runnable exceptionThrower,
                                    Class<? extends HttpResponseException> expectedExceptionType,
                                    int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            assertRestException(ex, expectedExceptionType, expectedStatusCode);
        }
    }

    /**
     * Helper method to verify the error was a HttpRequestException and it has a specific HTTP response code.
     *
     * @param exception Expected error thrown during the test.
     * @param expectedStatusCode Expected HTTP status code contained in the error response.
     */
    static void assertRestException(Throwable exception, int expectedStatusCode) {
        assertRestException(exception, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Throwable exception, Class<? extends HttpResponseException> expectedExceptionType,
                                    int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
    }

    /**
     * Helper method to verify that a command throws an IllegalArgumentException.
     *
     * @param exceptionThrower Command that should throw the exception
     */
    static <T> void assertRunnableThrowsException(Runnable exceptionThrower, Class<T> exception) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Exception ex) {
            assertEquals(exception, ex.getClass());
        }
    }

    public void sleepInRecordMode(long millis) {
        if (interceptorManager.isPlaybackMode()) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
