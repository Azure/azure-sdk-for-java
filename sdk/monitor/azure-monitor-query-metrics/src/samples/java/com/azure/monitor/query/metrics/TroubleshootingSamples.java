// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class TroubleshootingSamples {
    public void enableHttpLogging() {
        // BEGIN: readme-sample-enablehttplogging
        // Enable HTTP logging for troubleshooting
        MetricsClient metricsClient = new MetricsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: readme-sample-enablehttplogging
    }
}
