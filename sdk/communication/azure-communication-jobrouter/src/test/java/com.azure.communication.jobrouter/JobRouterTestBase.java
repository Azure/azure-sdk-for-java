// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.LabelOperator;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.WorkerSelector;
import com.azure.communication.jobrouter.models.options.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.options.CreateJobOptions;
import com.azure.communication.jobrouter.models.options.CreateQueueOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

class JobRouterTestBase extends TestBase {
    protected static final String JAVA_LIVE_TESTS = "JAVA_LIVE_TESTS";

    protected String getConnectionString() {
        String connectionString = interceptorManager.isPlaybackMode()
            ? "endpoint=https://REDACTED.int.communication.azure.net;accessKey=secret"
            : Configuration.getGlobalConfiguration().get("AZURE_TEST_JOBROUTER_CONNECTION_STRING");
        Objects.requireNonNull(connectionString);
        return connectionString;
    }

    <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
        HttpClient httpClient;

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

    protected RouterJob createJob(RouterClient routerClient, String queueId) {
        CreateJobOptions createJobOptions = new CreateJobOptions("job-id", "chat-channel", queueId)
            .setPriority(1)
            .setChannelReference("12345")
            .setRequestedWorkerSelectors(
                new ArrayList<WorkerSelector>() {
                    {
                        new WorkerSelector()
                            .setKey("Some-skill")
                            .setLabelOperator(LabelOperator.GREATER_THAN)
                            .setValue(10);
                    }
                }
            );
        return routerClient.createJob(createJobOptions);
    }
}
