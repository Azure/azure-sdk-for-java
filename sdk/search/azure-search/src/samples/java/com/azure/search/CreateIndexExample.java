// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.Configuration;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.Index;

import java.util.Arrays;

public class CreateIndexExample {
    /**
     * From the Azure portal, get your Azure Cognitive Search service name and API key and
     * populate ADMIN_KEY and SEARCH_SERVICE_NAME.
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");


    public static void main(String[] args) {
        SearchApiKeyCredential searchApiKeyCredential = new SearchApiKeyCredential(ADMIN_KEY);

        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(searchApiKeyCredential)
            .buildClient();

        Index newIndex = new Index()
            .setName("good-food")
            .setFields(
                Arrays.asList(new Field()
                        .setName("Name")
                        .setType(DataType.EDM_STRING)
                        .setKey(Boolean.TRUE),
                    new Field()
                        .setName("Cuisine")
                        .setType(DataType.EDM_STRING)));

        client.createIndex(newIndex);
    }
}
