// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample for deleting experiment metrics
 */
public class DeleteExperimentMetric {

    /**
     * Main method to demonstrate deleting a metric.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        deleteMetric();
    }

    /**
     * Deletes a metric
     */
    public static void deleteMetric() {
        // BEGIN: com.azure.analytics.onlineexperimentation.deletemetric
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        // Delete a metric by ID - removes it from the workspace
        client.deleteMetric("test_metric_id");

        System.out.println("Metric deleted successfully");
        // END: com.azure.analytics.onlineexperimentation.deletemetric
    }
}
