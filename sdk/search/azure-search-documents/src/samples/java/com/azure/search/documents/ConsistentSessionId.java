// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;


import com.azure.search.documents.models.Hotel;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;

public class ConsistentSessionId {

    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");

    public static void main(String[] args) {
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildClient();

        // To ensure more consistent and unique search results within a user's session, you can use session id.
        // Simply include the sessionId parameter in your queries to create a unique identifier for each user session.
        // This ensures a uniform experience for users throughout their "query session". By consistently using the same
        // sessionId, the system makes a best-effort attempt to target the same replica, improving the overall
        // consistency of search results for users within the specified session.
        SearchOptions searchOptions = new SearchOptions().setSessionId("Session-1").setFilter("Rating gt 3");

        SearchPagedIterable results = searchClient.search("hotel", searchOptions, null);
        results.forEach(result -> System.out.println("Hotel Id: " + result.getDocument(Hotel.class).getHotelId()));
    }
}
