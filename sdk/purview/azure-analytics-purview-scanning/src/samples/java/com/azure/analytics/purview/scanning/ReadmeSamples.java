// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.scanning;

import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Code samples for the README.md
 */
public class ReadmeSamples {
    /**
     * Sample for creating low level client.
     */
    public void createClient() {
        // BEGIN: readme-sample-createSystemScanRulesetsClient
        SystemScanRulesetsClient client = new PurviewScanningClientBuilder()
            .endpoint(System.getenv("SCANNING_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildSystemScanRulesetsClient();
        // END: readme-sample-createSystemScanRulesetsClient
    }
}
