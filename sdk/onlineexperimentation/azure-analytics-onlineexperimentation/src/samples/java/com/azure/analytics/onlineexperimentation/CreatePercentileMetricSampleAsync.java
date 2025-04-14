// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.*;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Sample for creating a percentile metric asynchronously
 */
public class CreatePercentileMetricSampleAsync {

    /**
     * Main method to demonstrate creating a percentile metric asynchronously.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        createPercentileMetricAsync()
            .block(); // Wait for the operation to complete
    }

    /**
     * Creates a percentile metric asynchronously
     * @return A Mono containing the created metric
     */
    public static Mono<ExperimentMetric> createPercentileMetricAsync() {
        // BEGIN: com.azure.analytics.onlineexperimentation.createpercentilemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        // Define the Percentile metric - calculates a specific percentile of a numeric value
        ExperimentMetric p95ResponseTimeMetric = new ExperimentMetric(
            LifecycleStage.ACTIVE,
            "P95 LLM response time [seconds]",
            "The 95th percentile of response time in seconds for LLM responses",
            Arrays.asList("Performance"),
            DesiredDirection.DECREASE,
            new PercentileMetricDefinition("ResponseReceived", "ResponseTimeSeconds", 95)
        );

        // Create the metric asynchronously
        return client.createOrUpdateMetric("p95_response_time_seconds", p95ResponseTimeMetric)
            .doOnNext(response -> {
                System.out.printf("Created metric: %s%n", response.getId());
            });
        // END: com.azure.analytics.onlineexperimentation.createpercentilemetricasync
    }
}
