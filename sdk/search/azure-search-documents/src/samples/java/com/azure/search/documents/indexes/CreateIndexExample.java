// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;

import java.util.Arrays;

public class CreateIndexExample {
    /**
     * From the Azure portal, get your Azure Cognitive Search service name and API key and populate ADMIN_KEY and
     * SEARCH_SERVICE_NAME.
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");
    private static final String INDEX_NAME = "good-food";

    public static void main(String[] args) {
        AzureKeyCredential searchApiKeyCredential = new AzureKeyCredential(ADMIN_KEY);

        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(searchApiKeyCredential)
            .buildClient();

        SearchIndex newIndex = new SearchIndex(INDEX_NAME,
            Arrays.asList(new SearchField("Name", SearchFieldDataType.STRING).setKey(Boolean.TRUE),
                new SearchField("Cuisine", SearchFieldDataType.STRING)));
        // Create index.
        client.createIndex(newIndex);

        // Cleanup index resource.
        client.deleteIndex(INDEX_NAME);
    }
}
