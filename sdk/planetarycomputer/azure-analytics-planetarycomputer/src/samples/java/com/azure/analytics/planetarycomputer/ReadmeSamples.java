// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.SharedAccessSignatureToken;
import com.azure.analytics.planetarycomputer.models.StacCollection;
import com.azure.analytics.planetarycomputer.models.StacItemCollection;
import com.azure.analytics.planetarycomputer.models.StacSearchParameters;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;

public final class ReadmeSamples {
    public void createStacClient() {
        // BEGIN: readme-sample-createStacClient
        StacClient stacClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacClient();
        // END: readme-sample-createStacClient
    }

    public void createStacAsyncClient() {
        // BEGIN: readme-sample-createStacAsyncClient
        StacAsyncClient stacAsyncClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacAsyncClient();
        // END: readme-sample-createStacAsyncClient
    }

    public void getCollection() {
        StacClient stacClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacClient();

        // BEGIN: readme-sample-getCollection
        StacCollection collection = stacClient.getCollection("naip-atl", null, null);
        System.out.printf("Collection ID: %s, Description: %s%n",
            collection.getId(), collection.getDescription());
        // END: readme-sample-getCollection
    }

    public void searchItems() {
        StacClient stacClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacClient();

        // BEGIN: readme-sample-searchItems
        StacItemCollection results = stacClient.search(
            new StacSearchParameters()
                .setCollections(Arrays.asList("naip-atl"))
                .setDatetime("2021-01-01T00:00:00Z/2022-12-31T00:00:00Z")
                .setLimit(10),
            null, null);
        System.out.printf("Found %d items%n", results.getFeatures().size());
        // END: readme-sample-searchItems
    }

    public void getToken() {
        // BEGIN: readme-sample-getToken
        SharedAccessSignatureClient sasClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildSharedAccessSignatureClient();
        SharedAccessSignatureToken token = sasClient.getToken("naip-atl", null);
        // END: readme-sample-getToken
    }
}
