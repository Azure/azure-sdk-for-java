// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.catalog;

import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Code samples for the README.md
 */
public class ReadmeSamples {
    /**
     * Sample for creating low level client.
     */
    public void createClient() {
        // BEGIN: readme-sample-createGlossaryClient
        GlossaryClient client = new PurviewCatalogClientBuilder()
            .endpoint(System.getenv("<account-name>.purview.azure.com"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildGlossaryClient();
        // END: readme-sample-createGlossaryClient
    }
}
