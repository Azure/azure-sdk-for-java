// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerProvider;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.FieldBuilderOptions;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.models.Hotel;

import java.util.List;

public class FieldBuilderExample {

    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");

    public static void main(String[] args) {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildClient();

        JacksonJsonSerializer serializer = new JacksonJsonSerializerProvider().createInstance();
        FieldBuilderOptions options = new FieldBuilderOptions().setJsonSerializer(serializer);
        // Prepare the hotel index schema. The schema pull from Hotel.java.
        // If you don't want to use the default Jackson serializer, pass null for serializer param.
        List<SearchField> searchFields = SearchIndexClient.buildSearchFields(Hotel.class, options);

        searchIndexClient.createIndex(new SearchIndex("hotel", searchFields));
    }
}
