// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.artifacts;

import com.azure.analytics.synapse.artifacts.models.NotebookResource;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class ArtifactsClientTestBase extends TestProxyTestBase {

    static final String NAME = "name";
    static final String SYNAPSE_PROPERTIES = "azure-analytics-synapse-artifacts.properties";
    static final String VERSION = "version";
    private final HttpLogOptions httpLogOptions = new HttpLogOptions();
    private final Map<String, String> properties = CoreUtils.getProperties(SYNAPSE_PROPERTIES);
    private final String clientName = properties.getOrDefault(NAME, "UnknownName");
    private final String clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
    // Disable `name` and `id` sanitizers from the list of common sanitizers
    public static final String[] REMOVE_SANITIZER_ID = {"AZSDK3430", "AZSDK3493"};

    private static final String[] DEFAULT_SCOPES = new String[] {"https://dev.azuresynapse.net/.default"};

    ArtifactsClientBuilder getArtifactsClientBuilder() {
        return clientSetup(httpPipeline -> new ArtifactsClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline));
    }

    protected String getEndpoint() {
        String endpoint = interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_SYNAPSE_WORKSPACE_URL");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }

    <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
        TokenCredential credential;

        switch (getTestMode()) {
            case RECORD:
                credential = new DefaultAzureCredentialBuilder().build();

                break;
            case LIVE:
                Configuration config = Configuration.getGlobalConfiguration();

                ChainedTokenCredentialBuilder chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder()
                    .addLast(new EnvironmentCredentialBuilder().build())
                    .addLast(new AzureCliCredentialBuilder().build())
                    .addLast(new AzureDeveloperCliCredentialBuilder().build())
                    .addLast(new AzurePowerShellCredentialBuilder().build());

                String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
                String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
                String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
                String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");

                if (!CoreUtils.isNullOrEmpty(serviceConnectionId)
                    && !CoreUtils.isNullOrEmpty(clientId)
                    && !CoreUtils.isNullOrEmpty(tenantId)
                    && !CoreUtils.isNullOrEmpty(systemAccessToken)) {

                    chainedTokenCredentialBuilder.addLast(new AzurePipelinesCredentialBuilder()
                        .systemAccessToken(systemAccessToken)
                        .clientId(clientId)
                        .tenantId(tenantId)
                        .serviceConnectionId(serviceConnectionId)
                        .build());
                }

                credential = chainedTokenCredentialBuilder.build();

                break;
            default:
                // On PLAYBACK mode
                credential = new MockTokenCredential();

                break;
        }

        HttpClient httpClient;
        Configuration buildConfiguration = Configuration.getGlobalConfiguration().clone();

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new BearerTokenAuthenticationPolicy(credential, DEFAULT_SCOPES));
        policies.add(new RetryPolicy());

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isPlaybackMode()) {
            httpClient = interceptorManager.getPlaybackClient();
        } else {
            httpClient = new NettyAsyncHttpClientBuilder().wiretap(true).build();
        }

        if (interceptorManager.isRecordMode()) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(new TestProxySanitizer("$..id", null, "00000000-0000-0000-0000-000000000000", TestProxySanitizerType.BODY_KEY));
            interceptorManager.removeSanitizers(REMOVE_SANITIZER_ID);
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        T client;
        client = clientBuilder.apply(pipeline);

        return Objects.requireNonNull(client);
    }

    void validateNotebook(NotebookResource expectedNotebook, NotebookResource actualNotebook) {
        assertEquals(expectedNotebook.getName(), actualNotebook.getName());
        assertEquals(expectedNotebook.getId(), actualNotebook.getId());
        assertEquals(expectedNotebook.getProperties().getDescription(), actualNotebook.getProperties().getDescription());
    }
}
