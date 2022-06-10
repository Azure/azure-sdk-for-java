// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Code samples for the README.md
 */
public class ReadmeSamples {

    /**
     * Sample for creating async client.
     */
    public void createAsyncClient() {
        // BEGIN: readme-sample-createAnomalyDetectorAsyncClient
        String endpoint = "<anomaly-detector-resource-endpoint>";
        HttpHeaders headers = new HttpHeaders()
            .put("Accept", ContentType.APPLICATION_JSON);

        String defaultScope = "https://cognitiveservices.azure.com/.default";
        HttpPipelinePolicy authPolicy = new BearerTokenAuthenticationPolicy(new DefaultAzureCredentialBuilder().build(),
            defaultScope);
        AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);

        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(HttpClient.createDefault())
            .policies(authPolicy, addHeadersPolicy).build();
        AnomalyDetectorAsyncClient anomalyDetectorAsyncClient = new AnomalyDetectorClientBuilder()
            .pipeline(httpPipeline)
            .endpoint(endpoint)
            .buildAsyncClient();
        // END: readme-sample-createAnomalyDetectorAsyncClient
    }

    /**
     * Sample for creating sync client.
     */
    public void createClient() {
        // BEGIN: readme-sample-createAnomalyDetectorClient
        String endpoint = "<anomaly-detector-resource-endpoint>";
        HttpHeaders headers = new HttpHeaders()
            .put("Accept", ContentType.APPLICATION_JSON);

        String defaultScope = "https://cognitiveservices.azure.com/.default";
        HttpPipelinePolicy authPolicy = new BearerTokenAuthenticationPolicy(new DefaultAzureCredentialBuilder().build(),
            defaultScope);
        AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);

        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(HttpClient.createDefault())
            .policies(authPolicy, addHeadersPolicy).build();
        AnomalyDetectorClient anomalyDetectorClient = new AnomalyDetectorClientBuilder()
            .pipeline(httpPipeline)
            .endpoint(endpoint)
            .buildClient();
        // END: readme-sample-createAnomalyDetectorClient
    }
}
