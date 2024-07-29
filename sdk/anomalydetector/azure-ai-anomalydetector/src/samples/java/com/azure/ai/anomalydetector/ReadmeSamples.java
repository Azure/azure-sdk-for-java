// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

public final class ReadmeSamples {

    private static void createClient() {
        // BEGIN: readme-sample-createAnomalyDetectorClient
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_ENDPOINT");
        String key = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_API_KEY");

        MultivariateClient multivariateClient = new AnomalyDetectorClientBuilder()
            .credential(new AzureKeyCredential(key))
            .endpoint(endpoint)
            .buildMultivariateClient();

        UnivariateClient univariateClient = new AnomalyDetectorClientBuilder()
            .credential(new AzureKeyCredential(key))
            .endpoint(endpoint)
            .buildUnivariateClient();
        // END: readme-sample-createAnomalyDetectorClient
    }
}
