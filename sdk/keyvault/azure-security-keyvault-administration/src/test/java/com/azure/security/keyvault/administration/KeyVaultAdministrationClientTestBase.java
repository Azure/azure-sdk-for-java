// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
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
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.administration.implementation.KeyVaultCredentialPolicy;
import org.junit.jupiter.params.provider.Arguments;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class KeyVaultAdministrationClientTestBase extends TestBase {
    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";
    static final String DISPLAY_NAME = "{displayName}";

    @Override
    protected String getTestName() {
        return "";
    }

    void beforeTestSetup() {
    }

    protected List<HttpPipelinePolicy> getPolicies() {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = System.getenv("ARM_CLIENTID");
            String clientKey = System.getenv("ARM_CLIENTKEY");
            String tenantId = System.getenv("AZURE_TENANT_ID");

            Objects.requireNonNull(clientId, "The client id cannot be null");
            Objects.requireNonNull(clientKey, "The client key cannot be null");
            Objects.requireNonNull(tenantId, "The tenant id cannot be null");

            credential = new ClientSecretCredentialBuilder()
                .clientSecret(clientKey)
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(null, SDK_NAME, SDK_VERSION, Configuration.getGlobalConfiguration().clone()));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        RetryStrategy strategy = new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16));
        policies.add(new RetryPolicy(strategy));

        if (credential != null) {
            policies.add(new KeyVaultCredentialPolicy(credential));
        }

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        return policies;
    }

    public String getEndpoint() {
        final String endpoint = interceptorManager.isPlaybackMode() ? "http://localhost:8080"
            : System.getenv("AZURE_KEYVAULT_ENDPOINT");
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
