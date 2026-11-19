// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.search.documents.indexes.models.ListingSearchType;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Demonstrates cursor pagination for indexes using the public synchronous and reactive list APIs.
 */
public class ListIndexesExample {
    private static final String ENDPOINT = System.getenv("SEARCH_ENDPOINT");
    private static final String API_KEY = System.getenv("SEARCH_API_KEY");

    public static void main(String[] args) {
        SearchIndexClient indexClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildClient();
        SearchIndexAsyncClient indexAsyncClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildAsyncClient();

        String prefix = "sample-paged-index-" + UUID.randomUUID().toString().replace("-", "");
        List<String> expectedNames = Arrays.asList(prefix + "-one", prefix + "-two");
        List<String> createdNames = new ArrayList<>();

        try {
            expectedNames.forEach(name -> {
                indexClient.createIndex(createIndex(name));
                createdNames.add(name);
            });

            RequestOptions requestOptions = new RequestOptions().addQueryParam("search", prefix)
                .addQueryParam("pageSize", "1")
                .addQueryParam("searchType", ListingSearchType.PREFIX.toString());

            List<PagedResponse<SearchIndex>> syncPages = new ArrayList<>();
            indexClient.listIndexes(requestOptions).iterableByPage().forEach(syncPages::add);
            verifyPages("synchronous", expectedNames, syncPages);

            List<PagedResponse<SearchIndex>> asyncPages
                = indexAsyncClient.listIndexes(requestOptions).byPage().collectList().block();
            verifyPages("reactive", expectedNames, asyncPages);
        } finally {
            createdNames.forEach(indexClient::deleteIndex);
        }
    }

    private static SearchIndex createIndex(String name) {
        return new SearchIndex(name, new SearchField("id", SearchFieldDataType.STRING).setKey(true));
    }

    private static void verifyPages(String clientType, List<String> expectedNames,
        List<PagedResponse<SearchIndex>> pages) {
        if (pages == null || pages.size() != expectedNames.size()) {
            throw new IllegalStateException("Expected one " + clientType + " page per sample index.");
        }

        Set<String> actualNames = pages.stream()
            .flatMap(page -> page.getElements().stream())
            .map(SearchIndex::getName)
            .collect(Collectors.toSet());
        if (!actualNames.equals(new HashSet<>(expectedNames))) {
            throw new IllegalStateException("The " + clientType + " pager returned missing or unexpected indexes.");
        }
        if (pages.stream().anyMatch(page -> page.getElements().stream().count() != 1)) {
            throw new IllegalStateException("The " + clientType + " pager didn't honor pageSize=1.");
        }
        if (pages.get(0).getContinuationToken() == null) {
            throw new IllegalStateException("The first " + clientType + " page didn't contain a continuation token.");
        }
        if (pages.get(pages.size() - 1).getContinuationToken() != null) {
            throw new IllegalStateException("The final " + clientType + " page contained a continuation token.");
        }

        System.out.println("Verified " + clientType + " cursor pagination across " + pages.size() + " pages.");
    }
}
