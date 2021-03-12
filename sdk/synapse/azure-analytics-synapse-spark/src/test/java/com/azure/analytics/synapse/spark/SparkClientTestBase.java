// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.spark;

import com.azure.analytics.synapse.spark.models.SparkBatchJob;
import com.azure.analytics.synapse.spark.models.SparkSession;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.*;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class SparkClientTestBase extends TestBase {

    static final String NAME = "name";
    static final String SYNAPSE_PROPERTIES = "azure-analytics-synapse-spark.properties";
    static final String VERSION = "version";
    private final HttpLogOptions httpLogOptions = new HttpLogOptions();
    private final Map<String, String> properties = CoreUtils.getProperties(SYNAPSE_PROPERTIES);
    private final String clientName = properties.getOrDefault(NAME, "UnknownName");
    private final String clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
    private final String fakeSparkPool = "testsparkpool";

    protected String getEndpoint() {
        String endpoint = interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_SYNAPSE_WORKSPACE_URL");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }

    protected String getSparkPoolName() {
        String sparkPoolName = interceptorManager.isPlaybackMode()
            ? fakeSparkPool
            : Configuration.getGlobalConfiguration().get("AZURE_SYNAPSE_SPARK_POOL_NAME");
        Objects.requireNonNull(sparkPoolName);
        return sparkPoolName;
    }

    <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = System.getenv("CLIENT_ID");
            String clientKey = System.getenv("CLIENT_SECRET");
            String tenantId = System.getenv("TENANT_ID");
            Objects.requireNonNull(clientId, "The client id cannot be null");
            Objects.requireNonNull(clientKey, "The client key cannot be null");
            Objects.requireNonNull(tenantId, "The tenant id cannot be null");
            credential = new ClientSecretCredentialBuilder()
                .clientSecret(clientKey)
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
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
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, SparkClientBuilder.DEFAULT_SCOPES));
        }

        policies.add(new RetryPolicy());

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isPlaybackMode()) {
            httpClient = interceptorManager.getPlaybackClient();
        } else {
            httpClient = new NettyAsyncHttpClientBuilder().wiretap(true).build();
        }
        policies.add(interceptorManager.getRecordPolicy());

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        T client;
        client = clientBuilder.apply(pipeline);

        return Objects.requireNonNull(client);
    }

    void validateSparkBatchJob(SparkBatchJob expectedSparkJob, SparkBatchJob actualSparkJob) {
        assertEquals(expectedSparkJob.getName(), actualSparkJob.getName());
        assertEquals(expectedSparkJob.getId(), actualSparkJob.getId());
        assertEquals(expectedSparkJob.getAppId(), actualSparkJob.getAppId());
        assertEquals(expectedSparkJob.getSubmitterId(), actualSparkJob.getSubmitterId());
        assertEquals(expectedSparkJob.getArtifactId(), actualSparkJob.getArtifactId());
    }

    void validateSparkSession(SparkSession expectedSparkSession, SparkSession actualSparkSession) {
        assertEquals(expectedSparkSession.getName(), actualSparkSession.getName());
        assertEquals(expectedSparkSession.getId(), actualSparkSession.getId());
        assertEquals(expectedSparkSession.getAppId(), actualSparkSession.getAppId());
        assertEquals(expectedSparkSession.getSubmitterId(), actualSparkSession.getSubmitterId());
        assertEquals(expectedSparkSession.getArtifactId(), actualSparkSession.getArtifactId());
    }
}
