// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedFluxBase;
import com.azure.core.implementation.serializer.jsonwrapper.JsonWrapper;
import com.azure.core.implementation.serializer.jsonwrapper.api.JsonApi;
import com.azure.core.implementation.serializer.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.core.util.Configuration;
import com.azure.search.models.SearchResult;
import com.azure.search.models.Hotel;

/**
 * This example shows how to convert a search result into a fully typed object.
 * In case that the searched document schema is known, the user can convert the result of the search query from a property bag into a fully typed object
 * Conversion to a well known type is done using azure-core jsonwrapper {@link com.azure.core.implementation.serializer.jsonwrapper.jacksonwrapper.JacksonDeserializer} utility with a Jackson implementation
 * <p>
 * This sample is based on the hotels-sample index available to install from the portal.
 * See https://docs.microsoft.com/en-us/azure/search/search-get-started-portal
 */
public class SearchAsyncWithFullyTypedDocumentsExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");

    private static final String INDEX_NAME = "hotels-sample-index";

    public static void main(String[] args) {
        SearchIndexAsyncClient searchClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName(INDEX_NAME)
            .buildAsyncClient();

        // Create an instance of JsonAPI with Jackson deserializer
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configureTimezone();

        PagedFluxBase<SearchResult, SearchPagedResponse> results = searchClient.search("searchText");
        results
            .subscribe(item -> {
                Document document = item.getDocument();
                // Convert the property bag received from the search query to an object of type Hotel
                Hotel hotel = jsonApi.convertObjectToType(document, Hotel.class);
                System.out.println("Hotel " + hotel.getHotelId());
            });

        results.blockLast();

    }
}
