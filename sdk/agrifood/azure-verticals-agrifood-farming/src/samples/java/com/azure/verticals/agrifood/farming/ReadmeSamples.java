// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.verticals.agrifood.farming;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;

/**
 * Code samples for the README.md
 */
public class ReadmeSamples {
    /**
    * Sample for creating low level client.
    */
    public void createClient() {
        // BEGIN: readme-sample-createPartiesClient
        PartiesClientBuilder builder = new PartiesClientBuilder()
            .endpoint("https://<farmbeats-endpoint>.farmbeats.azure.net");
        PartiesClient partiesClient = builder.buildClient();
        Response<BinaryData> response = partiesClient.getWithResponse("<party-id>", new RequestOptions());
        // END: readme-sample-createPartiesClient
    }
}
