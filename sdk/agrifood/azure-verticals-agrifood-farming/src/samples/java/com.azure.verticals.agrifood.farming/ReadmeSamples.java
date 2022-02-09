// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.verticals.agrifood.farming;

import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Code samples for the README.md
 */
public class ReadmeSamples {
    /**
     * Sample for creating low level client.
     */
    public void createClient() {
        // BEGIN: readme-sample-createFarmersBaseClient
        FarmersBaseClient client = new FarmBeatsClientBuilder()
                .endpoint("https://<farmbeats resource name>.farmbeats-dogfood.azure.net")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildFarmersBaseClient();
        // END: readme-sample-createFarmersBaseClient
    }
}
