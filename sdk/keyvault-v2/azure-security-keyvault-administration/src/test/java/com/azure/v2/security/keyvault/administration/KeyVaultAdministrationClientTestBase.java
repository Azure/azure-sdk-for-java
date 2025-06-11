// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.test.TestBase;
import com.azure.v2.core.test.models.CustomMatcher;
import com.azure.v2.core.test.models.TestProxyRequestMatcher;
import com.azure.v2.core.test.models.TestProxySanitizer;
import com.azure.v2.core.test.models.TestProxySanitizerType;
import com.azure.v2.core.test.utils.MockTokenCredential;
import com.azure.v2.identity.AzurePowerShellCredentialBuilder;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.administration.implementation.KeyVaultCredentialPolicy;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpRetryOptions;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.http.pipeline.UserAgentPolicy;
import io.clientcore.core.test.TestMode;
import io.clientcore.core.utils.configuration.Configuration;
import io.clientcore.core.utils.CoreUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.provider.Arguments;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class KeyVaultAdministrationClientTestBase extends TestBase {
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-security-keyvault-administration.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }
    protected static final boolean IS_MANAGED_HSM_DEPLOYED
        = Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT") != null;
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
        if (interceptorManager.isLiveMode()) {
            credential = new AzurePowerShellCredentialBuilder().additionallyAllowedTenants("*").build();
        } else if (interceptorManager.isRecordMode()) {
            credential = new DefaultAzureCredentialBuilder().additionallyAllowedTenants("*").build();
            List<TestProxySanitizer> customSanitizers = new ArrayList<>();
            customSanitizers.add(new TestProxySanitizer("token", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            interceptorManager.addSanitizers(customSanitizers);
        } else {
            credential = new MockTokenCredential();

            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new CustomMatcher().setComparingBodies(false)
                .setHeadersKeyOnlyMatch(Collections.singletonList("Accept"))
                .setExcludedHeaders(Arrays.asList("Authorization", "Accept-Language")));
            interceptorManager.addMatchers(customMatchers);
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(
            new UserAgentPolicy(null, CLIENT_NAME, CLIENT_VERSION, Configuration.getGlobalConfiguration().clone()));

        HttpRetryOptions retryOptions = new HttpRetryOptions()
            .setMaxRetries(5)
            .setRetryDelay(Duration.ofSeconds(2))
            .setMaxRetryDelay(Duration.ofSeconds(16));
        policies.add(new HttpRetryPolicy(retryOptions));

        if (credential != null) {
            // If in playback mode, disable the challenge resource verification.
            policies.add(new KeyVaultCredentialPolicy(credential, interceptorManager.isPlaybackMode()));
        }

        if (interceptorManager.isRecordMode() && !forCleanup) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .build();
    }

    public String getEndpoint() {
        final String endpoint = interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
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
