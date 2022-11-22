// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;

public final class ReadmeSamples {

    private static void createClient() {
        // BEGIN: readme-sample-createAnomalyDetectorClient
        String endpoint = "<anomaly-detector-resource-endpoint>";
        String key = "<anomaly-detector-resource-key>";

        HttpHeaders headers = new HttpHeaders()
            .put("Accept", ContentType.APPLICATION_JSON);

        HttpPipelinePolicy authPolicy = new AzureKeyCredentialPolicy("Ocp-Apim-Subscription-Key",
            new AzureKeyCredential(key));
        AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);

        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(HttpClient.createDefault())
            .policies(authPolicy, addHeadersPolicy).build();
        // Instantiate a client that will be used to call the service.
        HttpLogOptions httpLogOptions = new HttpLogOptions();
        httpLogOptions.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);

        AnomalyDetectorClient anomalyDetectorClient = new AnomalyDetectorClientBuilder()
            .pipeline(httpPipeline)
            .endpoint(endpoint)
            .httpLogOptions(httpLogOptions)
            .buildClient();
        // END: readme-sample-createAnomalyDetectorClient
    }
}
