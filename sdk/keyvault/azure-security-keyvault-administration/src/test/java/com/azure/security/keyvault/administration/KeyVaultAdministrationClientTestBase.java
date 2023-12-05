// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.credential.TokenCredential;
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
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.administration.implementation.KeyVaultCredentialPolicy;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.provider.Arguments;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class KeyVaultAdministrationClientTestBase extends TestProxyTestBase {
    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";
    protected static final boolean IS_MANAGED_HSM_DEPLOYED =
        Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT") != null;
    static final String DISPLAY_NAME = "{displayName}";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        Assumptions.assumeTrue(IS_MANAGED_HSM_DEPLOYED || getTestMode() == TestMode.PLAYBACK);
        KeyVaultCredentialPolicy.clearCache();
    }

    @Override
    protected String getTestName() {
        return "";
    }

    HttpPipeline getPipeline(HttpClient httpClient, boolean forCleanup) {
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

            if (interceptorManager.isRecordMode()) {
                List<TestProxySanitizer> customSanitizers = new ArrayList<>();
                customSanitizers.add(new TestProxySanitizer("token", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
                interceptorManager.addSanitizers(customSanitizers);
            }
        } else {
            credential = new MockTokenCredential();

            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(null, SDK_NAME, SDK_VERSION, Configuration.getGlobalConfiguration().clone()));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        RetryStrategy strategy = new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16));
        policies.add(new RetryPolicy(strategy));

        if (credential != null) {
            // If in playback mode, disable the challenge resource verification.
            policies.add(new KeyVaultCredentialPolicy(credential, interceptorManager.isPlaybackMode()));
        }

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isRecordMode() && !forCleanup) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .build();
    }

    public String getEndpoint() {
        final String endpoint = interceptorManager.isPlaybackMode() ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT");

        Objects.requireNonNull(endpoint);

        return endpoint;
    }

    /**
     * Returns a stream of arguments that includes all eligible {@link HttpClient HttpClients}.
     *
     * @return A stream of {@link HttpClient HTTP clients} to test.
     */
    static Stream<Arguments> createHttpClients() {
        return TestBase.getHttpClients().map(Arguments::of);
    }
}
