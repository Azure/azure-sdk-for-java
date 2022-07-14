// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.authentication.CommunicationConnectionString;
import com.azure.communication.jobrouter.implementation.authentication.HmacAuthenticationPolicy;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

class JobRouterClientTestBase extends TestBase {
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
}
