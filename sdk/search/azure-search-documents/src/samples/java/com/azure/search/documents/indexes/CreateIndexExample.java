// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;

public class CreateIndexExample {
    /**
     * From the Azure portal, get your Azure AI Search service name and API key and populate ADMIN_KEY and
     * SEARCH_SERVICE_NAME.
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");

    public static void main(String[] args) {
        // Create the SearchIndex client.
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildClient();

        // Configure the index using SearchFields
        String indexName = "hotels";
        SearchIndex newIndex = new SearchIndex(indexName,
            new SearchField("HotelId", SearchFieldDataType.STRING)
                .setKey(true)
                .setFilterable(true)
                .setSortable(true),
            new SearchField("HotelName", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setFilterable(true)
                .setSortable(true),
            new SearchField("Description", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE),
            new SearchField("DescriptionFr", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE),
            new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                .setSearchable(true)
                .setFilterable(true)
                .setFacetable(true),
            new SearchField("Address", SearchFieldDataType.COMPLEX)
                .setFields(
                    new SearchField("StreetAddress", SearchFieldDataType.STRING)
                        .setSearchable(true),
                    new SearchField("City", SearchFieldDataType.STRING)
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true),
                    new SearchField("StateProvince", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true),
                    new SearchField("Country", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setSynonymMapNames("synonymMapName")
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true),
                    new SearchField("PostalCode", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true)));

        // Create index.
        client.createIndex(newIndex);

        // Cleanup index resource.
        client.deleteIndex(indexName);
    }
}
