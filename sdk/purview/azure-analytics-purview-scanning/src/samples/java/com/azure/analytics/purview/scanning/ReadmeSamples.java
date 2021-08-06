// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.scanning;

import com.azure.identity.DefaultAzureCredentialBuilder;

public class ReadmeSamples {
    public static void main(String[] args) {
        SystemScanRulesetsClient client = new PurviewScanningClientBuilder()
            .endpoint(System.getenv("SCANNING_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildSystemScanRulesetsClient();
    }
}
