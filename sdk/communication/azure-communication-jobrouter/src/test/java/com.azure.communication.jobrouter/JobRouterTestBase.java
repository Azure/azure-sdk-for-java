// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.options.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.options.CreateQueueOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.util.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class JobRouterTestBase extends TestProxyTestBase {
    protected static final String JAVA_LIVE_TESTS = "JAVA_LIVE_TESTS";

    protected String getConnectionString() {
        String connectionString = interceptorManager.isPlaybackMode()
            ? "endpoint=https://REDACTED.int.communication.azure.net;accessKey=secret"
            : Configuration.getGlobalConfiguration().get("AZURE_TEST_JOBROUTER_CONNECTION_STRING");
        Objects.requireNonNull(connectionString);
        return connectionString;
    }

    protected RouterAdministrationClient getRouterAdministrationClient(HttpClient client) {
        HttpPipeline httpPipeline = buildHttpPipeline(client);
        RouterAdministrationClient routerAdministrationClient = new RouterAdministrationClientBuilder()
            .connectionString(getConnectionString())
            .pipeline(httpPipeline)
            .buildClient();
        return routerAdministrationClient;
    }

    protected RouterClient getRouterClient(HttpClient client) {
        HttpPipeline httpPipeline = buildHttpPipeline(client);
        RouterClient routerClient = new RouterClientBuilder()
            .connectionString(getConnectionString())
            .pipeline(httpPipeline)
            .buildClient();
        return routerClient;
    }

    private HttpPipeline buildHttpPipeline(HttpClient httpClient) {
        CommunicationConnectionString connectionString = new CommunicationConnectionString(getConnectionString());
        AzureKeyCredential credential = new AzureKeyCredential(connectionString.getAccessKey());

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(new HmacAuthenticationPolicy(credential));

        policies.add(new RetryPolicy());

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isPlaybackMode()) {
            httpClient = interceptorManager.getPlaybackClient();
            addMatchers();
        }
        if (interceptorManager.isRecordMode()) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    private void addMatchers() {
        interceptorManager.addMatchers(Arrays.asList(new CustomMatcher().setHeadersKeyOnlyMatch(
            Arrays.asList("x-ms-hmac-string-to-sign-base64"))));
    }

    protected JobQueue createQueue(RouterAdministrationClient routerAdminClient, String queueId, String distributionPolicyId) {
        String queueName = String.format("%s-Name", queueId);
        Map<String, LabelValue> queueLabels = new HashMap<String, LabelValue>() {
            {
                put("Label_1", new LabelValue("Value_1"));
            }
        };

        CreateQueueOptions createQueueOptions = new CreateQueueOptions(queueId, distributionPolicyId)
            .setLabels(queueLabels)
            .setName(queueName);

        return routerAdminClient.createQueue(createQueueOptions);
    }

    protected DistributionPolicy createDistributionPolicy(RouterAdministrationClient routerAdminClient, String id) {
        String distributionPolicyName = String.format("%s-Name", id);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            id,
            Duration.ofSeconds(10),
            new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
        )
            .setName(distributionPolicyName);

        return routerAdminClient.createDistributionPolicy(createDistributionPolicyOptions);
    }
}
